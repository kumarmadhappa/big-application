package com.bigapplication.bankingsystem.service;

import com.bigapplication.bankingsystem.dto.response.AccountResponse;
import java.util.List;

public interface AccountService {
    List<AccountResponse> getAccountsForCurrentUser(String username);
}
