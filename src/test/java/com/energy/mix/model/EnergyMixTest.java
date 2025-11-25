package com.energy.mix.model;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class EnergyMixTest {

    // Test: Can the EnergyMix object store all the energy data we need for one day?
    @Test
    void energyMix_ShouldProperlyStoreAndRetrieveData() {
        // Let's create some sample energy data for one day
        // First, create an empty EnergyMix object (like an empty report form)
        EnergyMix energyMix = new EnergyMix();
        
        // Create a map of different energy sources and their percentages:
        // - Biomass: 15%
        // - Nuclear: 25% 
        // - Wind: 20%
        Map<String, Double> mix = new HashMap<>();
        mix.put("biomass", 15.0);
        mix.put("nuclear", 25.0);
        mix.put("wind", 20.0);

        // Now let's fill out our energy report form:
        // - Set the date to January 1, 2024
        energyMix.setDate("2024-01-01");
        // - Set the energy mix percentages we created
        energyMix.setAverageMix(mix);
        // - Set the total clean energy percentage (biomass + nuclear + wind = 60%)
        energyMix.setCleanEnergyPercentage(60.0);

        // Now let's check if everything was stored correctly:
        // - Does it remember the date we set?
        assertEquals("2024-01-01", energyMix.getDate());
        // - Does it remember the total clean energy percentage?
        assertEquals(60.0, energyMix.getCleanEnergyPercentage());
        // - Does it have all 3 energy sources we put in?
        assertEquals(3, energyMix.getAverageMix().size());
        // - Can we get back the exact percentage for biomass?
        assertEquals(15.0, energyMix.getAverageMix().get("biomass"));
    }
}