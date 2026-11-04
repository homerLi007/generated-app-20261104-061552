# Safe File Deleter

A full-stack demo application for safely deleting files within a restricted directory. The backend enforces a configurable `allowed_root` boundary and refuses symlinks, non-regular files, and any path that escapes the root. The React frontend provides a simple UI for submitting paths and viewing per-path results.

## Stack

Spring Boot 3.2.5 / Java 17 / React 18 / Vite 5

## Layout

```
.
├── backend/
│   ├── pom.xml
│   └── src/
│       └── main/
│           ├── java/com/example/demo/
│           │   ├── DemoApplication.java
│           │   └── controller/ApiController.java
│           └── resources/application.properties
└── frontend/
    ├── index.html
    ├── package.json
    ├── vite.config.js
    └── src/
        ├── main.jsx
        ├── App.jsx
        └── App.css
```

## How to run

### Backend

```bash
cd backend
mvn spring-boot:run
```

The API listens on `http://localhost:8080`. Files are resolved relative to `app.allowed-root` (default `./allowed_root`), which must exist and be readable.

### Frontend

```bash
cd frontend
npm install
npm run dev
```

The Vite dev server runs on `http://localhost:5173` and proxies `/api` requests to the backend.

## API

### `POST /api/delete`

Deletes the given relative paths under `allowed_root`. Each path is validated independently; invalid paths are reported in `refused` rather than failing the whole request.

**Request body**

```json
{
  "paths": ["docs/old.txt", "tmp/cache.bin"]
}
```

**Response `200 OK`**

```json
{
  "deleted": ["docs/old.txt"],
  "refused": ["tmp/cache.bin"]
}
```

**Response `400 Bad Request`** — when `paths` is missing or null.

**Response `500 Internal Server Error`** — when `allowed_root` cannot be resolved.

A path is refused if it is blank, resolves outside `allowed_root` (after normalization and `toRealPath`), is a symbolic link, or is not a regular file.

## Notes

- **No authentication or authorization.** The API is intended for local/demo use only; do not expose it on an untrusted network.
- **No CSRF protection or rate limiting.** Add Spring Security and appropriate controls before any production use.
- **Deletion is permanent.** There is no trash/undo mechanism; `Files.delete` is called directly.
- **`allowed_root` must exist at startup.** The controller resolves it via `toRealPath()` on each request and returns `500` if it is missing or inaccessible.
- **Path traversal is mitigated but not a substitute for OS-level permissions.** Run the backend under a user whose filesystem access is already scoped to the intended directory.
- **Frontend is a dev-mode SPA.** `vite.config.js` proxies `/api` to `localhost:8080`; for production, build with `npm run build` and serve the output from the backend's `static/` directory (or a reverse proxy).
- **No persistence layer.** The project has no database; state is limited to the filesystem.