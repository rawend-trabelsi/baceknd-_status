package com.rawend.demo.services.impl;

import com.rawend.demo.Repository.AvisRepository;
import com.rawend.demo.Repository.ServiceRepository;
import com.rawend.demo.entity.AvisEntity;
import com.rawend.demo.entity.ServiceEntity;

import com.rawend.demo.services.AvisService;
import com.rawend.demo.services.JWTService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AvisServiceImpl implements AvisService {

    @Autowired
    private AvisRepository avisRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private JWTService jwtService;
    @Override
    public AvisEntity ajouterAvis(AvisEntity avis, String token) {
        // Extraire l'email de l'utilisateur à partir du token JWT
        String email = jwtService.extractUsername(token.replace("Bearer ", ""));

        // Vérifier si le service associé à l'avis existe
        Optional<ServiceEntity> serviceOpt = serviceRepository.findById(avis.getService().getId());
        if (serviceOpt.isEmpty()) {
            throw new IllegalArgumentException("Le service avec l'ID " + avis.getService().getId() + " n'a pas été trouvé.");
        }

        // Récupérer le service et assigner l'email de l'utilisateur à l'avis
        ServiceEntity service = serviceOpt.get();
        avis.setEmail(email);
        avis.setService(service); // Assigner le service complet à l'avis

        // Ajouter le titre du service à l'avis pour la base de données
        avis.setTitreService(service.getTitre()); // Assurez-vous d'assigner le titre ici

        // Sauvegarder l'avis dans la base de données
        AvisEntity savedAvis = avisRepository.save(avis);

        return savedAvis;
    }




    public List<AvisEntity> getAvisByService(Long serviceId) {
        return avisRepository.findByService_Id(serviceId);
    }
}
