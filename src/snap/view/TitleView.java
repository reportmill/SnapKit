/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.gfx.*;
import snap.util.*;

/**
 * A view to attach a title to another view.
 */
public class TitleView extends ParentView {

    // The title text
    String         _title;

    // The content
    View           _content;

/**
 * Returns the title.
 */
public String getTitle()  { return _title; }

/**
 * Sets the title.
 */
public void setTitle(String aTitle)  { _title = aTitle; }

/**
 * Returns the content.
 */
public View getContent()  { return _content; }

/**
 * Sets the content.
 */
public void setContent(View aView)
{
    View old = _content; if(aView==old) return;
    _content = aView; addChild(aView);
    firePropChange("Content", old, aView);
}

/**
 * Returns the insets.
 */
public Insets getInsetsAll()
{
    Insets ins = super.getInsetsAll(); double lheight = Math.ceil(getFont().getLineHeight());
    ins = Insets.add(ins, 2 + lheight, 2, 2, 2);
    return ins;
}

/**
 * Override to return preferred width of content.
 */
protected double getPrefWidthImpl(double aH)
{
    Insets ins = getInsetsAll();
    View c = getContent(); double cw = c!=null? c.getPrefWidth() : 0;
    return ins.left + cw + ins.right;
}

/**
 * Override to return preferred height of content.
 */
protected double getPrefHeightImpl(double aW)
{
    Insets ins = getInsetsAll();
    View c = getContent(); double ch = c!=null? c.getPrefHeight() : 0;
    return ins.top + ch + ins.bottom;
}

/**
 * Override to layout content.
 */
protected void layoutImpl()
{
    if(_content==null) return;
    Insets ins = getInsetsAll();
    double x = ins.left, y = ins.top, w = getWidth() - x - ins.right, h = getHeight() - y - ins.bottom;
    _content.setBounds(x, y, w, h);
}

/**
 * Override to paint title and stroke.
 */
protected void paintFront(Painter aPntr)
{
    double w = getWidth(), h = getHeight(), asc = getFont().getAscent(), dsc = getFont().getDescent();
    double sw = _title!=null? getFont().getStringAdvance(_title) + 10 : 0, sh = Math.round(asc+dsc);
    double x1 = 5+sw, y1 = sh/2;
    Path path = new Path(); path.moveTo(x1,y1); path.lineTo(w-2,y1); path.lineTo(w-2,h-2); path.lineTo(.5,h-2);
    path.lineTo(.5,y1); path.lineTo(5,y1);
    aPntr.translate(1,1); aPntr.setPaint(Color.WHITE); aPntr.setStroke(Stroke.Stroke1); aPntr.draw(path);
    aPntr.translate(-1,-1);
    aPntr.setPaint(Color.LIGHTGRAY); aPntr.draw(path);
    aPntr.setFont(getFont()); aPntr.setColor(Color.BLACK); if(_title!=null) aPntr.drawString(_title, 10, asc);
}

/**
 * XML archival.
 */
protected XMLElement toXMLView(XMLArchiver anArchiver)
{
    XMLElement e = super.toXMLView(anArchiver);
    if(getTitle()!=null && getTitle().length()>0) e.add("Title", getTitle());
    return e;
}

/**
 * XML unarchival.
 */
protected void fromXMLView(XMLArchiver anArchiver, XMLElement anElement)
{
    super.fromXMLView(anArchiver,anElement);
    setTitle(anElement.getAttributeValue("Title"));
}

/**
 * XML archival of children.
 */
protected void toXMLChildren(XMLArchiver anArchiver, XMLElement anElement)
{
    // Archive Content
    if(getContent()==null) return;
    anElement.add(anArchiver.toXML(getContent(), this));
}

/**
 * XML unarchival for shape children.
 */
protected void fromXMLChildren(XMLArchiver anArchiver, XMLElement anElement)
{
    // Iterate over child elements and unarchive child
    for(int i=0, iMax=anElement.size(); i<iMax; i++) { XMLElement childXML = anElement.get(i);
        Class childClass = anArchiver.getClass(childXML.getName());
        if(childClass!=null && View.class.isAssignableFrom(childClass)) {
            View view = (View)anArchiver.fromXML(childXML, this);
            setContent(view); break;
        }
    }
}

}