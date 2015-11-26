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

public class LightActivity extends Activity {

	public LightActivity() {
		// TODO Auto-generated constructor stub
	}

	// For Debugging
	private static final String TAG = "MeteoStation_v0.1.1_LIGHT";
	private static final boolean D = false;
	private static final String unit =" [lx]";

	// Key names received from the BluetoothService Handler
	private Handler lightHandler = new Handler();
	private Thread receiverDataThread;

	// Data for charts -> then from SQLite
	private final Integer[] yMinMaxRange = { 0, 800 };
	// daily chart
	private final String chartName = "Dzienny Rozk³ad Natê¿enia Oœwietlenia";
	private final String chartDescription = "Dzienny Rozk³ad Natê¿enia Oœwietlenia z 2 czujników";
	private final double[] firstSensorData = { 30, 28, 17, 368, 469,
			490, 410, 500, 520, 530, 534, 560, 567, 571,575, 578,
			601, 530, 502,404, 238, 106, 48,27 };
	private final double[] secondSensorData = { 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0};

	// year - average
	private final String tempName = "Natê¿enie oœwietlenia [lx]";
	private final String tempDescription = "Rozk³ad œredniej wart. natê¿. oœwietlernia w ci¹gu roku";
	private final double[] averageData = { 520, 530, 534, 560, 567, 571,575, 578,
			601, 530, 502,404 };

	// min max
	private final String[] minMaxDataParemters = { "Natê¿enie oœwietlenia",
			"Stopnie Celsjusza [°C]" };
	private final String minMaxDescription = "Miesiêczny rozk³ad natê¿enia oœwietlenia";
	private final double[] minValues = { 520-43, 530-43, 534-43, 560-43, 567-43, 571-43,575-43, 578-43,
			601-43, 530-43, 502-43,494-43 };
	private final double[] maxValues = { 520+37, 530+37, 534+37, 560+37, 567+37, 571+37,575+37, 578+37,
			601+37, 530+37, 502+37,404+37 };

	// private to connect xml with application
	private TextView lightValue, minLightValue, maxLightValue, titleLight,dateCalendar;
	private ImageView icon;
	private ImageButton rightArrowButton, leftArrowButton;
	private Spinner chartSpinner;
	private CurrentTimeFormatter timeAndDate;
	Boolean chartSpinnerWasTouched = false;

	private class LightResults extends DataResults {

		public LightResults() {
			super();
			this.maxLight="0"; 
		    this.minLight="0"; 
		    this.light="0";
		}

		private String maxLight; // used in Broadcast receiver
		private String minLight; // used in Broadcast receiver
		private String light; // used in Broadcast receiver

		@Override
		public void setCurrentValue(String currentValue) {
			this.light = currentValue;// + unitSymbol;

		}

		@Override
		public void setMinValue(String minValue,int firstTime) {
			if (firstTime>0)
				this.minLight = minValue;
			else
			this.minLight = super.computeMinValue(minValue, this.minLight);
		}

		@Override
		public void setMaxValue(String maxValue) {
			this.maxLight = super.computeMaxValue(maxValue, this.maxLight );
		}

		@Override
		public String getCurrentValue() {
			return light;
		}

		@Override
		public String getMinValue() {
			return minLight;
		}

		@Override
		public String getMaxValue() {
			return maxLight;
		}
	};

	LightResults allDataResults = new LightResults();
	LightDataReceiver myData;
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
		/* buttons right and left arrow */

		View.OnClickListener buttonHandler = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = null;
				switch (v.getId()) {

				case R.id.leftArrow:

					intent = new Intent(LightActivity.this,
							HumidityActivity.class);
					startActivity(intent);
					finish();
					break;
				}
			}
		};

		leftArrowButton = (ImageButton) findViewById(R.id.leftArrow);
		leftArrowButton.setOnClickListener(buttonHandler);
		rightArrowButton = (ImageButton) findViewById(R.id.rightArrow);
		rightArrowButton.setVisibility(View.INVISIBLE | View.GONE);
		
		timeAndDate=new CurrentTimeFormatter();
		dateCalendar = (TextView) findViewById(R.id.dateTime);
		dateCalendar.setText(timeAndDate.getCurrentTimeAndData());

		/* spinnner */

		chartSpinner = (Spinner) findViewById(R.id.chartSpinner);
		ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter
				.createFromResource(this, R.array.types_of_charts_light,
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
								minValues, maxValues,new Integer[]{ 450, 650});
						chart.setGradientStart(470, Color.BLUE);
						chart.setGradientStop(570, Color.GREEN);
						chartIntent = chart.execute(getApplicationContext());
						startActivity(chartIntent);

					} else if (position == 2) {
						SensorValuesChart chart = new SensorValuesChart(
								chartName, chartDescription, firstSensorData,
								secondSensorData,yMinMaxRange);
						chartIntent = chart.execute(getApplicationContext());
						startActivity(chartIntent);
					} else if (position == 3) {
						AverageDataChart avgChart = new AverageDataChart(
								tempName, tempDescription, averageData,yMinMaxRange);
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

		myData = new LightDataReceiver(); // new broadcast receiver for result
											// data
		intentDataFilter = new IntentFilter(MainActivity.DATA_ACTION);

		titleLight = (TextView) findViewById(R.id.type_of_data);
		lightValue = (TextView) findViewById(R.id.temperature);
		minLightValue = (TextView) findViewById(R.id.minValue);
		maxLightValue = (TextView) findViewById(R.id.maxValue);
		icon = (ImageView) findViewById(R.id.weather_icon);

		icon.setImageResource(R.drawable.light_bulb_icon);

		titleLight.setText("NATÊ¯ENIE OŒWIETLENIA");
		lightValue.setText(allDataResults.getCurrentValue()+unit);
		minLightValue.setText(allDataResults.getMinValue());
		maxLightValue.setText(allDataResults.getMaxValue());

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

					lightHandler.post(new Runnable() {

						@Override
						public void run() {
							lightValue.setText(allDataResults.getCurrentValue()+unit);
							minLightValue.setText(allDataResults.getMinValue());
							maxLightValue.setText(allDataResults.getMaxValue());
							dateCalendar.setText(timeAndDate.getCurrentTimeAndData());
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

	public class LightDataReceiver extends BroadcastReceiver {
		private String messageTAG = "LIGHT";
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