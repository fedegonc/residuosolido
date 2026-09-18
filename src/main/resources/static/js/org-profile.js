/* org-profile.js — lógica exclusiva de org/profile.html.
   Alterna entre la vista de solo lectura y el formulario de edición. */

(function () {
  'use strict';

  var editToggle = document.getElementById('editToggle');
  var viewCard = document.getElementById('viewCard');
  var editCard = document.getElementById('editCard');
  var cancelEdit = document.getElementById('cancelEdit');
  if (editToggle && viewCard && editCard) {
    editToggle.addEventListener('click', function () {
      viewCard.classList.add('is-hidden');
      editCard.classList.remove('is-hidden');
    });
  }
  if (cancelEdit && viewCard && editCard) {
    cancelEdit.addEventListener('click', function () {
      editCard.classList.add('is-hidden');
      viewCard.classList.remove('is-hidden');
    });
  }
})();
