/**
 * Funcionalidades para crear posts con Cloudinary en el panel de admin
 */

document.addEventListener('DOMContentLoaded', function() {
    const imageInput = document.getElementById('postImageFile');
    const imagePreview = document.getElementById('imagePreviewAdmin');
    const previewImg = document.getElementById('previewImgAdmin');
    const uploadStatus = document.getElementById('uploadStatus');
    const imageUrlHidden = document.getElementById('imageUrlHidden');
    const form = document.getElementById('createPostFormAdmin');
    
    // Preview de imagen
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
    form.addEventListener('submit', async function(e) {
        e.preventDefault();
        
        const imageFile = imageInput.files[0];
        
        try {
            // Subir imagen a Cloudinary si existe
            if (imageFile) {
                uploadStatus.textContent = 'Subiendo imagen...';
                uploadStatus.className = 'mt-2 text-sm text-blue-600';
                
                const imageFormData = new FormData();
                imageFormData.append('image', imageFile);
                
                const imageResponse = await fetch('/admin/upload-image', {
                    method: 'POST',
                    body: imageFormData
                });
                
                if (imageResponse.ok) {
                    const imageUrl = await imageResponse.text();
                    imageUrlHidden.value = imageUrl;
                    uploadStatus.textContent = 'Imagen subida exitosamente';
                    uploadStatus.className = 'mt-2 text-sm text-green-600';
                } else {
                    const errorText = await imageResponse.text();
                    uploadStatus.textContent = 'Error: ' + errorText;
                    uploadStatus.className = 'mt-2 text-sm text-red-600';
                    return;
                }
            }
            
            // Enviar formulario
            uploadStatus.textContent = 'Creando post...';
            uploadStatus.className = 'mt-2 text-sm text-blue-600';
            
            form.submit();
            
        } catch (error) {
            console.error('Error:', error);
            uploadStatus.textContent = 'Error al procesar la solicitud';
            uploadStatus.className = 'mt-2 text-sm text-red-600';
        }
    });
});
