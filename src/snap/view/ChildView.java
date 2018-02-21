/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;

/**
 * A ChildView is a HostView where all children are assumed to be guests.
 */
public class ChildView extends HostView {

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

}