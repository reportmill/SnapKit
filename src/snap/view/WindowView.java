package snap.view;
import java.io.File;
import java.lang.reflect.Array;
import java.util.*;
import snap.gfx.*;
import snap.util.*;

/**
 * A class to manage a Window.
 */
public class WindowView extends ParentView {
    
    // The panel's window title
    String                    _title;
    
    // The type
    String                    _type = TYPE_MAIN;
    
    // The root pane
    RootView                  _rpane;
    
    // Whether the panel's window is always on top
    boolean                   _alwaysOnTop;
    
    // The document file
    File                      _docFile;
    
    // The window image
    Image                     _image;
    
    // Whether the panel's window hides on deativate
    boolean                   _hideOnDeactivate;
    
    // Whether the panel's window is modal
    boolean                   _modal = false;
    
    // Whether the panel's window is resizable
    boolean                   _resizable = true;
    
    // The Frame save name
    String                    _saveName;
    
    // Save frame size
    boolean                   _saveSize;
    
    // A list of all open windows
    static List <WindowView>  _openWins = new ArrayList();
    
    // Constants for Type
    public static final String TYPE_MAIN = "MAIN";
    public static final String TYPE_UTILITY = "UTILITY";
    public static final String TYPE_PLAIN = "PLAIN";
    
    // Constants for style
    public static enum Style { Small }
    
    // Constants for properties
    public static final String AlwaysOnTop_Prop = "AlwaysOnTop";
    public static final String Image_Prop = "Image";
    public static final String Resizable_Prop = "Resizable";
    public static final String Title_Prop = "Title";

/**
 * Returns the title of the window.
 */
public String getTitle()  { return _title; }

/**
 * Sets the title of the window.
 */
public void setTitle(String aValue)
{
    if(SnapUtils.equals(aValue,_title)) return;
    firePropChange(Title_Prop, _title, _title=aValue);
}

/**
 * Returns the window type.
 */
public String getType()  { return _type; }

/**
 * Sets the window type.
 */
public void setType(String aType)  { _type = aType; }

/**
 * Returns whether the window is resizable.
 */
public boolean isResizable()  { return _resizable; }

/**
 * Sets whether the window is resizable (default to true).
 */
public void setResizable(boolean aValue)
{
    if(aValue==_resizable) return;
    firePropChange(Resizable_Prop, _resizable, _resizable=aValue);
}

/**
 * Returns the save name.
 */
public String getSaveName()  { return _saveName; }

/**
 * Sets the save name.
 */
public void setSaveName(String aName)  { _saveName = aName; }

/**
 * Returns whether to save size
 */
public boolean getSaveSize()  { return _saveSize; }

/**
 * Sets whether to save size.
 */
public void setSaveSize(boolean aValue)  { _saveSize = aValue; }

/**
 * Save frame.
 */
public void saveFrame()
{
    int x = (int)getX(), y = (int)getY();
    int w = (int)getWidth(), h = (int)getHeight();
    StringBuffer sb = new StringBuffer().append(x).append(' ').append(y);
    if(_saveSize) sb.append(' ').append(w).append(' ').append(h);
    PrefsUtils.prefsPut(_saveName + "Loc", sb.toString());
}

/**
 * Returns the root pane.
 */
public RootView getRootView()
{
    if(_rpane!=null) return _rpane;
    _rpane = new RootView(); _rpane._win = this; _rpane.getHelper();
    addChild(_rpane);
    return _rpane;
}

/**
 * Returns the menu bar associated with this window.
 */
public MenuBar getMenuBar()  { return getRootView().getMenuBar(); }

/**
 * Sets the menu bar associated with this window.
 */
public void setMenuBar(MenuBar aMenuBar)  { getRootView().setMenuBar(aMenuBar); }

/**
 * Returns the content associated with this window.
 */
public View getContent()  { return getRootView().getContent(); }

/**
 * Sets the content associated with this window.
 */
public void setContent(View aView)  { getRootView().setContent(aView); }

/**
 * Returns the content associated with this window, with option to set from owner if not there yet.
 */
public View getContent(boolean doSet)
{
    View content = getContent();
    if(content==null && doSet) {
        ViewOwner owner = getOwner(); if(owner==null) return null;
        content = owner.getUI();
        setContent(content);
    }
    return content;
}

/**
 * Returns whether the window is always on top.
 */
public boolean isAlwaysOnTop()  { return _alwaysOnTop; }

/**
 * Sets whether the window is always on top.
 */
public void setAlwaysOnTop(boolean aValue)
{
    if(aValue==_alwaysOnTop) return;
    firePropChange(AlwaysOnTop_Prop, _alwaysOnTop, _alwaysOnTop = aValue);
}

/**
 * Returns the document file for the window title bar proxy icon.
 */
public File getDocFile()  { return _docFile; }

/**
 * Returns the document file for the window title bar proxy icon.
 */
public void setDocFile(File aFile)
{
    if(SnapUtils.equals(aFile, _docFile)) return; _docFile = aFile;
    getHelper().setDocFile(aFile);
}

/**
 * Returns the icon image for the window.
 */
public Image getImage()  { return _image; }

/**
 * Sets the icon image for the window.
 */
public void setImage(Image anImage)  { firePropChange(Image_Prop, _image, _image = anImage); }
    
/**
 * Returns whether the window will hide on deactivate.
 */
public boolean isHideOnDeactivate()  { return _hideOnDeactivate; }

/**
 * Sets whether the window will hide on deacativate.
 */
public void setHideOnDeactivate(boolean aValue)  { _hideOnDeactivate = aValue; }

/**
 * Returns the modal mode of the window.
 */
public boolean isModal()  { return _modal; }

/**
 * Sets the modal mode of the window (defaults to false).
 */
public void setModal(boolean aValue)  { _modal = aValue; }

/**
 * Shows window in center of screen.
 */
public void show()
{
    Point pnt = getScreenLocation(null, Pos.CENTER, 0, 0);
    show(null, pnt.x, pnt.y);
}

/**
 * Show the window relative to given node.
 */
public void show(View aView, double aSX, double aSY)
{
    // Make sure content is set
    getContent(true);
    
    // Make sure window is initialized
    getHelper().checkInit();
    
    // If FrameSaveName provided, set Location from defaults and register to store future window moves
    if(getSaveName()!=null) {
        String locString = PrefsUtils.prefs().get(getSaveName() + "Loc", null);
        if(locString!=null) {
            String strings[] = locString.split(" ");
            aSX = StringUtils.intValue(strings[0]);
            aSY = StringUtils.intValue(strings[1]);
            int w = getSaveSize() && strings.length>2? StringUtils.intValue(strings[2]) : 0;
            int h = getSaveSize() && strings.length>3? StringUtils.intValue(strings[3]) : 0;
            if(w>0 && h>0) setSize(w,h);
        }
    }
    
    // Have helper show window
    getHelper().show(aView, aSX, aSY);
}

/**
 * Hide the window.
 */
public void hide()  { getHelper().hide(); }

/**
 * Packs the window.
 */
public void pack()  { getHelper().setPrefSize(); }

/**
 * Order window to front.
 */
public void toFront()  { getHelper().toFront(); }

/**
 * Returns the screen location for given node, position and offsets.
 */
public Point getScreenLocation(View aView, Pos aPos, double aDX, double aDY)
{
    // Make sure content is set
    getContent(true);
    
    // Get rect for given node and point for given offsets
    Rect rect = aView!=null? aView.getLocalToScreen().createTransformedShape(aView.getBoundsInside()).getBounds() :
        getEnv().getScreenBoundsInset();
    double x = aDX, y = aDY;
    getHelper().checkInit();
    
    // Modify x for given HPos
    switch(aPos.getHPos()) {
        case LEFT: x += rect.x; break;
        case CENTER: x += Math.round(rect.x + (rect.getWidth()-getWidth())/2); break;
        case RIGHT: x += rect.getMaxX() - getWidth(); break;
    }
    
    // Modify y for given VPos
    switch(aPos.getVPos()) {
        case TOP: y += rect.y; break;
        case CENTER: y += Math.round(rect.y + (rect.getHeight()-getHeight())/2); break;
        case BOTTOM: y += rect.getMaxY() - getHeight(); break;
    }
    
    // Return point
    return new Point(x,y);
}

/**
 * Override to return showing, since it is eqivalent for window.
 */
public boolean isVisible()  { return isShowing(); }

/**
 * Override to call show/hide.
 */
public void setVisible(boolean aValue)  { if(aValue) show(); else hide(); }

/**
 * Override to add/remove window to/from global windows list.
 */
public void setShowing(boolean aVal)
{
    if(aVal==isShowing()) return; super.setShowing(aVal);
    if(aVal) ListUtils.moveToFront(_openWins, this);
    else _openWins.remove(this);
}

/**
 * Override to move window to front of all windows list.
 */
protected void setFocused(boolean aValue)
{
    if(aValue==isFocused()) return; super.setFocused(aValue);
    if(aValue) ListUtils.moveToFront(_openWins, this);
}

/**
 * Returns an array of all open windows.
 */
public static WindowView[] getOpenWindows()  { return _openWins.toArray(new WindowView[_openWins.size()]); }

/**
 * Returns an array of all open windows.
 */
public static <T extends ViewOwner> T getOpenWindowOwner(Class <T> aClass)
{
    for(WindowView wnode : _openWins) {
        RootView rpane = wnode.getRootView();
        View content = rpane.getContent();
        ViewOwner ownr = content.getOwner();
        if(ownr!=null && aClass==null || aClass.isAssignableFrom(ownr.getClass()))
            return (T)ownr;
    }
    return null;
}

/**
 * Returns an array of all open windows.
 */
public static <T extends ViewOwner> T[] getOpenWindowOwners(Class <T> aClass)
{
    List <T> ownrs = new ArrayList();
    for(WindowView wnode : _openWins) {
        RootView rpane = wnode.getRootView();
        View content = rpane.getContent();
        ViewOwner ownr = content.getOwner();
        if(ownr!=null && aClass==null || aClass.isAssignableFrom(ownr.getClass()))
            ownrs.add((T)ownr);
    }
    return ownrs.toArray((T[])Array.newInstance(aClass, ownrs.size()));
}

}