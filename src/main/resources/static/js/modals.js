// Modal crear post
function openCreatePostModal() {
    document.getElementById('createPostModal').classList.remove('hidden');
}

function closeCreatePostModal() {
    document.getElementById('createPostModal').classList.add('hidden');
    document.getElementById('postForm').reset();
}

// Cerrar modal al hacer clic fuera
document.addEventListener('DOMContentLoaded', function() {
    const modal = document.getElementById('createPostModal');
    if (modal) {
        modal.addEventListener('click', function(e) {
            if (e.target === this) {
                closeCreatePostModal();
            }
        });
    }
    
    // Manejar envío del formulario
    const form = document.getElementById('postForm');
    if (form) {
        form.addEventListener('submit', function(e) {
            e.preventDefault();
            alert('Funcionalidad de crear post en desarrollo');
            closeCreatePostModal();
        });
    }
});
