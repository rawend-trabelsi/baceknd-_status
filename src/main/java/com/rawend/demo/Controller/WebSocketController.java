package com.rawend.demo.Controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {

    @MessageMapping("/sendNotification") // Endpoint pour recevoir les messages
    @SendTo("/topic/notifications") // Endpoint pour envoyer les messages aux clients
    public String sendNotification(String message) {
        return message; // Renvoyer le message à tous les clients abonnés
    }
}