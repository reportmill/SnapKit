/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.gfx;

/**
 * Graphics utilities.
 */
public class GFXUtils {

    /**
     * Sets this JVM to be headless.
     */
    public static void setHeadless()
    {
        try { System.setProperty("java.awt.headless", "true"); }
        catch(Throwable e) { }
    }
}