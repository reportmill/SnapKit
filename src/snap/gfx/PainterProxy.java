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
    protected Painter _pntr;

    /**
     * Constructor for given painter.
     */
    public PainterProxy(Painter aPntr)  { _pntr = aPntr; }

    /**
     * Returns the actual painter.
     */
    public Painter getPainter()  { return _pntr; }

    @Override
    public Font getFont()  { return _pntr.getFont(); }

    @Override
    public void setFont(Font font)  { _pntr.setFont(font); }

    @Override
    public Paint getPaint()  { return _pntr.getPaint(); }

    @Override
    public void setPaint(Paint paint)  { _pntr.setPaint(paint); }

    @Override
    public Stroke getStroke()  { return _pntr.getStroke(); }

    @Override
    public void setStroke(Stroke s)  { _pntr.setStroke( s); }

    @Override
    public double getOpacity()  { return _pntr.getOpacity(); }

    @Override
    public void setOpacity(double aValue)  { _pntr.setOpacity(aValue); }

    @Override
    public void clearRect(double aX, double aY, double aW, double aH) { _pntr.clearRect(aX,aY,aW,aH); }

    @Override
    public void draw(Shape s)  { _pntr.draw(s); }

    @Override
    public void fill(Shape s)  { _pntr.fill(s); }

    @Override
    public void drawLine(double x1, double y1, double x2, double y2)  { _pntr.drawLine(x1, y1, x2, y2); }

    @Override
    public void fillRect(double x, double y, double w, double h)  { _pntr.fillRect(x, y, w, h); }

    @Override
    public void drawRect(double x, double y, double w, double h)  { _pntr.drawRect(x, y, w, h); }

    @Override
    public void drawButton(double x, double y, double w, double h, boolean isPrsd)  { _pntr.drawButton(x,y,w,h,isPrsd); }

    @Override
    public void drawImage(Image img, Transform xform)  { _pntr.drawImage(img, xform); }

    @Override
    public void drawImage(Image img, double sx, double sy, double sw, double sh, double dx, double dy, double dw, double dh)
    {
        _pntr.drawImage(img, sx, sy, sw, sh, dx, dy, dw, dh);
    }

    @Override
    public void drawString(String str, double x, double y, double cs)  { _pntr.drawString(str, x, y, cs); }

    @Override
    public void strokeString(String str, double x, double y, double cs)  { _pntr.strokeString(str, x, y, cs); }

    @Override
    public Transform getTransform()  { return _pntr.getTransform(); }

    @Override
    public void setTransform(Transform aTrans)  { _pntr.setTransform(aTrans); }

    @Override
    public void transform(Transform Tx)  { _pntr.transform(Tx); }

    @Override
    public Shape getClip()  { return _pntr.getClip(); }

    @Override
    public Rect getClipBounds()  { return _pntr.getClipBounds(); }

    @Override
    public void clip(Shape s)  { _pntr.clip(s); }

    @Override
    public Composite getComposite()  { return _pntr.getComposite(); }

    @Override
    public void setComposite(Composite aComp) { _pntr.setComposite(aComp); }

    @Override
    public boolean setAntialiasing(boolean aValue)  { return _pntr.setAntialiasing(aValue); }

    @Override
    public double getImageQuality()  { return _pntr.getImageQuality(); }

    @Override
    public void setImageQuality(double aValue)  { _pntr.setImageQuality(aValue); }

    @Override
    public boolean isStrokePure()  { return _pntr.isStrokePure(); }

    @Override
    public void setStrokePure(boolean aValue)  { _pntr.setStrokePure(aValue); }

    @Override
    public boolean isPrinting()  { return _pntr.isPrinting(); }

    @Override
    public void setPrinting(boolean aValue)  { _pntr.setPrinting(aValue); }

    @Override
    public void save()  { _pntr.save(); }

    @Override
    public void restore()  { _pntr.restore(); }

    @Override
    public void flush()  { _pntr.flush(); }

    @Override
    public Object getNative()  { return _pntr.getNative(); }

    /** Standard toString implementation. */
    public String toString() { return getClass().getSimpleName() + ": " + _pntr.toString(); }
}