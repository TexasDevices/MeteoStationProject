package com.pack.meteostationapp;

public interface IMeasuredData {

	public void setCurrentValue(String currentValue);

	public void setMinValue(String minValue,int firstTime);

	public void setMaxValue(String maxValue);
    
	public String getCurrentValue();
	
	public String getMinValue();
	
	public String getMaxValue();

}
