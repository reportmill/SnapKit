/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.viewx;
import snap.web.WebResponse;

/**
 * A page subclass for FileNotFound errors.
 */
public class FileNotFoundPage extends TextPage {

/**
 * Override to configure page.
 */
protected String getDefaultText()
{
    WebResponse resp = getResponse(); if(resp==null) return "No Response found";
    
    StringBuffer sb = new StringBuffer();
    sb.append("FileNotFound: ").append("\n\n");
    sb.append("  - The requested URL " + resp.getURL().getString() + " was not found on this server.\n\n");
    sb.append("  - Response Code: ").append(resp.getCode()).append(' ').append(resp.getCodeString());
    return sb.toString();
}

/**
 * Returns the page title.
 */
public String getTitle()  { return "File Not Found: " + getURL().getString(); }

}