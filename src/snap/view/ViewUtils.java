/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import java.text.DecimalFormat;
import java.util.*;
import snap.gfx.*;
import snap.web.WebFile;

/**
 * Utility methods for nodes.
 */
public class ViewUtils {
    
    // Booleans for input event state and modifiers
    static boolean          _altDown, _cntrDown, _metaDown, _mouseDown, _shiftDown, _shortcutDown;

    // The current active Dragboard
    static Dragboard        _activeDragboard;
    
    // Color constants
    private static Color BACK_FILL = new Color("#F1F1F1");
    private static Color BACK_DARK_FILL = new Color("#C0C0C0");
    private static Color SELECT_COLOR = new Color("#0032D0");
    private static Color TARGET_COLOR = new Color("#4080F0");

    // Image Constants
    static Image        RootFile, DirFile, ClassFile, JavaFile, TableFile, PlainFile;

/**
 * Returns whether alt is down.
 */
public static boolean isAltDown()  { return _altDown; }

/**
 * Returns whether control is down.
 */
public static boolean isControlDown()  { return _cntrDown; }

/**
 * Returns whether meta is down.
 */
public static boolean isMetaDown()  { return _metaDown; }

/**
 * Returns whether mouse is down.
 */
public static boolean isMouseDown()  { return _mouseDown; }

/**
 * Returns whether shift is down.
 */
public static boolean isShiftDown()  { return _shiftDown; }

/**
 * Returns whether shortcut is down.
 */
public static boolean isShortcutDown()  { return _shortcutDown; }

/**
 * Returns the selection color.
 */
public static Paint getSelectFill()  { return SELECT_COLOR; }

/**
 * Returns the selection color.
 */
public static Paint getSelectTextFill()  { return Color.WHITE; }

/**
 * Returns the selection color.
 */
public static Paint getTargetFill()  { return TARGET_COLOR; }

/**
 * Returns the selection color.
 */
public static Paint getTargetTextFill()  { return Color.WHITE; }

/**
 * Returns the background fill.
 */
public static Paint getBackFill()  { return BACK_FILL; }

/**
 * Returns the background fill.
 */
public static Paint getBackDarkFill()  { return BACK_DARK_FILL; }

/**
 * Paint nodes.
 */
public static void paintAll(View aView, Painter aPntr)
{
    aPntr.save();
    if(aView.getClip()!=null)
        aPntr.clip(aView.getClip());
    layoutDeep(aView);
    aView.paintAll(aPntr);
    aPntr.restore();
}

/**
 * Layout nodes deep.
 */
public static void layoutDeep(View aView)
{
    ParentView par = aView instanceof ParentView? (ParentView)aView : null; if(par==null) return;
    par.layout();
    for(View child : par.getChildren())
        layoutDeep(child);
}

/**
 * Set a View showing.
 */
public static void setShowing(View aView, boolean aValue)  { aView.setShowing(aValue); }

/**
 * Set a View focused.
 */
public static void setFocused(View aView, boolean aValue)  { aView.setFocused(aValue); }

/**
 * Process an event.
 */
public static void processEvent(View aView, ViewEvent anEvent)  { aView.processEvent(anEvent); }

/**
 * Set active Dragboard.
 */
public static void setActiveDragboard(Dragboard aDragboard)  { _activeDragboard = aDragboard; }

/**
 * Returns the align x factor.
 */
public static final double getAlignX(View aView)  { return getAlignX(aView.getAlign()); }
    
/**
 * Returns the align y factor.
 */
public static final double getAlignY(View aView)  { return getAlignY(aView.getAlign()); }
    
/**
 * Returns the align x factor.
 */
public static final double getAlignX(Pos aPos)  { return getAlignX(aPos.getHPos()); }
    
/**
 * Returns the align y factor.
 */
public static final double getAlignY(Pos aPos)  { return getAlignY(aPos.getVPos()); }
    
/**
 * Returns the align x factor.
 */
public static final double getAlignX(HPos aPos)  { return aPos==HPos.RIGHT? 1 : aPos==HPos.CENTER? .5 : 0; }
    
/**
 * Returns the align y factor.
 */
public static final double getAlignY(VPos aPos)  { return aPos==VPos.BOTTOM? 1 : aPos==VPos.CENTER? .5 : 0; }
    
/**
 * Returns the child of given class hit by coords.
 */
public static View getChildAt(View aView, double aX, double aY)  { return getChildAt(aView, aX, aY, null); }

/**
 * Returns the child of given class hit by coords.
 */
public static <T extends View> T getChildAt(View aView, double aX, double aY, Class <T> aClass)
{
    ParentView par = aView instanceof ParentView? (ParentView)aView : null; if(par==null) return null;
    List <View> children = par.getChildren();
    for(int i=children.size()-1; i>=0; i--) { View child = children.get(i); if(!child.isPickable()) continue;
        Point p = child.parentToLocal(aX, aY);
        if(child.contains(p.x,p.y) && (aClass==null || aClass.isInstance(child)))
            return (T)child;
    }
    return null;
}

/**
 * Returns the view or child view hit by given coords, starting at given view.
 */
public static View getDeepestViewAt(View aView, double aX, double aY)  { return getDeepestViewAt(aView,aX,aY,null); }

/**
 * Returns the view or child view of given class hit by given coords, starting at given view.
 */
public static <T extends View> T getDeepestViewAt(View aView, double aX, double aY, Class <T> aClass)
{
    T view = getDeepestChildAt(aView, aX, aY, aClass);
    if(view==null && aView.contains(aX,aY) && (aClass==null || aClass.isInstance(aView))) view = (T)aView;
    return view;
}

/**
 * Returns the deepest child hit by given coords starting with children of given view.
 */
public static View getDeepestChildAt(View aView, double aX, double aY)  { return getDeepestChildAt(aView,aX,aY,null); }

/**
 * Returns the deepest child of given class hit by given coords starting with children of given view.
 */
public static <T extends View> T getDeepestChildAt(View aView, double aX, double aY, Class <T> aClass)
{
    ParentView par = aView instanceof ParentView? (ParentView)aView : null; if(par==null) return null;
    List <View> children = par.getChildren();
    for(int i=children.size()-1; i>=0; i--) { View child = children.get(i); if(!child.isPickable()) continue;
        Point p = child.parentToLocal(aX, aY);
        if(child.contains(p.x,p.y)) {
            T hcdeep = getDeepestChildAt(child, p.x, p.y, aClass);
            if(hcdeep!=null)
                return hcdeep;
            if(aClass==null || aClass.isInstance(child))
                return (T)child;
        }
    }
    return null;
}

/**
 * Returns a common ancestor for two nodes.
 */
public static View getCommonAncetor(View aView1, View aView2)
{
    for(View n1=aView1;n1!=null;n1=n1.getParent())
        for(View n2=aView2;n2!=null;n2=n2.getParent())
            if(n1==n2) return n1;
    return null;
}

/**
 * Returns the image for a file.
 */
public static Image getFileIconImage(WebFile aFile)
{
    if(RootFile==null) loadFileIconImages();
    if(aFile.isRoot()) return RootFile;
    if(aFile.isDir()) return DirFile;
    if(aFile.getType().equals("class")) return ClassFile;
    if(aFile.getType().equals("java")) return JavaFile;
    if(aFile.getType().equals("table")) return TableFile;
    return PlainFile;
}

/** Loads the file icon images. */
private static void loadFileIconImages()
{
    RootFile = Image.get(ViewUtils.class, "RootFile.png");
    DirFile = Image.get(ViewUtils.class, "DirFile.png");
    ClassFile = Image.get(ViewUtils.class, "ClassFile.png");
    JavaFile = Image.get(ViewUtils.class, "JavaFile.png");
    TableFile = Image.get(ViewUtils.class, "TableFile.png");
    PlainFile = Image.get(ViewUtils.class, "PlainFile.png");
}

/**
 * Returns an image for a View.
 */
public static Image getImage(View aView)
{
    Image img = Image.get((int)Math.ceil(aView.getWidth()), (int)Math.ceil(aView.getHeight()), true);
    Painter pntr = img.getPainter();
    paintAll(aView, pntr); pntr.flush();
    return img;
}
    
/**
 * Beep.
 */
public static void beep()  { GFXEnv.getEnv().beep(); }

/**
 * Prints a View hierarchy.
 */
public static void print(View aView, int aLevel)
{
    String indent = ""; for(int i=0;i<aLevel;i++) indent += "  ";
    System.out.printf("%s%s %s [%s %s %s %s]\n", indent, aView.getClass().getSimpleName(), aView.getName(),
        fmt(aView.getX()), fmt(aView.getY()), fmt(aView.getWidth()), fmt(aView.getHeight()));
    if(aView instanceof ParentView) { ParentView par = (ParentView)aView;
        for(View child : par.getChildren()) print(child, aLevel+1); }
}

/**
 * Copies a list of menu items.
 */
public static List <MenuItem> copyMenuItems(List <MenuItem> theItems)
{
    List <MenuItem> copy = new ArrayList();
    for(MenuItem mi : theItems) copy.add(copyMenuItem(mi));
    return copy;
}

/**
 * Copies a menu item.
 */
public static MenuItem copyMenuItem(MenuItem anItem)
{
    MenuItem mi = null;
    //if(anItem instanceof SeparatorMenuItem) mi = new SeparatorMenuItem(); else
    if(anItem instanceof Menu) { Menu menu = (Menu)anItem;
        Menu menu2 = new Menu(); mi = menu2;
        List <MenuItem> citems = copyMenuItems((List)menu.getChildren());
        for(MenuItem i : citems) menu2.addItem(i);
    }
    else mi = new MenuItem();
    mi.setText(anItem.getText()); mi.setName(anItem.getName()); mi.setShortcut(anItem.getShortcut());
    return mi;
}

// Used for print
private static String fmt(double aValue)  { return _fmt.format(aValue); }
private static DecimalFormat _fmt = new DecimalFormat("0.##");

}