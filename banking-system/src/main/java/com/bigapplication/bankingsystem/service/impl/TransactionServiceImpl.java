package com.bigapplication.bankingsystem.service.impl;

import com.bigapplication.bankingsystem.dto.response.TransactionResponse;
import com.bigapplication.bankingsystem.entity.AccountType;
import com.bigapplication.bankingsystem.entity.BankAccount;
import com.bigapplication.bankingsystem.entity.BankTransaction;
import com.bigapplication.bankingsystem.entity.TransactionType;
import com.bigapplication.bankingsystem.exception.BusinessRuleException;
import com.bigapplication.bankingsystem.exception.ResourceNotFoundException;
import com.bigapplication.bankingsystem.mapper.BankingMapper;
import com.bigapplication.bankingsystem.repository.BankAccountRepository;
import com.bigapplication.bankingsystem.repository.BankTransactionRepository;
import com.bigapplication.bankingsystem.service.TransactionService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TransactionServiceImpl implements TransactionService {

    private final BankAccountRepository accountRepository;
    private final BankTransactionRepository transactionRepository;
    private final BankingMapper mapper;

    public TransactionServiceImpl(BankAccountRepository accountRepository,
                                  BankTransactionRepository transactionRepository,
                                  BankingMapper mapper) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.mapper = mapper;
    }

    @Override
    public TransactionResponse deposit(Long accountId, BigDecimal amount, String actorUsername) {
        BankAccount account = findOwnedAccount(accountId, actorUsername);
        BigDecimal normalizedAmount = normalizePositiveAmount(amount);
        BigDecimal before = account.getBalance();
        BigDecimal after = before.add(normalizedAmount);
        account.setBalance(after);
        accountRepository.save(account);

        return mapper.toTransactionResponse(transactionRepository.save(BankTransaction.builder()
                .account(account)
                .transactionType(TransactionType.DEPOSIT)
                .amount(normalizedAmount)
                .balanceBefore(before)
                .balanceAfter(after)
                .performedBy(actorUsername)
                .build()));
    }

    @Override
    public TransactionResponse withdraw(Long accountId, BigDecimal amount, String actorUsername) {
        BankAccount account = findOwnedAccount(accountId, actorUsername);
        BigDecimal normalizedAmount = normalizePositiveAmount(amount);
        BigDecimal before = account.getBalance();
        BigDecimal after = before.subtract(normalizedAmount);

        if (account.getAccountType() == AccountType.SAVINGS && after.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessRuleException("Insufficient funds for savings account");
        }

        if (account.getAccountType() == AccountType.CREDIT) {
            BigDecimal creditLimit = account.getCreditLimit() == null ? BigDecimal.ZERO : account.getCreditLimit();
            if (after.compareTo(creditLimit.negate()) < 0) {
                throw new BusinessRuleException("Withdrawal exceeds credit limit");
            }
        }

        account.setBalance(after);
        accountRepository.save(account);
        return mapper.toTransactionResponse(transactionRepository.save(BankTransaction.builder()
                .account(account)
                .transactionType(TransactionType.WITHDRAWAL)
                .amount(normalizedAmount)
                .balanceBefore(before)
                .balanceAfter(after)
                .performedBy(actorUsername)
                .build()));
    }

    private BankAccount findOwnedAccount(Long accountId, String actorUsername) {
        BankAccount account = accountRepository.findLockedById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with id: " + accountId));
        String ownerUsername = account.getHolder().getUser().getUsername();
        if (!ownerUsername.equals(actorUsername)) {
            throw new BusinessRuleException("You are not allowed to perform operations on this account");
        }
        return account;
    }

    private BigDecimal normalizePositiveAmount(BigDecimal amount) {
        BigDecimal normalized = amount.setScale(2, RoundingMode.HALF_UP);
        if (normalized.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleException("Amount must be greater than zero");
        }
        return normalized;
    }
}
