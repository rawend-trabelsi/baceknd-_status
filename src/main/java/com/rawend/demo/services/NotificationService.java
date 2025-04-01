package com.rawend.demo.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import com.rawend.demo.Repository.NotificationRepository;
import com.rawend.demo.entity.Notification;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;
    @PersistenceContext
    private EntityManager entityManager;
    

    @Autowired
    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Autowired
    private SimpMessagingTemplate messagingTemplate; 
    public void sendNotificationToAdmins(String message) {
      
    	List<?> results = entityManager.createNativeQuery(
    		    "SELECT email FROM users WHERE role = 'ADMIN'"
    		).getResultList();

    		List<String> adminEmails = results.stream()
    		    .map(Object::toString)  // Convertit chaque objet en String
    		    .collect(Collectors.toList());


        // --- Création et envoi de la notification à chaque admin ---
        for (String adminEmail : adminEmails) {
            Notification notification = new Notification(adminEmail, message);
            notificationRepository.save(notification);

            // Envoi de la notification en temps réel via WebSocket
            messagingTemplate.convertAndSend("/topic/notifications/" + adminEmail, notification);
        }
    }

    public Long countUnreadNotifications(String userEmail) {
        return notificationRepository.countByUserEmailAndIsReadFalse(userEmail);
    }

    public Notification createNotification(String userEmail, String message) {
        Notification notification = new Notification(userEmail, message);
        return notificationRepository.save(notification);
    }

    public List<Notification> getNotificationsForUser(String userEmail) {
        return notificationRepository.findByUserEmailOrderByDateEnvoiDesc(userEmail);
    }

    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new RuntimeException("Notification non trouvée"));
        notification.setRead(true);
        notificationRepository.save(notification);
    }
    public List<Notification> getNotificationsByEmail(String email) {
        return notificationRepository.findByUserEmail(email);
    }
    public void saveAll(List<Notification> notifications) {
        notificationRepository.saveAll(notifications);
    }
    public List<Notification> getReadNotificationsByEmail(String email) {
        return notificationRepository.findByUserEmailAndIsReadTrue(email);
    }
    public Long getUnreadNotificationsCountByEmail(String email) {
        // Utiliser le repository pour compter les notifications non lues
        return notificationRepository.countByUserEmailAndIsReadFalse(email);
    }

}
