package com.dto.way.notification.web.controller;

import com.dto.way.notification.domain.entity.Notification;
import com.dto.way.notification.domain.service.EmitterService;
import com.dto.way.notification.domain.service.NotificationService;
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
    public SseEmitter stream(Authentication authentication,
                      @RequestHeader(value = "Last-Event-Id", required = false, defaultValue = "") String lastEventId) {

        // 이메일 대신 memberId 들어가야 함
        return emitterService.addEmitter(authentication.getName(), lastEventId);
    }

    // memberID 가지고 알림 목록 조회 api
    @PostMapping("/notification-list")
    public List<Notification>  readNotifications(@RequestBody String nickname) {
        return notificationService.selectNotificationList(nickname);
    }

}
