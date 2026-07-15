package com.zantrix.platform.fhir.internal;

import com.zantrix.audit.AuditExportSource;
import com.zantrix.audit.AuditRecord;
import org.hl7.fhir.r4.model.AuditEvent;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Reference;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Component
class AuditEventExporter {
    private final AuditExportSource source;private final FhirTransport transport;private final FhirMutationJournal journal;
    AuditEventExporter(AuditExportSource source,FhirTransport transport,FhirMutationJournal journal){
        this.source=source;this.transport=transport;this.journal=journal;
    }
    @Scheduled(fixedDelayString="${zantrix.fhir.audit-export-interval:PT1M}")
    public void export(){for(AuditRecord record:source.pendingExport(100)){try{export(record);}catch(RuntimeException ignored){return;}}}
    private void export(AuditRecord record){
        AuditEvent event=toFhir(record);String id=event.getIdElement().getIdPart();
        UUID operation=journal.begin(FhirOperation.CREATE,"AuditEvent",id,record.patientId());
        try{transport.create(event);journal.remoteSucceeded(operation,id);
            // The source relational event is itself the audit for this deterministic export.
            journal.audited(operation);source.markExported(record.id(),id);
        }catch(RuntimeException failure){journal.remoteFailed(operation,failure);throw failure;}
    }
    private static AuditEvent toFhir(AuditRecord record){
        AuditEvent event=new AuditEvent();event.setId(UUID.nameUUIDFromBytes(("zantrix-audit:"+record.id())
                .getBytes(StandardCharsets.UTF_8)).toString());
        event.setType(new Coding("http://terminology.hl7.org/CodeSystem/audit-event-type","rest","RESTful Operation"));
        event.setAction(AuditEvent.AuditEventAction.fromCode(action(record.action())));
        event.setRecorded(Date.from(record.recordedAt()));
        event.setOutcome("SUCCESS".equals(record.outcome())?AuditEvent.AuditEventOutcome._0:AuditEvent.AuditEventOutcome._8);
        event.addAgent().setRequestor(true).setWho(new Reference().setIdentifier(new Identifier()
                .setSystem("https://zantrix.org/identity/subject").setValue(record.actor())));
        event.getSource().setObserver(new Reference().setIdentifier(new Identifier()
                .setSystem("https://zantrix.org/system").setValue("zantrix-backend")));
        if(record.entityType()!=null&&record.entityId()!=null)event.addEntity().setWhat(new Reference(record.entityType()+"/"+record.entityId()));
        if(record.patientId()!=null)event.addEntity().setWhat(new Reference("Patient/"+record.patientId()));
        return event;
    }
    private static String action(String value){return switch(value){case"CREATE"->"C";case"READ"->"R";case"UPDATE"->"U";case"DELETE"->"D";default->"E";};}
}
