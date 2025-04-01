package com.rawend.demo.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.rawend.demo.entity.Abonnement;
import com.rawend.demo.services.AbonnementService;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/abonnements")
public class AbonnementController {

    @Autowired
    private AbonnementService abonnementService;

    // Récupérer tous les abonnements
    @GetMapping
    public ResponseEntity<List<Abonnement>> getAllAbonnements() {
        return ResponseEntity.ok(abonnementService.getAllAbonnements());
    }

    
    @GetMapping("/{id}")
    public ResponseEntity<Abonnement> getAbonnementById(@PathVariable Long id) {
        return abonnementService.getAbonnementById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

 
    @PostMapping
    public ResponseEntity<Abonnement> createAbonnement(@RequestParam("titre") String titre,
                                                             @RequestParam("prix") Double prix,
                                                             @RequestParam("image") MultipartFile imageFile) throws IOException {
        
        // Convertir l'image en tableau de bytes
        byte[] imageBytes = imageFile.getBytes();
        String imageName = imageFile.getOriginalFilename();

        // Créer l'abonnement
        Abonnement abonnement = new Abonnement();
        abonnement.setTitre(titre);
        abonnement.setPrix(prix);
        abonnement.setImage(imageBytes);
        abonnement.setImageName(imageName);

        // Sauvegarder l'abonnement dans la base de données
        Abonnement savedAbonnement = abonnementService.saveAbonnement(abonnement);

        return ResponseEntity.status(HttpStatus.CREATED).body(savedAbonnement);
    }

    // Mettre à jour un abonnement
    @PutMapping("/{id}")
    public ResponseEntity<Abonnement> updateAbonnement(@PathVariable Long id,
                                                             @RequestParam("titre") String titre,
                                                             @RequestParam("prix") Double prix,
                                                             @RequestParam(value = "image", required = false) MultipartFile imageFile) throws IOException {

        // Récupérer l'abonnement existant
        Abonnement abonnement = abonnementService.findById(id);  // Vous devez avoir cette méthode pour récupérer l'abonnement par ID
        if (abonnement == null) {
            return ResponseEntity.notFound().build();  // Retourner une réponse 404 si l'abonnement n'est pas trouvé
        }

        // Mise à jour des champs texte
        abonnement.setTitre(titre);
        abonnement.setPrix(prix);

      
        if (imageFile != null && !imageFile.isEmpty()) {
            byte[] imageBytes = imageFile.getBytes();
            String imageName = imageFile.getOriginalFilename();  // Nouveau nom de l'image
            abonnement.setImage(imageBytes);  // Mettre à jour l'image
            abonnement.setImageName(imageName);  // Mettre à jour le nom de l'image
        }

        // Sauvegarder l'abonnement mis à jour
        Abonnement updatedAbonnement = abonnementService.saveAbonnement(abonnement);  // Sauvegarde dans la base de données

        // Retourner la réponse avec l'abonnement mis à jour
        return ResponseEntity.ok(updatedAbonnement);
    }

    @GetMapping("/count")
    public ResponseEntity<Long> countAbonnements() {
        long count = abonnementService.countAbonnements();
        return ResponseEntity.ok(count);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAbonnement(@PathVariable Long id) {
        abonnementService.deleteAbonnement(id);
        return ResponseEntity.noContent().build();
    }
}
