package com.rawend.demo.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;


import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import com.rawend.demo.entity.Notification;
import com.rawend.demo.services.NotificationService;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationService notificationService;
    private final SimpMessagingTemplate messagingTemplate;

    public NotificationController(NotificationService notificationService, SimpMessagingTemplate messagingTemplate) {
        this.notificationService = notificationService;
        this.messagingTemplate = messagingTemplate;
    }
     @GetMapping("/{email}/unread/count")
    public ResponseEntity<Long> getUnreadNotificationsCount(@PathVariable String email) {
        // Calculer le nombre de notifications non lues pour l'utilisateur
        long unreadCount = notificationService.getUnreadNotificationsCountByEmail(email);
        
        // Retourner le comptage dans la réponse
        return ResponseEntity.ok(unreadCount);
    }



    @PostMapping("/send/{emailTechnicien}")
    public void sendNotification(@PathVariable String emailTechnicien, @RequestBody String message) {
        Notification notification = notificationService.createNotification(emailTechnicien, message);
        messagingTemplate.convertAndSend("/topic/notifications/" + emailTechnicien, notification);
    }
    @PostMapping("/markAsRead/{notificationId}")
    public ResponseEntity<Void> markNotificationAsRead(@PathVariable Long notificationId) {
        notificationService.markAsRead(notificationId);
        return ResponseEntity.ok().build();
    }
    @GetMapping("/{email}")
    public ResponseEntity<List<Notification>> getNotifications(@PathVariable String email) {
        // Récupérer les notifications par email
        List<Notification> notifications = notificationService.getNotificationsByEmail(email);
        
        // Trier par ID décroissant (le plus récent en premier)
        notifications.sort((n1, n2) -> Long.compare(n2.getId(), n1.getId()));

        // Mettre à jour l'état 'isRead' des notifications à true
        notifications.forEach(notification -> notification.setRead(true));

        // Sauvegarder les notifications mises à jour
        notificationService.saveAll(notifications);

        // Retourner les notifications mises à jour
        return ResponseEntity.ok(notifications);
    }

}
