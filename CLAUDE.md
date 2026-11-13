# Suivi de courses — notes pour Claude

App Android perso (WebView native, pas de Capacitor ni Gradle). Données : Sheets privés « Suivi de courses » (Thomas) et « Suivi de courses Dorian », lus via un script Apps Script autonome (« API Suivi de courses ») protégé par une clé ; paramètre `p=thomas|dorian`. Le script renvoie aussi l'ID du dossier Drive « Tickets à intégrer » du profil.

## Mettre à jour
- **Tickets** : skill `ajout-ticket` (connecteur Google Sheets). Aucun build.
- **Code** : modifier `app/assets/www/index.html` (interface) ou `MainActivity.java` (couche Android), augmenter `version` dans `package.json`, pousser sur `main`. Le workflow compile l'APK signé et publie la Release `vX.Y.Z` ; l'app propose la mise à jour au lancement.
- Ne jamais pousser de tag : la Release crée le tag côté GitHub.

## Secrets
- Un seul secret GitHub : `APP_SECRETS` (JSON : api, key, kspass, keystore base64).
- Ne jamais committer `config.local.json`, `*.jks`, `apps-script/Code.gs`, ni d'ID de Sheet ou de dossier.
- Même clé de signature obligatoire d'une version à l'autre (sinon désinstaller/réinstaller).

## Build local
`node scripts/build.mjs && KEYSTORE_FILE=… KEYSTORE_PASSWORD=… ./build.sh` (Android SDK : platform 34, build-tools 35, JDK 17+).
