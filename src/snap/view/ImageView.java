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
    
    // Whether to fit image so major image axis is visible if too big for view
    boolean      _fitMajor = true;
    
    // Whether to fit image so minor image axis is visible if too big for view
    boolean      _fitMinor;
    
    // Whether to grow image to major/minor attributes, even if image already fits in view
    boolean      _fitAlways;
    
    // Constants for properties
    public static final String Image_Prop = "Image";
    public static final String ImageName_Prop = "ImageName";
    public static final String FitMajor_Prop = "FitMajor";
    public static final String FitMinor_Prop = "FitMinor";
    public static final String FitAlways_Prop = "FitAlways";
    
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
    firePropChange(Image_Prop, _image, _image = anImage);
    relayoutParent(); repaint();
}

/**
 * Returns the image name, if loaded from local resource.
 */
public String getImageName()  { return _iname; }

/**
 * Sets the image name, if loaded from local resource.
 */
public void setImageName(String aName)
{
    if(SnapUtils.equals(aName, _iname)) return;
    firePropChange(ImageName_Prop, _iname, _iname = aName);
}

/**
 * Returns whether to fit image so major image axis is visible if too big for view.
 */
public boolean isFitMajor()  { return _fitMajor; }

/**
 * Sets whether to fit image so major image axis is visible if too big for view.
 */
public void setFitMajor(boolean aValue)
{
    if(aValue==_fitMajor) return;
    firePropChange(FitMajor_Prop, _fitMajor, _fitMajor = aValue);
    repaint();
}

/**
 * Returns whether to fit image so minor image axis is visible if too big for view.
 */
public boolean isFitMinor()  { return _fitMinor; }

/**
 * Sets whether to fit image so minor image axis is visible if too big for view.
 */
public void setFitMinor(boolean aValue)
{
    if(aValue==_fitMinor) return;
    firePropChange(FitMinor_Prop, _fitMinor, _fitMinor = aValue);
    repaint();
}

/**
 * Returns whether to grow image to major/minor attributes, even if image already fits in view.
 */
public boolean isFitAlways()  { return _fitAlways; }

/**
 * Sets whether to grow image to major/minor attributes, even if image already fits in view.
 */
public void setFitAlways(boolean aValue)
{
    if(aValue==_fitAlways) return;
    firePropChange(FitAlways_Prop, _fitAlways, _fitAlways = aValue);
    repaint();
}

/**
 * Returns the image bounds.
 */
public Rect getImageBounds()
{
    // Get insets, View width/height, available with/height, image width/height
    Insets ins = getInsetsAll(); if(_image==null) return null;
    double vw = getWidth(), vh = getHeight();
    double aw = vw - ins.left - ins.right, ah = vh - ins.top - ins.bottom;
    double iw = _image.getWidth(), ih = _image.getHeight();
    
    // Cacluate render width/height
    double w = iw; if(isGrowWidth() || iw>aw) w = aw;
    double h = ih; if(isGrowHeight() || ih>ah) h = ah;
    
    // Calculate image x/y based on insets and render image size
    double x = ins.left + Math.round(ViewUtils.getAlignX(this)*(aw-w));
    double y = ins.top + Math.round(ViewUtils.getAlignY(this)*(ah-h));
    return new Rect(x, y, w, h);
    
    //Rect bnds = new Rect(ins.left, ins.top, getWidth() - ins.left - ins.right, getHeight() - ins.top - ins.bottom);
    //return getImageBounds(iw, ih, bnds, getAlign(), isFitMajor(), isFitMinor(), isFitAlways());
}

/**
 * Returns the image bounds in a given rect.
 */
public static Rect getImageBounds(double aW, double aH, Rect aBnds, Pos anAlign,
    boolean fitMajor, boolean fitMinor, boolean fitAlways)
{
    boolean widthMajor = aW>=aH;
    boolean fitWidth = widthMajor? fitMajor : fitMinor;
    boolean fitHeight = widthMajor? fitMinor : fitMajor;
    double w = fitWidth && (fitAlways || aW>aBnds.width)? aBnds.width : aW;
    double h = fitHeight && (fitAlways || aH>aBnds.height)? aBnds.height : aH;
    double x = aBnds.x + Math.round(ViewUtils.getAlignX(anAlign)*(aBnds.width-w));
    double y = aBnds.y + Math.round(ViewUtils.getAlignY(anAlign)*(aBnds.height-h));
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
    String iname = getImageName(); if(iname!=null) e.add(ImageName_Prop, iname);
    
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
    String iname = anElement.getAttributeValue(ImageName_Prop);
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