package com.bigapplication.userservice;

import com.bigapplication.userservice.entity.User;
import com.bigapplication.userservice.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordHashingTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void storedPasswordShouldBeHashedAndVerifiable() throws Exception {
        String plaintextPassword = "StrongPass123!";

        String payload = objectMapper.writeValueAsString(validUserRequest("hash_user", "hash_user@example.com"));
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/auth/register")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk());

        User user = userRepository.findByUsername("hash_user").orElseThrow();

        assertThat(user.getPassword()).isNotBlank();
        assertThat(user.getPassword()).isNotEqualTo(plaintextPassword);
        assertThat(user.getPassword()).startsWith("$2");
        assertThat(passwordEncoder.matches(plaintextPassword, user.getPassword())).isTrue();
    }
}
