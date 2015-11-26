/**
 * Copyright (C) 2009 - 2013 SC 4ViewSoft SRL
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.pack.meteostationapp.chart;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.CategorySeries;
import org.achartengine.renderer.DialRenderer;
import org.achartengine.renderer.SimpleSeriesRenderer;
import org.achartengine.renderer.DialRenderer.Type;

import com.pack.meteostationapp.MainActivity;
import com.pack.meteostationapp.PressureActivity;
import com.pack.meteostationapp.R;
import com.pack.meteostationapp.TempActivity;

import android.R.bool;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Budget demo pie chart.
 */
public class CurrentDialChart extends Activity {

	private final String nameOfMeasuredValue1 = "Pr¹d ³adowania baterii";

	private final String nameOfMeasuredValue2 = "Napiêcie wyjœciowe";

	// chart containers
	private GraphicalView mChartView;
	private LinearLayout layout;
	private MyDataReceiver myData;
	private IntentFilter intentDataFilter;

	private String response = "0";
	private String currentData = "0";
	private String voltageData = "0";

	private CategorySeries category;
	private DialRenderer renderer;
	private SimpleSeriesRenderer r;

	private Boolean currentOrVoltageChartIsEnabled = false; // false for a
															// current

	private Handler currentSensorHandler = new Handler();
	private Thread receiverDataThread;

	private TextView chargerCurrent, chargerVoltage;
	private Button selectChartVorAButton, backButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.current_time_gauge);

		layout = (LinearLayout) findViewById(R.id.chartCurrent);
		// rmsData = "0";
		myData = new MyDataReceiver();
		intentDataFilter = new IntentFilter(MainActivity.DATA_ACTION); // for
																		// broadcast
																		// receiver
		currentSensorHandler = new Handler();

		chargerCurrent = (TextView) findViewById(R.id.currentValue);
		chargerVoltage = (TextView) findViewById(R.id.voltageValue);
		chargerCurrent.setText("0");
		chargerVoltage.setText("0");

		selectChartVorAButton = (Button) findViewById(R.id.VorAButton);
		selectChartVorAButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				if (v.getId() == R.id.VorAButton) {

					layout.removeView(mChartView);

					if (currentOrVoltageChartIsEnabled) {

						r.setColor(Color.rgb(120, 150, 70));
						category.set(0, nameOfMeasuredValue1,
								Double.parseDouble(currentData));
						renderer.setMajorTicksSpacing(100);
						renderer.setMinorTicksSpacing(25);
						renderer.setVisualTypes(new DialRenderer.Type[] {
								Type.ARROW, Type.NEEDLE, Type.NEEDLE });
						renderer.setMinValue(0);
						renderer.setMaxValue(1000);
						layout.addView(mChartView);
						mChartView.repaint();
						currentOrVoltageChartIsEnabled = false;
					}

					else {
						r.setColor(Color.BLUE);
						category.set(0, nameOfMeasuredValue2,
								Double.parseDouble(voltageData));
						renderer.setMajorTicksSpacing(1);
						renderer.setMinorTicksSpacing(0.25);
						renderer.setVisualTypes(new DialRenderer.Type[] {
								Type.NEEDLE, Type.NEEDLE, Type.NEEDLE });
						renderer.setMinValue(0);
						renderer.setMaxValue(10);
						layout.addView(mChartView);
						mChartView.repaint();
						currentOrVoltageChartIsEnabled = true;
					}
				}
			}
		});

		backButton = (Button) findViewById(R.id.back);

		backButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (v.getId() == R.id.back) {
					Intent backIntent = new Intent(CurrentDialChart.this,
							TempActivity.class);
					startActivity(backIntent);
					finish();
				}
			}
		});

		category = new CategorySeries(nameOfMeasuredValue1);
		category.add(nameOfMeasuredValue1, 0);
		renderer = new DialRenderer();
		renderer.setChartTitleTextSize(50);
		renderer.setLegendTextSize(25);
		renderer.setMargins(new int[] { 10, 10, 10, 0 });

		r = new SimpleSeriesRenderer();
		r.setColor(Color.rgb(120, 150, 70));
		renderer.addSeriesRenderer(r);

		renderer.setLabelsTextSize(18);
		renderer.setLabelsColor(Color.WHITE);
		renderer.setAntialiasing(true);
		renderer.setMajorTicksSpacing(100);
		renderer.setMinorTicksSpacing(25);
		renderer.setZoomButtonsVisible(true);
		renderer.setZoomEnabled(true);
		renderer.setZoomRate(90);
		renderer.setShowLabels(true);
		renderer.setVisualTypes(new DialRenderer.Type[] { Type.ARROW,
				Type.NEEDLE, Type.NEEDLE });
		renderer.setMinValue(0);
		renderer.setMaxValue(1000);

		mChartView = ChartFactory.getDialChartView(this, category, renderer);
		layout.addView(mChartView);

		receiverDataThread = new Thread(new Runnable() {
			Double currentValue, voltageValue;

			@Override
			public void run() {
				while (true) {

					try {
						Thread.sleep(800);

					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					currentSensorHandler.post(new Runnable() {

						@Override
						public void run() {
							if (!currentOrVoltageChartIsEnabled) {
								currentValue = Double.parseDouble(currentData);
								category.set(0, nameOfMeasuredValue1,
										currentValue);
							} else {
								voltageValue = Double.parseDouble(voltageData);
								category.set(0, nameOfMeasuredValue2,
										voltageValue);
							}
							chargerCurrent.setText(currentData);
							chargerVoltage.setText(voltageData);

							mChartView.repaint();

						}
					});

				}

			}
		});

		if (!receiverDataThread.isAlive()) {
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
		super.onResume();
		unregisterReceiver(myData);
	}

	public class MyDataReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent i) {
			response = i.getStringExtra("CURRENT");
			if (response != null) {
				currentData = response;
			}
			response = i.getStringExtra("VOLTAGE");
			if (response != null) {
				voltageData = response;
			}
			// abortBroadcast(); for sending ordererd broadcasts
		}
	}

}
