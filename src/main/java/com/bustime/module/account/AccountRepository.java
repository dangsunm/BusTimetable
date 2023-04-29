package com.bustime.module.account;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface AccountRepository extends JpaRepository<Account, Long>, QuerydslPredicateExecutor<Account> {
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    Account findByEmail(String email);
    Account findByUsername(String username);
    Account findAccountWithTagssById(Long id);
}
