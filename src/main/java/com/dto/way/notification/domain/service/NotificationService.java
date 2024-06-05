package com.dto.way.notification.domain.service;

import com.dto.way.notification.domain.entity.Notification;
import com.dto.way.notification.domain.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public void insertNotification(Notification notification) {
        notificationRepository.save(notification);
    }

    public List<Notification> selectNotificationList(Long memberId) {
        return notificationRepository.findByMemberId(memberId);
    }

    public boolean deleteNotification(String id) {
        if (id.isEmpty()) {
            return false;
        } else {
            notificationRepository.deleteById(id);
            return true;
        }
    }
}
