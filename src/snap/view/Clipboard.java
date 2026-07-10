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
    private Point _dragImageOffset = Point.ZERO;
    
    // The ClipboardData objects
    private Map <String,ClipboardData> _clipboardDatas = new TreeMap<>();
    
    // Constants for clipboard types
    public static final String STRING = "text/plain";
    public static final String FILE_LIST = "snap/files";
    public static final String IMAGE = "snap/image";
    public static final String COLOR = "snap/color";

    // Constant for Image MimeTypes
    private static final String[] IMAGE_MIME_TYPES = { MIMEType.PNG, MIMEType.JPEG, MIMEType.GIF };

    /**
     * Constructor.
     */
    public Clipboard()
    {
        super();
    }

    /**
     * Returns the clipboard content.
     */
    public boolean hasDataForMimeType(String aMimeType)
    {
        return _clipboardDatas.containsKey(aMimeType);
    }

    /**
     * Returns the clipboard content.
     */
    public ClipboardData getDataForMimeType(String aMimeType)
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
            addDataForMimeType(clipboardData, clipboardData.getMimeType());
    }

    /**
     * Adds clipboard content.
     */
    public void addDataForMimeType(Object theData, String aMimeType)
    {
        // Sanity check MIMEType should have / char
        if (aMimeType.indexOf('/') < 0) {
            System.err.println("Clipboard.addData: invalid MIME type format: " + aMimeType); return; }

        // Get ClipboardData for given MIME type and data
        ClipboardData clipboardData = theData instanceof ClipboardData ? (ClipboardData) theData :
            new ClipboardData(theData, aMimeType);

        // Call real addData() implementation
        addDataForMimeTypeImpl(clipboardData, aMimeType);
    }

    /**
     * Adds clipboard content.
     */
    protected void addDataForMimeTypeImpl(ClipboardData theData, String aMimeType)
    {
        _clipboardDatas.put(aMimeType, theData);
    }

    /**
     * Clears the data in the clipboard.
     */
    protected void clearData()  { _clipboardDatas.clear(); }

    /**
     * Returns the ClipboardDatas managed by this default implementation.
     */
    public Map <String,ClipboardData> getClipboardDatas()  { return _clipboardDatas; }

    /**
     * Returns the data for given MIME type as string.
     */
    public String getDataString(String aMimeType)
    {
        ClipboardData clipboardData = getDataForMimeType(aMimeType);
        return clipboardData != null ? clipboardData.getString() : null;
    }

    /**
     * Returns the data for given MIME type as byte array.
     */
    public byte[] getDataBytes(String aMimeType)
    {
        ClipboardData clipboardData = getDataForMimeType(aMimeType);
        return clipboardData != null ? clipboardData.getBytes() : null;
    }

    /**
     * Returns the clipboard content.
     */
    public boolean hasString()  { return hasDataForMimeType(STRING); }

    /**
     * Returns the clipboard content.
     */
    public String getString()  { return getDataString(STRING); }

    /**
     * Returns the clipboard content.
     */
    public boolean hasFiles()  { return hasDataForMimeType(FILE_LIST); }

    /**
     * Returns the clipboard content.
     */
    public List <ClipboardData> getFiles()
    {
        ClipboardData clipboardData = getDataForMimeType(FILE_LIST);
        return clipboardData != null ? clipboardData.getFiles() : null;
    }

    /**
     * Returns the clipboard content.
     */
    public List <File> getJavaFiles()
    {
        ClipboardData clipboardData = getDataForMimeType(FILE_LIST);
        return clipboardData != null ? clipboardData.getJavaFiles() : null;
    }

    /**
     * Returns the clipboard content.
     */
    public boolean hasImage()
    {
        // If Clipboard is image, return true
        if (hasDataForMimeType(IMAGE))
            return true;

        // Iterate over common image types and return true if any supported
        return ArrayUtils.hasMatch(IMAGE_MIME_TYPES, this::hasDataForMimeType);
    }

    /**
     * Returns the clipboard data as image.
     */
    public Image getImage()
    {
        // If Clipboard is image, return true
        ClipboardData clipboardData = getDataForMimeType(IMAGE);
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
        return ArrayUtils.findMatch(IMAGE_MIME_TYPES, this::hasDataForMimeType);
    }

    /**
     * Returns the clipboard data as image.
     */
    public Image getImage(String aMimeType)
    {
        ClipboardData clipboardData = getDataForMimeType(aMimeType);
        byte[] imageBytes = clipboardData.getBytes();
        return Image.getImageForSource(imageBytes);
    }

    /**
     * Returns the clipboard image data.
     */
    public ClipboardData getImageData()
    {
        String imageType = getImageType();
        return imageType != null ? getDataForMimeType(imageType) : null;
    }

    /**
     * Returns the clipboard content.
     */
    public boolean hasColor()  { return hasDataForMimeType(COLOR); }

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