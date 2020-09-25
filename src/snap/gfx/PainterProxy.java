/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.gfx;
import snap.geom.Rect;
import snap.geom.Shape;
import snap.geom.Transform;

/**
 * A Painter subclass that forward on to another.
 */
public class PainterProxy extends Painter {

    // The real painter
    protected Painter       _pntr;

    /**
     * Creates a new PainterProxy for given painter.
     */
    public PainterProxy(Painter aPntr)  { _pntr = aPntr; }

    /** Returns the actual painter. */
    public Painter getPntr()  { return _pntr; }

    /** Returns the font. */
    public Font getFont()  { return _pntr.getFont(); }

    /** Sets the font. */
    public void setFont(Font font)  { _pntr.setFont(font); }

    /** Returns the paint. */
    public Paint getPaint()  { return _pntr.getPaint(); }

    /** Sets the paint. */
    public void setPaint(Paint paint)  { _pntr.setPaint(paint); }

    /** Returns the stroke. */
    public Stroke getStroke()  { return _pntr.getStroke(); }

    /** Sets the stroke. */
    public void setStroke(Stroke s)  { _pntr.setStroke( s); }

    /** Returns the opacity. */
    public double getOpacity()  { return _pntr.getOpacity(); }

    /** Sets the opacity. */
    public void setOpacity(double aValue)  { _pntr.setOpacity(aValue); }

    /** Clears a rect. */
    public void clearRect(double aX, double aY, double aW, double aH) { _pntr.clearRect(aX,aY,aW,aH); }

    /** Stroke the given shape. */
    public void draw(Shape s)  { _pntr.draw(s); }

    /** Fill the given shape. */
    public void fill(Shape s)  { _pntr.fill(s); }

    /** Draw the given line. */
    public void drawLine(double x1, double y1, double x2, double y2)  { _pntr.drawLine(x1, y1, x2, y2); }

    /** Fill the given rect. */
    public void fillRect(double x, double y, double w, double h)  { _pntr.fillRect(x, y, w, h); }

    /** Draw the given rect. */
    public void drawRect(double x, double y, double w, double h)  { _pntr.drawRect(x, y, w, h); }

    /** Paints a 3D rect. */
    public void fill3DRect(double x, double y, double w, double h, boolean isRsd)  { _pntr.fill3DRect(x,y,w,h,isRsd); }

    /** Draws a button for the given rect with an option for pressed. */
    public void drawButton(double x, double y, double w, double h, boolean isPrsd)  { _pntr.drawButton(x,y,w,h,isPrsd); }

    /** Draw image with transform. */
    public void drawImage(Image img, Transform xform)  { _pntr.drawImage(img, xform); }

    /** Draw image in rect. */
    public void drawImage(Image img, double sx, double sy, double sw, double sh, double dx, double dy, double dw, double dh)
    {
        _pntr.drawImage(img, sx, sy, sw, sh, dx, dy, dw, dh);
    }

    /** Draw string at location. */
    public void drawString(String str, double x, double y)  { _pntr.drawString(str, x, y, 0); }

    /** Draw string at location with char spacing. */
    public void drawString(String str, double x, double y, double cs)  { _pntr.drawString(str, x, y, cs); }

    /** Transform by transform. */
    public Transform getTransform()  { return _pntr.getTransform(); }

    /** Transform by transform. */
    public void setTransform(Transform aTrans)  { _pntr.setTransform(aTrans); }

    /** Transform by transform. */
    public void transform(Transform Tx)  { _pntr.transform(Tx); }

    /** Return clip shape. */
    public Shape getClip()  { return _pntr.getClip(); }

    /** Return clip bounds. */
    public Rect getClipBounds()  { return _pntr.getClipBounds(); }

    /** Clip by shape. */
    public void clip(Shape s)  { _pntr.clip(s); }

    /** Sets the composite mode. */
    public void setComposite(Composite aComp) { _pntr.setComposite(aComp); }

    /** Sets whether antialiasing. */
    public boolean setAntialiasing(boolean aValue)  { return _pntr.setAntialiasing(aValue); }

    /** Sets image rendering quality. */
    public void setImageQuality(double aValue)  { _pntr.setImageQuality(aValue); }

    /** Returns whether shape painting is really printing. */
    public boolean isPrinting()  { return _pntr.isPrinting(); }

    /** Returns whether shape painting is really printing. */
    public void setPrinting(boolean aValue)  { _pntr.setPrinting(aValue); }

    /** Standard clone implementation. */
    public void save()  { _pntr.save(); }

    /** Disposes this painter. */
    public void restore()  { _pntr.restore(); }

    /** Flush any buffered paint operations. */
    public void flush()  { _pntr.flush(); }

    /** Standard toString implementation. */
    public String toString() { return getClass().getSimpleName() + ": " + _pntr.toString(); }

    /** Return native helper for painter, if available. */
    public Object getNative()  { return _pntr.getNative(); }
}