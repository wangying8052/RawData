/* =======================================
 * JFreeChart : a Java Chart Class Library
 * =======================================
 * Version:         0.5.6;
 * Project Lead:    David Gilbert (david.gilbert@bigfoot.com);
 *
 * File:            JFreeChart.java
 * Author:          David Gilbert;
 * Contributor(s):  Andrzej Porebski, David Li;
 *
 * (C) Copyright 2000, Simba Management Limited;
 *
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation;
 * either version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this library;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307, USA.
 *
 * $Id: JFreeChart.java,v 1.3.2.4 2000/11/27 23:24:54 dgilbert Exp $
 */

package com.jrefinery.chart;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.text.*;
import java.util.*;
import javax.swing.*;
import com.jrefinery.chart.event.*;

/**
 * A chart class implemented using the Java 2D APIs.  The current version supports bar charts,
 * line charts, pie charts and xy plots (including time series data).
 * <P>
 * JFreeChart coordinates several objects to achieve its aim of being able to draw a chart
 * on a Java 2D graphics device: a Title, a Legend, a Plot and a DataSource (the Plot in turn
 * manages a horizontal axis and a vertical axis).
 * <P>
 * You should use JFreeChartPanel to display a chart in a GUI.
 * @see JFreeChartPanel
 * @see Title
 * @see Legend
 * @see Plot
 * @see DataSource
 */
public class JFreeChart implements DataSourceChangeListener,
                                   TitleChangeListener,
                                   LegendChangeListener,
                                   PlotChangeListener {

  /** The chart title. */
  protected Title title;

  /** The chart legend. */
  protected Legend legend;

  /** The source of the data to be displayed in the chart. */
  protected DataSource data;

  /** Draws the visual representation of the data. */
  protected Plot plot;

  /** Flag that determines whether or not the chart is drawn with anti-aliasing. */
  protected boolean antialias;

  /** Paint used to draw the background of the chart. */
  protected Paint chartBackgroundPaint;

  /** Paint objects used to color each series in the chart. */
  protected Paint[] seriesPaint;

  /** Stroke objects used to draw each series in the chart. */
  protected Stroke[] seriesStroke;

  /** Paint objects used to draw the outline of each series in the chart. */
  protected Paint[] seriesOutlinePaint;

  /** Stroke objects used to draw the outline of each series in the chart. */
  protected Stroke[] seriesOutlineStroke;

  /** Storage for registered change listeners. */
  protected java.util.List listeners;


  /**
   * Returns the chart title.
   */
  public Title getTitle() {
    return title;
  }




  /**
   * Returns the current chart legend (possibly null);
   */
  public Legend getLegend() {
    return legend;
  }

  /**
   * Sets the chart legend, and notifies registered listeners that the chart has been modified.
   * @param legend The new chart legend (can be null);
   */
  public void setLegend(Legend legend) {
    this.legend = legend;
    if (legend!=null) {
      legend.addChangeListener(this);
    }
    fireChartChanged();
  }

  /**
   * Returns the current plot.
   */
  public Plot getPlot() {
    return this.plot;
  }


  /**
   * Returns the current status of the anti-alias flag;
   */
  public boolean getAntiAlias() {
    return antialias;
  }

  /**
   * Sets antialiasing on or off.
   */
  public void setAntiAlias(boolean flag) {
    this.antialias = flag;
    fireChartChanged();
  }

  /**
   * Returns the Paint used to fill the chart background.
   */
  public Paint getChartBackgroundPaint() {
    return chartBackgroundPaint;
  }

  /**
   * Sets the Paint used to fill the chart background, and notifies registered listeners that the
   * chart has been modified.
   * @param paint The new background paint;
   */
  public void setChartBackgroundPaint(Paint paint) {
    this.chartBackgroundPaint = paint;
    fireChartChanged();
  }

  /**
   * Sets the paint used to color any shapes representing series, and notifies registered
   * listeners that the chart has been modified.
   * @param paint An array of Paint objects used to color series;
   */
  public void setSeriesPaint(Paint[] paint) {
    this.seriesPaint = paint;
    fireChartChanged();
  }

  /**
   * Sets the stroke used to draw any shapes representing series, and notifies registered
   * listeners that the chart has been modified.
   * @param stroke An array of Stroke objects used to draw series;
   */
  public void setSeriesStroke(Stroke[] stroke) {
    this.seriesStroke = stroke;
    fireChartChanged();
  }

  /**
   * Sets the paint used to outline any shapes representing series, and notifies registered
   * listeners that the chart has been modified.
   * @param paint An array of Paint objects for drawing the outline of series shapes;
   */
  public void setSeriesOutlinePaint(Paint[] paint) {
    this.seriesOutlinePaint = paint;
    fireChartChanged();
  }

  /**
   * Sets the stroke used to draw any shapes representing series, and notifies registered
   * listeners that the chart has been modified.
   * @param stroke An array of Stroke objects;
   */
  public void setSeriesOutlineStroke(Stroke[] stroke) {
    this.seriesOutlineStroke = stroke;
    fireChartChanged();
  }

  /**
   * Returns the Paint used to color any shapes for the specified series.
   * @param index The index of the series of interest (zero-based);
   */
  public Paint getSeriesPaint(int index) {
    return seriesPaint[index % seriesPaint.length];
  }

  /**
   * Returns the Stroke used to draw any shapes for the specified series.
   * @param index The index of the series of interest (zero-based);
   */
  public Stroke getSeriesStroke(int index) {
    return seriesStroke[index % seriesStroke.length];
  }

  /**
   * Returns the Paint used to outline any shapes for the specified series.
   * @param index The index of the series of interest (zero-based);
   */
  public Paint getSeriesOutlinePaint(int index) {
    return seriesOutlinePaint[index % seriesOutlinePaint.length];
  }

  /**
   * Returns the Stroke used to outline any shapes for the specified series.
   * @param index The index of the series of interest (zero-based);
   */
  public Stroke getSeriesOutlineStroke(int index) {
    return seriesOutlineStroke[index % seriesOutlinePaint.length];
  }

  /**
   * Draws the chart on a Java 2D graphics device (such as the screen or a printer).  This method
   * is the focus of the entire JFreeChart API.
   * @param g2 The graphics device;
   * @param chartArea The area within which the chart should be drawn;
   */
  public void draw(Graphics2D g2, Rectangle2D chartArea) {

    if (antialias) {
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    }
    else {
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
    }

    g2.setPaint(chartBackgroundPaint);
    g2.fill(chartArea);

    // draw the title
    Rectangle2D titleArea = null;
    if (title!=null) {
      titleArea = title.draw(g2, chartArea);
    }
    else {
      titleArea = new Rectangle2D.Double(chartArea.getX(), chartArea.getY(), 0, 0);
    }

    // calculate the non-title area - we assume that the title is using up the area at the top
    // of the chart area (because that's what StandardTitle currently does) but later we'll
    // have to test for TOP, BOTTOM, LEFT or RIGHT....
    Rectangle2D nonTitleArea = new Rectangle2D.Double(chartArea.getX(),
                                                      chartArea.getY()+titleArea.getHeight(),
                                                      chartArea.getWidth(),
                                                      chartArea.getHeight()-titleArea.getHeight());

    // draw the legend
    Rectangle2D legendArea = null;
    if (legend!=null) {
      legendArea = legend.draw(g2, nonTitleArea);
    }
    else {
      legendArea = new Rectangle2D.Double(0, 0, 0, 0);
    }

    // calculate the draw area - we assume that the legend is using up the area at the right
    // of the nonTitleArea (because that's what StandardLegend currently does) but later we'll
    // have to test for TOP, BOTTOM, LEFT or RIGHT...
    Rectangle2D drawArea = new Rectangle2D.Double(nonTitleArea.getX(),
                                                  nonTitleArea.getY(),
                                                  nonTitleArea.getWidth()-legendArea.getWidth(),
                                                  nonTitleArea.getHeight());

    // draw the plot (axes and data visualisation)
    plot.draw(g2, drawArea);

  }

  /**
   * Notifies all registered listeners that the chart has been modified.
   */
  public void fireChartChanged() {
    ChartChangeEvent event = new ChartChangeEvent(this);
    notifyListeners(event);
  }

  /**
   * Registers an object for notification of changes to the chart.
   * @param listener The object being registered;
   */
  public void addChangeListener(ChartChangeListener listener) {
    listeners.add(listener);
  }

  /**
   * Unregisters an object for notification of changes to the chart.
   * @param listener The object being unregistered.
   */
  public void removeChangeListener(ChartChangeListener listener) {
    listeners.remove(listener);
  }

  /**
   * Notifies all registered listeners that the chart has been modified.
   * @param event Contains information about the event that triggered the notification;
   */
  public void notifyListeners(ChartChangeEvent event) {
    Iterator iterator = listeners.iterator();
    while (iterator.hasNext()) {
      ChartChangeListener listener = (ChartChangeListener)iterator.next();
      listener.chartChanged(event);
    }
  }


  /**
   * Receives notification that the chart title has changed, and passes this on to registered
   * listeners.
   * @param event Information about the chart title change;
   */
  public void titleChanged(TitleChangeEvent event) {
    event.setChart(this);
    notifyListeners(event);
  }

  /**
   * Receives notification that the chart legend has changed, and passes this on to registered
   * listeners.
   * @param event Information about the chart legend change;
   */
  public void legendChanged(LegendChangeEvent event) {
    event.setChart(this);
    notifyListeners(event);
  }

  /**
   * Receives notification that the plot has changed, and passes this on to registered listeners.
   * @param event Information about the plot change;
   */
  public void plotChanged(PlotChangeEvent event) {
    event.setChart(this);
    notifyListeners(event);
  }
}

}