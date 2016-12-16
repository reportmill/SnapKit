package snap.swing;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import snap.util.*;

/**
 * This class has a bunch of convenience utility methods from drawing lines, RMRects, buttons, etc.
 */
public class AWTUtils {
    
    // A shared basic 1 point wide stroke
    public static final BasicStroke    Stroke1 = new BasicStroke(1);
    
    // A shared basic dashed stroke
    public static final BasicStroke    StrokeDash1 = new BasicStroke(2f, 0, 0, 1, new float[] { 2, 2 }, 0);
    
    // An event mask for the "Command" key (control on Windows, Apple key on Mac)
    public static final int    SHORTCUT_MASK = SnapUtils.isApp? Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() :0;
    
    // An event mask for "Control" key (control on Mac, alt on windows)
    public static final int    CONTROL_MASK = SnapUtils.isWindows? InputEvent.ALT_MASK : InputEvent.CTRL_MASK;

    // Used for button drawing methods
    private static Color               _white = new Color(.9f, .95f, 1);
    private static Color               _lightGray = new Color(.9f, .9f, .9f);
    private static Color               _darkGray = new Color(.58f, .58f, .58f);
    private static Color               _darkerGray = new Color(.5f, .5f, .5f);
    private static Color               _gray = new Color(.7f, .7f, .7f);
    
    // Some shared font instances
    public static Font Arial8 = new Font("Arial",Font.PLAIN,8);
    public static Font Arial10 = new Font("Arial", Font.PLAIN, 10);
    public static Font ArialBold10 = new Font("Arial", Font.BOLD, 10);
    public static Font Arial11 = new Font("Arial", Font.PLAIN, 11);
    public static Font ArialBold11 = new Font("Arial", Font.BOLD, 11);
    public static Font Arial12 = new Font("Arial", Font.PLAIN, 12);
    public static Font ArialBold12 = new Font("Arial", Font.BOLD, 12);
    public static Font Arial14 = new Font("Arial", Font.PLAIN, 14);
    public static Font ArialBold14 = new Font("Arial", Font.BOLD, 14);
    public static Font Helvetica10 = Arial10;
    public static Font Helvetica11 = Arial11;
    public static Font HelveticaBold11 = ArialBold11;
    public static Font Helvetica12 = Arial12;
    public static Font HelveticaBold12 = ArialBold12;

/**
 * Returns whether the given event has command down.
 */
public static boolean isShortcutDown(InputEvent anEvent)  { return (anEvent.getModifiers() & SHORTCUT_MASK)>0; }
    
/**
 * Returns whether the given input event has control down.
 */
public static boolean isControlDown(InputEvent anEvent)  { return (anEvent.getModifiers() & CONTROL_MASK)>0; }

/**
 * Returns a basic stroke with the given stroke width.
 */
public static BasicStroke getStroke(float aStrokeWidth)
{
    return new BasicStroke(aStrokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
}

/**
 * Draws a line between the given points.
 */
public static void drawLine(Graphics2D g, Point2D p1, Point2D p2)
{
    drawLine(g, p1.getX(), p1.getY(), p2.getX(), p2.getY());
}

/**
 * Draws a line between the given points.
 */
public static void drawLine(Graphics2D g, double p1x, double p1y, double p2x, double p2y)
{ _l.x1 = p1x; _l.y1 = p1y; _l.x2 = p2x; _l.y2 = p2y; g.draw(_l); }
static Line2D.Double _l = new Line2D.Double();

/**
 * Strokes the given rect.
 */
public static void drawRect(Graphics2D g, double x, double y, double w, double h)
{ _r.x = x; _r.y = y; _r.width = w; _r.height = h; g.draw(_r); }
static Rectangle2D.Double _r = new Rectangle2D.Double();

/**
 * Fills the given rect.
 */
public static void fillRect(Graphics2D g, double x, double y, double w, double h)
{ _r.x = x; _r.y = y; _r.width = w; _r.height = h; g.fill(_r); }

/**
 * Fills the given rect with simple 3D button effect.
 */
public static void fill3DRect(Graphics2D g, Rectangle2D aRect, boolean isRaised)
{
    fill3DRect(g, aRect.getX(), aRect.getY(), aRect.getWidth(), aRect.getHeight(), isRaised);
}

/**
 * Fills the given rect with simple 3D button effect.
 */
public static void fill3DRect(Graphics2D g, double x, double y, double w, double h, boolean isRaised)
{
    g.fill3DRect((int)x, (int)y, (int)w, (int)h, isRaised);
}

/**
 * Draws a button for the given rect with an option for pressed.
 */
public static void drawButton(Graphics2D g, Rectangle2D aRect, boolean isPressed)
{
    drawButton(g, aRect.getX(), aRect.getY(), aRect.getWidth(), aRect.getHeight(), isPressed);
}

/**
 * Draws a button for the given rect with an option for pressed.
 */
public static void drawButton(Graphics2D g, double x, double y, double w, double h, boolean isPressed)
{
    g.setColor(Color.black); fillRect(g, x, y, w, h); // g.setColor(Color.lightGray); fill3DRect(g, aRect, true);
    g.setColor(_white); fillRect(g, x, y, --w, --h);
    g.setColor(_darkGray); fillRect(g, ++x, ++y, --w, --h);
    g.setColor(_lightGray); fillRect(g, x, y, --w, --h);
    g.setColor(isPressed? _darkerGray : _gray); fillRect(g, ++x, ++y, --w, --h);
}

/**
 * Turns antialiasing on or off for a given graphics (returns previous state).
 */
public static boolean setAntialiasing(Graphics2D g, boolean aFlag)
{
    // Get old state, set (or reset) rendering hint and return
    boolean old = g.getRenderingHint(RenderingHints.KEY_ANTIALIASING)==RenderingHints.VALUE_ANTIALIAS_ON;
    if(aFlag) g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    else g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
    return old;
}

/**
 * Returns a string representation of a given Insets.
 */
public static String toStringInsets(Insets theInsets)
{
    if(theInsets.top==theInsets.left && theInsets.top==theInsets.bottom && theInsets.top==theInsets.right)
        return "" + theInsets.top;
    return (theInsets.top + "," + theInsets.left + "," + theInsets.bottom + "," + theInsets.right);
}

/**
 * Returns an Insets instance from given string.
 */
public static Insets fromStringInsets(String aString)
{
    String margins[] = aString.split("\\,");
    int top = margins.length>0? StringUtils.intValue(margins[0]) : 0;
    int left = margins.length>1? StringUtils.intValue(margins[1]) : 0;
    int bottom = margins.length>2? StringUtils.intValue(margins[2]) : 0;
    int right = margins.length>3? StringUtils.intValue(margins[3]) : 0;
    return new Insets(top, left, bottom, right);
}

/**
 * Returns a String/hex representation of a given color (eg, "#FF00D8").
 */
public static String toStringColor(Color aColor)
{
    int r = aColor.getRed(), g = aColor.getGreen(), b = aColor.getBlue();
    return "#" + (r<16 ? "0" : "") + Integer.toHexString(r) + (g<16 ? "0" : "") + Integer.toHexString(g) +
         (b<16 ? "0" : "") + Integer.toHexString(b);
}

/**
 * Returns a Color from a String/hex representation.
 */
public static Color fromStringColor(String aString)
{
    // If color string starts with #-sign, try to extract hex color
    if(aString.startsWith("#")) {
        int i = 0; try { i = Integer.decode("0x" + aString.substring(1)).intValue(); }
        catch(Exception e) { }
        return new Color((i >> 16) & 0xFF, (i >> 8) & 0xFF, i & 0xFF);
    }
    
    // Try to locate named color
    try { return (Color)Color.class.getField(aString).get(Color.black); }
    catch(Exception e) { }
    
    // Failing all else, return black
    return Color.black;
}

/**
 * A method to hide the cursor.
 */
public static Cursor getHiddenCursor()
{
    if(_blankCursor==null) {
        BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        _blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(img, new Point(0, 0), "blank cursor");
    }
    return _blankCursor;
} static Cursor _blankCursor;

}