/**
 * Funcionalidades para crear posts con upload de imágenes
 */

let uploadedImageUrl = '';

// Abrir modal
function openCreatePostModal() {
    document.getElementById('createPostModal').classList.remove('hidden');
}

// Cerrar modal
function closeCreatePostModal() {
    document.getElementById('createPostModal').classList.add('hidden');
    document.getElementById('createPostForm').reset();
    document.getElementById('imagePreview').classList.add('hidden');
    uploadedImageUrl = '';
}

// Preview de imagen
document.addEventListener('DOMContentLoaded', function() {
    const imageInput = document.getElementById('postImage');
    const imagePreview = document.getElementById('imagePreview');
    const previewImg = document.getElementById('previewImg');
    
    imageInput.addEventListener('change', function(e) {
        const file = e.target.files[0];
        if (file) {
            const reader = new FileReader();
            reader.onload = function(e) {
                previewImg.src = e.target.result;
                imagePreview.classList.remove('hidden');
            };
            reader.readAsDataURL(file);
        } else {
            imagePreview.classList.add('hidden');
        }
    });
    
    // Manejar envío del formulario
    document.getElementById('createPostForm').addEventListener('submit', async function(e) {
        e.preventDefault();
        
        const title = document.getElementById('postTitle').value;
        const content = document.getElementById('postContent').value;
        const categoryId = document.getElementById('postCategory').value;
        const imageFile = document.getElementById('postImage').files[0];
        
        if (!title || !content || !categoryId) {
            alert('Por favor completa todos los campos obligatorios');
            return;
        }
        
        try {
            // Subir imagen si existe
            if (imageFile) {
                const imageFormData = new FormData();
                imageFormData.append('image', imageFile);
                
                const imageResponse = await fetch('/api/upload-image', {
                    method: 'POST',
                    body: imageFormData
                });
                
                if (imageResponse.ok) {
                    uploadedImageUrl = await imageResponse.text();
                } else {
                    const errorText = await imageResponse.text();
                    alert('Error al subir imagen: ' + errorText);
                    return;
                }
            }
            
            // Crear post
            const postFormData = new FormData();
            postFormData.append('title', title);
            postFormData.append('content', content);
            postFormData.append('imageUrl', uploadedImageUrl);
            postFormData.append('categoryId', categoryId);
            
            const postResponse = await fetch('/api/create-post', {
                method: 'POST',
                body: postFormData
            });
            
            if (postResponse.ok) {
                alert('Post creado exitosamente');
                closeCreatePostModal();
                // Recargar página para mostrar el nuevo post
                window.location.reload();
            } else {
                const errorText = await postResponse.text();
                alert('Error al crear post: ' + errorText);
            }
            
        } catch (error) {
            console.error('Error:', error);
            alert('Error al procesar la solicitud');
        }
    });
});

// Cerrar modal al hacer clic fuera
document.addEventListener('DOMContentLoaded', function() {
    const modal = document.getElementById('createPostModal');
    modal.addEventListener('click', function(e) {
        if (e.target === modal) {
            closeCreatePostModal();
        }
    });
});
