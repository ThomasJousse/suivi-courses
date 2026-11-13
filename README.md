# Suivi de courses — app Android

App perso (WebView native, sans framework) qui affiche les Sheets « Suivi de courses » de Thomas et Dorian, lus via un script Google Apps Script protégé par une clé. Les Sheets restent privés.

- **Nouveaux tickets** : intégrés par Claude (skill `ajout-ticket`) dans le Sheet → visibles dans l'app après ⟳ (ou au prochain lancement).
- **Nouvelle version du code** : augmenter `version` dans `package.json` puis pousser sur `main` → GitHub Actions compile l'APK signé et le publie dans les Releases → l'app propose la mise à jour au lancement.

## Secret GitHub nécessaire
Un seul : `APP_SECRETS` (JSON : api, key, kspass, keystore en base64).

## Structure
- `app/assets/www/index.html` : toute l'interface (HTML/CSS/JS)
- `app/src/.../MainActivity.java` : WebView + pont natif (requêtes vers le script et GitHub, liens externes, couleur de barre)
- `apps-script/Code.template.gs` : script à coller dans un projet Apps Script autonome
- `scripts/build.mjs` + `build.sh` : génération de la config puis compilation (aapt2, javac, d8, apksigner)
