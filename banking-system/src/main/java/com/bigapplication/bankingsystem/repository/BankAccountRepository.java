package com.bigapplication.bankingsystem.repository;

import com.bigapplication.bankingsystem.entity.BankAccount;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import jakarta.persistence.LockModeType;

public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {
    Optional<BankAccount> findByAccountNumber(String accountNumber);

    List<BankAccount> findByHolderId(Long holderId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<BankAccount> findLockedById(Long id);
}
