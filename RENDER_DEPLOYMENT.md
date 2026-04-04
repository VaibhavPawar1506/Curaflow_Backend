# Render Deployment Guide: CuraFlow Backend

This guide walks you through preparing and deploying the Java Spring Boot backend (`backend`) to Render with Docker.

The backend is configured through environment variables, includes a multi-stage `Dockerfile`, and ships with `render.yaml` so the service can be created through a Render Blueprint.

## What Was Changed
- Extracted MySQL credentials, `JWT_SECRET`, and `server.port` into environment variables (`DB_URL`, `DB_USER`, `DB_PASS`, `PORT`, `JWT_SECRET`)
- Removed the hardcoded JWT fallback so production requires `JWT_SECRET`
- Added a multi-stage `Dockerfile` for Render builds
- Updated `render.yaml` to deploy the backend as a Docker web service on Render

## 1. Database Setup First
Render does not natively provision MySQL like it does PostgreSQL, so you should:
- provision a MySQL database on an external provider such as Aiven, PlanetScale, or a hosted MySQL instance.
- alternatively, deploy a MySQL Docker container on Render (more complex).

Once provisioned, note the host, port, database name, username, and password.

`spring.jpa.hibernate.ddl-auto=update` is still enabled by default, so on first startup Hibernate will create or update the schema automatically and the seeded demo accounts can be inserted by the backend startup flow.

## 2. Deploying to Render via GitHub
### Next Steps for GitHub
1. Commit the backend changes, including `application.properties` and `render.yaml` from the `backend` folder.
2. Push your code to the branch you want Render to track.

### Next Steps on Render
1. Sign in to [Render.com](https://dashboard.render.com).
2. Open **Blueprints** and create a new Blueprint instance.
3. Connect your GitHub repository and point Render to `backend/render.yaml` if it does not auto-detect the file.
4. When Render reads `render.yaml`, provide these environment variables:
   - `DB_URL`: `jdbc:mysql://<your-cloud-db-host>:3306/<db_name>?useSSL=false&allowPublicKeyRetrieval=true`
   - `DB_USER`: your MySQL username
   - `DB_PASS`: your MySQL password
   - `PORT`: `10000` if you want to set it explicitly
5. Apply the Blueprint.

Render will build the image from `backend/Dockerfile` and inject the service port through `PORT`. The provided `render.yaml` asks Render to generate a random base64 `JWT_SECRET` automatically during initial Blueprint creation.

## 3. Local Docker Smoke Test
From the `backend` directory, build and run locally with:

```bash
docker build -t curaflow-backend .
docker run --rm -p 10000:10000 -e PORT=10000 -e JWT_SECRET=<base64-secret> -e DB_URL=jdbc:mysql://host:3306/db -e DB_USER=<user> -e DB_PASS=<pass> curaflow-backend
```

## 4. Updating the Frontend
Once Render deploys the backend, it will provide a public URL such as `https://curaflow-backend-api.onrender.com`.

Update the frontend and mobile app API base URLs to point at that deployed backend:
- `frontend/src/api/api.js`
- `mobile-app/src/api/api.js`
