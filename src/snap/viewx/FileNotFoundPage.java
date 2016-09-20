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
public void setResponse(WebResponse aResp)
{
    super.setResponse(aResp);
    
    StringBuffer sb = new StringBuffer();
    sb.append("FileNotFound: ").append("\n\n");
    sb.append("  - The requested URL " + aResp.getRequestURL().getString() + " was not found on this server.\n\n");
    sb.append("  - Response Code: ").append(aResp.getCode()).append(' ').append(aResp.getCodeString());
    setText(sb.toString());
}

/**
 * Returns the page title.
 */
public String getTitle()  { return "File Not Found: " + getURL().getString(); }

}