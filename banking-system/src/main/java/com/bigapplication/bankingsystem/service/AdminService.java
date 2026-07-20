package com.bigapplication.bankingsystem.service;

import com.bigapplication.bankingsystem.dto.request.CreateAccountRequest;
import com.bigapplication.bankingsystem.dto.request.UpdateAccountRequest;
import com.bigapplication.bankingsystem.dto.response.AccountResponse;
import java.util.List;

public interface AdminService {
    AccountResponse createAccount(CreateAccountRequest request);
    List<AccountResponse> getAllAccounts();
    List<AccountResponse> searchAccounts(String name, String accountNumber, Long accountId);
    AccountResponse updateAccount(Long accountId, UpdateAccountRequest request);
    void deleteAccount(Long accountId);
}
