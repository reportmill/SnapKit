/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import java.io.File;
import java.util.List;
import snap.gfx.Image;
import snap.gfx.Color;
import snap.util.ClassUtils;

/**
 * A class to handle system copy/paste.
 */
public interface Clipboard {
    
    // Constants for clipboard types
    public static String  STRING = "string";
    public static String  FILES = "files";
    public static String  IMAGE = "image";
    public static String  COLOR = "SnapColor";

/**
 * Returns the clipboard content.
 */
default boolean hasString()  { return hasContent(STRING); }

/**
 * Returns the clipboard content.
 */
default String getString()  { return getContent(STRING, String.class); }

/**
 * Returns the clipboard content.
 */
default boolean hasFiles()  { return hasContent(FILES); }

/**
 * Returns the clipboard content.
 */
default List <File> getFiles()  { return getContent(STRING, List.class); }

/**
 * Returns the clipboard content.
 */
default boolean hasImage()  { return hasContent(IMAGE); }

/**
 * Returns the clipboard content.
 */
default Image getImage()  { return getContent(IMAGE, Image.class); }

/**
 * Returns the clipboard content.
 */
default boolean hasColor()  { return hasContent(COLOR); }

/**
 * Returns the clipboard content.
 */
default Color getColor()  { return getContent(COLOR, Color.class); }

/**
 * Returns the clipboard content.
 */
public boolean hasContent(String aName);

/**
 * Returns the clipboard content.
 */
public Object getContent(String aName);

/**
 * Sets the clipboard content.
 */
public void setContent(Object ... theContents);

/**
 * Returns the drag item as given class.
 */
default <T> T getContent(String aName, Class<T> aClass)  { return ClassUtils.getInstance(getContent(aName), aClass); }

/**
 * Returns the system clipboard.
 */
public static Clipboard get()  { return ViewEnv.getEnv().getClipboard(); }

}