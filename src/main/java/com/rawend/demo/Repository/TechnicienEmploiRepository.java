package com.rawend.demo.Repository;


import com.rawend.demo.entity.TechnicienEmploi;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TechnicienEmploiRepository extends JpaRepository<TechnicienEmploi, Long> {
    Optional<TechnicienEmploi> findByUserId(Long userId);
    boolean existsByUserEmail(String email);
    Optional<TechnicienEmploi> findByEmail(String email);
    Optional<TechnicienEmploi> findByUserEmail(String email);


    
}