package com.dto.way.notification.web.controller;

import com.dto.way.notification.domain.entity.Notification;
import com.dto.way.notification.domain.service.EmitterService;
import com.dto.way.notification.domain.service.NotificationService;
import com.dto.way.notification.global.JwtUtils;
import com.dto.way.notification.web.response.ApiResponse;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

import static com.dto.way.notification.web.response.code.status.SuccessStatus.*;
import static com.dto.way.notification.web.response.code.status.ErrorStatus.*;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/notification-service")
public class NotificationController {

    private final EmitterService emitterService;
    private final NotificationService notificationService;
    private final JwtUtils jwtUtils;

    @GetMapping(value = "/sse-connection", produces = "text/event-stream")
    public SseEmitter stream(HttpServletRequest request,
                             @RequestHeader(value = "Last-Event-Id", required = false, defaultValue = "") String lastEventId) {

        // 토큰에서 요청 유저 정보 추출
        String token = jwtUtils.resolveToken(request);
        Claims claims = jwtUtils.parseClaims(token);

        Long loginMemberId = claims.get("memberId", Long.class);

        // 이메일 대신 memberId 들어가야 함
        return emitterService.addEmitter(loginMemberId, lastEventId);
    }

    // memberID 가지고 알림 목록 조회 api
    @Operation(summary = "알림목록 조회 API", description = "사용자의 알림 목록을 조회하는 API 입니다.")
    @PostMapping("/notification-list")
    public ApiResponse<List<Notification>> readNotifications(HttpServletRequest request) {

        // 토큰에서 요청 유저 정보 추출
        String token = jwtUtils.resolveToken(request);
        Claims claims = jwtUtils.parseClaims(token);

        Long loginMemberId = claims.get("memberId", Long.class);

        List<Notification> notificationList = notificationService.selectNotificationList(loginMemberId);
        return ApiResponse.of(_OK, notificationList);
    }

    @Operation(summary = "알림 삭제 API", description = "사용자가 알림목록에서 알림을 삭제하는 API 입니다. ")
    @DeleteMapping("/notification/delete/{id}")
    public ApiResponse deleteNotification(@PathVariable("id") String id) {

        boolean deleted = notificationService.deleteNotification(id);

        if (deleted) {
            return ApiResponse.of(NOTIFICATION_DELETED, null);
        } else {
            return ApiResponse.onFailure(NOTIFICATION_NOT_SENDED.getCode(), NOTIFICATION_NOT_SENDED.getMessage(), null);
        }
    }
}
