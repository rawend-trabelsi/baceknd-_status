package com.rawend.demo.Repository;


import com.rawend.demo.entity.PromotionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;



public interface PromotionRepository extends JpaRepository<PromotionEntity, Long> {
	
    Optional<PromotionEntity> findByCodePromo(String codePromo);
    List<PromotionEntity> findByServices_Id(Long serviceId);
    @Query("SELECT DISTINCT p FROM PromotionEntity p LEFT JOIN FETCH p.services")
    List<PromotionEntity> findAllWithServices();
  

}
