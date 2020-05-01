/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.web;
import snap.util.FilePathUtils;

/**
 * A class to describe type of content in WebFile (from WebURL).
 */
public class DataType {
    
    // The type
    String       _type;
    
    // Common types
    public static final DataType Class = new DataType("application/octet-stream");
    public static final DataType HTML = new DataType("text/html");
    public static final DataType Java = new DataType("text/java");
    public static final DataType Midi = new DataType("audio/mid");
    public static final DataType PDF = new DataType("application/pdf");
    public static final DataType ReportFile = new DataType("application/rpt");
    public static final DataType Text = new DataType("text/plain");
    public static final DataType Wav = new DataType("audio/x-wav");
    public static final DataType MP3 = new DataType("audio/mpeg");
    public static final DataType Settings = new DataType("application/settings");
    public static final DataType Unknown = new DataType("unknown");
    

    /**
     * Creates a new content type.
     */
    public DataType(String aType)  { _type = aType; }

    /**
     * Returns the default content type for a path.
     */
    public static DataType getPathDataType(String aPath)
    {
        String type = FilePathUtils.getExtension(aPath).toLowerCase();
        if (type.equals("class")) return Class;
        if (type.equals("htm") || type.equals("html")) return HTML;
        if (type.equals("java")) return Java;
        if (type.equals("mid") || type.equals("midi")) return Midi;
        if (type.equals("pdf")) return PDF;
        if (type.equals("rpt")) return ReportFile;
        if (type.equals("txt")) return Text;
        if (type.equals("wav") || type.equals("snd")) return Wav;
        if (type.equals("mp3")) return MP3;
        if (type.equals("settings")) return Settings;
        return Unknown;
    }

    /**
     * Returns whether type is image.
     */
    public static boolean isImageType(String aPath)
    {
        String type = FilePathUtils.getExtension(aPath).toLowerCase();
        return type.equals("jpg") || type.equals("jpeg") || type.equals("gif") || type.equals("png");
    }
}