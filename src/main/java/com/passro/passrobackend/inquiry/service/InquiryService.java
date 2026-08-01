package com.passro.passrobackend.inquiry.service;

import com.passro.passrobackend.account.entity.Account;
import com.passro.passrobackend.inquiry.dto.InquiryCreateRequestDto;
import com.passro.passrobackend.inquiry.dto.InquiryResponseDto;
import com.passro.passrobackend.inquiry.entity.Inquiry;
import com.passro.passrobackend.inquiry.repository.InquiryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InquiryService {

    private final InquiryRepository inquiryRepository;

    @Transactional
    public InquiryResponseDto createInquiry(Account account, InquiryCreateRequestDto request) {
        Inquiry inquiry = Inquiry.builder()
                .account(account)
                .category(request.getCategory())
                .title(request.getTitle())
                .content(request.getContent())
                .build();

        return InquiryResponseDto.fromInquiry(inquiryRepository.save(inquiry));
    }
}
