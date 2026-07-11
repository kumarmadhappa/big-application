package com.bigapplication.bankingsystem.repository;

import com.bigapplication.bankingsystem.entity.AccountHolder;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountHolderRepository extends JpaRepository<AccountHolder, Long> {
    Optional<AccountHolder> findByUserUsername(String username);
}
