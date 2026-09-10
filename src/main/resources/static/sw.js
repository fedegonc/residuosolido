// Service Worker — EcoSolicitud PWA
// Estrategia: cache-first para CSS/JS/imágenes, network-first para HTML.
// Las páginas HTML nunca se pre-cachean para evitar contenido stale.

const CACHE_NAME = 'ecosolicitud-v17';
const STATIC_ASSETS = [
  '/css/app.css',
  '/js/app.js',
  '/manifest.json',
  '/favicon.svg',
  '/icon-192.png',
  '/icon-512.png'
];

self.addEventListener('install', (event) => {
  event.waitUntil(
    caches.open(CACHE_NAME).then((cache) => cache.addAll(STATIC_ASSETS))
  );
  self.skipWaiting();
});

self.addEventListener('activate', (event) => {
  event.waitUntil(
    caches.keys().then((keys) =>
      Promise.all(keys.filter((k) => k !== CACHE_NAME).map((k) => caches.delete(k)))
    )
  );
  self.clients.claim();
});

self.addEventListener('fetch', (event) => {
  const req = event.request;
  if (req.method !== 'GET') return;

  const url = new URL(req.url);
  if (url.origin !== self.location.origin) return;

  // CSS, JS, imágenes: cache-first
  if (req.destination === 'style' || req.destination === 'script' || req.destination === 'image') {
    event.respondWith(
      caches.match(req).then((cached) =>
        cached || fetch(req).then((resp) => {
          const copy = resp.clone();
          caches.open(CACHE_NAME).then((cache) => cache.put(req, copy));
          return resp;
        }).catch(() => cached)
      )
    );
    return;
  }

  // Páginas HTML y API: siempre network-first, sin guardar en cache
  event.respondWith(
    fetch(req)
      .then((resp) => resp)
      .catch(() => caches.match(req).then((cached) => cached || caches.match('/')))
  );
});
