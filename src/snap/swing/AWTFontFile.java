package snap.swing;
import java.awt.*;
import java.awt.font.*;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.*;
import java.util.*;
import snap.geom.Path2D;
import snap.geom.Rect;
import snap.gfx.FontFile;
import snap.geom.Shape;
import snap.util.*;

/**
 * This class represents all the information about a font that is independent of size. This allows Font to be 
 * lighter weight (essentially just a font file at a given size).
 */
public class AWTFontFile extends FontFile {
    
    // Cached font name
    private String  _fontName;
    
    // Cached font name in English
    private String  _fontNameEnglish;
    
    // This font files AWT representation
    private java.awt.Font  _awt;
    
    // Cached font metrics
    private FontMetrics  _fontMetrics;

    // Cached graphics object
    private static Graphics2D  _g2d;

    /**
     * Creates a font file for a given font name.
     */
    public AWTFontFile(String aName)
    {
        // Get AWT font for given name and FontMetrics for font
        _awt = AWTFontUtils.getFont(aName, 1000f);
        _fontMetrics = getGraphics2D().getFontMetrics(_awt);

        // Cache font name and English name, so we have normalized versions
        _fontName = AWTFontUtils.getFontNameNormalized(_awt.getFontName());
        _fontNameEnglish = AWTFontUtils.getFontNameNormalized(_awt.getFontName(Locale.ENGLISH));
    }

    /**
     * Returns the name of this font.
     */
    public String getName()  { return _fontName; }

    /**
     * Returns the name of this font in English.
     */
    public String getNameEnglish()  { return _fontNameEnglish; }

    /**
     * Returns the family name of this font.
     */
    public String getFamily()  { return _awt.getFamily(); }

    /**
     * Returns the family name of this font in English.
     */
    public String getFamilyEnglish()  { return _awt.getFamily(Locale.ENGLISH); }

    /**
     * Returns the PostScript name of this font.
     */
    public String getPSName()  { return _awt.getPSName(); }

    /**
     * Returns the char advance for the given char.
     */
    protected double charAdvanceImpl(char aChar)  { return _fontMetrics.charWidth(aChar) / 1000d; }

    /**
     * Returns the bounds rect for glyphs in given string.
     */
    /*public Rect getStringBounds(String aString)
    {
        // Get bounds: Contains the origin, ascent, advance and height, including lineheight
        Rectangle2D bnds = _awt.getStringBounds(aString, frc);
        double glyphAsc = -bnds.getY();
        double glyphW = bnds.getWidth();
        double glyphH = bnds.getHeight();
        return new Rect(0, glyphAsc, glyphW, glyphH);
    }*/

    /**
     * Returns the bounds rect for glyphs in given string.
     */
    public Rect getGlyphBounds(String aString)
    {
        // Get bounds: Contains the origin, ascent, advance and height
        Graphics2D gfx = getGraphics2D();
        FontRenderContext fontRenderContext = gfx.getFontRenderContext();
        TextLayout textLayout = new TextLayout(aString, _awt, fontRenderContext);
        Rectangle2D bounds = textLayout.getBounds();

        // Get parts
        double glyphX = bounds.getX();
        double glyphAsc = bounds.getY();
        double glyphW = bounds.getWidth() + glyphX;
        double glyphH = bounds.getHeight();

        // Return
        return new Rect(0, glyphAsc, glyphW, glyphH);
    }

    /**
     * Returns the path for a given char at 1000pt.
     */
    protected Shape getCharPathImpl(char c)
    {
        // Get default graphics 2D, glyph vector for char and shape from glyph vector
        Graphics2D gfx = getGraphics2D();
        FontRenderContext fontRenderContext = gfx.getFontRenderContext();
        GlyphVector glyphVector = _awt.createGlyphVector(fontRenderContext, new char[] { c });
        java.awt.Shape shape = glyphVector.getOutline();
        return getShape(shape, true);
    }

    /**
     * Returns the path for given string in given point size with character spacing.
     */
    public Shape getOutline(CharSequence aStr, double aSize, double aX, double aY, double aCharSpacing)
    {
        // Get graphics, font render context and glyph vector
        String str = StringUtils.trimEnd(aStr);
        Graphics2D gfx = getGraphics2D();
        FontRenderContext fontRenderContext = gfx.getFontRenderContext();
        Font awtFont = _awt.deriveFont((float) aSize);
        GlyphVector glyphVector = awtFont.createGlyphVector(fontRenderContext, str);

        // Adjust glyph positions
        Point2D.Double point = new Point2D.Double(aX, aY);
        for (int i = 0, iMax = str.length(); i < iMax; i++) {
            glyphVector.setGlyphPosition(i, point);
            point.x += charAdvance(str.charAt(i)) * aSize + aCharSpacing;
        }

        // Return glyph vector
        java.awt.Shape glyphOutline = glyphVector.getOutline();
        return getShape(glyphOutline, false);
    }

    /**
     * Returns a shape for given java.awt.Shape with option to flip.
     */
    private Shape getShape(java.awt.Shape aShape, boolean flip)
    {
        // Loop vars
        Path2D path = new Path2D();
        double[] points = new double[6];

        // Iterate over shape segments and build path
        for (PathIterator pathIter = aShape.getPathIterator(null); !pathIter.isDone(); pathIter.next()) {

            // Get segment and points
            int type = pathIter.currentSegment(points);
            if (flip) {
                points[1] = -points[1];
                points[3] = -points[3];
                points[5] = -points[5];
            }

            // Handle segments
            switch (type) {
                case PathIterator.SEG_MOVETO: path.moveTo(points[0], points[1]); break;
                case PathIterator.SEG_LINETO: path.lineTo(points[0], points[1]); break;
                case PathIterator.SEG_QUADTO: path.quadTo(points[0], points[1], points[2], points[3]); break;
                case PathIterator.SEG_CUBICTO: path.curveTo(points[0], points[1], points[2], points[3], points[4], points[5]); break;
                case PathIterator.SEG_CLOSE: path.close();
            }
        }

        // Return path
        return path;
    }

    /**
     * Returns the max distance above the baseline that this font goes.
     */
    public double getAscent()  { return _fontMetrics.getAscent() / 1000f; }

    /**
     * Returns the max distance below the baseline that this font goes.
     */
    public double getDescent()  { return _fontMetrics.getDescent() / 1000f; }

    /**
     * Returns the default distance between lines for this font.
     */
    public double getLeading()  { return _fontMetrics.getLeading() / 1000f; }

    /**
     * Returns the max advance of characters in this font.
     */
    public double getMaxAdvance()  { return _fontMetrics.getMaxAdvance() / 1000f; }

    /**
     * Returns the default thickness that an underline should be drawn.
     */
    public double getUnderlineThickness()
    {
        // Get the AWT Font's LineMetrics for X and return underline thickness
        Graphics2D gfx = getGraphics2D();
        FontRenderContext fontRenderContext = gfx.getFontRenderContext();
        LineMetrics lineMetrics = _awt.getLineMetrics("X", fontRenderContext);
        return lineMetrics.getUnderlineThickness()/1000f;
    }

    /**
     * Returns if this font can display the given char.
     */
    protected boolean canDisplayImpl(char aChar)  { return _awt.canDisplay(aChar); }

    /**
     * Returns the awt font.
     */
    public java.awt.Font getNative()  { return _awt; }

    /**
     * Returns the awt font.
     */
    public String getNativeName()  { return _awt.getFontName(); }

    /**
     * Returns the awt font.
     */
    public java.awt.Font getNative(double aSize)  { return _awt.deriveFont((float) aSize); }

    /**
     * Returns the font name of this font file.
     */
    public String toString()  { return getName(); }

    /**
     * Returns a shared graphics objects that can be used to get a font render context.
     */
    static synchronized Graphics2D getGraphics2D()
    {
        // If already set, just return
        if (_g2d != null) return _g2d;

        // Create/configure Graphics2D
        BufferedImage img = new BufferedImage(1,1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        //g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        //g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);

        // Set, return
        return _g2d = g2d;
    }
}