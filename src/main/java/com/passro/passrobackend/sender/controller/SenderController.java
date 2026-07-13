package com.passro.passrobackend.sender.controller;

import com.passro.passrobackend.account.entity.Account;
import com.passro.passrobackend.global.response.APIResponse;
import com.passro.passrobackend.sender.code.SenderSuccessCode;
import com.passro.passrobackend.sender.service.SenderService;
import lombok.RequiredArgsConstructor;
import org.springframework.expression.spel.ast.NullLiteral;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/shipper")
@RequiredArgsConstructor
public class SenderController {
    private final SenderService senderService;

    @GetMapping("/delivery")
    @ResponseBody
    public APIResponse<?> listDelivery(@AuthenticationPrincipal Account account) {
         return APIResponse.onSuccess(SenderSuccessCode.OK, senderService.listAllBySender(account));
    }

    @GetMapping("/delivery/{deliveryId}")
    @ResponseBody
    public APIResponse<?> getDelivery(@AuthenticationPrincipal Account account, @PathVariable("deliveryId") Long deliveryId) {
        return APIResponse.onSuccess(SenderSuccessCode.OK, senderService.getDeliveryById(account, deliveryId));
    }

    @GetMapping("/matched")
    @ResponseBody
    public APIResponse<?> getMatched(@AuthenticationPrincipal Account account) {
        return APIResponse.onSuccess(SenderSuccessCode.OK, senderService.listMatchRequested());
    }

    @PostMapping("/matched/{deliveryId}")
    @ResponseBody
    public APIResponse<?> matchAccept(@AuthenticationPrincipal Account account, @PathVariable("deliveryId") Long deliveryId) {
        senderService.matchAccept(account, deliveryId);
        return APIResponse.onSuccess(SenderSuccessCode.OK, null);
    }

    @PostMapping("/acquire/{deliveryId}")
    @ResponseBody
    public APIResponse<?> acquireAccept(@AuthenticationPrincipal Account account, @PathVariable("deliveryId") Long deliveryId) {
        senderService.acquireAccept(account, deliveryId);
        return APIResponse.onSuccess(SenderSuccessCode.OK, null);
    }

    @PostMapping("/confirm/{deliveryId}")
    @ResponseBody
    public APIResponse<?> acquireConfirm(@AuthenticationPrincipal Account account, @PathVariable("deliveryId") Long deliveryId) {
        senderService.acquireConfirm(account, deliveryId);
        return APIResponse.onSuccess(SenderSuccessCode.OK, null);
    }

    @PostMapping("/complete/{deliveryId}")
    @ResponseBody
    public APIResponse<?> acquireComplete(@AuthenticationPrincipal Account account, @PathVariable("deliveryId") Long deliveryId) {
        senderService.acquireComplete(account, deliveryId);
        return APIResponse.onSuccess(SenderSuccessCode.OK, null);
    }
}
