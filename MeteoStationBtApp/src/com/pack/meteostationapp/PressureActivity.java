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

public class PressureActivity extends Activity {

	public PressureActivity() {
		// TODO Auto-generated constructor stub
	}

	// For Debugging
	private static final String TAG = "MeteoStation_v0.1.1_PRESSURE";
	private static final boolean D = false;
	private static final String unit = " [hPa]";

	// data received from the BluetoothService Handler

	private Handler pressureHandler = new Handler();
	private Thread receiverDataThread;

	// Data for charts -> then from SQLite
	private final Integer[] yMinMaxRange = { 950, 1045 };
	// daily chart
	private final String chartName = "Dzienny Rozk³ad Ciœnienia";
	private final String chartDescription = "Dzienny Rozk³ad Ciœnienia z 2 czujników";
	private final double[] firstSensorData = { 985.6, 985.8, 985.9, 986.6,
			987.1, 986.8, 986.9, 987.0, 987.0, 987.1, 987.2, 987.3, 987.1,
			986.9, 986.8, 985.6, 986.8, 986.9, 985.9, 985.8, 986.1, 985.6,
			985.6, 985.6 };
	private final double[] secondSensorData = { 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0, 0,
			0, 0};

	// year - average
	private final String pressureName = "Ciœnienie [hPa]";
	private final String pressureDescription = "Rozk³ad œredniej ciœnienia w ci¹gu roku";
	private final double[] averageData = { 999.6, 1001.8, 1004.9, 986.6, 987.1,
			1009.8, 1012.9, 1005.0, 1014.0, 987.1, 987.2, 987.3 };

	// min max
	private final String[] minMaxDataParemters = { "Ciœnienie", "[hPa]" };
	private final String minMaxDescription = "Miesiêczny rozk³ad ciœnieñ";
	private final double[] minValues = { 985.6-28, 985.8-28, 985.9-28, 986.6-28, 987.1-28,
			986.8-28, 986.9-28, 987.0-28, 987.0-28, 987.1-28, 987.2-28, 987.3-28 };
	private final double[] maxValues = { 1015.6+35.7, 999.8+35.7, 995.9+35.7, 1006.6+35.7, 987.1+35.7,
			986.8+35.7, 1000.9+35.7, 987.0+35.7, 977.0+35.7, 987.1+35.7, 987.2+35.7, 987.3+35.7 };

	// private to connect xml with application
	// private to connect xml with application
	private TextView pressureValue, minPressureValue, maxPressureValue,
			titlePressure, dateCalendar;
	private ImageView icon;
	private ImageButton rightArrowButton, leftArrowButton;
	private Spinner chartSpinner;
	private CurrentTimeFormatter timeAndDate;
	Boolean chartSpinnerWasTouched = false;

	private class PressureResults extends DataResults {

		public PressureResults() {
			super();

			this.maxPressure = "0";
			this.minPressure = "0";
			this.pressure = "0";
		}

		private String maxPressure; // used in Broadcast receiver
		private String minPressure; // used in Broadcast receiver
		private String pressure; // used in Broadcast receiver

		// private static final String sign=
		// private final String unitSymbol =
		// getResources().getString(R.string.tempUnit); FAILURE WHY???!

		@Override
		public void setCurrentValue(String currentValue) {
			this.pressure = currentValue;// + unitSymbol;

		}

		@Override
		public void setMinValue(String minValue, int firstTime) {
			if (firstTime > 0)
				this.minPressure = minValue;
			else
				this.minPressure = super.computeMinValue(minValue,
						this.minPressure);
		}

		@Override
		public void setMaxValue(String maxValue) {

			this.maxPressure = super
					.computeMaxValue(maxValue, this.maxPressure);

		}

		@Override
		public String getCurrentValue() {
			return pressure;
		}

		@Override
		public String getMinValue() {
			return minPressure;
		}

		@Override
		public String getMaxValue() {
			return maxPressure;
		}
	};

	PressureResults allDataResults = new PressureResults();
	PressureDataReceiver myData;
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

					intent = new Intent(PressureActivity.this,
							HumidityActivity.class);
					startActivity(intent);
					finish();
					break;

				case R.id.leftArrow:

					intent = new Intent(PressureActivity.this,
							TempActivity.class);
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
				.createFromResource(this, R.array.types_of_charts_pressure,
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
								minValues, maxValues,  new Integer[]{ 950, 1050});
						chartIntent = chart.execute(getApplicationContext());
						chart.setGradientStart(993, Color.RED);
						chart.setGradientStop(1005, Color.GREEN);
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

		myData = new PressureDataReceiver(); // new broadcast receiver for the
												// result data
		intentDataFilter = new IntentFilter(MainActivity.DATA_ACTION);

		titlePressure = (TextView) findViewById(R.id.type_of_data);
		pressureValue = (TextView) findViewById(R.id.temperature);
		minPressureValue = (TextView) findViewById(R.id.minValue);
		maxPressureValue = (TextView) findViewById(R.id.maxValue);
		icon = (ImageView) findViewById(R.id.weather_icon);

		icon.setImageResource(R.drawable.pressure_icon);

		titlePressure.setText("CIŒNIENIE");
		pressureValue.setText(allDataResults.getCurrentValue() + unit);
		minPressureValue.setText(allDataResults.getMinValue());
		maxPressureValue.setText(allDataResults.getMaxValue());

		// sendCommand("AAAA"); // start measure

		receiverDataThread = new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {

					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					pressureHandler.post(new Runnable() {

						@Override
						public void run() {
							pressureValue.setText(allDataResults
									.getCurrentValue() + unit);
							minPressureValue.setText(allDataResults
									.getMinValue());
							maxPressureValue.setText(allDataResults
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

	public class PressureDataReceiver extends BroadcastReceiver {
		private String messageTAG = "PRESSURE";
		private String response = null;
		private int powerOnFlag = 0;

		@Override
		public void onReceive(Context context, Intent i) {
			response = i.getStringExtra(messageTAG);
			if (response != null)
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
