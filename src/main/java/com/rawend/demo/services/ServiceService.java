package com.rawend.demo.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.stereotype.Service;

import com.rawend.demo.Repository.PromotionRepository;
import com.rawend.demo.Repository.ServiceRepository;
import com.rawend.demo.entity.PromotionEntity;
import com.rawend.demo.entity.ServiceEntity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ServiceService {

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private PromotionRepository  promotionRepository;
  
    public long countServices() {
        return serviceRepository.count();
    }

    public List<ServiceEntity> obtenirServicesParPromotion(Long promotionId) {
        // Récupérer la promotion par ID
        PromotionEntity promotion = promotionRepository.findById(promotionId)
            .orElseThrow(() -> new RuntimeException("Promotion not found"));

        // Retourner la liste des services associés à cette promotion
        return promotion.getServices(); // Assurez-vous que la relation est correctement configurée
    }public List<ServiceEntity> getServicesByPromotionId(Long promotionId) {
        return serviceRepository.findByPromotionId(promotionId);
    }

  
    public List<Map<String, Object>> obtenirServicesAvecPromotions() {
        List<ServiceEntity> services = serviceRepository.findAllWithPromotions();
        List<Map<String, Object>> result = new ArrayList<>();

        for (ServiceEntity service : services) {
            PromotionEntity promo = service.getPromotion();
            if (promo != null && promo.getActif()) {
                double originalPrice = service.getPrix();
                double discountedPrice = calculerPrixAvecReduction(service, promo);

                Map<String, Object> serviceMap = new HashMap<>();
                serviceMap.put("serviceId", service.getId());
                serviceMap.put("serviceName", service.getTitre());
                serviceMap.put("originalPrice", originalPrice);
                serviceMap.put("discountedPrice", discountedPrice);
                serviceMap.put("promotion", promo.getTauxReduction() + "% off");

                result.add(serviceMap);
            }
        }
        return result;
    }
  
    public List<ServiceEntity> getAllServices() {
        return serviceRepository.findAll();
    }

   
    public Optional<ServiceEntity> getServiceById(Long id) {
        return serviceRepository.findById(id);
    }

  
    public ServiceEntity saveService(ServiceEntity service) {
        return serviceRepository.save(service);
    }

  
    public void deleteService(Long id) {
        serviceRepository.deleteById(id);
    }

    public ServiceEntity findById(Long id) {
        
        return serviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Service not found with id: " + id));
    }
    public List<ServiceEntity> obtenirTousLesServicesAvecPromotions() {
        return serviceRepository.findAll().stream()
            .filter(service -> service.getPromotion() != null)
            .collect(Collectors.toList());
    }


    // Récupérer un service par ID
    public ServiceEntity obtenirServiceParId(Long serviceId) {
        return serviceRepository.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("Service non trouvé avec l'ID: " + serviceId));
    }
 // Méthode pour calculer le prix avec réduction
    private double calculerPrixAvecReduction(ServiceEntity service, PromotionEntity promo) {
        double prixInitial = service.getPrix();

        // Vérification si la promotion est un pourcentage
        if ("POURCENTAGE".equals(promo.getTypeReduction())) {
            // Calcul du prix après réduction pourcentage
            return prixInitial - (prixInitial * promo.getValeurReduction() / 100);
        }

        // Vérification si la promotion est un montant fixe
        if ("MONTANT".equals(promo.getTypeReduction())) {
            // Calcul du prix après réduction montant fixe
            return prixInitial - promo.getValeurReduction();
        }

        // Si le type de réduction ne correspond pas, retourner le prix initial
        return prixInitial;
    }
 // Convertir java.util.Date en java.time.LocalDate
    private LocalDate convertToLocalDate(Date date) {
        if (date == null) {
            return null; // Retourner null si la date est null
        }
        return date.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
    }
    public ServiceEntity mettreAJourService(ServiceEntity service) {
        return serviceRepository.save(service);  // Mise à jour du service avec le prix réduit
    }

    
}
