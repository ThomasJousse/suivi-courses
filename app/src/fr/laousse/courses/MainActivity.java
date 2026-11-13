package fr.laousse.courses;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends Activity {
    private WebView web;
    private final ExecutorService pool = Executors.newFixedThreadPool(4);
    private final Handler main = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        web = new WebView(this);
        setContentView(web);
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setAllowFileAccess(true);
        web.addJavascriptInterface(new Bridge(), "Native");
        web.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView v, WebResourceRequest r) {
                Uri u = r.getUrl();
                if ("file".equals(u.getScheme())) return false;
                openExternal(u.toString());
                return true;
            }
        });
        web.loadUrl("file:///android_asset/www/index.html");
    }

    private void openExternal(String url) {
        try {
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
        } catch (Exception ignored) { }
    }

    private static String fetch(String url) throws Exception {
        for (int hop = 0; hop < 6; hop++) {
            HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
            c.setConnectTimeout(15000);
            c.setReadTimeout(20000);
            c.setInstanceFollowRedirects(false);
            c.setRequestProperty("User-Agent", "SuiviCourses Android");
            c.setRequestProperty("Accept", "application/json");
            int code = c.getResponseCode();
            if (code >= 300 && code < 400) {
                String loc = c.getHeaderField("Location");
                c.disconnect();
                if (loc == null) throw new Exception("Redirection sans destination");
                url = new URL(new URL(url), loc).toString();
                continue;
            }
            if (code != 200) { c.disconnect(); throw new Exception("HTTP " + code); }
            InputStream in = c.getInputStream();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buf = new byte[16384];
            int n;
            while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
            in.close();
            c.disconnect();
            return out.toString("UTF-8");
        }
        throw new Exception("Trop de redirections");
    }

    private void callback(String id, boolean ok, String body) {
        final String js = "window.__nativeCb(" + JSONObject.quote(id) + "," + ok + "," + JSONObject.quote(body) + ")";
        main.post(() -> web.evaluateJavascript(js, null));
    }

    class Bridge {
        @JavascriptInterface
        public void get(final String url, final String id) {
            if (!(url.startsWith("https://script.google.com/") || url.startsWith("https://api.github.com/"))) { callback(id, false, "URL refusée"); return; }
            pool.execute(() -> {
                try { callback(id, true, fetch(url)); }
                catch (Exception e) { callback(id, false, String.valueOf(e.getMessage())); }
            });
        }
        @JavascriptInterface
        public void open(final String url) {
            if (url.startsWith("https://")) main.post(() -> openExternal(url));
        }
        @JavascriptInterface
        public void setBar(final String hex) {
            main.post(() -> {
                try { int c = Color.parseColor(hex); getWindow().setStatusBarColor(c); getWindow().setNavigationBarColor(c); }
                catch (Exception ignored) { }
            });
        }
    }

    @Override
    protected void onDestroy() { pool.shutdownNow(); super.onDestroy(); }
}
