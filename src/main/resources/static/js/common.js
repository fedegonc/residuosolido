/**
 * Funcionalidades JavaScript comunes para toda la aplicación
 * Incluye utilidades y funciones que pueden ser reutilizadas en múltiples páginas
 */

/**
 * Utilidades para manejo de notificaciones
 */
const NotificationUtils = {
    /**
     * Muestra una notificación al usuario
     * @param {string} message - El mensaje a mostrar
     * @param {string} type - El tipo de notificación (success, error, warning, info)
     * @param {number} duration - Duración en milisegundos (por defecto 3000)
     */
    show: function(message, type = 'success', duration = 3000) {
        // Crear elemento de notificación
        const notification = document.createElement('div');
        notification.className = `fixed top-4 right-4 z-50 p-4 rounded-lg shadow-lg transition-all duration-300 transform translate-x-full`;
        
        // Aplicar estilos según el tipo
        const typeStyles = {
            success: 'bg-green-500 text-white',
            error: 'bg-red-500 text-white',
            warning: 'bg-yellow-500 text-black',
            info: 'bg-blue-500 text-white'
        };
        
        notification.className += ` ${typeStyles[type] || typeStyles.success}`;
        notification.textContent = message;
        
        // Agregar al DOM
        document.body.appendChild(notification);
        
        // Animar entrada
        setTimeout(() => {
            notification.classList.remove('translate-x-full');
        }, 100);
        
        // Remover después de la duración especificada
        setTimeout(() => {
            notification.classList.add('translate-x-full');
            setTimeout(() => {
                if (notification.parentNode) {
                    notification.parentNode.removeChild(notification);
                }
            }, 300);
        }, duration);
    }
};

/**
 * Utilidades para manejo de formularios
 */
const FormUtils = {
    /**
     * Valida un formulario básico
     * @param {HTMLFormElement} form - El formulario a validar
     * @returns {boolean} - true si es válido, false si no
     */
    validate: function(form) {
        const requiredFields = form.querySelectorAll('[required]');
        let isValid = true;
        
        requiredFields.forEach(field => {
            if (!field.value.trim()) {
                this.showFieldError(field, 'Este campo es requerido');
                isValid = false;
            } else {
                this.clearFieldError(field);
            }
        });
        
        return isValid;
    },
    
    /**
     * Muestra un error en un campo específico
     * @param {HTMLElement} field - El campo con error
     * @param {string} message - El mensaje de error
     */
    showFieldError: function(field, message) {
        // Remover error previo
        this.clearFieldError(field);
        
        // Agregar clase de error
        field.classList.add('border-red-500');
        
        // Crear mensaje de error
        const errorElement = document.createElement('div');
        errorElement.className = 'text-red-500 text-sm mt-1';
        errorElement.textContent = message;
        errorElement.setAttribute('data-error-for', field.name || field.id);
        
        // Insertar después del campo
        field.parentNode.insertBefore(errorElement, field.nextSibling);
    },
    
    /**
     * Limpia el error de un campo
     * @param {HTMLElement} field - El campo a limpiar
     */
    clearFieldError: function(field) {
        field.classList.remove('border-red-500');
        
        // Remover mensaje de error existente
        const errorElement = field.parentNode.querySelector(`[data-error-for="${field.name || field.id}"]`);
        if (errorElement) {
            errorElement.remove();
        }
    }
};

/**
 * Utilidades para animaciones y efectos visuales
 */
const AnimationUtils = {
    /**
     * Hace scroll suave a un elemento
     * @param {string|HTMLElement} target - El selector o elemento objetivo
     * @param {number} offset - Offset en píxeles (por defecto 0)
     */
    scrollTo: function(target, offset = 0) {
        const element = typeof target === 'string' ? document.querySelector(target) : target;
        
        if (element) {
            const elementPosition = element.getBoundingClientRect().top + window.pageYOffset;
            const offsetPosition = elementPosition - offset;
            
            window.scrollTo({
                top: offsetPosition,
                behavior: 'smooth'
            });
        }
    },
    
    /**
     * Aplica un efecto de fade in a un elemento
     * @param {HTMLElement} element - El elemento a animar
     * @param {number} duration - Duración en milisegundos
     */
    fadeIn: function(element, duration = 300) {
        element.style.opacity = '0';
        element.style.display = 'block';
        
        const start = performance.now();
        
        const animate = (currentTime) => {
            const elapsed = currentTime - start;
            const progress = Math.min(elapsed / duration, 1);
            
            element.style.opacity = progress;
            
            if (progress < 1) {
                requestAnimationFrame(animate);
            }
        };
        
        requestAnimationFrame(animate);
    }
};

/**
 * Utilidades para manejo de datos
 */
const DataUtils = {
    /**
     * Realiza una petición fetch con manejo de errores
     * @param {string} url - La URL a consultar
     * @param {Object} options - Opciones para fetch
     * @returns {Promise} - Promesa con la respuesta
     */
    fetchWithErrorHandling: async function(url, options = {}) {
        try {
            const response = await fetch(url, {
                headers: {
                    'Content-Type': 'application/json',
                    ...options.headers
                },
                ...options
            });
            
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            
            return await response.json();
        } catch (error) {
            console.error('Error en la petición:', error);
            NotificationUtils.show('Error al cargar los datos', 'error');
            throw error;
        }
    }
};

// Exponer utilidades globalmente
window.NotificationUtils = NotificationUtils;
window.FormUtils = FormUtils;
window.AnimationUtils = AnimationUtils;
window.DataUtils = DataUtils;

// Inicialización común
document.addEventListener('DOMContentLoaded', function() {
    console.log('Utilidades comunes cargadas correctamente');
});
