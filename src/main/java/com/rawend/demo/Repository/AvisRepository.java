package com.rawend.demo.Repository;



import com.rawend.demo.entity.AvisEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AvisRepository extends JpaRepository<AvisEntity, Long> {
    // Trouver les avis par ID du service
    List<AvisEntity> findByService_Id(Long serviceId);
}
