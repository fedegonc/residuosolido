/* app.js — JS global de Eco Solicitud: se carga en TODAS las páginas (base.html).
   Solo va aquí lo que es (a) verdaderamente global (i18n, toasts) o (b) un
   comportamiento de componente activado por markup (data-attr/clase), reusado
   en más de una página. Lógica exclusiva de una sola página va en su propio
   archivo, cargado vía layout:fragment="pageScripts" de esa página:
     - request-form.html → /js/request-form.js
     - org/profile.html  → /js/org-profile.js
   HTMX maneja navbar, modal y actualizaciones parciales.

   Componentes globales activados por markup (usados en más de una página):
     .check-card       → request-form.html, org/profile.html
     [data-toggle-target] (password) → auth/login.html, auth/register.html
     .radio-card       → request-form.html
     #imageFile/#fileName → request-form.html
     selector de país (#*CountryCode) → request-form.html (guestPhone/userPhone), org/profile.html (phone) */

(function () {
  'use strict';

  /* ─── i18n client-side: el servidor ya resolvió el idioma (?lang= → sesión)
     y sirvió el catálogo correcto en window.uiCopies vía /js/i18n.js?l=<lang>.
     Acá solo se aplica; no hay estado de idioma en el cliente. ─── */
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

  /* ─── Check-card visual state (componente) ─── */
  document.querySelectorAll('.check-card input[type="checkbox"]').forEach(function (cb) {
    function update() { cb.closest('.check-card').classList.toggle('check-card--checked', cb.checked); }
    cb.addEventListener('change', update);
    update();
  });

  /* ─── Password visibility toggle (componente) ─── */
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

  /* ─── Interacciones delegadas (los handlers inline onclick/onsubmit están
     bloqueados por CSP script-src 'self') ─── */
  document.addEventListener('click', function (e) {
    var overlay = e.target.closest && e.target.closest('.modal-overlay');
    if (overlay && (e.target.closest('.modal__close') || e.target === overlay)) {
      overlay.remove();
      return;
    }
    var rejectToggle = e.target.closest && e.target.closest('#rejectToggle');
    if (rejectToggle) {
      document.getElementById('rejectForm').classList.remove('is-hidden');
      rejectToggle.classList.add('is-hidden');
      return;
    }
    if (e.target.closest && e.target.closest('#rejectCancel')) {
      document.getElementById('rejectForm').classList.add('is-hidden');
      document.getElementById('rejectToggle').classList.remove('is-hidden');
    }
  });
  document.addEventListener('keydown', function (e) {
    if (e.key === 'Escape') {
      var overlay = document.querySelector('.modal-overlay');
      if (overlay) overlay.remove();
    }
  });
  document.addEventListener('submit', function (e) {
    var msg = e.target.getAttribute && e.target.getAttribute('data-confirm');
    if (msg && !window.confirm(msg)) e.preventDefault();
  });

  /* ─── Re-apply i18n after HTMX swaps ─── */
  document.body.addEventListener('htmx:afterSwap', applyTranslations);

  /* ─── Toast de error de red/servidor para requests HTMX ─── */
  function showErrorToast() {
    document.querySelectorAll('.toast').forEach(function (el) { el.remove(); });
    var toast = document.createElement('div');
    toast.className = 'alert alert--error toast';
    toast.setAttribute('role', 'alert');
    toast.innerHTML = '<i class="fa-solid fa-triangle-exclamation" aria-hidden="true"></i><span data-i18n="_server_error_generic">' +
      (translations._server_error_generic || 'Algo salió mal. Probá de nuevo en un momento.') + '</span>';
    document.body.appendChild(toast);
    setTimeout(function () { toast.remove(); }, 5000);
  }
  document.body.addEventListener('htmx:responseError', showErrorToast);
  document.body.addEventListener('htmx:sendError', showErrorToast);

  /* ─── Radio card visual state (componente) ─── */
  document.querySelectorAll('.radio-card input[type="radio"]').forEach(function (rb) {
    rb.addEventListener('change', function () {
      document.querySelectorAll('.radio-card').forEach(function (card) {
        card.classList.remove('radio-card--checked');
      });
      rb.closest('.radio-card').classList.add('radio-card--checked');
    });
    if (rb.checked) rb.closest('.radio-card').classList.add('radio-card--checked');
  });

  /* ─── File upload name display (componente) ─── */
  var fileInput = document.getElementById('imageFile');
  var fileName = document.getElementById('fileName');
  if (fileInput && fileName) {
    fileInput.addEventListener('change', function () {
      fileName.textContent = fileInput.files && fileInput.files.length > 0 ? fileInput.files[0].name : '';
    });
  }

  /* ─── Phone country selector (UY/BR) (componente) ─── */
  var PHONE_PREFIXES = ['guestPhone', 'userPhone', 'phone'];
  var PHONE_PLACEHOLDERS = { '+598': '9X XXX XXX', '+55': '9XXXX-XXXX' };
  var PHONE_PATTERNS = { '+598': '[0-9 ]{8,11}', '+55': '[0-9-]{8,12}' };

  PHONE_PREFIXES.forEach(function (prefix) {
    var countrySel = document.getElementById(prefix + 'CountryCode');
    var nationalInput = document.getElementById(prefix + 'National');
    var dddGroup = document.getElementById(prefix + 'DddGroup');
    if (!countrySel || !nationalInput) return;

    function updateForCountry() {
      var code = countrySel.value;
      nationalInput.setAttribute('placeholder', PHONE_PLACEHOLDERS[code] || '');
      nationalInput.setAttribute('pattern', PHONE_PATTERNS[code] || '');
      if (dddGroup) dddGroup.classList.toggle('is-hidden', code !== '+55');
    }
    countrySel.addEventListener('change', updateForCountry);
    updateForCountry();
  });
})();
