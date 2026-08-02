package com.passro.passrobackend.inquiry.service;

import com.passro.passrobackend.account.entity.Account;
import com.passro.passrobackend.inquiry.dto.InquiryCreateRequestDto;
import com.passro.passrobackend.inquiry.dto.InquiryResponseDto;
import com.passro.passrobackend.inquiry.entity.Inquiry;
import com.passro.passrobackend.inquiry.enums.InquiryCategory;
import com.passro.passrobackend.inquiry.repository.InquiryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class InquiryServiceTest {

    @Mock
    private InquiryRepository inquiryRepository;

    @InjectMocks
    private InquiryService inquiryService;

    private Account account(Long id) {
        return Account.builder().id(id).nickname("tester").build();
    }

    @Test
    @DisplayName("공통 문의 작성 성공")
    void createInquiry_success() {
        // given
        Account account = account(10L);
        InquiryCreateRequestDto request = InquiryCreateRequestDto.builder()
                .category(InquiryCategory.ACCOUNT)
                .title("로그인이 안돼요")
                .content("비밀번호 재설정 이메일이 오지 않습니다.")
                .build();
        given(inquiryRepository.save(any(Inquiry.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        // when
        InquiryResponseDto response = inquiryService.createInquiry(account, request);

        // then
        assertThat(response.getCategory()).isEqualTo(InquiryCategory.ACCOUNT);
        assertThat(response.getTitle()).isEqualTo("로그인이 안돼요");
        assertThat(response.getContent()).isEqualTo("비밀번호 재설정 이메일이 오지 않습니다.");
        assertThat(response.getWriterNickname()).isEqualTo("tester");
        verify(inquiryRepository).save(any(Inquiry.class));
    }
}
