# Backend Operations

## Schema Repair

If you are running the backend on MySQL, the scripts in `tools/` can help repair or clear the schema and seeded data.

- `tools/schema_repair.sql` (MySQL-only)

This updates:

- `users.role`
- `hospitals.status`
- physical verification columns on `hospitals`

## Reset Login Data

You can wipe all login-related demo data in either of these ways:

1. Run SQL directly with:
   - `tools/reset_logins.sql`
2. Run the JDBC helper:
   - `tools/ResetLogins.java`

## Reseeding Demo Accounts

After a reset, start the backend again. `DataSeeder` will recreate the seeded accounts automatically.

## Important Runtime Assumptions

- Database: `healthcare_db`
- User: `root`
- Password: `password`
- Backend port: `8082`

## Verification Flow Summary

Hospital onboarding is now:

1. Hospital registers
2. Status becomes `PENDING`
3. Supervisor schedules inspection
4. Status becomes `INSPECTION_SCHEDULED`
5. Supervisor completes physical verification
6. Status becomes `PHYSICALLY_VERIFIED`
7. Final approval sets status to `APPROVED`
