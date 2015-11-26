package com.pack.meteostationapp;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;

import com.pack.meteostationapp.chart.AverageDataChart;
import com.pack.meteostationapp.chart.CurrentDialChart;
import com.pack.meteostationapp.chart.SensorValuesChart;
import com.pack.meteostationapp.chart.MonthlyMinMaxChart;

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
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class TempActivity extends Activity {

	public TempActivity() {
		// TODO Auto-generated constructor stub
	}

	// For Debugging
	private static final String TAG = "MeteoStation_v0.1.1_TEMPERATURE";
	private static final boolean D = false;
	private static final String unit = " [°C]";
	// data received from the BluetoothService Handler
	private Handler tempHandler = new Handler();
	private Thread receiverDataThread;

	// Data for charts -> then from SQLite
	private final Integer[] yMinMaxRange = { -25, 40 };
	// daily chart
	private final String chartName = "Dzienny Rozk³ad Temperatury";
	private final String chartDescription = "Dzienny Rozk³ad Temperatury z 2 czujników";
	private final double[] firstSensorData = { 12.5, 14.5, 13.9, 14.2, 16.5,
			16.9, 18.3, 20.1, 21.8, 22.3, 22.2, 21.9, 21.7, 23.6, 25.4, 24.3,
			22.6, 20.9, 19.2, 18.6, 18.4, 18.2, 17.7, 16.3 };
	private final double[] secondSensorData = { 12.2, 21.5, 21.7, 21.5, 21.4,
			21.4, 21.3, 21.1, 20.6, 20.3, 20.2, 19.9, 19.7, 19.6, 19.9, 20.3,
			20.6, 20.9, 21.2, 21.6, 21.9, 22.1, 21.7, 21.5 };

	// year - average
	private final String tempName = "Temperatura [°C]";
	private final String tempDescription = "Rozk³ad œredniej temperatury w ci¹gu roku";
	private final double[] averageData = { -1.3, 5.5, 10.8, 14.8, 18.4, 23.4,
			26.4, 26.1, 23.6, 14.3, 9.2, 5.7 };

	// min max
	private final String[] minMaxDataParemters = { "Temperatura",
			"Stopnie Celsjusza [°C]" };
	private final String minMaxDescription = "Miesiêczny rozk³ad temperatur";
	private final double[] minValues = { -24, -19, -10, -1, 7, 12, 15, 14, 9,
			1, -11, -16 };
	private final double[] maxValues = { 7, 12, 24, 28, 33, 35, 37, 36, 28, 19,
			11, 4 };

	// private to connect xml with application
	private TextView tempValue, minTempValue, maxTempValue, titleTemp,
			dateCalendar;
	private ImageView icon;
	private ImageButton rightArrowButton, leftArrowButton;
	private Spinner chartSpinner;
	private CurrentTimeFormatter timeAndDate;
	Boolean chartSpinnerWasTouched = false;

	private class TemperatureResults extends DataResults {

		public TemperatureResults() {
			super();
			this.maxTemp = "0";
			this.minTemp = "0";
			this.temp = "0";
		}

		private String maxTemp; // used in Broadcast receiver
		private String minTemp; // used in Broadcast receiver
		private String temp; // used in Broadcast receiver

		// private static final String sign=
		// private final String unitSymbol =
		// getResources().getString(R.string.tempUnit); FAILURE WHY???!

		@Override
		public void setCurrentValue(String currentValue) {
			this.temp = currentValue;// + unitSymbol;

		}

		@Override
		public void setMinValue(String minValue, int firstTime) {
			if (firstTime > 0)
				this.minTemp = minValue;
			else
				this.minTemp = super.computeMinValue(minValue, this.minTemp);
		}

		@Override
		public void setMaxValue(String maxValue) {
			this.maxTemp = super.computeMaxValue(maxValue, this.maxTemp);
		}

		@Override
		public String getCurrentValue() {
			return temp;
		}

		@Override
		public String getMinValue() {
			return minTemp;
		}

		@Override
		public String getMaxValue() {
			return maxTemp;
		}
	};

	TemperatureResults allDataResults = new TemperatureResults();
	TempDataReceiver myData;
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
			// sendCommand("MESSAGE FROM TABLET"); // start to measure
			Log.e(TAG, "SENDEDNDND");
		} else
			setStatus(R.string.title_not_connected);

		/* buttons right and left arrow */

		View.OnClickListener buttonHandler = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = null;
				switch (v.getId()) {

				case R.id.rightArrow:

					intent = new Intent(TempActivity.this,
							PressureActivity.class);
					startActivity(intent);
					finish();
					break;

				case R.id.leftArrow:

					intent = new Intent(TempActivity.this,
							CurrentDialChart.class);
					startActivity(intent);
					finish();
					break;
				}
			}
		};

		rightArrowButton = (ImageButton) findViewById(R.id.rightArrow);
		rightArrowButton.setOnClickListener(buttonHandler);
		leftArrowButton = (ImageButton) findViewById(R.id.leftArrow);
		leftArrowButton.setImageDrawable(getResources().getDrawable(
				R.drawable.windmill_icon));
		leftArrowButton.setBackgroundColor(Color.BLACK);
		// leftArrowButton.setVisibility(View.INVISIBLE | View.GONE); //TODO -
		// uncomment it
		leftArrowButton.setOnClickListener(buttonHandler);

		timeAndDate = new CurrentTimeFormatter();
		dateCalendar = (TextView) findViewById(R.id.dateTime);
		dateCalendar.setText(timeAndDate.getCurrentTimeAndData());

		/* spinnner */

		chartSpinner = (Spinner) findViewById(R.id.chartSpinner);
		ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter
				.createFromResource(this, R.array.types_of_charts_temp,
						android.R.layout.simple_spinner_item);
		spinnerAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		chartSpinner.setAdapter(spinnerAdapter);
		// chartSpinner.setPromptId(R.string.chart_prompt);

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
								minValues, maxValues,  new Integer[]{ -5, 30});
						chart.setGradientStart(2, Color.BLUE);
						chart.setGradientStop(18, Color.GREEN);
						chartIntent = chart.execute(getApplicationContext());
						startActivity(chartIntent);

					} else if (position == 2) {
						SensorValuesChart chart = new SensorValuesChart(
								chartName, chartDescription, firstSensorData,
								secondSensorData, yMinMaxRange);
						chartIntent = chart.execute(getApplicationContext());
						startActivity(chartIntent);
					} else if (position == 3) {
						AverageDataChart avgChart = new AverageDataChart(
								tempName, tempDescription, averageData,
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

		myData = new TempDataReceiver(); // new broadcast receiver for result
											// data
		intentDataFilter = new IntentFilter(MainActivity.DATA_ACTION);

		titleTemp = (TextView) findViewById(R.id.type_of_data);
		tempValue = (TextView) findViewById(R.id.temperature);
		minTempValue = (TextView) findViewById(R.id.minValue);
		maxTempValue = (TextView) findViewById(R.id.maxValue);
		icon = (ImageView) findViewById(R.id.weather_icon);

		icon.setImageResource(R.drawable.partly_cloudly);

		titleTemp.setText("TEMPERATURA");
		tempValue.setText(allDataResults.getCurrentValue() + unit); // hardcoded
																	// for now
		minTempValue.setText(allDataResults.getMinValue());
		maxTempValue.setText(allDataResults.getMaxValue());

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

					tempHandler.post(new Runnable() {

						@Override
						public void run() {
							tempValue.setText(allDataResults.getCurrentValue()
									+ unit);
							minTempValue.setText(allDataResults.getMinValue());
							maxTempValue.setText(allDataResults.getMaxValue());
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
		// chartSpinnerWasTouched=false;
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

	private void sendCommand(String message) {
		// Check that we're actually connected before trying anything
		if (MainActivity.mMeteoAppBtService.getState() != BtService.STATE_CONNECTED) {
			Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT)
					.show();
			return;
		}

		// Check that there's actually something to send
		if (message.length() > 0) {
			// Get the message bytes and tell the BluetoothService to write
			byte[] send = message.getBytes();
			Toast.makeText(this, "WYSLANO", Toast.LENGTH_SHORT).show();
			MainActivity.mMeteoAppBtService.write(send);
		}
	}

	private final void setStatus(int resId) {
		final ActionBar actionBar = getActionBar();
		actionBar.setSubtitle(resId);
	}

	private final void setStatus(CharSequence subTitle) {
		final ActionBar actionBar = getActionBar();
		actionBar.setSubtitle(subTitle);
	}

	public class TempDataReceiver extends BroadcastReceiver {
		private String messageTAG = "TEMPERATURE";
		private String response = "0";
		private int powerOnFlag = 0;

		@Override
		public void onReceive(Context context, Intent i) {
			response = i.getStringExtra(messageTAG);
			if (response != null) {
				allDataResults.setCurrentValue(response);
				allDataResults.setMaxValue(response);

				if (powerOnFlag == 0) {
					allDataResults.setMinValue(response, 1);
					powerOnFlag = 1;
				} else
					allDataResults.setMinValue(response, 0);

			}
		}
	}

}
