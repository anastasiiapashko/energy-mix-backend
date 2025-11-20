package com.energy.mix.model;

import java.time.LocalDateTime;

//przechowuje pojedyncze interwały
public class EnergyInterval {

	private LocalDateTime startTime;
	private LocalDateTime endTime;
	private double cleanEnergyPercentage;
	
	public EnergyInterval() {}
	
	public EnergyInterval(LocalDateTime startTime, LocalDateTime endTime, double cleanEnergyPercentage) {
		this.startTime = startTime;
		this.endTime = endTime;
		this.cleanEnergyPercentage = cleanEnergyPercentage;
	}
	
	//gettery, settery
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
