package com.dto.way.notification.web.controller;

import com.dto.way.notification.domain.entity.Notification;
import com.dto.way.notification.domain.service.EmitterService;
import com.dto.way.notification.domain.service.NotificationService;
import com.dto.way.notification.global.JwtUtils;
import com.dto.way.notification.web.response.ApiResponse;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;

import static com.dto.way.notification.web.response.code.status.SuccessStatus._OK;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/notification-service")
public class NotificationController {

    private final EmitterService emitterService;
    private final NotificationService notificationService;
    private final JwtUtils jwtUtils;

    @GetMapping(value = "/sse-connection", produces = "text/event-stream")
    public ApiResponse<SseEmitter> stream(HttpServletRequest request,
                             @RequestHeader(value = "Last-Event-Id", required = false, defaultValue = "") String lastEventId) {

        // 토큰에서 요청 유저 정보 추출
        String token = jwtUtils.resolveToken(request);
        Claims claims = jwtUtils.parseClaims(token);

        Long loginMemberId = claims.get("memberId", Long.class);

        // 이메일 대신 memberId 들어가야 함
        SseEmitter sseEmitter = emitterService.addEmitter(loginMemberId, lastEventId);
        return ApiResponse.of(_OK, sseEmitter);
    }

    // memberID 가지고 알림 목록 조회 api
    @PostMapping("/notification-list")
    public ApiResponse<List<Notification>> readNotifications(HttpServletRequest request) {

        // 토큰에서 요청 유저 정보 추출
        String token = jwtUtils.resolveToken(request);
        Claims claims = jwtUtils.parseClaims(token);

        Long loginMemberId = claims.get("memberId", Long.class);

        List<Notification> notificationList = notificationService.selectNotificationList(loginMemberId);
        return ApiResponse.of(_OK, notificationList);
    }

}
