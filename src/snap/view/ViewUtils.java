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
    static boolean          _altDown, _cntrDown, _metaDown, _mouseDown, _mouseDrag, _shiftDown, _shortcutDown;
    
    // The last MouseDown event
    static ViewEvent        _lastMouseDown;

    // Color constants
    private static Color BACK_FILL = new Color("#E9E8EA");
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
 * Returns whether mouse is being dragged.
 */
public static boolean isMouseDrag()  { return _mouseDrag; }

/**
 * Returns whether shift is down.
 */
public static boolean isShiftDown()  { return _shiftDown; }

/**
 * Returns whether shortcut is down.
 */
public static boolean isShortcutDown()  { return _shortcutDown; }

/**
 * Returns the last MouseDown event.
 */
public static ViewEvent getMouseDown()  { return _lastMouseDown; }

/**
 * Sets the MouseDown event.
 */
public static void setMouseDown(ViewEvent anEvent)
{
    // If time within range, bump click count
    if(_lastMouseDown!=null && (anEvent.getWhen() - _lastMouseDown.getWhen() < ViewEvent.CLICK_TIME))
        anEvent.setClickCount(_lastMouseDown.getClickCount()+1);
        
    // Set new event
    _lastMouseDown = anEvent; _mouseDown = true;
}

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
 * Returns the bounds of a given view list.
 */
public static Rect getBoundsOfViews(View aPar, List <? extends View> aList)
{
    // If list is null or empty, return this shape's bounds inside
    if(aList==null || aList.size()==0)
        return aPar.getBoundsLocal();
    
    // Declare and initialize a rect to frame of first shape in list
    View child0 = aList.get(0);
    Rect rect = child0.localToParent(child0.getBoundsLocal()).getBounds();
    
    // Iterate over successive shapes in list and union their frames
    for(int i=1, iMax=aList.size(); i<iMax; i++) { View child = aList.get(i);
        Rect bnds = child.localToParent(child.getBoundsLocal()).getBounds();
        rect.unionEvenIfEmpty(bnds);
    }
    
    // Return frame
    return rect;
}

/**
 * Returns an identifier string for a given view.
 */
public static String getId(View aView)
{
    String name = aView.getName()!=null? aView.getName() : aView.getClass().getSimpleName();
    return name + " " + System.identityHashCode(aView);
}

/**
 * Paint nodes.
 */
public static void paintAll(View aView, Painter aPntr)
{
    aPntr.save();
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
    if(par.isNeedsLayout())
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
 * Returns the lean x factor.
 */
public static final double getLeanX(View aView)  { return getAlignX(aView.getLeanX()); }
    
/**
 * Returns the lean y factor.
 */
public static final double getLeanY(View aView)  { return getAlignY(aView.getLeanY()); }

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
    View children[] = par.getChildren();
    for(int i=children.length-1; i>=0; i--) { View child = children[i]; if(!child.isPickable()) continue;
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
    View children[] = par.getChildren();
    for(int i=children.length-1; i>=0; i--) { View child = children[i]; if(!child.isPickable()) continue;
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
 * Binds a view property to another view's same prop.
 */
public static void bind(View aView1, String aProp, View aView2, boolean doBoth)
{
    aView1.addPropChangeListener(pc -> aView2.setValue(aProp, pc.getNewValue()), aProp);
    if(doBoth) aView2.addPropChangeListener(pc -> aView1.setValue(aProp, pc.getNewValue()), aProp);
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
        List <MenuItem> citems = copyMenuItems(menu.getItems());
        for(MenuItem i : citems) menu2.addItem(i);
    }
    else mi = new MenuItem();
    mi.setText(anItem.getText()); mi.setName(anItem.getName()); mi.setShortcut(anItem.getShortcut());
    return mi;
}

// Used for print
private static String fmt(double aValue)  { return _fmt.format(aValue); }
private static DecimalFormat _fmt = new DecimalFormat("0.##");

/**
 * Silly feature to make any view draggable.
 */
public static void enableDragging(View aView)
{
    aView.addEventHandler(e -> handleDrag(e), View.MousePress, View.MouseDrag, View.MouseRelease);
}

/** Helper for enableDragging. */
static void handleDrag(ViewEvent anEvent)
{
    View view = anEvent.getView(); view.setManaged(false); anEvent.consume();
    Point mpt = _mpt; _mpt = anEvent.getPoint(view.getParent());
    
    if(anEvent.isMousePress()) return;
    
    view.setXY(view.getX() + (_mpt.x - mpt.x), view.getY() + (_mpt.y - mpt.y));
    view.getParent().repaint(); // Bogus - to fix dragging artifacts due to border
}

// For dragging
static Point _mpt;

}