package com.energy.mix.controller;

import com.energy.mix.model.EnergyMix;
import com.energy.mix.service.EnergyService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// This is an integration test - it tests the WHOLE application working together
// It starts up the entire Spring Boot app, just like it would run in real life
@SpringBootTest
@AutoConfigureMockMvc
class EnergyControllerIntegrationTest {

    // This lets us simulate a web browser making requests to our app
    // Without actually starting a real web server
    @Autowired
    private MockMvc mockMvc;

    // We're replacing the real EnergyService with a fake one we can control
    // This way we don't have to call the real energy API during tests
    @MockBean
    private EnergyService energyService;

    

    // Test: When someone visits the energy mix endpoint, do they get good data?
    @Test
    void getEnergyMix_ShouldReturnOkStatus() throws Exception {
        // Let's create some fake energy data that looks real
        List<EnergyMix> mockData = Arrays.asList(
            createEnergyMix("2024-01-01", 65.5),
            createEnergyMix("2024-01-02", 70.2),
            createEnergyMix("2024-01-03", 68.8)
        );
        
        // Tell our fake service: "When someone asks for energy mix data, return this fake data"
        when(energyService.getEnergyMixForThreeDays()).thenReturn(mockData);

        // Now let's simulate a web browser making a request to our app:
        // - Go to the URL "/api/energy/mix"
        // - And check what comes back
        mockMvc.perform(get("/api/energy/mix"))
                // We expect a successful response (HTTP 200)
                .andExpect(status().isOk())
                // We expect to get back a list/array of data
                .andExpect(jsonPath("$").isArray())
                // And that list should have exactly 3 items (3 days of data)
                .andExpect(jsonPath("$.length()").value(3));
    }

    // Test: When someone asks for optimal charging with good input, do they get a good result?
    @Test
    void getOptimalCharging_WithValidParameter_ShouldReturnOkStatus() throws Exception {
        // Create what a good response should look like
        Map<String, Object> mockResult = new HashMap<>();
        mockResult.put("startTime", "2024-01-01 14:00:00");
        mockResult.put("endTime", "2024-01-01 17:00:00");
        mockResult.put("cleanEnergyPercentage", 75.5);
        mockResult.put("requestedHours", 2);
        
        // Tell our fake service what to return
        when(energyService.findOptimalChargingWindow(2)).thenReturn(mockResult);

        // Simulate a web request with a good parameter: hours=2
        mockMvc.perform(get("/api/energy/optimal-charging")
                .param("hours", "2"))
                // Should get a successful response
                .andExpect(status().isOk())
                // Should get the right start time back
                .andExpect(jsonPath("$.startTime").value("2024-01-01 14:00:00"))
                // Should get the right clean energy percentage back
                .andExpect(jsonPath("$.cleanEnergyPercentage").value(75.5));
    }

    // Test: When someone asks for 7 hours (too many), does the app properly say "no"?
    @Test
    void getOptimalCharging_WithInvalidParameter_ShouldReturnBadRequest() throws Exception {
        // We don't need to mock the service here because the controller
        // should catch the bad input BEFORE even calling the service

        // Simulate asking for 7 hours (which is not allowed)
        mockMvc.perform(get("/api/energy/optimal-charging")
                .param("hours", "7"))
                // Should get a "bad request" error (HTTP 400)
                .andExpect(status().isBadRequest())
                // With the right error message
                .andExpect(content().string("Godziny muszą być między 1 a 6"));
    }

    // Test: When the service itself has a problem, does the controller handle it nicely?
    @Test
    void getOptimalCharging_WithServiceException_ShouldReturnBadRequest() throws Exception {
        // Tell our fake service to throw an exception when called
        when(energyService.findOptimalChargingWindow(3))
            .thenThrow(new IllegalArgumentException("Some service error"));

        // Simulate a request that should trigger the service error
        mockMvc.perform(get("/api/energy/optimal-charging")
                .param("hours", "3"))
                // Should still get a proper error response, not crash
                .andExpect(status().isBadRequest())
                // With the service's error message
                .andExpect(content().string("Some service error"));
    }

    // Test: When someone forgets to say how many hours they need, does the app complain?
    @Test
    void getOptimalCharging_WithoutParameter_ShouldReturnBadRequest() throws Exception {
        // Simulate a request without the "hours" parameter
        mockMvc.perform(get("/api/energy/optimal-charging"))
                // Should say "hey, you forgot to tell me how many hours!"
                .andExpect(status().isBadRequest());
    }

    // Helper method to create fake energy data for testing
    // This just makes a EnergyMix object with some sample numbers
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