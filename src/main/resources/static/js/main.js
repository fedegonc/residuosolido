/**
 * Residuos Sólidos - Funcionalidades JavaScript principales
 */

// Esperar a que el DOM esté completamente cargado
document.addEventListener('DOMContentLoaded', function() {
    initLanguageSelector();
    initDebugMode();
});

/**
 * Inicializa el selector de idioma
 * Nota: Ya no necesitamos manejar el evento change porque el formulario se envía automáticamente
 * con el atributo onchange="this.form.submit()"
 */
function initLanguageSelector() {
    const languageSelector = document.getElementById('language-selector');
    if (!languageSelector) return;
    
    // Cargar idioma preferido guardado (solo para mostrar inicialmente)
    const savedLanguage = localStorage.getItem('preferred-language');
    if (savedLanguage && !new URLSearchParams(window.location.search).has('lang')) {
        // Solo establecer el valor si no hay un parámetro lang en la URL
        languageSelector.value = savedLanguage;
        // Enviar el formulario para aplicar el idioma guardado
        document.getElementById('language-form').submit();
    } else if (!savedLanguage && !new URLSearchParams(window.location.search).has('lang')) {
        // Si no hay idioma guardado ni parámetro lang, establecer portugués por defecto
        localStorage.setItem('preferred-language', 'pt');
        languageSelector.value = 'pt';
    }
    
    // Guardar la selección actual en localStorage cuando cambia
    languageSelector.addEventListener('change', function() {
        const selectedLanguage = this.value;
        localStorage.setItem('preferred-language', selectedLanguage);
        
        // Mostrar mensaje de cambio de idioma
        const message = {
            'es': 'Cambiando a Español...',
            'pt': 'Mudando para Português...'
        };
        
        // Crear notificación temporal
        const notification = document.createElement('div');
        notification.className = 'fixed top-4 right-4 bg-green-600 text-white px-4 py-2 rounded-md shadow-lg z-50';
        notification.textContent = message[selectedLanguage];
        document.body.appendChild(notification);
        
        // El formulario se envía automáticamente por el onchange en el HTML
        console.log('Cambiando idioma a: ' + selectedLanguage);
    });
}

/**
 * Inicializa la funcionalidad del modo debug
 */
function initDebugMode() {
    const debugToggle = document.getElementById('debug-toggle');
    if (!debugToggle) return;
    
    debugToggle.addEventListener('click', function() {
        document.body.classList.toggle('debug-mode');
        
        if (document.body.classList.contains('debug-mode')) {
            // Aplicar bordes a todos los elementos cuando está en modo debug
            const style = document.createElement('style');
            style.id = 'debug-styles';
            style.innerHTML = `
                .debug-mode * {
                    outline: 1px solid rgba(255, 0, 0, 0.2) !important;
                }
                .debug-mode div {
                    outline: 1px solid rgba(0, 0, 255, 0.2) !important;
                }
                .debug-mode section {
                    outline: 2px solid rgba(0, 128, 0, 0.3) !important;
                }
                .debug-mode nav {
                    outline: 2px solid rgba(128, 0, 128, 0.3) !important;
                }
                .debug-mode p {
                    outline: 1px dashed rgba(255, 165, 0, 0.5) !important;
                }
                .debug-mode h1, .debug-mode h2, .debug-mode h3 {
                    outline: 1px dashed rgba(255, 255, 0, 0.5) !important;
                }
                .debug-mode #debug-toggle {
                    background-color: #e53e3e;
                }
            `;
            document.head.appendChild(style);
        } else {
            // Eliminar estilos de debug
            const debugStyles = document.getElementById('debug-styles');
            if (debugStyles) {
                debugStyles.remove();
            }
        }
    });
}
