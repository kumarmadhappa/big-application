package com.bigapplication.bankingsystem;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:banking_system_smoke;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "app.jwt.secret=test-secret-key-for-jwt-tests-with-32-bytes-min",
        "app.admin.password=Admin@123",
        "spring.flyway.enabled=false"
})
class BankingSystemApplicationTests {

    @Test
    void contextLoads() {
    }
}
