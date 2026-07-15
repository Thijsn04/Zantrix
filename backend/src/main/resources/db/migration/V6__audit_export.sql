alter table audit_event add column export_status varchar(16) not null default 'PENDING';
alter table audit_event add column fhir_audit_event_id varchar(255);
create index idx_audit_event_export on audit_event (export_status, id);
