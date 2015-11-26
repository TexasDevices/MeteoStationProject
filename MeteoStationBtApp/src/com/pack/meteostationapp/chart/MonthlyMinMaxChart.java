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

import java.sql.Struct;
import java.util.HashMap;

import org.achartengine.ChartFactory;
import org.achartengine.chart.BarChart.Type;
import org.achartengine.model.RangeCategorySeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.SimpleSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint.Align;

/**
 * Temperature demo range chart.
 */
public class MonthlyMinMaxChart extends AbstractBuildChart {

  public MonthlyMinMaxChart(String[] dataParameters,String description,double[] minValues,double[] maxValues,Integer[] yMinMax) {
	this.dataName=dataParameters[0];
	this.dataUnit=dataParameters[1];
	this.description=description;
	this.minValues= minValues;
	this.maxValues= maxValues;
	this.yMinMax=yMinMax;
	
  }
	
    private double[] minValues; 
    private double[] maxValues; 
	private String dataName; // name of the data, unit of the data
	private String dataUnit;
	private String description;
	 private Integer[] yMinMax; 
	 
	 private double gradientStart,gradientStop;
	 private int gradientStartColor,gradientStopColor;
	 
	//private HashMap<Integer, Color> gradientValues;
	
   /**
   * Returns the chart name.
   * 
   * @return the chart name
   */
	
  public  String getName() {
    return dataName;
  }

  /**
   * Returns the chart description.
   * 
   * @return the chart description
   */
  public String getDesc() {
    return description;
  }
  
  
  public void setGradientStart(double value,int c) {
	 gradientStart=value;
	 gradientStartColor=c;
	  
	  
  }
  public void setGradientStop(double value,int c) {
	  gradientStop=value;
		 gradientStopColor=c;
	  
  }
  
  
  

  /**
   * Executes the chart demo.
   * 
   * @param context the context
   * @return the built intent
   */
  public Intent execute(Context context) {

    XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
    RangeCategorySeries series = new RangeCategorySeries(dataName);
    int length = minValues.length;
    for (int k = 0; k < length; k++) {
      series.add(minValues[k], maxValues[k]);
    }
    dataset.addSeries(series.toXYSeries());
    int[] colors = new int[] { Color.CYAN };
    XYMultipleSeriesRenderer renderer = buildBarRenderer(colors);
    setChartSettings(renderer, getDesc(), "Miesi¹c", dataUnit, 0.5, 12.5,
        yMinMax[0], yMinMax[1], Color.GRAY, Color.LTGRAY);
    renderer.setBarSpacing(0.5);
    renderer.setXLabels(0);
    renderer.setYLabels(10);
    renderer.addXTextLabel(1, "Sty");
    renderer.addXTextLabel(2, "Lut");
    renderer.addXTextLabel(3, "Mar");
    renderer.addXTextLabel(4, "Kwi");
    renderer.addXTextLabel(5, "Maj");
    renderer.addXTextLabel(6, "Cze");
    renderer.addXTextLabel(7, "Lip");
    renderer.addXTextLabel(8, "Sie");
    renderer.addXTextLabel(9, "Wrz");
    renderer.addXTextLabel(10, "PaŸ");
    renderer.addXTextLabel(11, "Lis");
    renderer.addXTextLabel(12, "Dec");
    renderer.setMargins(new int[] {30, 70, 10, 0});
    renderer.setYLabelsAlign(Align.RIGHT);
    SimpleSeriesRenderer r = renderer.getSeriesRendererAt(0);
    r.setDisplayChartValues(true);
    r.setChartValuesTextSize(12);
    r.setChartValuesSpacing(3);
    r.setGradientEnabled(true);
    r.setGradientStart(gradientStart,  gradientStartColor);
    r.setGradientStop( gradientStop,  gradientStopColor);
    return ChartFactory.getRangeBarChartIntent(context, dataset, renderer, Type.DEFAULT,
    		getDesc());
  }

}
