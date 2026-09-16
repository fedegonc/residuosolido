/* app.js — JS compartido para Eco Solicitud.
   i18n client-side + navbar toggle + modal + check-cards. */

(function () {
  'use strict';

  /* ─── i18n client-side ─── */
  var SUPPORTED = ['es', 'pt'];
  var DEFAULT_LANG = 'es';
  var serverLang = document.documentElement.lang;
  var lang = serverLang || localStorage.getItem('lang') || DEFAULT_LANG;
  lang = lang.split('-')[0].toLowerCase();
  if (SUPPORTED.indexOf(lang) === -1) lang = DEFAULT_LANG;
  localStorage.setItem('lang', lang);

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
      el.getAttribute('data-i18n-attr').split(',').forEach(function (pair) {
        var parts = pair.trim().split(':');
        if (translations[parts[1].trim()]) el.setAttribute(parts[0].trim(), translations[parts[1].trim()]);
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

  /* ─── Check-card visual state ─── */
  document.querySelectorAll('.check-card input[type="checkbox"]').forEach(function (cb) {
    function update() { cb.closest('.check-card').classList.toggle('check-card--checked', cb.checked); }
    cb.addEventListener('change', update);
    update();
  });

  /* ─── Password visibility toggle ─── */
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
