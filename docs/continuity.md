# Availability, emergency access and maintenance

Reconstructed from the owner-supplied handover dated 15 October 2026. This document records design requirements and their stated limits; it does not establish implemented functionality.

[Documentation index](README.md)

## Foundational requirements

Availability, privacy and security are crucial.
The owner explicitly accepted the need to keep usable local capabilities available during internet failure where the infrastructure remains reachable.
Differentiate:

- Internet failure.
- Application failure.
- Database failure.
- Identity-provider failure.
- Local-network failure.
- Power failure.

## External transactions

For external communication:

- Preserve pending work durably where appropriate.
- Show accurate delivery state.
- Retry safely.
- Prevent duplicate processing.

Queued is not the same as delivered.

## Local emergency provision

The owner requested a local emergency server/workstation concept inspired by hospital BCA/downtime computers.
This also applies to sites using central Zantrix hosting.

The accepted [Emergency Read Contract and independent reader design](emergency-read-design.md) makes a working versioned tested contract mandatory before production activation for every persistent patient/dossier-data owner (R135–R142). It adds manifest classification/exemption evidence, standardized baseline rendering without module runtime code, transactional-outbox-triggered projections, atomic durable packages, verified patient/owner coverage and independent audit reconciliation. Exact protocols/security profiles and runtime proof remain open.

## Read-only

The emergency provision is read-only.
It must not:

- Change clinical records.
- Prescribe.
- Create clinical orders.

Care delivered during an outage requires a separate downtime documentation and reconciliation procedure.

## Dataset

The owner prefers access to everything in the patient record.
The intended goal includes:

- Clinical content.
- Documents.
- History.

Precise handling remains open for:

- Medical images.
- Externally hosted documents.
- Referenced data.
- Full resource history.
- Patient selection per site.

Do not claim completeness without a defined and monitored dataset. The reader must know its expected data owners and show missing/failed/unsupported sections explicitly. Verified no records is different from never received or outside site scope. The emergency contract requirement does not silently settle the dataset exceptions above.

## Freshness target

Continuously synchronize.
The owner accepted:
At most five minutes behind the primary system under healthy connectivity.

This is a design target, not a measured SLA.
During outage the copy naturally gets older.
Display:

- Emergency mode.
- Last successful synchronization.
- Data age.
- Completeness.
- Missing or failed content.

## Offline security

The emergency provision requires:

- Encryption.
- Protected keys.
- Authentication without the failed primary service.
- Appropriate offline authorization.
- Local audit.
- Controlled reconciliation after recovery.

The detailed implementation is open.

## Independence

The emergency reader must not require live access to:

- Primary Core.
- Primary Bridge.
- Primary database.
- Unavailable external identity provider.

It may reuse code, not live dependencies.

## Planned maintenance

The owner accepts scheduled downtime.

Behavior/data-changing updates are scheduled by an authorized administrator or by Zantrix under an institution's prior managed-hosting mandate. Automatic checking/downloading does not authorize installation. Institutions can trial updates in test before production. Urgent security-update handling and mandatory testing thresholds remain open; see [module lifecycle](module-lifecycle.md#update-authorization-and-maintenance).
Before maintenance:

- Check compatibility.
- Check backups and restoration readiness.
- Check the emergency provision.
- Check freshness and completeness.
- Communicate appropriately.

After maintenance:

- Verify migrations.
- Verify representative functionality.
- Release access deliberately.

Downgrading the application binary is not automatically a safe rollback after a database migration.

## Open operational targets

Still to determine:

- Recovery time objective.
- Recovery point objective.
- Backup retention.
- Restoration tests.
- Hardware requirements.
- Power/network resilience.
- Alerts.
- Responsibility.
- Service commitments.

The five-minute source-coverage target does not replace backups or define a backup recovery point objective. Atomic installation of one module package does not prove a consistent whole dossier; cross-owner cutoff/dependency handling needs explicit evidence. Hash-chained local logs require protected/anchored integrity and idempotent recovery ingestion and do not establish absolute completeness or standards compliance.
