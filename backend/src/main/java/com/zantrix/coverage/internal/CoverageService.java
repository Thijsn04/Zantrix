package com.zantrix.coverage.internal;

import com.zantrix.coverage.CoverageRequest;
import com.zantrix.coverage.CoverageSummary;
import com.zantrix.platform.fhir.FhirAccessGateway;
import com.zantrix.platform.fhir.FhirBundles;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Coverage;
import org.hl7.fhir.r4.model.Period;
import org.hl7.fhir.r4.model.Reference;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * A patient's insurance coverage.
 *
 * This records what the patient has, not what a payer will pay. Eligibility
 * checking against a national payer belongs to a regional adapter pack and is
 * deliberately absent, so nothing here should be read as a confirmation that a
 * treatment is covered.
 */
@Service
public class CoverageService {

    private static final String COVERAGE_TYPE = "http://terminology.hl7.org/CodeSystem/v3-ActCode";
    private static final String RELATIONSHIP = "http://terminology.hl7.org/CodeSystem/subscriber-relationship";
    private static final String GROUP_CLASS = "http://terminology.hl7.org/CodeSystem/coverage-class";
    private static final Set<String> RELATIONSHIPS = Set.of("self", "spouse", "child", "parent", "common", "other");

    private final FhirAccessGateway fhir;

    public CoverageService(FhirAccessGateway fhir) { this.fhir = fhir; }

    public CoverageSummary add(CoverageRequest request) {
        if (!RELATIONSHIPS.contains(request.relationship())) {
            throw new IllegalArgumentException("relationship must be one of " + RELATIONSHIPS);
        }
        if (request.end() != null && !request.end().isAfter(request.start())) {
            throw new IllegalArgumentException("Coverage must end after it starts");
        }

        Coverage coverage = new Coverage();
        coverage.setId(UUID.randomUUID().toString());
        coverage.setStatus(Coverage.CoverageStatus.ACTIVE);
        coverage.setBeneficiary(new Reference("Patient/" + request.patientId()));
        coverage.setSubscriberId(request.subscriberId());
        coverage.setType(new CodeableConcept(new Coding(
                COVERAGE_TYPE, request.typeCode(), request.typeDisplay())));
        coverage.setRelationship(new CodeableConcept(new Coding(
                RELATIONSHIP, request.relationship(), request.relationship())));
        coverage.addPayor(new Reference("Organization/" + request.payorOrganizationId())
                .setDisplay(request.payorDisplay()));

        Period period = new Period().setStart(toDate(request.start()));
        if (request.end() != null) {
            period.setEnd(toDate(request.end()));
        }
        coverage.setPeriod(period);

        if (request.groupNumber() != null && !request.groupNumber().isBlank()) {
            coverage.addClass_()
                    .setType(new CodeableConcept(new Coding(GROUP_CLASS, "group", "Group")))
                    .setValue(request.groupNumber());
        }

        fhir.create(coverage, request.patientId());
        return summary(coverage);
    }

    public List<CoverageSummary> list(String patientId, boolean includeCancelled) {
        List<Coverage> found = FhirBundles.resources(fhir.search("Coverage", Map.of(
                "beneficiary", List.of("Patient/" + patientId), "_count", List.of("100")),
                patientId), Coverage.class);
        return found.stream()
                .filter(value -> includeCancelled || value.getStatus() == Coverage.CoverageStatus.ACTIVE)
                .map(CoverageService::summary)
                .toList();
    }

    /** Ends a policy without removing it, so what applied at the time of care stays readable. */
    public CoverageSummary cancel(String id, String patientId) {
        Coverage coverage = fhir.read(Coverage.class, id, patientId);
        if (coverage.getStatus() != Coverage.CoverageStatus.ACTIVE) {
            throw new IllegalArgumentException("Only active coverage can be cancelled");
        }
        coverage.setStatus(Coverage.CoverageStatus.CANCELLED);
        if (!coverage.hasPeriod() || !coverage.getPeriod().hasEnd()) {
            coverage.getPeriod().setEnd(new Date());
        }
        fhir.update(coverage, patientId);
        return summary(coverage);
    }

    private static Date toDate(LocalDate value) {
        return Date.from(value.atStartOfDay(ZoneOffset.UTC).toInstant());
    }

    private static LocalDate toLocalDate(Date value) {
        return value == null ? null : value.toInstant().atZone(ZoneOffset.UTC).toLocalDate();
    }

    private static CoverageSummary summary(Coverage coverage) {
        Coding type = coverage.getType().getCodingFirstRep();
        Reference payor = coverage.getPayor().isEmpty() ? null : coverage.getPayorFirstRep();
        String group = coverage.getClass_().stream()
                .filter(value -> "group".equals(value.getType().getCodingFirstRep().getCode()))
                .map(Coverage.ClassComponent::getValue)
                .findFirst().orElse(null);
        return new CoverageSummary(
                coverage.getIdElement().getIdPart(),
                coverage.getBeneficiary().getReferenceElement().getIdPart(),
                coverage.getStatus() == null ? null : coverage.getStatus().toCode(),
                type.getCode(), type.getDisplay(),
                payor == null ? null : payor.getDisplay(),
                payor == null ? null : payor.getReferenceElement().getIdPart(),
                coverage.getRelationship().getCodingFirstRep().getCode(),
                coverage.hasSubscriberId() ? coverage.getSubscriberId() : null,
                group,
                coverage.hasPeriod() ? toLocalDate(coverage.getPeriod().getStart()) : null,
                coverage.hasPeriod() ? toLocalDate(coverage.getPeriod().getEnd()) : null);
    }
}
