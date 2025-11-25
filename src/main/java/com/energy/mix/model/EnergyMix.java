package com.energy.mix.model;

import java.util.Map;

public class EnergyMix {

	private String date;	// Date (today, tomorrow, day after)
	private Map<String, Double> averageMix; // Stores different energy sources with their percentages
	private double cleanEnergyPercentage; // Percentage of clean energy
	
	// Empty constructor
	public EnergyMix() {}
	
	// Getters and setters
	public String getDate() {
		return date;
	}
	
	public void setDate(String date) {
		this.date = date;
		
	}
	
	public Map<String, Double> getAverageMix(){
		return averageMix;
	}
	
	public void setAverageMix(Map<String, Double> averageMix) {
		this.averageMix = averageMix;
	}
	
	public double getCleanEnergyPercentage() {
		return cleanEnergyPercentage;
	}
	
	public void setCleanEnergyPercentage(double cleanEnergyPercentage) {
		this.cleanEnergyPercentage = cleanEnergyPercentage;
	}

}