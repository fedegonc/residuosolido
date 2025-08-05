/**
 * Utilidad reutilizable para vista previa de imágenes
 * Uso: ImagePreview.init(inputSelector, previewContainerSelector, previewImgSelector)
 */
const ImagePreview = {
    /**
     * Inicializa la vista previa de imagen
     * @param {string} inputSelector - Selector del input file
     * @param {string} previewContainerSelector - Selector del contenedor de vista previa
     * @param {string} previewImgSelector - Selector de la imagen de vista previa
     */
    init: function(inputSelector, previewContainerSelector, previewImgSelector) {
        const input = document.querySelector(inputSelector);
        const previewContainer = document.querySelector(previewContainerSelector);
        const previewImg = document.querySelector(previewImgSelector);
        
        if (!input || !previewContainer || !previewImg) {
            console.warn('ImagePreview: Elementos no encontrados', {
                input: !!input,
                previewContainer: !!previewContainer,
                previewImg: !!previewImg
            });
            return false;
        }
        
        input.addEventListener('change', function(e) {
            const file = e.target.files[0];
            
            if (file && file.type.startsWith('image/')) {
                const reader = new FileReader();
                reader.onload = function(event) {
                    previewImg.src = event.target.result;
                    previewContainer.classList.remove('hidden');
                    previewContainer.style.display = 'block';
                };
                reader.readAsDataURL(file);
            } else {
                ImagePreview.hidePreview(previewContainer);
            }
        });
        
        return true;
    },
    
    /**
     * Oculta la vista previa
     * @param {Element} previewContainer - Contenedor de vista previa
     */
    hidePreview: function(previewContainer) {
        previewContainer.classList.add('hidden');
        previewContainer.style.display = 'none';
    },
    
    /**
     * Muestra la vista previa
     * @param {Element} previewContainer - Contenedor de vista previa
     */
    showPreview: function(previewContainer) {
        previewContainer.classList.remove('hidden');
        previewContainer.style.display = 'block';
    }
};

// Auto-inicialización para casos comunes
document.addEventListener('DOMContentLoaded', function() {
    // Hero image (admin config)
    ImagePreview.init(
        '#heroImageFile',
        '#heroImagePreview', 
        '#heroPreviewImg'
    );
    
    // Profile image (user form)
    ImagePreview.init(
        'input[name="profileImageFile"]',
        '#profileImagePreview',
        '#profilePreviewImg'
    );
});
