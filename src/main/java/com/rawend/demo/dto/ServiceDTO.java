package com.rawend.demo.dto;

public class ServiceDTO {
    private Long id;
    private String titre;

    public ServiceDTO(Long id, String titre) {
        this.id = id;
        this.titre = titre;
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }
}