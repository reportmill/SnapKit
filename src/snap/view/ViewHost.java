package snap.view;
import snap.util.*;

/**
 * An interface for Views primarily intended to hold other (arbitrary) views.
 */
public interface ViewHost {

    /**
     * ViewHost method: Returns the number of guest views.
     */
    public int getGuestCount();

    /**
     * ViewHost method: Returns the guest view at given index.
     */
    public View getGuest(int anIndex);

    /**
     * ViewHost method: Adds the given view to this host's guest (children) list at given index.
     */
    public void addGuest(View aChild, int anIndex);

    /**
     * ViewHost method: Remove's guest at given index from this host's guest (children) list.
     */
    public View removeGuest(int anIndex);

    /**
     * Adds the given view to the end of this view's guest list.
     */
    default void addGuest(View aChild)  { addGuest(aChild, getGuestCount()); }

    /**
     * Removes the given child from this view's children list.
     */
    default int removeGuest(View aView)
    {
        int ind = indexOfGuest(this, aView);
        if (ind >= 0)
            removeGuest(ind);
        return ind;
    }

    /**
     * Returns the array of guests.
     */
    default View[] getGuests()  { return getGuests(this); }

    /**
     * ViewHost helper method.
     */
    default int indexOfGuest(View aView)  { return indexOfGuest(this, aView); }

    /**
     * Removes all children from this view (in reverse order).
     */
    default void removeGuests()  { removeGuests(this); }

    /**
     * Returns the guests array.
     */
    public static View[] getGuests(ViewHost aHost)
    {
        int gc = aHost.getGuestCount();
        View[] guests = new View[gc];
        for (int i=0; i<gc; i++)
            guests[i] = aHost.getGuest(i);
        return guests;
    }

    /**
     * Sets children to given list.
     */
    public static void setGuests(ViewHost aHost, View ... theChildren)
    {
        removeGuests(aHost);
        for (View c : theChildren)
            aHost.addGuest(c);
    }

    /**
     * Removes all children from this view (in reverse order).
     */
    public static void removeGuests(ViewHost aHost)
    {
        for (int i=aHost.getGuestCount()-1; i>=0; i--)
            aHost.removeGuest(i);
    }

    /**
     * Returns whether given view parent is a ViewHost and view is one of its guests.
     */
    public static ViewHost getHost(View aView)
    {
        ParentView par = aView != null ? aView.getParent() : null;
        ViewHost host = par instanceof ViewHost ? (ViewHost) par : null;
        return host;
    }

    /**
     * Returns whether given view parent is a ViewHost and view is one of its guests.
     */
    public static boolean isGuest(View aView)
    {
        ViewHost host = getHost(aView);
        return host!=null && isGuest(host, aView);
    }

    /**
     * Returns whether given view is a guest view of this ViewHost.
     */
    public static boolean isGuest(ViewHost aHost, View aView)  { return indexOfGuest(aHost, aView)>=0; }

    /**
     * ViewHost helper method.
     */
    public static int indexOfGuest(ViewHost aHost, View aView)
    {
        if (aHost == null) return -1;
        for (int i=0,iMax=aHost.getGuestCount(); i<iMax; i++) {
            View guest = aHost.getGuest(i);
            if (guest == aView)
                return i;
        }
        return -1;
    }

    /**
     * ViewHost helper method.
     */
    public static int indexInHost(View aView)
    {
        ViewHost host = getHost(aView);
        return indexOfGuest(host, aView);
    }

    /**
     * XML archival of ViewHost.Guests.
     */
    public static void toXMLGuests(ViewHost aHost, XMLArchiver anArchiver, XMLElement anElement)
    {
        // Archive guests
        for (int i=0, iMax=aHost.getGuestCount(); i<iMax; i++) {
            View child = aHost.getGuest(i);
            anElement.add(anArchiver.toXML(child, aHost));
        }
    }

    /**
     * XML unarchival of ViewHost.Guests.
     */
    public static void fromXMLGuests(ViewHost aHost, XMLArchiver anArchiver, XMLElement anElement)
    {
        // Iterate over child elements and unarchive as child views
        for (int i=0, iMax=anElement.size(); i<iMax; i++) {
            XMLElement childXML = anElement.get(i);
            Class cls = anArchiver.getClass(childXML.getName());
            if (cls != null && View.class.isAssignableFrom(cls)) {
                View view = (View) anArchiver.fromXML(childXML, aHost);
                aHost.addGuest(view);
            }
        }
    }
}