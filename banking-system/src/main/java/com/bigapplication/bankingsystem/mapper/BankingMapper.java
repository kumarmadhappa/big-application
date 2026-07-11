package com.bigapplication.bankingsystem.mapper;

import com.bigapplication.bankingsystem.dto.response.AccountResponse;
import com.bigapplication.bankingsystem.dto.response.AuthUserResponse;
import com.bigapplication.bankingsystem.dto.response.TransactionResponse;
import com.bigapplication.bankingsystem.entity.BankAccount;
import com.bigapplication.bankingsystem.entity.BankTransaction;
import com.bigapplication.bankingsystem.entity.BankUser;
import org.springframework.stereotype.Component;

@Component
public class BankingMapper {

    public AuthUserResponse toAuthUser(BankUser user) {
        return AuthUserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(user.getRoles())
                .build();
    }

    public AccountResponse toAccountResponse(BankAccount account) {
        return AccountResponse.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .accountSegment(account.getAccountSegment())
                .accountType(account.getAccountType())
                .balance(account.getBalance())
                .creditLimit(account.getCreditLimit())
                .holderType(account.getHolder().getHolderType())
                .holderName(account.getHolder().getDisplayName())
                .holderUsername(account.getHolder().getUser().getUsername())
                .build();
    }

    public TransactionResponse toTransactionResponse(BankTransaction transaction) {
        return TransactionResponse.builder()
                .transactionId(transaction.getId())
                .accountId(transaction.getAccount().getId())
                .accountNumber(transaction.getAccount().getAccountNumber())
                .transactionType(transaction.getTransactionType())
                .amount(transaction.getAmount())
                .balanceBefore(transaction.getBalanceBefore())
                .balanceAfter(transaction.getBalanceAfter())
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}
