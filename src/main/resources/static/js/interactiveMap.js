// Función global para inicializar el mapa de organizaciones
function initOrganizationsMap(elementId, organizations) {
    console.log('Inicializando mapa de organizaciones...', organizations);
    
    // Verificar si el elemento existe
    const mapElement = document.getElementById(elementId);
    if (!mapElement) {
        console.error('Elemento del mapa no encontrado:', elementId);
        return;
    }
    
    // Coordenadas por defecto (Uruguay)
    const defaultLat = -30.9054;
    const defaultLng = -55.5511;
    
    // Inicializar el mapa
    const map = L.map(elementId).setView([defaultLat, defaultLng], 7);
    
    // Agregar capa de OpenStreetMap
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '© OpenStreetMap contributors'
    }).addTo(map);
    
    // Invalidar tamaño para asegurar renderizado correcto
    setTimeout(() => {
        map.invalidateSize();
    }, 100);
    
    // Crear grupo de marcadores
    const markersGroup = L.layerGroup().addTo(map);
    
    // Función para mostrar información de organización
    function showOrgInfo(org) {
        const infoElement = document.getElementById('selectedOrgInfo');
        if (!infoElement) return;
        
        document.getElementById('selectedOrgName').textContent = org.name || 'Sin nombre';
        document.getElementById('selectedOrgAddress').textContent = org.address || 'Dirección no especificada';
        document.getElementById('selectedOrgCoords').textContent = 
            'Lat: ' + (org.lat || 'N/A') + ', Lng: ' + (org.lng || 'N/A');
        infoElement.classList.remove('hidden');
    }
    
    // Agregar marcadores para cada organización
    if (organizations && organizations.length > 0) {
        const bounds = [];
        
        organizations.forEach(function(org, index) {
            if (org.lat && org.lng) {
                const lat = parseFloat(org.lat);
                const lng = parseFloat(org.lng);
                
                if (isNaN(lat) || isNaN(lng)) {
                    console.warn('Coordenadas inválidas para organización:', org);
                    return;
                }
                
                // Crear marcador personalizado
                const marker = L.marker([lat, lng], {
                    title: org.name || 'Organización'
                });
                
                // Popup con información
                const popupContent = `
                    <div class="p-2">
                        <h4 class="font-semibold text-gray-900 mb-1">${org.name || 'Sin nombre'}</h4>
                        <p class="text-sm text-gray-600 mb-2">${org.address || 'Dirección no especificada'}</p>
                        <div class="text-xs text-gray-500 mb-2">
                            <span class="inline-flex items-center px-2 py-1 rounded-full bg-green-100 text-green-800">
                                ✓ Activa
                            </span>
                        </div>
                        <div class="text-xs text-gray-400">
                            📧 ${org.email || 'Email no disponible'}
                        </div>
                    </div>
                `;
                
                marker.bindPopup(popupContent);
                
                // Evento de clic
                marker.on('click', function() {
                    showOrgInfo(org);
                });
                
                markersGroup.addLayer(marker);
                bounds.push([lat, lng]);
            }
        });
        
        // Ajustar vista para mostrar todos los marcadores
        if (bounds.length > 0) {
            if (bounds.length === 1) {
                map.setView(bounds[0], 13);
            } else {
                try {
                    map.fitBounds(bounds, { padding: [20, 20] });
                } catch (e) {
                    console.error('Error al ajustar límites del mapa:', e);
                    map.setView([defaultLat, defaultLng], 7);
                }
            }
        }
        
        // Actualizar contador
        const countElement = document.getElementById('orgCount');
        if (countElement) {
            countElement.textContent = bounds.length + ' organizaciones';
        }
    } else {
        console.log('No hay organizaciones con coordenadas válidas');
    }
    
    // Evento de clic en el mapa para ocultar info
    map.on('click', function() {
        const infoElement = document.getElementById('selectedOrgInfo');
        if (infoElement) {
            infoElement.classList.add('hidden');
        }
    });
    
    console.log('Mapa de organizaciones inicializado correctamente');
    return map;
}
