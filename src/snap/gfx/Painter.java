/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.gfx;
import java.util.*;

/**
 * A base class for painting RMShapes.
 */
public abstract class Painter {

    // Whether painting is being done for static output
    boolean        _printing;
    
    // Constants for composite
    public enum Composite { SRC_OVER, SRC_IN, DST_IN }

/**
 * Returns the current color.
 */
public Color getColor()
{
    Paint pnt = getPaint();
    if(pnt instanceof Color) return (Color)pnt;
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
    Stroke s = getStroke(); if(s!=null) s = s.copyForWidth(aWidth); else s = new Stroke(aWidth);
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
public abstract void draw(Shape s);

/**
 * Fill the given shape.
 */
public abstract void fill(Shape s);

/**
 * Draw the given line.
 */
public void drawLine(double x1, double y1, double x2, double y2)  { draw(new Line(x1, y1, x2, y2)); }

/**
 * Fill the given rect.
 */
public void fillRect(double x, double y, double w, double h)  { fill(new Rect(x,y,w,h)); }

/**
 * Draw the given rect.
 */
public void drawRect(double x, double y, double w, double h)  { draw(new Rect(x,y,w,h)); }

/**
 * Paints a 3D rect.
 */
public void fill3DRect(double x, double y, double w, double h, boolean raised)
{
    /*Paint p = getPaint();
    Color c = getColor(), brighter = c.brighter().brighter(), darker = c.darker();
    setColor(raised? c : darker); fillRect(x+1, y+1, w-2, h-2);
    setColor(raised? brighter : darker); fillRect(x, y, 1, h); fillRect(x+1, y, w-2, 1);
    setColor(raised? darker : brighter); fillRect(x+1, y+h-1, w-1, 1); fillRect(x+w-1, y, 1, h-1);
    setPaint(p);*/
    
    Color c = getColor(), brighter = c.brighter().brighter(), darker = c.darker();
    if(!raised) setColor(darker); fillRect(x+1, y+1, w-2, h-2);
    setColor(raised? brighter : darker); drawLine(x, y, x, y+h-1); drawLine(x+1, y, x+w-2, y);
    setColor(raised? darker : brighter); drawLine(x+1, y+h-1, x+w-1, y+h-1); drawLine(x+w-1, y, x+w-1, y+h-2);
    setColor(c);
}

/**
 * Draws a button for the given rect with an option for pressed.
 */
public void drawButton(Rect aRect, boolean isPressed)
{
    drawButton(aRect.getX(), aRect.getY(), aRect.getWidth(), aRect.getHeight(), isPressed);
}

/**
 * Draws a button for the given rect with an option for pressed.
 */
public void drawButton(double x, double y, double w, double h, boolean isPressed)
{
    setColor(Color.BLACK); fillRect(x, y, w, h);
    setColor(_white); fillRect(x, y, --w, --h);
    setColor(_darkGray); fillRect(++x, ++y, --w, --h);
    setColor(_lightGray); fillRect(x, y, --w, --h);
    setColor(isPressed? _darkerGray : _gray); fillRect(++x, ++y, --w, --h);
}

// DrawButton colors
static Color _white = new Color(.9f, .95f, 1), _lightGray = new Color(.9f, .9f, .9f);
static Color _darkGray = new Color(.58f, .58f, .58f), _darkerGray = new Color(.5f, .5f, .5f);
static Color _gray = new Color(.7f, .7f, .7f);

// Button states
public static final int BUTTON_NORMAL = 0;
public static final int BUTTON_OVER = 1;
public static final int BUTTON_PRESSED = 2;

/**
 * Draws a button for the given rect with an option for pressed.
 */
public void drawButton2(double x, double y, double w, double h)  { drawButton2(x,y,w,h,0); }

/**
 * Draws a button for the given rect with an option for pressed.
 */
public void drawButton2(double x, double y, double w, double h, int aState)  { drawButton2(x,y,w,h,aState,3); }
/**
 * Draws a button for the given rect with an option for pressed.
 */
public void drawButton2(double x, double y, double w, double h, int aState, int aRounding)
{
    RoundRect rect = new RoundRect(x,y,w,h,aRounding); setPaint(_gpaint1); fill(rect);    // Paint background gradient
    rect.setRect(x+.5,y+.5,w-1,h); setColor(_c6); draw(rect);                         // Paint outer bottom ring light gray
    rect.setRect(x+1.5,y+1.5,w-3,h-4); setPaint(_gpaint2); draw(rect);                // Paint inner ring light gray
    rect.setRect(x+.5,y+.5,w-1,h-1); setColor(_c0); draw(rect);                       // Paint outer ring
    
    // Handle BUTTON_OVER, BUTTON_PRESSED
    if(aState==BUTTON_OVER) { setPaint(_over); rect.setRect(x,y,w,h); fill(rect); }
    else if(aState==BUTTON_PRESSED) { setPaint(_prsd); rect.setRect(x,y,w,h); fill(rect); }
}

// Outer ring and outer lighted ring
static Color _c0 = Color.get("#a6a6a6"), _c6 = Color.get("#ffffffBB");
static Color _over = Color.get("#FFFFFF50"), _prsd = Color.get("#0000001A");

// Button background gradient (light gray top to dark gray bottom)
static Color _c1 = Color.get("#e8e8e8"), _c2 = Color.get("#d3d3d3");
static GradientPaint.Stop _stops1[] = { new GradientPaint.Stop(0,_c1), new GradientPaint.Stop(1,_c2) };
static GradientPaint _gpaint1 = new GradientPaint(.5,0,.5,1,_stops1);

// Button inner ring gradient (light gray top to dark gray bottom)
static Color _c3 = Color.get("#fbfbfb"), _c4 = Color.get("#dbdbdb");
static GradientPaint.Stop _stops2[] = { new GradientPaint.Stop(0,_c3), new GradientPaint.Stop(1,_c4) };
static GradientPaint _gpaint2 = new GradientPaint(.5,0,.5,1,_stops2);

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
public void drawString(String aStr, double aX, double aY)  { drawString(aStr, aX, aY, 0); }

/**
 * Draw string at location with char spacing.
 */
public void drawString(String aStr, double aX, double aY, double aCSpace)
{
    if(aCSpace==0) { drawString(aStr, aX, aY); return; } double x = aX;
    for(int i=0,iMax=aStr.length(); i<iMax; i++) { char c = aStr.charAt(i);
        drawString(String.valueOf(c), x, aY); x += getFont().charAdvance(c) + aCSpace; }
}

/**
 * Returns string bounds.
 */
public Rect getStringBounds(String aStr)  { return getFont().getStringBounds(aStr); }

/**
 * Translate by x,y.
 */
public void translate(double tx, double ty)  { transform(Transform.getTrans(tx, ty)); }

/**
 * Rotate by angle in degrees.
 */
public void rotate(double theDegrees)  { transform(Transform.getRotate(theDegrees)); }

/**
 * Scale by sx, sy.
 */
public void scale(double sx, double sy)  { transform(Transform.getScale(sx, sy)); }

/**
 * Transform by transform.
 */
public abstract Transform getTransform();

/**
 * Transform by transform.
 */
public abstract void setTransform(Transform aTrans);

/**
 * Transform by transform.
 */
public abstract void transform(Transform aTrans);

/**
 * Return clip shape.
 */
public abstract Shape getClip();

/**
 * Return clip bounds.
 */
public Rect getClipBounds()  { Shape c = getClip(); return c!=null? c.getBounds() : null; }

/**
 * Clip by shape.
 */
public abstract void clip(Shape s);

/**
 * Clip to rect.
 */
public void clipRect(double aX, double aY, double aW, double aH)  { clip(new Rect(aX,aY,aW,aH)); }

/**
 * Sets the composite mode.
 */
public void setComposite(Composite aComp) { }

/**
 * Sets whether antialiasing.
 */
public boolean setAntialiasing(boolean aValue)  { return false; }

/**
 * Sets image rendering quality.
 */
public void setImageQuality(double aValue)  { }

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
 * Standard toString implementation.
 */
public String toString() { return getClass().getName() + " { font=" + getFont() + ", color=" + getColor() + " }"; }

/**
 * Return native helper for painter, if available.
 */
public Object getNative()  { return null; }

/**
 * Return native helper for painter as given class, if available.
 */
public <T> T getNative(Class <T> aClass)  { Object ntv = getNative(); return aClass.isInstance(ntv)? (T)ntv : null; }

/**
 * A class that provide extra painter properties.
 */
public static class Props {
    
    public Object getProp(String aKey)  { return _map.get(aKey); } Map _map = new HashMap();
    public Object setProp(String aKey, Object aVal)  { return _map.put(aKey, aVal); }
}

//public boolean hit(Rectangle rect, Shape s, boolean onStroke);
//public GraphicsConfiguration getDeviceConfiguration();
//public void shear(double shx, double shy);
//public void setBackground(Color color);
//public Color getBackground();
//public Graphics create(int x, int y, int width, int height) { }
//public void setPaintMode();
//public void setXORMode(Color c1);
//public FontMetrics getFontMetrics() { return _g2.getFontMetrics(); }
//public FontRenderContext getFontRenderContext();
//public void copyArea(int x, int y, int width, int height, int dx, int dy);
//public void drawRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight);
//public void fillRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight);
//public void drawOval(int x, int y, int width, int height);
//public void fillOval(int x, int y, int width, int height);
//public void drawArc(int x, int y, int width, int height, int startAngle, int arcAngle);
//public void fillArc(int x, int y, int width, int height, int startAngle, int arcAngle);
//public void drawPolyline(int xPoints[], int yPoints[], int nPoints);
//public void drawPolygon(int xPoints[], int yPoints[], int nPoints);
//public void fillPolygon(int xPoints[], int yPoints[], int nPoints);
//public void fillPolygon(Polygon p);
//public boolean hitClip(int x, int y, int width, int height);
//public Composite getComposite();
//public void setComposite(Composite comp);

}