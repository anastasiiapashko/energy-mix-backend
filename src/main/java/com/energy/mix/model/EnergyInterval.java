package com.energy.mix.model;

import java.time.LocalDateTime;

// Stores single 30-minute time intervals
public class EnergyInterval {

	private LocalDateTime startTime;    // When the interval starts
	private LocalDateTime endTime;      // When the interval ends  
	private double cleanEnergyPercentage; // Clean energy % during this interval
	
	// Empty constructor
	public EnergyInterval() {}
	
	// Constructor with all data
	public EnergyInterval(LocalDateTime startTime, LocalDateTime endTime, double cleanEnergyPercentage) {
		this.startTime = startTime;
		this.endTime = endTime;
		this.cleanEnergyPercentage = cleanEnergyPercentage;
	}
	
	// Getters and setters
	public LocalDateTime getStartTime() {
		return startTime;
	}
    public void setStartTime(LocalDateTime startTime) { 
    	this.startTime = startTime; 
    }
    
    public LocalDateTime getEndTime() { 
    	return endTime; 
    }
    public void setEndTime(LocalDateTime endTime) { 
    	this.endTime = endTime; 
    }
    
    public double getCleanEnergyPercentage() { 
    	return cleanEnergyPercentage; 
    }
    public void setCleanEnergyPercentage(double cleanEnergyPercentage) { 
        this.cleanEnergyPercentage = cleanEnergyPercentage; 
    }
}