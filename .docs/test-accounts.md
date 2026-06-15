# Zantrix Test Accounts

This document contains the credentials for the pre-configured test users in the local development environment (Keycloak). 
All test users have the password: **test**

| Username | Roles / Access | Description |
| :--- | :--- | :--- |
| `doctor_test` | `DOCTOR` | Has standard medical access and can trigger Break-the-Glass emergency mode. |
| `nurse_test` | `NURSE` | Has standard nursing access and can trigger Break-the-Glass emergency mode. |
| `privacy_test` | `PRIVACY_OFFICER` | Has administrative privacy access to view audit logs and compliance data. |

> [!NOTE]  
> If you start Keycloak with a fresh database volume, the `realm-export.json` will automatically seed these users into the `zantrix` realm.
