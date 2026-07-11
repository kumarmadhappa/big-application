package com.bigapplication.bankingsystem.repository;

import com.bigapplication.bankingsystem.entity.BankTransaction;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BankTransactionRepository extends JpaRepository<BankTransaction, Long> {
    List<BankTransaction> findTop20ByAccountIdOrderByCreatedAtDesc(Long accountId);
}
