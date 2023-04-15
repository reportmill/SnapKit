package snap.gfx;
import snap.geom.*;
import snap.util.XMLArchiver;
import snap.util.XMLElement;

import java.util.Arrays;
import java.util.Objects;

/**
 * This class holds a selection of Border subclasses.
 */
public class Borders {

    // Common borders
    public static final Border BLACK_BORDER = Border.createLineBorder(Color.BLACK, 1);
    public static final Border EMPTY_BORDER = Border.createLineBorder(Color.BLACK, 0);

    // Border constants
    private static Color BORDER_GRAY = Color.LIGHTGRAY;
    private static Color BORDER_DARKGRAY = BORDER_GRAY.darker();

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
        public LineBorder(Color aColor, double aWidth)
        {
            _color = aColor;
            _stroke = Stroke.getStroke(aWidth);
        }

        /** Creates lineBorder. */
        public LineBorder(Color aColor, Stroke aStroke)
        {
            _color = aColor;
            _stroke = aStroke;
        }

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
            // Get/set color and stroke
            Color color = getColor();
            Stroke stroke = getStroke();
            aPntr.setPaint(color);
            aPntr.setStroke(stroke);

            // Handle Rect special: Paint border just inside rect (if thinner/shorter than border stroke, don't go negative)
            if (aShape instanceof Rect) {

                // Get shape rect inset by half stroke width to keep border inside bounds
                Rect rect = (Rect) aShape;
                double borderW = getWidth();
                double insX = rect.width >= borderW ? borderW / 2 : rect.width / 2;
                double insY = rect.height >= borderW ? borderW / 2 : rect.height / 2;
                rect = rect.getInsetRect(insX, insY);

                // If stroke is dashed, see if we need to draw as horizontal or vertical line to avoid dash overlaps
                boolean isCustomDashArray = !Arrays.equals(stroke.getDashArray(), Stroke.DASH_SOLID);
                if (isCustomDashArray && (rect.width < .001 || rect.height < .001)) {
                    if (rect.width < .001)
                        aPntr.drawLine(rect.x, rect.y, rect.x, rect.getMaxY());
                    aPntr.drawLine(rect.x, rect.y, rect.getMaxX(), rect.y);
                }

                // Otherwise just draw (stroke) rect
                else aPntr.draw(rect);
            }

            // Handle arbitrary shape
            else aPntr.draw(aShape);

            // Restore stroke (bogus)
            aPntr.setStroke(Stroke.Stroke1);
        }

        /**
         * Returns copy of this border with new color.
         */
        public LineBorder copyForColor(Color aColor)
        {
            LineBorder c = clone();
            c._color = aColor;
            return c;
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
            return (LineBorder) super.clone();
        }

        /**
         * Standard equals implementation.
         */
        public boolean equals(Object anObj)
        {
            // Check identity, superclass and get other
            if(anObj == this) return true;
            if(!super.equals(anObj)) return false;
            LineBorder other = (LineBorder) anObj;
            return other._stroke.equals(_stroke);
        }

        /** XML Archival. */
        public XMLElement toXML(XMLArchiver anArchiver)
        {
            XMLElement e = super.toXML(anArchiver);
            if(!_color.equals(Color.BLACK))
                e.add("Color", '#' + _color.toHexString());
            if(getWidth() != 1)
                e.add("Width", getWidth());
            return e;
        }

        /** XML Unarchival. */
        public Border fromXML(XMLArchiver anArchiver, XMLElement anElement)
        {
            if(anElement.hasAttribute("Color"))
                _color = new Color(anElement.getAttributeValue("Color"));
            if(anElement.hasAttribute("line-color"))
                _color = new Color(anElement.getAttributeValue("line-color"));
            if(anElement.hasAttribute("Width"))
                _stroke = _stroke.copyForWidth(anElement.getAttributeFloatValue("Width"));
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
            Rect bounds = aShape.getBounds();
            double boundsX = bounds.x;
            double boundsY = bounds.y;
            double boundsW = bounds.width;
            double boundsH = bounds.height;

            // Paint highlight
            aPntr.setColor(Color.WHITE);
            aPntr.setStroke(Stroke.Stroke1);
            aPntr.drawRect(boundsX + .5,boundsY + .5,boundsW - 1,boundsH - 1);
            aPntr.drawRect(boundsX + 1.5,boundsY + 1.5,boundsW - 3,boundsH - 3);

            // Paint Lowered
            if(_type == LOWERED) {
                aPntr.setColor(BORDER_GRAY);
                aPntr.drawLine(boundsX + .5,boundsY + .5,boundsX + boundsW - 1,boundsY + .5);
                aPntr.drawLine(boundsX + .5,boundsY + .5,boundsX + .5,boundsY + boundsH - 1);
                aPntr.setColor(BORDER_DARKGRAY);
                aPntr.drawLine(boundsX + 1.5,boundsY + 1.5,boundsX + boundsW-3,boundsY + 1.5);
                aPntr.drawLine(boundsX + 1.5,boundsY + 1.5,boundsX + 1.5,boundsY + boundsH - 3);
            }

            // Paint Raised
            else {
                aPntr.setColor(BORDER_DARKGRAY);
                aPntr.drawLine(boundsX + .5,boundsY + boundsH - .5,boundsX + boundsW - 1,boundsY + boundsH - .5);
                aPntr.drawLine(boundsX + boundsW - .5,boundsY + .5,boundsX + boundsW - .5,boundsY + boundsH - 1);
                aPntr.setColor(BORDER_GRAY);
                aPntr.drawLine(boundsX + 1.5,boundsY + boundsH - 1.5,boundsX + boundsW - 3,boundsY + boundsH - 1.5);
                aPntr.drawLine(boundsX + boundsW - 1.5,boundsY + 1.5,boundsX + boundsW - 1.5,boundsY + boundsH - 3);
            }
        }

        /** XML Archival. */
        public XMLElement toXML(XMLArchiver anArchiver)
        {
            XMLElement e = super.toXML(anArchiver);
            if(_type == RAISED)
                e.add("Type", "RAISED");
            return e;
        }

        /** XML Unarchival. */
        public Border fromXML(XMLArchiver anArchiver, XMLElement anElement)
        {
            String type = anElement.getAttributeValue("bevel-type", "lowered");
            if(anElement.hasAttribute("Type"))
                type = anElement.getAttributeValue("Type");
            if(anElement.hasAttribute("bevel-type"))
                type = anElement.getAttributeValue("bevel-type");
            if(type.equals("RAISED") || type.equals("raised"))
                _type = RAISED;
            return this;
        }

        @Override
        public boolean equals(Object anObj)
        {
            if (this == anObj) return true;
            if (anObj == null || getClass() != anObj.getClass()) return false;
            if (!super.equals(anObj)) return false;
            BevelBorder that = (BevelBorder) anObj;
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

        /** Creates new EtchBorder. */
        public EtchBorder() { }

        /** Creates the insets. */
        protected Insets createInsets()  { return new Insets(2); }

        /** Paint border. */
        public void paint(Painter aPntr, Shape aShape)
        {
            aPntr.setStroke(Stroke.Stroke1);
            Rect rect = aShape.getBounds();
            double w = rect.getWidth(), h = rect.getHeight();
            aPntr.setColor(Color.WHITE);
            aPntr.drawRect(1.5,1.5,w-2,h-2);
            aPntr.setColor(BORDER_GRAY);
            aPntr.drawRect(.5,.5,w-2,h-2);
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

        /**
         * Constructor.
         */
        public EdgeBorder()
        {
            super();
        }

        /**
         * Constructor for edges.
         */
        public EdgeBorder(boolean showTop, boolean showRight, boolean showBottom, boolean showLeft)
        {
            this();
            _showLeft = showLeft;
            _showRight = showRight;
            _showTop = showTop;
            _showBottom = showBottom;
        }

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
            boolean showTop = isShowTop();
            boolean showRight = isShowRight();
            boolean showBottom = isShowBottom();
            boolean showLeft = isShowLeft();
            double rectW = rect.width;
            double rectH = rect.height;

            // Otherwise, build path based on sides showing and return
            Path2D path = new Path2D();
            if(showTop) { path.moveTo(0,0); path.lineTo(rectW, 0); }
            if(showRight) { if(!showTop) path.moveTo(rectW, 0); path.lineTo(rectW, rectH); }
            if(showBottom) { if(!showRight) path.moveTo(rectW, rectH); path.lineTo(0, rectH); }
            if(showLeft) { if(!showBottom) path.moveTo(0, rectH); path.lineTo(0,0); }
            return path;
        }

        /**
         * Returns a duplicate stroke with new ShowTop.
         */
        public EdgeBorder copyForShowEdge(Pos aPos, boolean aValue)
        {
            EdgeBorder clone = (EdgeBorder) clone();
            switch (aPos) {
                case CENTER_LEFT: clone._showLeft = aValue; break;
                case CENTER_RIGHT: clone._showRight = aValue; break;
                case TOP_CENTER: clone._showTop = aValue; break;
                case BOTTOM_CENTER: clone._showBottom = aValue;
            }
            return clone;
        }

        /**
         * Standard equals implementation.
         */
        public boolean equals(Object anObj)
        {
            // Check identity, super, class and get other
            if(anObj == this) return true;
            if(!super.equals(anObj)) return false;
            EdgeBorder other = anObj instanceof EdgeBorder ? (EdgeBorder) anObj : null; if (other == null) return false;

            // Check ShowLeft, ShowRight, ShowTop, ShowBottom
            if(other._showLeft != _showLeft) return false;
            if(other._showRight != _showRight) return false;
            if(other._showTop != _showTop) return false;
            if(other._showBottom != _showBottom) return false;
            return true; // Return true since all checks passed
        }

        /**
         * XML archival.
         */
        public XMLElement toXML(XMLArchiver anArchiver)
        {
            // Archive basic stroke attributes
            XMLElement e = super.toXML(anArchiver);
            e.add("type", "border");

            // Archive ShowLeft, ShowRight, ShowTop, ShowBottom
            if(!isShowLeft())
                e.add("show-left", false);
            if(!isShowRight())
                e.add("show-right", false);
            if(!isShowTop())
                e.add("show-top", false);
            if(!isShowBottom())
                e.add("show-bottom", false);
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
            if(anElement.hasAttribute("show-left"))
                _showLeft = anElement.getAttributeBoolValue("show-left");
            if(anElement.hasAttribute("show-right"))
                _showRight = anElement.getAttributeBoolValue("show-right");
            if(anElement.hasAttribute("show-top"))
                _showTop = anElement.getAttributeBoolValue("show-top");
            if(anElement.hasAttribute("show-bottom"))
                _showBottom = anElement.getAttributeBoolValue("show-bottom");
            return this;
        }
    }
}
