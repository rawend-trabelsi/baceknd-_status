package com.rawend.demo.Controller;


import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.rawend.demo.entity.PromotionEntity;
import com.rawend.demo.entity.ServiceEntity;
import com.rawend.demo.entity.TypeReduction;
import com.rawend.demo.entity.User;
import com.rawend.demo.services.JWTService;
import com.rawend.demo.services.ServiceService;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/services")
public class ServiceController {

    @Autowired
    private ServiceService serviceService;
    @Autowired
    private JWTService jwtService;  
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @GetMapping("/admin")
    public ResponseEntity<List<Map<String, Object>>> getAllServicesForAdmin() {
        
        List<Map<String, Object>> services = serviceService.getAllServices().stream()
            .map(service -> {
                Map<String, Object> filteredService = new HashMap<>();
                filteredService.put("id", service.getId());
                filteredService.put("titre", service.getTitre());
                filteredService.put("description", service.getDescription());
                filteredService.put("prix", service.getPrix());
                filteredService.put("duree", service.getDuree());
                filteredService.put("image", service.getImage());
                filteredService.put("imageName", service.getImageName());
                
            

                return filteredService;
            })
            .collect(Collectors.toList());

        return ResponseEntity.ok(services);
    }
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllServices() {
        List<Map<String, Object>> services = serviceService.getAllServices().stream()
            .map(service -> {
                Map<String, Object> filteredService = new HashMap<>();
                filteredService.put("id", service.getId());
                filteredService.put("titre", service.getTitre());
                filteredService.put("description", service.getDescription());
                filteredService.put("prix", service.getPrix());
                filteredService.put("duree", service.getDuree());
                filteredService.put("image", service.getImage());
                filteredService.put("imageName", service.getImageName());

                PromotionEntity promo = service.getPromotion();
                if (promo != null) {
                    // Vérifier si un code promo est présent (supposons qu'il existe une méthode getCodePromo())
                    if (promo.getCodePromo() == null || promo.getCodePromo().isEmpty()) {
                        LocalDate today = LocalDate.now();
                        LocalDateTime startOfDay = today.atStartOfDay(); // Début de la journée
                        LocalDateTime endOfDay = today.atTime(23, 59, 59); // Fin de la journée

                        LocalDateTime promoStart = convertToLocalDateTime(promo.getDateDebut()).withHour(0).withMinute(0).withSecond(0); // Début de la promotion
                        LocalDateTime promoEnd = convertToLocalDateTime(promo.getDateFin()).withHour(23).withMinute(59).withSecond(59); // Fin de la promotion

                        if (!promoStart.isAfter(endOfDay) && !promoEnd.isBefore(startOfDay)) {
                            // Promotion active, afficher le prix réduit
                            Double discountedPrice = calculerPrixAvecReduction(service, promo);
                            String promotionValue;

                            if (promo.getTypeReduction() == TypeReduction.POURCENTAGE) {
                                promotionValue = "- " + (promo.getValeurReduction() % 1 == 0 
                                    ? String.format("%.0f", promo.getValeurReduction()) 
                                    : String.valueOf(promo.getValeurReduction())) + "% ";
                            } else if (promo.getTypeReduction() == TypeReduction.MONTANT_FIXE) {
                                promotionValue = "- " + (promo.getValeurReduction() % 1 == 0 
                                    ? String.format("%.0f", promo.getValeurReduction()) 
                                    : String.valueOf(promo.getValeurReduction())) + "DT";
                            } else {
                                promotionValue = "Aucune promotion";
                            }

                            filteredService.put("promotion", promotionValue);
                            filteredService.put("discountedPrice", discountedPrice);
                        }
                    }
                }
                return filteredService;
            })
            .collect(Collectors.toList());

        return ResponseEntity.ok(services);
    }

    // Méthode pour convertir une Date en LocalDateTime
    private LocalDateTime convertToLocalDateTime(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }
   

    private double calculerPrixAvecReduction(ServiceEntity service, PromotionEntity promo) {
        double prixInitial = service.getPrix();

        switch (promo.getTypeReduction()) {
            case POURCENTAGE:
                return prixInitial * (1 - promo.getValeurReduction() / 100);
            case MONTANT_FIXE:
                return prixInitial - promo.getValeurReduction();
            default:
                return prixInitial;
        }
    }


    private LocalDate convertToLocalDate(Date date) {
        if (date == null) {
            return null;
        }
        return date.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        
    }
   

  
    @GetMapping("/{id}")
    public ResponseEntity<ServiceEntity> getServiceById(@PathVariable Long id) {
        return serviceService.getServiceById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

   
    @PostMapping
    public ResponseEntity<ServiceEntity> createService(@RequestParam("titre") String titre,
                                                       @RequestParam("description") String description,
                                                       @RequestParam("prix") Double prix,
                                                       @RequestParam("duree") String duree,  // Remplacé "durée" par "duree"
                                                       @RequestParam("image") MultipartFile imageFile) throws IOException {
        
     
        byte[] imageBytes = imageFile.getBytes();
        String imageName = imageFile.getOriginalFilename();

        
        ServiceEntity service = new ServiceEntity();
        service.setTitre(titre);
        service.setDescription(description);
        service.setPrix(prix);
        service.setDuree(duree);  
        service.setImage(imageBytes);
        service.setImageName(imageName);

       
        ServiceEntity savedService = serviceService.saveService(service);

        return ResponseEntity.status(HttpStatus.CREATED).body(savedService);
    }
    @PutMapping("/{id}")
    public ResponseEntity<ServiceEntity> updateService(@PathVariable Long id,
                                                       @RequestParam("titre") String titre,
                                                       @RequestParam("description") String description,
                                                       @RequestParam("prix") Double prix,
                                                       @RequestParam("duree") String duree,
                                                       @RequestParam(value = "image", required = false) MultipartFile imageFile) throws IOException {

   
        ServiceEntity service = serviceService.findById(id);  
        if (service == null) {
            return ResponseEntity.notFound().build(); 
        }

        
        service.setTitre(titre);
        service.setDescription(description);
        service.setPrix(prix);
        service.setDuree(duree);

        
        if (imageFile != null && !imageFile.isEmpty()) {
            byte[] imageBytes = imageFile.getBytes();
            String imageName = imageFile.getOriginalFilename();  
            service.setImage(imageBytes);  
            service.setImageName(imageName);  
        }

     
        ServiceEntity updatedService = serviceService.saveService(service); 
      
        return ResponseEntity.ok(updatedService);
    }

   
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteService(@PathVariable Long id) {
        serviceService.deleteService(id);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/titres")
    public ResponseEntity<List<String>> getAllServiceTitles() {
        List<String> titles = serviceService.getAllServices().stream()
            .map(ServiceEntity::getTitre) 
            .collect(Collectors.toList());

        return ResponseEntity.ok(titles);
    }
    @GetMapping("/count")
    public ResponseEntity<Long> countServices() {
        long count = serviceService.countServices();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/{id}/reserver")
    public ResponseEntity<?> getServiceDetails(@PathVariable Long id, @RequestHeader("Authorization") String token) {
        try {
            // Extraction de l'email depuis le token
            String email = jwtService.extractUsername(token.substring(7)); // Enlever "Bearer " du token

            // Récupération du service par ID
            Optional<ServiceEntity> serviceOpt = serviceService.getServiceById(id);
            if (!serviceOpt.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Service non trouvé");
            }
            ServiceEntity service = serviceOpt.get();

            // Récupération du téléphone de l'utilisateur depuis la base de données
            String phone = getUserPhoneByEmail(email);

            // Calcul du prix avec ou sans réduction
            Double prixFinal = service.getPrix();
            PromotionEntity promo = service.getPromotion();
            if (promo != null) {
                if (promo.getTypeReduction() == TypeReduction.POURCENTAGE) {
                    prixFinal = service.getPrix() * (1 - promo.getValeurReduction() / 100);
                } else if (promo.getTypeReduction() == TypeReduction.MONTANT_FIXE) {
                    prixFinal = service.getPrix() - promo.getValeurReduction();
                }
            }

            // Construction de la réponse
            Map<String, Object> response = new HashMap<>();
            response.put("email", email);
            response.put("phone", phone); // Ajout du téléphone à la réponse
            response.put("titre", service.getTitre());
            response.put("prix", prixFinal); // Toujours afficher le prix final

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors de la récupération du service");
        }
    }

    // Méthode pour récupérer le téléphone de l'utilisateur par son email
    private String getUserPhoneByEmail(String email) {
        // Requête SQL pour récupérer le téléphone de l'utilisateur
        String sql = "SELECT phone FROM users WHERE email = ?";

        // Exécution de la requête SQL
        try {
            return jdbcTemplate.queryForObject(sql, new Object[]{email}, String.class);
        } catch (Exception e) {
            return "Phone not found"; // Retourner une valeur par défaut si l'utilisateur n'est pas trouvé
        }
    }

}

