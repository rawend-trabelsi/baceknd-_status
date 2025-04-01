package com.rawend.demo.dto;

import com.rawend.demo.entity.ModePaiement;
import java.time.LocalDateTime;

public record ReservationRequest(
    Long id,                  // ID de la réservation
    Long userId,              // ID de l'utilisateur
    Long serviceId,           // ID du service
    Double prix, 
    String codePromo ,// Prix final du service
    ModePaiement modePaiement,// Mode de paiement (enligne, espèce, etc.)
    String localisation,      // Coordonnées GPS sous forme "latitude,longitude"
    LocalDateTime dateReservation, // Date et heure de la réservation
    LocalDateTime dateCreation,  // Date de création de la réservation

    String titreService,      // Titre du service
    String email,             // Email de l'utilisateur
    String phone              // Téléphone de l'utilisateur
) {}
