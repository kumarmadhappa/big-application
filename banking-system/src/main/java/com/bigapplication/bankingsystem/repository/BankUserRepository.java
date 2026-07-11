package com.bigapplication.bankingsystem.repository;

import com.bigapplication.bankingsystem.entity.BankUser;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BankUserRepository extends JpaRepository<BankUser, Long> {
    Optional<BankUser> findByUsername(String username);

    Optional<BankUser> findByEmail(String email);

    Optional<BankUser> findByUsernameOrEmail(String username, String email);
}
