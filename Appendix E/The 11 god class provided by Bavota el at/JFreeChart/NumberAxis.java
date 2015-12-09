/* =======================================
 * JFreeChart : a Java Chart Class Library
 * =======================================
 *
 * Project Info:  http://www.jrefinery.com/jfreechart;
 * Project Lead:  David Gilbert (david.gilbert@jrefinery.com);
 *
 * (C) Copyright 2000, 2001, Simba Management Limited and Contributors;
 *
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation;
 * either version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * library; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307, USA.
 *
 * ---------------
 * NumberAxis.java
 * ---------------
 * (C) Copyright 2000, 2001, Simba Management Limited and Contributors;
 *
 * Original Author:  David Gilbert;
 * Contributor(s):   Laurence Vanhelsuwe;
 *
 * $Id: NumberAxis.java,v 1.8 2001/12/10 14:07:48 mungady Exp $
 *
 * Changes (from 18-Sep-2001)
 * --------------------------
 * 18-Sep-2001 : Added standard header and fixed DOS encoding problem (DG);
 * 22-Sep-2001 : Changed setMinimumAxisValue(...) and setMaximumAxisValue(...) so that they
 *               clear the autoRange flag (DG);
 * 27-Nov-2001 : Removed old, redundant code (DG);
 * 30-Nov-2001 : Added accessor methods for the standard tick units (DG);
 *
 */

package com.jrefinery.chart;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.text.*;
import javax.swing.*;
import com.jrefinery.chart.event.*;

/**
 * The base class for axes that display numerical data.
 * <P>
 * The 'auto tick value' mechanism is an adaptation of code suggested by Laurence Vanhelsuwe
 * (see LV's online book "Mastering JavaBeans" at http:www.lv.clara.co.uk/masbeans.html).
 * @see HorizontalNumberAxis
 * @see VerticalNumberAxis
 */
public abstract class NumberAxis extends ValueAxis {

    /** The default minimum axis value. */
    public static final Number DEFAULT_MINIMUM_AXIS_VALUE = new Double(0.0);

    /** The default maximum axis value. */
    public static final Number DEFAULT_MAXIMUM_AXIS_VALUE = new Double(1.0);

    /** The default value for the upper margin. */
    public static final double DEFAULT_UPPER_MARGIN = 0.05;

    /** The default value for the lower margin. */
    public static final double DEFAULT_LOWER_MARGIN = 0.05;

    /** The default minimum auto range. */
    public static final Number DEFAULT_MINIMUM_AUTO_RANGE = new Double(0.0000001);

    /** The default tick unit. */
    public static final NumberTickUnit DEFAULT_TICK_UNIT
                                       = new NumberTickUnit(new Double(1.0),
                                                            new DecimalFormat("0"));

    /** The lowest value showing on the axis. */
    protected Number minimumAxisValue;

    /** The highest value showing on the axis. */
    protected Number maximumAxisValue;

    /** A flag that indicates whether or not zero *must* be included when automatically determining
     * the axis range. */
    protected boolean autoRangeIncludesZero;

    /** The minimum size of a range that is determined automatically. */
    protected Number autoRangeMinimumSize;

    /** The upper margin.  This is a percentage that indicates the amount by which the maximum
        axis value exceeds the maximum data value when the axis range is determined
        automatically. */
    protected double upperMargin;

    /** The lower margin.  This is a percentage that indicates the amount by which the minimum
        axis value is "less than" the minimum data value when the axis range is determined
        automatically. */
    protected double lowerMargin;

    /** The tick unit for the axis. */
    protected NumberTickUnit tickUnit;

    /** The standard tick units for the axis. */
    protected TickUnits standardTickUnits;


    /**
     * Returns the minimum value for the axis.
     * @return The minimum value for the axis.
     */
    public Number getMinimumAxisValue() {
	return minimumAxisValue;
    }

    /**
     * Sets the minimum value for the axis.
     * <P>
     * Registered listeners are notified that the axis has been modified.
     * @param value The new minimum.
     */
    public void setMinimumAxisValue(Number value) {

        // check argument...
        if (value==null) {
            throw new IllegalArgumentException("NumberAxis.setMinimumAxisValue(Number): "
                                               +"null not permitted.");
        }

        // make the change...
	if (!value.equals(this.minimumAxisValue)) {
	    this.minimumAxisValue = value;
            this.autoRange = false;
            notifyListeners(new AxisChangeEvent(this));
        }

    }

    /**
     * Returns the maximum value for the axis.
     */
    public Number getMaximumAxisValue() {
	return maximumAxisValue;
    }

    /**
     * Sets the maximum value for the axis.
     * <P>
     * Registered listeners are notified that the axis has been modified.
     * @param value The new maximum.
     */
    public void setMaximumAxisValue(Number value) {

        // check argument...
        if (value==null) {
            throw new IllegalArgumentException("NumberAxis.setMinimumAxisValue(Number): "
                                               +"null not permitted.");
        }

        // make the change...
	if (!value.equals(this.maximumAxisValue)) {
	    this.maximumAxisValue = value;
            this.autoRange = false;
            notifyListeners(new AxisChangeEvent(this));
        }

    }

    /**
     * Returns the flag that indicates whether or not the automatic axis range (if indeed it is
     * determined automatically) is forced to include zero.
     */
    public boolean autoRangeIncludesZero() {
	return this.autoRangeIncludesZero;
    }

    /**
     * Sets the flag that indicates whether or not the automatic axis range is forced to include
     * zero.
     * @param flag The new value of the flag;
     */
    public void setAutoRangeIncludesZero(boolean flag) {
	if (autoRangeIncludesZero!=flag) {
	    this.autoRangeIncludesZero = flag;
	    notifyListeners(new AxisChangeEvent(this));
	}
    }

    /**
     * Returns the minimum size of the automatic axis range (if indeed it is determined
     * automatically).
     */
    public Number getAutoRangeMinimumSize() {
	return this.autoRangeMinimumSize;
    }

    /**
     * Sets the minimum size of the automatic axis range.
     * @param minimum The new minimum.
     */
    public void setAutoRangeMinimumSize(Number size) {

        // check argument...
        if (size==null) {
            throw new IllegalArgumentException("NumberAxis.setAutoRangeMinimumSize(Number): "
                                               +"null not permitted.");
        }

        // make the change...
	if (autoRangeMinimumSize.doubleValue()!=size.doubleValue()) {
	    this.autoRangeMinimumSize = size;
	    notifyListeners(new AxisChangeEvent(this));
	}

    }

    /**
     * Returns the margin (as a percentage of the range) by which the maximum axis value exceeds
     * the maximum data value.
     */
    public double getUpperMargin() {
        return this.upperMargin;
    }

    /**
     * Sets the upper margin.
     * @param margin The new margin;
     */
    public void setUpperMargin(double margin) {
        this.upperMargin = margin;
        notifyListeners(new AxisChangeEvent(this));
    }

    /**
     * Returns the margin (as a percentage of the range) by which the minimum axis value is less
     * than the minimum data value.
     */
    public double getLowerMargin() {
        return this.lowerMargin;
    }

    /**
     * Sets the lower margin.
     * @param margin The new margin;
     */
    public void setLowerMargin(double margin) {
        this.lowerMargin = margin;
        notifyListeners(new AxisChangeEvent(this));
    }

    /**
     * Returns the standard tick units for the axis.
     * <P>
     * If autoTickUnitSelection is on, the tick unit for the axis will be automatically selected
     * from this collection.
     */
    public TickUnits getStandardTickUnits() {
        return this.standardTickUnits;
    }

    /**
     * Sets the standard tick units for the axis.
     * @param units The tick units.
     */
    public void setStandardTickUnits(TickUnits units) {
        this.standardTickUnits = units;
        notifyListeners(new AxisChangeEvent(this));
    }

    /**
     * Returns the tick unit for the axis.
     * @return The tick unit for the axis.
     */
    public NumberTickUnit getTickUnit() {
        return this.tickUnit;
    }

    /**
     * Converts a value to a string, using the current format for the tick labels on the axis.
     */
    public String valueToString(Number value) {

        // is there an override format?
        return this.tickUnit.formatter.format(value.doubleValue());

    }


    /**
     * Calculates the value of the lowest visible tick on the axis.
     * @return The value of the lowest visible tick on the axis.
     */
    public double calculateLowestVisibleTickValue() {

	double min = minimumAxisValue.doubleValue();
	double unit = getTickUnit().getValue().doubleValue();
	double index = Math.ceil(min/unit);
	return index*unit;

    }

    /**
     * Calculates the value of the highest visible tick on the axis.
     * @return The value of the highest visible tick on the axis.
     */
    public double calculateHighestVisibleTickValue() {

	double max = maximumAxisValue.doubleValue();
	double unit = getTickUnit().getValue().doubleValue();
	double index = Math.floor(max/unit);
	return index*unit;

    }

    /**
     * Calculates the number of visible ticks.
     * @return The number of visible ticks on the axis.
     */
    public int calculateVisibleTickCount() {

	double low = minimumAxisValue.doubleValue();
	double high = maximumAxisValue.doubleValue();
	double unit = getTickUnit().getValue().doubleValue();
	return (int)(Math.floor(high/unit)-Math.ceil(low/unit)+1);

    }

    /**
     * Creates the standard tick units.
     * <P>
     * If you don't like these defaults, create your own instance of TickUnits and then pass it to
     * the setStandardTickUnits(...) method.
     */
    private TickUnits createStandardTickUnits() {

        TickUnits units = new TickUnits();

        units.add(new NumberTickUnit(new Double(0.0000001),  new DecimalFormat("0.0000000")));
        units.add(new NumberTickUnit(new Double(0.000001),   new DecimalFormat("0.000000")));
        units.add(new NumberTickUnit(new Double(0.00001),    new DecimalFormat("0.00000")));
        units.add(new NumberTickUnit(new Double(0.0001),     new DecimalFormat("0.0000")));
        units.add(new NumberTickUnit(new Double(0.001),      new DecimalFormat("0.000")));
        units.add(new NumberTickUnit(new Double(0.01),       new DecimalFormat("0.00")));
        units.add(new NumberTickUnit(new Double(0.1),        new DecimalFormat("0.0")));
        units.add(new NumberTickUnit(new Long(1L),           new DecimalFormat("0")));
        units.add(new NumberTickUnit(new Long(10L),          new DecimalFormat("0")));
        units.add(new NumberTickUnit(new Long(100L),         new DecimalFormat("0")));
        units.add(new NumberTickUnit(new Long(1000L),        new DecimalFormat("#,##0")));
        units.add(new NumberTickUnit(new Long(10000L),       new DecimalFormat("#,##0")));
        units.add(new NumberTickUnit(new Long(100000L),      new DecimalFormat("#,##0")));
        units.add(new NumberTickUnit(new Long(1000000L),     new DecimalFormat("#,###,##0")));
        units.add(new NumberTickUnit(new Long(10000000L),    new DecimalFormat("#,###,##0")));
        units.add(new NumberTickUnit(new Long(100000000L),   new DecimalFormat("#,###,##0")));
        units.add(new NumberTickUnit(new Long(1000000000L),  new DecimalFormat("#,###,###,##0")));

        units.add(new NumberTickUnit(new Double(0.00000025), new DecimalFormat("0.00000000")));
        units.add(new NumberTickUnit(new Double(0.0000025),  new DecimalFormat("0.0000000")));
        units.add(new NumberTickUnit(new Double(0.000025),   new DecimalFormat("0.000000")));
        units.add(new NumberTickUnit(new Double(0.00025),    new DecimalFormat("0.00000")));
        units.add(new NumberTickUnit(new Double(0.0025),     new DecimalFormat("0.0000")));
        units.add(new NumberTickUnit(new Double(0.025),      new DecimalFormat("0.000")));
        units.add(new NumberTickUnit(new Double(0.25),       new DecimalFormat("0.00")));
        units.add(new NumberTickUnit(new Double(2.5),        new DecimalFormat("0.0")));
        units.add(new NumberTickUnit(new Long(25L),          new DecimalFormat("0")));
        units.add(new NumberTickUnit(new Long(250L),         new DecimalFormat("0")));
        units.add(new NumberTickUnit(new Long(2500L),        new DecimalFormat("#,##0")));
        units.add(new NumberTickUnit(new Long(25000L),       new DecimalFormat("#,##0")));
        units.add(new NumberTickUnit(new Long(250000L),      new DecimalFormat("#,##0")));
        units.add(new NumberTickUnit(new Long(2500000L),     new DecimalFormat("#,###,##0")));
        units.add(new NumberTickUnit(new Long(25000000L),    new DecimalFormat("#,###,##0")));
        units.add(new NumberTickUnit(new Long(250000000L),   new DecimalFormat("#,###,##0")));
        units.add(new NumberTickUnit(new Long(2500000000L),  new DecimalFormat("#,###,###,##0")));

        units.add(new NumberTickUnit(new Double(0.0000005),  new DecimalFormat("0.0000000")));
        units.add(new NumberTickUnit(new Double(0.000005),   new DecimalFormat("0.000000")));
        units.add(new NumberTickUnit(new Double(0.00005),    new DecimalFormat("0.00000")));
        units.add(new NumberTickUnit(new Double(0.0005),     new DecimalFormat("0.0000")));
        units.add(new NumberTickUnit(new Double(0.005),      new DecimalFormat("0.000")));
        units.add(new NumberTickUnit(new Double(0.05),       new DecimalFormat("0.00")));
        units.add(new NumberTickUnit(new Double(0.5),        new DecimalFormat("0.0")));
        units.add(new NumberTickUnit(new Long(5L),           new DecimalFormat("0")));
        units.add(new NumberTickUnit(new Long(50L),          new DecimalFormat("0")));
        units.add(new NumberTickUnit(new Long(500L),         new DecimalFormat("0")));
        units.add(new NumberTickUnit(new Long(5000L),        new DecimalFormat("#,##0")));
        units.add(new NumberTickUnit(new Long(50000L),       new DecimalFormat("#,##0")));
        units.add(new NumberTickUnit(new Long(500000L),      new DecimalFormat("#,##0")));
        units.add(new NumberTickUnit(new Long(5000000L),     new DecimalFormat("#,###,##0")));
        units.add(new NumberTickUnit(new Long(50000000L),    new DecimalFormat("#,###,##0")));
        units.add(new NumberTickUnit(new Long(500000000L),   new DecimalFormat("#,###,##0")));
        units.add(new NumberTickUnit(new Long(5000000000L),  new DecimalFormat("#,###,###,##0")));

        return units;

    }

}
