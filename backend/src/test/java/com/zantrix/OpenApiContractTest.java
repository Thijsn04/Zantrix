package com.zantrix;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.zantrix.administration.internal.AdministrationService;
import com.zantrix.allergies.internal.AllergyService;
import com.zantrix.audit.AuditQuery;
import com.zantrix.audit.AuditTrailVerifier;
import com.zantrix.coverage.internal.CoverageService;
import com.zantrix.documentation.internal.DocumentationService;
import com.zantrix.encounter.internal.EncounterService;
import com.zantrix.immunizations.internal.ImmunizationService;
import com.zantrix.medications.internal.MedicationService;
import com.zantrix.orders.internal.OrderService;
import com.zantrix.patient.internal.PatientMergeService;
import com.zantrix.patient.internal.PatientService;
import com.zantrix.platform.OpenApiConfiguration;
import com.zantrix.platform.fhir.FhirAccessGateway;
import com.zantrix.privacy.internal.ConsentService;
import com.zantrix.platform.security.EmergencyAccessReviewRecorder;
import com.zantrix.problems.internal.ProblemService;
import com.zantrix.scheduling.internal.SchedulingService;
import com.zantrix.terminology.internal.TerminologyService;
import com.zantrix.vitals.internal.VitalsService;
import com.zantrix.workflow.internal.WorkflowService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springdoc.core.configuration.SpringDocConfiguration;
import org.springdoc.core.properties.SpringDocConfigProperties;
import org.springdoc.webmvc.core.configuration.SpringDocWebMvcConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import ca.uhn.fhir.context.FhirContext;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Generates the published API contract and fails when it drifts.
 *
 * The document is produced from the real controller signatures, so it cannot
 * describe an endpoint the implementation does not have. Committing it means a
 * contract change arrives as a reviewable diff rather than as a surprise in a
 * consumer, and the frontend generates its types from the same file.
 *
 * <p>This is a controller slice rather than a full application test on purpose:
 * an API description is a property of the web layer, and generating it should
 * not require a database. Services are mocked because their behaviour is
 * irrelevant to the shape of the contract.
 *
 * <p>Every controller is scanned rather than listed, so a new one cannot stay
 * out of the published contract by omission. A missing collaborator then fails
 * the context loudly instead.
 *
 * <p>Run with {@code -Dopenapi.write=true} to accept an intended change.
 */
@WebMvcTest
@AutoConfigureMockMvc(addFilters = false)
@Import({ OpenApiConfiguration.class, OpenApiContractTest.FhirContextForDocumentation.class, SpringDocConfiguration.class, SpringDocWebMvcConfiguration.class,
        SpringDocConfigProperties.class, JacksonAutoConfiguration.class })
class OpenApiContractTest {

    /** Committed alongside the code it describes, so a contract change is visible in review. */
    private static final Path CONTRACT = Path.of("openapi.json");

    @Autowired
    private MockMvc mvc;

    @MockBean private AdministrationService administration;
    @MockBean private AllergyService allergies;
    @MockBean private AuditQuery auditQuery;
    @MockBean private AuditTrailVerifier auditVerifier;
    @MockBean private ConsentService consents;
    @MockBean private CoverageService coverage;
    @MockBean private DocumentationService documentation;
    @MockBean private EmergencyAccessReviewRecorder emergencyReviews;
    @MockBean private EncounterService encounters;
    @MockBean private FhirAccessGateway fhir;
    @MockBean private ImmunizationService immunizations;
    @MockBean private MedicationService medications;
    @MockBean private OrderService orders;
    @MockBean private PatientService patients;
    @MockBean private PatientMergeService merges;
    @MockBean private ProblemService problems;
    @MockBean private SchedulingService scheduling;
    @MockBean private TerminologyService terminology;
    @MockBean private VitalsService vitals;
    @MockBean private WorkflowService workflow;

    /** The FHIR facade is part of the published surface, so it is described too. */
    @TestConfiguration
    static class FhirContextForDocumentation {
        @Bean
        FhirContext fhirContext() { return FhirContext.forR4(); }
    }

    @Test
    void publishedContractMatchesTheControllers() throws Exception {
        String generated = normalize(mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));

        if (Boolean.getBoolean("openapi.write") || !Files.exists(CONTRACT)) {
            Files.writeString(CONTRACT, generated, StandardCharsets.UTF_8);
            assertThat(Files.exists(CONTRACT))
                    .as("Wrote %s. Commit it so the contract change is reviewed.", CONTRACT.toAbsolutePath())
                    .isTrue();
            return;
        }

        assertThat(generated)
                .as("The API contract changed. Review the difference, then regenerate with "
                        + "./mvnw test -Dtest=OpenApiContractTest -Dopenapi.write=true and commit %s. "
                        + "The frontend generates its types from this file.", CONTRACT)
                .isEqualTo(Files.readString(CONTRACT, StandardCharsets.UTF_8));
    }

    /**
     * springdoc emits object properties in a different order from run to run,
     * so the document is sorted before it is written or compared. Without this
     * the gate fails at random, which is worse than having no gate at all.
     */
    private static String normalize(String json) throws Exception {
        ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        return mapper.writeValueAsString(sorted(mapper.readTree(json), mapper)) + "\n";
    }

    private static JsonNode sorted(JsonNode node, ObjectMapper mapper) {
        if (node.isObject()) {
            ObjectNode result = mapper.createObjectNode();
            List<String> names = new ArrayList<>();
            node.fieldNames().forEachRemaining(names::add);
            Collections.sort(names);
            names.forEach(name -> result.set(name, sorted(node.get(name), mapper)));
            return result;
        }
        if (node.isArray()) {
            ArrayNode result = mapper.createArrayNode();
            node.forEach(element -> result.add(sorted(element, mapper)));
            return result;
        }
        return node;
    }
}
