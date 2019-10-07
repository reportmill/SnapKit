/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;

/**
 * A ChildView is a ParentView (and ViewHost) that allows children to be modified.
 */
public class ChildView extends ParentView implements ViewHost {

/**
 * Adds the given child to the end of this view's children list.
 */
public void addChild(View aChild)  { super.addChild(aChild); }

/**
 * Adds the given child to this view's children list at the given index.
 */
public void addChild(View aChild, int anIndex)  { super.addChild(aChild, anIndex); }

/**
 * Remove's the child at the given index from this view's children list.
 */
public View removeChild(int anIndex)  { return super.removeChild(anIndex); }

/**
 * Removes the given child from this view's children list.
 */
public int removeChild(View aChild)  { return super.removeChild(aChild); }

/**
 * Removes all children from this view (in reverse order).
 */
public void removeChildren()  { super.removeChildren(); }

/**
 * Sets children to given list.
 */
public void setChildren(View ... theChildren)  { super.setChildren(theChildren); }

/**
 * ViewHost method: Returns the number of guest views.
 */
public int getGuestCount()  { return getChildCount(); }

/**
 * ViewHost method: Returns the guest view at given index.
 */
public View getGuest(int anIndex)  { return getChild(anIndex); }

/**
 * ViewHost method: Adds the given view to this host's guest (children) list at given index.
 */
public void addGuest(View aChild, int anIndex)
{
    addChild(aChild, anIndex);
}

/**
 * ViewHost method: Remove's guest at given index from this host's guest (children) list.
 */
public View removeGuest(int anIndex)
{
    View child = removeChild(anIndex);
    return child;
}

}