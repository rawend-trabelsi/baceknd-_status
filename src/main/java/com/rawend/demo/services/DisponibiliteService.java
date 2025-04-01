package com.rawend.demo.services;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.rawend.demo.Repository.AffectationTechnicienRepository;
import com.rawend.demo.Repository.ReservationRepository;
import com.rawend.demo.Repository.TechnicienEmploiRepository;
import com.rawend.demo.entity.AffectationTechnicien;
import com.rawend.demo.entity.DisponibiliteDTO;
import com.rawend.demo.entity.JourRepos;
import com.rawend.demo.entity.ReservationEntity;
import com.rawend.demo.entity.TechnicienEmploi;
@Service
public class DisponibiliteService {

    @Autowired
    private TechnicienEmploiRepository technicienEmploiRepository;

    @Autowired
    private AffectationTechnicienRepository affectationTechnicienRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    public List<DisponibiliteDTO> getDisponibilites(Long technicienId) {
        List<DisponibiliteDTO> disponibilites = new ArrayList<>();

        // Récupérer l'emploi du technicien (horaires de travail)
        TechnicienEmploi emploi = technicienEmploiRepository.findById(technicienId)
                .orElseThrow(() -> new RuntimeException("Technicien non trouvé"));

        // Récupérer les affectations et les réservations
        List<AffectationTechnicien> affectations = affectationTechnicienRepository.findByTechnicienId(technicienId);
        List<ReservationEntity> reservations = reservationRepository.findByTechnicienId(technicienId);

        // Jour de repos : Période où le technicien ne travaille pas
        LocalDate jourRepos = getJourRepos(emploi.getJourRepos());
        if (jourRepos != null) {
            DisponibiliteDTO jourReposIndisponible = new DisponibiliteDTO();
            jourReposIndisponible.setDateDebut(jourRepos.atStartOfDay());
            jourReposIndisponible.setDateFin(jourRepos.plusDays(1).atStartOfDay());
            disponibilites.add(jourReposIndisponible);
        }

     
        for (AffectationTechnicien affectation : affectations) {
            DisponibiliteDTO nonDisponible = new DisponibiliteDTO();
            nonDisponible.setDateDebut(affectation.getDateDebut());
            nonDisponible.setDateFin(affectation.getDateFin());
            disponibilites.add(nonDisponible);
        }

        for (ReservationEntity reservation : reservations) {
            DisponibiliteDTO nonDisponible = new DisponibiliteDTO();
            nonDisponible.setDateDebut(reservation.getDateReservation());
            nonDisponible.setDateFin(reservation.getDateReservation().plusHours(1));  
            disponibilites.add(nonDisponible);
        }

       
        LocalTime heureDebut = emploi.getHeureDebut();
        LocalTime heureFin = emploi.getHeureFin();
        
        LocalDateTime plageDebut = LocalDateTime.now().withHour(heureDebut.getHour()).withMinute(heureDebut.getMinute());
        LocalDateTime plageFin = LocalDateTime.now().withHour(heureFin.getHour()).withMinute(heureFin.getMinute());
        
        DisponibiliteDTO emploiIndisponible = new DisponibiliteDTO();
        emploiIndisponible.setDateDebut(plageDebut);
        emploiIndisponible.setDateFin(plageFin);
        disponibilites.add(emploiIndisponible);

        return disponibilites;
    }

    private LocalDate getJourRepos(JourRepos jourRepos) {
        switch (jourRepos) {
            case LUNDI: return LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY));
            case MARDI: return LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.TUESDAY));
            case MERCREDI: return LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.WEDNESDAY));
            case JEUDI: return LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.THURSDAY));
            case VENDREDI: return LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.FRIDAY));
            case SAMEDI: return LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.SATURDAY));
            case DIMANCHE: return LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.SUNDAY));
            default: return null;
        }
    }
}
