package com.rawend.demo.config;



import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Arrays;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.rawend.demo.entity.Role;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class SecurityConfiguration {

  
    private final UserDetailsService userDetailsService; 
    private final JWTAuthenticationFilter jwtAuthenticationFilter;// Injection du UserDetailsService

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()  
            .authorizeRequests()
            
            .requestMatchers(HttpMethod.PUT,"/api/technicien/**").hasAuthority("ADMIN")
            .requestMatchers(HttpMethod.DELETE, "/api/technicien/**").hasAuthority("ADMIN") 
            .requestMatchers(HttpMethod.GET, "/api/technicien/**").hasAnyAuthority("ADMIN")
            .requestMatchers(HttpMethod.POST, "/api/technicien/**").hasAnyAuthority("ADMIN")
       
            .requestMatchers(HttpMethod.PUT, "/reservations/*/terminer").hasAuthority("TECHNICIEN")
            .requestMatchers(HttpMethod.POST, "/api/promotions/apply").hasAnyAuthority("USER")

               /* .requestMatchers(HttpMethod.GET, "/api/promotions/servicesWithPromotions").hasAnyRole("USER", "ADMIN")*/
                .requestMatchers("/error").permitAll()
               
                .requestMatchers(HttpMethod.POST, "/api/reservations/**").hasAnyAuthority("USER")
          
            
                .requestMatchers(HttpMethod.PUT,"/api/promotions/**").hasAuthority("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/promotions/**").hasAuthority("ADMIN") 
                .requestMatchers(HttpMethod.GET, "/api/promotions/**").hasAnyAuthority("ADMIN", "USER")
                .requestMatchers(HttpMethod.POST, "/api/promotions/**").hasAnyAuthority("ADMIN", "USER")
                
                .requestMatchers(HttpMethod.GET, "/api/avis/**").hasAnyAuthority("ADMIN", "USER")
                .requestMatchers(HttpMethod.POST, "/api/avis/**").hasAnyAuthority("USER")

                .requestMatchers(HttpMethod.PUT, "/api/services/promo").permitAll()
                .requestMatchers(HttpMethod.PUT, "/api/services/**").hasAuthority("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/services/**").hasAuthority("ADMIN") // Only ADMIN can delete services
                .requestMatchers(HttpMethod.POST, "/api/services/**").hasAuthority("ADMIN") // Only ADMIN can create services
                .requestMatchers(HttpMethod.GET, "/api/services/**").hasAnyAuthority("ADMIN", "USER")
                
                .requestMatchers(HttpMethod.PUT, "/api/abonnements/**").hasAuthority("ADMIN")  // Only ADMIN can update abonnements
                .requestMatchers(HttpMethod.DELETE, "/api/abonnements/**").hasAuthority("ADMIN") // Only ADMIN can delete abonnements
                .requestMatchers(HttpMethod.POST, "/api/abonnements/**").hasAuthority("ADMIN") // Only ADMIN can create abonnements
                .requestMatchers(HttpMethod.GET, "/api/abonnements/**").hasAnyAuthority("ADMIN", "USER") // Both ADMIN and USER can view abonnements


     
              
               
                .anyRequest().authenticated()
            .and()
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService); // Utilisation de userDetailsService injecté
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}

