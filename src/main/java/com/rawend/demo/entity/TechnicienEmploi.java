package com.rawend.demo.entity;

import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.time.LocalTime;

@Entity
@Table(name = "technicien_emplois")
@Getter
@Setter
public class TechnicienEmploi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JourRepos jourRepos;

    @Column(nullable = false)
    private LocalTime heureDebut = LocalTime.of(8, 0); // Par défaut 08:00

    @Column(nullable = false)
    private LocalTime heureFin = LocalTime.of(22, 0); // Par défaut 22:00


    
    @Column(nullable = true)
    private String email;

    @Column(nullable = true)
    private String username;

    @Column(nullable = true)
    private String phone;
}