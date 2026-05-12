/* ── PAWS App — Main JS ── */
'use strict';

// ─── Constants ───────────────────────────────────────────────────────────────
const API_BASE       = '/api';
const STOMP_ENDPOINT = '/ws';
const SYNC_TAG       = 'sync-incidents';

// Hwange Main Camp 20km² bounding box
const HWANGE_CENTER  = [-18.93, 26.48];
const HWANGE_BOUNDS  = [[-18.98, 26.43], [-18.88, 26.53]];
const MAP_ZOOM_MIN   = 12;
const MAP_ZOOM_MAX   = 16;
const MAP_ZOOM_INIT  = 13;

const INCIDENT_COLORS = {
  POACHING:                '#c0392b',
  HUMAN_WILDLIFE_CONFLICT: '#a0522d',
  VELD_FIRE:               '#e67e22',
  WATER_PAN:               '#2980b9',
  FORAGE:                  '#2d5a27',
};

const INCIDENT_LABELS = {
  POACHING:                '🔴 Poaching',
  HUMAN_WILDLIFE_CONFLICT: '🟤 Human-Wildlife',
  VELD_FIRE:               '🟠 Veld Fire',
  WATER_PAN:               '🔵 Water Pan',
  FORAGE:                  '🟢 Forage',
};

// ─── State ───────────────────────────────────────────────────────────────────
let authToken   = null;
let currentUser = null;
let stompClient = null;
let maps        = {};
let activeFilter = 'ALL';
let allIncidents = [];

// ─── IndexedDB ───────────────────────────────────────────────────────────────
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

async function queueIncident(incident) {
  const db = await openDb();
  return new Promise((resolve, reject) => {
    const tx    = db.transaction('pending-incidents', 'readwrite');
    const store = tx.objectStore('pending-incidents');
    const req   = store.add({ incident, token: authToken });
    req.onsuccess = () => resolve();
    req.onerror   = () => reject(req.error);
  });
}

// ─── Auth helpers ─────────────────────────────────────────────────────────────
function decodeJwt(token) {
  try {
    return JSON.parse(atob(token.split('.')[1]));
  } catch {
    return null;
  }
}

function saveSession(data) {
  authToken   = data.token;
  currentUser = data;
  localStorage.setItem('paws_token',    data.token);
  localStorage.setItem('paws_user',     JSON.stringify(data));
}

function loadSession() {
  authToken   = localStorage.getItem('paws_token');
  const raw   = localStorage.getItem('paws_user');
  currentUser = raw ? JSON.parse(raw) : null;
}

function clearSession() {
  authToken   = null;
  currentUser = null;
  localStorage.removeItem('paws_token');
  localStorage.removeItem('paws_user');
}

// ─── HTTP helpers ─────────────────────────────────────────────────────────────
async function apiGet(path) {
  const resp = await fetch(API_BASE + path, {
    headers: { Authorization: `Bearer ${authToken}` },
  });
  if (!resp.ok) throw new Error(await resp.text());
  return resp.json();
}

async function apiPost(path, body) {
  const resp = await fetch(API_BASE + path, {
    method:  'POST',
    headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${authToken}` },
    body:    JSON.stringify(body),
  });
  if (!resp.ok) throw new Error(await resp.text());
  return resp.json();
}

async function apiPatch(path) {
  const resp = await fetch(API_BASE + path, {
    method:  'PATCH',
    headers: { Authorization: `Bearer ${authToken}` },
  });
  if (!resp.ok) throw new Error(await resp.text());
  return resp.json();
}

async function apiPostForm(path, formData) {
  const resp = await fetch(API_BASE + path, {
    method:  'POST',
    headers: { Authorization: `Bearer ${authToken}` },
    body:    formData,
  });
  if (!resp.ok) throw new Error(await resp.text());
  return resp.json();
}

// ─── Screen routing ───────────────────────────────────────────────────────────
function showScreen(id) {
  document.querySelectorAll('.screen').forEach((s) => {
    s.classList.remove('active');
    s.classList.add('hidden');
  });
  const target = document.getElementById(id);
  target.classList.remove('hidden');
  target.classList.add('active');
}

function routeByRole(role) {
  if (role === 'COMMUNITY') {
    showScreen('screen-community');
    initCommunityMap();
  } else if (role === 'MANAGER' || role === 'SENIOR_MANAGER') {
    showScreen('screen-manager');
    if (role === 'SENIOR_MANAGER') {
      document.getElementById('admin-tab-btn').classList.remove('hidden');
    }
    initManagerDashboard();
  } else {
    // RANGER or SUPERVISOR_RANGER
    showScreen('screen-ranger');
    initRangerDashboard();
  }
}

// ─── Map factory ──────────────────────────────────────────────────────────────
function createMap(containerId, center, zoom) {
  if (maps[containerId]) {
    maps[containerId].remove();
    delete maps[containerId];
  }
  const map = L.map(containerId, {
    center,
    zoom,
    minZoom: MAP_ZOOM_MIN,
    maxZoom: MAP_ZOOM_MAX,
    maxBounds: HWANGE_BOUNDS,
    maxBoundsViscosity: 1.0,
  });
  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '© OpenStreetMap contributors',
    maxZoom: MAP_ZOOM_MAX,
  }).addTo(map);
  maps[containerId] = map;
  return map;
}

function incidentMarker(incident) {
  const color = INCIDENT_COLORS[incident.type] || '#666';
  return L.circleMarker([incident.latitude, incident.longitude], {
    radius:      10,
    fillColor:   color,
    color:       '#fff',
    weight:      2,
    opacity:     1,
    fillOpacity: 0.85,
  }).bindPopup(`
    <strong>${INCIDENT_LABELS[incident.type] || incident.type}</strong><br>
    ${incident.description}<br>
    <em>By: ${incident.reportedBy}</em><br>
    <span class="incident-status status-${incident.status}">${incident.status}</span>
  `);
}

// ─── Ranger Dashboard ─────────────────────────────────────────────────────────
async function initRangerDashboard() {
  loadWildlifeCards();
  loadRangerIncidents();

  // Tab navigation
  document.querySelectorAll('#screen-ranger .nav-btn').forEach((btn) => {
    btn.addEventListener('click', () => {
      document.querySelectorAll('#screen-ranger .nav-btn').forEach((b) => b.classList.remove('active'));
      btn.classList.add('active');
      const tabId = btn.dataset.tab;
      document.querySelectorAll('#screen-ranger .tab-panel').forEach((p) => p.classList.remove('active'));
      document.getElementById(tabId).classList.add('active');

      if (tabId === 'ranger-map') initRangerMap();
      if (tabId === 'ranger-report') initSightingMap();
    });
  });
}

async function loadWildlifeCards() {
  const categories = await apiGet('/wildlife/categories');
  const grid = document.getElementById('wildlife-cards');
  grid.innerHTML = categories.map((c) => `
    <div class="wildlife-card risk-${c.riskLevel}">
      <span class="risk-badge ${c.riskLevel}">${c.riskLevel}</span>
      <h4>${c.category}</h4>
      <div class="species-list">${c.species.join('<br>')}</div>
    </div>
  `).join('');
}

async function loadRangerIncidents() {
  const incidents = await apiGet('/incidents');
  const list = document.getElementById('ranger-incident-list');
  list.innerHTML = incidents.filter((i) => i.status !== 'RESOLVED').map((i) => `
    <div class="incident-card type-${i.type}">
      <div class="incident-header">
        <span class="incident-type-label">${INCIDENT_LABELS[i.type] || i.type}</span>
        <span class="incident-status status-${i.status}">${i.status}</span>
      </div>
      <p class="incident-description">${i.description}</p>
      <div class="incident-meta">
        <span>📍 ${i.latitude.toFixed(4)}, ${i.longitude.toFixed(4)}</span>
        <span>👤 ${i.reportedBy}</span>
        <span>🕐 ${formatTime(i.timestamp)}</span>
      </div>
      ${i.responders && i.responders.length ? `<div class="incident-meta">🧑‍✈️ ${i.responders.join(', ')}</div>` : ''}
      <div class="incident-actions">
        ${i.status === 'OPEN' ? `<button class="btn btn-sm btn-respond" onclick="respondToIncident(${i.id})">Respond</button>` : ''}
      </div>
    </div>
  `).join('') || '<p style="color:var(--color-text-muted)">No active incidents.</p>';
}

function initRangerMap() {
  setTimeout(async () => {
    const map = createMap('map-ranger', HWANGE_CENTER, MAP_ZOOM_INIT);
    const incidents = await apiGet('/incidents');
    incidents.forEach((i) => incidentMarker(i).addTo(map));
    // Pre-warm tile cache for offline use
    prewarmTiles(map);
  }, 100);
}

function initSightingMap() {
  setTimeout(() => {
    const map = createMap('map-sighting', HWANGE_CENTER, MAP_ZOOM_INIT);
    let marker = null;
    map.on('click', (e) => {
      if (marker) marker.remove();
      marker = L.marker([e.latlng.lat, e.latlng.lng]).addTo(map);
      document.getElementById('sighting-lat').value = e.latlng.lat;
      document.getElementById('sighting-lng').value  = e.latlng.lng;
    });
    // Auto-detect GPS
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition((pos) => {
        const lat = pos.coords.latitude;
        const lng = pos.coords.longitude;
        document.getElementById('sighting-lat').value = lat;
        document.getElementById('sighting-lng').value  = lng;
        if (marker) marker.remove();
        marker = L.marker([lat, lng]).addTo(map);
        map.setView([lat, lng], MAP_ZOOM_INIT);
      });
    }
  }, 100);
}

window.respondToIncident = async (id) => {
  try {
    await apiPatch(`/incidents/${id}/respond`);
    loadRangerIncidents();
    showToast('You are now responding to incident #' + id);
  } catch (e) {
    showToast('Failed to respond: ' + e.message, 'danger');
  }
};

// ─── Manager Dashboard ────────────────────────────────────────────────────────
async function initManagerDashboard() {
  loadManagerIncidents();
  connectWebSocket();

  document.querySelectorAll('#screen-manager .nav-btn').forEach((btn) => {
    btn.addEventListener('click', () => {
      document.querySelectorAll('#screen-manager .nav-btn').forEach((b) => b.classList.remove('active'));
      btn.classList.add('active');
      const tabId = btn.dataset.tab;
      document.querySelectorAll('#screen-manager .tab-panel').forEach((p) => p.classList.remove('active'));
      document.getElementById(tabId).classList.add('active');

      if (tabId === 'manager-map')       initManagerHeatmap();
      if (tabId === 'manager-responders') loadResponderPanel();
      if (tabId === 'manager-admin')      loadAdminPanel();
    });
  });

  // Filter bar
  document.querySelectorAll('.filter-btn').forEach((btn) => {
    btn.addEventListener('click', () => {
      document.querySelectorAll('.filter-btn').forEach((b) => b.classList.remove('active'));
      btn.classList.add('active');
      activeFilter = btn.dataset.filter;
      renderManagerIncidents();
    });
  });
}

async function loadManagerIncidents() {
  allIncidents = await apiGet('/incidents');
  renderManagerIncidents();
}

function renderManagerIncidents() {
  const filtered = activeFilter === 'ALL'
    ? allIncidents
    : allIncidents.filter((i) => i.type === activeFilter);

  const list = document.getElementById('manager-incident-list');
  list.innerHTML = filtered.map((i) => `
    <div class="incident-card type-${i.type}">
      <div class="incident-header">
        <span class="incident-type-label">${INCIDENT_LABELS[i.type] || i.type}</span>
        <span class="incident-status status-${i.status}">${i.status}</span>
      </div>
      <p class="incident-description">${i.description}</p>
      <div class="incident-meta">
        <span>📍 ${i.latitude.toFixed(4)}, ${i.longitude.toFixed(4)}</span>
        <span>👤 ${i.reportedBy}</span>
        <span>🕐 ${formatTime(i.timestamp)}</span>
      </div>
      ${i.responders && i.responders.length ? `<div class="incident-meta" style="margin-top:4px">🧑‍✈️ <strong>Responders:</strong> ${i.responders.join(', ')}</div>` : ''}
    </div>
  `).join('') || '<p style="color:var(--color-text-muted)">No incidents found.</p>';
}

function initManagerHeatmap() {
  setTimeout(async () => {
    const map = createMap('map-manager', HWANGE_CENTER, MAP_ZOOM_INIT);
    const points = await apiGet('/incidents/heatmap');
    points.forEach((p) => {
      L.circleMarker([p.latitude, p.longitude], {
        radius:      Math.max(12, (p.count || 1) * 6),
        fillColor:   INCIDENT_COLORS[p.type] || '#666',
        color:       'transparent',
        fillOpacity: 0.45,
      }).addTo(map);
    });
    // Also add crisp markers on top
    const incidents = await apiGet('/incidents');
    incidents.forEach((i) => incidentMarker(i).addTo(map));
    prewarmTiles(map);
  }, 100);
}

async function loadResponderPanel() {
  const incidents = await apiGet('/incidents');
  const active = incidents.filter((i) => i.status === 'IN_PROGRESS');
  const list = document.getElementById('responder-list');
  list.innerHTML = active.length
    ? active.map((i) => `
        <div class="incident-card type-${i.type}">
          <div class="incident-header">
            <span class="incident-type-label">${INCIDENT_LABELS[i.type]}</span>
            <span class="incident-status status-${i.status}">${i.status}</span>
          </div>
          <p class="incident-description">${i.description}</p>
          <div class="incident-meta">🧑‍✈️ <strong>Responding:</strong> ${(i.responders || []).join(', ') || 'None assigned'}</div>
        </div>
      `).join('')
    : '<p style="color:var(--color-text-muted)">No rangers currently responding.</p>';
}

async function loadAdminPanel() {
  const users = await apiGet('/admin/users');
  const list  = document.getElementById('user-admin-list');
  const roles = ['COMMUNITY', 'RANGER', 'SUPERVISOR_RANGER', 'MANAGER'];
  list.innerHTML = users.map((u) => `
    <div class="user-admin-row">
      <div class="user-info">
        <strong>${u.fullName}</strong>
        <small>${u.username} · ${u.role}</small>
      </div>
      <select class="field" onchange="updateUserRole(${u.id}, this.value)" style="width:auto;padding:6px 8px">
        ${roles.map((r) => `<option value="${r}" ${r === u.role ? 'selected' : ''}>${r.replace('_', ' ')}</option>`).join('')}
      </select>
    </div>
  `).join('');
}

window.updateUserRole = async (id, role) => {
  try {
    await fetch(`${API_BASE}/admin/users/${id}/role?role=${role}`, {
      method:  'PATCH',
      headers: { Authorization: `Bearer ${authToken}` },
    });
    showToast(`Role updated to ${role}`);
  } catch (e) {
    showToast('Failed to update role', 'danger');
  }
};

// ─── WebSocket ────────────────────────────────────────────────────────────────
function connectWebSocket() {
  const socket = new SockJS(STOMP_ENDPOINT);
  stompClient = Stomp.over(socket);
  stompClient.debug = null;
  stompClient.connect({}, () => {
    stompClient.subscribe('/topic/incidents', (msg) => {
      const incident = JSON.parse(msg.body);
      // Update local list
      const idx = allIncidents.findIndex((i) => i.id === incident.id);
      if (idx >= 0) allIncidents[idx] = incident;
      else allIncidents.unshift(incident);
      renderManagerIncidents();
      showToast(`New update: ${INCIDENT_LABELS[incident.type] || incident.type} — ${incident.status}`, 'warning');
    });
  }, () => {
    // Reconnect after 5s on error
    setTimeout(connectWebSocket, 5000);
  });
}

// ─── Community Dashboard ──────────────────────────────────────────────────────
function initCommunityMap() {
  setTimeout(() => {
    const map = createMap('map-community', HWANGE_CENTER, MAP_ZOOM_INIT);
    let marker = null;
    map.on('click', (e) => {
      if (marker) marker.remove();
      marker = L.marker([e.latlng.lat, e.latlng.lng]).addTo(map);
      document.getElementById('comm-lat').value = e.latlng.lat;
      document.getElementById('comm-lng').value  = e.latlng.lng;
    });
  }, 150);
}

async function submitCommunityReport(form) {
  const type        = document.getElementById('comm-type').value;
  const description = document.getElementById('comm-description').value;
  const lat         = parseFloat(document.getElementById('comm-lat').value);
  const lng         = parseFloat(document.getElementById('comm-lng').value);

  if (!type || !description || isNaN(lat) || isNaN(lng)) {
    showFieldError('community-error', 'Please fill in all fields and pin a location on the map.');
    return;
  }

  const payload = { type, description, latitude: lat, longitude: lng };

  if (!navigator.onLine) {
    await queueIncident(payload);
    if ('serviceWorker' in navigator && 'SyncManager' in window) {
      const reg = await navigator.serviceWorker.ready;
      await reg.sync.register(SYNC_TAG);
    }
    document.getElementById('community-offline-notice').classList.remove('hidden');
    showSuccessScreen();
    return;
  }

  try {
    await apiPost('/incidents', payload);
    showSuccessScreen();
  } catch (e) {
    showFieldError('community-error', 'Submission failed. Please try again.');
  }
}

function showSuccessScreen() {
  document.getElementById('form-community-report').classList.add('hidden');
  document.getElementById('community-success').classList.remove('hidden');
}

// ─── Toast ────────────────────────────────────────────────────────────────────
function showToast(message, type = '') {
  const container = document.getElementById('toast-container');
  if (!container) return;
  const toast = document.createElement('div');
  toast.className = `toast ${type ? 'toast-' + type : ''}`;
  toast.textContent = message;
  container.appendChild(toast);
  setTimeout(() => toast.remove(), 4000);
}

// ─── Utils ────────────────────────────────────────────────────────────────────
function showFieldError(elementId, message) {
  const el = document.getElementById(elementId);
  el.textContent = message;
  el.classList.remove('hidden');
}

function hideFieldError(elementId) {
  const el = document.getElementById(elementId);
  el.classList.add('hidden');
}

function formatTime(ts) {
  if (!ts) return '';
  const d = new Date(ts);
  return d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) + ' ' + d.toLocaleDateString();
}

function prewarmTiles(map) {
  // Force-load all visible tiles into the tile cache by panning slightly
  setTimeout(() => map.invalidateSize(), 200);
}

// ─── Online/Offline banner ────────────────────────────────────────────────────
window.addEventListener('online',  () => document.getElementById('community-offline-notice')?.classList.add('hidden'));
window.addEventListener('offline', () => document.getElementById('community-offline-notice')?.classList.remove('hidden'));

// ─── Event wiring ─────────────────────────────────────────────────────────────
document.addEventListener('DOMContentLoaded', () => {
  // Service worker registration
  if ('serviceWorker' in navigator) {
    navigator.serviceWorker.register('/service-worker.js').catch(console.error);
  }

  // Restore session
  loadSession();
  if (authToken && currentUser) {
    const decoded = decodeJwt(authToken);
    if (decoded && decoded.exp * 1000 > Date.now()) {
      routeByRole(currentUser.role);
    } else {
      clearSession();
    }
  }

  // ── Login ──
  document.getElementById('form-login').addEventListener('submit', async (e) => {
    e.preventDefault();
    hideFieldError('login-error');
    const username = document.getElementById('login-username').value;
    const password = document.getElementById('login-password').value;
    try {
      const resp = await fetch(`${API_BASE}/auth/login`, {
        method:  'POST',
        headers: { 'Content-Type': 'application/json' },
        body:    JSON.stringify({ username, password }),
      });
      if (!resp.ok) throw new Error('Invalid credentials');
      const data = await resp.json();
      saveSession(data);
      routeByRole(data.role);
    } catch {
      showFieldError('login-error', 'Invalid username or password.');
    }
  });

  // ── Register ──
  document.getElementById('form-register').addEventListener('submit', async (e) => {
    e.preventDefault();
    hideFieldError('register-error');
    const fullName = document.getElementById('reg-fullname').value;
    const username = document.getElementById('reg-username').value;
    const password = document.getElementById('reg-password').value;
    try {
      const resp = await fetch(`${API_BASE}/auth/register`, {
        method:  'POST',
        headers: { 'Content-Type': 'application/json' },
        body:    JSON.stringify({ username, password, fullName }),
      });
      if (!resp.ok) {
        const err = await resp.json();
        throw new Error(err.error || 'Registration failed');
      }
      const data = await resp.json();
      saveSession(data);
      routeByRole(data.role);
    } catch (err) {
      showFieldError('register-error', err.message);
    }
  });

  // Tab switches on auth screen
  document.getElementById('goto-register').addEventListener('click', (e) => {
    e.preventDefault();
    document.getElementById('tab-login').classList.add('hidden');
    document.getElementById('tab-register').classList.remove('hidden');
  });
  document.getElementById('goto-login').addEventListener('click', (e) => {
    e.preventDefault();
    document.getElementById('tab-register').classList.add('hidden');
    document.getElementById('tab-login').classList.remove('hidden');
  });

  // Logout buttons
  ['ranger-logout', 'manager-logout', 'community-logout'].forEach((id) => {
    document.getElementById(id)?.addEventListener('click', () => {
      clearSession();
      if (stompClient) stompClient.disconnect();
      showScreen('screen-auth');
      document.getElementById('tab-login').classList.remove('hidden');
      document.getElementById('tab-register').classList.add('hidden');
    });
  });

  // Sighting form
  document.getElementById('form-sighting').addEventListener('submit', async (e) => {
    e.preventDefault();
    hideFieldError('sighting-error');
    const species     = document.getElementById('sighting-species').value;
    const description = document.getElementById('sighting-description').value;
    const lat         = document.getElementById('sighting-lat').value;
    const lng         = document.getElementById('sighting-lng').value;
    const photoInput  = document.getElementById('sighting-photo');

    if (!lat || !lng) {
      showFieldError('sighting-error', 'Please pin your location on the map.');
      return;
    }

    const formData = new FormData();
    formData.append('species',     species);
    formData.append('description', description);
    formData.append('latitude',    lat);
    formData.append('longitude',   lng);
    if (photoInput.files[0]) formData.append('photo', photoInput.files[0]);

    try {
      await apiPostForm('/sightings', formData);
      e.target.reset();
      showToast('Sighting reported successfully!');
    } catch {
      showFieldError('sighting-error', 'Failed to submit sighting. Try again.');
    }
  });

  // Community report form
  document.getElementById('form-community-report').addEventListener('submit', (e) => {
    e.preventDefault();
    hideFieldError('community-error');
    submitCommunityReport(e.target);
  });

  // Community new report button
  document.getElementById('community-new-report')?.addEventListener('click', () => {
    document.getElementById('community-success').classList.add('hidden');
    document.getElementById('form-community-report').classList.remove('hidden');
    document.getElementById('community-offline-notice').classList.add('hidden');
    document.getElementById('form-community-report').reset();
  });
});

