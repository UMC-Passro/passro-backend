package com.passro.passrobackend.shipper.controller;

import com.passro.passrobackend.account.entity.Account;
import com.passro.passrobackend.delivery.entity.Delivery;
import com.passro.passrobackend.global.response.APIResponse;
import com.passro.passrobackend.shipper.code.ShipperSuccessCode;
import com.passro.passrobackend.shipper.dto.ShipperDeliveryDto;
import com.passro.passrobackend.shipper.service.ShipperService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/shipper")
@RequiredArgsConstructor
public class ShipperController {
    private final ShipperService shipperService;

    @GetMapping("/delivery")
    @ResponseBody
    public APIResponse<?> listDelivery(@AuthenticationPrincipal Account account) {
         return APIResponse.onSuccess(ShipperSuccessCode.OK, shipperService.listAllBySender(account).stream().map(ShipperDeliveryDto::fromDelivery).toList());
    }

    @GetMapping("/delivery/{deliveryId}")
    @ResponseBody
    public APIResponse<?> getDelivery(@AuthenticationPrincipal Account account, @PathVariable("deliveryId") Long deliveryId) {
        Delivery delivery = shipperService.getDeliveryById(account, deliveryId);

        return APIResponse.onSuccess(ShipperSuccessCode.OK, ShipperDeliveryDto.fromDelivery(delivery));
    }

    @GetMapping("/matched")
    @ResponseBody
    public APIResponse<?> getMatched(@AuthenticationPrincipal Account account) {
        return APIResponse.onSuccess(ShipperSuccessCode.OK, shipperService.listMatchRequested().stream().map(ShipperDeliveryDto::fromDelivery).toList());
    }

    @PostMapping("/matched/{deliveryId}")
    @ResponseBody
    public APIResponse<?> matchAccept(@AuthenticationPrincipal Account account, @PathVariable("deliveryId") Long deliveryId) {
        shipperService.matchAccept(account, deliveryId);
        return APIResponse.onSuccess(ShipperSuccessCode.OK, null);
    }

    @PostMapping("/acquire/{deliveryId}")
    @ResponseBody
    public APIResponse<?> acquireAccept(@AuthenticationPrincipal Account account, @PathVariable("deliveryId") Long deliveryId) {
        shipperService.acquireAccept(account, deliveryId);
        return APIResponse.onSuccess(ShipperSuccessCode.OK, null);
    }

    @PostMapping("/confirm/{deliveryId}")
    @ResponseBody
    public APIResponse<?> acquireConfirm(@AuthenticationPrincipal Account account, @PathVariable("deliveryId") Long deliveryId) {
        shipperService.acquireConfirm(account, deliveryId);
        return APIResponse.onSuccess(ShipperSuccessCode.OK, null);
    }

    @PostMapping("/complete/{deliveryId}")
    @ResponseBody
    public APIResponse<?> acquireComplete(@AuthenticationPrincipal Account account, @PathVariable("deliveryId") Long deliveryId) {
        shipperService.acquireComplete(account, deliveryId);
        return APIResponse.onSuccess(ShipperSuccessCode.OK, null);
    }
}
