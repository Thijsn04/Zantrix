package com.zantrix.care.internal;

import com.zantrix.care.CareTeamRequest;
import com.zantrix.care.CareTeamSummary;
import com.zantrix.care.GoalRequest;
import com.zantrix.care.GoalSummary;
import com.zantrix.platform.fhir.FhirAccessGateway;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Goal;
import org.hl7.fhir.r4.model.Reference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class CareCoordinationServiceTest {

    private FhirAccessGateway fhir;
    private CareCoordinationService service;

    @BeforeEach
    void setUp() {
        fhir = mock(FhirAccessGateway.class);
        service = new CareCoordinationService(fhir);
    }

    private static Goal activeGoal() {
        Goal goal = new Goal();
        goal.setId("g-1");
        goal.setLifecycleStatus(Goal.GoalLifecycleStatus.ACTIVE);
        goal.setSubject(new Reference("Patient/p-1"));
        goal.setDescription(new CodeableConcept().setText("Lower HbA1c below 7 percent"));
        return goal;
    }

    @Test
    void addsAnActiveGoal() {
        GoalSummary summary = service.addGoal(new GoalRequest("p-1", "Lower HbA1c below 7 percent",
                "high-priority", LocalDate.now().plusMonths(3), "c-12", null));

        assertThat(summary.lifecycleStatus()).isEqualTo("active");
        assertThat(summary.priority()).isEqualTo("high-priority");
        assertThat(summary.addressesConditionId()).isEqualTo("c-12");
        verify(fhir).create(any(Goal.class), anyString());
    }

    @Test
    void refusesAGoalTargetedInThePast() {
        assertThatThrownBy(() -> service.addGoal(new GoalRequest("p-1", "Something",
                null, LocalDate.now().minusDays(1), null, null)))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(fhir);
    }

    @Test
    void refusesAnUnknownPriority() {
        assertThatThrownBy(() -> service.addGoal(new GoalRequest("p-1", "Something",
                "urgent-ish", null, null, null)))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(fhir);
    }

    @Test
    void closingAGoalRequiresAKnownOutcome() {
        assertThatThrownBy(() -> service.closeGoal("g-1", "p-1", "sort of"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("outcome");
        verifyNoInteractions(fhir);
    }

    @Test
    void closingRecordsWhetherTheGoalWasAchieved() {
        when(fhir.read(Goal.class, "g-1", "p-1")).thenReturn(activeGoal());

        GoalSummary summary = service.closeGoal("g-1", "p-1", "not-achieved");

        // Completed with the outcome kept: whether it was met is clinical information.
        assertThat(summary.lifecycleStatus()).isEqualTo("completed");
        assertThat(summary.achievementStatus()).isEqualTo("not-achieved");
        verify(fhir).update(any(Goal.class), anyString());
    }

    @Test
    void abandoningAGoalCancelsItRatherThanCompletingIt() {
        when(fhir.read(Goal.class, "g-1", "p-1")).thenReturn(activeGoal());

        GoalSummary summary = service.closeGoal("g-1", "p-1", "no-longer-desired");

        assertThat(summary.lifecycleStatus()).isEqualTo("cancelled");
    }

    @Test
    void definesACareTeamWithItsMembers() {
        CareTeamSummary summary = service.defineTeam(new CareTeamRequest("p-1", "Diabetes team",
                List.of(new CareTeamRequest.Member("pr-9", "158965000", "Physician"))));

        assertThat(summary.status()).isEqualTo("active");
        assertThat(summary.members()).singleElement()
                .satisfies(member -> assertThat(member.practitionerId()).isEqualTo("pr-9"));
    }
}
