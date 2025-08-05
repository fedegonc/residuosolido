// Index.js - Funcionalidad JavaScript para la página principal
document.addEventListener('DOMContentLoaded', function() {
    console.log('Index.js cargado');
    
    // Funciones principales
    window.toggleCategory = toggleCategory;
    window.scrollToSections = scrollToSections;
    window.openCreatePostModal = openCreatePostModal;
    window.closeCreatePostModal = closeCreatePostModal;
});

/**
 * Toggle categorías en sidebar
 */
function toggleCategory(categoryId) {
    const content = document.getElementById('categories-' + categoryId);
    const arrow = document.getElementById('arrow-' + categoryId);
    
    if (content && arrow) {
        if (content.classList.contains('hidden')) {
            content.classList.remove('hidden');
            arrow.classList.add('rotate-90');
        } else {
            content.classList.add('hidden');
            arrow.classList.remove('rotate-90');
        }
    }
}

/**
 * Scroll suave a secciones
 */
function scrollToSections() {
    const sectionsElement = document.querySelector('aside');
    if (sectionsElement) {
        sectionsElement.scrollIntoView({ 
            behavior: 'smooth',
            block: 'start'
        });
    }
}

/**
 * Modal de crear post
 */
function openCreatePostModal() {
    const modal = document.getElementById('createPostModal');
    if (modal) {
        modal.classList.remove('hidden');
        document.body.style.overflow = 'hidden';
    }
}

function closeCreatePostModal() {
    const modal = document.getElementById('createPostModal');
    if (modal) {
        modal.classList.add('hidden');
        document.body.style.overflow = 'auto';
        
        // Reset form
        const form = document.getElementById('createPostForm');
        if (form) {
            form.reset();
        }
    }
}

// Cerrar modal con Escape
document.addEventListener('keydown', function(event) {
    if (event.key === 'Escape') {
        closeCreatePostModal();
    }
});

// Cerrar modal clickeando fuera
document.addEventListener('click', function(event) {
    const modal = document.getElementById('createPostModal');
    if (modal && event.target === modal) {
        closeCreatePostModal();
    }
});
