// https://ernienotes.wordpress.com/2010/12/27/creating-a-java-swing-range-slider/
//package slider;

package dnd;

import javax.swing.JSlider;

/**
 * An extension of JSlider to select a range of values using two thumb controls.
 * The thumb controls are used to select the lower and upper value of a range
 * with predetermined minimum and maximum values.
 * 
 * <p>Uses the default BoundedRangeModel, which
 * supports an inner range defined by a value and an extent.  The upper value
 * returned by TimeRangeSlider is simply the lower value plus the extent.</p>
 */
/**
 * Slider with independent lower and upper thumbs for a playback interval.
 */
public class TimeRangeSlider extends JSlider {

    /** Current playback position. Unlike the interval bounds, it is fractional. */
    private double playheadValue = 0.0;

    /**
     * Constructs a time-range slider with default bounds.
     * and 100.
     */
    public TimeRangeSlider() {
        initSlider();
    }

    /**
     * Constructs a time-range slider with the specified bounds.
     * values.
     */
    public TimeRangeSlider(int min, int max) {
        super(min, max);
        initSlider();
    }

    /**
     * Initializes the slider by setting default properties.
     */
    private void initSlider() {
        setOrientation(HORIZONTAL);
    }

    /**
     * Overrides the superclass method to install the UI delegate to draw two
     * thumbs.
     */
    @Override
    public void updateUI() {
        setUI(new TimeRangeSliderUI(this));
        // Update UI for slider labels.  This must be called after updating the
        // UI of the slider.  Refer to JSlider.updateUI().
        updateLabelUIs();
    }

    /**
     * Returns the lower value in the range.
     */
    @Override
    public int getValue() {
        return super.getValue();
    }

    /**
     * Sets the lower value in the range.
     */
    @Override
    public void setValue(int value) {
        int oldValue = getValue();
        if (oldValue == value) {
            return;
        }

        // Compute new value and extent to maintain upper value.
        int oldExtent = getExtent();
        int newValue = Math.min(Math.max(getMinimum(), value), oldValue + oldExtent);
        int newExtent = oldExtent + oldValue - newValue;

        // Set new value and extent, and fire a single change event.
        getModel().setRangeProperties(newValue, newExtent, getMinimum(), getMaximum(), getValueIsAdjusting());
    }

    /**
     * Returns the upper value in the range.
     */
    public int getUpperValue() {
        return getValue() + getExtent();
    }

    /**
     * Sets the upper value in the range.
     */
    public void setUpperValue(int value) {
        // Compute new extent.
        int lowerValue = getValue();
        int newExtent = Math.min(Math.max(0, value - lowerValue), getMaximum() - lowerValue);
        
        // Set extent to set upper value.
        setExtent(newExtent);
    }
    public double getPlayheadValue() {
        return playheadValue;
    }

    /**
     * Sets the current playback position without rounding it to whole seconds.
     */
    public void setPlayheadValue(double value) {
        double bounded = Math.max(getMinimum(), Math.min(getMaximum(), value));
        double oldValue = playheadValue;
        if (Double.compare(oldValue, bounded) == 0) return;
        playheadValue = bounded;
        firePropertyChange("playheadValue", oldValue, bounded);
        repaint();
    }
}
