package com.passro.passrobackend.account.repository;

import com.passro.passrobackend.account.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

    Optional <Account> findByEmail(String email);

    Optional<Account> findFirstByNameAndPhone(String name, String phone);

    Optional<Account> findFirstByNameAndPhoneAndEmail(String name, String phone, String email);
}
