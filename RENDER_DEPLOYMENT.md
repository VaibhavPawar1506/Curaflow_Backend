# Render Web Service Deployment: CuraFlow Backend

This backend is prepared for deployment as a manual Render **Web Service** using Docker. It does not require a Render Blueprint.

## What Is Configured

- Spring Boot reads Render's `PORT` environment variable with a safe default of `10000`.
- PostgreSQL settings come from environment variables: `DB_URL`, `DB_USER`, and `DB_PASS`.
- `JWT_SECRET` is required in production and no longer has a hardcoded fallback.
- SQL debug logging is disabled by default for cleaner Render logs.
- File uploads default to `/tmp/curaflow/prescriptions`, which is ephemeral on Render unless you attach persistent storage.
- The Dockerfile builds the Maven app and runs the packaged Spring Boot jar.

## 1. Prepare PostgreSQL

Create a PostgreSQL database before deploying the web service. You can use Render PostgreSQL or another hosted PostgreSQL provider.

You need these values:

- Database host
- Database port, usually `5432`
- Database name
- Database username
- Database password

Your backend expects the JDBC URL format:

```text
jdbc:postgresql://<host>:5432/<database_name>
```

If Render gives you a URL like `postgresql://user:pass@host:5432/db`, convert it to the JDBC format above and put the username/password into `DB_USER` and `DB_PASS`.

## 2. Create The Render Web Service

In Render:

1. Click **New +**.
2. Choose **Web Service**.
3. Connect the GitHub repository that contains this backend.
4. Choose the branch you want to deploy.
5. Set **Runtime** to **Docker**.
6. If the repository root is this `backend` folder, leave **Root Directory** empty.
7. If this backend is inside a larger monorepo on GitHub, set **Root Directory** to `backend`.
8. Use the default Dockerfile path: `Dockerfile`.
9. Choose an instance type.
10. Add the environment variables below.
11. Click **Create Web Service**.

## 3. Required Environment Variables

Set these in the Render Web Service environment tab:

```text
PORT=10000
DB_URL=jdbc:postgresql://<host>:5432/<database_name>
DB_USER=<database_user>
DB_PASS=<database_password>
JWT_SECRET=<base64_32_byte_or_longer_secret>
JWT_EXPIRATION_MS=86400000
DB_DDL_AUTO=update
DEFAULT_HOSPITAL_CODE=HOSP001
```

Generate `JWT_SECRET` locally with PowerShell:

```powershell
[Convert]::ToBase64String((1..32 | ForEach-Object { Get-Random -Maximum 256 }))
```

Or with OpenSSL:

```bash
openssl rand -base64 32
```

## 4. Optional Environment Variables

```text
JPA_SHOW_SQL=false
HIBERNATE_FORMAT_SQL=false
JPA_OPEN_IN_VIEW=false
FILE_UPLOAD_DIR=/tmp/curaflow/prescriptions
```

Use a Render persistent disk if uploaded prescription files must survive redeploys/restarts. If you attach a disk, set `FILE_UPLOAD_DIR` to a path on that disk.

## 5. Local Validation

From this backend folder:

```powershell
.\mvnw.cmd -q -DskipTests compile
.\mvnw.cmd test
```

Local Docker smoke test:

```powershell
docker build -t curaflow-backend .
docker run --rm -p 10000:10000 `
  -e PORT=10000 `
  -e DB_URL=jdbc:postgresql://host.docker.internal:5432/healthcare_db `
  -e DB_USER=postgres `
  -e DB_PASS=tiger `
  -e JWT_SECRET=<base64-secret> `
  curaflow-backend
```

Then open:

```text
http://localhost:10000/swagger-ui.html
```

## 6. After Deployment

Render will give you a public backend URL like:

```text
https://curaflow-backend-api.onrender.com
```

Update the frontend and mobile app API base URLs to use that deployed backend URL.
