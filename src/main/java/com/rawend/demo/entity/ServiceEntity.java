package com.rawend.demo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.fasterxml.jackson.annotation.JsonManagedReference;
@Entity
@Table(name = "service")
@Getter
@Setter
public class ServiceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titre;
    private String description;
    private Double prix;
    private String duree;
    private byte[] image;
    private String imageName;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name = "promotion_id", nullable = true)
    @OnDelete(action = OnDeleteAction.SET_NULL) 
    private PromotionEntity promotion;
    
  
    @JsonManagedReference  // Annotation pour gérer la sérialisation
    @OneToMany(mappedBy = "service", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AvisEntity> avisList;



    @ManyToMany(mappedBy = "services")
    private List<PromotionEntity> promotions; // Liste des promotions associées

    // Calcul automatique du prix après promotion
    public Double getPrixApresReduction() {
        if (promotion != null && promotion.getTauxReduction() != null) {
            return prix - (prix * promotion.getTauxReduction() / 100);
        }
        return prix;
    }

    public Double getTauxPromotion() {
        return (promotion != null) ? promotion.getTauxReduction() : 0.0;
    }
}