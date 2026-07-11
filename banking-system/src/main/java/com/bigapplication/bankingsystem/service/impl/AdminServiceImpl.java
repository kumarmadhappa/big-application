package com.bigapplication.bankingsystem.service.impl;

import com.bigapplication.bankingsystem.dto.request.CreateAccountRequest;
import com.bigapplication.bankingsystem.dto.response.AccountResponse;
import com.bigapplication.bankingsystem.entity.AccountHolder;
import com.bigapplication.bankingsystem.entity.AccountType;
import com.bigapplication.bankingsystem.entity.BankAccount;
import com.bigapplication.bankingsystem.entity.BankUser;
import com.bigapplication.bankingsystem.entity.UserRole;
import com.bigapplication.bankingsystem.exception.BusinessRuleException;
import com.bigapplication.bankingsystem.exception.DuplicateResourceException;
import com.bigapplication.bankingsystem.mapper.BankingMapper;
import com.bigapplication.bankingsystem.repository.AccountHolderRepository;
import com.bigapplication.bankingsystem.repository.BankAccountRepository;
import com.bigapplication.bankingsystem.repository.BankUserRepository;
import com.bigapplication.bankingsystem.service.AdminService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AdminServiceImpl implements AdminService {

    private final BankUserRepository userRepository;
    private final AccountHolderRepository holderRepository;
    private final BankAccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final BankingMapper mapper;

    public AdminServiceImpl(BankUserRepository userRepository,
                            AccountHolderRepository holderRepository,
                            BankAccountRepository accountRepository,
                            PasswordEncoder passwordEncoder,
                            BankingMapper mapper) {
        this.userRepository = userRepository;
        this.holderRepository = holderRepository;
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
        this.mapper = mapper;
    }

    @Override
    public AccountResponse createAccount(CreateAccountRequest request) {
        ensureUnique(request.getUsername(), request.getEmail());
        validateAccountRules(request);

        BankUser user = BankUser.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .enabled(true)
                .roles(Set.of(UserRole.HOLDER))
                .build();
        user = userRepository.save(user);

        AccountHolder holder = AccountHolder.builder()
                .holderType(request.getHolderType())
                .displayName(request.getDisplayName())
                .user(user)
                .build();
        holder = holderRepository.save(holder);

        BankAccount account = BankAccount.builder()
                .accountNumber(generateAccountNumber())
                .accountSegment(request.getAccountSegment())
                .accountType(request.getAccountType())
                .balance(normalize(request.getInitialBalance()))
                .creditLimit(request.getCreditLimit() == null ? null : normalize(request.getCreditLimit()))
                .holder(holder)
                .build();
        return mapper.toAccountResponse(accountRepository.save(account));
    }

    private void validateAccountRules(CreateAccountRequest request) {
        BigDecimal initialBalance = normalize(request.getInitialBalance());
        if (request.getAccountType() == AccountType.SAVINGS && initialBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessRuleException("Savings account cannot start with negative balance");
        }
        if (request.getAccountType() == AccountType.CREDIT) {
            if (request.getCreditLimit() == null || request.getCreditLimit().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessRuleException("Credit account requires a positive credit limit");
            }
            BigDecimal minAllowed = normalize(request.getCreditLimit()).negate();
            if (initialBalance.compareTo(minAllowed) < 0) {
                throw new BusinessRuleException("Initial balance exceeds allowed credit limit");
            }
        }
    }

    private void ensureUnique(String username, String email) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new DuplicateResourceException("Username already exists: " + username);
        }
        if (userRepository.findByEmail(email).isPresent()) {
            throw new DuplicateResourceException("Email already exists: " + email);
        }
    }

    private String generateAccountNumber() {
        for (int i = 0; i < 20; i++) {
            String candidate = "AC" + System.currentTimeMillis() + ThreadLocalRandom.current().nextInt(1000, 9999);
            if (accountRepository.findByAccountNumber(candidate).isEmpty()) {
                return candidate;
            }
        }
        throw new BusinessRuleException("Unable to generate unique account number");
    }

    private BigDecimal normalize(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP);
    }
}
