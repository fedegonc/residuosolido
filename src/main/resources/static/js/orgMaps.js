// Script para mapas individuales de organizaciones
document.addEventListener('DOMContentLoaded', function () {
    // Buscar mapas de organizaciones
    const orgMaps = document.querySelectorAll('[id^="orgMap-"]');
    
    orgMaps.forEach(function(mapElement) {
        const orgId = mapElement.id.replace('orgMap-', '');
        const lat = parseFloat(mapElement.dataset.lat);
        const lng = parseFloat(mapElement.dataset.lng);
        const orgName = mapElement.dataset.name;
        
        if (!isNaN(lat) && !isNaN(lng)) {
            // Crear mapa
            const map = L.map(mapElement.id).setView([lat, lng], 15);
            
            // Agregar capa de tiles
            L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                attribution: '© OpenStreetMap contributors'
            }).addTo(map);
            
            // Agregar marcador
            L.marker([lat, lng])
                .bindPopup(`<strong>${orgName}</strong>`)
                .addTo(map);
                
            // Redimensionar mapa
            setTimeout(() => {
                map.invalidateSize();
            }, 100);
        }
    });
});
