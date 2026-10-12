// config.js - TeamPulse Production Configuration
(function () {
  const RENDER_API = 'https://teampulse.onrender.com';

  function resolveApiBase() {
    if (window.API_BASE_URL) return window.API_BASE_URL;
    const saved = localStorage.getItem('API_BASE_URL');
    if (saved && saved.trim()) return saved.trim().replace(/\/+$/, '');

    // If served on Render domain (teampulse.onrender.com), use same-origin relative URLs
    if (window.location.hostname.includes('render.com')) {
      return '';
    }

    return RENDER_API;
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
