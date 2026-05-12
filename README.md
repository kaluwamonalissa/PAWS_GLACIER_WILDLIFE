# PAWS Glacier Wildlife

PAWS (Protected Area Wildlife System) is a Spring Boot backend with a Progressive Web App (PWA) frontend for wildlife incident reporting and response.

This prototype is configured for Hwange National Park workflows and supports role-based access for community users, rangers, team leaders, managers, senior managers, and analysts.

## Current MVP Capabilities

- JWT login and role-based authorization
- User registration and basic forgot-password SMS stub endpoint
- Incident lifecycle workflow:
  - create incident
  - assign/respond
  - update/resolve
  - mark false alarm/duplicate
- Dashboard summary and CSV export
- Heatmap endpoint for high-risk area visualization
- Patrol logging endpoint support
- Local photo upload storage in `uploads/`
- PWA service worker with:
  - shell caching
  - OpenStreetMap tile caching
  - incident GET cache fallback
  - background sync replay for queued incident reports

## Tech Stack

- Java 17
- Spring Boot 3.4.x
- Spring Security + JWT
- Spring Data JPA
- H2 in-memory database
- Static frontend (HTML/CSS/JS) served by Spring Boot
- Leaflet + OpenStreetMap tiles

## Project Layout

```text
PAWS_GLACIER_WILDLIFE/
├── src/main/java/za/co/mwm/paws/paws/
│   ├── config/
│   ├── controller/
│   ├── domain/
│   ├── dto/
│   ├── repository/
│   ├── security/
│   └── service/
├── src/main/resources/
│   ├── application.yaml
│   └── static/
│       ├── index.html
│       ├── css/app.css
│       ├── js/app.js
│       └── service-worker.js
├── src/test/
├── uploads/
└── README.md
```

## Running Locally

From `PAWS_GLACIER_WILDLIFE/`:

```bash
./mvnw spring-boot:run
```

App URL:

- `http://localhost:8080`

## Getting Started (Postman Demo)

Use the Postman assets in `docs/postman/` to demo the backend quickly.

### 1) Start the backend

From `PAWS_GLACIER_WILDLIFE/`:

```bash
./mvnw spring-boot:run
```

Wait for startup to complete, then confirm the app is reachable at:

- `http://localhost:8080`

### 2) Import Postman files

Import these files into Postman:

- `docs/postman/PAWS_Demo.postman_collection.json`
- `docs/postman/PAWS_Demo.postman_environment.json`

Select the environment:

- `PAWS Demo Local`

### 3) Run requests in demo order

Recommended folder order in the collection:

1. `00 - Authentication`
2. `01 - Community Demo Flow`
3. `02 - Manager Demo Flow`
4. `03 - Ranger Demo Flow`
5. `04 - Analyst Demo Flow`
6. `05 - Senior Manager Admin`

Notes:

- The authentication requests store JWT tokens automatically into environment variables.
- `Create Incident (Rancher)` stores `incidentId` for assignment/respond/update requests.
- `Get All Users` (Senior Manager folder) captures `newUserId` for role updates and activation/deactivation.

### 4) Seed users for login

On first startup, seed data creates demo users in `src/main/java/za/co/mwm/paws/paws/config/DataInitializer.java`.

Default password:

- `password`

Seeded usernames:

- `seniormanager`
- `manager`
- `analyst`
- `teamleader`
- `ranger1`
- `ranger2`
- `rancher1`
- `rancher2`

### 5) Common troubleshooting

- `401 Unauthorized`: run `00 - Authentication` first and ensure `PAWS Demo Local` is selected.
- `403 Forbidden`: the endpoint requires a different role; use the matching login request.
- `incidentId` empty: run `Create Incident (Rancher)` again to set it.
- Connection refused: confirm the backend is running on `http://localhost:8080`.

## Database and Seed Data

- Database: H2 (`jdbc:h2:mem:pawsdb`)
- H2 Console: `http://localhost:8080/h2-console`
- Seed initializer class: `src/main/java/za/co/mwm/paws/paws/config/DataInitializer.java`

On first startup, seed data creates demo users, incidents, responder assignments, and a patrol.

Default seeded password for demo users:

- `password`

Seeded usernames:

- `seniormanager`
- `manager`
- `analyst`
- `teamleader`
- `ranger1`
- `ranger2`
- `rancher1`
- `rancher2`

## Roles in the System

Defined in `src/main/java/za/co/mwm/paws/paws/domain/Role.java`:

- `RANCHER`
- `RANGER`
- `TEAM_LEADER`
- `MANAGER`
- `SENIOR_MANAGER`
- `ANALYST`

## Key API Endpoints

Authentication (`/api/auth`):

- `POST /login`
- `POST /register`
- `POST /forgot-password?username=...` (stub)

Incidents (`/api/incidents`):

- `POST /`
- `GET /`
- `GET /mine`
- `PATCH /{id}/respond`
- `PATCH /{id}/update`
- `PATCH /{id}/assign/{rangerId}`
- `PATCH /{id}/flag?status=...`
- `GET /heatmap`

Dashboard (`/api/dashboard`):

- `GET /summary`
- `GET /export` (CSV)

## Uploads

Incident images are saved to:

- `uploads/`

Ensure this folder is writable by the running process.

## PWA and Offline Behavior

Service worker file:

- `src/main/resources/static/service-worker.js`

Implemented behavior:

- Caches core app shell assets
- Caches OpenStreetMap tiles for offline map reuse
- Uses network-first with cache fallback for incident reads
- Replays queued incident submissions using Background Sync (`sync-incidents`)

## Test

```bash
./mvnw test
```

## Notes

- This is a prototype and uses local/in-memory defaults for speed of iteration.
- JWT secret and operational settings are in `src/main/resources/application.yaml` and should be externalized for production.
