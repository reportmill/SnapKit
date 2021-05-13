package snap.gfx;
import snap.geom.*;
import snap.util.XMLArchiver;
import snap.util.XMLElement;

import java.util.Objects;

/**
 * This class holds a selection of Border subclasses.
 */
public class Borders {

    // Common borders
    public static final Border BLACK_BORDER = Border.createLineBorder(Color.BLACK, 1);

    // Border constants
    private static Color BORDER_GRAY = Color.LIGHTGRAY;
    private static Color BORDER_DARKGRAY = BORDER_GRAY.darker();

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
        protected Insets createInsets()  { return new Insets(_tp,_rt,_bm,_lt); }

        /** XML Archival. */
        public XMLElement toXML(XMLArchiver anArchiver)
        {
            XMLElement e = new XMLElement("EmptyBorder");
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
        Color       _color = Color.BLACK;

        // The stroke
        Stroke      _stroke = Stroke.Stroke1;

        /** Creates lineBorder. */
        public LineBorder()  { }

        /** Creates lineBorder. */
        public LineBorder(Color aColor, double aWidth)  { _color = aColor; _stroke = Stroke.getStroke(aWidth); }

        /** Creates lineBorder. */
        public LineBorder(Color aColor, Stroke aStroke)  { _color = aColor; _stroke = aStroke; }

        /** Returns color. */
        public Color getColor()  { return _color; }

        /** Returns the width. */
        public double getWidth()  { return _stroke.getWidth(); }

        /** Returns the stroke. */
        public Stroke getStroke()  { return _stroke; }

        /** Creates the insets. */
        protected Insets createInsets()  { return new Insets(getWidth()); }

        /** Paint border. */
        public void paint(Painter aPntr, Shape aShape)
        {
            aPntr.setPaint(getColor());
            aPntr.setStroke(getStroke());
            if (aShape instanceof Rect) { Rect rect = (Rect)aShape;
                rect = rect.getInsetRect(getWidth()/2);
                aPntr.draw(rect);
            }
            else aPntr.draw(aShape);
            aPntr.setStroke(Stroke.Stroke1);
        }

        /**
         * Returns copy of this border with new color.
         */
        public LineBorder copyForColor(Color aColor)
        {
            LineBorder c = clone(); c._color = aColor; return c;
        }

        /**
         * Returns copy of this border with new stroke width.
         */
        public LineBorder copyForStrokeWidth(double aWidth)
        {
            return copyForStroke(getStroke().copyForWidth(aWidth));
        }

        /**
         * Returns copy of this border with new stroke width.
         */
        public LineBorder copyForStroke(Stroke aStroke)
        {
            LineBorder c = clone(); c._stroke = aStroke; return c;
        }

        /**
         * Override to return as LineBorder.
         */
        protected LineBorder clone()
        {
            return (LineBorder)super.clone();
        }

        /**
         * Standard equals implementation.
         */
        public boolean equals(Object anObj)
        {
            // Check identity, superclass and get other
            if(anObj==this) return true;
            if(!super.equals(anObj)) return false;
            LineBorder other = (LineBorder)anObj;
            return other._stroke.equals(_stroke);
        }

        /** XML Archival. */
        public XMLElement toXML(XMLArchiver anArchiver)
        {
            XMLElement e = new XMLElement("LineBorder");
            if(!_color.equals(Color.BLACK)) e.add("Color", '#' + _color.toHexString());
            if(getWidth()!=1) e.add("Width", getWidth());
            return e;
        }

        /** XML Unarchival. */
        public Border fromXML(XMLArchiver anArchiver, XMLElement anElement)
        {
            if(anElement.hasAttribute("Color")) _color = new Color(anElement.getAttributeValue("Color"));
            if(anElement.hasAttribute("line-color")) _color = new Color(anElement.getAttributeValue("line-color"));
            if(anElement.hasAttribute("Width")) _stroke = _stroke.copyForWidth(anElement.getAttributeFloatValue("Width"));
            return this;
        }

        /** Standard toString implementation. */
        public String toString()
        {
            return "LineBorder { Color=" + _color.toHexString() + ", Width=" + getWidth() + " }";
        }
    }

    /**
     * A subclass for bevel border.
     */
    public static class BevelBorder extends Border {

        // The type
        int _type = LOWERED;
        public static final int LOWERED = 0, RAISED = 1;

        /** Creates new border. */
        public BevelBorder() { }

        /** Creates new border. */
        public BevelBorder(int aType) { _type = aType; }

        /** Returns the type. */
        public int getType()  { return _type; }

        /** Creates the insets. */
        protected Insets createInsets()  { return new Insets(2); }

        /** Paint border. */
        public void paint(Painter aPntr, Shape aShape)
        {
            Rect rect = aShape.getBounds(); double x = rect.x, y = rect.y, w = rect.width, h = rect.height;
            aPntr.setStroke(Stroke.Stroke1); aPntr.setColor(Color.WHITE);
            aPntr.drawRect(x+.5,y+.5,w-1,h-1); aPntr.drawRect(x+1.5,y+1.5,w-3,h-3);
            if(_type==LOWERED) {
                aPntr.setColor(BORDER_GRAY); aPntr.drawLine(x+.5,y+.5,x+w-1,y+.5); aPntr.drawLine(x+.5,y+.5,x+.5,y+h-1);
                aPntr.setColor(BORDER_DARKGRAY); aPntr.drawLine(x+1.5,y+1.5,x+w-3,y+1.5);
                aPntr.drawLine(x+1.5,y+1.5,x+1.5,y+h-3);
            }
            else {
                aPntr.setColor(BORDER_DARKGRAY); aPntr.drawLine(x+.5,y+h-.5,x+w-1,y+h-.5);
                aPntr.drawLine(x+w-.5,y+.5,x+w-.5,y+h-1);
                aPntr.setColor(BORDER_GRAY); aPntr.drawLine(x+1.5,y+h-1.5,x+w-3,y+h-1.5);
                aPntr.drawLine(x+w-1.5,y+1.5,x+w-1.5,y+h-3);
            }
        }

        /** XML Archival. */
        public XMLElement toXML(XMLArchiver anArchiver)
        {
            XMLElement e = new XMLElement("BevelBorder");
            if(_type==RAISED) e.add("Type", "RAISED");
            return e;
        }

        /** XML Unarchival. */
        public Border fromXML(XMLArchiver anArchiver, XMLElement anElement)
        {
            String type = anElement.getAttributeValue("bevel-type", "lowered");
            if(anElement.hasAttribute("Type")) type = anElement.getAttributeValue("Type");
            if(anElement.hasAttribute("bevel-type")) type = anElement.getAttributeValue("bevel-type");
            if(type.equals("RAISED") || type.equals("raised")) _type = RAISED;
            return this;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;
            BevelBorder that = (BevelBorder) o;
            return _type == that._type;
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(_type);
        }
    }

    /**
     * A subclass for etched border.
     */
    public static class EtchBorder extends Border {

        // The type
        int _type = LOWERED;
        public static final int LOWERED = 0, RAISED = 1;

        /** Creates new EtchBorder. */
        public EtchBorder() { }

        /** Creates new EtchBorder. */
        public EtchBorder(int aType) { _type = aType; }

        /** Returns the type. */
        public int getType()  { return _type; }

        /** Creates the insets. */
        protected Insets createInsets()  { return new Insets(2); }

        /** Paint border. */
        public void paint(Painter aPntr, Shape aShape)
        {
            aPntr.setStroke(Stroke.Stroke1);
            Rect rect = aShape.getBounds(); double w = rect.getWidth(), h = rect.getHeight();
            aPntr.setColor(Color.WHITE); aPntr.drawRect(1.5,1.5,w-2,h-2);
            aPntr.setColor(BORDER_GRAY); aPntr.drawRect(.5,.5,w-2,h-2);
        }

        /** XML Archival. */
        public XMLElement toXML(XMLArchiver anArchiver)
        {
            XMLElement e = new XMLElement("EtchBorder");
            if(_type==RAISED) e.add("Type", "RAISED");
            return e;
        }

        /** XML Unarchival. */
        public Border fromXML(XMLArchiver anArchiver, XMLElement anElement)
        {
            String type = anElement.getAttributeValue("Type", "LOWERED");
            if(type.equals("RAISED")) _type = RAISED;
            return this;
        }
    }

    /**
     * A subclass for compound border.
     */
    public static class CompoundBorder extends Border {

        // The two borders
        Border  _obdr, _ibdr;

        /** Creates a CompoundBorder. */
        public CompoundBorder(Border anOuterBdr, Border anInnerBdr)  { _obdr = anOuterBdr; _ibdr = anInnerBdr; }

        /** Returns the real border. */
        public Border getOuterBorder()  { return _obdr; }

        /** Returns the real border. */
        public Border getInnerBorder()  { return _ibdr; }

        /** Creates the insets. */
        protected Insets createInsets()  { return Insets.add(_obdr.getInsets(),_ibdr.getInsets()); }

        /** Paint border. */
        public void paint(Painter aPntr, Shape aShape)
        {
            _obdr.paint(aPntr, aShape);
            Insets ins = _obdr.getInsets();
            Rect bnds = aShape.getBounds(); if(bnds==aShape) bnds = bnds.clone(); bnds.inset(ins);
            _ibdr.paint(aPntr, bnds);
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

    /**
     * This Border subclass strokes the rectangular border of a given shape, with option include/exclude
     * individual sides.
     */
    public static class EdgeBorder extends LineBorder {

        // Whether to show left/right borders
        private boolean _showLeft = true, _showRight = true;

        // Whether to show top/bottom borders
        private boolean _showTop = true, _showBottom = true;

        /** Returns whether to show left border. */
        public boolean isShowLeft()  { return _showLeft; }

        /** Returns whether to show right border. */
        public boolean isShowRight()  { return _showRight; }

        /** Returns whether to show top border. */
        public boolean isShowTop()  { return _showTop; }

        /** Returns whether to show bottom border. */
        public boolean isShowBottom()  { return _showBottom; }

        /** Returns whether to show all borders. */
        public boolean isShowAll()  { return _showLeft && _showRight && _showTop && _showBottom; }

        /**
         * Paint border.
         */
        public void paint(Painter aPntr, Shape aShape)
        {
            Color color = getColor();
            Stroke stroke = getStroke();
            aPntr.setPaint(color);
            aPntr.setStroke(stroke);
            Shape spath = getBoxPath(aShape);
            aPntr.draw(spath);
        }

        /**
         * Returns the path to be stroked, transformed from the input path.
         */
        private Shape getBoxPath(Shape aShape)
        {
            // If showing all borders, just return bounds
            Rect rect = aShape.getBounds(); if(isShowAll()) return rect;
            boolean st = isShowTop(), sr = isShowRight(), sb = isShowBottom(), sl = isShowLeft();
            double w = rect.width, h = rect.height;

            // Otherwise, build path based on sides showing and return
            Path path = new Path();
            if(st) { path.moveTo(0,0); path.lineTo(w, 0); }
            if(sr) { if(!st) path.moveTo(w, 0); path.lineTo(w, h); }
            if(sb) { if(!sr) path.moveTo(w, h); path.lineTo(0, h); }
            if(sl) { if(!sb) path.moveTo(0, h); path.lineTo(0,0); }
            return path;
        }

        /**
         * Returns a duplicate stroke with new ShowTop.
         */
        public EdgeBorder copyForShowEdge(Pos aPos, boolean aValue)
        {
            EdgeBorder s = (EdgeBorder)clone();
            switch (aPos) {
                case CENTER_LEFT: s._showLeft = aValue; break;
                case CENTER_RIGHT: s._showRight = aValue; break;
                case TOP_CENTER: s._showTop = aValue; break;
                case BOTTOM_CENTER: s._showBottom = aValue;
            }
            return s;
        }

        /**
         * Standard equals implementation.
         */
        public boolean equals(Object anObj)
        {
            // Check identity, super, class and get other
            if(anObj==this) return true;
            if(!super.equals(anObj)) return false;
            EdgeBorder other = anObj instanceof EdgeBorder ? (EdgeBorder)anObj : null; if (other==null) return false;

            // Check ShowLeft, ShowRight, ShowTop, ShowBottom
            if(other._showLeft!=_showLeft) return false;
            if(other._showRight!=_showRight) return false;
            if(other._showTop!=_showTop) return false;
            if(other._showBottom!=_showBottom) return false;
            return true; // Return true since all checks passed
        }

        /**
         * XML archival.
         */
        public XMLElement toXML(XMLArchiver anArchiver)
        {
            // Archive basic stroke attributes
            XMLElement e = super.toXML(anArchiver); e.add("type", "border");

            // Archive ShowLeft, ShowRight, ShowTop, ShowBottom
            if(!isShowLeft()) e.add("show-left", false);
            if(!isShowRight()) e.add("show-right", false);
            if(!isShowTop()) e.add("show-top", false);
            if(!isShowBottom()) e.add("show-bottom", false);
            return e;
        }

        /**
         * XML unarchival.
         */
        public EdgeBorder fromXML(XMLArchiver anArchiver, XMLElement anElement)
        {
            // Unarchive basic stroke attributes
            super.fromXML(anArchiver, anElement);

            // Unarchive ShowLeft, ShowRight, ShowTop, ShowBottom
            if(anElement.hasAttribute("show-left")) _showLeft = anElement.getAttributeBoolValue("show-left");
            if(anElement.hasAttribute("show-right")) _showRight = anElement.getAttributeBoolValue("show-right");
            if(anElement.hasAttribute("show-top")) _showTop = anElement.getAttributeBoolValue("show-top");
            if(anElement.hasAttribute("show-bottom")) _showBottom = anElement.getAttributeBoolValue("show-bottom");
            return this;
        }
    }
}
