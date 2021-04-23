/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.gfx;
import snap.geom.Line;
import snap.geom.Rect;
import snap.geom.Shape;
import snap.geom.Transform;

import java.util.*;

/**
 * A base class for painting Shapes, Text and Images.
 */
public abstract class Painter {
    
    // The image quality
    private double  _imageQuality = .5;
    
    // Whether stroked path is used as is (otherwise, points might be rounded)
    private boolean _strokePure;

    // The composite mode
    private Composite  _composite = Composite.SRC_OVER;

    // Whether painting is being done for static output
    private boolean  _printing;
    
    // A shared rect for draw/fill/clipRect calls
    private Rect  _rect = new Rect();
    
    // A shared line for drawLine calls
    private Line  _line = new Line(0, 0, 0, 0);
    
    // Constants for composite
    public enum Composite { SRC_OVER, SRC_IN, DST_IN, DST_OUT }

    /**
     * Returns the current color.
     */
    public Color getColor()
    {
        Paint paint = getPaint();
        if (paint instanceof Color)
            return (Color) paint;
        return null;
    }

    /**
     * Sets the current color.
     */
    public void setColor(Color aColor)  { setPaint(aColor); }

    /**
     * Returns the current font.
     */
    public abstract Font getFont();

    /**
     * Sets the current font.
     */
    public abstract void setFont(Font font);

    /**
     * Returns the current paint.
     */
    public abstract Paint getPaint();

    /**
     * Sets the current paint.
     */
    public abstract void setPaint(Paint paint);

    /**
     * Returns the current stroke.
     */
    public abstract Stroke getStroke();

    /**
     * Sets the current stroke.
     */
    public abstract void setStroke(Stroke s);

    /**
     * Sets the current stroke.
     */
    public void setStrokeWidth(double aWidth)
    {
        Stroke s = getStroke();
        if (s!=null)
            s = s.copyForWidth(aWidth);
        else s = new Stroke(aWidth);
        setStroke(s);
    }

    /**
     * Returns the opacity.
     */
    public abstract double getOpacity();

    /**
     * Sets the opacity.
     */
    public abstract void setOpacity(double aValue);

    /**
     * Clears a rect.
     */
    public void clearRect(double aX, double aY, double aW, double aH) { }

    /**
     * Stroke the given shape.
     */
    public abstract void draw(Shape aShape);

    /**
     * Fill the given shape.
     */
    public abstract void fill(Shape aShape);

    /**
     * Draw the given line.
     */
    public void drawLine(double x1, double y1, double x2, double y2)
    {
        _line.setPoints(x1, y1, x2, y2);
        draw(_line);
    }

    /**
     * Fill the given rect.
     */
    public void fillRect(double x, double y, double w, double h)
    {
        _rect.setRect(x, y, w, h);
        fill(_rect);
    }

    /**
     * Draw the given rect.
     */
    public void drawRect(double x, double y, double w, double h)
    {
        _rect.setRect(x, y, w, h);
        draw(_rect);
    }

    /**
     * Convenience to stroke given shape with given paint.
     */
    public void drawWithPaint(Shape aShape, Paint aPaint)
    {
        setPaint(aPaint);
        draw(aShape);
    }

    /**
     * Convenience to fill given shape with given paint.
     */
    public void fillWithPaint(Shape aShape, Paint aPaint)
    {
        setPaint(aPaint);
        fill(aShape);
    }

    /**
     * Draw the given line.
     */
    public void drawLineWithPaint(double x1, double y1, double x2, double y2, Paint aPaint)
    {
        setPaint(aPaint);
        drawLine(x1, y1, x2, y2);
    }

    /**
     * Draw given rect with given paint.
     */
    public void drawRectWithPaint(double aX, double aY, double aW, double aH, Paint aPaint)
    {
        setPaint(aPaint);
        drawRect(aX, aY, aW, aH);
    }

    /**
     * Fill given rect with given paint.
     */
    public void fillRectWithPaint(double aX, double aY, double aW, double aH, Paint aPaint)
    {
        setPaint(aPaint);
        fillRect(aX, aY, aW, aH);
    }

    /**
     * Draw image with transform.
     */
    public void drawImage(Image anImg, Transform xform)
    {
        save(); transform(xform);
        drawImage(anImg, 0, 0);
        restore();
    }

    /**
     * Draw image in rect.
     */
    public void drawImage(Image anImg, double aX, double aY)  { drawImage(anImg,aX,aY,anImg.getWidth(),anImg.getHeight()); }

    /**
     * Draw image in rect.
     */
    public void drawImage(Image anImg, double aX, double aY, double aWidth, double aHeight)
    {
        drawImage(anImg, 0, 0, anImg.getWidth(), anImg.getHeight(), aX, aY, aWidth, aHeight);
    }

    /**
     * Draw image in rect.
     */
    public abstract void drawImage(Image img, double sx, double sy, double sw, double sh,
        double dx, double dy, double dw, double dh);

    /**
     * Draw string at location.
     */
    public void drawString(String aStr, double aX, double aY)
    {
        drawString(aStr, aX, aY, 0);
    }

    /**
     * Draw string at location with char spacing.
     */
    public void drawString(String aStr, double aX, double aY, double aCSpace)
    {
        // Simple case of no extra char space
        if (aCSpace==0) {
            drawString(aStr, aX, aY);
            return;
        }

        // Iterate over chars and draw each
        double x = aX;
        for (int i=0, iMax=aStr.length(); i<iMax; i++) { char c = aStr.charAt(i);
            drawString(String.valueOf(c), x, aY);
            x += getFont().charAdvance(c) + aCSpace;
        }
    }

    /**
     * Translate by x,y.
     */
    public void translate(double tx, double ty)
    {
        transform(new Transform(tx, ty));
    }

    /**
     * Rotate by angle in degrees.
     */
    public void rotate(double theDegrees)
    {
        transform(Transform.getRotate(theDegrees));
    }

    /**
     * Rotate by angle in degrees.
     */
    public void rotateAround(double theDegrees, double aX, double aY)
    {
        transform(Transform.getRotateAround(theDegrees, aX, aY));
    }

    /**
     * Scale by sx, sy.
     */
    public void scale(double sx, double sy)
    {
        transform(Transform.getScale(sx, sy));
    }

    /**
     * Return transform.
     */
    public abstract Transform getTransform();

    /**
     * Set to transform.
     */
    public abstract void setTransform(Transform aTrans);

    /**
     * Transform by transform.
     */
    public void setTransform(double a, double b, double c, double d, double tx, double ty)
    {
        setTransform(new Transform(a,b,c,d,tx,ty));
    }

    /**
     * Transform by transform.
     */
    public abstract void transform(Transform aTrans);

    /**
     * Transform by transform.
     */
    public void transform(double a, double b, double c, double d, double tx, double ty)
    {
        transform(new Transform(a,b,c,d,tx,ty));
    }

    /**
     * Return clip shape.
     */
    public abstract Shape getClip();

    /**
     * Return clip bounds.
     */
    public Rect getClipBounds()
    {
        Shape c = getClip();
        return c!=null ? c.getBounds() : null;
    }

    /**
     * Clip by shape.
     */
    public abstract void clip(Shape s);

    /**
     * Clip to rect.
     */
    public void clipRect(double aX, double aY, double aW, double aH)
    {
        _rect.setRect(aX, aY, aW, aH);
        clip(_rect);
    }

    /**
     * Returns the composite mode.
     */
    public Composite getComposite()  { return _composite; }

    /**
     * Sets the composite mode.
     */
    public void setComposite(Composite aComp) { _composite = aComp; }

    /**
     * Sets whether antialiasing.
     */
    public boolean setAntialiasing(boolean aValue)  { return false; }

    /**
     * Returns image rendering quality.
     */
    public double getImageQuality()  { return _imageQuality; }

    /**
     * Sets image rendering quality.
     */
    public void setImageQuality(double aValue)  { _imageQuality = aValue; }

    /**
     * Returns whether stroked path is used as is (otherwise, points might be rounded).
     */
    public boolean isStrokePure()  { return _strokePure; }

    /**
     * Sets whether stroked path is used as is (otherwise, points might be rounded).
     */
    public void setStrokePure(boolean aValue)  { _strokePure = aValue; }

    /**
     * Returns whether painting is for static output.
     */
    public boolean isPrinting()  { return _printing; }

    /**
     * Sets whether shape painting is for static output.
     */
    public void setPrinting(boolean aValue)  { _printing = aValue; }

    /**
     * Returns the object that provides extra painter properties.
     */
    public Props getProps()  { return _props; } Props _props;

    /**
     * Sets the object that provides extra painter properties.
     */
    public void setProps(Props theProps)  { _props = theProps; }

    /**
     * Saves the graphics state.
     */
    public abstract void save();

    /**
     * Restores the graphics state.
     */
    public abstract void restore();

    /**
     * Flush any buffered paint operations.
     */
    public void flush()  { }

    /**
     * Paints a 3D rect.
     */
    public void fill3DRect(double x, double y, double w, double h, boolean raised)
    {
        Color c = getColor();
        Color brighter = c.brighter().brighter();
        Color darker = c.darker();

        fillRectWithPaint(x+1, y+1, w-2, h-2, raised ? c : darker);
        drawLineWithPaint(x, y, x, y+h-1, raised ? brighter : darker);
        drawLine(x+1, y, x+w-2, y);
        drawLineWithPaint(x+1, y+h-1, x+w-1, y+h-1, raised ? darker : brighter);
        drawLine(x+w-1, y, x+w-1, y+h-2);
        setColor(c);
    }

    /**
     * Draws a button for the given rect with an option for pressed.
     */
    public void drawButton(Rect aRect, boolean isPressed)
    {
        drawButton(aRect.x, aRect.y, aRect.width, aRect.height, isPressed);
    }

    /**
     * Draws a button for the given rect with an option for pressed.
     */
    public void drawButton(double x, double y, double w, double h, boolean isPressed)
    {
        fillRectWithPaint(x, y, w, h, Color.BLACK);
        fillRectWithPaint(x, y, --w, --h, _white);
        fillRectWithPaint(++x, ++y, --w, --h, _darkGray);
        fillRectWithPaint(x, y, --w, --h, _lightGray);
        fillRectWithPaint(++x, ++y, --w, --h, isPressed ? _darkerGray : _gray);
    }

    // DrawButton colors
    private static Color _white = new Color(.9f, .95f, 1);
    private static Color _lightGray = new Color(.9f, .9f, .9f);
    private static Color _darkGray = new Color(.58f, .58f, .58f);
    private static Color _darkerGray = new Color(.5f, .5f, .5f);
    private static Color _gray = new Color(.7f, .7f, .7f);

    /**
     * Return native helper for painter, if available.
     */
    public Object getNative()  { return null; }

    /**
     * Return native helper for painter as given class, if available.
     */
    public <T> T getNative(Class<T> aClass)
    {
        Object ntv = getNative();
        return aClass.isInstance(ntv) ? (T) ntv : null;
    }

    /**
     * Standard toString implementation.
     */
    public String toString()
    {
        return getClass().getName() + " { font=" + getFont() + ", color=" + getColor() + " }";
    }

    /**
     * A class that provide extra painter properties.
     */
    public static class Props {

        Map<String,Object> _map = new HashMap<>();
        public Object getProp(String aKey)  { return _map.get(aKey); }
        public Object setProp(String aKey, Object aVal)  { return _map.put(aKey, aVal); }
    }

//public void draw/fillRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight);
//public void draw/fillOval(int x, int y, int width, int height);
//public void draw/fillArc(int x, int y, int width, int height, int startAngle, int arcAngle);
//public void drawPolyline(int xPoints[], int yPoints[], int nPoints);
//public void draw/fillPolygon(int xPoints[], int yPoints[], int nPoints);
//public void fillPolygon(Polygon p);
//public void shear(double shx, double shy);
//public void setBackground(Color color);
//public void setPaintMode();
//public void setXORMode(Color c1);
//public Graphics create(int x, int y, int width, int height) { }
//public void copyArea(int x, int y, int width, int height, int dx, int dy);
//public boolean hit(Rectangle rect, Shape s, boolean onStroke);
//public boolean hitClip(int x, int y, int width, int height);
//public GraphicsConfiguration getDeviceConfiguration();
//public FontMetrics getFontMetrics(), getFontRenderContext();

}