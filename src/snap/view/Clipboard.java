/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import java.io.File;
import java.util.*;
import java.util.function.Consumer;

import snap.geom.Point;
import snap.gfx.*;
import snap.util.Loadable;
import snap.web.MIMEType;

/**
 * A class to handle system copy/paste and to initiate drag and drop.
 */
public abstract class Clipboard implements Loadable {
    
    // The drag image
    Image                       _img;
    
    // The point that the drag image should be dragged by
    Point _imgOffset = new Point();
    
    // The ClipboardData objects
    Map <String,ClipboardData>  _cdatas = new TreeMap<>();
    
    // Constants for clipboard types
    public static String  STRING = "text/plain";
    public static String  FILE_LIST = "snap/files";
    public static String  IMAGE = "snap/image";
    public static String  COLOR = "snap/color";

    /**
     * Returns the clipboard content.
     */
    public boolean hasData(String aMimeType)  { return hasDataImpl(aMimeType); }

    /**
     * Returns the clipboard content.
     */
    protected boolean hasDataImpl(String aMimeType)  { return _cdatas.containsKey(aMimeType); }

    /**
     * Returns the clipboard content.
     */
    public ClipboardData getData(String aMimeType)  { return getDataImpl(aMimeType); }

    /**
     * Returns the clipboard content.
     */
    protected ClipboardData getDataImpl(String aMimeType)
    {
        return _cdatas.get(aMimeType);
    }

    /**
     * Sets the clipboard content.
     */
    public void addData(Object theData)
    {
        if (theData instanceof File)
            theData = Collections.singletonList(theData);

        ClipboardData cdata = ClipboardData.get(theData);
        if (cdata!=null)
            addData(cdata.getMIMEType(), cdata);
    }

    /**
     * Adds clipboard content.
     */
    public void addData(String aMimeType, Object theData)
    {
        // Sanity check MIMEType should have / char
        if (aMimeType.indexOf('/')<0) {
            System.err.println("Clipboard.addData: invalid MIME type format: " + aMimeType); return; }

        // Get ClipboardData for given MIME type and data
        ClipboardData cdata = theData instanceof ClipboardData ? (ClipboardData)theData :
            new ClipboardData(aMimeType, theData);

        // Call real addData() implementation
        addDataImpl(aMimeType, cdata);
    }

    /**
     * Adds clipboard content.
     */
    protected void addDataImpl(String aMimeType, ClipboardData theData)
    {
        _cdatas.put(aMimeType, theData);
    }

    /**
     * Clears the data in the clipboard.
     */
    public void clearData()  { _cdatas.clear(); }

    /**
     * Returns the ClipboardDatas managed by this default implementation.
     */
    public Map <String,ClipboardData> getClipboardDatas()  { return _cdatas; }

    /**
     * Returns the data for given MIME type as string.
     */
    public String getDataString(String aMimeType)
    {
        ClipboardData data = getData(aMimeType);
        return data!=null ? data.getString() : null;
    }

    /**
     * Returns the data for given MIME type as byte array.
     */
    public byte[] getDataBytes(String aMimeType)
    {
        ClipboardData data = getData(aMimeType);
        return data!=null ? data.getBytes() : null;
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
        ClipboardData cdata = getData(FILE_LIST);
        return cdata!=null ? cdata.getFiles() : null;
    }

    /**
     * Returns the clipboard content.
     */
    public List <File> getJavaFiles()
    {
        ClipboardData cdata = getData(FILE_LIST);
        return cdata!=null ? cdata.getJavaFiles() : null;
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
        String types[] = new String[] { MIMEType.PNG, MIMEType.JPEG, MIMEType.GIF };
        for (String type : types)
            if (hasData(type))
                return true;
        return false;
    }

    /**
     * Returns the clipboard data as image.
     */
    public Image getImage()
    {
        // If Clipboard is image, return true
        ClipboardData data = getData(IMAGE);
        if (data!=null)
            return data.getImage();

        // Iterate over common image types and if found, return image
        String types[] = new String[] { MIMEType.PNG, MIMEType.JPEG, MIMEType.GIF };
        for (String type : types)
            if (hasData(type))
                return getImage(type);
        return null;
    }

    /**
     * Returns the clipboard data as image.
     */
    public Image getImage(String aMimeType)
    {
        ClipboardData cdata = getData(aMimeType);
        byte bytes[] = cdata.getBytes();
        return Image.get(bytes);
    }

    /**
     * Returns the clipboard image data.
     */
    public ClipboardData getImageData()
    {
        String types[] = new String[] { MIMEType.PNG, MIMEType.JPEG, MIMEType.GIF };
        for (String type : types)
            if (hasData(type))
                return getData(type);
        return null;
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
        String cstr = getDataString(COLOR);
        return cstr!=null ? Color.get(cstr) : null;
    }

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
        if (anImage!=null)
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
        Clipboard cb = get(); cb.clearData();
        return cb;
    }

    /**
     * Returns the clipboard for drag and drop.
     */
    public static Clipboard getDrag()
    {
        return ViewEnv.getEnv().getClipboardDrag();
    }
}