/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.gfx.Image;
import java.util.List;

/**
 * An interface for providing tree item information.
 */
public abstract class TreeResolver <T> {
    
    /**
     * Returns the parent of given item.
     */
    public abstract T getParent(T anItem);

    /**
     * Whether given object is a parent (has children).
     */
    public abstract boolean isParent(T anItem);

    /**
     * Returns the children.
     */
    public abstract List<T> getChildren(T aParent);

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
        Image image = getImage(anItem);
        return image != null ? new ImageView(image) : null;
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
        Image image = getImageAfter(anItem);
        return image != null ? new ImageView(image) : null;
    }

    /**
     * The icon of given item.
     */
    public Image getBranchImage(T anItem)  { return null; }

    /**
     * An Adapter class.
     */
    public static class Adapter <T> extends TreeResolver <T> {

        /** Returns the parent of given item. */
        public T getParent(T anItem)  { return null; }

        /** Whether given object is a parent (has children). */
        public boolean isParent(T anItem)  { return false; }

        /** Returns the children. */
        public List<T> getChildren(T aParent)  { return null; }
    }
}