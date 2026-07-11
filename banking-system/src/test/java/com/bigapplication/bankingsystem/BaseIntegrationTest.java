package com.bigapplication.bankingsystem;

import com.bigapplication.bankingsystem.repository.BankAccountRepository;
import com.bigapplication.bankingsystem.repository.BankTransactionRepository;
import com.bigapplication.bankingsystem.repository.BankUserRepository;
import com.bigapplication.bankingsystem.repository.AccountHolderRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:banking_system_test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false",
        "app.jwt.secret=test-secret-key-for-jwt-tests-with-32-bytes-min",
        "app.admin.username=bank_admin",
        "app.admin.email=bank.admin@example.com",
        "app.admin.password=Admin@123"
})
@AutoConfigureMockMvc
abstract class BaseIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    private BankTransactionRepository bankTransactionRepository;

    @Autowired
    private BankAccountRepository bankAccountRepository;

    @Autowired
    private BankUserRepository bankUserRepository;

    @Autowired
    private AccountHolderRepository accountHolderRepository;

    @BeforeEach
    void cleanup() {
        bankTransactionRepository.deleteAll();
        bankAccountRepository.deleteAll();
        accountHolderRepository.deleteAll();
        bankUserRepository.findByUsername("bank_admin").ifPresent(admin -> {
            bankUserRepository.deleteAll(bankUserRepository.findAll().stream()
                    .filter(user -> !user.getId().equals(admin.getId()))
                    .toList());
        });
    }

    protected String loginAndGetAccessToken(String login, String password) throws Exception {
        String payload = objectMapper.writeValueAsString(Map.of(
                "login", login,
                "password", password
        ));
        MvcResult result = mockMvc.perform(post("/api/banking/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        return root.path("data").path("accessToken").asText();
    }
}
