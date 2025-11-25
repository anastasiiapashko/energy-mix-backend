package com.energy.mix.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class EnergyIntervalTest {

    // Test: When we create an EnergyInterval with all the data at once, does it remember everything correctly?
    @Test
    void energyInterval_ShouldProperlyStoreAndRetrieveData() {
        // Let's create some sample data to test with:
        // - Start time: January 1, 2024 at 10:00 AM
        // - End time: January 1, 2024 at 10:30 AM (30 minutes later)
        // - Clean energy percentage: 75.5%
        LocalDateTime start = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime end = LocalDateTime.of(2024, 1, 1, 10, 30);
        double cleanPercentage = 75.5;

        // Now let's create an EnergyInterval object with all this data
        EnergyInterval interval = new EnergyInterval(start, end, cleanPercentage);

        // Let's check if it stored everything correctly:
        // - Does it remember the start time we gave it?
        assertEquals(start, interval.getStartTime());
        // - Does it remember the end time we gave it?
        assertEquals(end, interval.getEndTime());
        // - Does it remember the clean energy percentage we gave it?
        assertEquals(75.5, interval.getCleanEnergyPercentage());
    }

    // Test: What if we create an empty EnergyInterval and fill it with data later?
    // (This tests the "setter" methods)
    @Test
    void energyInterval_WithNoArgsConstructor_ShouldWork() {
        // First, create an empty EnergyInterval (like an empty box)
        EnergyInterval interval = new EnergyInterval();
        
        // Create some sample times:
        // - Start time: right now
        // - End time: 30 minutes from now
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusMinutes(30);

        // Now let's fill our empty EnergyInterval with data piece by piece:
        // - Set the start time
        interval.setStartTime(start);
        // - Set the end time
        interval.setEndTime(end);
        // - Set the clean energy percentage
        interval.setCleanEnergyPercentage(80.0);

        // Now let's check if it stored everything correctly:
        // - Did it remember the start time we set?
        assertEquals(start, interval.getStartTime());
        // - Did it remember the end time we set?
        assertEquals(end, interval.getEndTime());
        // - Did it remember the clean energy percentage we set?
        assertEquals(80.0, interval.getCleanEnergyPercentage());
    }
}