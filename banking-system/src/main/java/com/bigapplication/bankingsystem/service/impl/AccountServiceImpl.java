package com.bigapplication.bankingsystem.service.impl;

import com.bigapplication.bankingsystem.dto.response.AccountResponse;
import com.bigapplication.bankingsystem.entity.AccountHolder;
import com.bigapplication.bankingsystem.exception.ResourceNotFoundException;
import com.bigapplication.bankingsystem.mapper.BankingMapper;
import com.bigapplication.bankingsystem.repository.AccountHolderRepository;
import com.bigapplication.bankingsystem.repository.BankAccountRepository;
import com.bigapplication.bankingsystem.service.AccountService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AccountServiceImpl implements AccountService {

    private final AccountHolderRepository holderRepository;
    private final BankAccountRepository accountRepository;
    private final BankingMapper mapper;

    public AccountServiceImpl(AccountHolderRepository holderRepository,
                              BankAccountRepository accountRepository,
                              BankingMapper mapper) {
        this.holderRepository = holderRepository;
        this.accountRepository = accountRepository;
        this.mapper = mapper;
    }

    @Override
    public List<AccountResponse> getAccountsForCurrentUser(String username) {
        AccountHolder holder = holderRepository.findByUserUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Account holder not found for username: " + username));
        return accountRepository.findByHolderId(holder.getId()).stream()
                .map(mapper::toAccountResponse)
                .toList();
    }
}
