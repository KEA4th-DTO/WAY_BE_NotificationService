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
import java.time.LocalDateTime;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmitterService {

    public static final Long DEFAULT_TIMEOUT = 3600L * 1000;

    private final NotificationService notificationService;
    private final EmitterRepository emitterRepository;

    @KafkaListener(topics = "follow", groupId = "group_1")
    public void listen(NotificationMessage notificationMessage) {
        String nickname = notificationMessage.getSendedMember();

        Notification notification = Notification.builder()
                .nickname(nickname)
                .message(notificationMessage.getMessage())
                .createdAt(LocalDateTime.now())
                .build();

        notificationService.insertNotification(notification);

        Map<String, SseEmitter> sseEmitters = emitterRepository.findAllEmittersStartWithByNickname(nickname);
        sseEmitters.forEach(
                (key, emitter) -> {
                    emitterRepository.saveEventCache(key, notification);
                    sendToClient(emitter, key, notification);
                }
        );
    }

    private void sendToClient(SseEmitter emitter, String emitterId, Object data) {
        try {
            emitter.send(SseEmitter.event()
                    .id(emitterId)
                    .data(data));
            log.info("Kafka로 부터 전달 받은 메세지 전송. emitterId = {}, message = {}", emitterId, data);
        } catch (IOException e) {
            emitterRepository.deleteByEmitterId(emitterId);
            log.error("메세지 전송 에러 = {}", e);
        }
    }

    public SseEmitter addEmitter(String nickname, String lastEventId) {
        String emitterId = nickname + "_" + System.currentTimeMillis();
        SseEmitter emitter = emitterRepository.save(emitterId, new SseEmitter(DEFAULT_TIMEOUT));
        log.info("emitterId = {} 사용자-emitter 연결!", emitterId);

        emitter.onCompletion(() -> {
            log.info("onCompletion callback");
            emitterRepository.deleteByEmitterId(emitterId);
        });

        emitter.onTimeout(() -> {
            log.info("onTimeout callback");
            emitterRepository.deleteByEmitterId(emitterId);
        });

        sendToClient(emitter, emitterId, "connected!!");

        if (!lastEventId.isEmpty()) {
            Map<String, Object> events = emitterRepository.findAllEventCacheStartWithByNickname(nickname);
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
                log.info("하트비트 메세지 전송");
            } catch (IOException e) {
                emitterRepository.deleteByEmitterId(key);
                log.info("하트비트 메세지 전송 실패 = {}", e.getMessage());
            }
        });
    }
}
