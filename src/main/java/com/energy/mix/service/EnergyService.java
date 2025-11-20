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
    
    public List<EnergyMix> getEnergyMixForThreeDays() {
        List<EnergyMix> result = new ArrayList<>();
        
        for (int i = 0; i < 3; i++) {
            LocalDate date = LocalDate.now().plusDays(i);
            EnergyMix mix = getEnergyMixForDate(date);
            result.add(mix);
        }
        
        return result;
    }
    
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
    
    private EnergyMix parseEnergyDataFromResponse(String date, String jsonResponse) {
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);
            JsonNode dataArray = root.path("data");
            
            // Mapy do zliczania sum dla każdego źródła energii
            Map<String, Double> sums = new HashMap<>();
            Map<String, Integer> counts = new HashMap<>();
            
            // Lista czystych źródeł energii
            List<String> cleanSources = Arrays.asList("biomass", "nuclear", "hydro", "wind", "solar");
            
            // półgodzinne interwały
            for(JsonNode interval : dataArray) {
                JsonNode generationMix = interval.path("generationmix");
                
                for (JsonNode fuel : generationMix) {
                    String fuelName = fuel.path("fuel").asText();
                    double percentage = fuel.path("perc").asDouble();
                    
                    // Dodaj do sumy dla tego źródła
                    sums.put(fuelName, sums.getOrDefault(fuelName, 0.0) + percentage);
                    counts.put(fuelName, counts.getOrDefault(fuelName, 0) + 1);
                }
            }
            
            // obliczenie średniej
            Map<String, Double> averages = new HashMap<>();
            for (String fuel : sums.keySet()) {
                double average = sums.get(fuel) / counts.get(fuel);
                averages.put(fuel, Math.round(average * 10.0) / 10.0);
            }
            
            // Oblicz % czystej energii
            double cleanEnergyTotal = 0;
            for (String cleanSource : cleanSources) {
                cleanEnergyTotal += averages.getOrDefault(cleanSource, 0.0);
            }
            double cleanEnergyPercentage = Math.round(cleanEnergyTotal * 10.0) / 10.0;
            
            // Stwórz obiekt EnergyMix
            EnergyMix energyMix = new EnergyMix();
            energyMix.setDate(date);
            energyMix.setAverageMix(averages);
            energyMix.setCleanEnergyPercentage(cleanEnergyPercentage);
            
            return energyMix;
            
        } catch (Exception e) {
            throw new RuntimeException("Błąd parsowania danych z API", e);
        }    
    }
    
  //metoda do pobrania interwałów
 // Pobiera WSZYSTKIE interwały półgodzinne dla 2 dni (jutro i pojutrze)
    private List<EnergyInterval> getEnergyIntervalsForTwoDays() {
        List<EnergyInterval> allIntervals = new ArrayList<>();
        
        // Tylko 2 dni: jutro (i=1) i pojutrze (i=2)
        for (int i = 1; i <= 2; i++) {
            LocalDate date = LocalDate.now().plusDays(i);
            List<EnergyInterval> dayIntervals = getEnergyIntervalsForDate(date);
            allIntervals.addAll(dayIntervals);
        }
        
        return allIntervals;
    }

    // Pobiera interwały dla JEDNEGO dnia
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
    
 // Parsuje interwały z JSON (podobnie jak wcześniej, ale zwraca listę interwałów)
    private List<EnergyInterval> parseEnergyIntervalsFromResponse(String jsonResponse) {
        List<EnergyInterval> intervals = new ArrayList<>();
        
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);
            JsonNode dataArray = root.path("data");
            
            // Lista czystych źródeł energii
            List<String> cleanSources = Arrays.asList("biomass", "nuclear", "hydro", "wind", "solar");
            
            for (JsonNode interval : dataArray) {
                String from = interval.path("from").asText(); // "2025-11-20T00:00Z"
                String to = interval.path("to").asText();     // "2025-11-20T00:30Z"
                
                // Konwersja string na LocalDateTime
                LocalDateTime startTime = LocalDateTime.parse(from.replace("Z", ""));
                LocalDateTime endTime = LocalDateTime.parse(to.replace("Z", ""));
                
                // Oblicz % czystej energii dla tego interwału
                double cleanEnergyPercentage = calculateCleanEnergyForInterval(interval, cleanSources);
                
                intervals.add(new EnergyInterval(startTime, endTime, cleanEnergyPercentage));
            }
            
        } catch (Exception e) {
            throw new RuntimeException("Błąd parsowania interwałów", e);
        }
        
        return intervals;
    }

    // Oblicza % czystej energii dla JEDNEGO interwału
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
    
 // KLASA POMOCNICZA dla przechowywania najlepszego okna
    private static class OptimalWindow {
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private double averageCleanEnergy;
        
        public OptimalWindow() {}
        
        // gettery i settery
        public LocalDateTime getStartTime() { return startTime; }
        public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
        
        public LocalDateTime getEndTime() { return endTime; }
        public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
        
        public double getAverageCleanEnergy() { return averageCleanEnergy; }
        public void setAverageCleanEnergy(double averageCleanEnergy) { 
            this.averageCleanEnergy = averageCleanEnergy; 
        }
    }
    
    public Map<String, Object> findOptimalChargingWindow(int hours) {
        // 1 godzina = 2 interwały półgodzinne
        int intervalsNeeded = hours * 2;
        
        // Pobierz wszystkie interwały dla 2 dni
        List<EnergyInterval> allIntervals = getEnergyIntervalsForTwoDays();
        
        // Znajdź najlepsze okno
        OptimalWindow optimalWindow = findBestWindow(allIntervals, intervalsNeeded);
        
        return Map.of(
            "startTime", optimalWindow.getStartTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
            "endTime", optimalWindow.getEndTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
            "cleanEnergyPercentage", optimalWindow.getAverageCleanEnergy(),
            "requestedHours", hours
        );
    }

    // Szuka najlepszego okna spośród wszystkich interwałów
    private OptimalWindow findBestWindow(List<EnergyInterval> intervals, int intervalsNeeded) {
        if (intervals.size() < intervalsNeeded) {
            throw new RuntimeException("Za mało danych do znalezienia okna");
        }
        
        OptimalWindow bestWindow = null;
        double bestAverage = -1;
        
        // Przesuwaj okno po wszystkich interwałach
        for (int i = 0; i <= intervals.size() - intervalsNeeded; i++) {
            double windowSum = 0;
            
            // Oblicz sumę czystej energii dla tego okna
            for (int j = 0; j < intervalsNeeded; j++) {
                windowSum += intervals.get(i + j).getCleanEnergyPercentage();
            }
            
            double windowAverage = windowSum / intervalsNeeded;
            
            // Jeśli to najlepsze okno dotychczas, zapisz je
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