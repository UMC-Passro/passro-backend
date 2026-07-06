package com.passro.passrobackend.account.repository;

import com.passro.passrobackend.account.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {
}
