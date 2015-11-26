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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.achartengine.ChartFactory;
import org.achartengine.chart.PointStyle;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;
import org.achartengine.util.MathHelper;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.hardware.SensorEvent;

/**
 * Temperature sensor demo chart.
 */
public class SensorValuesChart extends AbstractBuildChart {
  private static final long HOUR = 3600 * 1000;

  private static final long DAY = HOUR * 24;

  private static final int HOURS = 24;


  public SensorValuesChart(String name,String description,double[] firstSensorData,double[] secondSensorData, Integer[] yMinMax ){
	  
	this.name = name;
	this.description = description;
	this.firstSensorData= firstSensorData;
	this.secondSensorData= secondSensorData;
	this.yMinMax=yMinMax;
	  
  }
  
  private String name;
  private String description;
  private double[] firstSensorData;  //TODO - checking if data is correct
  private double[] secondSensorData;
  private Integer[] yMinMax; 
  
  
  /**
   * Returns the chart name.
   * 
   * @return the chart name
   */
  public String getName() {
    return name;
  }

  /**
   * Returns the chart description.
   * 
   * @return the chart description
   */
  public String getDesc() {
    return description;
  }

  /**
   * Executes the chart demo.
   * 
   * @param context the context
   * @return the built intent
   */
  public Intent execute(Context context) {
    String[] titles = new String[] { "1 czujnik", "2 czujnik" };
    long now = Math.round(new Date().getTime() / DAY) * DAY;
    List<Date[]> x = new ArrayList<Date[]>();
    for (int i = 0; i < titles.length; i++) {
      Date[] dates = new Date[HOURS];
      for (int j = 0; j < HOURS; j++) {
        dates[j] = new Date(now - (HOURS - j) * HOUR);
      }
      x.add(dates);
    }
    List<double[]> values = new ArrayList<double[]>();

    values.add(firstSensorData);
    values.add(secondSensorData);

    int[] colors = new int[] { Color.GREEN, Color.BLUE };
    PointStyle[] styles = new PointStyle[] { PointStyle.CIRCLE, PointStyle.DIAMOND };
    XYMultipleSeriesRenderer renderer = buildRenderer(colors, styles);
    int length = renderer.getSeriesRendererCount();
    for (int i = 0; i < length; i++) {
      ((XYSeriesRenderer) renderer.getSeriesRendererAt(i)).setFillPoints(true);
    }
    setChartSettings(renderer, getDesc(), "Godzina", "Stopnie Celsjusza [°C]",
        x.get(0)[0].getTime(), x.get(0)[HOURS - 1].getTime(), yMinMax[0], yMinMax[1], Color.LTGRAY, Color.LTGRAY);
    renderer.setXLabels(10);
    renderer.setYLabels(10);
    renderer.setShowGrid(true);
    renderer.setXLabelsAlign(Align.CENTER);
    renderer.setYLabelsAlign(Align.RIGHT);
    Intent intent = ChartFactory.getTimeChartIntent(context, buildDateDataset(titles, x, values),
        renderer, "h:mm a");
    return intent;
  }

}
