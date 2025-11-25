package com.energy.mix.service;

import com.energy.mix.model.EnergyMix;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EnergyServiceTest {

    // This is a fake HTTP client that we can control
    // (the real one would call the actual energy API)
    @Mock
    private RestTemplate restTemplate;

    // This is the real EnergyService we're testing, but with our fake HTTP client
    @InjectMocks
    private EnergyService energyService;

    private ObjectMapper objectMapper;
    private String mockApiResponse;

    // This runs before each test to set everything up
    @BeforeEach
    void setUp() {
        // We need this to convert JSON strings to Java objects
        objectMapper = new ObjectMapper();
        
        // This is a bit technical - we're manually setting the ObjectMapper 
        // inside the EnergyService because it's normally set by Spring
        try {
            var field = EnergyService.class.getDeclaredField("objectMapper");
            field.setAccessible(true);
            field.set(energyService, objectMapper);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // This is fake data that looks exactly like what the real API would return
        // We're making up some energy mix data for 4 different time periods
        mockApiResponse = """
            {
                "data": [
                    {
                        "from": "2024-01-01T00:00:00Z",
                        "to": "2024-01-01T00:30:00Z",
                        "generationmix": [
                            {"fuel": "biomass", "perc": 10.0},
                            {"fuel": "coal", "perc": 5.0},
                            {"fuel": "nuclear", "perc": 20.0},
                            {"fuel": "wind", "perc": 15.0},
                            {"fuel": "solar", "perc": 5.0},
                            {"fuel": "hydro", "perc": 2.0},
                            {"fuel": "gas", "perc": 43.0}
                        ]
                    },
                    {
                        "from": "2024-01-01T00:30:00Z", 
                        "to": "2024-01-01T01:00:00Z",
                        "generationmix": [
                            {"fuel": "biomass", "perc": 12.0},
                            {"fuel": "coal", "perc": 3.0},
                            {"fuel": "nuclear", "perc": 22.0},
                            {"fuel": "wind", "perc": 18.0},
                            {"fuel": "solar", "perc": 2.0},
                            {"fuel": "hydro", "perc": 1.0},
                            {"fuel": "gas", "perc": 42.0}
                        ]
                    },
                    {
                        "from": "2024-01-01T01:00:00Z",
                        "to": "2024-01-01T01:30:00Z",
                        "generationmix": [
                            {"fuel": "biomass", "perc": 8.0},
                            {"fuel": "coal", "perc": 4.0},
                            {"fuel": "nuclear", "perc": 25.0},
                            {"fuel": "wind", "perc": 20.0},
                            {"fuel": "solar", "perc": 3.0},
                            {"fuel": "hydro", "perc": 2.0},
                            {"fuel": "gas", "perc": 38.0}
                        ]
                    },
                    {
                        "from": "2024-01-01T01:30:00Z",
                        "to": "2024-01-01T02:00:00Z",
                        "generationmix": [
                            {"fuel": "biomass", "perc": 15.0},
                            {"fuel": "coal", "perc": 2.0},
                            {"fuel": "nuclear", "perc": 18.0},
                            {"fuel": "wind", "perc": 25.0},
                            {"fuel": "solar", "perc": 4.0},
                            {"fuel": "hydro", "perc": 1.0},
                            {"fuel": "gas", "perc": 35.0}
                        ]
                    }
                ]
            }
            """;
    }

    // Test: When we ask for energy mix data, we should get 3 days worth
    @Test
    void getEnergyMixForThreeDays_ShouldReturnThreeDays() {
        // Tell our fake HTTP client: "When anyone asks for API data, return our fake response"
        when(restTemplate.getForObject(anyString(), eq(String.class)))
            .thenReturn(mockApiResponse);

        // Now let's actually call the method that gets energy mix data
        List<EnergyMix> result = energyService.getEnergyMixForThreeDays();

        // Check if everything worked:
        // - Did we get some data back?
        // - Did we get exactly 3 days? (today, tomorrow, day after)
        // - For each day, check:
        //   * Does it have a date?
        //   * Does it have energy mix data?
        //   * Is the clean energy percentage between 0-100%? (makes sense, right?)
        assertNotNull(result);
        assertEquals(3, result.size());
        
        result.forEach(energyMix -> {
            assertNotNull(energyMix.getDate());
            assertNotNull(energyMix.getAverageMix());
            assertTrue(energyMix.getCleanEnergyPercentage() >= 0);
            assertTrue(energyMix.getCleanEnergyPercentage() <= 100);
        });
    }

    // Test: When we ask for the best charging time with valid hours, we should get a good result
    @Test
    void findOptimalChargingWindow_WithValidHours_ShouldReturnOptimalWindow() {
        // Again, tell our fake HTTP client to return our fake data
        when(restTemplate.getForObject(anyString(), eq(String.class)))
            .thenReturn(mockApiResponse);
        
        // We're testing with 2 hours of charging time
        int hours = 2;

        // Call the method that finds the best time to charge the car
        Map<String, Object> result = energyService.findOptimalChargingWindow(hours);

        // Check the result:
        // - Did we get something back?
        // - Does it tell us when to start charging?
        // - Does it tell us when to stop charging?
        // - Does it tell us how clean the energy will be during that time?
        // - Is the clean energy percentage reasonable (0-100%)?
        assertNotNull(result);
        assertNotNull(result.get("startTime"));
        assertNotNull(result.get("endTime"));
        assertNotNull(result.get("cleanEnergyPercentage"));
        
        double percentage = (Double) result.get("cleanEnergyPercentage");
        assertTrue(percentage >= 0 && percentage <= 100);
    }

    // Test: When someone asks for 7 hours (too many), it should complain
    @Test
    void findOptimalChargingWindow_WithInvalidHours_ShouldThrowException() {
        // Testing with 7 hours (we only allow 1-6 hours)
        int invalidHours = 7;

        // When we call the method, it should throw an exception saying "hey, that's not allowed!"
        // We're checking that it throws the right kind of exception
        assertThrows(IllegalArgumentException.class, 
            () -> energyService.findOptimalChargingWindow(invalidHours));
    }

    // Test: When someone asks for 0 hours (too few), it should also complain
    @Test
    void findOptimalChargingWindow_WithZeroHours_ShouldThrowException() {
        // Testing with 0 hours (doesn't make sense to charge for 0 hours!)
        int invalidHours = 0;

        // Should throw the same kind of exception
        assertThrows(IllegalArgumentException.class, 
            () -> energyService.findOptimalChargingWindow(invalidHours));
    }

    // Test: When we ask for just 1 hour, it should work fine
    @Test
    void findOptimalChargingWindow_WithOneHour_ShouldWork() {
        // Fake HTTP client returns our fake data
        when(restTemplate.getForObject(anyString(), eq(String.class)))
            .thenReturn(mockApiResponse);
        
        // Testing with 1 hour (the minimum allowed)
        int hours = 1;

        // Call the method
        Map<String, Object> result = energyService.findOptimalChargingWindow(hours);

        // Check that we got a result with start and end times
        assertNotNull(result);
        assertNotNull(result.get("startTime"));
        assertNotNull(result.get("endTime"));
    }
}