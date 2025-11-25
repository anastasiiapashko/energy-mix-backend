package com.energy.mix.controller;

import com.energy.mix.model.EnergyMix;
import com.energy.mix.service.EnergyService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EnergyControllerTest {

    // This is a fake EnergyService that we can control for testing
    @Mock
    private EnergyService energyService;

    // This is the real EnergyController we're testing, but with the fake service injected
    @InjectMocks
    private EnergyController energyController;

    // Test: When someone asks for energy mix data, they should get a list of 3 days
    @Test
    void getEnergyMix_ShouldReturnEnergyMixList() {
        // Here's the fake data we want our fake service to return
        List<EnergyMix> expectedMix = Arrays.asList(
            createEnergyMix("2024-01-01", 65.5),
            createEnergyMix("2024-01-02", 70.2),
            createEnergyMix("2024-01-03", 68.8)
        );
        
        // Tell our fake service: "When someone calls getEnergyMixForThreeDays, return this fake data"
        when(energyService.getEnergyMixForThreeDays()).thenReturn(expectedMix);

        // Now let's actually call the method we're testing
        List<EnergyMix> result = energyController.getEnergyMix();

        // Check if everything worked as expected:
        // - Did we get some data back? (not null)
        // - Did we get exactly 3 days of data?
        // - Is the first day's data correct?
        // - And let's make sure our fake service was actually called
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("2024-01-01", result.get(0).getDate());
        assertEquals(65.5, result.get(0).getCleanEnergyPercentage());
        verify(energyService).getEnergyMixForThreeDays();
    }

    // Test: When someone asks for optimal charging with valid hours, they should get a good result
    @Test
    void getOptimalCharging_WithValidHours_ShouldReturnOptimalWindow() {
        // We're testing with 3 hours
        int hours = 3;
        
        // This is what we expect the service to return
        Map<String, Object> expectedResult = new HashMap<>();
        expectedResult.put("startTime", "2024-01-01 14:00:00");
        expectedResult.put("endTime", "2024-01-01 17:00:00");
        expectedResult.put("cleanEnergyPercentage", 75.5);
        expectedResult.put("requestedHours", 3);
        
        // Tell our fake service what to return
        when(energyService.findOptimalChargingWindow(hours)).thenReturn(expectedResult);

        // Call the actual method
        ResponseEntity<?> response = energyController.getOptimalCharging(hours);

        // Check the response:
        // - Did we get a response?
        // - Was it a successful response (HTTP 200)?
        // - Is the response body the right type?
        // - Does it contain the expected data?
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody() instanceof Map);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) response.getBody();
        assertEquals("2024-01-01 14:00:00", result.get("startTime"));
        assertEquals(75.5, result.get("cleanEnergyPercentage"));
        
        // Make sure the service was actually called
        verify(energyService).findOptimalChargingWindow(hours);
    }

    // Test: When someone asks for 0 hours (too few), they should get an error
    @Test
    void getOptimalCharging_WithInvalidHours_ShouldReturnBadRequest() {
        // Testing with 0 hours (invalid)
        int invalidHours = 0;

        // Call the method
        ResponseEntity<?> response = energyController.getOptimalCharging(invalidHours);

        // Check that we got a "bad request" error (HTTP 400)
        // and the right error message
        assertNotNull(response);
        assertEquals(400, response.getStatusCodeValue());
        assertEquals("Godziny muszą być między 1 a 6", response.getBody());
    }

    // Test: When someone asks for 7 hours (too many), they should get an error
    @Test
    void getOptimalCharging_WithTooManyHours_ShouldReturnBadRequest() {
        // Testing with 7 hours (invalid)
        int invalidHours = 7;

        // Call the method
        ResponseEntity<?> response = energyController.getOptimalCharging(invalidHours);

        // Should get the same error as above
        assertNotNull(response);
        assertEquals(400, response.getStatusCodeValue());
        assertEquals("Godziny muszą być między 1 a 6", response.getBody());
    }

    // Test: When the service itself has a problem, we should handle it gracefully
    @Test
    void getOptimalCharging_WithServiceException_ShouldReturnBadRequest() {
        int hours = 2;
        
        // Tell our fake service to throw an exception when called
        when(energyService.findOptimalChargingWindow(hours))
            .thenThrow(new IllegalArgumentException("Service error message"));

        // Call the method
        ResponseEntity<?> response = energyController.getOptimalCharging(hours);

        // We should get a "bad request" error with the service's error message
        assertNotNull(response);
        assertEquals(400, response.getStatusCodeValue());
        assertEquals("Service error message", response.getBody());
    }

    // Test: When something completely unexpected goes wrong, we should handle it
    @Test
    void getOptimalCharging_WithOtherException_ShouldReturnInternalServerError() {
        int hours = 2;
        
        // Tell our fake service to throw a generic exception
        when(energyService.findOptimalChargingWindow(hours))
            .thenThrow(new RuntimeException("Unexpected error"));

        // Call the method
        ResponseEntity<?> response = energyController.getOptimalCharging(hours);

        // We should get an "internal server error" (HTTP 500)
        // with a generic error message
        assertNotNull(response);
        assertEquals(500, response.getStatusCodeValue());
        assertTrue(response.getBody().toString().contains("Wystąpił błąd serwera"));
    }

    // Helper method to create fake energy mix data for testing
    // This just creates a EnergyMix object with some sample data
    private EnergyMix createEnergyMix(String date, double cleanPercentage) {
        EnergyMix mix = new EnergyMix();
        mix.setDate(date);
        mix.setCleanEnergyPercentage(cleanPercentage);
        mix.setAverageMix(Map.of(
            "biomass", 15.0,
            "nuclear", 25.0,
            "wind", 20.0,
            "solar", 5.0,
            "gas", 35.0
        ));
        return mix;
    }
}