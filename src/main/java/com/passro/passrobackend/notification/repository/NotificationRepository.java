package com.passro.passrobackend.notification.repository;

import com.passro.passrobackend.account.entity.Account;
import com.passro.passrobackend.notification.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // 특정 계정의 알림 목록 (최신순, 페이지네이션)
    Page<Notification> findAllByAccountOrderByCreatedAtDesc(Account account, Pageable pageable);

    // 특정 계정의 미확인 알림 수
    long countByAccountAndIsReadFalse(Account account);
}
