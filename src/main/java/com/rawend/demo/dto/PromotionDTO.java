package com.rawend.demo.dto;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import com.rawend.demo.entity.PromotionEntity;
import com.rawend.demo.entity.ServiceEntity;

public class PromotionDTO {
    private Long id; // Ajout de l'ID
    private boolean actif;
    private String typeReduction;
    private double valeurReduction;
    private Date dateDebut;
    private Date dateFin;
 
    private List<ServiceDTO> servicesDTO; // Services pour GET (DTO)
    private String codePromo;

    public PromotionDTO() {}

    // Constructeur pour mapper PromotionEntity vers PromotionDTO
    public PromotionDTO(PromotionEntity promotionEntity) {
        this.id = promotionEntity.getId(); // Ajout de l'ID
        this.actif = promotionEntity.getActif();
        this.typeReduction = promotionEntity.getTypeReduction().name();
        this.valeurReduction = promotionEntity.getValeurReduction();
        this.dateDebut = promotionEntity.getDateDebut();
        this.dateFin = promotionEntity.getDateFin();
        this.codePromo = promotionEntity.getCodePromo();

        // Remplir la liste servicesDTO pour la réponse GET
        this.servicesDTO = promotionEntity.getServices().stream()
            .map(service -> new ServiceDTO(service.getId(), service.getTitre())) // Extraire ID et titre des services
            .collect(Collectors.toList());
    }

 

    public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public boolean isActif() {
		return actif;
	}

	public void setActif(boolean actif) {
		this.actif = actif;
	}

	public String getTypeReduction() {
		return typeReduction;
	}

	public void setTypeReduction(String typeReduction) {
		this.typeReduction = typeReduction;
	}

	public double getValeurReduction() {
		return valeurReduction;
	}

	public void setValeurReduction(double valeurReduction) {
		this.valeurReduction = valeurReduction;
	}

	public Date getDateDebut() {
		return dateDebut;
	}

	public void setDateDebut(Date dateDebut) {
		this.dateDebut = dateDebut;
	}

	public Date getDateFin() {
		return dateFin;
	}

	public void setDateFin(Date dateFin) {
		this.dateFin = dateFin;
	}

	public String getCodePromo() {
		return codePromo;
	}

	public void setCodePromo(String codePromo) {
		this.codePromo = codePromo;
	}

	public List<ServiceDTO> getServicesDTO() {
        return servicesDTO;
    }

    public void setServicesDTO(List<ServiceDTO> servicesDTO) {
        this.servicesDTO = servicesDTO;
    }

  
    // Autres getters et setters pour le reste des propriétés
}
