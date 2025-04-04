package com.rawend.demo.services;

import com.rawend.demo.dto.ReservationRequest;
import com.rawend.demo.entity.*;
import com.rawend.demo.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ReservationService {

    @Autowired
    private ReservationRepository reservationRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ServiceRepository serviceRepository;
    
    @Autowired
    private TechnicienEmploiRepository technicienEmploiRepository;
    @Autowired
    private PromotionService promotionService;
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private AffectationTechnicienRepository affectationTechnicienRepository;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public List<ReservationEntity> getReservationsByClientEmail(String email) {
        return reservationRepository.findByUserEmailOrderByDateCreationDesc(email);
    }
    
    private DayOfWeek convertJourReposToDayOfWeek(JourRepos jourRepos) {
        switch (jourRepos) {
            case LUNDI: return DayOfWeek.MONDAY;
            case MARDI: return DayOfWeek.TUESDAY;
            case MERCREDI: return DayOfWeek.WEDNESDAY;
            case JEUDI: return DayOfWeek.THURSDAY;
            case VENDREDI: return DayOfWeek.FRIDAY;
            case SAMEDI: return DayOfWeek.SATURDAY;
            case DIMANCHE: return DayOfWeek.SUNDAY;
            default: return null;
        }
    }
  
 // Dans ReservationService.java
    public ReservationEntity getReservationByIdAndEmail(Long reservationId, String email) {
        return reservationRepository.findByIdAndUserEmail(reservationId, email)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, 
                "Réservation non trouvée ou accès non autorisé"
            ));
    }
    public Map<String, Object> createReservation(ReservationRequest request, Authentication authentication) {

        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        ServiceEntity service = serviceRepository.findById(request.serviceId())
            .orElseThrow(() -> new RuntimeException("Service non trouvé"));

        Double prixFinal = service.getPrix();
        String duree = service.getDuree();

        
        PromotionEntity promo = service.getPromotion();

        if (request.codePromo() != null && !request.codePromo().isEmpty()) {
            
            promo = promotionService.trouverParCode(request.codePromo());
            if (promo != null && promo.getActif() && promotionService.estServiceEligible(promo.getId(), service.getId())) {
            	   
            	 LocalDate today = LocalDate.now();  	
            	 LocalDateTime startOfDay = today.atStartOfDay();
                   LocalDateTime endOfDay = today.atTime(23, 59, 59);
                   LocalDateTime promoStart = convertToLocalDateTime(promo.getDateDebut()).withHour(0).withMinute(0).withSecond(0);
                   LocalDateTime promoEnd = convertToLocalDateTime(promo.getDateFin()).withHour(23).withMinute(59).withSecond(59);

                
                if (!startOfDay.isBefore(promoStart) && !endOfDay.isAfter(promoEnd)) {
                    prixFinal = promo.getTypeReduction() == TypeReduction.POURCENTAGE
                        ? service.getPrix() * (1 - promo.getValeurReduction() / 100)
                        : service.getPrix() - promo.getValeurReduction();
                }
            }
        } else if (promo != null && (promo.getCodePromo() == null || promo.getCodePromo().isEmpty())) {
        
            LocalDate today = LocalDate.now();
            LocalDateTime startOfDay = today.atStartOfDay();
            LocalDateTime endOfDay = today.atTime(23, 59, 59);
            LocalDateTime promoStart = convertToLocalDateTime(promo.getDateDebut()).withHour(0).withMinute(0).withSecond(0);
            LocalDateTime promoEnd = convertToLocalDateTime(promo.getDateFin()).withHour(23).withMinute(59).withSecond(59);

            if (!promoStart.isAfter(endOfDay) && !promoEnd.isBefore(startOfDay)) {
                prixFinal = promo.getTypeReduction() == TypeReduction.POURCENTAGE
                    ? service.getPrix() * (1 - promo.getValeurReduction() / 100)
                    : service.getPrix() - promo.getValeurReduction();
            }
        }

        ReservationEntity reservation = new ReservationEntity();
        reservation.setUser(user);
        reservation.setStatus(ReservationStatus.EN_ATTENTE); 
        reservation.setService(service);
     // Formatage du prix avant l'insertion
        double prixArrondi = BigDecimal.valueOf(prixFinal)
                                       .setScale(3, RoundingMode.HALF_UP)
                                       .doubleValue();

        reservation.setPrix(prixArrondi); 
        reservation.setDuree(duree);
        reservation.setModePaiement(request.modePaiement());
        reservation.setLocalisation(request.localisation());
        reservation.setDateReservation(request.dateReservation());
        reservation.setDateCreation(LocalDateTime.now());
        reservation.setEmail(email);
        reservation.setPhone(user.getPhone());
        reservation.setTitreService(service.getTitre());
        ReservationEntity savedReservation = reservationRepository.save(reservation);
        scheduleNotificationReminders(savedReservation);
        Map<String, Object> response = new HashMap<>();
        response.put("reservationId", savedReservation.getId());
        response.put("userEmail", savedReservation.getEmail());
        response.put("userPhone", savedReservation.getPhone());
        response.put("serviceTitre", savedReservation.getTitreService());
        response.put("prixFinal", String.format("%.3f", savedReservation.getPrix()));
        response.put("modePaiement", savedReservation.getModePaiement());
        response.put("duree", savedReservation.getDuree());
        response.put("localisation", savedReservation.getLocalisation());
        response.put("dateReservation", savedReservation.getDateReservation());
        response.put("dateCreation", savedReservation.getDateCreation());
        response.put("serviceId", service.getId());
        response.put("userId", user.getId());

        return response;
    }
    private void scheduleNotificationReminders(ReservationEntity reservation) {
        LocalDate nowDate = LocalDate.now();
        LocalDateTime nowDateTime = LocalDateTime.now();
        LocalDate reservationDate = reservation.getDateReservation().toLocalDate();
        LocalDate creationDate = reservation.getDateCreation().toLocalDate();
        
        String userEmail = reservation.getUser().getEmail();
        String serviceTitle = reservation.getTitreService();

        // 1. Rappel 7 jours avant
        if (!creationDate.isAfter(reservationDate.minusDays(7))) {
            LocalDate reminder7dDate = reservationDate.minusDays(7);
            if (reminder7dDate.isAfter(nowDate)) {
                scheduleReminderAtFixedTime(userEmail, serviceTitle, reservation.getDateReservation(), 
                                          reminder7dDate.atTime(8, 0), "7 jours avant");
            } else {
                sendReminder(userEmail, serviceTitle, reservation.getDateReservation(), "7 jours avant");
            }
        }

        // 2. Rappel 48h avant (converti en 2 jours)
        if (!creationDate.isAfter(reservationDate.minusDays(2))) {
            LocalDate reminder2dDate = reservationDate.minusDays(2);
            if (reminder2dDate.isAfter(nowDate)) {
                scheduleReminderAtFixedTime(userEmail, serviceTitle, reservation.getDateReservation(),
                                         reminder2dDate.atTime(8, 0), "48 heures avant");
            } else {
                sendReminder(userEmail, serviceTitle, reservation.getDateReservation(), "48 heures avant");
            }
        }

        // 3. Rappel 2h avant (gestion précise)
        LocalDateTime reminder2h = reservation.getDateReservation().minusHours(2);
        if (reservation.getDateCreation().isBefore(reminder2h)) {
            if (reminder2h.isAfter(nowDateTime)) {
                scheduleReminderAtFixedTime(userEmail, serviceTitle, reservation.getDateReservation(),
                                         reminder2h, "2 heures avant");
            } else {
                sendReminder(userEmail, serviceTitle, reservation.getDateReservation(), "2 heures avant");
            }
        }
    }

    private void scheduleReminderAtFixedTime(String email, String service, LocalDateTime reservationDateTime,
                                           LocalDateTime reminderTime, String reminderType) {
        long delay = Duration.between(LocalDateTime.now(), reminderTime).toMillis();
        scheduler.schedule(() -> 
            sendReminder(email, service, reservationDateTime, reminderType),
            delay,
            TimeUnit.MILLISECONDS
        );
        log.info("Rappel {} planifié pour {}", reminderType, reminderTime);
    }

    private void sendReminder(String email, String service, LocalDateTime date, String reminderType) {
        String message = String.format(
            "⏰ Rappel (%s) : '%s' prévu le %s",
            reminderType,
            service,
            date.format(DateTimeFormatter.ofPattern("dd/MM à HH:mm"))
        );
        notificationService.sendNotificationToUser(email, message);
        log.info("Notification envoyée : {}", message);
    }

    
    private LocalDateTime convertToLocalDateTime(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }
    public void modifierAffectationTechnicien(Long reservationId, String emailTechnicien) {
        ReservationEntity reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Réservation introuvable"));

        TechnicienEmploi technicien = technicienEmploiRepository.findByUserEmail(emailTechnicien)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Technicien introuvable"));

        
        LocalDate reservationDate = reservation.getDateReservation().toLocalDate();
        JourRepos jourReposTechnicien = technicien.getJourRepos();
        JourRepos jourReservation = convertirJourEnFrancais(reservationDate.getDayOfWeek().toString());

        if (jourReposTechnicien == jourReservation) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le technicien est en repos le " + jourReservation);
        }

        String dureeStr = reservation.getDuree().toLowerCase(); 
        int dureeMinutes = Integer.parseInt(dureeStr.replaceAll("[^0-9]", "")) * (dureeStr.contains("h") ? 60 : 1);


        LocalDateTime dateDebut = reservation.getDateReservation();
        LocalDateTime dateFin = dateDebut.plusMinutes(dureeMinutes);

      
        LocalTime heureDebutTravail = technicien.getHeureDebut();
        LocalTime heureFinTravail = technicien.getHeureFin();

        if (dateDebut.toLocalTime().isBefore(heureDebutTravail) || dateFin.toLocalTime().isAfter(heureFinTravail)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La réservation dépasse les horaires de travail du technicien (" + heureDebutTravail + " - " + heureFinTravail + ")");
        }

       
        boolean hasConflict = affectationTechnicienRepository.existsByTechnicienIdAndDateDebutBeforeAndDateFinAfter(
                technicien.getId(), dateFin, dateDebut);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String formattedDateDebut = dateDebut.format(formatter);
        String formattedDateFin = dateFin.format(formatter);
    

        if (hasConflict) {
        	throw new ResponseStatusException(
        		    HttpStatus.BAD_REQUEST,
        		    "Le technicien a  une réservation entre " + formattedDateDebut + " et " + formattedDateFin);}

        
        List<AffectationTechnicien> affectations = affectationTechnicienRepository.findByReservationId(reservationId);
        if (affectations.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Affectation introuvable");
        }

 
        AffectationTechnicien affectation = affectations.get(0);

        affectation.setTechnicien(technicien);
        affectation.setDateDebut(dateDebut);
        affectation.setDateFin(dateFin);
        affectation.setUsername(technicien.getUser().getUsername());
        affectationTechnicienRepository.save(affectation);

        reservation.setStatus(ReservationStatus.EN_COURS);
        reservationRepository.save(reservation);
        reservation.setTechnicienId(technicien.getId());
        reservationRepository.save(reservation);
    }

    @Transactional
    public void marquerReservationTerminee(Long reservationId, Authentication authentication) {
        try {
            ReservationEntity reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> {
                    log.error("Réservation non trouvée ID: {}", reservationId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Réservation non trouvée");
                });

            // Vérification du technicien
            String emailTechnicien = authentication.getName();
            TechnicienEmploi technicien = technicienEmploiRepository.findByUserEmail(emailTechnicien)
                .orElseThrow(() -> {
                    log.error("Technicien non trouvé: {}", emailTechnicien);
                    return new ResponseStatusException(HttpStatus.FORBIDDEN, "Accès refusé");
                });

            // Vérification que c'est bien le technicien affecté
            if (!technicien.getId().equals(reservation.getTechnicienId())) {
                log.warn("Tentative non autorisée - Technicien {} essaye de terminer réservation {}",
                    technicien.getId(), reservationId);
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Action interdite");
            }

            // Vérification que la réservation est bien en cours
            if (reservation.getStatus() != ReservationStatus.EN_COURS) {
                log.warn("Statut incorrect pour réservation {}: {}", reservationId, reservation.getStatus());
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "La réservation doit être en cours pour être terminée");
            }

            // Mise à jour de la réservation
            reservation.setStatus(ReservationStatus.TERMINEE);
            reservation.setDateFinReelle(LocalDateTime.now());
            reservationRepository.save(reservation);

            // Notification aux admins
            String message = String.format(
                "Réservation: %d terminée\n du Service: %s\n Client: %s\n Technicien: %s\n Terminée le: %s",
                reservation.getId(),
                reservation.getTitreService(),
                reservation.getUser().getUsername(),
                technicien.getUser().getUsername(),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
            );
            // Notification à l'utilisateur
            String userMessage = String.format(
                "Votre réservation a été terminée\n" +
                "de Service: %s\n" +
                "Merci pour votre confiance !",
              
                reservation.getTitreService(),
                technicien.getUser().getUsername(),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
            );
            
            notificationService.sendNotificationToUser(
                reservation.getUser().getEmail(), 
                userMessage
            );

            notificationService.sendNotificationToAdmins(message);

            log.info("Réservation {} terminée avec succès par {}", reservationId, emailTechnicien);

        } catch (Exception e) {
            log.error("Erreur lors de la finalisation: {}", e.getMessage());
            throw e;
        }
    }
    public void affecterTechnicienAReservation(Long reservationId, Long technicienId) {
        ReservationEntity reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Réservation introuvable"));

        TechnicienEmploi technicien = technicienEmploiRepository.findById(technicienId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Technicien introuvable"));

        LocalDate reservationDate = reservation.getDateReservation().toLocalDate();
        JourRepos jourReposTechnicien = technicien.getJourRepos();
        JourRepos jourReservation = convertirJourEnFrancais(reservationDate.getDayOfWeek().toString());

        if (jourReposTechnicien == jourReservation) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le technicien est en repos le " + jourReservation);
        }

        String dureeStr = reservation.getDuree().toLowerCase(); 

        int dureeMinutes = 0;
        if (dureeStr.contains("h")) { 
            dureeMinutes = Integer.parseInt(dureeStr.replaceAll("[^0-9]", "")) * 60;
        } else if (dureeStr.contains("min")) { 
            dureeMinutes = Integer.parseInt(dureeStr.replaceAll("[^0-9]", ""));
        } else { 
            dureeMinutes = Integer.parseInt(dureeStr.replaceAll("[^0-9]", ""));
        }

       
        LocalDateTime dateDebut = reservation.getDateReservation();
        LocalDateTime dateFin = dateDebut.plusMinutes(dureeMinutes);


        // Vérification des horaires de travail du technicien
        LocalTime heureDebutTravail = technicien.getHeureDebut();
        LocalTime heureFinTravail = technicien.getHeureFin();

        if (dateDebut.toLocalTime().isBefore(heureDebutTravail) || dateFin.toLocalTime().isAfter(heureFinTravail)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La reservation depasse les horaires de travail du technicien (" + heureDebutTravail + " - " + heureFinTravail + ")");
        }
        

        // Vérification des conflits de réservation
        boolean hasConflict = affectationTechnicienRepository.existsByTechnicienIdAndDateDebutBeforeAndDateFinAfter(
                technicienId, dateFin, dateDebut);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String formattedDateDebut = dateDebut.format(formatter);
        String formattedDateFin = dateFin.format(formatter);
    

        if (hasConflict) {
        	throw new ResponseStatusException(
        		    HttpStatus.BAD_REQUEST,
        		    "Le technicien a  une reservation entre " + formattedDateDebut + " et " + formattedDateFin);}

        // Création de l'affectation si tout est bon
        AffectationTechnicien affectation = new AffectationTechnicien();
        affectation.setTechnicien(technicien);
        affectation.setReservation(reservation);
        affectation.setDateDebut(dateDebut);
        affectation.setDateFin(dateFin);
        affectation.setUsername(technicien.getUser().getUsername());
        reservation.setStatus(ReservationStatus.EN_COURS);
        reservation.setTechnicienId(technicienId);
        reservationRepository.save(reservation);
        affectationTechnicienRepository.save(affectation);

        reservation.setTechnicienId(technicienId);
        reservationRepository.save(reservation);
    }
    public List<ReservationEntity> getAllReservations() {
        return reservationRepository.findAll();
    }


    public JourRepos convertirJourEnFrancais(String jourAnglais) {
        Map<String, JourRepos> mapping = new HashMap<>();
        mapping.put("MONDAY", JourRepos.LUNDI);
        mapping.put("TUESDAY", JourRepos.MARDI);
        mapping.put("WEDNESDAY", JourRepos.MERCREDI);
        mapping.put("THURSDAY", JourRepos.JEUDI);
        mapping.put("FRIDAY", JourRepos.VENDREDI);
        mapping.put("SATURDAY", JourRepos.SAMEDI);
        mapping.put("SUNDAY", JourRepos.DIMANCHE);
        return mapping.getOrDefault(jourAnglais, null);
    }
    public List<Map<String, Object>> getAllReservationsWithDates() {
        List<ReservationEntity> reservations = reservationRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();

        for (ReservationEntity reservation : reservations) {
            Map<String, Object> reservationMap = new HashMap<>();
            reservationMap.put("id", reservation.getId());
            reservationMap.put("titre", reservation.getTitreService());
            
            LocalDateTime dateDebut = reservation.getDateReservation();
            reservationMap.put("dateDebut", dateDebut);
            
            LocalDateTime dateFin = calculateDateFin(dateDebut, reservation.getDuree());
            reservationMap.put("dateFin", dateFin);
            
            result.add(reservationMap);
        }
        return result;
    }

    private LocalDateTime calculateDateFin(LocalDateTime dateDebut, String duree) {
       
        Pattern pattern = Pattern.compile("(\\d+)h(\\d+)min");
        Matcher matcher = pattern.matcher(duree);
        
        if (matcher.find()) {
            int heures = Integer.parseInt(matcher.group(1));
            int minutes = Integer.parseInt(matcher.group(2));
            return dateDebut.plusHours(heures).plusMinutes(minutes);
        }
        
      
        pattern = Pattern.compile("(\\d+)min");
        matcher = pattern.matcher(duree);
        
        if (matcher.find()) {
            int minutes = Integer.parseInt(matcher.group(1));
            return dateDebut.plusMinutes(minutes);
        }
        
        return dateDebut.plusHours(1); // Par défaut 1 heure
    }

    public List<LocalDateTime> getDatesIndisponibles() {
        List<ReservationEntity> reservations = reservationRepository.findAll();
        long totalTechniciens = technicienEmploiRepository.count();
        
        Map<LocalDateTime, Long> reservationsCount = new HashMap<>();
        
        for (ReservationEntity res : reservations) {
            LocalDateTime dateDebut = res.getDateReservation();
            reservationsCount.put(dateDebut, 
                reservationsCount.getOrDefault(dateDebut, 0L) + 1);
        }
        
        List<LocalDateTime> datesIndisponibles = new ArrayList<>();
        for (Map.Entry<LocalDateTime, Long> entry : reservationsCount.entrySet()) {
            if (entry.getValue() >= totalTechniciens) {
                datesIndisponibles.add(entry.getKey());
            }
        }
        
        return datesIndisponibles;
    }
    public List<Map<String, LocalDateTime>> getCreneauxIndisponiblesComplets() {
        List<ReservationEntity> reservations = reservationRepository.findAll();
        List<TechnicienEmploi> techniciens = technicienEmploiRepository.findAll();

        // Compter les techniciens disponibles par jour
        Map<DayOfWeek, Long> techniciensDisponiblesParJour = techniciens.stream()
            .collect(Collectors.groupingBy(
                tech -> convertJourReposToDayOfWeek(tech.getJourRepos()),
                Collectors.counting()
            ));

        // Calculer max techniciens disponibles par jour
        Map<DayOfWeek, Long> maxTechniciensParJour = new HashMap<>();
        long totalTechniciens = techniciens.size();

        for (DayOfWeek jour : DayOfWeek.values()) {
            long enRepos = techniciensDisponiblesParJour.getOrDefault(jour, 0L);
            maxTechniciensParJour.put(jour, totalTechniciens - enRepos);
        }

        // Liste des événements de début et fin des réservations
        List<Map.Entry<LocalDateTime, Integer>> events = new ArrayList<>();

        for (ReservationEntity reservation : reservations) {
            LocalDateTime debut = reservation.getDateReservation();
            LocalDateTime fin = calculateDateFin(debut, reservation.getDuree());

            events.add(new AbstractMap.SimpleEntry<>(debut, +1)); // Début : +1 technicien occupé
            events.add(new AbstractMap.SimpleEntry<>(fin, -1));   // Fin : -1 technicien occupé
        }

      
        events.sort(Map.Entry.comparingByKey());

        List<Map<String, LocalDateTime>> creneauxIndisponibles = new ArrayList<>();
        long techniciensOccupes = 0;
        LocalDateTime debutIndisponibilite = null;

        for (Map.Entry<LocalDateTime, Integer> event : events) {
            LocalDateTime currentTime = event.getKey();
            techniciensOccupes += event.getValue();

            DayOfWeek jour = currentTime.getDayOfWeek();
            long maxDisponibles = maxTechniciensParJour.getOrDefault(jour, totalTechniciens);

            if (techniciensOccupes >= maxDisponibles) {
                // Début de l’indisponibilité
                if (debutIndisponibilite == null) {
                    debutIndisponibilite = currentTime;
                }
            } else {
                // Fin de l’indisponibilité
                if (debutIndisponibilite != null) {
                    // Vérifier si le créneau précédent finit exactement au moment où celui-ci commence
                    if (!creneauxIndisponibles.isEmpty() && 
                        creneauxIndisponibles.get(creneauxIndisponibles.size() - 1).get("fin").equals(debutIndisponibilite)) {
                        
                        // Fusionner avec le dernier créneau
                        creneauxIndisponibles.get(creneauxIndisponibles.size() - 1).put("fin", currentTime);
                    } else {
                        // Ajouter un nouveau créneau
                        Map<String, LocalDateTime> creneau = new HashMap<>();
                        creneau.put("debut", debutIndisponibilite);
                        creneau.put("fin", currentTime);
                        creneauxIndisponibles.add(creneau);
                    }
                    debutIndisponibilite = null;
                }
            }
        }

        return creneauxIndisponibles;
    }

 
}

