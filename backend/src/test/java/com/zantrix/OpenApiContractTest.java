package com.zantrix;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.zantrix.administration.internal.AdministrationService;
import com.zantrix.administration.web.AdministrationController;
import com.zantrix.allergies.internal.AllergyService;
import com.zantrix.allergies.web.AllergyController;
import com.zantrix.audit.AuditQuery;
import com.zantrix.audit.AuditTrailVerifier;
import com.zantrix.audit.web.AuditController;
import com.zantrix.documentation.internal.DocumentationService;
import com.zantrix.documentation.web.DocumentationController;
import com.zantrix.encounter.internal.EncounterService;
import com.zantrix.encounter.web.EncounterController;
import com.zantrix.medications.internal.MedicationService;
import com.zantrix.medications.web.MedicationController;
import com.zantrix.orders.internal.OrderService;
import com.zantrix.orders.web.OrderController;
import com.zantrix.patient.internal.PatientMergeService;
import com.zantrix.patient.internal.PatientService;
import com.zantrix.patient.web.PatientController;
import com.zantrix.platform.OpenApiConfiguration;
import com.zantrix.platform.fhir.FhirAccessGateway;
import com.zantrix.platform.fhir.FhirServerController;
import com.zantrix.platform.iam.IamController;
import com.zantrix.platform.web.SystemController;
import com.zantrix.privacy.internal.ConsentService;
import com.zantrix.privacy.web.ConsentController;
import com.zantrix.privacy.web.EmergencyAccessReviewController;
import com.zantrix.platform.security.EmergencyAccessReviewRecorder;
import com.zantrix.problems.internal.ProblemService;
import com.zantrix.problems.web.ProblemController;
import com.zantrix.scheduling.internal.SchedulingService;
import com.zantrix.scheduling.web.SchedulingController;
import com.zantrix.terminology.internal.TerminologyService;
import com.zantrix.terminology.web.TerminologyController;
import com.zantrix.vitals.internal.VitalsService;
import com.zantrix.vitals.web.VitalsController;
import com.zantrix.workflow.internal.WorkflowService;
import com.zantrix.workflow.web.WorkflowController;
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
 * <p>Run with {@code -Dopenapi.write=true} to accept an intended change.
 */
@WebMvcTest(controllers = {
        AdministrationController.class, AllergyController.class, AuditController.class,
        ConsentController.class, DocumentationController.class, EmergencyAccessReviewController.class,
        EncounterController.class, FhirServerController.class, IamController.class,
        MedicationController.class, OrderController.class, PatientController.class,
        ProblemController.class, SchedulingController.class, SystemController.class,
        TerminologyController.class, VitalsController.class, WorkflowController.class,
})
@AutoConfigureMockMvc(addFilters = false)
@Import({ OpenApiConfiguration.class, SpringDocConfiguration.class, SpringDocWebMvcConfiguration.class,
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
    @MockBean private DocumentationService documentation;
    @MockBean private EmergencyAccessReviewRecorder emergencyReviews;
    @MockBean private EncounterService encounters;
    @MockBean private FhirAccessGateway fhir;
    @MockBean private MedicationService medications;
    @MockBean private OrderService orders;
    @MockBean private PatientService patients;
    @MockBean private PatientMergeService merges;
    @MockBean private ProblemService problems;
    @MockBean private SchedulingService scheduling;
    @MockBean private TerminologyService terminology;
    @MockBean private VitalsService vitals;
    @MockBean private WorkflowService workflow;

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
