// config.js - TeamPulse Dynamic API Configuration
(function () {
  function resolveApiBase() {
    if (window.API_BASE_URL) return window.API_BASE_URL;
    const saved = localStorage.getItem('API_BASE_URL');
    if (saved && saved.trim()) return saved.trim().replace(/\/+$/, '');

    // If served by static server on port 5500, point to backend port 3333
    if (window.location.port === '5500') {
      return 'http://localhost:3333';
    }

    // When served directly by Spring Boot (locally or deployed on Render), use same-origin relative URLs
    return '';
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
