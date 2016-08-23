package snap.gfx;
import snap.util.*;

/**
 * A class to represent a painted stroke.
 */
public abstract class Border implements XMLArchiver.Archivable {
    
    // Cached version of insets
    Insets           _insets = null;
    
    // Shared insets
    private static final Insets _empty = new Insets(0);
    private static Color BORDER_GRAY = Color.LIGHTGRAY;
    private static Color BORDER_DARKGRAY = BORDER_GRAY.darker();
    
/**
 * Returns the insets.
 */
public Insets getInsets()  { return _insets!=null? _insets : (_insets=_empty); }

/**
 * Returns the basic color of the border.
 */
public Color getColor()  { return Color.BLACK; }

/**
 * Returns the basic width of the border.
 */
public double getWidth()  { return 1; }

/**
 * Paint border.
 */
public void paint(Painter aPntr, Shape aShape)  { }

/**
 * Creates an empty border for inset.
 */
public static Border.EmptyBorder createEmptyBorder(double w)  { return new EmptyBorder(w,w,w,w); }

/**
 * Creates an empty border.
 */
public static Border.EmptyBorder createEmptyBorder(double tp, double rt, double bm, double lt)
{
    return new EmptyBorder(tp,rt,bm,lt);
}

/**
 * Creates an empty border.
 */
public static Border.LineBorder createLineBorder(Color aColor, double aWidth)
{
    return new LineBorder(aColor, aWidth);
}

/**
 * Creates a compound border.
 */
public static Border createCompoundBorder(Border aB1, Border aB2)  { return new CompoundBorder(aB1, aB2); }

/**
 * Creates a compound border.
 */
public static Border.BevelBorder createLoweredBevelBorder()
{
    return new BevelBorder(BevelBorder.LOWERED);
}

/**
 * XML unarchival.
 */
public static Border fromXMLBorder(XMLArchiver anArchiver, XMLElement anElement)
{
    String type = anElement.getAttributeValue("type", "");
    Border border = null;
    if(type.equals("line")) border = new LineBorder();
    else if(type.equals("bevel")) border = new BevelBorder();
    else if(type.equals("etched")) border = new EtchedBorder();
    else if(type.equals("empty")) border = new EmptyBorder();
    else border = new NullBorder();
    border.fromXML(anArchiver, anElement);
    if(anElement.getAttributeValue("title")!=null) System.err.println("Border.fromXML: No more titles!");
    return border;
}

/**
 * A subclass for empty border.
 */
public static class NullBorder extends Border {

    /** XML Archival. */
    public XMLElement toXML(XMLArchiver anArchiver)  { XMLElement e = new XMLElement("border"); return e; }
    
    /** XML Unarchival. */
    public Border fromXML(XMLArchiver anArchiver, XMLElement anElement)  { return this; }
}

/**
 * A subclass for empty border.
 */
public static class EmptyBorder extends Border {
    
    // The insets
    double _tp, _rt, _bm, _lt;
    
    /** Creates a new EmptyBorder. */
    public EmptyBorder()  { }

    /** Creates a new EmptyBorder with insets. */
    public EmptyBorder(double tp, double rt, double bm, double lt)  { _tp = tp; _rt = rt; _bm = bm; _lt = lt; }
    
    /** Returns the insets. */
    public Insets getInsets()  { return _insets!=null? _insets : (_insets=new Insets(_tp,_rt,_bm,_lt)); }

    /** XML Archival. */
    public XMLElement toXML(XMLArchiver anArchiver)
    {
        XMLElement e = new XMLElement("border"); e.add("type", "empty");
        if(_tp!=0) e.add("Top", _tp); if(_lt!=0) e.add("Left", _lt);
        if(_bm!=0) e.add("Bottom", _bm); if(_rt!=0) e.add("Right", _rt);
        return e;
    }
    
    /** XML Unarchival. */
    public Border fromXML(XMLArchiver anArchiver, XMLElement anElement)
    {
        _tp = anElement.getAttributeFloatValue("Top", 0); _lt = anElement.getAttributeFloatValue("Left", 0);
        _bm = anElement.getAttributeFloatValue("Bottom", 0); _rt = anElement.getAttributeFloatValue("Right", 0);
        return this;
    }
}

/**
 * A subclass for line border.
 */
public static class LineBorder extends Border {

    // The color
    Color       _color;
    
    // The width
    double      _width;
    
    /** Creates a new line border. */
    public LineBorder()  { _color = Color.BLACK; _width = 1; }
    
    /** Creates a new line border. */
    public LineBorder(Color aColor, double aWidth)  { _color = aColor; _width = aWidth; }
    
    /** Returns color. */
    public Color getColor()  { return _color; }
    
    /** Returns the width. */
    public double getWidth()  { return _width; }
    
    /** Returns the insets. */
    public Insets getInsets()  { return _insets!=null? _insets : (_insets=new Insets(_width)); }

    /** Paint border. */
    public void paint(Painter aPntr, Shape aShape)
    {
        aPntr.setPaint(getColor()); aPntr.setStroke(_width==1? Stroke.Stroke1 : new Stroke(_width));
        if(aShape instanceof RectBase) { RectBase r = (RectBase)aShape; double hw = _width/2; r = r.clone();
            r.inset(_width/2); aPntr.draw(r); }
        else aPntr.draw(aShape);
    }

    /** XML Archival. */
    public XMLElement toXML(XMLArchiver anArchiver)
    {
        XMLElement e = new XMLElement("border"); e.add("type", "line");
        if(!_color.equals(Color.BLACK)) e.add("line-color", '#' + _color.toHexString());
        return e;
    }
    
    /** XML Unarchival. */
    public Border fromXML(XMLArchiver anArchiver, XMLElement anElement)
    {
        if(anElement.hasAttribute("line-color"))
            _color = new Color(anElement.getAttributeValue("line-color"));
        return this;
    }
    
    /** Standard toString implementation. */
    public String toString()  { return "LineBorder { color=" + _color + ", width=" + _width + " }"; }
}

/**
 * A subclass for bevel border.
 */
public static class BevelBorder extends Border {

    // The type
    int _type = 1; static int RAISED = 0, LOWERED = 1;
    
    /** Creates new border. */
    public BevelBorder() { }

    /** Creates new border. */
    public BevelBorder(int aType) { _type = aType; }

    /** Returns the type. */
    public int getType()  { return _type; }
    
    /** Returns the insets. */
    public Insets getInsets()  { return _insets!=null? _insets : (_insets=new Insets(2)); }
    
    /** Paint border. */
    public void paint(Painter aPntr, Shape aShape)
    {
        Rect rect = aShape.getBounds(); double w = rect.getWidth(), h = rect.getHeight();
        aPntr.setStroke(Stroke.Stroke1);
        aPntr.setColor(Color.WHITE); aPntr.drawRect(.5,.5,w-1,h-1); aPntr.drawRect(1.5,1.5,w-3,h-3);
        if(_type==LOWERED) {
            aPntr.setColor(BORDER_GRAY); aPntr.drawLine(.5,.5,w-1,.5); aPntr.drawLine(.5,.5,.5,h-1);
            aPntr.setColor(BORDER_DARKGRAY); aPntr.drawLine(1.5,1.5,w-3,1.5); aPntr.drawLine(1.5,1.5,1.5,h-3);
        }
        else {
            aPntr.setColor(BORDER_DARKGRAY); aPntr.drawLine(.5,h-.5,w-1,h-.5); aPntr.drawLine(w-.5,.5,w-.5,h-1);
            aPntr.setColor(BORDER_GRAY); aPntr.drawLine(1.5,h-1.5,w-3,h-1.5); aPntr.drawLine(w-1.5,1.5,w-1.5,h-3);
        }
    }

    /** XML Archival. */
    public XMLElement toXML(XMLArchiver anArchiver)
    {
        XMLElement e = new XMLElement("border"); e.add("type", "bevel");
        if(_type==RAISED) e.add("bevel-type", "raised");
        return e;
    }
    
    /** XML Unarchival. */
    public Border fromXML(XMLArchiver anArchiver, XMLElement anElement)
    {
        String type = anElement.getAttributeValue("bevel-type", "lowered");
        if(type.equals("raised")) _type = 0;
        return this;
    }
}

/**
 * A subclass for etched border.
 */
public static class EtchedBorder extends Border {

    // The type
    int _type = 1; int RAISED = 0, LOWERED = 1;

    /** Creates new border. */
    public EtchedBorder() { }

    /** Creates new border. */
    public EtchedBorder(int aType) { _type = aType; }

    /** Returns the type. */
    public int getType()  { return _type; }
    
    /** Returns the insets. */
    public Insets getInsets()  { return _insets!=null? _insets : (_insets=new Insets(2)); }

    /** Paint border. */
    public void paint(Painter aPntr, Shape aShape)
    {
        Rect rect = aShape.getBounds(); double w = rect.getWidth(), h = rect.getHeight();
        aPntr.setColor(Color.WHITE); aPntr.drawRect(1.5,1.5,w-2,h-2);
        aPntr.setColor(BORDER_GRAY); aPntr.drawRect(.5,.5,w-2,h-2);
    }
    
    /** XML Archival. */
    public XMLElement toXML(XMLArchiver anArchiver)
    {
        XMLElement e = new XMLElement("border"); e.add("type", "etched");
        if(_type==RAISED) e.add("etch-type", "raised");
        return e;
    }
    
    /** XML Unarchival. */
    public Border fromXML(XMLArchiver anArchiver, XMLElement anElement)
    {
        String type = anElement.getAttributeValue("etch-type", "lowered");
        if(type.equals("raised")) _type = RAISED;
        return this;
    }
}

/**
 * A subclass for compound border.
 */
public static class CompoundBorder extends Border {

    // The two borders
    Border  _obdr, _ibdr;
    
    /** Creates a new TitledBorder. */
    public CompoundBorder(Border anOutsdBdr, Border anInsdBdr)  { _obdr = anOutsdBdr; _ibdr = anInsdBdr; }
    
    /** Returns the real border. */
    public Border getOutsideBorder()  { return _obdr; }

    /** Returns the real border. */
    public Border getInsideBorder()  { return _ibdr; }

    /** Returns the insets. */
    public Insets getInsets()
    {
        if(_insets!=null ) return _insets;
        return _insets = Insets.add(_obdr.getInsets(),_ibdr.getInsets());
    }

    /** XML Archival. */
    public XMLElement toXML(XMLArchiver anArchiver)
    {
        throw new RuntimeException("Border.CompoundBorder: archival not implemented");
    }
    
    /** XML Unarchival. */
    public Border fromXML(XMLArchiver anArchiver, XMLElement anElement)
    {
        throw new RuntimeException("Border.CompoundBorder: unarchival not implemented");
    }
}

}