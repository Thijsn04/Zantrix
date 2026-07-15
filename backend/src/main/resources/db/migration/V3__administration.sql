-- Non-clinical deployment configuration. Clinical and directory data remains FHIR.
create table feature_flag (
    name        varchar(128) primary key,
    enabled     boolean      not null,
    updated_at  timestamptz  not null,
    updated_by  varchar(255) not null
);

insert into feature_flag (name, enabled, updated_at, updated_by) values
    ('patients', true, current_timestamp, 'migration'),
    ('scheduling', true, current_timestamp, 'migration'),
    ('clinical-core', true, current_timestamp, 'migration'),
    ('medication-safety', true, current_timestamp, 'migration');
