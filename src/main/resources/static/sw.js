/* SW mínimo: Chrome exige un fetch handler para que la app sea instalable
   (ver docs/MEJORAS.md — PWA mínimo). NO cachea nada: en activate limpia los
   caches heredados del SW viejo (era PWA completa, descartada por stale-cache)
   y todo request va directo a la red. */
self.addEventListener('install', function () {
  self.skipWaiting();
});

self.addEventListener('activate', function (event) {
  event.waitUntil(
    caches.keys()
      .then(function (names) { return Promise.all(names.map(function (n) { return caches.delete(n); })); })
      .then(function () { return self.clients.claim(); })
  );
});

self.addEventListener('fetch', function (event) {
  event.respondWith(fetch(event.request));
});
