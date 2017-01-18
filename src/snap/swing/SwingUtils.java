package snap.swing;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.List;
import javax.swing.*;
import snap.util.SnapUtils;

/**
 * This class offers a number of useful general purpose Swing utilities.
 */
public class SwingUtils {

/**
 * Returns the first parent of given component which is window.
 */
public static Window getWindow(Component aComponent)  { return getParent(aComponent, Window.class); }

/**
 * Returns the first parent of given component which is an instance of given class.
 */
public static <T> T getParent(Component aComponent, Class <T> aClass)
{
    while(aComponent!=null && !aClass.isInstance(aComponent))
        aComponent = aComponent.getParent();
    return (T)aComponent;
}

/**
 * Returns a string from given transferable.
 */
public static String getString(Transferable aTrans)
{
    // Handle StringFlavor
    if(aTrans.isDataFlavorSupported(DataFlavor.stringFlavor))
        try { return (String)aTrans.getTransferData(DataFlavor.stringFlavor); }
        catch(Exception e) { e.printStackTrace(); return null; }
    
    // Handle FileList
    List <File> files = getFiles(aTrans);
    if(files!=null && files.size()>0)
        return files.get(0).getAbsolutePath();
    
    // Otherwise return null
    return null;
}

/**
 * Returns a list of files from a given transferable.
 */
public static List <File> getFiles(Transferable aTrans)
{
    // Handle JavaFileListFlavor
    if(aTrans.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
        try { return (List)aTrans.getTransferData(DataFlavor.javaFileListFlavor); }
        catch(Exception e) { System.err.println(e); return null; }
    
    // Otherwise return null
    return null;
}

/**
 * This utility method returns key text for a key stroke and tries to make it more conforming.
 */
public static String getKeyText(KeyStroke aKeyStroke)
{
    // Get normal key text
    String keyText = KeyEvent.getKeyText(aKeyStroke.getKeyCode());
    
    // Do some substitutions
    keyText = keyText.replace("Semicolon", ";");
    keyText = keyText.replace("Back Slash", "\\");
    keyText = keyText.replace("Open Bracket", "[");
    keyText = keyText.replace("Close Bracket", "]");
    
    // Return key text
    return keyText;
}

/**
 * This utility method tries to get a keystroke from a string and tries to be more forgiving than
 * KeyStroke.getKeyStroke().
 */
public static KeyStroke getKeyStroke(String aKey)
{
    // Get key width some JFX conversions
    String key = aKey.replace("Shortcut", "meta").replace("+", " ").replace("Shift", "shift");
    if(key.equals("ESC")) key = "ESCAPE";
    if(key.equals("BACKSPACE")) key = "BACK_SPACE";

    // If Windows, convert "meta" to "control"
    if(SnapUtils.isWindows) key = key.replace("meta", "control");
    
    // Try normal KeyStroke method
    KeyStroke kstroke = KeyStroke.getKeyStroke(key);
    if(kstroke!=null)
        return kstroke;
    
    // Do some common substitutions
    key = key.replace(";", "SEMICOLON");
    key = key.replace("\\", "BACK_SLASH");
    key = key.replace("[", "OPEN_BRACKET");
    key = key.replace("]", "CLOSE_BRACKET");
    
    // Get last component and make sure its in upper case
    int index = key.lastIndexOf(" ") + 1;
    key = key.substring(0, index) + key.substring(index).toUpperCase();

    // Try again
    kstroke = KeyStroke.getKeyStroke(key);
    if(kstroke==null) System.err.println("SwingUtils.getKeyStroke: Invalid key accelerator format: " + aKey);
    return kstroke;
}

/**
 * Returns the size available from given point to in component to bottom right of component screen.
 */
public static Dimension getScreenSizeAvailable(Component aComp, int anX, int aY)
{
    Rectangle rect = getScreenBounds(aComp, anX, aY, true);
    Point sp = getScreenLocation(aComp, anX, aY);
    return new Dimension(rect.x + rect.width - sp.x, rect.y + rect.height - sp.y);
}

/**
 * Returns the screen bounds for a component location (or screen location if component null).
 */
public static Rectangle getScreenBounds(Component aComp, int anX, int aY, boolean doInset)
{
    GraphicsConfiguration gc = getGraphicsConfiguration(aComp, anX, aY);
    Rectangle rect = gc!=null? gc.getBounds() : new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
    Insets ins = doInset && gc!=null? Toolkit.getDefaultToolkit().getScreenInsets(gc) : null;
    if(ins!=null) { int lt = ins.left, rt = ins.right, tp = ins.top, bt = ins.bottom;
        rect.setBounds(rect.x + lt, rect.y + tp, rect.width - lt - rt, rect.height - tp - bt); }
    return rect;
}

/**
 * Returns the GraphicsConfiguration for a point.
 */
public static GraphicsConfiguration getGraphicsConfiguration(Component aComp, int anX, int aY)
{
    // Get initial GC from component (if available) and point on screen
    GraphicsConfiguration gc = aComp!=null? aComp.getGraphicsConfiguration() : null;
    Point spoint = getScreenLocation(aComp, anX, aY);
    
    // Replace with alternate GraphicsConfiguration if point on another screen
    for(GraphicsDevice gd : GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()) {
        if(gd.getType() == GraphicsDevice.TYPE_RASTER_SCREEN) {
            GraphicsConfiguration dgc = gd.getDefaultConfiguration();
            if(dgc.getBounds().contains(spoint.x, spoint.y)) {
                gc = dgc; break; }
        }
    }
    
    // Return GraphicsConfiguration
    return gc;
}

/**
 * Returns the screen location for a component and X and Y.
 */
private static Point getScreenLocation(Component aComp, int anX, int aY)
{
    Point point = aComp!=null && aComp.isShowing()? aComp.getLocationOnScreen() : new Point();
    point.x += anX; point.y += aY; return point;
}

}