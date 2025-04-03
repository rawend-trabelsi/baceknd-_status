package com.rawend.demo.Repository;

import com.rawend.demo.entity.ReservationEntity;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<ReservationEntity, Long> {
	List<ReservationEntity> findByTechnicienId(Long technicienId);

	}


