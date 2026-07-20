package com.bigapplication.bankingsystem.service.impl;

import com.bigapplication.bankingsystem.dto.request.CreateAccountRequest;
import com.bigapplication.bankingsystem.dto.request.UpdateAccountRequest;
import com.bigapplication.bankingsystem.dto.response.AccountResponse;
import com.bigapplication.bankingsystem.entity.AccountHolder;
import com.bigapplication.bankingsystem.entity.AccountType;
import com.bigapplication.bankingsystem.entity.BankAccount;
import com.bigapplication.bankingsystem.entity.BankUser;
import com.bigapplication.bankingsystem.entity.UserRole;
import com.bigapplication.bankingsystem.exception.BusinessRuleException;
import com.bigapplication.bankingsystem.exception.DuplicateResourceException;
import com.bigapplication.bankingsystem.exception.ResourceNotFoundException;
import com.bigapplication.bankingsystem.mapper.BankingMapper;
import com.bigapplication.bankingsystem.repository.AccountHolderRepository;
import com.bigapplication.bankingsystem.repository.BankAccountRepository;
import com.bigapplication.bankingsystem.repository.BankUserRepository;
import com.bigapplication.bankingsystem.repository.BankTransactionRepository;
import com.bigapplication.bankingsystem.service.AdminService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AdminServiceImpl implements AdminService {

    private static final Logger log = LoggerFactory.getLogger(AdminServiceImpl.class);

    private final BankUserRepository userRepository;
    private final AccountHolderRepository holderRepository;
    private final BankAccountRepository accountRepository;
    private final BankTransactionRepository transactionRepository;
    private final PasswordEncoder passwordEncoder;
    private final BankingMapper mapper;

    public AdminServiceImpl(BankUserRepository userRepository,
                            AccountHolderRepository holderRepository,
                            BankAccountRepository accountRepository,
                            BankTransactionRepository transactionRepository,
                            PasswordEncoder passwordEncoder,
                            BankingMapper mapper) {
        this.userRepository = userRepository;
        this.holderRepository = holderRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.passwordEncoder = passwordEncoder;
        this.mapper = mapper;
    }

    @Override
    public AccountResponse createAccount(CreateAccountRequest request) {
        log.info("Creating banking account username={} holderName={} type={} segment={}",
                request.getUsername(), request.getDisplayName(), request.getAccountType(), request.getAccountSegment());
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
        AccountResponse response = mapper.toAccountResponse(accountRepository.save(account));
        log.info("Banking account created successfully accountNumber={} username={}",
                response.getAccountNumber(), request.getUsername());
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountResponse> getAllAccounts() {
        log.info("Loading all banking accounts for admin view");
        return accountRepository.findAll().stream()
                .map(mapper::toAccountResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountResponse> searchAccounts(String name, String accountNumber, Long accountId) {
        String normalizedName = normalizeSearchText(name);
        String normalizedAccountNumber = normalizeSearchText(accountNumber);
        if (normalizedName == null && normalizedAccountNumber == null && accountId == null) {
            log.info("Skipping admin account search because no search criteria were provided");
            return List.of();
        }

        log.info("Searching banking accounts for admin view name={} accountNumber={} accountId={}",
                normalizedName, normalizedAccountNumber, accountId);
        Map<Long, BankAccount> matches = new LinkedHashMap<>();
        if (accountId != null) {
            accountRepository.findById(accountId).ifPresent(account -> matches.put(account.getId(), account));
        }
        if (normalizedAccountNumber != null) {
            accountRepository.findByAccountNumberContainingIgnoreCaseOrderByIdAsc(normalizedAccountNumber)
                    .forEach(account -> matches.putIfAbsent(account.getId(), account));
        }
        if (normalizedName != null) {
            accountRepository.findByHolderDisplayNameContainingIgnoreCaseOrderByIdAsc(normalizedName)
                    .forEach(account -> matches.putIfAbsent(account.getId(), account));
        }
        return matches.values().stream()
                .map(mapper::toAccountResponse)
                .toList();
    }

    @Override
    public AccountResponse updateAccount(Long accountId, UpdateAccountRequest request) {
        log.info("Updating banking account id={} displayName={} type={} segment={}",
                accountId, request.getDisplayName(), request.getAccountType(), request.getAccountSegment());
        BankAccount account = accountRepository.findLockedById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with id: " + accountId));

        BigDecimal currentBalance = normalize(account.getBalance());
        BigDecimal nextCreditLimit = request.getCreditLimit() == null ? null : normalize(request.getCreditLimit());
        validateAccountRules(request.getAccountType(), currentBalance, nextCreditLimit);

        account.getHolder().setDisplayName(request.getDisplayName());
        account.setAccountSegment(request.getAccountSegment());
        account.setAccountType(request.getAccountType());
        account.setCreditLimit(nextCreditLimit);

        AccountResponse response = mapper.toAccountResponse(accountRepository.save(account));
        log.info("Banking account updated successfully accountId={} accountNumber={}",
                response.getId(), response.getAccountNumber());
        return response;
    }

    @Override
    public void deleteAccount(Long accountId) {
        log.info("Deleting banking account id={}", accountId);
        BankAccount account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with id: " + accountId));
        transactionRepository.deleteByAccountId(accountId);
        accountRepository.delete(account);
        log.info("Banking account deleted successfully id={}", accountId);
    }

    private void validateAccountRules(CreateAccountRequest request) {
        validateAccountRules(request.getAccountType(), normalize(request.getInitialBalance()),
                request.getCreditLimit() == null ? null : normalize(request.getCreditLimit()));
    }

    private String normalizeSearchText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private void validateAccountRules(AccountType accountType, BigDecimal balance, BigDecimal creditLimit) {
        if (accountType == AccountType.SAVINGS && balance.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessRuleException("Savings account cannot start with negative balance");
        }
        if (accountType == AccountType.CREDIT) {
            if (creditLimit == null || creditLimit.compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessRuleException("Credit account requires a positive credit limit");
            }
            BigDecimal minAllowed = creditLimit.negate();
            if (balance.compareTo(minAllowed) < 0) {
                throw new BusinessRuleException("Initial balance exceeds allowed credit limit");
            }
        }
    }

    private void ensureUnique(String username, String email) {
        if (userRepository.findByUsername(username).isPresent()) {
            log.warn("Duplicate banking username attempted username={}", username);
            throw new DuplicateResourceException("Username already exists: " + username);
        }
        if (userRepository.findByEmail(email).isPresent()) {
            log.warn("Duplicate banking email attempted email={}", email);
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
