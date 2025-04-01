package com.rawend.demo.services;

import com.rawend.demo.entity.AvisEntity;

import java.util.List;

public interface AvisService {
    AvisEntity ajouterAvis(AvisEntity avis, String token);
    List<AvisEntity> getAvisByService(Long serviceId);
}
