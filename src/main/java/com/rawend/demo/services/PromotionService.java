package com.rawend.demo.services;

import com.rawend.demo.entity.PromotionEntity;
import com.rawend.demo.entity.ServiceEntity;
import com.rawend.demo.entity.TypeReduction;
import com.rawend.demo.Repository.PromotionRepository;
import com.rawend.demo.Repository.ServiceRepository;
import com.rawend.demo.services.PromotionNotFoundException;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Date;

@Service
public class PromotionService {
	   @Autowired
	    private EntityManager entityManager;

    @Autowired
    private PromotionRepository promotionRepository;

    @Autowired
    private ServiceService serviceService;
    @Autowired
    private ServiceRepository serviceRepository;
    public List<PromotionEntity> obtenirToutesLesPromotions() {
        return promotionRepository.findAllWithServices();
    }
    
    public void mettreAJourPromotion(PromotionEntity promotion) {
        promotionRepository.save(promotion); 
    }
    public long countPromotions() {
        return promotionRepository.count();
    }

    public List<PromotionEntity> obtenirPromotionsParService(Long serviceId) {
        return promotionRepository.findByServices_Id(serviceId);
    }
    public PromotionEntity creerPromotion(PromotionEntity promotionEntity) {
        return promotionRepository.save(promotionEntity);
    }

    @Transactional
    public void supprimerPromotion(PromotionEntity promotion) {
        promotionRepository.delete(promotion);
    }

 // Dans PromotionService
    public void supprimerAssociationPromotionService(Long promotionId, Long serviceId) {
        // Récupérer la promotion par ID
        PromotionEntity promotion = promotionRepository.findById(promotionId)
            .orElseThrow(() -> new RuntimeException("Promotion not found"));

        // Trouver le service par ID
        ServiceEntity service = serviceRepository.findById(serviceId)
            .orElseThrow(() -> new RuntimeException("Service not found"));

        // Dissocier le service de la promotion
        promotion.getServices().remove(service);
        promotionRepository.save(promotion); // Sauvegarder la promotion mise à jour

        // Vous pouvez aussi mettre à jour le service pour enlever la référence à la promotion
        service.setPromotion(null);
        serviceRepository.save(service); // Sauvegarder le service mis à jour
    }

    public boolean estServiceEligible(Long promotionId, Long serviceId) {
        Optional<ServiceEntity> serviceOpt = serviceRepository.findById(serviceId);
        
        if (serviceOpt.isPresent()) {
            ServiceEntity service = serviceOpt.get();
            return service.getPromotion() != null && service.getPromotion().getId().equals(promotionId);
        }
        
        return false;
    }



    public boolean existeDansTableIntermediaire(Long promotionId) {
        Query query = entityManager.createNativeQuery("SELECT COUNT(*) FROM promotion_service WHERE promotion_id = :promotionId");
        query.setParameter("promotionId", promotionId);
        Number count = (Number) query.getSingleResult();
        return count.intValue() > 0;
    }
  

    public List<PromotionEntity> listerPromotions() {
        return promotionRepository.findAll();
    }
    public PromotionEntity mettreAJourPromotion(Long promotionId, PromotionEntity promotionEntity) {
        Optional<PromotionEntity> promotionOptional = promotionRepository.findById(promotionId);
        if (promotionOptional.isPresent()) {
            PromotionEntity promoExistante = promotionOptional.get();
            promoExistante.setCodePromo(promotionEntity.getCodePromo());
            promoExistante.setValeurReduction(promotionEntity.getValeurReduction());
            promoExistante.setActif(promotionEntity.getActif());
            promoExistante.setDateDebut(promotionEntity.getDateDebut());
            promoExistante.setDateFin(promotionEntity.getDateFin());
            promoExistante.setTypeReduction(promotionEntity.getTypeReduction());
            return promotionRepository.save(promoExistante);
        } else {
            throw new PromotionNotFoundException("Promotion non trouvée");
        }
    }

 
    public PromotionEntity obtenirPromotion(Long promotionId) {
        return promotionRepository.findById(promotionId)
                .orElseThrow(() -> new PromotionNotFoundException("Promotion non trouvée"));
    }

 
    public PromotionEntity findActivePromotion() {
        LocalDate today = LocalDate.now();
        return promotionRepository.findAll().stream()
                .filter(promo -> promo.getActif() && 
                                 !convertToLocalDate(promo.getDateDebut()).isAfter(today) && 
                                 !convertToLocalDate(promo.getDateFin()).isBefore(today))
                .findFirst()
                .orElse(null); 
    }
    public double calculerPrixAvecReduction(ServiceEntity service, PromotionEntity promo) {
        double prixInitial = service.getPrix();

        if (service.getPromotion() != null) {
            promo = service.getPromotion();  
        }

        if (promo != null) {
            if (promo.getTypeReduction() == TypeReduction.POURCENTAGE) {
                return prixInitial - (prixInitial * promo.getValeurReduction() / 100);
            }

            if (promo.getTypeReduction() == TypeReduction.MONTANT_FIXE) {
                return prixInitial - promo.getValeurReduction();
            }
        }

        return prixInitial;
    }
   

    public PromotionEntity trouverParCode(String codePromo) {
        return promotionRepository.findByCodePromo(codePromo)
                .filter(promo -> promo.getActif() &&
                        !convertToLocalDate(promo.getDateDebut()).isAfter(LocalDate.now()) &&
                        !convertToLocalDate(promo.getDateFin()).isBefore(LocalDate.now()))
                .orElse(null);
    }


    private LocalDate convertToLocalDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

   
    public void supprimerPromotion(Long promotionId) {
        promotionRepository.deleteById(promotionId);
    }
  
    public PromotionEntity obtenirPromotionParId(Long promotionId) {
        return promotionRepository.findById(promotionId).orElse(null);
    }
    public PromotionEntity trouverPromoActive() {
     
        List<PromotionEntity> promotionsActives = promotionRepository.findAll().stream()
            .filter(promo -> promo.getActif() && convertToLocalDate(promo.getDateDebut()).isBefore(LocalDate.now()) 
                             && convertToLocalDate(promo.getDateFin()).isAfter(LocalDate.now()))
            .collect(Collectors.toList());

        if (promotionsActives.isEmpty()) {
            return null;  
        }

      
        return promotionsActives.get(0);  
    }
   
    public List<ServiceEntity> obtenirServicesAvecPromotion(Long promotionId) {
        return serviceRepository.findByPromotionId(promotionId); 
    }

   
    
    public List<ServiceEntity> obtenirTousLesServices() {
      
        return serviceRepository.findAll();
    }
}