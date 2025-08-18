// Index.js - Funcionalidad básica para la página principal
document.addEventListener('DOMContentLoaded', function() {
    window.toggleCategory = toggleCategory;
});

function toggleCategory(categoryId) {
    const content = document.getElementById('categories-' + categoryId);
    const arrow = document.getElementById('arrow-' + categoryId);
    
    if (content && arrow) {
        content.classList.toggle('hidden');
        arrow.classList.toggle('rotate-90');
    }
}
