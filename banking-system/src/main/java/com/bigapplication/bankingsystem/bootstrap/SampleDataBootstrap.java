package com.bigapplication.bankingsystem.bootstrap;

import com.bigapplication.bankingsystem.dto.request.CreateAccountRequest;
import com.bigapplication.bankingsystem.entity.AccountSegment;
import com.bigapplication.bankingsystem.entity.AccountType;
import com.bigapplication.bankingsystem.entity.HolderType;
import com.bigapplication.bankingsystem.repository.BankAccountRepository;
import com.bigapplication.bankingsystem.service.AdminService;
import java.math.BigDecimal;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile({"dev", "local", "test"})
public class SampleDataBootstrap {

    private static final Logger log = LoggerFactory.getLogger(SampleDataBootstrap.class);

    @Bean
    ApplicationRunner seedSampleAccounts(AdminService adminService, BankAccountRepository accountRepository) {
        return args -> {
            log.info("Checking sample banking data bootstrap");
            if (accountRepository.count() > 0) {
                log.info("Sample banking data already present; skipping seed");
                return;
            }

            for (CreateAccountRequest request : sampleAccounts()) {
                adminService.createAccount(request);
            }

            log.info("Seeded 5 sample banking accounts");
        };
    }

    private List<CreateAccountRequest> sampleAccounts() {
        return List.of(
                request(HolderType.PERSON, "Alice Johnson", "alice.johnson", "alice.johnson@example.com",
                        AccountSegment.CONSUMER, AccountType.SAVINGS, "1000.00", null),
                request(HolderType.PERSON, "Bob Martin", "bob.martin", "bob.martin@example.com",
                        AccountSegment.CONSUMER, AccountType.CREDIT, "250.00", "1500.00"),
                request(HolderType.COMPANY, "Acme Retail Ltd", "acme.retail", "acme.retail@example.com",
                        AccountSegment.COMMERCIAL, AccountType.SAVINGS, "15000.00", null),
                request(HolderType.COMPANY, "Northwind Traders", "northwind.traders", "northwind.traders@example.com",
                        AccountSegment.COMMERCIAL, AccountType.CREDIT, "5000.00", "25000.00"),
                request(HolderType.PERSON, "Charlie Brown", "charlie.brown", "charlie.brown@example.com",
                        AccountSegment.CONSUMER, AccountType.SAVINGS, "300.00", null)
        );
    }

    private CreateAccountRequest request(HolderType holderType,
                                         String displayName,
                                         String username,
                                         String email,
                                         AccountSegment accountSegment,
                                         AccountType accountType,
                                         String initialBalance,
                                         String creditLimit) {
        CreateAccountRequest request = new CreateAccountRequest();
        request.setHolderType(holderType);
        request.setDisplayName(displayName);
        request.setUsername(username);
        request.setEmail(email);
        request.setPassword("StrongPass123!");
        request.setAccountSegment(accountSegment);
        request.setAccountType(accountType);
        request.setInitialBalance(new BigDecimal(initialBalance));
        request.setCreditLimit(creditLimit == null ? null : new BigDecimal(creditLimit));
        return request;
    }
}
