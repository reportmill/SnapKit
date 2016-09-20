/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.gfx.*;

/**
 * A class to handle dragging.
 */
public interface Dragboard extends Clipboard {
    
/**
 * Returns the drag image.
 */
public Image getDragImage();

/**
 * Sets the drag image.
 */
public void setDragImage(Image anImage);

/**
 * Returns the drag image offset.
 */
public Point getDragImageOffset();

/**
 * Sets the drag image offset.
 */
public void setDragImageOffset(Point aPnt);

/**
 * Sets the drag image offset.
 */
default void setDragImageOffset(double aX, double aY)  { setDragImageOffset(Point.get(aX,aY)); }

/**
 * Sets the drag image offset.
 */
default void setDragImage(Image anImage, double aX, double aY)  { setDragImage(anImage); setDragImageOffset(aX,aY); }

/**
 * Returns the drag image x offset.
 */
default double getDragImageOffsetX()  { Point p = getDragImageOffset(); return p!=null? p.getX() : 0; }

/**
 * Returns the drag image y offset.
 */
default double getDragImageOffsetY()  { Point p = getDragImageOffset(); return p!=null? p.getY() : 0; }

/**
 * Starts the drag.
 */
public void startDrag();

/**
 * Returns the active Dragboard.
 */
public static Dragboard get()  { return ViewUtils._activeDragboard; }

}