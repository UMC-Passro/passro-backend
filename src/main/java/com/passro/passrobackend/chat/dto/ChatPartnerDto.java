package com.passro.passrobackend.chat.dto;

import com.passro.passrobackend.account.entity.Account;

public record ChatPartnerDto(
        Long id,
        String nickname,
        String picture
) {
    public static ChatPartnerDto from(Account account) {
        return new ChatPartnerDto(
                account.getId(),
                account.getNickname(),
                account.getPicture()
        );
    }
}