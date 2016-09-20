/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import java.lang.reflect.Array;
import java.util.List;
import snap.gfx.Image;
import snap.util.*;

/**
 * An interface for providing tree item information.
 */
public class TreeResolver <T> {
    
    // The tree
    TreeView     _tree;
    
/**
 * Returns the tree.
 */
public TreeView getTree()  { return _tree; }

/**
 * Sets the tree.
 */
public void setTree(TreeView aTree)  { _tree = aTree; }

/**
 * Returns the parent of given item.
 */
public T getParent(T anItem)
{
    List <T> items = _tree.getItems();
    int index = ListUtils.indexOfId(items, anItem); if(index<0) return null;
    for(int i=index-1;i>=0;i--) { T item = items.get(i);
        if(isParent(item) && getTree().isExpanded(item) && ArrayUtils.containsId(_tree.getChildren(item), anItem))
            return item; }
    return null;
}

/**
 * Whether given object is a parent (has children).
 */
public boolean isParent(T anItem)  { return false; }

/**
 * The number of children in given parent.
 */
public int getChildCount(T aParent)  { return getChildren(aParent).length; }

/**
 * The child at given index in given parent.
 */
public T getChild(T aParent, int anIndex)  { return getChildren(aParent)[anIndex]; }

/**
 * Returns the children.
 */
public T[] getChildren(T aParent)
{
    int count = getChildCount(aParent);
    T children[] = (T[])Array.newInstance(aParent.getClass(),count);
    for(int i=0;i<count;i++) children[i] = getChild(aParent, i);
    return children;
}

/**
 * Returns the text to be used for given item.
 */
public String getText(T anItem)  { return anItem.toString(); }

/**
 * Returns the text to be used for given item in given column.
 */
public String getText(T anItem, int aCol)  { return getText(anItem); }

/**
 * Return the image to be used for given item.
 */
public Image getImage(T anItem)  { return null; }

/**
 * Return the graphic to be used for given item.
 */
public View getGraphic(T anItem)
{
    Image img = getImage(anItem);
    return img!=null? new ImageView(img) : null;
}

/**
 * Return the image to be used for given item after item text.
 */
public Image getImageAfter(T anItem)  { return null; }

/**
 * Return the graphic to be used for given item after text.
 */
public View getGraphicAfter(T anItem)
{
    Image img = getImageAfter(anItem);
    return img!=null? new ImageView(img) : null;
}

/**
 * The icon of given item.
 */
public Image getBranchImage(T anItem)  { return null; }

}