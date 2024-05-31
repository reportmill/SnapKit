/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import java.io.File;
import java.util.*;
import snap.geom.Point;
import snap.gfx.*;
import snap.util.ArrayUtils;
import snap.util.Loadable;
import snap.web.MIMEType;

/**
 * A class to handle system copy/paste and to initiate drag and drop.
 */
public abstract class Clipboard implements Loadable {
    
    // The drag image
    private Image _dragImage;
    
    // The point that the drag image should be dragged by
    private Point _dragImageOffset = new Point();
    
    // The ClipboardData objects
    private Map <String,ClipboardData> _clipboardDatas = new TreeMap<>();
    
    // Constants for clipboard types
    public static String  STRING = "text/plain";
    public static String  FILE_LIST = "snap/files";
    public static String  IMAGE = "snap/image";
    public static String  COLOR = "snap/color";

    // Constant for Image MimeTypes
    private static final String[] IMAGE_MIME_TYPES = { MIMEType.PNG, MIMEType.JPEG, MIMEType.GIF };

    /**
     * Returns the clipboard content.
     */
    public boolean hasData(String aMimeType)  { return hasDataImpl(aMimeType); }

    /**
     * Returns the clipboard content.
     */
    protected boolean hasDataImpl(String aMimeType)  { return _clipboardDatas.containsKey(aMimeType); }

    /**
     * Returns the clipboard content.
     */
    public ClipboardData getData(String aMimeType)  { return getDataImpl(aMimeType); }

    /**
     * Returns the clipboard content.
     */
    protected ClipboardData getDataImpl(String aMimeType)
    {
        return _clipboardDatas.get(aMimeType);
    }

    /**
     * Sets the clipboard content.
     */
    public void addData(Object theData)
    {
        ClipboardData clipboardData = ClipboardData.getClipboardDataForObject(theData);
        if (clipboardData != null)
            addData(clipboardData.getMIMEType(), clipboardData);
    }

    /**
     * Adds clipboard content.
     */
    public void addData(String aMimeType, Object theData)
    {
        // Sanity check MIMEType should have / char
        if (aMimeType.indexOf('/') < 0) {
            System.err.println("Clipboard.addData: invalid MIME type format: " + aMimeType); return; }

        // Get ClipboardData for given MIME type and data
        ClipboardData clipboardData = theData instanceof ClipboardData ? (ClipboardData) theData :
            new ClipboardData(aMimeType, theData);

        // Call real addData() implementation
        addDataImpl(aMimeType, clipboardData);
    }

    /**
     * Adds clipboard content.
     */
    protected void addDataImpl(String aMimeType, ClipboardData theData)
    {
        _clipboardDatas.put(aMimeType, theData);
    }

    /**
     * Clears the data in the clipboard.
     */
    public void clearData()  { _clipboardDatas.clear(); }

    /**
     * Returns the ClipboardDatas managed by this default implementation.
     */
    public Map <String,ClipboardData> getClipboardDatas()  { return _clipboardDatas; }

    /**
     * Returns the data for given MIME type as string.
     */
    public String getDataString(String aMimeType)
    {
        ClipboardData clipboardData = getData(aMimeType);
        return clipboardData != null ? clipboardData.getString() : null;
    }

    /**
     * Returns the data for given MIME type as byte array.
     */
    public byte[] getDataBytes(String aMimeType)
    {
        ClipboardData clipboardData = getData(aMimeType);
        return clipboardData != null ? clipboardData.getBytes() : null;
    }

    /**
     * Returns the clipboard content.
     */
    public boolean hasString()  { return hasData(STRING); }

    /**
     * Returns the clipboard content.
     */
    public String getString()  { return getDataString(STRING); }

    /**
     * Returns the clipboard content.
     */
    public boolean hasFiles()  { return hasData(FILE_LIST); }

    /**
     * Returns the clipboard content.
     */
    public List <ClipboardData> getFiles()
    {
        ClipboardData clipboardData = getData(FILE_LIST);
        return clipboardData != null ? clipboardData.getFiles() : null;
    }

    /**
     * Returns the clipboard content.
     */
    public List <File> getJavaFiles()
    {
        ClipboardData clipboardData = getData(FILE_LIST);
        return clipboardData != null ? clipboardData.getJavaFiles() : null;
    }

    /**
     * Returns the clipboard content.
     */
    public boolean hasImage()
    {
        // If Clipboard is image, return true
        if (hasData(IMAGE))
            return true;

        // Iterate over common image types and return true if any supported
        return ArrayUtils.hasMatch(IMAGE_MIME_TYPES, this::hasData);
    }

    /**
     * Returns the clipboard data as image.
     */
    public Image getImage()
    {
        // If Clipboard is image, return true
        ClipboardData clipboardData = getData(IMAGE);
        if (clipboardData != null)
            return clipboardData.getImage();

        // Iterate over common image types and if found, return image
        String imageType = getImageType();
        return imageType != null ? getImage(imageType) : null;
    }

    /**
     * Returns the image mime type.
     */
    private String getImageType()
    {
        return ArrayUtils.findMatch(IMAGE_MIME_TYPES, this::hasData);
    }

    /**
     * Returns the clipboard data as image.
     */
    public Image getImage(String aMimeType)
    {
        ClipboardData clipboardData = getData(aMimeType);
        byte[] imageBytes = clipboardData.getBytes();
        return Image.getImageForSource(imageBytes);
    }

    /**
     * Returns the clipboard image data.
     */
    public ClipboardData getImageData()
    {
        String imageType = getImageType();
        return imageType != null ? getData(imageType) : null;
    }

    /**
     * Returns the clipboard content.
     */
    public boolean hasColor()  { return hasData(COLOR); }

    /**
     * Returns the clipboard content.
     */
    public Color getColor()
    {
        String colorStr = getDataString(COLOR);
        return colorStr != null ? Color.get(colorStr) : null;
    }

    /**
     * Returns the drag image.
     */
    public Image getDragImage()  { return _dragImage; }

    /**
     * Sets the drag image.
     */
    public void setDragImage(Image anImage)
    {
        _dragImage = anImage;
        if (anImage != null) {
            double imageW = anImage.getWidth();
            double imageH = anImage.getHeight();
            setDragImageOffset(imageW / 2, imageH / 2);
        }
    }

    /**
     * Returns the drag image offset.
     */
    public Point getDragImageOffset()  { return _dragImageOffset; }

    /**
     * Sets the drag image offset.
     */
    public void setDragImageOffset(Point aPnt)  { _dragImageOffset = aPnt; }

    /**
     * Sets the drag image offset.
     */
    public void setDragImageOffset(double aX, double aY)
    {
        setDragImageOffset(new Point(aX,aY));
    }

    /**
     * Sets the drag image offset.
     */
    public void setDragImage(Image anImage, double aX, double aY)
    {
        setDragImage(anImage); setDragImageOffset(aX,aY);
    }

    /**
     * Starts the drag.
     */
    public abstract void startDrag();

    /**
     * Returns the view that started the
     */
    public View getDragSourceView()  { return null; }

    /**
     * Returns whether clipboard is loaded.
     */
    public boolean isLoaded()  { return true; }

    /**
     * Adds a callback to be triggered when resources loaded.
     */
    public void addLoadListener(Runnable aRun)  { }

    /**
     * Returns the system clipboard.
     */
    public static Clipboard get()
    {
        return ViewEnv.getEnv().getClipboard();
    }

    /**
     * Returns the system clipboard with cleared data.
     */
    public static Clipboard getCleared()
    {
        Clipboard clipboard = get();
        clipboard.clearData();
        return clipboard;
    }

    /**
     * Returns the clipboard for drag and drop.
     */
    public static Clipboard getDrag()
    {
        return ViewEnv.getEnv().getClipboardDrag();
    }
}