/**
 * Funcionalidades para los menús desplegables de categorías
 * Maneja la interacción con las categorías jerárquicas y la carga dinámica de posts
 */

// Datos de posts por categoría (esto podría venir de una API en el futuro)
const categoryPosts = {
    'reciclable': [
        {
            id: 1, 
            title: 'Guía de Reciclaje de Plásticos', 
            content: 'Los plásticos son uno de los materiales más comunes...', 
            sourceUrl: 'https://www.greenpeace.org/usa/research/plastic-recycling/', 
            sourceName: 'Greenpeace'
        },
        {
            id: 2, 
            title: 'Reciclaje de Papel y Cartón', 
            content: 'El papel y cartón representan una gran parte...', 
            sourceUrl: 'https://www.epa.gov/recycle/how-do-i-recycle-common-recyclables', 
            sourceName: 'EPA'
        }
    ],
    'no-reciclable': [
        {
            id: 3, 
            title: 'Manejo de Residuos Peligrosos', 
            content: 'Los residuos peligrosos como baterías...', 
            sourceUrl: 'https://www.epa.gov/hw/household-hazardous-waste-hhw', 
            sourceName: 'EPA'
        }
    ],
    'informaciones': [
        {
            id: 4, 
            title: 'Educación Ambiental en el Hogar', 
            content: 'La educación ambiental comienza en casa...', 
            sourceUrl: 'https://www.unep.org/explore-topics/education-environment', 
            sourceName: 'UNEP'
        },
        {
            id: 5, 
            title: 'Impacto Ambiental de los Residuos', 
            content: 'Comprende el verdadero impacto que nuestros residuos...', 
            sourceUrl: 'https://www.worldbank.org/en/topic/urbandevelopment/brief/solid-waste-management', 
            sourceName: 'World Bank'
        }
    ]
};

/**
 * Alterna la visibilidad de una categoría específica
 * @param {string} categorySlug - El slug de la categoría a alternar
 */
function toggleCategory(categorySlug) {
    const content = document.getElementById(`content-${categorySlug}`);
    const arrow = document.getElementById(`arrow-${categorySlug}`);
    const postsContainer = document.getElementById(`posts-${categorySlug}`);
    
    if (!content || !arrow || !postsContainer) {
        console.error(`Elementos no encontrados para la categoría: ${categorySlug}`);
        return;
    }
    
    if (content.classList.contains('hidden')) {
        // Mostrar contenido
        content.classList.remove('hidden');
        arrow.classList.add('rotate-180');
        loadPosts(categorySlug, postsContainer);
    } else {
        // Ocultar contenido
        content.classList.add('hidden');
        arrow.classList.remove('rotate-180');
    }
}

/**
 * Carga los posts de una categoría específica en el contenedor
 * @param {string} categorySlug - El slug de la categoría
 * @param {HTMLElement} container - El contenedor donde cargar los posts
 */
function loadPosts(categorySlug, container) {
    const posts = categoryPosts[categorySlug] || [];
    
    // Limpiar contenedor
    container.innerHTML = '';
    
    if (posts.length === 0) {
        container.innerHTML = `
            <div class="text-center py-6">
                <div class="bg-gradient-to-br from-gray-50 to-blue-50 rounded-xl p-6 border border-gray-100">
                    <div class="w-12 h-12 mx-auto mb-4 bg-gradient-to-br from-gray-400 to-gray-600 rounded-full flex items-center justify-center">
                        <svg class="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9.663 17h4.673M12 3v1m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 117.072 0l-.548.547A3.374 3.374 0 0014 18.469V19a2 2 0 11-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z"></path>
                        </svg>
                    </div>
                    <h4 class="font-semibold text-gray-800 mb-2">Contenido en desarrollo</h4>
                    <p class="text-sm text-gray-600 mb-3">Estamos preparando información valiosa para esta categoría.</p>
                    <a href="/posts" class="inline-flex items-center text-sm text-green-600 hover:text-green-800 font-medium">
                        <svg class="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 7l5 5m0 0l-5 5m5-5H6"></path>
                        </svg>
                        Ver todas las publicaciones
                    </a>
                </div>
            </div>
        `;
        return;
    }
    
    // Crear elementos de post
    posts.forEach(post => {
        const postElement = createPostElement(post);
        container.appendChild(postElement);
    });
}

/**
 * Crea un elemento HTML para un post
 * @param {Object} post - Los datos del post
 * @returns {HTMLElement} - El elemento HTML del post
 */
function createPostElement(post) {
    const postElement = document.createElement('div');
    postElement.className = 'bg-white p-4 rounded-lg shadow-sm border-l-4 border-gray-300 hover:shadow-md transition-shadow';
    
    postElement.innerHTML = `
        <h5 class="font-semibold text-gray-800 mb-2">
            <a href="/posts/${post.id}" class="hover:text-green-600 transition-colors">${post.title}</a>
        </h5>
        <p class="text-gray-600 text-sm mb-3">${post.content.substring(0, 100)}...</p>
        <div class="flex items-center justify-between">
            <a href="/posts/${post.id}" class="text-green-600 hover:text-green-800 text-sm font-medium transition-colors">
                Leer más
            </a>
            <a href="${post.sourceUrl}" target="_blank" class="text-blue-600 hover:text-blue-800 text-xs transition-colors">
                ${post.sourceName}
            </a>
        </div>
    `;
    
    return postElement;
}

/**
 * Hace scroll hasta una categoría específica y la expande automáticamente
 * @param {string} categorySlug - El slug de la categoría objetivo
 */
function scrollToCategory(categorySlug) {
    // Cerrar el menú hamburguesa primero
    const mobileMenu = document.getElementById('mobile-menu');
    if (mobileMenu && !mobileMenu.classList.contains('hidden')) {
        mobileMenu.classList.add('hidden');
    }
    
    // Buscar el elemento de la categoría
    const categoryButton = document.querySelector(`button[onclick="toggleCategory('${categorySlug}')"]`);
    
    if (categoryButton) {
        // Hacer scroll hasta la categoría con un offset para el header
        const offsetTop = categoryButton.offsetTop - 100;
        
        window.scrollTo({
            top: offsetTop,
            behavior: 'smooth'
        });
        
        // Expandir la categoría automáticamente después de un breve delay
        setTimeout(() => {
            const content = document.getElementById(`content-${categorySlug}`);
            if (content && content.classList.contains('hidden')) {
                toggleCategory(categorySlug);
            }
        }, 500); // Delay para que termine el scroll
    } else {
        console.warn(`Categoría no encontrada: ${categorySlug}`);
        // Si no encuentra la categoría, hacer scroll al inicio de las categorías
        const categoriesSection = document.querySelector('[th\\:fragment="categories"]');
        if (categoriesSection) {
            categoriesSection.scrollIntoView({ behavior: 'smooth', block: 'start' });
        }
    }
}

/**
 * Inicializa las funcionalidades de categorías
 */
function initializeCategories() {
    // Exponer las funciones globalmente para los botones onclick
    window.toggleCategory = toggleCategory;
    window.scrollToCategory = scrollToCategory;
    
    console.log('Funcionalidades de categorías inicializadas correctamente');
}

// Inicializar cuando el DOM esté listo
document.addEventListener('DOMContentLoaded', initializeCategories);
