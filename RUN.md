# How to Run the Project

Project structure:
- `d:\Final year project\backend`
- `d:\Final year project\frontend`
- `d:\Final year project\mobile-app`

## 1. Backend (Spring Boot)
Open a terminal in the backend folder (`d:\Final year project\backend`) and run:

```powershell
$env:JWT_SECRET="MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY="
.\mvnw.cmd spring-boot:run
```

The backend defaults to `http://localhost:10000`. It also expects PostgreSQL locally at:

```text
jdbc:postgresql://localhost:5432/healthcare_db
username: postgres
password: tiger
```

You can override those values in PowerShell before starting:

```powershell
$env:DB_URL="jdbc:postgresql://localhost:5432/healthcare_db"
$env:DB_USER="postgres"
$env:DB_PASS="tiger"
$env:PORT="10000"
```

Alternatively, you can run the `ManagementSystemApplication` class from your IDE after setting the same environment variables.

## 2. Frontend (React + Vite)
Open a **new** terminal and run the following commands:

```powershell
cd d:\Final year project\frontend
npm install
npm run dev
```

Frontend URL: `http://localhost:5173`

## 3. Mobile App (Expo)
Open a **third** terminal and run:

```powershell
cd d:\Final year project\mobile-app
npm install
npx expo start --port 8081
```

### Running on Device
- USB Device: Run `adb reverse tcp:10000 tcp:10000` first to connect to the backend, then press `a` in the Expo terminal.
- Physical Wi-Fi: Use the Expo Go app to scan the QR code. Update the IP in the mobile app API config if needed.

## Database Repair / Reset Helpers
If you have issues with stale data or roles, use the files in the `tools/` folder:

- `tools/schema_repair.sql`: Fixes enum values.
- `tools/reset_logins.sql`: Wipes demo data for a fresh start.

Detailed notes: `docs/BACKEND_OPERATIONS.md` and `docs/FINAL_YEAR_SCOPE.md`.
