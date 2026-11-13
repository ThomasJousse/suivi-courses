#!/bin/bash
# Compile, aligne et signe l'APK sans Gradle (aapt2 + javac + d8 + apksigner).
# Lancer après `node scripts/build.mjs` (génère config.js et build.env).
set -euo pipefail
SDK="${ANDROID_HOME:-$HOME/android}"
BT="$SDK/build-tools/${BUILD_TOOLS:-35.0.0}"
JAR="$SDK/platforms/android-34/android.jar"
[ -f build.env ] && source build.env
VERSION_CODE="${VERSION_CODE:-1}"; VERSION_NAME="${VERSION_NAME:-1.0.0}"
rm -rf build && mkdir -p build/res build/gen build/classes build/dex
sed -e "s/android:versionCode=\"[0-9]*\"/android:versionCode=\"$VERSION_CODE\"/" \
    -e "s/android:versionName=\"[^\"]*\"/android:versionName=\"$VERSION_NAME\"/" app/AndroidManifest.xml > build/AndroidManifest.xml
"$BT/aapt2" compile --dir app/res -o build/res/
"$BT/aapt2" link -o build/base.apk -I "$JAR" --manifest build/AndroidManifest.xml -A app/assets \
  --java build/gen build/res/*.flat --min-sdk-version 26 --target-sdk-version 34
javac -source 8 -target 8 -nowarn -classpath "$JAR" -d build/classes \
  build/gen/fr/laousse/courses/R.java app/src/fr/laousse/courses/*.java
"$BT/d8" --release --min-api 26 --lib "$JAR" --output build/dex $(find build/classes -name '*.class')
cp build/base.apk build/unsigned.apk
(cd build/dex && zip -q ../unsigned.apk classes.dex)
"$BT/zipalign" -f -p 4 build/unsigned.apk build/aligned.apk
"$BT/apksigner" sign --ks "${KEYSTORE_FILE:?KEYSTORE_FILE manquant}" --ks-pass "pass:${KEYSTORE_PASSWORD:?}" --key-pass "pass:${KEYSTORE_PASSWORD}" \
  --out "build/suivi-courses-v$VERSION_NAME.apk" build/aligned.apk
"$BT/apksigner" verify "build/suivi-courses-v$VERSION_NAME.apk"
ls -la build/*.apk
