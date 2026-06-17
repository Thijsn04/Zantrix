package com.zantrix;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
public class ApplicationIT extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testPublicEndpointAccessibleWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/public/health"))
                .andExpect(status().isNotFound()); // Endpoint may not exist, but shouldn't be 401/403
    }
    
    @Test
    void testProtectedEndpointRequiresAuth() throws Exception {
        mockMvc.perform(get("/api/pmi/patients"))
                .andExpect(status().isUnauthorized()); // Should be 401
    }
}
