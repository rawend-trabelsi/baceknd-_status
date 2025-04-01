package com.rawend.demo.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.rawend.demo.entity.Abonnement;

public interface AbonnementRepository extends JpaRepository<Abonnement, Long> {
}
