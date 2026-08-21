package com.f2c.marketplace.controller;

import com.f2c.marketplace.model.Notification;
import com.f2c.marketplace.model.User;
import com.f2c.marketplace.repository.NotificationRepository;
import com.f2c.marketplace.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<?> getNotifications(Authentication authentication) {
        try {
            User user = userService.findByEmail(authentication.getName());
            List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
            return ResponseEntity.ok(notifications);
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PutMapping("/mark-read/{notifId}")
    public ResponseEntity<?> markAsRead(@PathVariable Long notifId, Authentication authentication) {
        try {
            User user = userService.findByEmail(authentication.getName());
            Notification notification = notificationRepository.findById(notifId)
                    .orElseThrow(() -> new RuntimeException("Notification not found"));

            if (!notification.getUser().getId().equals(user.getId())) {
                return ResponseEntity.status(403).body("Unauthorized access");
            }

            notification.setIsRead(true);
            notificationRepository.save(notification);
            return ResponseEntity.ok("Notification marked as read");
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }
}
