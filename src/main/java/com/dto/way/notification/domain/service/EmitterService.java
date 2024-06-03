package com.dto.way.notification.domain.service;

import com.dto.way.message.NotificationMessage;
import com.dto.way.notification.domain.entity.Notification;
import com.dto.way.notification.domain.repository.EmitterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmitterService {

    public static final Long DEFAULT_TIMEOUT = 3600L * 1000;

    private final NotificationService notificationService;
    private final EmitterRepository emitterRepository;

    @KafkaListener(topics = {"follow", "like", "comment", "reply"}, groupId = "group_1")
    public void listen(NotificationMessage notificationMessage) {
        log.info("Received Kafka message: {}", notificationMessage);

        Notification notification = Notification.builder()
                .memberId(notificationMessage.getTargetMemberId())
                .nickname(notificationMessage.getTargetMemberNickname())
                .message(notificationMessage.getMessage())
                .createdAt(LocalDateTime.now())
                .expireAt(Instant.now().plus(1, ChronoUnit.DAYS))
                .build();

        log.info("Built notification: {}", notification);
        notificationService.insertNotification(notification);
        log.info("Notification inserted into DB");

        Map<String, SseEmitter> sseEmitters = emitterRepository.findAllEmittersStartWithByMemberId(notificationMessage.getTargetMemberId());
        log.info("Found SSE Emitters: {}", sseEmitters);

        sseEmitters.forEach((key, emitter) -> {
            emitterRepository.saveEventCache(key, notification);
            log.info("Event cached with key: {}", key);
            sendToClient(emitter, key, notification);
        });
    }

    private void sendToClient(SseEmitter emitter, String emitterId, Object data) {
        try {
            emitter.send(SseEmitter.event().id(emitterId).data(data));
            log.info("Sent message to client. emitterId = {}, message = {}", emitterId, data);
        } catch (IOException e) {
            emitterRepository.deleteByEmitterId(emitterId);
            log.error("Error sending message to client: {}", e.getMessage());
        }
    }

    public SseEmitter addEmitter(Long memberId, String lastEventId) {
        String emitterId = memberId + "_" + System.currentTimeMillis();
        SseEmitter emitter = emitterRepository.save(emitterId, new SseEmitter(DEFAULT_TIMEOUT));
        log.info("Emitter connected: emitterId = {}", emitterId);

        emitter.onCompletion(() -> {
            log.info("Emitter completed: emitterId = {}", emitterId);
            emitterRepository.deleteByEmitterId(emitterId);
        });

        emitter.onTimeout(() -> {
            log.info("Emitter timeout: emitterId = {}", emitterId);
            emitterRepository.deleteByEmitterId(emitterId);
        });

        sendToClient(emitter, emitterId, "connected!!");

        if (!lastEventId.isEmpty()) {
            Map<String, Object> events = emitterRepository.findAllEventCacheStartWithByMemberId(memberId);
            events.entrySet().stream()
                    .filter(entry -> lastEventId.compareTo(entry.getKey()) < 0)
                    .forEach(entry -> sendToClient(emitter, entry.getKey(), entry.getValue()));
        }

        return emitter;
    }

    @Scheduled(fixedRate = 180000)
    public void sendHeartBeat() {
        Map<String, SseEmitter> allEmitters = emitterRepository.findAllEmitters();
        allEmitters.forEach((key, emitter) -> {
            try {
                emitter.send(SseEmitter.event().id(key).name("heartbeat").data(""));
                log.info("Heartbeat message sent: key = {}", key);
            } catch (IOException e) {
                emitterRepository.deleteByEmitterId(key);
                log.error("Error sending heartbeat message: {}", e.getMessage());
            }
        });
    }
}
