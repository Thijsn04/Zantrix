create table emergency_access_review (
    id             uuid         primary key,
    occurred_at    timestamptz  not null,
    actor          varchar(255) not null,
    reason_code    varchar(64)  not null,
    patient_id     varchar(255) not null,
    resource_type  varchar(255) not null,
    resource_id    varchar(255),
    task_id        varchar(255),
    status         varchar(16)  not null,
    reviewed_at    timestamptz,
    reviewed_by    varchar(255),
    outcome_code   varchar(64)
);

create index idx_emergency_review_status on emergency_access_review (status, occurred_at);
create index idx_emergency_review_patient on emergency_access_review (patient_id, occurred_at);
