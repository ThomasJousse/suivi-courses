/**
 * Suivi de courses — API de lecture des Sheets pour l'app Android.
 * Projet Apps Script autonome (script.google.com), propriétaire : le compte des Sheets.
 * Les Sheets restent privés : seul ce script les lit, et il ne répond qu'avec la bonne clé.
 * Déploiement : Déployer > Nouveau déploiement > Application Web, exécuter en tant que « Moi », accès « Tout le monde ».
 */
const CLE = "COLLE_ICI_LA_CLE";
const PROFILS = {
  thomas: { sheet: "ID_SHEET_THOMAS", inbox: "ID_DOSSIER_A_INTEGRER_THOMAS", inboxName: "Tickets à intégrer Thomas", sheetName: "Suivi de courses" },
  dorian: { sheet: "ID_SHEET_DORIAN", inbox: "ID_DOSSIER_A_INTEGRER_DORIAN", inboxName: "Tickets à intégrer Dorian", sheetName: "Suivi de courses Dorian" },
};
const ONGLETS = { tickets: "Tickets", articles: "Articles", suivi: "Suivi mensuel" };

function doGet(e) {
  const p = (e && e.parameter) || {};
  if (p.k !== CLE) return json_({ error: "forbidden" });
  const prof = PROFILS[p.p || "thomas"];
  if (!prof) return json_({ error: "profil" });
  try {
    const ss = SpreadsheetApp.openById(prof.sheet);
    const out = { at: Date.now(), inbox: prof.inbox, inboxName: prof.inboxName, sheetName: prof.sheetName };
    for (const k in ONGLETS) {
      const sh = ss.getSheetByName(ONGLETS[k]);
      out[k] = sh ? sh.getDataRange().getDisplayValues() : [];
    }
    return json_(out);
  } catch (err) {
    return json_({ error: "tool_error" });
  }
}

function json_(o) {
  return ContentService.createTextOutput(JSON.stringify(o)).setMimeType(ContentService.MimeType.JSON);
}
