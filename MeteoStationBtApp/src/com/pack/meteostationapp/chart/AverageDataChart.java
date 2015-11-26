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
import java.util.List;

import org.achartengine.ChartFactory;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint.Align;

/**
 * Average temperature demo chart.
 */
public class AverageDataChart extends AbstractBuildChart {
	
	
	
	 public AverageDataChart(String dataName,String description,double[] averageData,Integer[] yMinMax ){
		  
			this.dataName = dataName;
			this.description = description;
			this.averageData= averageData;
			this.yMinMax=yMinMax;
			  
		  }
		  
		  private String dataName;
		  private String description;
		  private double[] averageData;  //TODO - checking if data is correct
		  private Integer[] yMinMax; 
	
	
	
  /**
   * Returns the chart name.
   * 
   * @return the chart name
   */
  public String getName() {
    return "Œrednia wartoœæ: "+dataName;
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
    String[] titles = new String[] { getName() };
    List<double[]> x = new ArrayList<double[]>();
    for (int i = 0; i < titles.length; i++) {
      x.add(new double[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12 });
    }
    List<double[]> values = new ArrayList<double[]>();
    values.add(averageData);
    int[] colors = new int[] { Color.YELLOW };
    PointStyle[] styles = new PointStyle[] { PointStyle.CIRCLE };
    XYMultipleSeriesRenderer renderer = buildRenderer(colors, styles);
    int length = renderer.getSeriesRendererCount();
    for (int i = 0; i < length; i++) {
      ((XYSeriesRenderer) renderer.getSeriesRendererAt(i)).setFillPoints(true);
    }
    setChartSettings(renderer, getName(), "Miesi¹c", dataName, 0.5, 12.5, yMinMax[0], yMinMax[1],
        Color.LTGRAY, Color.LTGRAY);
    renderer.setXLabels(0);
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
    renderer.setYLabels(10);
    renderer.setShowGrid(true);
    renderer.setXLabelsAlign(Align.RIGHT);
    renderer.setYLabelsAlign(Align.RIGHT);
    renderer.setZoomButtonsVisible(true);
    renderer.setPanLimits(new double[] { -10, 20, -10, 40 });
    renderer.setZoomLimits(new double[] { -10, 20, -10, 40 });

    XYMultipleSeriesDataset dataset = buildDataset(titles, x, values);
    Intent intent = ChartFactory.getLineChartIntent(context, dataset, renderer,
        getDesc());
    return intent;
  }

}
