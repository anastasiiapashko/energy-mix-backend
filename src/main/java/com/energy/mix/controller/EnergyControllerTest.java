//package com.energy.mix.controller;
//
//import com.energy.mix.model.EnergyMix;
//import com.energy.mix.service.EnergyService;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.test.web.servlet.MockMvc;
//
//import java.util.Arrays;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import static org.mockito.ArgumentMatchers.anyInt;
//import static org.mockito.Mockito.when;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//@WebMvcTest(EnergyController.class)
//class EnergyControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @MockBean
//    private EnergyService energyService;
//
//    @Test
//    void testGetEnergyMix() throws Exception {
//        // Given
//        EnergyMix energyMix = new EnergyMix();
//        energyMix.setDate("2024-01-15");
//        energyMix.setAverageMix(new HashMap<>(Map.of("gas", 30.0, "wind", 25.0)));
//        energyMix.setCleanEnergyPercentage(55.0);
//
//        List<EnergyMix> mockData = Arrays.asList(energyMix);
//
//        // When
//        when(energyService.getEnergyMixForThreeDays()).thenReturn(mockData);
//
//        // Then
//        mockMvc.perform(get("/api/energy/mix"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$[0].date").value("2024-01-15"))
//                .andExpect(jsonPath("$[0].cleanEnergyPercentage").value(55.0));
//    }
//
//    @Test
//    void testGetOptimalCharging() throws Exception {
//        // Given
//        Map<String, Object> mockResult = new HashMap<>();
//        mockResult.put("startTime", "2024-01-15 22:00");
//        mockResult.put("endTime", "2024-01-16 01:00");
//        mockResult.put("cleanEnergyPercentage", 75.5);
//        mockResult.put("requestedHours", 3);
//
//        // When
//        when(energyService.findOptimalChargingWindow(3)).thenReturn(mockResult);
//
//        // Then
//        mockMvc.perform(get("/api/energy/optimal-charging?hours=3"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.startTime").value("2024-01-15 22:00"))
//                .andExpect(jsonPath("$.cleanEnergyPercentage").value(75.5));
//    }
//
//    @Test
//    void testGetOptimalChargingInvalidHours() throws Exception {
//        mockMvc.perform(get("/api/energy/optimal-charging?hours=0"))
//                .andExpect(status().isBadRequest());
//    }
//}