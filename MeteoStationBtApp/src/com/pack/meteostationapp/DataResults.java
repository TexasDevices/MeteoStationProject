package com.pack.meteostationapp;

public abstract class DataResults implements IMeasuredData {

	public DataResults() {
		this.setCurrentValue("0");
		this.setMaxValue("0");
		this.setMinValue("0",0);
	}

	public String computeMaxValue(String data, String currentMax) {

		if((data==null)||(currentMax==null)){return currentMax;}
		Double tempData = Double.parseDouble(data);
		Double tempMax = Double.parseDouble(currentMax);
		return tempData > tempMax ? data : currentMax;
	}

	public String computeMinValue(String data, String currentMin) {
		if((data==null)||(currentMin==null)){return currentMin;}
		Double tempData = Double.parseDouble(data);
		Double tempMin = Double.parseDouble(currentMin);
		return tempData < tempMin ? data : currentMin;
	}

}
