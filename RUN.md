# How to Run the Project

Project structure:
- `d:\Final year project\backend`
- `d:\Final year project\frontend`
- `d:\Final year project\mobile-app`

## 1. Backend (Spring Boot)
Open a terminal in the backend folder (`d:\Final year project\backend`) and run:
```powershell
./mvnw spring-boot:run
```
*Alternatively, you can run the `ManagementSystemApplication` class from your IDE.*

## 2. Frontend (React + Vite)
Open a **new** terminal and run the following commands:
```powershell
cd d:\Final year project\frontend
npm install
npm run dev
```
*Frontend URL: `http://localhost:5173`*

## 3. Mobile App (Expo)
> [!IMPORTANT]
> **Port Conflict Warning**: The backend is configured to use port `8082`. Start Expo on a different port to avoid collisions.

Open a **third** terminal and run:
```powershell
cd d:\Final year project\mobile-app
npm install
npx expo start --port 8081
```

### Running on Device
*   **USB Device (Recommended)**: Run `adb reverse tcp:8082 tcp:8082` first to connect to the backend, then press `a` in the Expo terminal.
*   **Physical (Wi-Fi)**: Use the Expo Go app to scan the QR code. You will need to update the IP in `mobile-app/src/services/api.js`.

---

## Database Repair / Reset Helpers
If you have issues with stale data or roles, use the files in the `tools/` folder:
- `tools/schema_repair.sql`: Fixes enum values.
- `tools/reset_logins.sql`: Wipes demo data for a fresh start.

Detailed notes: `docs/BACKEND_OPERATIONS.md` and `docs/FINAL_YEAR_SCOPE.md`
