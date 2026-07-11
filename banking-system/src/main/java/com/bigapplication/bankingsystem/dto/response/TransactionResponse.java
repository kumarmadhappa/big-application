package com.bigapplication.bankingsystem.dto.response;

import com.bigapplication.bankingsystem.entity.TransactionType;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class TransactionResponse {
    Long transactionId;
    Long accountId;
    String accountNumber;
    TransactionType transactionType;
    BigDecimal amount;
    BigDecimal balanceBefore;
    BigDecimal balanceAfter;
    Instant createdAt;
}
