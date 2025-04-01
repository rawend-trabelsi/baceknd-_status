package com.rawend.demo.Controller;

import com.rawend.demo.Repository.AvisRepository;
import com.rawend.demo.Repository.ServiceRepository;
import com.rawend.demo.entity.AvisEntity;
import com.rawend.demo.entity.ServiceEntity;
import com.rawend.demo.services.AvisService;
// Assurez-vous d'importer le repository
import com.rawend.demo.services.JWTService;  // Assurez-vous d'importer JWTService
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/avis")
public class AvisController {

    @Autowired
    private AvisService avisService;

    @Autowired
    private ServiceRepository serviceRepository;  
    
    @Autowired
    private AvisRepository avisRepository;

    @Autowired
    private JWTService jwtService;  

    @PostMapping("/ajouter")
    public ResponseEntity<?> ajouterAvis(@RequestBody AvisEntity avis, @RequestHeader("Authorization") String token) {
      
        if (avis.getService() == null || avis.getService().getId() == null) {
            return ResponseEntity.badRequest().body("L'ID du service est requis.");
        }

      
        Optional<ServiceEntity> serviceOpt = serviceRepository.findById(avis.getService().getId());
        if (serviceOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Le service avec l'ID " + avis.getService().getId() + " n'a pas été trouvé.");
        }

        
        ServiceEntity service = serviceOpt.get();
        avis.setService(service);

      
        String email = jwtService.extractUsername(token.replace("Bearer ", ""));
        avis.setEmail(email);

       
        avis.setTitreService(service.getTitre());

   
        AvisEntity savedAvis = avisService.ajouterAvis(avis, token);

        if (savedAvis == null) {
            return ResponseEntity.badRequest().body("Erreur lors de l'ajout de l'avis.");
        }


        return ResponseEntity.ok(
                Map.of(
                        "id", savedAvis.getId(),
                        "idservice", savedAvis.getService().getId(),
                        "titreService", savedAvis.getTitreService(),
                        "commentaire", savedAvis.getCommentaire(),
                        "etoile", savedAvis.getEtoile(),
                        "email", savedAvis.getEmail()
                )
        );
    }
    @GetMapping("/email")
    public ResponseEntity<?> getEmail(@RequestHeader("Authorization") String token) {
        try {
            // Extraire l'email du token JWT
            String email = jwtService.extractUsername(token.replace("Bearer ", ""));
            return ResponseEntity.ok(email); // Retourner l'email au frontend
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token invalide ou expiré");
        }
    }
    @GetMapping("/service/{serviceId}/titre")
    public ResponseEntity<?> getTitreService(@PathVariable Long serviceId) {
        Optional<ServiceEntity> serviceOpt = serviceRepository.findById(serviceId);
        if (serviceOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Service non trouvé");
        }
        ServiceEntity service = serviceOpt.get();
        return ResponseEntity.ok(service.getTitre()); // Retourner le titre du service
    }

    @GetMapping("/service/{idservice}")
    public ResponseEntity<List<Map<String, Object>>> getAvisByService(@PathVariable("idservice") Long idService) {
        // Récupérer les avis associés à l'ID du service
        List<AvisEntity> avisList = avisRepository.findByService_Id(idService);

        // Si aucun avis n'est trouvé pour le service
        if (avisList.isEmpty()) {
            return ResponseEntity.noContent().build(); // Retourner 204 No Content si aucun avis n'est trouvé
        }

        // Créer une liste de réponse avec les champs spécifiés
        List<Map<String, Object>> response = avisList.stream()
                .map(avis -> {
                    // Utilisation de HashMap au lieu de Map.of() pour résoudre le problème de type
                    Map<String, Object> avisMap = new HashMap<>();
                    avisMap.put("idavis", avis.getId());
                    avisMap.put("idservice", avis.getService().getId()); // L'id du service
                    avisMap.put("titreService", avis.getService().getTitre());
                    avisMap.put("email", avis.getEmail());
             
                    avisMap.put("commentaire", avis.getCommentaire());
                    avisMap.put("etoile", avis.getEtoile());
                    return avisMap;
                })
                .collect(Collectors.toList());

       
        return ResponseEntity.ok(response);
    }
    @GetMapping("/all")
    public ResponseEntity<List<Map<String, Object>>> getTousLesAvis() {
    
        List<AvisEntity> avisList = avisRepository.findAll();

     
        if (avisList.isEmpty()) {
            return ResponseEntity.noContent().build(); 
        }

        List<Map<String, Object>> response = avisList.stream()
                .map(avis -> {
                   
                    Map<String, Object> avisMap = new HashMap<>();
                    avisMap.put("idavis", avis.getId());
                    avisMap.put("idservice", avis.getService().getId());
                    avisMap.put("email", avis.getEmail());
                    avisMap.put("etoile", avis.getEtoile());
                    avisMap.put("commentaire", avis.getCommentaire());
                    avisMap.put("titreService", avis.getService().getTitre());
;

                    return avisMap;
                })
                .collect(Collectors.toList());

       
        return ResponseEntity.ok(response);
    }
    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> getAvisCount() {
        long count = avisRepository.count(); 
        Map<String, Long> response = new HashMap<>();
        response.put("totalAvis", count);
        return ResponseEntity.ok(response);
    }
}

