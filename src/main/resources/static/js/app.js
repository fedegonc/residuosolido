/* app.js — JS compartido para EcoSolicitud.
   i18n client-side + navbar toggle.
   Se carga una sola vez desde layout/base.html. */

(function () {
  'use strict';

  /* ─── i18n client-side ───
     Carga JSON desde /i18n/{page}/{lang}.json
     Escanea [data-i18n], [data-i18n-attr], [data-i18n-html] y reemplaza.

     Prioridad de idioma:
     1. localStorage('lang') — elegido por el usuario
     2. <html lang> — seteado por Thymeleaf (Accept-Language del navegador)
     3. 'es' — fallback por defecto */

  var SUPPORTED = ['es', 'pt'];
  var DEFAULT_LANG = 'es';

  var lang = localStorage.getItem('lang') || document.documentElement.lang || DEFAULT_LANG;
  // Normalizar: "pt-BR" → "pt", "es-AR" → "es"
  lang = lang.split('-')[0].toLowerCase();
  if (SUPPORTED.indexOf(lang) === -1) lang = DEFAULT_LANG;

  var translations = {};

  function loadPage(page) {
    return fetch('/i18n/' + page + '/' + lang + '.json')
      .then(function (r) { return r.ok ? r.json() : {}; })
      .then(function (data) {
        for (var k in data) translations[k] = data[k];
        applyTranslations();
      })
      .catch(function () {});
  }

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

  // Carga common siempre, luego la página específica inferida de la URL
  var pageName = inferPageName(window.location.pathname);

  function inferPageName(path) {
    // Mapeo URL → carpeta i18n. Único lugar que se mantiene.
    var map = [
      { match: /^\/$/,                page: 'home' },
      { match: /^\/index$/,           page: 'home' },
      { match: /^\/rastrear$/,        page: 'track' },
      { match: /^\/metricas$/,        page: 'metrics' },
      { match: /^\/auth\/.*/,         page: 'auth' },
      { match: /^\/solicitudes?.*/,   page: 'requests' },
      { match: /^\/solicitud\/.*/,    page: 'requests' },
      { match: /^\/usuarios\/.*/,     page: 'users' },
      { match: /^\/acopio\/.*/,       page: 'org' },
    ];
    for (var i = 0; i < map.length; i++) {
      if (map[i].match.test(path)) return map[i].page;
    }
    return '';
  }

  loadPage('common').then(function () {
    if (pageName) loadPage(pageName);
  });

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
})();
