package com.rawend.demo.config;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Remplacez ceci par la logique de récupération d'un utilisateur depuis votre base de données
        throw new UsernameNotFoundException("Utilisateur non trouvé avec le nom d'utilisateur : " + username);
    }
}
