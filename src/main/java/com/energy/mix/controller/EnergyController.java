package com.energy.mix.controller;


import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.*;

import com.energy.mix.model.EnergyMix;
import com.energy.mix.service.EnergyService;

@RestController
@RequestMapping("/api/energy")
@CrossOrigin(origins = "*")
public class EnergyController {
    
    private final EnergyService energyService;  // ← Wstrzyknięty Service
    
    // Konstruktor - Spring wstrzyknie EnergyService
    public EnergyController(EnergyService energyService) {
        this.energyService = energyService;
    }
    
    //Endpoint do wyświetlania miksu energetycznego
    @GetMapping("/mix")
    public List<EnergyMix> getEnergyMix() {
        return energyService.getEnergyMixForThreeDays();  // ← Deleguje do Service
    }
    
    @GetMapping("/optimal-charging")
    public Map<String, Object> getOptimalCharging(@RequestParam int hours) {
        // Walidacja: 1-6 godzin
        if (hours < 1 || hours > 6) {
            throw new IllegalArgumentException("Godziny muszą być między 1 a 6");
        }
        
        return energyService.findOptimalChargingWindow(hours);
    }
}

//do tego chce i myślę, że dobrym pomysłem będzię ż e crossorigin bedzie w osobnym pliku konfiguracyjnym