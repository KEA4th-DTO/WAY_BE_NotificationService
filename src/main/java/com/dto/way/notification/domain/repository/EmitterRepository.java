package com.dto.way.notification.domain.repository;

import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class EmitterRepository {

    private final Map<String, SseEmitter> emiiters = new ConcurrentHashMap<>();
    private final Map<String, Object> eventCache = new ConcurrentHashMap<>();

    public SseEmitter save(String emitterId, SseEmitter sseEmitter) {
        emiiters.put(emitterId, sseEmitter);
        return sseEmitter;
    }

    public void saveEventCache(String emitterId, Object event) {
        eventCache.put(emitterId, event);
    }

    public Map<String, SseEmitter> findAllEmitters() {
        return emiiters;
    }

    public Map<String, SseEmitter> findAllEmittersStartWithByMemberId(Long memberId) {
        return emiiters.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(String.valueOf(memberId)))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public Map<String, Object> findAllEventCacheStartWithByMemberId(Long memberId) {
        return eventCache.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(String.valueOf(memberId)))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public void deleteByEmitterId(String emitterId) {
        emiiters.remove(emitterId);
    }
}
