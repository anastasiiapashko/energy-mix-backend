package com.energy.mix.model;

import java.util.Map;

public class EnergyMix {

	private String date;	//data (dziś, jutro, pojutrze)
	private Map<String, Double> averageMix; //przechowuje różne źródła z ich procentami
	private double cleanEnergyPercentage; //procent czystej energii
	
	//konstructor
	public EnergyMix() {}
	
	//gettery i settery
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

