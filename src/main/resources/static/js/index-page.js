(function () {
  const DEFAULT_ZOOM = 15;

  function init() {
    initLucide();
    initOrganizationMaps();
  }

  function initLucide() {
    if (window.lucide && typeof window.lucide.createIcons === 'function') {
      window.lucide.createIcons();
    }
  }

  function initOrganizationMaps() {
    if (typeof L === 'undefined') {
      console.warn('[index-page] Leaflet no está disponible.');
      return;
    }

    const mapElements = document.querySelectorAll('.org-map');
    mapElements.forEach(createOrganizationMap);
  }

  function createOrganizationMap(element) {
    const lat = parseFloat(element.getAttribute('data-lat'));
    const lng = parseFloat(element.getAttribute('data-lng'));

    if (!Number.isFinite(lat) || !Number.isFinite(lng)) {
      console.warn('[index-page] Coordenadas inválidas para mapa', { elementId: element.id, lat, lng });
      return;
    }

    const name = element.getAttribute('data-name') || 'Organización';

    // Limpiar contenido previo (placeholder)
    element.innerHTML = '';

    const map = L.map(element, {
      zoomControl: false,
      attributionControl: false,
      dragging: false,
      touchZoom: false,
      scrollWheelZoom: false,
      doubleClickZoom: false,
      boxZoom: false,
      keyboard: false
    }).setView([lat, lng], DEFAULT_ZOOM);

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 18
    }).addTo(map);

    L.marker([lat, lng])
      .addTo(map)
      .bindPopup(`<b>${name}</b><br/>Ubicación de la organización`);

    // Asegurar tamaño correcto tras renderizado
    setTimeout(() => map.invalidateSize(), 0);
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }
})();
