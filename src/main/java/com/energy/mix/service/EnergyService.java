package com.energy.mix.service;

import com.energy.mix.model.EnergyInterval;
import com.energy.mix.model.EnergyMix;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class EnergyService {
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    public EnergyService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }
    
    // Gets energy mix for today + next 2 days
    public List<EnergyMix> getEnergyMixForThreeDays() {
        List<EnergyMix> result = new ArrayList<>();
        
        for (int i = 0; i < 3; i++) {
            LocalDate date = LocalDate.now().plusDays(i);
            EnergyMix mix = getEnergyMixForDate(date);
            result.add(mix);
        }
        
        return result;
    }
    
    // Gets energy data for one specific day from the API
    private EnergyMix getEnergyMixForDate(LocalDate date) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm'Z'");
            String from = date.atStartOfDay().format(formatter);
            String to = date.plusDays(1).atStartOfDay().format(formatter);
            
            String url = "https://api.carbonintensity.org.uk/generation/" + from + "/" + to;
            String response = restTemplate.getForObject(url, String.class);
            
            return parseEnergyDataFromResponse(date.toString(), response);
            
        } catch (Exception e) {
            throw new RuntimeException("Błąd pobierania danych z API dla daty: " + date, e);
        }
    }
    
    // Converts JSON API response into EnergyMix object
    private EnergyMix parseEnergyDataFromResponse(String date, String jsonResponse) {
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);
            JsonNode dataArray = root.path("data");
            
            // Track totals for each energy source
            Map<String, Double> sums = new HashMap<>();
            Map<String, Integer> counts = new HashMap<>();
            
            // Clean energy sources we care about
            List<String> cleanSources = Arrays.asList("biomass", "nuclear", "hydro", "wind", "solar");
            
            // Process each 30-minute interval
            for(JsonNode interval : dataArray) {
                JsonNode generationMix = interval.path("generationmix");
                
                for (JsonNode fuel : generationMix) {
                    String fuelName = fuel.path("fuel").asText();
                    double percentage = fuel.path("perc").asDouble();
                    
                    // Add to total for this fuel type
                    sums.put(fuelName, sums.getOrDefault(fuelName, 0.0) + percentage);
                    counts.put(fuelName, counts.getOrDefault(fuelName, 0) + 1);
                }
            }
            
            // Calculate averages for each fuel type
            Map<String, Double> averages = new HashMap<>();
            for (String fuel : sums.keySet()) {
                double average = sums.get(fuel) / counts.get(fuel);
                averages.put(fuel, Math.round(average * 10.0) / 10.0);
            }
            
            // Calculate total clean energy percentage
            double cleanEnergyTotal = 0;
            for (String cleanSource : cleanSources) {
                cleanEnergyTotal += averages.getOrDefault(cleanSource, 0.0);
            }
            double cleanEnergyPercentage = Math.round(cleanEnergyTotal * 10.0) / 10.0;
            
            // Create and return the EnergyMix object
            EnergyMix energyMix = new EnergyMix();
            energyMix.setDate(date);
            energyMix.setAverageMix(averages);
            energyMix.setCleanEnergyPercentage(cleanEnergyPercentage);
            
            return energyMix;
            
        } catch (Exception e) {
            throw new RuntimeException("Błąd parsowania danych z API", e);
        }    
    }
    
    // Gets all 30-minute intervals for tomorrow and day after tomorrow
    private List<EnergyInterval> getEnergyIntervalsForTwoDays() {
        List<EnergyInterval> allIntervals = new ArrayList<>();
        
        // Only next 2 days (tomorrow and day after)
        for (int i = 1; i <= 2; i++) {
            LocalDate date = LocalDate.now().plusDays(i);
            List<EnergyInterval> dayIntervals = getEnergyIntervalsForDate(date);
            allIntervals.addAll(dayIntervals);
        }
        
        return allIntervals;
    }

    // Gets intervals for one specific day
    private List<EnergyInterval> getEnergyIntervalsForDate(LocalDate date) {
        List<EnergyInterval> intervals = new ArrayList<>();
        
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm'Z'");
            String from = date.atStartOfDay().format(formatter);
            String to = date.plusDays(1).atStartOfDay().format(formatter);
            
            String url = "https://api.carbonintensity.org.uk/generation/" + from + "/" + to;
            String response = restTemplate.getForObject(url, String.class);
            
            intervals = parseEnergyIntervalsFromResponse(response);
            
        } catch (Exception e) {
            throw new RuntimeException("Błąd pobierania interwałów dla daty: " + date, e);
        }
        
        return intervals;
    }
    
    // Converts JSON response into list of EnergyInterval objects
    private List<EnergyInterval> parseEnergyIntervalsFromResponse(String jsonResponse) {
        List<EnergyInterval> intervals = new ArrayList<>();
        
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);
            JsonNode dataArray = root.path("data");
            
            List<String> cleanSources = Arrays.asList("biomass", "nuclear", "hydro", "wind", "solar");
            
            for (JsonNode interval : dataArray) {
                String from = interval.path("from").asText();
                String to = interval.path("to").asText();
                
                // Convert string to LocalDateTime
                LocalDateTime startTime = LocalDateTime.parse(from.replace("Z", ""));
                LocalDateTime endTime = LocalDateTime.parse(to.replace("Z", ""));
                
                // Calculate clean energy % for this interval
                double cleanEnergyPercentage = calculateCleanEnergyForInterval(interval, cleanSources);
                
                intervals.add(new EnergyInterval(startTime, endTime, cleanEnergyPercentage));
            }
            
        } catch (Exception e) {
            throw new RuntimeException("Błąd parsowania interwałów", e);
        }
        
        return intervals;
    }

    // Calculates clean energy % for a single 30-minute interval
    private double calculateCleanEnergyForInterval(JsonNode interval, List<String> cleanSources) {
        double cleanEnergy = 0;
        JsonNode generationMix = interval.path("generationmix");
        
        for (JsonNode fuel : generationMix) {
            String fuelName = fuel.path("fuel").asText();
            double percentage = fuel.path("perc").asDouble();
            
            if (cleanSources.contains(fuelName)) {
                cleanEnergy += percentage;
            }
        }
        
        return Math.round(cleanEnergy * 10.0) / 10.0;
    }
    
    // Helper class to store the best charging window
    private static class OptimalWindow {
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private double averageCleanEnergy;
        
        public OptimalWindow() {}
        
        public LocalDateTime getStartTime() { return startTime; }
        public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
        
        public LocalDateTime getEndTime() { return endTime; }
        public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
        
        public double getAverageCleanEnergy() { return averageCleanEnergy; }
        public void setAverageCleanEnergy(double averageCleanEnergy) { 
            this.averageCleanEnergy = averageCleanEnergy; 
        }
    }
    
    // Main method to find the best time to charge electric car
    public Map<String, Object> findOptimalChargingWindow(int hours) {
        // Validate input: only 1-6 hours allowed
        if (hours < 1 || hours > 6) {
            throw new IllegalArgumentException("Godziny muszą być między 1 a 6");
        }
        
        // Convert hours to 30-minute intervals needed
        int intervalsNeeded = hours * 2;
        
        // Get all intervals for next 2 days
        List<EnergyInterval> allIntervals = getEnergyIntervalsForTwoDays();
        
        // Find the best time window
        OptimalWindow optimalWindow = findBestWindow(allIntervals, intervalsNeeded);
        
        // Return result with formatted times
        return Map.of(
            "startTime", optimalWindow.getStartTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
            "endTime", optimalWindow.getEndTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
            "cleanEnergyPercentage", optimalWindow.getAverageCleanEnergy(),
            "requestedHours", hours
        );
    }

    // Finds the best consecutive time window with highest clean energy
    private OptimalWindow findBestWindow(List<EnergyInterval> intervals, int intervalsNeeded) {
        if (intervals.size() < intervalsNeeded) {
            throw new RuntimeException("Za mało danych do znalezienia okna");
        }
        
        OptimalWindow bestWindow = null;
        double bestAverage = -1;
        
        // Slide window through all intervals
        for (int i = 0; i <= intervals.size() - intervalsNeeded; i++) {
            double windowSum = 0;
            
            // Calculate sum for current window
            for (int j = 0; j < intervalsNeeded; j++) {
                windowSum += intervals.get(i + j).getCleanEnergyPercentage();
            }
            
            double windowAverage = windowSum / intervalsNeeded;
            
            // If this is the best window so far, save it
            if (windowAverage > bestAverage) {
                bestAverage = windowAverage;
                bestWindow = new OptimalWindow();
                bestWindow.setStartTime(intervals.get(i).getStartTime());
                bestWindow.setEndTime(intervals.get(i + intervalsNeeded - 1).getEndTime());
                bestWindow.setAverageCleanEnergy(Math.round(windowAverage * 10.0) / 10.0);
            }
        }
        
        return bestWindow;
    }
}