package com.example.ms_account_reservation.repository;

import com.example.ms_account_reservation.entity.AccountStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountStatusRepository extends JpaRepository<AccountStatus, Integer> {
    Optional<AccountStatus> findByName(String name);
    boolean existsByName(String name);
}