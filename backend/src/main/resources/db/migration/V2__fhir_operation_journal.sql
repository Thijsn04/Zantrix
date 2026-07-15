-- Durable, privacy-safe journal for FHIR mutations that cross the HAPI and
-- Zantrix database boundary. No resource body or clinical content is stored.

create table fhir_operation_journal (
    id              uuid         primary key,
    started_at      timestamptz  not null,
    updated_at      timestamptz  not null,
    actor           varchar(255) not null,
    source_ip       varchar(64),
    operation       varchar(16)  not null,
    resource_type   varchar(255) not null,
    resource_id     varchar(255),
    patient_id      varchar(255),
    state           varchar(32)  not null,
    error_code      varchar(128),
    attempts        integer      not null default 0
);

create index idx_fhir_operation_journal_state
    on fhir_operation_journal (state, updated_at);

create index idx_fhir_operation_journal_patient
    on fhir_operation_journal (patient_id, started_at);
