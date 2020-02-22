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
    public void setPattern(String format);

    /** Returns a formatted string. */
    public String format(Object anObj);
    
    /** Returns a format sytle. */
    public TextStyle formatStyle(Object anObj);

}