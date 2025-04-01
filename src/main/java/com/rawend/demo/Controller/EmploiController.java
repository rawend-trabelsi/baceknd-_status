package com.rawend.demo.Controller;


import com.rawend.demo.entity.TechnicienEmploi;
import com.rawend.demo.entity.User;
import com.rawend.demo.services.EmploiService;
import com.rawend.demo.entity.Role;
import com.rawend.demo.Repository.UserRepository;
import com.rawend.demo.dto.EmploiRequest;
import com.rawend.demo.Repository.TechnicienEmploiRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/emplois")
public class EmploiController {

    private final EmploiService emploiService;

    public EmploiController(EmploiService emploiService) {
        this.emploiService = emploiService;
    }

    @PostMapping("/technicien/ajout")
    public ResponseEntity<?> ajouterEmploiTechnicien(@RequestBody EmploiRequest request) {
        try {
            emploiService.ajouterEmploiTechnicienParEmail(request);
            return ResponseEntity.ok("Emploi ajouté pour le technicien !");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @GetMapping("/techniciens/emails")
    public ResponseEntity<?> getEmailsTechniciens() {
        try {
            return ResponseEntity.ok(emploiService.getEmailsTechniciens());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @PutMapping("/technicien/update/{id}")
    public ResponseEntity<?> updateEmploiTechnicien(
            @PathVariable Long id, // L'ID récupéré depuis l'URL
            @RequestBody EmploiRequest request) { // Le corps contient les informations à mettre à jour
        try {
            emploiService.updateEmploiTechnicien(id, request); // Appel de la méthode avec l'ID et l'objet request
            return ResponseEntity.ok("Emploi du technicien mis à jour !");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @GetMapping("/techniciens")
    public ResponseEntity<?> getAllTechniciens() {
        try {
            // Appel de la méthode qui récupère tous les techniciens sous forme de Map
            return ResponseEntity.ok(emploiService.getAllTechniciensAsMap());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @GetMapping("/technicien/existe")
    public ResponseEntity<?> emailPresentDansTable(@RequestParam String email) {
        try {
            boolean existe = emploiService.emailTechnicienExiste(email);
            return ResponseEntity.ok(existe);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }




}