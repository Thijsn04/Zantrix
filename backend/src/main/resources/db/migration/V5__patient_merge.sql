create table patient_merge (
    id            uuid         primary key,
    source_id     varchar(255) not null,
    target_id     varchar(255) not null,
    status        varchar(16)  not null,
    prepared_at   timestamptz  not null,
    merged_at     timestamptz,
    merged_by     varchar(255) not null,
    unmerged_at   timestamptz,
    unmerged_by   varchar(255),
    manifest      text         not null,
    error_code    varchar(128),
    lock_version  bigint       not null default 0
);

create unique index idx_patient_merge_active_source
    on patient_merge (source_id) where status in ('PREPARED', 'MERGED');
create index idx_patient_merge_target on patient_merge (target_id, prepared_at);
