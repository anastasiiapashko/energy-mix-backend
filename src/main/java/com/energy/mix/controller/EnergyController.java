package com.energy.mix.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.energy.mix.model.EnergyMix;
import com.energy.mix.service.EnergyService;

// Main controller - handles web requests
@RestController
// All URLs start with "/api/energy"
@RequestMapping("/api/energy")
// Allows web pages from any website to use our API
@CrossOrigin(origins = "*")
public class EnergyController {
    
    // The service that does all calculations
    private final EnergyService energyService;
    
    // Constructor - Spring provides the EnergyService automatically
    public EnergyController(EnergyService energyService) {
        this.energyService = energyService;
    }
    
    // Handles GET requests to "/api/energy/mix"
    // Returns energy mix data for 3 days
    @GetMapping("/mix")
    public List<EnergyMix> getEnergyMix() {
        return energyService.getEnergyMixForThreeDays();
    }
    
    // Handles GET requests to "/api/energy/optimal-charging" 
    // Requires "hours" parameter (?hours=3)
    @GetMapping("/optimal-charging")
    public ResponseEntity<?> getOptimalCharging(@RequestParam int hours) {
        try {
            // Validate input - only 1-6 hours allowed
            if (hours < 1 || hours > 6) {
                return ResponseEntity.badRequest().body("Godziny muszą być między 1 a 6");
            }
            
            // Find best charging time
            Map<String, Object> result = energyService.findOptimalChargingWindow(hours);
            
            // Return success with result
            return ResponseEntity.ok(result);
            
        } catch (IllegalArgumentException e) {
            // Handle input errors from service
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            // Handle unexpected errors
            return ResponseEntity.internalServerError().body("Wystąpił błąd serwera: " + e.getMessage());
        }
    }
}