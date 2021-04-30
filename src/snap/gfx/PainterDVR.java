/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.gfx;
import java.util.*;

import snap.geom.Rect;
import snap.geom.Shape;
import snap.geom.Transform;
import snap.util.SnapUtils;
import snap.util.StringUtils;

/**
 * A Painter subclass that records painting operations for later execution instead of executing them immediately.
 */
public class PainterDVR extends PainterImpl {
    
    // The instructions
    private List<Instruction>  _instrs = new ArrayList<>();
    
    // The Painter (for EffectPntr)
    private Painter  _pntr;

    /**
     * Constructor.
     */
    public PainterDVR()  { }

    /**
     * Creates a new PainterDVR for given painter (which supports applyEffect()).
     */
    public PainterDVR(Painter aPntr)
    {
        super();
        _pntr = aPntr;
    }

    /**
     * Executes the instructions stored in this PainterDVR to given painter.
     */
    public void exec(Painter aPntr)
    {
        for (Instruction i : _instrs)
            i.exec(aPntr);
    }

    /**
     * Clears the instructions stored in this PainterDVR.
     */
    public void clear()  { _instrs.clear(); }

    /**
     * Returns image of given shape inside a gutter of given inset (maybe should be insets one day).
     */
    public Image getImage(Rect aRect, int anInset)
    {
        // Get shape image width and height
        int width = (int) Math.round(aRect.getWidth()) + anInset*2;
        int height = (int) Math.round(aRect.getHeight()) + anInset*2;

        // Create new image for dvr painter and paint dvr to it
        Image img = Image.get(width, height, true);
        Painter ipntr = img.getPainter();
        ipntr.setImageQuality(1);
        ipntr.clipRect(0, 0, width, height);
        ipntr.translate(anInset, anInset);
        exec(ipntr);
        return img;
    }

    /** Sets the font. */
    public void setFont(Font aFont)
    {
        super.setFont(aFont);
        add(new SetFont(aFont));
    }

    /** Sets the paint. */
    public void setPaint(Paint aPaint)
    {
        super.setPaint(aPaint);
        add(new SetPaint(aPaint));
    }

    /** Sets the stroke. */
    public void setStroke(Stroke aStroke)
    {
        super.setStroke(aStroke);
        add(new SetStroke(aStroke));
    }

    /** Sets the opacity. */
    public void setOpacity(double aValue)
    {
        super.setOpacity(aValue);
        add(new SetOpacity(aValue));
    }

    /** Stroke the given shape. */
    public void draw(Shape aShape)
    {
        super.draw(aShape);
        add(new DrawShape(aShape));
    }

    /** Fill the given shape. */
    public void fill(Shape aShape)
    {
        super.fill(aShape);
        add(new FillShape(aShape));
    }

    /** Draw image with transform. */
    public void drawImage(Image anImg, Transform aTrans)
    {
        super.drawImage(anImg,aTrans);
        add(new DrawImageX(anImg, aTrans));
    }

    /** Draw image in rect. */
    public void drawImage(Image img, double sx, double sy, double sw, double sh, double dx, double dy, double dw, double dh)
    {
        super.drawImage(img, sx, sy, sw, sh, dx, dy, dw, dh);
        add(new DrawImage(img, sx, sy, sw, sh, dx, dy, dw, dh));
    }

    /** Draw string at location with char spacing. */
    public void drawString(String aStr, double aX, double aY, double aCSpace)
    {
        super.drawString(aStr, aX, aY, aCSpace);
        add(new DrawString(aStr, aX, aY, aCSpace));
    }

    /**
     * Transform by transform.
     */
    public void setTransform(Transform aTrans)
    {
        super.setTransform(aTrans);
        add(new SetTransform(aTrans));
    }

    /** Transform by transform. */
    public void transform(Transform aTrans)
    {
        super.transform(aTrans);
        add(new TransformBy(aTrans));
    }

    /** Clip by shape. */
    public void clip(Shape aShape)
    {
        super.clip(aShape);
        add(new ClipBy(aShape));
    }

    /** Standard clone implementation. */
    public void save()
    {
        super.save();
        add(new Save());
    }

    /** Disposes this painter. */
    public void restore()
    {
        super.restore();
        add(new Restore());
    }

    /** Override to forward to real painter. */
    public Props getProps()
    {
        return _pntr != null ? _pntr.getProps() : null;
    }

    /**
     * Adds an instruction.
     */
    public void add(Instruction anInstr)
    {
        _instrs.add(anInstr);
    }

    /**
     * Standard equals implementation.
     */
    public boolean equals(Object anObj)
    {
        if (anObj == this) return true;
        PainterDVR other = anObj instanceof PainterDVR ? (PainterDVR) anObj : null; if (other == null) return false;
        int len = _instrs.size(); if (len != other._instrs.size()) return false;
        for (int i=0; i<len; i++) { Instruction i1 = _instrs.get(i), i2 = other._instrs.get(i);
            if (i1.getClass() != i2.getClass()) return false;
            if (!i1.equals(i2)) return false;
        }
        return true;
    }

    /**
     * A class to represent instructions.
     */
    public abstract static class Instruction {

        /** Plays the op in given painter. */
        public abstract void exec(Painter aPntr);

        /** Standard equals implementation. */
        public boolean equals(Object anObj) { return true; }

        /** Standard toString implementation. */
        public String toString()  { return StringUtils.toString(this).toString(); }
    }

    /**
     * An instruction to setFont().
     */
    public static class SetFont extends Instruction {

        /** Creates a new SetFont. */
        public SetFont(Font aFont)  { _font = aFont; } Font _font;

        /** Returns the font. */
        public Font getFont()  { return _font; }

        /** Plays the op in given painter. */
        public void exec(Painter aPntr)  { aPntr.setFont(_font); }

        /** Standard equals implementation. */
        public boolean equals(Object anObj) { return _font.equals(((SetFont)anObj)._font); }

        /** Standard toString implementation. */
        public String toString()  { return StringUtils.toString(this, "Font").toString(); }
    }

    /**
     * An instruction to setPaint().
     */
    public static class SetPaint extends Instruction {

        /** Creates a new SetPaint. */
        public SetPaint(Paint aPaint)  { _paint = aPaint; } Paint _paint;

        /** Returns the paint. */
        public Paint getPaint()  { return _paint; }

        /** Plays the op in given painter. */
        public void exec(Painter aPntr)  { aPntr.setPaint(_paint); }

        /** Standard equals implementation. */
        public boolean equals(Object anObj) { return _paint.equals(((SetPaint)anObj)._paint); }

        /** Standard toString implementation. */
        public String toString()  { return StringUtils.toString(this, "Paint").toString(); }
    }

    /**
     * An instruction to setStroke().
     */
    public static class SetStroke extends Instruction {

        /** Creates a new SetStroke. */
        public SetStroke(Stroke aStroke)  { _stroke = aStroke; } Stroke _stroke;

        /** Returns the stroke. */
        public Stroke getStroke()  { return _stroke; }

        /** Plays the op in given painter. */
        public void exec(Painter aPntr)  { aPntr.setStroke(_stroke); }

        /** Standard equals implementation. */
        public boolean equals(Object anObj) { return _stroke.equals(((SetStroke)anObj)._stroke); }

        /** Standard toString implementation. */
        public String toString()  { return StringUtils.toString(this, "Stroke").toString(); }
    }

    /**
     * An instruction to setOpacity().
     */
    public static class SetOpacity extends Instruction {

        /** Creates a new SetOpacity. */
        public SetOpacity(double aValue)  { _opacity = aValue; } double _opacity;

        /** Returns the opacity. */
        public double getOpacity()  { return _opacity; }

        /** Plays the op in given painter. */
        public void exec(Painter aPntr)  { aPntr.setOpacity(_opacity); }

        /** Standard equals implementation. */
        public boolean equals(Object anObj) { return _opacity!=((SetOpacity)anObj)._opacity; }

        /** Standard toString implementation. */
        public String toString()  { return StringUtils.toString(this, "Opacity").toString(); }
    }

    /**
     * An instruction to draw(Shape).
     */
    public static class DrawShape extends Instruction {

        /** Creates a new DrawShape. */
        public DrawShape(Shape aShape)  { _shape = aShape; } Shape _shape;

        /** Returns the shape. */
        public Shape getShape()  { return _shape; }

        /** Plays the op in given painter. */
        public void exec(Painter aPntr)  { aPntr.draw(_shape); }

        /** Standard equals implementation. */
        public boolean equals(Object anObj) { return _shape.equals(((DrawShape)anObj)._shape); }

        /** Standard toString implementation. */
        public String toString()  { return StringUtils.toString(this, "Shape").toString(); }
    }

    /**
     * An instruction to fill(Shape).
     */
    public static class FillShape extends Instruction {

        /** Creates a new FillShape. */
        public FillShape(Shape aShape)  { _shape = aShape; } Shape _shape;

        /** Returns the shape. */
        public Shape getShape()  { return _shape; }

        /** Plays the op in given painter. */
        public void exec(Painter aPntr)  { aPntr.fill(_shape); }

        /** Standard equals implementation. */
        public boolean equals(Object anObj) { return _shape.equals(((FillShape)anObj)._shape); }

        /** Standard toString implementation. */
        public String toString()  { return StringUtils.toString(this, "Shape").toString(); }
    }

    /**
     * An instruction to drawImage(Image,Transform).
     */
    public static class DrawImage extends Instruction {

        // Ivars
        Image _img; double _sx, _sy, _sw, _sh, _dx, _dy, _dw, _dh;

        /** Creates a new DrawImage. */
        public DrawImage(Image img, double sx, double sy, double sw, double sh, double dx, double dy, double dw, double dh)
        {
            _img = img; _sx = sx; _sy = sy; _sw = sw; _sh = sh; _dx = dx; _dy = dy; _dw = dw; _dh = dh;
        }

        /** Plays the op in given painter. */
        public void exec(Painter aPntr)  { aPntr.drawImage(_img, _sx, _sy, _sw, _sh, _dx, _dy, _dw, _dh); }

        /** Standard equals implementation. */
        public boolean equals(Object anObj)
        {
            DrawImage o = (DrawImage)anObj;
            return _img==o._img && _sx==o._sx && _sy==o._sy && _sw==o._sw && _sh==o._sh &&
                _sx==o._sx && _sy==o._sy && _sw==o._sw && _sh==o._sh;
        }

        /** Standard toString implementation. */
        public String toString()  { return StringUtils.toString(this).toString(); }
    }

    /**
     * An instruction to drawImage(Image,Transform).
     */
    public static class DrawImageX extends Instruction {

        /** Creates a new DrawImage. */
        public DrawImageX(Image anImg, Transform aTrans)  { _img = anImg; _xfm = aTrans; } Image _img; Transform _xfm;

        /** Plays the op in given painter. */
        public void exec(Painter aPntr)  { aPntr.drawImage(_img, _xfm); }

        /** Standard equals implementation. */
        public boolean equals(Object anObj)
        {
            DrawImageX o = (DrawImageX)anObj;
            return _img==o._img && SnapUtils.equals(_xfm,o._xfm);
        }
    }

    /**
     * An instruction to drawString(str,x,y,cs).
     */
    public static class DrawString extends Instruction {

        // Ivars
        String _str; double _x, _y, _cs;

        /** Creates a new DrawString. */
        public DrawString(String aStr, double x, double y, double cs)  { _str = aStr; _x = x; _y = y; _cs = cs; }

        /** Returns the string. */
        public String getString()  { return _str; }

        /** Plays the op in given painter. */
        public void exec(Painter aPntr)  { aPntr.drawString(_str, _x, _y, _cs); }

        /** Standard equals implementation. */
        public boolean equals(Object anObj)
        {
            DrawString o = (DrawString)anObj;
            return _str.equals(o._str) && _x==o._x && _y==o._y && _cs==o._cs;
        }

        /** Standard toString implementation. */
        public String toString()  { return StringUtils.toString(this, "String").toString(); }
    }

    /**
     * An instruction to setTransform().
     */
    public static class SetTransform extends Instruction {

        /** Creates a new SetTransform. */
        public SetTransform(Transform aTrans)  { _xfm = aTrans; } Transform _xfm;

        /** Plays the op in given painter. */
        public void exec(Painter aPntr)  { aPntr.setTransform(_xfm); }

        /** Standard equals implementation. */
        public boolean equals(Object anObj) { return _xfm.equals(((SetTransform)anObj)._xfm); }

        /** Standard toString implementation. */
        public String toString()  { return StringUtils.toString(this).toString(); }
    }

    /**
     * An instruction to transform().
     */
    public static class TransformBy extends Instruction {

        /** Creates a new TransformBy. */
        public TransformBy(Transform aTrans)  { _xfm = aTrans; } Transform _xfm;

        /** Plays the op in given painter. */
        public void exec(Painter aPntr)  { aPntr.transform(_xfm); }

        /** Standard equals implementation. */
        public boolean equals(Object anObj) { return _xfm.equals(((TransformBy)anObj)._xfm); }

        /** Standard toString implementation. */
        public String toString()  { return StringUtils.toString(this).toString(); }
    }

    /**
     * An instruction to save().
     */
    public static class Save extends Instruction {

        /** Plays the op in given painter. */
        public void exec(Painter aPntr)  { aPntr.save(); }
    }

    /**
     * An instruction to restore().
     */
    public static class Restore extends Instruction {

        /** Plays the op in given painter. */
        public void exec(Painter aPntr)  { aPntr.restore(); }
    }

    /**
     * An instruction to clip().
     */
    public static class ClipBy extends Instruction {

        /** Creates a new ClipBy. */
        public ClipBy(Shape aShape)  { _shape = aShape; } Shape _shape;

        /** Plays the op in given painter. */
        public void exec(Painter aPntr)  { aPntr.clip(_shape); }
    }
}