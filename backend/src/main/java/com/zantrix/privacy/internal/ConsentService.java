package com.zantrix.privacy.internal;

import com.zantrix.platform.fhir.FhirAccessGateway;
import com.zantrix.platform.fhir.FhirBundles;
import com.zantrix.privacy.ConsentRequest;
import com.zantrix.privacy.ConsentSummary;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Consent;
import org.hl7.fhir.r4.model.Period;
import org.hl7.fhir.r4.model.Reference;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
public class ConsentService {
    private final FhirAccessGateway fhir;
    public ConsentService(FhirAccessGateway fhir) { this.fhir=fhir; }

    public ConsentSummary create(ConsentRequest request) {
        if (request.end()!=null && !request.end().isAfter(request.start()))
            throw new IllegalArgumentException("Consent end must be after start");
        Consent.ConsentProvisionType type;
        try { type=Consent.ConsentProvisionType.fromCode(request.type().toLowerCase(Locale.ROOT)); }
        catch (Exception failure) { throw new IllegalArgumentException("type must be permit or deny",failure); }
        Consent consent=new Consent(); consent.setId(UUID.randomUUID().toString());
        consent.setStatus(Consent.ConsentState.ACTIVE);
        consent.setScope(new CodeableConcept(new Coding("http://terminology.hl7.org/CodeSystem/consentscope","patient-privacy","Privacy Consent")));
        consent.addCategory(new CodeableConcept(new Coding("http://loinc.org","59284-0","Patient Consent")));
        consent.setPatient(new Reference("Patient/"+request.patientId()));
        consent.setDateTime(new Date());
        if(request.policyUri()!=null&&!request.policyUri().isBlank()) consent.addPolicy().setUri(request.policyUri());
        consent.getProvision().setType(type).setPeriod(new Period().setStart(Date.from(request.start()))
                .setEnd(request.end()==null?null:Date.from(request.end())));
        request.resourceTypes().forEach(resourceType->consent.getProvision().addClass_(new Coding(
                "http://hl7.org/fhir/resource-types",resourceType,resourceType)));
        fhir.create(consent,request.patientId()); return summary(consent);
    }

    public List<ConsentSummary> list(String patientId) {
        return FhirBundles.resources(fhir.search("Consent", Map.of("patient",List.of(patientId),"_sort",List.of("-date"),"_count",List.of("100")),patientId),Consent.class)
                .stream().map(ConsentService::summary).toList();
    }
    public ConsentSummary revoke(String id,String patientId) {
        Consent consent=fhir.read(Consent.class,id,patientId);
        if(consent.getStatus()!=Consent.ConsentState.ACTIVE) throw new IllegalArgumentException("Only active consent can be revoked");
        consent.setStatus(Consent.ConsentState.INACTIVE); fhir.update(consent,patientId); return summary(consent);
    }
    private static ConsentSummary summary(Consent consent) {
        Period period=consent.getProvision().getPeriod();
        return new ConsentSummary(consent.getIdElement().getIdPart(),consent.getPatient().getReferenceElement().getIdPart(),
                consent.getStatus().toCode(),consent.getProvision().getType().toCode(),
                consent.getProvision().getClass_().stream().map(Coding::getCode).toList(),
                period.getStart()==null?null:period.getStart().toInstant(),period.getEnd()==null?null:period.getEnd().toInstant());
    }
}
