package com.github.deniskoriavets.smartexpensetracker.repository;

import com.github.deniskoriavets.smartexpensetracker.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID> {
}
