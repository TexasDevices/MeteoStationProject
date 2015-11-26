package com.pack.meteostationapp;

import com.pack.meteostationapp.chart.AverageDataChart;
import com.pack.meteostationapp.chart.MonthlyMinMaxChart;
import com.pack.meteostationapp.chart.SensorValuesChart;

import android.app.ActionBar;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

public class HumidityActivity extends Activity {

	public HumidityActivity() {
		// TODO Auto-generated constructor stub
	}

	// For Debugging
	private static final String TAG = "MeteoStation_v0.1.1_HUMIDITY";
	private static final boolean D = false;
	private static final String unit = " [%]";

	// Key names received from the BluetoothService Handler
	private Handler humidityHandler = new Handler();
	private Thread receiverDataThread;

	// Data for charts -> then from SQLite
	private final Integer[] yMinMaxRange = { 0, 100 };

	// daily chart
	private final String chartName = "Dzienny Rozk³ad Wilgotnoœci";
	private final String chartDescription = "Dzienny Rozk³ad Wilgotnoœci z 2 czujników";
	private final double[] firstSensorData = { 77.1, 77.3, 77.1, 77.0, 77.4,
			77.2, 77.5, 77.4, 78.4, 79.2, 79.5, 79.4, 80.5, 81.4, 83.4, 84.2,
			85.5, 86.4, 85.4, 84.2, 83.5, 84.4, 83.5, 84.4, 83.5 };
	private final double[] secondSensorData = { 77.1, 77.3, 77.1, 77.0, 77.4,
			77.2, 77.5, 77.4, 78.4, 79.2, 79.5, 79.4, 80.5, 81.4, 83.4, 84.2,
			85.5, 86.4, 85.4, 84.2, 83.5, 84.4, 83.5, 84.4, 83.5 };

	// year - average
	private final String pressureName = "Wilgotnoœæ [%]";
	private final String pressureDescription = "Rozk³ad œredniej wilgotnoœci w ci¹gu roku";
	private final double[] averageData = { 81.4, 83.4, 84.2, 85.5, 86.4, 85.4,
			84.2, 83.5, 84.4, 83.5, 84.4, 83.5 };

	// min max
	private final String[] minMaxDataParemters = { "Wilgotnoœæ", "[hPa]" };
	private final String minMaxDescription = "Miesiêczny rozk³ad wilgotnoœci";
	private final double[] minValues = { 71.4, 73.4, 74.2, 75.5, 56.4, 35.4,
			29.2, 43.5, 44.4, 53.5, 60.4, 63.5 };
	private final double[] maxValues = { 85.4, 86.4, 87.2, 89.5, 90.4, 91.4,
			91.2, 90.5, 91.4, 92.5, 96.4, 91.5 };

	// private to connect xml with application
	private TextView humidityValue, minHumidityValue, maxHumidityValue,
			titleHumidity, dateCalendar;
	private ImageView icon;
	private ImageButton rightArrowButton, leftArrowButton;
	private Spinner chartSpinner;
	private CurrentTimeFormatter timeAndDate;
	Boolean chartSpinnerWasTouched = false;

	private class HumidityResults extends DataResults {

		public HumidityResults() {
			super();
			this.maxHumidity = "0";
			this.minHumidity = "0";
			this.humidity = "0";
		}

		private String maxHumidity; // used in Broadcast receiver
		private String minHumidity; // used in Broadcast receiver
		private String humidity; // used in Broadcast receiver

		// private static final String sign=
		// private final String unitSymbol =
		// getResources().getString(R.string.tempUnit); FAILURE WHY???!

		@Override
		public void setCurrentValue(String currentValue) {
			this.humidity = currentValue;// + unitSymbol;

		}

		@Override
		public void setMinValue(String minValue,int firstTime){
			if (firstTime>0)
				this.minHumidity = minValue;
			else
			this.minHumidity = super.computeMinValue(minValue, this.minHumidity);
		}

		@Override
		public void setMaxValue(String maxValue) {
			this.maxHumidity = super.computeMaxValue(maxValue, this.maxHumidity );
		}

		@Override
		public String getCurrentValue() {
			return humidity;
		}

		@Override
		public String getMinValue() {
			return minHumidity;
		}

		@Override
		public String getMaxValue() {
			return maxHumidity;
		}
	};

	HumidityResults allDataResults = new HumidityResults();
	HumidityDataReceiver myData;
	IntentFilter intentDataFilter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_weather);
		if (MainActivity.isConnected) {
			setStatus(getString(R.string.title_connected_to,
					MainActivity.mConnectedDeviceName));

			String[] message = getResources().getStringArray(
					R.array.COMMANDS_TO_METER);
			// ////////////////////////////////////////////
			// sendCommand(message[0]); // start to measure
			Log.e(TAG, "SENDEDNDND");
		} else
			setStatus(R.string.title_not_connected);

		View.OnClickListener buttonHandler = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = null;
				switch (v.getId()) {

				case R.id.rightArrow:

					intent = new Intent(HumidityActivity.this,
							LightActivity.class);
					startActivity(intent);
					finish();
					break;

				case R.id.leftArrow:

					intent = new Intent(HumidityActivity.this,
							PressureActivity.class);
					startActivity(intent);
					finish();
					break;
				}
			}
		};

		rightArrowButton = (ImageButton) findViewById(R.id.rightArrow);
		rightArrowButton.setOnClickListener(buttonHandler);
		leftArrowButton = (ImageButton) findViewById(R.id.leftArrow);
		leftArrowButton.setOnClickListener(buttonHandler);

		timeAndDate = new CurrentTimeFormatter();
		dateCalendar = (TextView) findViewById(R.id.dateTime);
		dateCalendar.setText(timeAndDate.getCurrentTimeAndData());

		/* chart spinnner */

		chartSpinner = (Spinner) findViewById(R.id.chartSpinner);
		ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter
				.createFromResource(this, R.array.types_of_charts_humidity,
						android.R.layout.simple_spinner_item);
		spinnerAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		chartSpinner.setAdapter(spinnerAdapter);

		final class ChartSpinnerListener implements OnItemSelectedListener {
			Intent chartIntent = null;

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				if (chartSpinnerWasTouched) {
					if (D)
						Toast.makeText(
								parent.getContext(),
								"OnItemSelectedListener : "
										+ parent.getItemAtPosition(position)
												.toString(), Toast.LENGTH_SHORT)
								.show();
					if (position == 1) {
						MonthlyMinMaxChart chart = new MonthlyMinMaxChart(
								minMaxDataParemters, minMaxDescription,
								minValues, maxValues, new Integer[]{ 10, 95});
						chartIntent = chart.execute(getApplicationContext());
						chart.setGradientStart(36, Color.BLUE);
						chart.setGradientStop(78, Color.YELLOW);
						startActivity(chartIntent);

					} else if (position == 2) {
						SensorValuesChart chart = new SensorValuesChart(
								chartName, chartDescription, firstSensorData,
								secondSensorData, yMinMaxRange);
						chartIntent = chart.execute(getApplicationContext());
						startActivity(chartIntent);
					} else if (position == 3) {
						AverageDataChart avgChart = new AverageDataChart(
								pressureName, pressureDescription, averageData,
								yMinMaxRange);
						chartIntent = avgChart.execute(getApplicationContext());
						startActivity(chartIntent);
					} else {

						if (D)
							Toast.makeText(parent.getContext(), "No item !: ",
									Toast.LENGTH_SHORT).show();
					}
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				chartSpinner.setPromptId(R.string.chart_prompt);
			}

		}
		;
		chartSpinnerWasTouched = true; // to avoid first openingof new chart
		chartSpinner.setOnItemSelectedListener(new ChartSpinnerListener());

		myData = new HumidityDataReceiver(); // new broadcast receiver for
												// result data
		intentDataFilter = new IntentFilter(MainActivity.DATA_ACTION);

		titleHumidity = (TextView) findViewById(R.id.type_of_data);
		humidityValue = (TextView) findViewById(R.id.temperature);
		minHumidityValue = (TextView) findViewById(R.id.minValue);
		maxHumidityValue = (TextView) findViewById(R.id.maxValue);
		icon = (ImageView) findViewById(R.id.weather_icon);

		icon.setImageResource(R.drawable.humidity_icon);

		titleHumidity.setText("WILGOTNOŒÆ");
		humidityValue.setText(allDataResults.getCurrentValue() + unit);
		minHumidityValue.setText(allDataResults.getMinValue());
		maxHumidityValue.setText(allDataResults.getMaxValue());

		receiverDataThread = new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {

					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					humidityHandler.post(new Runnable() {

						@Override
						public void run() {
							humidityValue.setText(allDataResults
									.getCurrentValue() + unit);
							minHumidityValue.setText(allDataResults
									.getMinValue());
							maxHumidityValue.setText(allDataResults
									.getMaxValue());
							dateCalendar.setText(timeAndDate
									.getCurrentTimeAndData());
						}
					});

				}

			}
		});

		if (!receiverDataThread.isAlive()) {
			if (D)
				Log.i(TAG, "Thread_START");
			receiverDataThread.start();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		registerReceiver(myData, intentDataFilter);
	}

	@Override
	protected void onDestroy() {

		if (D)
			Log.e(TAG, "-- ON DESTROY --");
		super.onDestroy();
		// /////////////////////////sendCommand("BBBx"); // finish measure
		unregisterReceiver(myData);
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (D)
			Log.e(TAG, "-- ON STOP --");
	}

	@Override
	public void onPause() {
		super.onPause();

		if (D)
			Log.e(TAG, "-- ON PAUSE --");
		// /if (mChatService != null) mChatService.stop();
	}

	private final void setStatus(int resId) {
		final ActionBar actionBar = getActionBar();
		actionBar.setSubtitle(resId);
	}

	private final void setStatus(CharSequence subTitle) {
		final ActionBar actionBar = getActionBar();
		actionBar.setSubtitle(subTitle);
	}

	public class HumidityDataReceiver extends BroadcastReceiver {
		private String messageTAG = "HUMIDITY";
		private String response = null;
		private int powerOnFlag = 0;

		@Override
		public void onReceive(Context context, Intent i) {
			response = i.getStringExtra(messageTAG);
			if (response != null) {
				allDataResults.setMaxValue(response);
				allDataResults.setCurrentValue(response);
				if (powerOnFlag == 0) {
					allDataResults.setMinValue(response, 1);
					powerOnFlag = 1;
				} else
					allDataResults.setMinValue(response, 0);
			}
		}
	}
}