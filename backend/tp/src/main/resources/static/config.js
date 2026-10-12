// config.js - TeamPulse Dynamic API Configuration
(function () {
  function resolveApiBase() {
    // 1. Check runtime override
    if (window.API_BASE_URL) return window.API_BASE_URL;

    // 2. Check saved preference in localStorage
    const saved = localStorage.getItem('API_BASE_URL');
    if (saved && saved.trim()) return saved.trim().replace(/\/+$/, '');

    const hostname = window.location.hostname;
    const port = window.location.port;

    // 3. Local standalone frontend servers (e.g. port 5500, Live Server, file://)
    if (port === '5500' || port === '3000' || port === '5173' || window.location.protocol === 'file:') {
      return 'http://localhost:3333';
    }

    // 4. Running locally on Spring Boot (http://localhost:3333)
    if ((hostname === 'localhost' || hostname === '127.0.0.1') && port === '3333') {
      return '';
    }

    // 5. If running on a separate static frontend host (e.g. GitHub Pages, Netlify, separate Render static site)
    if (hostname.includes('client') || hostname.includes('frontend') || hostname.includes('github.io')) {
      return 'https://teampulse.onrender.com';
    }

    // 6. Running directly on the unified Render Web Service (https://teampulse.onrender.com)
    return '';
  }

  window.API_BASE = resolveApiBase();

  // Helper method: call setBackendUrl("https://your-backend.onrender.com") anytime to switch backend URL
  window.setBackendUrl = function (url) {
    if (!url || !url.trim()) {
      localStorage.removeItem('API_BASE_URL');
    } else {
      localStorage.setItem('API_BASE_URL', url.trim().replace(/\/+$/, ''));
    }
    window.location.reload();
  };
})();
