/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.gfx.*;
import snap.util.*;

/**
 * A View subclass for images.
 */
public class ImageView extends View {
    
    // The image
    Image        _image;

    // The image name, if loaded from local resource
    String       _iname;
    
/**
 * Creates a new ImageNode.
 */
public ImageView() { }

/**
 * Creates a new ImageNode with Image.
 */
public ImageView(Image anImage) { setImage(anImage); }

/**
 * Creates a new ImageNode for given URL.
 */
public ImageView(Object aSource) { _image = Image.get(aSource); }

/**
 * Returns the image.
 */
public Image getImage()  { return _image; }

/**
 * Sets the image.
 */
public void setImage(Image anImage)
{
    firePropChange("Image", _image, _image = anImage);
    relayoutParent(); repaint();
}

/**
 * Returns the image name, if loaded from local resource.
 */
public String getImageName()  { return _iname; }

/**
 * Sets the image name, if loaded from local resource.
 */
public void setImageName(String aName)  { _iname = aName; }

/**
 * Returns the image bounds.
 */
public Rect getImageBounds()
{
    Insets ins = getInsetsAll(); if(_image==null) return null;
    double vw = getWidth(), vh = getHeight();
    double pw = vw - ins.left - ins.right, ph = vh - ins.top - ins.bottom;
    double iw = _image.getWidth(), w = iw; if(isGrowWidth() || iw>pw) w = pw;
    double ih = _image.getHeight(), h = ih; if(isGrowHeight() || ih>ph) h = ph;
    double x = ins.left + Math.round(ViewUtils.getAlignX(this)*(pw-w));
    double y = ins.top + Math.round(ViewUtils.getAlignY(this)*(ph-h));
    return new Rect(x, y, w, h);
}

/**
 * Returns the default alignment.
 */    
public Pos getDefaultAlign()  { return Pos.CENTER; }

/**
 * Returns the preferred width.
 */
public double getPrefWidthImpl(double aH)
{
    Insets ins = getInsetsAll();
    return ins.left + (_image!=null? _image.getWidth() : 0) + ins.right;
}

/**
 * Returns the preferred height.
 */
public double getPrefHeightImpl(double aW)
{
    Insets ins = getInsetsAll();
    return ins.top + (_image!=null? _image.getHeight() : 0) + ins.bottom;
}

/**
 * Paints node.
 */
public void paintFront(Painter aPntr)
{
    // Calcuate text x/y based on insets, font and alignment
    Rect ibnds = getImageBounds(); if(ibnds==null) return;
    double iw = _image.getWidth(), ih = _image.getHeight();
    boolean noResize = ibnds.width==iw && ibnds.height==ih && aPntr.getTransform().isSimple();
    if(noResize) aPntr.setImageQuality(0);
    aPntr.drawImage(_image, ibnds.x, ibnds.y, ibnds.width, ibnds.height);
    aPntr.setImageQuality(.5);
}

/**
 * XML archival.
 */
public XMLElement toXML(XMLArchiver anArchiver)
{
    // Archive basic shape attributes
    XMLElement e = super.toXML(anArchiver);
    
    // Archive Image or ImageName
    Image image = getImage();
    String iname = getImageName(); if(iname!=null) e.add("ImageName", iname);
    
    // Archive image bytes as archiver resource
    else if(image!=null) {
        String rname = anArchiver.addResource(image.getBytes(), "" + System.identityHashCode(this));
        e.add("resource", rname);
    }
    
    // Archive GrowToFit, PreserveRatio
    //if(!isGrowToFit()) e.add("GrowToFit", isGrowToFit());
    //if(!getPreserveRatio()) e.add("PreserveRatio", getPreserveRatio());
    
    // Return
    return e;
}

/**
 * XML unarchival.
 */
public Object fromXML(XMLArchiver anArchiver, XMLElement anElement)
{
    // Unarchive basic shape attributes
    super.fromXML(anArchiver, anElement);
    
    // Unarchive ImageName
    String iname = anElement.getAttributeValue("ImageName");
    if(iname==null) iname = anElement.getAttributeValue("image");
    if(iname!=null) {
        setImageName(iname);
        Image image = ViewArchiver.getImage(anArchiver, iname);
        if(image!=null) setImage(image);
    }
    
    // Unarchive image resource: get resource bytes, page and set ImageData
    String rname = anElement.getAttributeValue("resource");
    byte bytes[] = rname!=null? anArchiver.getResource(rname) : null; // Get resource bytes
    if(rname!=null)
        setImage(Image.get(bytes));
    
    // Unarchive GrowToFit, PreserveRatio
    //if(anElement.hasAttribute("GrowToFit")) setGrowToFit(anElement.getAttributeBooleanValue("GrowToFit"));
    //if(anElement.hasAttribute("PreserveRatio")) setPreserveRatio(anElement.getAttributeBooleanValue("PreserveRatio"));
    return this;
}

}