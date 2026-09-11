/* app.js — JS compartido para EcoSolicitud.
   i18n client-side + navbar toggle.
   Se carga una sola vez desde layout/base.html. */

(function () {
  'use strict';

  /* ─── i18n client-side ───
     Carga JSON desde /i18n/{page}/{lang}.json
     Escanea [data-i18n], [data-i18n-attr], [data-i18n-html] y reemplaza.

     Prioridad de idioma:
     1. <html lang> — seteado por el servidor (ciudad del usuario o Accept-Language)
     2. localStorage('lang') — elegido por el usuario explícitamente
     3. 'es' — fallback por defecto */

  var SUPPORTED = ['es', 'pt'];
  var DEFAULT_LANG = 'es';

  // El servidor ya resuelve el idioma (ciudad del usuario o Accept-Language).
  // Solo usamos localStorage si el servidor no seteó el atributo lang.
  var serverLang = document.documentElement.lang;
  var lang = serverLang || localStorage.getItem('lang') || DEFAULT_LANG;
  // Normalizar: "pt-BR" → "pt", "es-AR" → "es"
  lang = lang.split('-')[0].toLowerCase();
  if (SUPPORTED.indexOf(lang) === -1) lang = DEFAULT_LANG;
  // Sincronizar localStorage con el servidor para evitar desync
  localStorage.setItem('lang', lang);

  // El servidor inyecta las traducciones de la página actual en window.uiCopies
  // (ver UiCopyCatalog.java). Esto elimina el fetch asíncrono y el FOUC.
  var translations = window.uiCopies || {};

  function applyTranslations() {
    document.querySelectorAll('[data-i18n]').forEach(function (el) {
      var key = el.getAttribute('data-i18n');
      if (translations[key]) el.textContent = translations[key];
    });
    document.querySelectorAll('[data-i18n-html]').forEach(function (el) {
      var key = el.getAttribute('data-i18n-html');
      if (translations[key]) el.innerHTML = translations[key];
    });
    document.querySelectorAll('[data-i18n-attr]').forEach(function (el) {
      var pairs = el.getAttribute('data-i18n-attr').split(',');
      pairs.forEach(function (pair) {
        var parts = pair.trim().split(':');
        var attr = parts[0].trim();
        var key = parts[1].trim();
        if (translations[key]) el.setAttribute(attr, translations[key]);
      });
    });
  }

  applyTranslations();

  /* ─── Language selector ─── */
  function markActiveLang() {
    document.querySelectorAll('[data-lang]').forEach(function (btn) {
      btn.classList.toggle('is-active', btn.getAttribute('data-lang') === lang);
    });
  }

  document.querySelectorAll('[data-lang]').forEach(function (btn) {
    btn.addEventListener('click', function () {
      var chosen = btn.getAttribute('data-lang');
      if (chosen !== lang) {
        localStorage.setItem('lang', chosen);
        // Sync server-side session locale (Spring's LocaleChangeInterceptor,
        // param "lang") so server-rendered messages (flash/validation errors)
        // match the chosen language, not just the client-side i18n texts.
        var url = new URL(window.location.href);
        url.searchParams.set('lang', chosen);
        window.location.href = url.toString();
      }
    });
  });

  markActiveLang();

  /* ─── Navbar toggle (mobile) ─── */
  var btn = document.getElementById('menuBtn');
  var menu = document.getElementById('dropdownMenu');
  var menuIcon = document.getElementById('menuIcon');
  var closeIcon = document.getElementById('closeIcon');

  if (btn && menu) {
    btn.addEventListener('click', function () {
      menu.classList.toggle('is-hidden');
      if (menuIcon) menuIcon.classList.toggle('is-hidden');
      if (closeIcon) closeIcon.classList.toggle('is-hidden');
      btn.setAttribute('aria-expanded', String(!menu.classList.contains('is-hidden')));
    });

    document.addEventListener('click', function (e) {
      if (!btn.contains(e.target) && !menu.contains(e.target)) {
        menu.classList.add('is-hidden');
        if (menuIcon) menuIcon.classList.remove('is-hidden');
        if (closeIcon) closeIcon.classList.add('is-hidden');
      }
    });
  }

  /* ─── Check-card visual state (reusable across all checkbox-card forms) ─── */
  document.querySelectorAll('.check-card input[type="checkbox"]').forEach(function (cb) {
    function update() { cb.closest('.check-card').classList.toggle('check-card--checked', cb.checked); }
    cb.addEventListener('change', update);
    update();
  });

  /* ─── Password visibility toggle (reusable) ─── */
  document.querySelectorAll('[data-toggle-target]').forEach(function (toggleBtn) {
    toggleBtn.addEventListener('click', function () {
      var input = document.getElementById(toggleBtn.getAttribute('data-toggle-target'));
      if (!input) return;
      var icon = toggleBtn.querySelector('i');
      var willShow = input.type === 'password';
      input.type = willShow ? 'text' : 'password';
      if (icon) {
        icon.classList.toggle('fa-eye', !willShow);
        icon.classList.toggle('fa-eye-slash', willShow);
      }
      toggleBtn.setAttribute('aria-pressed', String(willShow));
    });
  });

  /* ─── PWA: Service Worker registration ─── */
  if ('serviceWorker' in navigator) {
    window.addEventListener('load', function () {
      navigator.serviceWorker.register('/sw.js', { updateViaCache: 'none' }).then(function (reg) {
        reg.update();
      }).catch(function () {});
    });
  }

  /* ─── PWA: Install prompt ─── */
  var deferredPrompt = null;
  window.addEventListener('beforeinstallprompt', function (e) {
    e.preventDefault();
    deferredPrompt = e;
    var btn = document.getElementById('pwaInstall');
    if (btn) btn.style.display = 'inline-flex';
  });
  window.installPwa = function () {
    if (!deferredPrompt) return;
    deferredPrompt.prompt();
    deferredPrompt.userChoice.then(function () {
      deferredPrompt = null;
      var btn = document.getElementById('pwaInstall');
      if (btn) btn.style.display = 'none';
    });
  };
  window.addEventListener('appinstalled', function () {
    var btn = document.getElementById('pwaInstall');
    if (btn) btn.style.display = 'none';
  });

  /* ─── Theme toggle ─── */
  function applyTheme(t) {
    document.documentElement.setAttribute('data-theme', t);
    var meta = document.getElementById('meta-theme-color');
    if (meta) meta.setAttribute('content', t === 'dark' ? '#0f1419' : '#2d6a4f');
  }
  function toggleTheme() {
    var current = localStorage.getItem('theme') || 'light';
    var next = current === 'dark' ? 'light' : 'dark';
    localStorage.setItem('theme', next);
    applyTheme(next);
  }
  document.querySelectorAll('#themeToggle, #themeToggleMobile').forEach(function (btn) {
    if (btn) btn.addEventListener('click', toggleTheme);
  });

  /* ─── Modal: Mis solicitudes ─── */
  window.openTrackModal = function () {
    var m = document.getElementById('trackModal');
    if (m) m.classList.add('is-open');
  };
  window.closeTrackModal = function () {
    var m = document.getElementById('trackModal');
    if (m) m.classList.remove('is-open');
  };
  document.addEventListener('keydown', function (e) {
    if (e.key === 'Escape') window.closeTrackModal();
  });
})();
