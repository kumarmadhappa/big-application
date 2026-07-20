package com.bigapplication.bankingsystem;

import com.fasterxml.jackson.databind.JsonNode;
import java.math.BigDecimal;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
    void adminCanListUpdateDeleteAndTransactOnAnyAccount() throws Exception {
        String adminToken = loginAndGetAccessToken("bank_admin", "Admin@123");
        Long accountId = createAccount(adminToken, "ops_holder", "ops@example.com", "Operations Holder").id();

        mockMvc.perform(get("/api/banking/admin/accounts")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].holderUsername").exists());

        mockMvc.perform(put("/api/banking/admin/accounts/{accountId}", accountId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "displayName", "Operations Holder Updated",
                                "accountSegment", "COMMERCIAL",
                                "accountType", "SAVINGS",
                                "creditLimit", new BigDecimal("0.00")
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.holderName").value("Operations Holder Updated"));

        mockMvc.perform(post("/api/banking/admin/accounts/{accountId}/transactions/deposit", accountId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("amount", new BigDecimal("75.00")))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.transactionType").value("DEPOSIT"))
                .andExpect(jsonPath("$.data.balanceAfter").value(175.00));

        mockMvc.perform(post("/api/banking/admin/accounts/{accountId}/transactions/withdraw", accountId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("amount", new BigDecimal("25.00")))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.transactionType").value("WITHDRAWAL"))
                .andExpect(jsonPath("$.data.balanceAfter").value(150.00));

        mockMvc.perform(delete("/api/banking/admin/accounts/{accountId}", accountId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void adminCanSearchAccountsByNameAccountNumberOrAccountId() throws Exception {
        String adminToken = loginAndGetAccessToken("bank_admin", "Admin@123");
        CreatedAccount alphaAccount = createAccount(adminToken, "alpha_holder", "alpha@example.com", "Alpha Holder");
        CreatedAccount betaAccount = createAccount(adminToken, "beta_holder", "beta@example.com", "Beta Holder");

        mockMvc.perform(get("/api/banking/admin/accounts/search")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("name", "alpha"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].holderUsername").value("alpha_holder"));

        mockMvc.perform(get("/api/banking/admin/accounts/search")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("accountNumber", betaAccount.accountNumber()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].holderUsername").value("beta_holder"));

        mockMvc.perform(get("/api/banking/admin/accounts/search")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("accountId", alphaAccount.id().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].holderUsername").value("alpha_holder"));

        mockMvc.perform(get("/api/banking/admin/accounts/search")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));
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

    private CreatedAccount createAccount(String adminToken, String username, String email, String displayName) throws Exception {
        String payload = objectMapper.writeValueAsString(Map.of(
                "holderType", "PERSON",
                "displayName", displayName,
                "username", username,
                "email", email,
                "password", "StrongPass123!",
                "accountSegment", "CONSUMER",
                "accountType", "SAVINGS",
                "initialBalance", new BigDecimal("100.00"),
                "creditLimit", new BigDecimal("0.00")
        ));

        MvcResult result = mockMvc.perform(post("/api/banking/admin/accounts")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode data = objectMapper.readTree(result.getResponse().getContentAsString()).path("data");
        return new CreatedAccount(data.path("id").asLong(), data.path("accountNumber").asText());
    }

    private record CreatedAccount(Long id, String accountNumber) {
    }
}
