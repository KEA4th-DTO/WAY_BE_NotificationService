package com.dto.way.notification.web.controller;

import com.dto.way.notification.domain.entity.Notification;
import com.dto.way.notification.domain.service.EmitterService;
import com.dto.way.notification.domain.service.NotificationService;
import com.dto.way.notification.web.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/notification-service")
public class NotificationController {

    private final EmitterService emitterService;
    private final NotificationService notificationService;

    @GetMapping(value = "/sse-connection", produces = "text/event-stream")
    public ApiResponse<SseEmitter> stream(Authentication authentication,
                                         @RequestHeader(value = "Last-Event-Id", required = false, defaultValue = "") String lastEventId) {

        // 이메일 대신 memberId 들어가야 함
        SseEmitter sseEmitter = emitterService.addEmitter(authentication.getName(), lastEventId);
        return ApiResponse.onSuccess(sseEmitter);
    }

    // memberID 가지고 알림 목록 조회 api
    @PostMapping("/notification-list")
    public ApiResponse<List<Notification>> readNotifications(@RequestBody String nickname) {
        // authentication에서 이메일로 유저정보를 찾고 거기서 닉네임을 찾아야한다.
        // requestbody로 닉네임 받으면 안된다. 수정 필요.
        List<Notification> notificationList = notificationService.selectNotificationList(nickname);
        return ApiResponse.onSuccess(notificationList);
    }

}
