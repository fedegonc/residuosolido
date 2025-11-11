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
    const MAX_RETRIES = 6; // ~3s total si interval=500ms
    const RETRY_INTERVAL = 500;

    function tryInit(attempt = 1) {
      if (typeof L === 'undefined') {
        if (attempt === 1) {
          console.warn('[index-page] Leaflet no está disponible. Reintentando...');
        }
        if (attempt < MAX_RETRIES) {
          setTimeout(() => tryInit(attempt + 1), RETRY_INTERVAL);
        } else {
          console.warn('[index-page] Leaflet no se cargó a tiempo. Se mantiene fallback.');
        }
        return;
      }

      const mapElements = document.querySelectorAll('.org-map');
      mapElements.forEach(createOrganizationMap);
    }

    tryInit();
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

  // Ejecutar tras DOM listo; y si la ventana ya cargó, garantiza que Leaflet esté disponible
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }
  window.addEventListener('load', () => initOrganizationMaps());
})();
