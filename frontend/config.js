// config.js - TeamPulse Dynamic API Configuration
(function () {
  const LOCAL_API = 'http://localhost:3333';
  // Deployed Render backend URL default (weblink: teampulse)
  const DEFAULT_PROD_API = 'https://teampulse.onrender.com';

  function resolveApiBase() {
    if (window.API_BASE_URL) return window.API_BASE_URL;
    const saved = localStorage.getItem('API_BASE_URL');
    if (saved && saved.trim()) return saved.trim().replace(/\/+$/, '');

    const hostname = window.location.hostname;
    if (!hostname || hostname === 'localhost' || hostname === '127.0.0.1') {
      return LOCAL_API;
    }
    return DEFAULT_PROD_API.replace(/\/+$/, '');
  }

  window.API_BASE = resolveApiBase();

  window.setBackendUrl = function (url) {
    if (!url || !url.trim()) {
      localStorage.removeItem('API_BASE_URL');
    } else {
      localStorage.setItem('API_BASE_URL', url.trim().replace(/\/+$/, ''));
    }
    window.location.reload();
  };
})();
