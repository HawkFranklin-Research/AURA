/* First-party only. Never exchange, log, render, persist or send authorization values to a server. */
(() => {
  "use strict";
  const query = new URLSearchParams(window.location.search);
  // Remove sensitive query values from the visible URL and this browser history entry.
  window.history.replaceState(null, "", window.location.pathname);
  const status = document.getElementById("status");
  const button = document.getElementById("return");
  const one = name => {
    const values = query.getAll(name);
    return values.length === 1 ? values[0] : null;
  };
  const app = one("app");
  const code = one("code");
  const state = one("state");
  const apps = new Set(["com.hawkfranklin.aura", "com.hawkfranklin.aura.debug"]);
  const expectedPath = app === "com.hawkfranklin.aura.debug"
    ? "/aura/openrouter/debug/callback" : "/aura/openrouter/callback";
  if (window.location.origin !== "https://hawkfranklin.eu" || window.location.pathname.replace(/\/$/, "") !== expectedPath || !apps.has(app) ||
      query.has("error") || !code || !state || code.length > 4096 || state.length > 512 ||
      /\s/.test(code) || /\s/.test(state)) {
    status.textContent = "Authorization was declined, expired or incomplete. Return to AURA and start sign-in again.";
    return;
  }
  const response = new URLSearchParams({code, state}).toString();
  // Explicit package targeting avoids sending the authorization response to a generic handler.
  button.href = `intent://openrouter/callback?${response}#Intent;scheme=${app};package=${app};end`;
  button.textContent = app.endsWith(".debug") ? "Return to AURA Debug" : "Return to AURA";
  button.hidden = false;
  status.textContent = "Authorization response received. Tap the button to finish securely in AURA.";
})();
