package com.zantrix.charting;

import com.zantrix.charting.domain.ClinicalNoteEntity;
import com.zantrix.charting.domain.NoteStatus;
import com.zantrix.charting.repository.ClinicalNoteRepository;
import com.zantrix.IntegrationTestBase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.assertEquals;

@AutoConfigureMockMvc
public class ClinicalNoteIntegrationTest extends IntegrationTestBase {

    @Autowired
    private org.springframework.test.web.servlet.MockMvc mockMvc;

    @Autowired
    private ClinicalNoteRepository repository;

    @Test
    void testDraftToSignedWorkflow() throws Exception {
        UUID patientId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();

        // 1. Create Draft
        String noteJson = """
            {
                "patientId": "%s",
                "authorId": "%s",
                "noteType": "Consultation",
                "content": "{\\"text\\": \\"Patient feels better.\\"}"
            }
            """.formatted(patientId.toString(), authorId.toString());

        String responseContent = mockMvc.perform(post("/api/v1/charting/notes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(noteJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andReturn().getResponse().getContentAsString();

        String noteIdStr = responseContent.split("\"id\":\"")[1].split("\"")[0];
        UUID noteId = UUID.fromString(noteIdStr);

        // 2. Sign Note
        mockMvc.perform(post("/api/v1/charting/notes/" + noteId + "/sign")
                .param("authorId", authorId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FINAL"));

        // 3. Verify DB State
        ClinicalNoteEntity signedNote = repository.findById(noteId).orElseThrow();
        assertEquals(NoteStatus.FINAL, signedNote.getStatus());

        // 4. Try to modify signed note (Should fail)
        signedNote.setContent("""
            {
                "text": "Malicious modification"
            }
            """);
        String modifiedJson = """
            {
                "id": "%s",
                "patientId": "%s",
                "authorId": "%s",
                "content": "{\\"text\\": \\"Malicious modification\\"}"
            }
            """.formatted(noteId.toString(), patientId.toString(), authorId.toString());

        mockMvc.perform(post("/api/v1/charting/notes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(modifiedJson))
                .andExpect(status().is5xxServerError()); // Because IllegalStateException is thrown in service
    }
}
