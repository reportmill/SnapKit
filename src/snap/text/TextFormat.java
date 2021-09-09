/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.text;
import snap.util.XMLArchiver.Archivable;

/**
 * An interface for text formating.
 */
public interface TextFormat extends Archivable {

    /** Returns a format pattern string. */
    public String getPattern();
    
    /** Sets a format pattern string. */
    default void setPattern(String format)  { System.err.println("TextFormat.setPattern: This should go"); }

    /** Returns a formatted string. */
    public String format(Object anObj);
    
    /** Returns a format style. This should go - need formatRich() returning RichText instead. */
    default TextStyle formatStyle(Object anObj)  { return null; }

}