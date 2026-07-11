package com.bigapplication.bankingsystem.dto.response;

import com.bigapplication.bankingsystem.entity.AccountSegment;
import com.bigapplication.bankingsystem.entity.AccountType;
import com.bigapplication.bankingsystem.entity.HolderType;
import java.math.BigDecimal;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AccountResponse {
    Long id;
    String accountNumber;
    AccountSegment accountSegment;
    AccountType accountType;
    BigDecimal balance;
    BigDecimal creditLimit;
    HolderType holderType;
    String holderName;
    String holderUsername;
}
