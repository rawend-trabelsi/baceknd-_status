package com.rawend.demo.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.rawend.demo.Repository.AbonnementRepository;
import com.rawend.demo.entity.Abonnement;

import java.util.List;
import java.util.Optional;

@Service
public class AbonnementService {

    @Autowired
    private AbonnementRepository abonnementRepository;
    public long countAbonnements() {
        return abonnementRepository.count();
    }
  
    public List<Abonnement> getAllAbonnements() {
        return abonnementRepository.findAll();
    }

    
    public Optional<Abonnement> getAbonnementById(Long id) {
        return abonnementRepository.findById(id);
    }

    public Abonnement saveAbonnement(Abonnement abonnement) {
        return abonnementRepository.save(abonnement);
    }

   
    public void deleteAbonnement(Long id) {
        abonnementRepository.deleteById(id);
    }
    public Abonnement findById(Long id) {
        
        return abonnementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Abonnement not found with id: " + id));
    }

}
