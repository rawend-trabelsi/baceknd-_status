package com.rawend.demo.services;


import com.rawend.demo.entity.TechnicienEmploi;
import com.rawend.demo.entity.User;
import com.rawend.demo.entity.JourRepos;
import com.rawend.demo.entity.Role;
import com.rawend.demo.Repository.UserRepository;
import com.rawend.demo.dto.EmploiRequest;
import com.rawend.demo.Repository.TechnicienEmploiRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

@Service
public class EmploiService {

    private final UserRepository userRepository;
    private final TechnicienEmploiRepository technicienEmploiRepository;

    public EmploiService(UserRepository userRepository, TechnicienEmploiRepository technicienEmploiRepository) {
        this.userRepository = userRepository;
        this.technicienEmploiRepository = technicienEmploiRepository;
    }

    public void ajouterEmploiTechnicienParEmail(EmploiRequest request) {
        User user = userRepository.findByEmail(request.getEmail()).orElseThrow(
                () -> new RuntimeException("Utilisateur avec cet email introuvable"));

        if (!user.getRole().equals(Role.TECHNICIEN)) {
            throw new RuntimeException("Cet utilisateur n'est pas un technicien !");
        }

        TechnicienEmploi emploi = new TechnicienEmploi(); // heureDebut = 08:00 et heureFin = 22:00 par défaut
        emploi.setUser(user);
        emploi.setEmail(user.getEmail());
        emploi.setPhone(user.getPhone());
        emploi.setUsername(user.getUsernameValue());
        emploi.setJourRepos(JourRepos.valueOf(request.getJourRepos().toUpperCase()));

       

        technicienEmploiRepository.save(emploi);
    }

    public List<String> getEmailsTechniciens() {
        List<User> techniciens = userRepository.findByRole(Role.TECHNICIEN);

        if (techniciens.isEmpty()) {
            throw new RuntimeException("Aucun technicien trouvé !");
        }

        return techniciens.stream().map(User::getEmail).collect(Collectors.toList());
    }
    public void updateEmploiTechnicien(Long id, EmploiRequest request) {
        TechnicienEmploi emploi = technicienEmploiRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Technicien avec cet ID introuvable"));

        if (request.getJourRepos() != null) {
            emploi.setJourRepos(JourRepos.valueOf(request.getJourRepos().toUpperCase()));
        }
        if (request.getHeureDebut() != null) {
            emploi.setHeureDebut(request.getHeureDebut());
        }
        if (request.getHeureFin() != null) {
            emploi.setHeureFin(request.getHeureFin());
        }

        technicienEmploiRepository.save(emploi);
    }
    public List<Map<String, Object>> getAllTechniciensAsMap() {
        List<TechnicienEmploi> techniciens = technicienEmploiRepository.findAll();

       
        return techniciens.stream()
            .map(technicien -> {
                // Créer une nouvelle instance de HashMap pour chaque technicien
                Map<String, Object> technicienMap = new HashMap<>();
              
                technicienMap.put("email", technicien.getEmail());
                technicienMap.put("phone", technicien.getPhone());
                technicienMap.put("username", technicien.getUsername());
                technicienMap.put("heureDebut", technicien.getHeureDebut());
                technicienMap.put("heureFin", technicien.getHeureFin());
                technicienMap.put("id", technicien.getId());
                technicienMap.put("jourRepos", technicien.getJourRepos());
               

                return technicienMap;
            })
            .collect(Collectors.toList());  // Collecte la liste des maps
    }
    public boolean emailTechnicienExiste(String email) {
        return technicienEmploiRepository.existsByUserEmail(email);
    }

    
}
