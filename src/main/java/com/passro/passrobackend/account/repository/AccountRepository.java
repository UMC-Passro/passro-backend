package com.passro.passrobackend.account.repository;

import com.passro.passrobackend.account.entity.Account;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

    Optional <Account> findByEmail(String email);

    Optional<Account> findFirstByNameAndPhone(String name, String phone);

    Optional<Account> findFirstByNameAndPhoneAndEmail(String name, String phone, String email);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Account a WHERE a.id = :id")
    Optional<Account> findByIdForUpdate(@Param("id") Long id);
}
