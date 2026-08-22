package com.f2c.marketplace.controller;

import com.f2c.marketplace.model.Notification;
import com.f2c.marketplace.model.User;
import com.f2c.marketplace.repository.NotificationRepository;
import com.f2c.marketplace.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
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

    @GetMapping("/unread-count")
    public ResponseEntity<?> getUnreadCount(Authentication authentication) {
        try {
            User user = userService.findByEmail(authentication.getName());
            List<Notification> unread = notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(user.getId());
            return ResponseEntity.ok(unread.size());
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Long id, Authentication authentication) {
        try {
            User user = userService.findByEmail(authentication.getName());
            Notification notification = notificationRepository.findById(id)
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

    @PutMapping("/read-all")
    @Transactional
    public ResponseEntity<?> markAllAsRead(Authentication authentication) {
        try {
            User user = userService.findByEmail(authentication.getName());
            List<Notification> unread = notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(user.getId());
            for (Notification n : unread) {
                n.setIsRead(true);
                notificationRepository.save(n);
            }
            return ResponseEntity.ok("All notifications marked as read");
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> deleteNotification(@PathVariable Long id, Authentication authentication) {
        try {
            User user = userService.findByEmail(authentication.getName());
            Notification notification = notificationRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Notification not found"));

            if (!notification.getUser().getId().equals(user.getId())) {
                return ResponseEntity.status(403).body("Unauthorized access");
            }

            notificationRepository.delete(notification);
            return ResponseEntity.ok("Notification deleted successfully");
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }
}
