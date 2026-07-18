package com.bigapplication.bankingsystem.service;

import com.bigapplication.bankingsystem.dto.response.TransactionResponse;
import java.math.BigDecimal;

public interface TransactionService {
    TransactionResponse deposit(Long accountId, BigDecimal amount, String actorUsername);

    TransactionResponse withdraw(Long accountId, BigDecimal amount, String actorUsername);

    TransactionResponse deposit(Long accountId, BigDecimal amount, String actorUsername, boolean allowAnyAccount);

    TransactionResponse withdraw(Long accountId, BigDecimal amount, String actorUsername, boolean allowAnyAccount);
}
