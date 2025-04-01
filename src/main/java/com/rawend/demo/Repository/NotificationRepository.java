package com.rawend.demo.Repository;


import org.springframework.data.jpa.repository.JpaRepository;

import com.rawend.demo.entity.Notification;

import java.util.List;


public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
   
    List<Notification> findByUserEmailOrderByDateEnvoiDesc(String userEmail);
    List<Notification> findByUserEmail(String userEmail);
    Long countByUserEmailAndIsReadFalse(String userEmail);
    List<Notification> findByUserEmailAndIsReadTrue(String userEmail);
  
   
}
