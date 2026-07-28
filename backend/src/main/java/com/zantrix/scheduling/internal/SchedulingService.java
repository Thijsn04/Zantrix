package com.zantrix.scheduling.internal;

import com.zantrix.platform.fhir.FhirAccessGateway;
import com.zantrix.platform.fhir.FhirBundles;
import com.zantrix.scheduling.AppointmentRequest;
import com.zantrix.scheduling.AppointmentSummary;
import com.zantrix.scheduling.SchedulingConflictException;
import com.zantrix.scheduling.ScheduleRequest;
import com.zantrix.scheduling.SlotSummary;
import com.zantrix.terminology.TerminologyValidator;
import org.hl7.fhir.r4.model.Appointment;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Schedule;
import org.hl7.fhir.r4.model.Slot;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class SchedulingService {

    private static final String SNOMED = "http://snomed.info/sct";
    private final FhirAccessGateway fhir;
    private final TerminologyValidator terminology;

    public SchedulingService(FhirAccessGateway fhir, TerminologyValidator terminology) {
        this.fhir = fhir;
        this.terminology = terminology;
    }

    public AppointmentSummary book(AppointmentRequest request) {
        terminology.requireValid(SNOMED, request.serviceCode(), request.serviceDisplay());
        if (!request.end().isAfter(request.start())) {
            throw new IllegalArgumentException("end must be after start");
        }
        List<AppointmentSummary> conflicts = conflicts(request.practitionerId(), request.start(), request.end());
        if (!conflicts.isEmpty()) {
            throw new SchedulingConflictException(conflicts);
        }
        Appointment appointment = new Appointment();
        appointment.setId(UUID.randomUUID().toString());
        appointment.setStatus(Appointment.AppointmentStatus.BOOKED);
        appointment.setStart(Date.from(request.start()));
        appointment.setEnd(Date.from(request.end()));
        appointment.addServiceType(new CodeableConcept(new Coding(
                SNOMED, request.serviceCode(), request.serviceDisplay())));
        appointment.addParticipant().setActor(new Reference("Patient/" + request.patientId()))
                .setStatus(Appointment.ParticipationStatus.ACCEPTED);
        appointment.addParticipant().setActor(new Reference("Practitioner/" + request.practitionerId()))
                .setStatus(Appointment.ParticipationStatus.ACCEPTED);
        if (request.locationId() != null && !request.locationId().isBlank()) {
            appointment.addParticipant().setActor(new Reference("Location/" + request.locationId()))
                    .setStatus(Appointment.ParticipationStatus.ACCEPTED);
        }
        appointment.setComment(request.comment());
        if (request.slotId() == null || request.slotId().isBlank()) {
            fhir.create(appointment, request.patientId());
        } else {
            Slot slot = fhir.read(Slot.class, request.slotId(), request.patientId());
            if (slot.getStatus() != Slot.SlotStatus.FREE
                    || !slot.getStart().toInstant().equals(request.start())
                    || !slot.getEnd().toInstant().equals(request.end())) {
                throw new SchedulingConflictException(List.of());
            }
            appointment.addSlot(new Reference("Slot/" + request.slotId()));
            slot.setStatus(Slot.SlotStatus.BUSY);
            Bundle bundle = new Bundle().setType(Bundle.BundleType.TRANSACTION);
            Bundle.BundleEntryComponent slotEntry = bundle.addEntry().setResource(slot);
            slotEntry.getRequest().setMethod(Bundle.HTTPVerb.PUT).setUrl("Slot/" + request.slotId());
            if (slot.getMeta().hasVersionId()) {
                slotEntry.getRequest().setIfMatch("W/\"" + slot.getMeta().getVersionId() + "\"");
            }
            bundle.addEntry().setResource(appointment).getRequest().setMethod(Bundle.HTTPVerb.PUT)
                    .setUrl("Appointment/" + appointment.getIdElement().getIdPart());
            fhir.transaction(bundle, request.patientId());
        }
        return summary(appointment);
    }

    public List<SlotSummary> createSchedule(ScheduleRequest request) {
        terminology.requireValid(SNOMED, request.serviceCode(), request.serviceDisplay());
        Schedule schedule = new Schedule();
        schedule.setId(UUID.randomUUID().toString());
        schedule.setActive(true);
        schedule.addActor(new Reference("Practitioner/" + request.practitionerId()));
        if (request.locationId() != null && !request.locationId().isBlank()) {
            schedule.addActor(new Reference("Location/" + request.locationId()));
        }
        schedule.addServiceType(new CodeableConcept(new Coding(SNOMED,
                request.serviceCode(), request.serviceDisplay())));
        Bundle bundle = new Bundle().setType(Bundle.BundleType.TRANSACTION);
        bundle.addEntry().setResource(schedule).getRequest().setMethod(Bundle.HTTPVerb.PUT)
                .setUrl("Schedule/" + schedule.getIdElement().getIdPart());
        List<Slot> slots = request.slots().stream().map(interval -> {
            if (!interval.end().isAfter(interval.start())) throw new IllegalArgumentException("slot end must be after start");
            Slot slot = new Slot();
            slot.setId(UUID.randomUUID().toString());
            slot.setSchedule(new Reference("Schedule/" + schedule.getIdElement().getIdPart()));
            slot.setStatus(Slot.SlotStatus.FREE);
            slot.setStart(Date.from(interval.start()));
            slot.setEnd(Date.from(interval.end()));
            slot.addServiceType(new CodeableConcept(new Coding(SNOMED,
                    request.serviceCode(), request.serviceDisplay())));
            bundle.addEntry().setResource(slot).getRequest().setMethod(Bundle.HTTPVerb.PUT)
                    .setUrl("Slot/" + slot.getIdElement().getIdPart());
            return slot;
        }).toList();
        fhir.transaction(bundle, null);
        return slots.stream().map(slot -> summary(slot, schedule)).toList();
    }

    public List<SlotSummary> availability(String practitionerId, Instant start, Instant end) {
        List<Schedule> schedules = FhirBundles.resources(fhir.search("Schedule", Map.of(
                "actor", List.of("Practitioner/" + practitionerId), "active", List.of("true"),
                "_count", List.of("100")), null), Schedule.class);
        return schedules.stream().flatMap(schedule -> FhirBundles.resources(fhir.search("Slot", Map.of(
                        "schedule", List.of(schedule.getIdElement().getIdPart()), "status", List.of("free"),
                        "start", List.of("ge" + DateTimeFormatter.ISO_INSTANT.format(start),
                                "lt" + DateTimeFormatter.ISO_INSTANT.format(end)), "_count", List.of("500")), null),
                Slot.class).stream().map(slot -> summary(slot, schedule))).toList();
    }

    /**
     * The appointments a department is working, across every patient.
     *
     * The window is supplied as explicit instants rather than a date, because
     * the server has no reliable way to know which timezone a clinic day means.
     * The caller resolves its own local day boundaries.
     */
    public List<AppointmentSummary> daySchedule(Instant from, Instant to,
                                                String practitionerId, String locationId) {
        if (!to.isAfter(from)) {
            throw new IllegalArgumentException("to must be after from");
        }
        Map<String, List<String>> parameters = new LinkedHashMap<>();
        parameters.put("date", List.of("ge" + DateTimeFormatter.ISO_INSTANT.format(from),
                "lt" + DateTimeFormatter.ISO_INSTANT.format(to)));
        if (practitionerId != null && !practitionerId.isBlank()) {
            parameters.put("actor", List.of("Practitioner/" + practitionerId));
        } else if (locationId != null && !locationId.isBlank()) {
            parameters.put("actor", List.of("Location/" + locationId));
        }
        parameters.put("_sort", List.of("date"));
        parameters.put("_count", List.of("200"));
        return FhirBundles.resources(fhir.search("Appointment", parameters, null), Appointment.class).stream()
                .map(SchedulingService::summary).toList();
    }

    public List<AppointmentSummary> listForPatient(String patientId) {
        return FhirBundles.resources(fhir.search("Appointment", Map.of(
                "patient", List.of(patientId), "_sort", List.of("date"), "_count", List.of("100")),
                patientId), Appointment.class).stream().map(SchedulingService::summary).toList();
    }

    public AppointmentSummary changeStatus(String id, String patientId, Appointment.AppointmentStatus status) {
        Appointment appointment = fhir.read(Appointment.class, id, patientId);
        if (appointment.getStatus() == Appointment.AppointmentStatus.FULFILLED
                || appointment.getStatus() == Appointment.AppointmentStatus.CANCELLED
                || appointment.getStatus() == Appointment.AppointmentStatus.NOSHOW) {
            throw new IllegalArgumentException("A terminal appointment cannot change status");
        }
        appointment.setStatus(status);
        fhir.update(appointment, patientId);
        return summary(appointment);
    }

    private List<AppointmentSummary> conflicts(String practitionerId, Instant start, Instant end) {
        Map<String, List<String>> parameters = Map.of(
                "actor", List.of("Practitioner/" + practitionerId),
                "date", List.of("lt" + DateTimeFormatter.ISO_INSTANT.format(end),
                        "gt" + DateTimeFormatter.ISO_INSTANT.format(start)),
                "status:not", List.of("cancelled", "noshow"),
                "_count", List.of("100"));
        return FhirBundles.resources(fhir.search("Appointment", parameters, null), Appointment.class).stream()
                .filter(existing -> existing.getStart() != null && existing.getEnd() != null
                        && existing.getStart().toInstant().isBefore(end)
                        && existing.getEnd().toInstant().isAfter(start))
                .map(SchedulingService::summary)
                .toList();
    }

    private static AppointmentSummary summary(Appointment appointment) {
        String patient = participant(appointment, "Patient");
        String practitioner = participant(appointment, "Practitioner");
        String location = participant(appointment, "Location");
        String service = appointment.getServiceType().isEmpty() ? null
                : appointment.getServiceTypeFirstRep().getCodingFirstRep().getDisplay();
        return new AppointmentSummary(appointment.getIdElement().getIdPart(), patient, practitioner,
                location, appointment.getStatus().toCode(), service,
                appointment.getStart() == null ? null : appointment.getStart().toInstant(),
                appointment.getEnd() == null ? null : appointment.getEnd().toInstant());
    }

    private static String participant(Appointment appointment, String resourceType) {
        return appointment.getParticipant().stream()
                .map(participant -> participant.getActor().getReferenceElement())
                .filter(reference -> resourceType.equals(reference.getResourceType()))
                .map(reference -> reference.getIdPart())
                .findFirst().orElse(null);
    }

    private static SlotSummary summary(Slot slot, Schedule schedule) {
        String practitioner = schedule.getActor().stream().map(Reference::getReferenceElement)
                .filter(reference -> "Practitioner".equals(reference.getResourceType()))
                .map(reference -> reference.getIdPart()).findFirst().orElse(null);
        String location = schedule.getActor().stream().map(Reference::getReferenceElement)
                .filter(reference -> "Location".equals(reference.getResourceType()))
                .map(reference -> reference.getIdPart()).findFirst().orElse(null);
        String service = slot.getServiceType().isEmpty() ? null
                : slot.getServiceTypeFirstRep().getCodingFirstRep().getDisplay();
        return new SlotSummary(slot.getIdElement().getIdPart(), schedule.getIdElement().getIdPart(),
                practitioner, location, service, slot.getStatus().toCode(), slot.getStart().toInstant(),
                slot.getEnd().toInstant());
    }
}
