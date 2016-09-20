/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.gfx;
import snap.util.*;

/**
 * A custom class.
 */
public class Effect implements XMLArchiver.Archivable {

/**
 * Returns the name of the effect.
 */
public String getName()  { String cname = getClass().getSimpleName(); return cname.substring(0,cname.length()-6); }

/**
 * Returns the bounds required to render this effect applied to given rect.
 */
public Rect getBounds(Rect aRect)  { return aRect; }

/**
 * Apply the effect from given DVR to painter.
 */
public void applyEffect(PainterDVR aPDVR, Painter aPntr, Rect aRect)
{
    System.err.println(getClass().getSimpleName() + ".apply: Not implemented");
}

/**
 * XML archival.
 */
public XMLElement toXML(XMLArchiver anArchiver)  { XMLElement e = new XMLElement("effect"); return e; }

/**
 * XML unarchival.
 */
public Object fromXML(XMLArchiver anArchiver, XMLElement anElement)  { return this; }

}