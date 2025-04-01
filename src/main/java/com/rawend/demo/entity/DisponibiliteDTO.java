package com.rawend.demo.entity;

import java.time.LocalDateTime;

public class DisponibiliteDTO {
    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;

    // Getters et setters
    public LocalDateTime getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(LocalDateTime dateDebut) {
        this.dateDebut = dateDebut;
    }

    public LocalDateTime getDateFin() {
        return dateFin;
    }

    public void setDateFin(LocalDateTime dateFin) {
        this.dateFin = dateFin;
    }
}

