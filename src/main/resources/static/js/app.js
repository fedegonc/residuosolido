/* app.js — JS global de Eco Solicitud: se carga en TODAS las páginas (base.html).
   Solo va aquí lo que es (a) verdaderamente global (i18n, toasts) o (b) un
   comportamiento de componente activado por markup (data-attr/clase), reusado
   en más de una página. Lógica exclusiva de una sola página va en su propio
   archivo, cargado vía layout:fragment="pageScripts" de esa página:
     - request-form.html → /js/request-form.js
     - org/profile.html  → /js/org-profile.js
   El modal de rastreo del navbar es markup inline oculto (is-hidden) — se
   abre con [data-modal-open] y se cierra con .modal__close / overlay / Escape.

   Componentes globales activados por markup (usados en más de una página):
     .check-card       → request-form.html, org/profile.html
     .password-field__toggle (PIN) → auth/login.html, auth/register.html
     .radio-card       → request-form.html
     #imageFile/#fileName → request-form.html
     selector de país (#*CountryCode) → index, request-form (guestPhone/userPhone),
     register, org/profile (phone), track (trackPhone) — markup canónico en fragments/forms.html */

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
        if (parts[1] && translations[parts[1].trim()]) el.setAttribute(parts[0].trim(), translations[parts[1].trim()]);
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
  document.querySelectorAll('.password-field__toggle').forEach(function (toggleBtn) {
    toggleBtn.addEventListener('click', function () {
      var input = toggleBtn.closest('.password-field').querySelector('input');
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
    var opener = e.target.closest && e.target.closest('[data-modal-open]');
    if (opener) {
      var modal = document.getElementById(opener.getAttribute('data-modal-open'));
      var target = modal && modal.querySelector('.modal-overlay');
      if (target) target.classList.remove('is-hidden');
      return;
    }
    var overlay = e.target.closest && e.target.closest('.modal-overlay');
    if (overlay && (e.target.closest('.modal__close') || e.target === overlay)) {
      overlay.classList.add('is-hidden');
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
      var overlay = document.querySelector('.modal-overlay:not(.is-hidden)');
      if (overlay) overlay.classList.add('is-hidden');
    }
  });
  document.addEventListener('submit', function (e) {
    var msg = e.target.getAttribute && e.target.getAttribute('data-confirm');
    if (msg && !window.confirm(msg)) e.preventDefault();
  });

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
  var PHONE_PREFIXES = ['guestPhone', 'userPhone', 'phone', 'trackPhone'];
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

    /* Prefill: el index manda ?telefono=<texto libre> y el server lo vuelca
       crudo en el input nacional. Si trae código de país (con o sin '+'),
       lo reparte entre select / nacional / DDD. Espejo de PhoneNumber.normalize. */
    function distributePrefill() {
      var raw = nationalInput.value.replace(/[\s\-()]/g, '');
      if (!raw) return;
      var code = null;
      var national = raw;
      if (raw.indexOf('+598') === 0) { code = '+598'; national = raw.slice(4); }
      else if (raw.indexOf('+55') === 0) { code = '+55'; national = raw.slice(3); }
      else if (/^598\d{8}$/.test(raw)) { code = '+598'; national = raw.slice(3); }
      else if (/^55\d{11}$/.test(raw)) { code = '+55'; national = raw.slice(2); }
      // 11 dígitos sin código: jamás es UY válido (exige 8), se asume BR con DDD
      else if (/^\d{2}9\d{8}$/.test(raw)) { code = '+55'; national = raw; }
      if (code === '+55' && /^\d{11}$/.test(national)) {
        var dddInput = dddGroup && dddGroup.querySelector('input');
        if (dddInput) dddInput.value = national.slice(0, 2);
        national = national.slice(2);
      }
      if (code) countrySel.value = code;
      if ((code || countrySel.value) === '+598') national = national.replace(/^0+/, '');
      nationalInput.value = national;
    }

    distributePrefill();
    countrySel.addEventListener('change', updateForCountry);
    updateForCountry();
  });

  /* ─── Track (guest): el server espera un solo param `telefono` E.164.
     El componente renderiza sin names (inputs = solo UI); JS promueve el
     nacional a `telefono` y al submit lo reescribe como E.164. Sin JS,
     telefono no se sube y el server simplemente no busca (no rompe). ─── */
  var trackForm = document.getElementById('trackForm');
  if (trackForm) {
    var trackNat = document.getElementById('trackPhoneNational');
    trackNat.name = 'telefono';
    trackForm.addEventListener('submit', function () {
      var code = document.getElementById('trackPhoneCountryCode').value;
      var nat = trackNat.value.replace(/[\s\-()]/g, '');
      var full;
      if (nat.charAt(0) === '+') {
        full = nat;
      } else if (code === '+55') {
        // 11 dígitos = DDD embebido (espejo de distributePrefill)
        var ddd = nat.length === 11 ? nat.slice(0, 2) : document.getElementById('trackPhoneDdd').value.trim();
        if (nat.length === 11) nat = nat.slice(2);
        full = code + ddd + nat;
      } else {
        full = code + nat.replace(/^0+/, '');
      }
      trackNat.value = full;
    });
  }
})();
