/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import java.util.*;
import snap.geom.Path2D;
import snap.geom.Point;
import snap.geom.Shape;
import snap.gfx.*;
import snap.props.PropSet;
import snap.util.*;

/**
 * This class has the behavior of a slider, without boundaries (so you can set values beyond the visible min and max).
 * Furthermore, it has a 3D thumbwheel look, that is particularly nice for radial values. It also has an optional linear
 * look, which is good for unbounded linear values.
 */
public class ThumbWheel extends View {

    // Value
    private double _value = 0;
    
    // Amount to round value to
    private double _round = 0;
    
    // Visible min/max
    private double _visibleMin = 0;
    private double _visibleMax = 100;
    
    // Absolute min/max
    private double _absoluteMin = -Float.MAX_VALUE;
    private double _absoluteMax = Float.MAX_VALUE;
    
    // Absolute mode
    private byte _absoluteMode = ABSOLUTE_BOUNDED;
    
    // The type of thumbwheel (radial or linear)
    private byte _type = TYPE_RADIAL;

    // Value to snap back to if snaps-back
    // How often to draw a dash (in points or degs)
    private int _dashInterval = 10;
    
    // Set this to NO if you want relative vals
    private boolean _showMainDash = true;

    // Mouse location at last press
    private Point _pressedMousePoint;
    
    // Value at last press
    private double _pressedValue;

    // Background of Thumbwheel in radial mode
    private Image _image;
    
    // Shared map of images
    private static Map<String,Image> _images = new HashMap<>();
    
    // Constants for type
    public static final byte TYPE_RADIAL = 0;
    public static final byte TYPE_LINEAR = 1;
    
    // Constants for absolute behavior
    public static final byte ABSOLUTE_BOUNDED = 0;
    public static final byte ABSOLUTE_WRAPPED = 1;

    // Constants for properties
    public static final String Min_Prop = "Min";
    public static final String Max_Prop = "Max";
    public static final String AbsMin_Prop = "AbsMin";
    public static final String AbsMax_Prop = "AbsMax";
    public static final String Round_Prop = "Round";
    public static final String Type_Prop = "Type";
    public static final String Value_Prop = "Value";

    /**
     * Creates a new thumbwheel.
     */
    public ThumbWheel()
    {
        super();
        setActionable(true);
        enableEvents(MousePress, MouseDrag, MouseRelease);
    }

    /**
     * Returns the type (radial or linear).
     */
    public byte getType()  { return _type; }

    /**
     * Sets the type (radial or linear).
     */
    public void setType(byte aType)
    {
        _type = aType;
        clearImage();
        repaint();
    }

    /**
     * Returns the value.
     */
    public double getValue()
    {
        return _round == 0 ? _value : MathUtils.round(_value, _round);
    }

    /**
     * Sets the value.
     */
    public void setValue(double aValue)
    {
        // Clamp or Wrap aValue wrt the absoluteMode
        if (aValue < getAbsoluteMin() || aValue > getAbsoluteMax()) {
            if (isBounded())
                aValue = MathUtils.clamp(aValue, _absoluteMin, _absoluteMax);
            else if (isWrapped())
                aValue = MathUtils.clamp_wrap(aValue, _absoluteMin, _absoluteMax);
        }

        // Set value, fire action and repaint
        _value = aValue;
        fireActionEvent(null);
        repaint();
    }

    /** Returns the value that thumbwheel values are rounded to. */
    public double getRound() { return _round; }

    /** Sets the value that thumbwheel values are rounded to. */
    public void setRound(double aValue)
    {
        if (aValue == _round) return;
        firePropChange(Round_Prop, _round, _round = aValue);
    }

    /** Returns the smallest value in the visible range (ie, on the left side) of the thumbhweel. */
    public double getVisibleMin() { return _visibleMin; }

    /** Sets the smallest value in the visible range (ie, on the left side) of the thumbhweel. */
    public void setVisibleMin(double aValue)
    {
        if (aValue == _visibleMin) return;
        firePropChange(Min_Prop, _visibleMin, _visibleMin = aValue);
    }

    /** Returns the largest value in the visible range (ie, on the right side) of the thumbhweel. */
    public double getVisibleMax() { return _visibleMax; }

    /** Sets the largest value in the visible range (ie, on the right side) of the thumbhweel. */
    public void setVisibleMax(double aValue)
    {
        if (aValue == _visibleMax) return;
        firePropChange(Max_Prop, _visibleMax, _visibleMax = aValue);
    }

    /** Returns the smallest value permitted by the thumbwheel (even when outside visible range). */
    public double getAbsoluteMin() { return _absoluteMin; }

    /** Sets the smallest value permitted by the thumbwheel (even when outside visible range). */
    public void setAbsoluteMin(double aValue)
    {
        if (aValue == _absoluteMin) return;
        firePropChange(AbsMin_Prop, _absoluteMin, _absoluteMin = aValue);
    }

    /** Returns the largest value permitted by the thumbwheel (even when outside visible range). */
    public double getAbsoluteMax() { return _absoluteMax; }

    /** Sets the largest value permitted by the thumbwheel (even when outside visible range). */
    public void setAbsoluteMax(double aValue)
    {
        if (aValue == _absoluteMax) return;
        firePropChange(AbsMax_Prop, _absoluteMax, _absoluteMax = aValue);
    }

    /** Returns the thumbhweel absolute mode (ABSOLUTE_BOUNDED or ABSOLUTE_WRAPPED). */
    public byte getAbsoluteMode() { return _absoluteMode; }

    /** Sets the thumbhweel absolute mode (ABSOLUTE_BOUNDED or ABSOLUTE_WRAPPED). */
    public void setAbsoluteMode(byte aValue) { _absoluteMode = aValue; }

    /**
     * Forwards mouse events to mouse methods.
     */
    protected void processEvent(ViewEvent anEvent)
    {
        // If disabled, just return
        if (!isEnabled()) return;

        // Handle MousePress: Record pressed mouse point and value
        if (anEvent.isMousePress()) {
            _pressedMousePoint = anEvent.getPoint();
            _pressedValue = getValue();
        }

        // Handle MouseDrag
        else if (anEvent.isMouseDrag()) {
            // Get values for last point and current point
            double lastPointVal = getValueAtPoint(_pressedMousePoint);
            double currPointVal = getValueAtPoint(anEvent.getPoint());

            // If bounded and we are already at absoluteMax and currentPoint is greater, return
            if (isBounded() && MathUtils.equals(getValue(), getAbsoluteMax()) && currPointVal>getAbsoluteMax()) return;

            // If bounded and we are already at absoluteMin and currPoint is less, return
            if (isBounded() && MathUtils.equals(getValue(), getAbsoluteMin()) && currPointVal<getAbsoluteMin()) return;

            // Set the float value relative to last point
            setValue(_pressedValue + (currPointVal - lastPointVal));
        }

        // Handle MouseRelease
        else if (anEvent.isMouseRelease()) {

            // Get values for last point and current point
            double lastPointVal = getValueAtPoint(_pressedMousePoint);
            double currPointVal = getValueAtPoint(anEvent.getPoint());

            // If bounded and we are already at absoluteMax and currentPoint is greater, return
            if (isBounded() && MathUtils.equals(_value, _absoluteMax) && currPointVal>_absoluteMax) return;

            // If bounded and we are already at absoluteMin and currPoint is less, return
            if (isBounded() && MathUtils.equals(_value, _absoluteMin) && currPointVal<_absoluteMin) return;

            // Set the float value relative to last point
            setValue(_pressedValue + (currPointVal - lastPointVal));
        }
    }

    /**
     * This method gives the value that corresponds to a point with respect to the given frame and the visible range.
     * When in radial mode, the point on the thumbwheel is approximated with a power series for arcCos to get legal values
     * for points outside of the frame.
     */
    public double getValueAtPoint(Point aPoint)
    {
        // Get stuff
        double pos = isVertical()? getHeight() - aPoint.y : aPoint.x;
        double width = isVertical()? getHeight() : getWidth();

        // If linear, just return linear extrapolation of point
        if (isLinear()) return getVisibleMin() + pos*getVisibleRange()/width;

        // Get radius
        double radius = width/2;
        double x = (radius - pos)/radius;

        // Get degrees by pwr series approximation of ArcCos (Pi/2 - x - x^3/6)
        double angle = Math.PI/2 - x - x*x*x/6;

        // Convert angle to thumbwheel coords
        return getVisibleMin() + angle*getVisibleRange()/Math.PI;
    }

    /**
     * Returns whether thumbwheel is radial.
     */
    public boolean isRadial() { return getType()==TYPE_RADIAL; }

    /**
     * Returns whether thumbwheel is linear.
     */
    public boolean isLinear() { return getType()==TYPE_LINEAR; }

    /**
     * Returns whether thumbwheel is absolute bounded.
     */
    public boolean isBounded() { return getAbsoluteMode()==ABSOLUTE_BOUNDED; }

    /**
     * Returns whether thumbwheel does absolute wrapping.
     */
    public boolean isWrapped() { return getAbsoluteMode()==ABSOLUTE_WRAPPED; }

    /**
     * Returns the extent of the thumbwheel's visible range.
     */
    public double getVisibleRange() { return getVisibleMax() - getVisibleMin(); }

    /**
     * Override to paint thumbwheel.
     */
    protected void paintFront(Painter aPntr)
    {
        // Bail if too small
        if (getWidth()<10 || getHeight()<10) return;

        // Get thumbwheel color
        Color color = getFillColor();

        // Draw linear background
        if (isLinear()) {
            aPntr.setColor(color);
            aPntr.fill3DRect(0, 0, getWidth(), getHeight(), false);
            aPntr.fillRect(2, 2, getWidth()-4, getHeight()-4);
        }

        // Otherwise draw radial background
        else {
            if (_image==null) _image = getBackImage();
            aPntr.drawImage(_image, 0, 0, getWidth(), getHeight());
        }

        // Get the userpath for the dashes
        Shape path = getThumbWheelDashes();

        // Draw dashes once for white part of groove
        if (isHorizontal()) aPntr.translate(1, 0);
        else aPntr.translate(0, 1);
        aPntr.setStroke(Stroke.getStrokeRound(1));

        // Get inset thumbwheel width/height
        double width = getWidth()-4;
        double height = getHeight()-4;

        // Draw linear white dashes
        if (isLinear()) { aPntr.setColor(color.brighter()); aPntr.draw(path); }

        // Break up radial white dashes to fade a little bit at ends
        else {
            aPntr.setColor(color); aPntr.draw(path);

            aPntr.save();
            if (isHorizontal()) aPntr.clipRect(width/4, 0, width/2, height);
            else aPntr.clipRect(0, height/4, width, height/2);
            aPntr.setColor(color.brighter()); aPntr.draw(path);
            aPntr.restore();
        }

        if (isHorizontal()) aPntr.translate(-1, 0);
        else aPntr.translate(0, -1);

        // Draw again for dark part of groove
        if (isLinear()) aPntr.setColor(color.darker());
        else aPntr.setColor(Color.BLACK);
        aPntr.draw(path);

        // If disabled then dim ThumbWheel out
        if (!isEnabled()) { aPntr.setColor(new Color(1d,1d,1,.5)); aPntr.fillRect(2,2,width,height); }
        //aPntr.setAntialiasing(old);
    }

    /**
     * Override to suppress.
     */
    protected void paintBack(Painter aPntr)  { }

    /**
     * Returns a Java2D shape for painting thumbwheel dashes.
     */
    private Shape getThumbWheelDashes()
    {
        double minX = 2;
        double minY = 2;
        double maxX = getWidth();
        double maxY = getHeight();
        double fwidth = maxX-4;
        double height = maxY-4;

        // Get dashInterval (in pnts or degs depending on display mode) and shift
        Path2D path = new Path2D();
        double length = isVertical()? height - 1f : fwidth;
        int dashInt = isLinear()? _dashInterval : (int)Math.round(360/(Math.PI*length/_dashInterval));
        int shift = getShift();

        // Calculate dash sizes
        double dashBase = isVertical()? minX : minY;
        double dashHeight = isVertical()? fwidth : height;
        double dashMinTop = dashBase + dashHeight*.25f, dashMajTop = dashBase + dashHeight*.5f, dashTop = dashBase + dashHeight;

        double base = isVertical()? minY : minX;
        double width = length, halfWidth = width/2;
        double mid = base + halfWidth, top = base + width;

        // Calculate whether first dash is a major one
        boolean isMajor = (shift >= 0) == isEven(shift / dashInt);

        // Calculate Linear dashes
        if (isLinear()) {

            // Set Main dash
            double mainDash = base + shift;

            // Calculate starting point and set the dashes
            double x = MathUtils.clamp_wrap(shift,0,dashInt);
            x = MathUtils.mod(x, ((shift>=0)? dashInt : 999999));
            x += base;

            if (isVertical()) while (x<top) {
                path.moveTo(dashBase, maxY - x);
                double value = isMajor? dashMajTop : dashMinTop;
                if (MathUtils.equals(x, mainDash) && _showMainDash) value = dashTop;
                path.lineTo(value, maxY - x);
                x += dashInt;
                isMajor = !isMajor;
            }

            else while (x<top) {
                path.moveTo(x, maxY - dashBase);
                double value = isMajor? dashMajTop : dashMinTop;
                if (MathUtils.equals(x, mainDash) && _showMainDash) value = dashTop;
                path.lineTo(x, maxY - value);
                x += dashInt;
                isMajor = !isMajor;
            }
        }

        // Calculate Radial Dashes
        else {

            // Inset dash size for beveled edges
            dashBase++;
            dashTop--;

            // Calc Main dash if we show it and it is in sight
            double mainDash = mid - Math.cos(shift*Math.PI/180f)*halfWidth;

            // Calculate the starting point and set the dashes
            double x = MathUtils.clamp_wrap(shift, 0, dashInt);
            x = MathUtils.mod(x, ((shift>=0)? dashInt : 999999));

            if (isVertical()) while (x<180) {
                double linDash = mid - Math.cos(x*Math.PI/180f)*halfWidth;
                path.moveTo(dashBase, linDash);

                // Check to see if this is a valid main dash
                double value = isMajor? dashMajTop : dashMinTop;
                if (isMajor && MathUtils.equals(linDash,mainDash) && _showMainDash && MathUtils.between(shift, 0, 180))
                    value = dashTop;

                path.lineTo(value, linDash);
                x += dashInt;
                isMajor = !isMajor;
            }

            else while (x<180) {
                double linDash = mid - Math.cos(x*Math.PI/180f)*halfWidth;
                path.moveTo(linDash, maxY - dashBase);

                // Check to see if this is a valid main dash
                double value = isMajor? dashMajTop : dashMinTop;
                if (isMajor && MathUtils.equals(linDash, mainDash) && _showMainDash && MathUtils.between(shift, 0, 180))
                    value = dashTop;

                path.lineTo(linDash, maxY - value);
                x += dashInt;
                isMajor = !isMajor;
            }
        }

        return path;
    }

    /**
     * Returns shift.
     */
    private int getShift()
    {
        // Handle linear
        if (isLinear()) {
            if (isHorizontal()) return (int)Math.round((_value - _visibleMin)/getVisibleRange()*getWidth() + .5f);
            return (int)Math.round((_value - _visibleMin)/getVisibleRange()*getHeight() +.5f);
        }

        // Handle radial
        return (int)Math.round((_value - _visibleMin)/getVisibleRange()*180f + .5f);
    }

    /**
     * Returns the background image if radial thumbwheel (keeps a cache based on orientation, size & color).
     */
    private Image getBackImage()
    {
        // Get the thumbwheel color
        Color color = getFillColor();

        // Get name for image and try to find new image (return if already created/cached)
        int imageW = (int) Math.round(getWidth());
        int imageH = (int) Math.round(getHeight());
        String imageName = (isVertical() ? "V" : "H") + imageW + "x" + imageH + "_" + color.getRGB();
        Image image = _images.get(imageName); if (image != null) return image;

        // Get new image and put in cache, then draw button background
        image = Image.getImageForSize(imageW, imageH, false);
        Painter pntr = image.getPainter();

        // Draw button background
        pntr.setColor(color);
        pntr.fill3DRect(0, 0, imageW, imageH, false);

        // If Horizontal: Draw top 2 points brighter, middle normal and bottom darker
        if (isHorizontal()) {
            drawGradient(pntr, 2, 2, imageW-4, 2, color.brighter());
            drawGradient(pntr, 2, 4, imageW-4, imageH-8, color);
            drawGradient(pntr, 2, imageH-4, imageW-4, 2, color.darker());
        }

        // If Vertical: Draw left 2 points brighter, middle normal and right darker
        else {
            drawGradient(pntr, 2, 2, 2, imageH-4, color.brighter());
            drawGradient(pntr, 4, 2, imageW-4, imageH-4, color);
            drawGradient(pntr, imageW-4, 2, 2, imageH-4, color.darker());
        }

        // Flush painter, add image to map and return
        pntr.flush();
        _images.put(imageName, image);
        return image;
    }

    /**
     * Clears the image.
     */
    private void clearImage()
    {
        _image = null;
    }

    /**
     * Draws radial image.
     */
    private void drawGradient(Painter aPntr, double aX, double aY, double aW, double aH, Color aColor)
    {
        int length = isHorizontal() ? (int) aW : (int) aH;
        double r = aColor.getRedInt();
        double g = aColor.getGreenInt();
        double b = aColor.getBlueInt();
        double radius = (length - 1) / 2f;
        double radiusSquared = radius * radius;

        // Fill strip image with color components for each point along the thumbWheelLength
        Image strip = Image.getImageForSize((int) aW, 1, false);
        Painter spntr = strip.getPainter();
        for (int i = 0; i < length; i++) {

            // Calculate the height of the thumbwheel at current point
            double h = Math.sqrt(radiusSquared - (radius - i) * (radius - i)) / radius;

            // Get red, green and blue component of color (scaled for the height)
            int ri = (int) Math.round(r * h);
            int gi = (int) Math.round(g * h);
            int bi = (int) Math.round(b * h);
            int val = (255 << 24) + (ri << 16) + (gi << 8) + bi; //int val = (ri<<16) + (gi<<8) + bi;
            spntr.setColor(new Color(val)); spntr.fillRect(i,0,1,1); //strip.setRGB(i, 0, val);
        }
        spntr.flush();

        // Draw strip into image
        aPntr.drawImage(strip, aX, aY, aW, aH);
    }

    /**
     * Override to reset image.
     */
    public void setWidth(double aValue)
    {
        if (aValue!=getWidth()) clearImage();
        super.setWidth(aValue);
    }

    /**
     * Override to reset image.
     */
    public void setHeight(double aValue)
    {
        if (aValue!=getHeight()) clearImage();
        super.setHeight(aValue);
    }

    /**
     * Returns whether a number is even (not odd).
     */
    private boolean isEven(float aValue)  { return Math.round(aValue)%2==0; }

    /**
     * Override to support props for this class.
     */
    @Override
    protected void initProps(PropSet aPropSet)
    {
        // Do normal version
        super.initProps(aPropSet);

        // Type, Min, Max, AbsMin, AbsMax, Round
        aPropSet.addPropNamed(Type_Prop, byte.class, TYPE_RADIAL);
        aPropSet.addPropNamed(Min_Prop, double.class, 0d);
        aPropSet.addPropNamed(Max_Prop, double.class, 100d);
        aPropSet.addPropNamed(AbsMin_Prop, double.class, (double) -Float.MAX_VALUE);
        aPropSet.addPropNamed(AbsMax_Prop, double.class, (double) Float.MAX_VALUE);
        aPropSet.addPropNamed(Round_Prop, double.class, 1);
    }

    /**
     * Override to support props for this class.
     */
    @Override
    public Object getPropValue(String aPropName)
    {
        switch (aPropName) {

            // Type, Min, Max, AbsMin, AbsMax, Round, Value
            case Type_Prop: return getType();
            case Min_Prop: return getVisibleMin();
            case Max_Prop: return getVisibleMax();
            case AbsMin_Prop: return getAbsoluteMin();
            case AbsMax_Prop: return getAbsoluteMax();
            case Round_Prop: return getRound();
            case Value_Prop: return getValue();

            // Do normal version
            default: return super.getPropValue(aPropName);
        }
    }

    /**
     * Override to support props for this class.
     */
    @Override
    public void setPropValue(String aPropName, Object aValue)
    {
        switch (aPropName) {

            // Type, Min, Max, AbsMin, AbsMax, Round, Value
            case Type_Prop: setType((byte) Convert.intValue(aValue)); break;
            case Min_Prop: setVisibleMin(Convert.doubleValue(aValue)); break;
            case Max_Prop: setVisibleMax(Convert.doubleValue(aValue)); break;
            case AbsMin_Prop: setAbsoluteMin(Convert.doubleValue(aValue)); break;
            case AbsMax_Prop: setAbsoluteMax(Convert.doubleValue(aValue)); break;
            case Round_Prop: setRound(Convert.doubleValue(aValue)); break;
            case Value_Prop: setValue(Convert.doubleValue(aValue)); break;

            // Do normal version
            default: super.setPropValue(aPropName, aValue);
        }
    }

    /**
     * XML archival.
     */
    public XMLElement toXML(XMLArchiver anArchiver)
    {
        // Archive basic view attributes
        XMLElement e = super.toXML(anArchiver);

        // Archive Type, VisibleMin, VisibleMax, AbsoluteMin, AbsoluteMax and Round
        if (getType() != TYPE_RADIAL) e.add("Type", "linear");
        if (getVisibleMin() != 0) e.add("Min", getVisibleMin());
        if (getVisibleMax() != 100) e.add("Max", getVisibleMax());
        if (getAbsoluteMin()!=-Float.MAX_VALUE) e.add("AbsMin", getAbsoluteMin());
        if (getAbsoluteMax()!=Float.MAX_VALUE) e.add("AbsMax", getAbsoluteMax());
        if (getRound()!=0) e.add("Round", getRound());

        // Return element
        return e;
    }

    /**
     * XML unarchival.
     */
    public Object fromXML(XMLArchiver anArchiver, XMLElement anElement)
    {
        // Unarchive basic view attributes
        super.fromXML(anArchiver, anElement);

        // Unarchive Type
        if (anElement.getAttributeValue("Type", "radial").equals("linear"))
            setType(TYPE_LINEAR);

        // Unarchive VisibleMin, VisibleMax, AbsoluteMin, AbsoluteMax and Round
        setVisibleMin(anElement.getAttributeDoubleValue("Min", getVisibleMin()));
        setVisibleMax(anElement.getAttributeDoubleValue("Max", getVisibleMax()));
        setAbsoluteMin(anElement.getAttributeDoubleValue("AbsMin", getAbsoluteMin()));
        setAbsoluteMax(anElement.getAttributeDoubleValue("AbsMax", getAbsoluteMax()));
        setRound(anElement.getAttributeDoubleValue("Round"));

        // Return
        return this;
    }
}