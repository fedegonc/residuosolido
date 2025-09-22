(function () {
  // Default center (Rivera / Sant'Ana do Livramento approx)
  const DEFAULT_CENTER = { lat: -30.895854, lng: -55.535653, zoom: 14 };

  function toNumber(val, fallback) {
    if (val === null || val === undefined) return fallback;
    if (typeof val === 'string' && val.trim() === '') return fallback;
    const n = Number(val);
    return Number.isFinite(n) ? n : fallback;
  }

  function initLeafletWidget(container) {
    const latInputSel = container.getAttribute('data-lat-input');
    const lngInputSel = container.getAttribute('data-lng-input');
    const latInput = latInputSel ? document.querySelector(latInputSel) : null;
    const lngInput = lngInputSel ? document.querySelector(lngInputSel) : null;

    let lat = toNumber(container.getAttribute('data-lat'), NaN);
    let lng = toNumber(container.getAttribute('data-lng'), NaN);
    const zoom = toNumber(container.getAttribute('data-zoom'), DEFAULT_CENTER.zoom);

    if (!Number.isFinite(lat) || !Number.isFinite(lng)) {
      // Try from inputs
      if (latInput && lngInput) {
        lat = toNumber(latInput.value, DEFAULT_CENTER.lat);
        lng = toNumber(lngInput.value, DEFAULT_CENTER.lng);
      } else {
        lat = DEFAULT_CENTER.lat;
        lng = DEFAULT_CENTER.lng;
      }
    }

    // Debug: initial values
    try {
      console.log('[LeafletMap] init', {
        containerId: container.id,
        lat,
        lng,
        zoom,
        latInputSel,
        lngInputSel
      });
    } catch (_) {}

    // Create map
    const map = L.map(container).setView([lat, lng], zoom);

    L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
    }).addTo(map);

    // Marker
    const marker = L.marker([lat, lng], { draggable: true }).addTo(map);

    function updateInputs(latlng) {
      if (latInput) latInput.value = latlng.lat.toFixed(6);
      if (lngInput) lngInput.value = latlng.lng.toFixed(6);
      try {
        console.log('[LeafletMap] updateInputs', { lat: latlng.lat, lng: latlng.lng });
      } catch (_) {}
    }

    // Map click sets marker
    map.on('click', function (e) {
      marker.setLatLng(e.latlng);
      updateInputs(e.latlng);
      try {
        console.log('[LeafletMap] map click', { lat: e.latlng.lat, lng: e.latlng.lng });
      } catch (_) {}
    });

    // Dragging marker updates inputs
    marker.on('dragend', function () {
      const pos = marker.getLatLng();
      updateInputs(pos);
      try {
        console.log('[LeafletMap] marker dragend', { lat: pos.lat, lng: pos.lng });
      } catch (_) {}
    });

    // If inputs change manually, update marker
    function bindInput(input, isLat) {
      if (!input) return;
      input.addEventListener('change', function () {
        const newLat = toNumber(latInput ? latInput.value : lat, lat);
        const newLng = toNumber(lngInput ? lngInput.value : lng, lng);
        if (Number.isFinite(newLat) && Number.isFinite(newLng)) {
          const latlng = { lat: newLat, lng: newLng };
          marker.setLatLng(latlng);
          map.setView(latlng);
          try {
            console.log('[LeafletMap] input change', { lat: newLat, lng: newLng });
          } catch (_) {}
        }
      });
    }

    bindInput(latInput, true);
    bindInput(lngInput, false);

    // Resize handling (in case of tabs/hidden)
    setTimeout(() => {
      map.invalidateSize();
    }, 0);

    // Store reference if needed later
    container._leaflet = { map, marker };
  }

  function initAll() {
    const containers = document.querySelectorAll('.rs-leaflet');
    try { console.log('[LeafletMap] found containers', containers.length); } catch (_) {}
    containers.forEach(initLeafletWidget);
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initAll);
  } else {
    initAll();
  }
})();
