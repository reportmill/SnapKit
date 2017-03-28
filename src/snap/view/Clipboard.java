/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import java.io.File;
import java.util.List;
import snap.gfx.Image;
import snap.gfx.Color;
import snap.gfx.Point;
import snap.util.ClassUtils;

/**
 * A class to handle system copy/paste and to initiate drag and drop.
 */
public abstract class Clipboard {
    
    // The drag image
    Image                 _img;
    
    // The point that the drag image should be dragged by
    Point                 _imgOffset = new Point();
    
    // Constants for clipboard types
    public static String  STRING = "string";
    public static String  FILES = "files";
    public static String  IMAGE = "image";
    public static String  COLOR = "SnapColor";

/**
 * Returns the clipboard content.
 */
public boolean hasString()  { return hasContent(STRING); }

/**
 * Returns the clipboard content.
 */
public String getString()  { return getContent(STRING, String.class); }

/**
 * Returns the clipboard content.
 */
public boolean hasFiles()  { return hasContent(FILES); }

/**
 * Returns the clipboard content.
 */
public List <File> getFiles()  { return getContent(STRING, List.class); }

/**
 * Returns the clipboard content.
 */
public boolean hasImage()  { return hasContent(IMAGE); }

/**
 * Returns the clipboard content.
 */
public Image getImage()  { return getContent(IMAGE, Image.class); }

/**
 * Returns the clipboard content.
 */
public boolean hasColor()  { return hasContent(COLOR); }

/**
 * Returns the clipboard content.
 */
public Color getColor()  { return getContent(COLOR, Color.class); }

/**
 * Returns the clipboard content.
 */
public abstract boolean hasContent(String aName);

/**
 * Returns the clipboard content.
 */
public abstract Object getContent(String aName);

/**
 * Sets the clipboard content.
 */
public abstract void setContent(Object ... theContents);

/**
 * Returns the drag item as given class.
 */
public <T> T getContent(String aName, Class<T> aClass)  { return ClassUtils.getInstance(getContent(aName), aClass); }

/**
 * Returns the drag image.
 */
public Image getDragImage()  { return _img; }

/**
 * Sets the drag image.
 */
public void setDragImage(Image anImage)
{
    _img = anImage;
    if(anImage!=null)
        setDragImageOffset(anImage.getWidth()/2,anImage.getHeight()/2);
}

/**
 * Returns the drag image offset.
 */
public Point getDragImageOffset()  { return _imgOffset; }

/**
 * Sets the drag image offset.
 */
public void setDragImageOffset(Point aPnt)  { _imgOffset = aPnt; }

/**
 * Sets the drag image offset.
 */
public void setDragImageOffset(double aX, double aY)  { setDragImageOffset(new Point(aX,aY)); }

/**
 * Sets the drag image offset.
 */
public void setDragImage(Image anImage, double aX, double aY)  { setDragImage(anImage); setDragImageOffset(aX,aY); }

/**
 * Starts the drag.
 */
public abstract void startDrag();

/**
 * Returns the system clipboard.
 */
public static Clipboard get()  { return ViewEnv.getEnv().getClipboard(); }

/**
 * Returns the clipboard for drag and drop.
 */
public static Clipboard getDrag()  { return ViewEnv.getEnv().getClipboardDrag(); }

}