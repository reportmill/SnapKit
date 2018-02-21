package snap.view;
import snap.util.*;

/**
 * A HostView is a ParentView that can hold one or more arbitrary child views (or "Guest Views").
 * 
 * Known HostViews in SnapKit: BoxView, ColView*, PageView*, RowView*, ScaleBox, Scroller, SplitView,
 * SpringView*, StackView*, SwitchView*, TabView, TitleView (* view indicates ChildView).
 */
public class HostView extends ParentView {

    // Constants for properties
    public static final String Guest_Prop = "Guest";
    
/**
 * Returns the number of guest views.
 */
public int getGuestCount()  { return getChildCount(); }

/**
 * Returns the guest view at given index.
 */
public View getGuest(int anIndex)  { return getChild(anIndex); }

/**
 * Adds the given view to the end of this view's guest list.
 */
public void addGuest(View aChild)  { addGuest(aChild, getGuestCount()); }

/**
 * Adds the given child to this view's children list at the given index.
 */
public void addGuest(View aChild, int anIndex)
{
    addChild(aChild, anIndex);
    fireGuestPropChange(null, aChild, anIndex);
}

/**
 * Remove's the child at the given index from this view's children list.
 */
public View removeGuest(int anIndex)
{
    View child = removeChild(anIndex);
    fireGuestPropChange(child, null, anIndex);
    return child;
}

/**
 * Removes the given child from this view's children list.
 */
public int removeGuest(View aView)
{
    int ind = indexOfGuest(aView);
    if(ind>=0) removeGuest(ind);
    return ind;
}

/**
 * Removes all children from this view (in reverse order).
 */
public void removeGuests()  { for(int i=getGuestCount()-1; i>=0; i--) removeGuest(i); }

/**
 * Fires the Guest_Prop change.
 */
protected void fireGuestPropChange(View oldVal, View newVal, int anIndex)
{
    if(!_pcs.hasListener(Guest_Prop)) return;
    firePropChange(new GuestChange(this, oldVal, newVal, anIndex));
}

/**
 * Returns the guests array.
 */
public View[] getGuests()
{
    int gc = getGuestCount();
    View guests[] = new View[gc];
    for(int i=0;i<gc;i++) guests[i] = getGuest(i);
    return guests;
}

/**
 * Sets children to given list.
 */
public void setGuests(View ... theChildren)  { removeGuests(); for(View c : theChildren) addGuest(c); }

/**
 * Returns whether given view is a guest view of this HostView.
 */
public boolean isGuest(View aView)  { return indexOfGuest(aView)>=0; }

/**
 * Returns the index of given guest view.
 */
public int indexOfGuest(View aView)
{
    for(int i=0,iMax=getGuestCount();i<iMax;i++) if(getGuest(i)==aView) return i;
    return -1;
}

/**
 * XML archival of children.
 */
protected void toXMLChildren(XMLArchiver anArchiver, XMLElement anElement)
{
    // Archive guests
    for(int i=0, iMax=getGuestCount(); i<iMax; i++) { View child = getGuest(i);
        anElement.add(anArchiver.toXML(child, this)); }    
}

/**
 * XML unarchival for shape children.
 */
protected void fromXMLChildren(XMLArchiver anArchiver, XMLElement anElement)
{
    // Iterate over child elements and unarchive as child views
    for(int i=0, iMax=anElement.size(); i<iMax; i++) { XMLElement childXML = anElement.get(i);
        Class cls = anArchiver.getClass(childXML.getName());
        if(cls!=null && View.class.isAssignableFrom(cls)) {
            View view = (View)anArchiver.fromXML(childXML, this);
            addGuest(view);
        }
    }
}

/**
 * A PropChange for Guest_Prop.
 */
private class GuestChange extends PropChange {
    
    /** Create GuestChange. */
    public GuestChange(Object src, Object oval, Object nval, int ind)  { super(src,Guest_Prop,oval,nval,ind); }
    
    /** Override to handle special. */
    protected void doChange(Object oVal, Object nVal)
    {
        if(nVal!=null) addGuest((View)nVal, getIndex());
        else removeGuest(getIndex());
    }
}

}