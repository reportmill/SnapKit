package snap.view;
import snap.util.*;

/**
 * An interface for Views primarily intended to hold other (arbitrary) views.
 */
public interface ViewHost {

    /**
     * ViewHost method: Returns the number of guest views.
     */
    int getGuestCount();

    /**
     * ViewHost method: Returns the guest view at given index.
     */
    View getGuest(int anIndex);

    /**
     * ViewHost method: Adds the given view to this host's guest (children) list at given index.
     */
    void addGuest(View aChild, int anIndex);

    /**
     * ViewHost method: Remove's guest at given index from this host's guest (children) list.
     */
    View removeGuest(int anIndex);

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
    default View[] getGuests()
    {
        int guestCount = getGuestCount();
        View[] guests = new View[guestCount];
        for (int i = 0; i < guestCount; i++)
            guests[i] = getGuest(i);
        return guests;
    }

    /**
     * Sets the array of guests.
     */
    default void setGuests(View[] theViews)
    {
        while (getGuestCount() > 0)
            removeGuest(0);
        for (View view : theViews)
            addGuest(view);
    }

    /**
     * Returns whether given view parent is a ViewHost and view is one of its guests.
     */
    static ViewHost getHost(View aView)
    {
        ParentView par = aView != null ? aView.getParent() : null;
        ViewHost host = par instanceof ViewHost ? (ViewHost) par : null;
        return host;
    }

    /**
     * ViewHost helper method.
     */
    static int indexOfGuest(ViewHost aHost, View aView)
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
    static int indexInHost(View aView)
    {
        ViewHost host = getHost(aView);
        return indexOfGuest(host, aView);
    }

    /**
     * XML archival of ViewHost.Guests.
     */
    static void toXMLGuests(ViewHost aHost, XMLArchiver anArchiver, XMLElement anElement)
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
    static void fromXMLGuests(ViewHost aHost, XMLArchiver anArchiver, XMLElement anElement)
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