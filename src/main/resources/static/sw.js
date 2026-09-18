/* Kill-switch: el PWA/Service Worker fue descartado (ver docs/MEJORAS.md #11).
   Este script solo existe para desinstalarse a sí mismo en navegadores que
   todavía tengan el Service Worker viejo activo (registrado antes del 15/9). */
self.addEventListener('install', function (event) {
  self.skipWaiting();
});

self.addEventListener('activate', function (event) {
  event.waitUntil(
    caches.keys()
      .then(function (cacheNames) {
        return Promise.all(cacheNames.map(function (name) { return caches.delete(name); }));
      })
      .then(function () { return self.registration.unregister(); })
      .then(function () { return self.clients.matchAll(); })
      .then(function (clients) {
        clients.forEach(function (client) { client.navigate(client.url); });
      })
  );
});
