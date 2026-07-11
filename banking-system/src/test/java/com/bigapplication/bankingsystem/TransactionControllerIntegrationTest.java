package com.bigapplication.bankingsystem;

import com.fasterxml.jackson.databind.JsonNode;
import java.math.BigDecimal;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TransactionControllerIntegrationTest extends BaseIntegrationTest {

    @Test
    void holderCanDepositAndWithdraw() throws Exception {
        String adminToken = loginAndGetAccessToken("bank_admin", "Admin@123");
        Long accountId = createCreditAccount(adminToken, "credit_holder", "credit@example.com");
        String holderToken = loginAndGetAccessToken("credit_holder", "StrongPass123!");

        String depositPayload = objectMapper.writeValueAsString(Map.of("amount", new BigDecimal("200.00")));
        mockMvc.perform(post("/api/banking/accounts/{accountId}/transactions/deposit", accountId)
                        .header("Authorization", "Bearer " + holderToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(depositPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.transactionType").value("DEPOSIT"))
                .andExpect(jsonPath("$.data.balanceAfter").value(300.00));

        String withdrawPayload = objectMapper.writeValueAsString(Map.of("amount", new BigDecimal("250.00")));
        mockMvc.perform(post("/api/banking/accounts/{accountId}/transactions/withdraw", accountId)
                        .header("Authorization", "Bearer " + holderToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(withdrawPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.transactionType").value("WITHDRAWAL"))
                .andExpect(jsonPath("$.data.balanceAfter").value(50.00));
    }

    @Test
    void savingsAccountCannotOverdraw() throws Exception {
        String adminToken = loginAndGetAccessToken("bank_admin", "Admin@123");
        Long accountId = createSavingsAccount(adminToken, "savings_holder", "savings@example.com");
        String holderToken = loginAndGetAccessToken("savings_holder", "StrongPass123!");

        String withdrawPayload = objectMapper.writeValueAsString(Map.of("amount", new BigDecimal("150.00")));
        mockMvc.perform(post("/api/banking/accounts/{accountId}/transactions/withdraw", accountId)
                        .header("Authorization", "Bearer " + holderToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(withdrawPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Insufficient funds for savings account"));
    }

    @Test
    void holderCanSeeOwnAccounts() throws Exception {
        String adminToken = loginAndGetAccessToken("bank_admin", "Admin@123");
        createSavingsAccount(adminToken, "owner_holder", "owner@example.com");
        String holderToken = loginAndGetAccessToken("owner_holder", "StrongPass123!");

        mockMvc.perform(get("/api/banking/accounts/mine")
                        .header("Authorization", "Bearer " + holderToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].holderUsername").value("owner_holder"));
    }

    private Long createSavingsAccount(String adminToken, String username, String email) throws Exception {
        String payload = objectMapper.writeValueAsString(Map.of(
                "holderType", "PERSON",
                "displayName", "Savings Holder",
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
        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        return root.path("data").path("id").asLong();
    }

    private Long createCreditAccount(String adminToken, String username, String email) throws Exception {
        String payload = objectMapper.writeValueAsString(Map.of(
                "holderType", "COMPANY",
                "displayName", "Credit Holder",
                "username", username,
                "email", email,
                "password", "StrongPass123!",
                "accountSegment", "COMMERCIAL",
                "accountType", "CREDIT",
                "initialBalance", new BigDecimal("100.00"),
                "creditLimit", new BigDecimal("1000.00")
        ));
        MvcResult result = mockMvc.perform(post("/api/banking/admin/accounts")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        return root.path("data").path("id").asLong();
    }
}
