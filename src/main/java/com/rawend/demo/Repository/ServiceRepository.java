package com.rawend.demo.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.rawend.demo.entity.ServiceEntity;

public interface ServiceRepository extends JpaRepository<ServiceEntity, Long> {
	   ServiceEntity findById(long id);
	 
	       @Query("SELECT s FROM ServiceEntity s WHERE s.promotion IS NOT NULL")
	       List<ServiceEntity> findAllWithPromotions();
	     
	      
	      

	       @Query("SELECT s FROM ServiceEntity s WHERE s.promotion.id = :promotionId")
	       List<ServiceEntity> findByPromotionId(@Param("promotionId") Long promotionId);
}
