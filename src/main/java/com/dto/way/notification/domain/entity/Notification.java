package com.dto.way.notification.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.time.Instant;
import java.time.LocalDateTime;

@Document(collection = "notification")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @MongoId
    private String id;
    private Long memberId;
    private String nickname;
    private String message;
    private LocalDateTime createdAt;
    private boolean isRead;
    @Indexed(name = "expireAt", expireAfterSeconds = 0)
    private Instant expireAt;
}
