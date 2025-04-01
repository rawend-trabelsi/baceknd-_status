package com.rawend.demo.services;

import java.util.Date;
import java.util.Map;

import org.springframework.security.core.userdetails.UserDetails;

public interface JWTService {
 String extractUsername(String token);
 String generateToken(UserDetails userDetails) ;
 boolean isTokenValid(String token, UserDetails userDetails);
 boolean isTokenExpired(String token);
 Date extractExpiration(String token);
 String generateRefreshToken(Map<String, Object> extraClaims,UserDetails userDetails);

}
