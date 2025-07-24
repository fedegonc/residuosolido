/**
 * Funcionalidades para el menú móvil (hamburguesa)
 * Maneja la apertura/cierre del menú y la navegación móvil
 */

/**
 * Inicializa las funcionalidades del menú móvil
 */
function initializeMobileMenu() {
    console.log('Inicializando menú móvil...');
    
    // Buscar el botón y menú específicos
    const mobileMenuButton = document.getElementById('mobile-menu-button');
    const mobileMenu = document.getElementById('mobile-menu');
    
    console.log('Botón encontrado:', mobileMenuButton);
    console.log('Menú encontrado:', mobileMenu);
    
    if (mobileMenuButton && mobileMenu) {
        // Agregar event listener al botón
        mobileMenuButton.addEventListener('click', function(e) {
            e.preventDefault();
            e.stopPropagation();
            
            console.log('Botón hamburguesa clickeado');
            
            // Toggle del menú
            if (mobileMenu.classList.contains('hidden')) {
                mobileMenu.classList.remove('hidden');
                console.log('Menú abierto');
            } else {
                mobileMenu.classList.add('hidden');
                console.log('Menú cerrado');
            }
        });
        
        // Cerrar menú al hacer click fuera
        document.addEventListener('click', function(e) {
            if (!e.target.closest('#mobile-menu') && !e.target.closest('#mobile-menu-button')) {
                if (!mobileMenu.classList.contains('hidden')) {
                    mobileMenu.classList.add('hidden');
                    console.log('Menú cerrado por click externo');
                }
            }
        });
        
        console.log('Event listeners agregados correctamente');
    } else {
        console.error('No se encontraron los elementos del menú móvil');
    }
    
    console.log('Funcionalidades del menú móvil inicializadas');
}

/**
 * Cierra el menú móvil programáticamente
 */
function closeMobileMenu() {
    const mobileMenus = document.querySelectorAll('[id^="mobile-menu"]');
    const mobileMenuButtons = document.querySelectorAll('[id^="mobile-menu-button"]');
    
    mobileMenus.forEach(menu => {
        menu.classList.add('hidden');
    });
    
    mobileMenuButtons.forEach(button => {
        button.classList.remove('menu-open');
    });
}

/**
 * Abre el menú móvil programáticamente
 */
function openMobileMenu() {
    const mobileMenu = document.getElementById('mobile-menu');
    const mobileMenuButton = document.getElementById('mobile-menu-button');
    
    if (mobileMenu && mobileMenuButton) {
        mobileMenu.classList.remove('hidden');
        mobileMenuButton.classList.add('menu-open');
    }
}

/**
 * Verifica si el menú móvil está abierto
 * @returns {boolean} - true si está abierto, false si está cerrado
 */
function isMobileMenuOpen() {
    const mobileMenu = document.getElementById('mobile-menu');
    return mobileMenu && !mobileMenu.classList.contains('hidden');
}

// Exponer funciones globalmente
window.closeMobileMenu = closeMobileMenu;
window.openMobileMenu = openMobileMenu;
window.isMobileMenuOpen = isMobileMenuOpen;

// Inicializar cuando el DOM esté listo
document.addEventListener('DOMContentLoaded', initializeMobileMenu);
