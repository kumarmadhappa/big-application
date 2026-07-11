package com.bigapplication.bankingsystem.service;

import com.bigapplication.bankingsystem.dto.request.CreateAccountRequest;
import com.bigapplication.bankingsystem.dto.response.AccountResponse;

public interface AdminService {
    AccountResponse createAccount(CreateAccountRequest request);
}
