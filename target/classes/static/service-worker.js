/* ── PAWS Service Worker ── */
const CACHE_VERSION = 'v1';
const SHELL_CACHE   = `paws-shell-${CACHE_VERSION}`;
const TILE_CACHE    = `paws-tiles-${CACHE_VERSION}`;
const DATA_CACHE    = `paws-data-${CACHE_VERSION}`;
const SYNC_TAG      = 'sync-incidents';

const SHELL_ASSETS = [
  '/',
  '/index.html',
  '/manifest.json',
  '/css/app.css',
  '/js/app.js',
  'https://unpkg.com/leaflet@1.9.4/dist/leaflet.css',
  'https://unpkg.com/leaflet@1.9.4/dist/leaflet.js',
  'https://cdn.jsdelivr.net/npm/sockjs-client@1.6.1/dist/sockjs.min.js',
  'https://cdn.jsdelivr.net/npm/stompjs@2.3.3/lib/stomp.min.js',
];

// ── Install: cache app shell ──
self.addEventListener('install', (event) => {
  event.waitUntil(
    caches.open(SHELL_CACHE).then((cache) => cache.addAll(SHELL_ASSETS))
  );
  self.skipWaiting();
});

// ── Activate: clean stale caches ──
self.addEventListener('activate', (event) => {
  event.waitUntil(
    caches.keys().then((keys) =>
      Promise.all(
        keys
          .filter((k) => k !== SHELL_CACHE && k !== TILE_CACHE && k !== DATA_CACHE)
          .map((k) => caches.delete(k))
      )
    )
  );
  self.clients.claim();
});

// ── Fetch: routing strategy ──
self.addEventListener('fetch', (event) => {
  const { request } = event;
  const url = new URL(request.url);

  // OSM tile requests — cache-first, pre-warm on first load
  if (url.hostname.includes('tile.openstreetmap.org')) {
    event.respondWith(tileFirst(request));
    return;
  }

  // API incidents GET — network-first, cache fallback
  if (url.pathname === '/api/incidents' && request.method === 'GET') {
    event.respondWith(networkFirstData(request));
    return;
  }

  // App shell — cache-first
  if (request.method === 'GET') {
    event.respondWith(cacheFirst(request));
  }
});

// ── Background Sync: replay queued community incident submissions ──
self.addEventListener('sync', (event) => {
  if (event.tag === SYNC_TAG) {
    event.waitUntil(replayPendingIncidents());
  }
});

// ── Strategies ──
async function cacheFirst(request) {
  const cached = await caches.match(request);
  if (cached) return cached;
  try {
    const response = await fetch(request);
    if (response.ok) {
      const cache = await caches.open(SHELL_CACHE);
      cache.put(request, response.clone());
    }
    return response;
  } catch {
    return new Response('Offline', { status: 503 });
  }
}

async function tileFirst(request) {
  const cached = await caches.match(request);
  if (cached) return cached;
  try {
    const response = await fetch(request);
    if (response.ok) {
      const cache = await caches.open(TILE_CACHE);
      cache.put(request, response.clone());
    }
    return response;
  } catch {
    return new Response('Tile unavailable offline', { status: 503 });
  }
}

async function networkFirstData(request) {
  try {
    const response = await fetch(request);
    if (response.ok) {
      const cache = await caches.open(DATA_CACHE);
      cache.put(request, response.clone());
    }
    return response;
  } catch {
    const cached = await caches.match(request);
    return cached || new Response('[]', { headers: { 'Content-Type': 'application/json' } });
  }
}

// ── IndexedDB helpers ──
function openDb() {
  return new Promise((resolve, reject) => {
    const req = indexedDB.open('paws-offline', 1);
    req.onupgradeneeded = (e) => {
      e.target.result.createObjectStore('pending-incidents', { autoIncrement: true });
    };
    req.onsuccess = () => resolve(req.result);
    req.onerror   = () => reject(req.error);
  });
}

async function getPendingIncidents() {
  const db = await openDb();
  return new Promise((resolve, reject) => {
    const tx    = db.transaction('pending-incidents', 'readonly');
    const store = tx.objectStore('pending-incidents');
    const req   = store.getAll();
    req.onsuccess = () => resolve(req.result);
    req.onerror   = () => reject(req.error);
  });
}

async function clearPendingIncidents() {
  const db = await openDb();
  return new Promise((resolve, reject) => {
    const tx    = db.transaction('pending-incidents', 'readwrite');
    const store = tx.objectStore('pending-incidents');
    const req   = store.clear();
    req.onsuccess = () => resolve();
    req.onerror   = () => reject(req.error);
  });
}

async function replayPendingIncidents() {
  const pending = await getPendingIncidents();
  if (!pending.length) return;

  for (const payload of pending) {
    await fetch('/api/incidents', {
      method:  'POST',
      headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${payload.token}` },
      body:    JSON.stringify(payload.incident),
    });
  }
  await clearPendingIncidents();
}

