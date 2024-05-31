package com.dto.way.message;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationMessage {

    private Long targetMemberId;
    private String message;
    private String targetMemberNickName;
    private LocalDateTime createdAt;

}