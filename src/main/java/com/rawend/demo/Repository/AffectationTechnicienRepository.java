package com.rawend.demo.Repository;



import com.rawend.demo.entity.AffectationTechnicien;
import com.rawend.demo.entity.TechnicienEmploi;

import jakarta.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AffectationTechnicienRepository extends JpaRepository<AffectationTechnicien, Long> {

    
    List<AffectationTechnicien> findByTechnicienId(Long technicienId);

  
    List<AffectationTechnicien> findByReservationId(Long reservationId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM affectation_technicien WHERE reservation_id = :reservationId", nativeQuery = true)
    void deleteByReservationId(@Param("reservationId") Long reservationId);
    boolean existsByTechnicienIdAndDateDebutLessThanAndDateFinGreaterThan(
            Long technicienId, LocalDateTime dateFin, LocalDateTime dateDebut);
    

    // Vérification si une réservation commence pendant une autre
    boolean existsByTechnicienIdAndDateDebutBetween(
            Long technicienId, LocalDateTime start, LocalDateTime end);

    // Vérification si une réservation se termine pendant une autre
    boolean existsByTechnicienIdAndDateFinBetween(
            Long technicienId, LocalDateTime start, LocalDateTime end);
    @Query("SELECT t FROM TechnicienEmploi t WHERE t.user.email = :email")
    Optional<TechnicienEmploi> findByUserEmail(@Param("email") String email);
    
   
    boolean existsByTechnicienIdAndDateDebutBeforeAndDateFinAfter(Long technicienId, LocalDateTime dateFin, LocalDateTime dateDebut);
}