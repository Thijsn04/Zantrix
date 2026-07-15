package com.zantrix.orders.internal;

import com.zantrix.orders.OrderRequest;
import com.zantrix.orders.OrderSummary;
import com.zantrix.orders.ResultMeasurement;
import com.zantrix.orders.ResultRequest;
import com.zantrix.orders.ResultSummary;
import com.zantrix.platform.fhir.FhirAccessGateway;
import com.zantrix.platform.fhir.FhirBundles;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DiagnosticReport;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Quantity;
import org.hl7.fhir.r4.model.Range;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.ServiceRequest;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.Task;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
public class OrderService {

    private static final String UCUM = "http://unitsofmeasure.org";
    private final FhirAccessGateway fhir;

    public OrderService(FhirAccessGateway fhir) { this.fhir = fhir; }

    public OrderSummary place(OrderRequest request) {
        ServiceRequest order = new ServiceRequest();
        order.setId(UUID.randomUUID().toString());
        order.setStatus(ServiceRequest.ServiceRequestStatus.ACTIVE);
        order.setIntent(ServiceRequest.ServiceRequestIntent.ORDER);
        order.setPriority(ServiceRequest.ServiceRequestPriority.fromCode(request.priority().toLowerCase(Locale.ROOT)));
        order.setSubject(new Reference("Patient/" + request.patientId()));
        order.setEncounter(new Reference("Encounter/" + request.encounterId()));
        order.setRequester(new Reference("Practitioner/" + request.requesterId()));
        order.setAuthoredOn(new Date());
        order.setCode(new CodeableConcept(new Coding(request.codeSystem(), request.code(), request.display())));
        order.addCategory(new CodeableConcept(new Coding(
                "http://snomed.info/sct", categoryCode(request.category()), request.category())));
        if (request.clinicalNote() != null && !request.clinicalNote().isBlank()) {
            order.addNote().setText(request.clinicalNote());
        }
        Task task = new Task();
        task.setId(UUID.randomUUID().toString());
        task.setStatus(Task.TaskStatus.REQUESTED);
        task.setIntent(Task.TaskIntent.ORDER);
        task.setFor(new Reference("Patient/" + request.patientId()));
        task.setEncounter(new Reference("Encounter/" + request.encounterId()));
        task.setFocus(new Reference("ServiceRequest/" + order.getIdElement().getIdPart()));
        task.setAuthoredOn(new Date());
        task.setDescription("Perform " + request.display());
        Bundle bundle = new Bundle().setType(Bundle.BundleType.TRANSACTION);
        addPut(bundle, order);
        addPut(bundle, task);
        fhir.transaction(bundle, request.patientId());
        return summary(order, task.getIdElement().getIdPart());
    }

    public List<OrderSummary> list(String patientId, boolean includeCompleted) {
        Map<String, List<String>> parameters = includeCompleted
                ? Map.of("patient", List.of(patientId), "_sort", List.of("-authored"), "_count", List.of("300"))
                : Map.of("patient", List.of(patientId), "status", List.of("active,on-hold,draft"),
                "_sort", List.of("-authored"), "_count", List.of("300"));
        return FhirBundles.resources(fhir.search("ServiceRequest", parameters, patientId),
                ServiceRequest.class).stream().map(order -> summary(order, null)).toList();
    }

    public ResultSummary result(String orderId, ResultRequest request) {
        ServiceRequest order = fhir.read(ServiceRequest.class, orderId, request.patientId());
        if (order.getStatus() != ServiceRequest.ServiceRequestStatus.ACTIVE
                && order.getStatus() != ServiceRequest.ServiceRequestStatus.ONHOLD) {
            throw new IllegalArgumentException("Only an active order can be resulted");
        }
        List<Observation> observations = request.measurements().stream()
                .map(measurement -> observation(order, request, measurement)).toList();
        DiagnosticReport report = report(order, request, observations);
        order.setStatus(ServiceRequest.ServiceRequestStatus.COMPLETED);
        List<Task> tasks = FhirBundles.resources(fhir.search("Task", Map.of(
                "focus", List.of("ServiceRequest/" + orderId), "_count", List.of("20")),
                request.patientId()), Task.class);
        tasks.forEach(task -> {
            task.setStatus(Task.TaskStatus.COMPLETED);
            task.setLastModified(new Date());
            task.addOutput().setType(new CodeableConcept().setText("Diagnostic report"))
                    .setValue(new Reference("DiagnosticReport/" + report.getIdElement().getIdPart()));
        });
        Task review = resultReviewTask(order, report);
        Bundle transaction = new Bundle().setType(Bundle.BundleType.TRANSACTION);
        observations.forEach(observation -> addPut(transaction, observation));
        addPut(transaction, report);
        addPut(transaction, order);
        tasks.forEach(task -> addPut(transaction, task));
        addPut(transaction, review);
        fhir.transaction(transaction, request.patientId());
        return resultSummary(report);
    }

    public List<ResultSummary> results(String patientId) {
        return FhirBundles.resources(fhir.search("DiagnosticReport", Map.of(
                "patient", List.of(patientId), "_sort", List.of("-date"), "_count", List.of("300")),
                patientId), DiagnosticReport.class).stream().map(OrderService::resultSummary).toList();
    }

    private static Observation observation(ServiceRequest order, ResultRequest request,
                                           ResultMeasurement measurement) {
        Observation observation = new Observation();
        observation.setId(UUID.randomUUID().toString());
        observation.setStatus(Observation.ObservationStatus.FINAL);
        observation.addCategory(new CodeableConcept(new Coding(
                "http://terminology.hl7.org/CodeSystem/observation-category", "laboratory", "Laboratory")));
        observation.setCode(new CodeableConcept(new Coding(measurement.codeSystem(), measurement.code(),
                measurement.display())));
        observation.setSubject(order.getSubject());
        observation.setEncounter(order.getEncounter());
        observation.addBasedOn(new Reference("ServiceRequest/" + order.getIdElement().getIdPart()));
        observation.addPerformer(new Reference("Practitioner/" + request.performerId()));
        observation.setEffective(new org.hl7.fhir.r4.model.DateTimeType(Date.from(request.issuedAt())));
        observation.setIssued(Date.from(request.issuedAt()));
        if (measurement.numericValue() != null) {
            observation.setValue(new Quantity().setValue(measurement.numericValue()).setUnit(measurement.unit())
                    .setSystem(UCUM).setCode(measurement.unit()));
            if (measurement.referenceLow() != null || measurement.referenceHigh() != null) {
                Range range = new Range();
                if (measurement.referenceLow() != null) {
                    range.setLow(new Quantity().setValue(measurement.referenceLow()).setSystem(UCUM)
                            .setCode(measurement.unit()).setUnit(measurement.unit()));
                }
                if (measurement.referenceHigh() != null) {
                    range.setHigh(new Quantity().setValue(measurement.referenceHigh()).setSystem(UCUM)
                            .setCode(measurement.unit()).setUnit(measurement.unit()));
                }
                observation.addReferenceRange().setLow(range.getLow()).setHigh(range.getHigh());
            }
        } else {
            observation.setValue(new StringType(measurement.textValue()));
        }
        if (measurement.interpretationCode() != null && !measurement.interpretationCode().isBlank()) {
            observation.addInterpretation(new CodeableConcept(new Coding(
                    "http://terminology.hl7.org/CodeSystem/v3-ObservationInterpretation",
                    measurement.interpretationCode(), measurement.interpretationCode())));
        }
        return observation;
    }

    private static DiagnosticReport report(ServiceRequest order, ResultRequest request,
                                           List<Observation> observations) {
        DiagnosticReport report = new DiagnosticReport();
        report.setId(UUID.randomUUID().toString());
        report.setStatus(DiagnosticReport.DiagnosticReportStatus.FINAL);
        report.setCode(order.getCode());
        report.addCategory(order.getCategoryFirstRep());
        report.setSubject(order.getSubject());
        report.setEncounter(order.getEncounter());
        report.addBasedOn(new Reference("ServiceRequest/" + order.getIdElement().getIdPart()));
        report.setEffective(new org.hl7.fhir.r4.model.DateTimeType(Date.from(request.issuedAt())));
        report.setIssued(Date.from(request.issuedAt()));
        report.addPerformer(new Reference("Practitioner/" + request.performerId()));
        observations.forEach(observation -> report.addResult(new Reference(
                "Observation/" + observation.getIdElement().getIdPart())));
        report.setConclusion(request.conclusion());
        return report;
    }

    private static Task resultReviewTask(ServiceRequest order, DiagnosticReport report) {
        Task task = new Task();
        task.setId(UUID.randomUUID().toString());
        task.setStatus(Task.TaskStatus.READY);
        task.setIntent(Task.TaskIntent.ORDER);
        task.setPriority(Task.TaskPriority.ROUTINE);
        task.setFor(order.getSubject());
        task.setEncounter(order.getEncounter());
        task.setFocus(new Reference("DiagnosticReport/" + report.getIdElement().getIdPart()));
        task.setOwner(order.getRequester());
        task.setAuthoredOn(new Date());
        task.setDescription("Review result: " + order.getCode().getCodingFirstRep().getDisplay());
        return task;
    }

    private static String categoryCode(String category) {
        return switch (category.toLowerCase(Locale.ROOT)) {
            case "laboratory" -> "108252007";
            case "imaging" -> "363679005";
            case "procedure" -> "71388002";
            case "referral" -> "3457005";
            default -> throw new IllegalArgumentException("category must be laboratory, imaging, procedure, or referral");
        };
    }

    private static void addPut(Bundle bundle, org.hl7.fhir.r4.model.Resource resource) {
        bundle.addEntry().setResource(resource).getRequest().setMethod(Bundle.HTTPVerb.PUT)
                .setUrl(resource.fhirType() + "/" + resource.getIdElement().getIdPart());
    }

    private static OrderSummary summary(ServiceRequest order, String taskId) {
        Coding code = order.getCode().getCodingFirstRep();
        return new OrderSummary(order.getIdElement().getIdPart(),
                order.getSubject().getReferenceElement().getIdPart(),
                order.getEncounter().getReferenceElement().getIdPart(),
                order.getCategory().isEmpty() ? null : order.getCategoryFirstRep().getCodingFirstRep().getDisplay(),
                code.getCode(), code.getDisplay(), order.getStatus().toCode(), order.getPriority().toCode(),
                order.getAuthoredOn() == null ? null : order.getAuthoredOn().toInstant(), taskId);
    }

    private static ResultSummary resultSummary(DiagnosticReport report) {
        String orderId = report.getBasedOn().isEmpty() ? null
                : report.getBasedOnFirstRep().getReferenceElement().getIdPart();
        return new ResultSummary(report.getIdElement().getIdPart(), orderId,
                report.getSubject().getReferenceElement().getIdPart(), report.getStatus().toCode(),
                report.getConclusion(), report.getIssued() == null ? null : report.getIssued().toInstant(),
                report.getResult().stream().map(reference -> reference.getReferenceElement().getIdPart()).toList());
    }
}
