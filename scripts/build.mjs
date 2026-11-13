// Génère app/assets/www/config.js à partir des variables d'environnement (secret GitHub en CI) ou de config.local.json
import fs from "node:fs";
const pkg = JSON.parse(fs.readFileSync("package.json", "utf8"));
let local = {};
try { local = JSON.parse(fs.readFileSync("config.local.json", "utf8")); } catch {}
const cfg = {
  api: process.env.COURSES_API_URL || local.api || "",
  key: process.env.COURSES_API_KEY || local.key || "",
  repo: process.env.GITHUB_REPOSITORY || local.repo || "",
  version: (process.env.APP_VERSION || pkg.version).replace(/^v/, ""),
};
fs.writeFileSync("app/assets/www/config.js", "window.APP_CONFIG=" + JSON.stringify(cfg) + ";\n");
const [a, b, c] = cfg.version.split(".").map(Number);
fs.writeFileSync("build.env", `VERSION_NAME=${cfg.version}\nVERSION_CODE=${a * 10000 + b * 100 + c}\n`);
console.log("config.js →", { ...cfg, key: cfg.key ? "***" : "(vide)" });
