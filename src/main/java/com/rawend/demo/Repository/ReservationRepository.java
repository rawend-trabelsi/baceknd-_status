package com.rawend.demo.Repository;

import com.rawend.demo.entity.ReservationEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReservationRepository extends JpaRepository<ReservationEntity, Long> {
	List<ReservationEntity> findByTechnicienId(Long technicienId);
	@Query("SELECT r FROM ReservationEntity r WHERE r.user.email = :email ORDER BY r.dateCreation DESC")
	List<ReservationEntity> findByUserEmailOrderByDateCreationDesc(@Param("email") String email);
	// Dans ReservationRepository.java
	Optional<ReservationEntity> findByIdAndUserEmail(Long id, String email);
	}


