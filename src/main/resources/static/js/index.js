// Index.js - Funcionalidad completa para la página principal
document.addEventListener('DOMContentLoaded', function() {
    // Inicializar funcionalidades
    initGuestDropdown();
    initOrganizationsMap();
    initLogoutAlert();
    initPerformanceTracking();
    
    // Exponer función global
    window.toggleCategory = toggleCategory;
});

// === FUNCIONES DE CATEGORÍAS ===
function toggleCategory(categoryId) {
    const content = document.getElementById('categories-' + categoryId);
    const arrow = document.getElementById('arrow-' + categoryId);
    
    if (content && arrow) {
        content.classList.toggle('hidden');
        arrow.classList.toggle('rotate-90');
    }
}

// === GUEST DROPDOWN ===
function initGuestDropdown() {
    console.log('DOM cargado, configurando guest dropdown');
    
    const toggleBtn = document.getElementById('menu-toggle-btn');
    const menu = document.getElementById('guest-menu');
    
    console.log('Botón toggle:', toggleBtn);
    console.log('Menu elemento:', menu);
    
    if (toggleBtn && menu) {
        // Event listener para el botón
        toggleBtn.addEventListener('click', function(e) {
            e.preventDefault();
            e.stopPropagation();
            
            console.log('Click en botón menú');
            const isHidden = menu.classList.contains('hidden');
            console.log('Menu está hidden:', isHidden);
            
            menu.classList.toggle('hidden');
            
            const newState = menu.classList.contains('hidden');
            console.log('Nuevo estado hidden:', newState);
        });
        
        // Event listener para cerrar al hacer click fuera
        document.addEventListener('click', function(e) {
            if (!toggleBtn.contains(e.target) && !menu.contains(e.target)) {
                if (!menu.classList.contains('hidden')) {
                    console.log('Cerrando menu por click fuera');
                    menu.classList.add('hidden');
                }
            }
        });
        
        console.log('Event listeners configurados correctamente');
    } else {
        console.error('No se encontraron los elementos necesarios');
        console.error('toggleBtn:', toggleBtn);
        console.error('menu:', menu);
    }
}

// === MAPAS DE ORGANIZACIONES ===
function initOrganizationsMap() {
    // Verificar si Leaflet está disponible
    if (typeof L === 'undefined') {
        console.log('Leaflet no está cargado, cargando dinámicamente...');
        loadLeafletAndInitMap();
        return;
    }
    
    setupOrganizationMaps();
}

function loadLeafletAndInitMap() {
    // Cargar CSS de Leaflet
    const leafletCSS = document.createElement('link');
    leafletCSS.rel = 'stylesheet';
    leafletCSS.href = 'https://unpkg.com/leaflet@1.9.4/dist/leaflet.css';
    document.head.appendChild(leafletCSS);
    
    // Cargar JS de Leaflet
    const leafletJS = document.createElement('script');
    leafletJS.src = 'https://unpkg.com/leaflet@1.9.4/dist/leaflet.js';
    leafletJS.onload = function() {
        setupOrganizationMaps();
    };
    document.head.appendChild(leafletJS);
}

function setupOrganizationMaps() {
    // Inicializar mapas individuales de organizaciones
    const orgMaps = document.querySelectorAll('[id^="orgMap-"]');
    
    orgMaps.forEach(mapElement => {
        const lat = parseFloat(mapElement.dataset.lat);
        const lng = parseFloat(mapElement.dataset.lng);
        const name = mapElement.dataset.name;
        
        if (!lat || !lng) return;
        
        // Inicializar mapa individual
        const map = L.map(mapElement.id).setView([lat, lng], 15);
        
        // Agregar tiles
        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            attribution: ' OpenStreetMap contributors'
        }).addTo(map);
        
        // Agregar marcador
        const marker = L.marker([lat, lng]).addTo(map);
        marker.bindPopup(`
            <div class="p-2">
                <h3 class="font-bold text-green-700">${name}</h3>
                <p class="text-sm text-gray-600">Ubicación de la organización</p>
            </div>
        `);
        
        // Deshabilitar interacciones para mapas pequeños
        map.dragging.disable();
        map.touchZoom.disable();
        map.doubleClickZoom.disable();
        map.scrollWheelZoom.disable();
        map.boxZoom.disable();
        map.keyboard.disable();
        if (map.tap) map.tap.disable();
    });
    
    // Redimensionar mapa cuando sea necesario
    setTimeout(() => {
        const orgMaps = document.querySelectorAll('[id^="orgMap-"]');
        orgMaps.forEach(mapElement => {
            const mapInstance = window[mapElement.id + '_instance'];
            if (mapInstance) {
                mapInstance.invalidateSize();
            }
        });
    }, 100);
}

// === LOGOUT ALERT ===
function initLogoutAlert() {
    const params = new URLSearchParams(window.location.search);
    const hasLogoutParam = params.has('logout') && params.get('logout') === '1';
    const STORAGE_KEY = 'app.logoutShown';
    const MESSAGE = 'Sesión cerrada de forma segura. Para acceder nuevamente, inicia sesión.';

    // Si viene el flag en la URL, guarda en localStorage para persistir tras reloads
    if (hasLogoutParam) {
        try { localStorage.setItem(STORAGE_KEY, '1'); } catch (_) {}
    }

    const shouldShow = hasLogoutParam || (function() { 
        try { return localStorage.getItem(STORAGE_KEY) === '1'; } catch (_) { return false; } 
    })();
    
    const alertEl = document.getElementById('logoutAlert');
    const alertText = document.getElementById('logoutAlertText');
    const alertClose = document.getElementById('logoutAlertClose');

    if (shouldShow && alertEl && alertText) {
        alertText.textContent = MESSAGE;
        alertEl.classList.remove('hidden');
    }

    if (alertClose) {
        alertClose.addEventListener('click', function() {
            alertEl.classList.add('hidden');
            try { localStorage.removeItem(STORAGE_KEY); } catch (_) {}
        });
    }
}

// === PERFORMANCE TRACKING ===
function initPerformanceTracking() {
    // Interceptar console.log para enviar al backend
    const originalLog = console.log;
    const logBuffer = [];
    let logTimer;

    console.log = function(...args) {
        originalLog.apply(console, args);
        
        // Agregar al buffer
        logBuffer.push({
            level: 'info',
            message: args.join(' '),
            timestamp: Date.now(),
            url: window.location.href
        });
        
        // Enviar batch cada 100ms
        clearTimeout(logTimer);
        logTimer = setTimeout(() => {
            if (logBuffer.length > 0) {
                fetch('/api/tracking/console-log', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ logs: [...logBuffer] })
                }).catch(() => {});
                logBuffer.length = 0;
            }
        }, 100);
    };

    // Medir tiempo de carga (sin bloquear)
    window.addEventListener('load', function() {
        requestAnimationFrame(() => {
            if (window.performance) {
                const timing = window.performance.timing;
                const pageLoadTime = timing.loadEventEnd - timing.navigationStart;
                console.log('Tiempo de carga total: ' + pageLoadTime + 'ms');
                
                // Enviar métricas de rendimiento si es > 500ms
                if (pageLoadTime > 500) {
                    setTimeout(() => {
                        fetch('/api/tracking/performance', {
                            method: 'POST',
                            headers: { 'Content-Type': 'application/json' },
                            body: JSON.stringify({
                                loadTime: pageLoadTime,
                                url: window.location.href,
                                timestamp: Date.now()
                            })
                        }).catch(() => {});
                    }, 500);
                }
            }
        });
    });
}
