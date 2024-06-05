package com.dto.way.notification.domain.repository;

import com.dto.way.notification.domain.entity.Notification;
import lombok.NonNull;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends MongoRepository<Notification, String> {

    @Override
    void deleteById(@NonNull String id);
  
    List<Notification> findByMemberIdOrderByCreatedAtDesc(Long memberId);
}
