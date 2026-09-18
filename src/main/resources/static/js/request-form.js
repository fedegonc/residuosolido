/* request-form.js — lógica exclusiva de users/request-form.html.
   Filtra los materiales visibles/habilitados según la organización elegida. */

(function () {
  'use strict';

  /* Contrato: data-materials es CSV plano (ej. "PLASTICO,VIDRIO"), generado por
     User.getAcceptedMaterialsCsv() — ver ese método antes de tocar este parseo. */
  function filterMaterialsByOrg() {
    var orgSelect = document.getElementById('organizationId');
    if (!orgSelect) return;
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
        card.classList.remove('check-card--checked');
      }
    });
  }
  document.addEventListener('change', function (e) {
    if (e.target && e.target.id === 'organizationId') filterMaterialsByOrg();
  });
  // Re-run after HTMX swaps org options (ciudad cambia → refresca organizaciones)
  document.body.addEventListener('htmx:afterSwap', filterMaterialsByOrg);
  filterMaterialsByOrg();
})();
