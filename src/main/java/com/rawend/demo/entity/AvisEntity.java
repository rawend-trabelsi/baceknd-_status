package com.rawend.demo.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "avis")
@Getter
@Setter
public class AvisEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonBackReference 
    @ManyToOne
    @JoinColumn(name = "service_id", nullable = false) // Assurez-vous que le service est bien présent
    private ServiceEntity service;

    private Integer etoile; // Note entre 1 et 5
    private String commentaire;
    private String email; // L'email de l'utilisateur qui a laissé l'avis

    // Nouveau champ titreService pour stocker le titre directement dans la base
    private String titreService;

    // Méthode pour obtenir le titre du service (si nécessaire)
    public String getTitreService() {
        if (this.service != null) {
            return this.service.getTitre();
        }
        return null; // Si service est nul, on retourne null
    }
}



