package com.pack.meteostationapp;

import java.util.Calendar;

public class CurrentTimeFormatter {
	
	CurrentTimeFormatter(){calendar = Calendar.getInstance();}
	
	
	private Calendar calendar;

	public synchronized String getCurrentTimeAndData() {
		calendar = Calendar.getInstance();
		Integer hour = calendar.get(Calendar.HOUR_OF_DAY);
		Integer min = calendar.get(Calendar.MINUTE);
		Integer year = calendar.get(Calendar.YEAR);
		Integer month = calendar.get(Calendar.MONTH);
		Integer day = calendar.get(Calendar.DAY_OF_MONTH);

		return formatDataAndTime(hour) + " : " + formatDataAndTime(min)
				+ "    " + formatDataAndTime(day) + " - "
				+ formatDataAndTime(month+1) + " - " + formatDataAndTime(year);
	}

	private String formatDataAndTime(Integer timeValue) {
		String formattedValue = timeValue.toString();
		if (timeValue < 10)
			formattedValue = "0" + formattedValue;
		return formattedValue;
	}
	
	
}
