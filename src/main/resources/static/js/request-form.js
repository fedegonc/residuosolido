/* request-form.js — lógica exclusiva de users/request-form.html.
   - ciudad → fetch /solicitudes/org-options → repuebla #organizationId
   - organización → filtra los materiales visibles/habilitados (data-materials CSV) */

(function () {
  'use strict';

  var translations = window.uiCopies || {};
  var orgSelect = document.getElementById('organizationId');
  if (!orgSelect) return;

  function showErrorToast() {
    document.querySelectorAll('.toast').forEach(function (el) { el.remove(); });
    var toast = document.createElement('div');
    toast.className = 'alert alert--error toast';
    toast.setAttribute('role', 'alert');
    toast.innerHTML = '<svg class="icon" aria-hidden="true"><use href="/images/icons.svg#triangle-exclamation"/></svg><span>' +
      (translations._server_error_generic || 'Algo salió mal. Probá de nuevo en un momento.') + '</span>';
    document.body.appendChild(toast);
    setTimeout(function () { toast.remove(); }, 5000);
  }

  /* Contrato: data-materials es CSV plano (ej. "PLASTICO,VIDRIO"), generado por
     User.getAcceptedMaterialsCsv() — ver ese método antes de tocar este parseo. */
  function filterMaterialsByOrg() {
    var selected = orgSelect.options[orgSelect.selectedIndex];
    var csv = selected ? selected.getAttribute('data-materials') : null;
    var allowed = csv ? csv.split(',').filter(Boolean) : null;
    document.querySelectorAll('.check-card').forEach(function (card) {
      var input = card.querySelector('input[type="checkbox"]');
      if (!input) return;
      var isAllowed = !allowed || allowed.length === 0 || allowed.indexOf(card.getAttribute('data-material')) !== -1;
      card.classList.toggle('is-hidden', !isAllowed);
      input.disabled = !isAllowed;
      if (!isAllowed && input.checked) {
        input.checked = false;
      }
    });
  }

  document.addEventListener('change', function (e) {
    if (e.target && e.target.id === 'organizationId') filterMaterialsByOrg();
  });
  filterMaterialsByOrg();

  /* Ciudad → repuebla el select de organizaciones (GET devuelve <option>s). */
  var ciudad = document.getElementById('ciudad');
  var loading = document.getElementById('materialsLoading');
  if (ciudad && !ciudad.disabled) {
    ciudad.addEventListener('change', function () {
      console.log('Ciudad cambió a:', ciudad.value);
      if (!ciudad.value) return;
      if (loading) loading.classList.remove('is-hidden');
      var url = '/solicitudes/org-options?ciudad=' + encodeURIComponent(ciudad.value);
      console.log('Fetching:', url);
      fetch(url)
        .then(function (r) { return r.ok ? r.text() : Promise.reject(r.status); })
        .then(function (html) {
          console.log('Response recibida, actualizando select...');
          orgSelect.innerHTML = html;
          console.log('innerHTML actualizado, opciones:', orgSelect.options.length);
          filterMaterialsByOrg();
        })
        .catch(function (err) {
          console.error('Error al cargar organizaciones:', err);
          showErrorToast();
        })
        .finally(function () {
          if (loading) loading.classList.add('is-hidden');
        });
    });
  }
})();
