package com.passro.passrobackend.chat.controller;

import com.passro.passrobackend.account.entity.Account;
import com.passro.passrobackend.account.repository.AccountRepository;
import com.passro.passrobackend.chat.code.ChatSuccessCode;
import com.passro.passrobackend.chat.dto.ChatMessageRequestDto;
import com.passro.passrobackend.chat.dto.ChatMessageResponseDto;
import com.passro.passrobackend.chat.dto.ChatRoomInfoResponseDto;
import com.passro.passrobackend.chat.service.ChatService;
import com.passro.passrobackend.global.response.APIResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// TODO: 인증 구현 후 아래 작업 필요
//  1. @RequestParam Long accountId 파라미터 전부 제거
//  2. 각 메서드 파라미터에 @AuthenticationPrincipal Account account 추가
//  3. accountRepository.findById(accountId) 호출 전부 제거
//  4. AccountRepository 의존성 제거 (필드 및 import)
//  5. SecurityConfiguration에서 "/chat/**" permitAll 제거
@Tag(name = "Chat", description = "채팅 API - delivery의 sender/shipper 간 1:1 채팅. WAIT·CANCEL 상태의 배송건은 접근 불가.")
@RestController
@RequestMapping("/chat/{deliveryId}")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    // TODO: 인증 구현 후 제거
    private final AccountRepository accountRepository;

    @Operation(
            summary = "메시지 조회",
            description = "afterId 없으면 전체 메시지 반환 (최초 진입), afterId 있으면 해당 id 이후의 새 메시지만 반환 (polling). 조회 시 상대방 메시지 자동 읽음 처리."
    )
    @GetMapping("/messages")
    public APIResponse<List<ChatMessageResponseDto>> getMessages(
            @PathVariable Long deliveryId,
            @Parameter(description = "마지막으로 받은 메시지 id. 없으면 전체 조회, 있으면 해당 id 이후 메시지만 반환")
            @RequestParam(required = false) Long afterId,
            @Parameter(description = "TODO: 인증 구현 후 제거") @RequestParam Long accountId
    ) {
        Account account = accountRepository.findById(accountId).orElseThrow(); // TODO: 인증 구현 후 제거
        List<ChatMessageResponseDto> messages = (afterId == null)
                ? chatService.getMessages(deliveryId, account)
                : chatService.getMessagesAfter(deliveryId, afterId, account);

        return APIResponse.onSuccess(ChatSuccessCode.OK, messages);
    }

    @Operation(
            summary = "메시지 전송",
            description = "메시지를 전송한다. 전송된 메시지는 isRead=false로 저장되며, 상대방이 메시지 조회 시 자동으로 읽음 처리된다."
    )
    @PostMapping("/messages")
    public APIResponse<ChatMessageResponseDto> sendMessage(
            @PathVariable Long deliveryId,
            @Parameter(description = "TODO: 인증 구현 후 제거") @RequestParam Long accountId,
            @Valid @RequestBody ChatMessageRequestDto request
    ) {
        Account account = accountRepository.findById(accountId).orElseThrow(); // TODO: 인증 구현 후 제거
        return APIResponse.onSuccess(ChatSuccessCode.CREATED, chatService.sendMessage(deliveryId, request, account));
    }

    @Operation(
            summary = "채팅방 헤더 정보 조회",
            description = "채팅방 상단에 표시할 정보를 반환한다. 상대방 닉네임·프로필 사진, 물품명, 출발지·도착지, 현재 배송 상태를 포함한다."
    )
    @GetMapping("/info")
    public APIResponse<ChatRoomInfoResponseDto> getChatRoomInfo(
            @PathVariable Long deliveryId,
            @Parameter(description = "TODO: 인증 구현 후 제거") @RequestParam Long accountId
    ) {
        Account account = accountRepository.findById(accountId).orElseThrow(); // TODO: 인증 구현 후 제거
        return APIResponse.onSuccess(ChatSuccessCode.OK, chatService.getChatRoomInfo(deliveryId, account));
    }

    @Operation(
            summary = "안읽은 메시지 수 조회",
            description = "상대방이 보낸 메시지 중 아직 읽지 않은 메시지 수를 반환한다. 채팅 목록 화면의 뱃지 숫자 표시에 사용한다."
    )
    @GetMapping("/unread-count")
    public APIResponse<Long> getUnreadCount(
            @PathVariable Long deliveryId,
            @Parameter(description = "TODO: 인증 구현 후 제거") @RequestParam Long accountId
    ) {
        Account account = accountRepository.findById(accountId).orElseThrow(); // TODO: 인증 구현 후 제거
        return APIResponse.onSuccess(ChatSuccessCode.OK, chatService.getUnreadCount(deliveryId, account));
    }
}