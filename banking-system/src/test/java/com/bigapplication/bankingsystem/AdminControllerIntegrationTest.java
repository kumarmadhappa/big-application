package com.bigapplication.bankingsystem;

import java.math.BigDecimal;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminControllerIntegrationTest extends BaseIntegrationTest {

    @Test
    void adminCanCreateSavingsAccount() throws Exception {
        String adminToken = loginAndGetAccessToken("bank_admin", "Admin@123");

        String payload = objectMapper.writeValueAsString(Map.of(
                "holderType", "PERSON",
                "displayName", "Alice Person",
                "username", "alice_holder",
                "email", "alice@example.com",
                "password", "StrongPass123!",
                "accountSegment", "CONSUMER",
                "accountType", "SAVINGS",
                "initialBalance", new BigDecimal("500.00"),
                "creditLimit", new BigDecimal("0.00")
        ));

        mockMvc.perform(post("/api/banking/admin/accounts")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.holderUsername").value("alice_holder"))
                .andExpect(jsonPath("$.data.accountType").value("SAVINGS"));
    }

    @Test
    void nonAdminCannotCreateAccount() throws Exception {
        String adminToken = loginAndGetAccessToken("bank_admin", "Admin@123");

        String createPayload = objectMapper.writeValueAsString(Map.of(
                "holderType", "PERSON",
                "displayName", "Bob Person",
                "username", "bob_holder",
                "email", "bob@example.com",
                "password", "StrongPass123!",
                "accountSegment", "CONSUMER",
                "accountType", "SAVINGS",
                "initialBalance", new BigDecimal("100.00"),
                "creditLimit", new BigDecimal("0.00")
        ));
        mockMvc.perform(post("/api/banking/admin/accounts")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createPayload))
                .andExpect(status().isCreated());

        String holderToken = loginAndGetAccessToken("bob_holder", "StrongPass123!");

        mockMvc.perform(post("/api/banking/admin/accounts")
                        .header("Authorization", "Bearer " + holderToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createPayload.replace("bob_holder", "bob_holder_2").replace("bob@example.com", "bob2@example.com")))
                .andExpect(status().isForbidden());
    }
}
