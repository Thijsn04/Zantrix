package com.zantrix.care.internal;

import com.zantrix.care.CareTeamRequest;
import com.zantrix.care.CareTeamSummary;
import com.zantrix.care.GoalRequest;
import com.zantrix.care.GoalSummary;
import com.zantrix.platform.fhir.FhirAccessGateway;
import com.zantrix.platform.fhir.FhirBundles;
import org.hl7.fhir.r4.model.CareTeam;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DateType;
import org.hl7.fhir.r4.model.Goal;
import org.hl7.fhir.r4.model.Reference;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Care coordination: the team around a patient, and what the care is working towards.
 *
 * A goal is a clinical intention, so it is closed by recording an outcome
 * rather than by deletion. Whether a goal was achieved is itself information a
 * later clinician needs.
 */
@Service
public class CareCoordinationService {

    private static final String ACHIEVEMENT = "http://terminology.hl7.org/CodeSystem/goal-achievement";
    private static final String PARTICIPANT_ROLE = "http://snomed.info/sct";
    private static final Set<String> PRIORITIES = Set.of("high-priority", "medium-priority", "low-priority");
    private static final Set<String> OUTCOMES = Set.of("achieved", "not-achieved", "no-longer-desired");

    private final FhirAccessGateway fhir;

    public CareCoordinationService(FhirAccessGateway fhir) { this.fhir = fhir; }

    // Care team

    public CareTeamSummary defineTeam(CareTeamRequest request) {
        CareTeam team = new CareTeam();
        team.setId(UUID.randomUUID().toString());
        team.setStatus(CareTeam.CareTeamStatus.ACTIVE);
        team.setName(request.name());
        team.setSubject(new Reference("Patient/" + request.patientId()));
        request.members().forEach(member -> team.addParticipant()
                .setMember(new Reference("Practitioner/" + member.practitionerId()))
                .setRole(List.of(new CodeableConcept(new Coding(
                        PARTICIPANT_ROLE, member.roleCode(), member.roleDisplay())))));
        fhir.create(team, request.patientId());
        return summary(team);
    }

    public List<CareTeamSummary> teams(String patientId) {
        return FhirBundles.resources(fhir.search("CareTeam", Map.of(
                "patient", List.of(patientId), "_count", List.of("50")), patientId), CareTeam.class)
                .stream().map(CareCoordinationService::summary).toList();
    }

    /** Standing down a team keeps who was involved, which a later reader still needs. */
    public CareTeamSummary standDownTeam(String id, String patientId) {
        CareTeam team = fhir.read(CareTeam.class, id, patientId);
        if (team.getStatus() != CareTeam.CareTeamStatus.ACTIVE) {
            throw new IllegalArgumentException("Only an active care team can be stood down");
        }
        team.setStatus(CareTeam.CareTeamStatus.INACTIVE);
        fhir.update(team, patientId);
        return summary(team);
    }

    // Goals

    public GoalSummary addGoal(GoalRequest request) {
        if (request.priority() != null && !request.priority().isBlank()
                && !PRIORITIES.contains(request.priority())) {
            throw new IllegalArgumentException("priority must be one of " + PRIORITIES);
        }
        if (request.targetDate() != null && request.targetDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("A goal cannot target a date in the past");
        }

        Goal goal = new Goal();
        goal.setId(UUID.randomUUID().toString());
        goal.setLifecycleStatus(Goal.GoalLifecycleStatus.ACTIVE);
        goal.setSubject(new Reference("Patient/" + request.patientId()));
        goal.setDescription(new CodeableConcept().setText(request.description()));
        if (request.priority() != null && !request.priority().isBlank()) {
            goal.setPriority(new CodeableConcept(new Coding(
                    "http://terminology.hl7.org/CodeSystem/goal-priority",
                    request.priority(), request.priority())));
        }
        if (request.targetDate() != null) {
            goal.addTarget().setDue(new DateType(request.targetDate().toString()));
        }
        if (request.addressesConditionId() != null && !request.addressesConditionId().isBlank()) {
            goal.addAddresses(new Reference("Condition/" + request.addressesConditionId()));
        }
        if (request.note() != null && !request.note().isBlank()) {
            goal.addNote().setText(request.note());
        }
        fhir.create(goal, request.patientId());
        return summary(goal);
    }

    public List<GoalSummary> goals(String patientId, boolean includeClosed) {
        return FhirBundles.resources(fhir.search("Goal", Map.of(
                "patient", List.of(patientId), "_count", List.of("100")), patientId), Goal.class)
                .stream()
                .filter(goal -> includeClosed || goal.getLifecycleStatus() == Goal.GoalLifecycleStatus.ACTIVE)
                .map(CareCoordinationService::summary)
                .toList();
    }

    /**
     * Closing a goal records what became of it. Whether it was achieved is
     * clinical information, so an outcome is required rather than optional.
     */
    public GoalSummary closeGoal(String id, String patientId, String outcome) {
        if (outcome == null || !OUTCOMES.contains(outcome)) {
            throw new IllegalArgumentException("outcome must be one of " + OUTCOMES);
        }
        Goal goal = fhir.read(Goal.class, id, patientId);
        if (goal.getLifecycleStatus() != Goal.GoalLifecycleStatus.ACTIVE) {
            throw new IllegalArgumentException("Only an active goal can be closed");
        }
        goal.setLifecycleStatus("no-longer-desired".equals(outcome)
                ? Goal.GoalLifecycleStatus.CANCELLED
                : Goal.GoalLifecycleStatus.COMPLETED);
        goal.setAchievementStatus(new CodeableConcept(new Coding(ACHIEVEMENT, outcome, outcome)));
        fhir.update(goal, patientId);
        return summary(goal);
    }

    private static CareTeamSummary summary(CareTeam team) {
        List<CareTeamSummary.Member> members = team.getParticipant().stream().map(participant -> {
            Coding role = participant.getRole().isEmpty()
                    ? new Coding() : participant.getRoleFirstRep().getCodingFirstRep();
            return new CareTeamSummary.Member(
                    participant.getMember().getReferenceElement().getIdPart(),
                    role.getCode(), role.getDisplay());
        }).toList();
        return new CareTeamSummary(team.getIdElement().getIdPart(),
                team.getSubject().getReferenceElement().getIdPart(), team.getName(),
                team.getStatus() == null ? null : team.getStatus().toCode(), members);
    }

    private static GoalSummary summary(Goal goal) {
        LocalDate due = goal.getTarget().isEmpty() ? null : dueDate(goal);
        return new GoalSummary(goal.getIdElement().getIdPart(),
                goal.getSubject().getReferenceElement().getIdPart(),
                goal.getDescription().getText(),
                goal.getLifecycleStatus() == null ? null : goal.getLifecycleStatus().toCode(),
                goal.hasAchievementStatus() ? goal.getAchievementStatus().getCodingFirstRep().getCode() : null,
                goal.hasPriority() ? goal.getPriority().getCodingFirstRep().getCode() : null,
                due,
                goal.getAddresses().isEmpty() ? null
                        : goal.getAddressesFirstRep().getReferenceElement().getIdPart());
    }

    private static LocalDate dueDate(Goal goal) {
        if (goal.getTargetFirstRep().getDue() instanceof DateType date && date.getValue() != null) {
            return date.getValue().toInstant().atZone(ZoneOffset.UTC).toLocalDate();
        }
        return null;
    }
}
