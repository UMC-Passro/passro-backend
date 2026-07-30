package com.passro.passrobackend.account.repository;

import com.passro.passrobackend.account.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    boolean existsByMail(String mail);

    boolean existsByNickname(String nickname);

    boolean existsByPhoneNumber(String phoneNumber);

    Optional <Account> findByMail(String mail);

    Optional<Account> findFirstByNameAndPhoneNumber(String name, String phoneNumber);

    Optional<Account> findFirstByNameAndPhoneNumberAndMail(String name, String phoneNumber, String mail);
}
