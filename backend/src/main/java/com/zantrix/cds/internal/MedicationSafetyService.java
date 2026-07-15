package com.zantrix.cds.internal;

import com.zantrix.cds.MedicationSafetyChecker;
import com.zantrix.cds.SafetyAssessment;
import com.zantrix.cds.SafetyIssue;
import com.zantrix.platform.fhir.FhirAccessGateway;
import com.zantrix.platform.fhir.FhirBundles;
import org.hl7.fhir.r4.model.AllergyIntolerance;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
class MedicationSafetyService implements MedicationSafetyChecker {

    private static final String RXNORM = "http://www.nlm.nih.gov/research/umls/rxnorm";
    private final FhirAccessGateway fhir;
    private final HighPriorityInteractionKnowledgeBase knowledge = new HighPriorityInteractionKnowledgeBase();

    MedicationSafetyService(FhirAccessGateway fhir) { this.fhir = fhir; }

    @Override
    public SafetyAssessment assess(String patientId, String rxNormIngredientCode, String display) {
        if (rxNormIngredientCode == null || rxNormIngredientCode.isBlank()) {
            throw new IllegalArgumentException("An RxNorm ingredient code is required for medication safety checking");
        }
        List<SafetyIssue> issues = new ArrayList<>();
        checkAllergies(patientId, rxNormIngredientCode, display, issues);
        checkActiveMedications(patientId, rxNormIngredientCode, issues);
        return new SafetyAssessment(HighPriorityInteractionKnowledgeBase.NAME,
                HighPriorityInteractionKnowledgeBase.VERSION, "15 high-priority interaction classes plus coded allergies",
                false, issues);
    }

    private void checkAllergies(String patientId, String newCode, String display, List<SafetyIssue> issues) {
        List<AllergyIntolerance> allergies = FhirBundles.resources(fhir.search("AllergyIntolerance", Map.of(
                "patient", List.of(patientId), "clinical-status", List.of("active"), "_count", List.of("500")),
                patientId), AllergyIntolerance.class);
        for (AllergyIntolerance allergy : allergies) {
            boolean matches = allergy.getCode().getCoding().stream().anyMatch(coding ->
                    RXNORM.equals(coding.getSystem()) && newCode.equals(coding.getCode()));
            if (matches) {
                issues.add(new SafetyIssue("DRUG-ALLERGY", "critical",
                        "The patient has an active coded allergy or intolerance to " + display + ".",
                        null, "FHIR AllergyIntolerance/" + allergy.getIdElement().getIdPart()));
            }
        }
    }

    private void checkActiveMedications(String patientId, String newCode, List<SafetyIssue> issues) {
        List<MedicationRequest> medications = FhirBundles.resources(fhir.search("MedicationRequest", Map.of(
                "patient", List.of(patientId), "status", List.of("active,on-hold"), "_count", List.of("500")),
                patientId), MedicationRequest.class);
        for (MedicationRequest medication : medications) {
            Coding ingredient = medication.getMedicationCodeableConcept().getCoding().stream()
                    .filter(coding -> RXNORM.equals(coding.getSystem())).findFirst().orElse(null);
            if (ingredient == null || ingredient.getCode() == null) {
                continue;
            }
            for (HighPriorityInteractionKnowledgeBase.Interaction interaction
                    : knowledge.find(newCode, ingredient.getCode())) {
                issues.add(new SafetyIssue(interaction.ruleId(), "critical", interaction.summary(),
                        medication.getIdElement().getIdPart(), HighPriorityInteractionKnowledgeBase.SOURCE));
            }
        }
    }
}
