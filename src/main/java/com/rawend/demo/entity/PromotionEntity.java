package com.rawend.demo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
@Entity
@Table(name = "promotion")
@Getter
@Setter
public class PromotionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String codePromo; 
    private Double valeurReduction;  
    private Boolean actif; 
    private Date dateDebut;  
    private Date dateFin;  
  
    @Enumerated(EnumType.STRING)
    private TypeReduction typeReduction;
    @PrePersist
    @PreUpdate
    public void verifierExpiration() {
        LocalDate today = LocalDate.now();
        if (this.dateFin != null && convertToLocalDate(this.dateFin).isBefore(today)) {
            this.actif = false;
        }
    }

    private LocalDate convertToLocalDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }
    @ManyToMany
    @JoinTable(
        name = "promotion_service",
        joinColumns = @JoinColumn(name = "promotion_id"),
        inverseJoinColumns = @JoinColumn(name = "service_id")
    )
    private List<ServiceEntity> services= new ArrayList<>();
    public Double getTauxReduction() {
        if (this.typeReduction == TypeReduction.POURCENTAGE) {
            return this.valeurReduction;  
        } else if (this.typeReduction == TypeReduction.MONTANT_FIXE) {
            return this.valeurReduction; 
    }
		return valeurReduction;
    }}
