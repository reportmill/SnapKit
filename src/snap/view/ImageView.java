/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.geom.Insets;
import snap.geom.Pos;
import snap.geom.Rect;
import snap.gfx.*;
import snap.util.*;

/**
 * A View subclass for images.
 */
public class ImageView extends View {
    
    // The image
    protected Image  _image;

    // The image name, if loaded from local resource
    private String  _iname;
    
    // Whether to resize image to fill view width
    private boolean  _fillWidth;
    
    // Whether to resize image to fill view height
    private boolean  _fillHeight;
    
    // Whether to preserve aspect ratio of image when resized
    private boolean  _keepAspect;
    
    // Whether to allow image size to extend beyond view bounds
    private boolean  _allowBleed;
    
    // The frame
    private int  _frame;

    // Constants for properties
    public static final String Image_Prop = "Image";
    public static final String ImageName_Prop = "ImageName";
    public static final String FillWidth_Prop = "FillWidth";
    public static final String FillHeight_Prop = "FillHeight";
    public static final String KeepAspect_Prop = "KeepAspect";
    public static final String AllowBleed_Prop = "AllowBleed";
    
    /**
     * Creates a new ImageNode.
     */
    public ImageView()  { }

    /**
     * Creates a new ImageNode with Image.
     */
    public ImageView(Image anImage)  { setImage(anImage); }

    /**
     * Creates a new ImageNode with Image and FillWidth/FillHeight params.
     */
    public ImageView(Image anImage, boolean isFillWidth, boolean isFillHeight)
    {
        setImage(anImage); setFillWidth(isFillWidth); setFillHeight(isFillHeight);
    }

    /**
     * Creates a new ImageNode for given URL.
     */
    public ImageView(Object aSource)  { _image = Image.get(aSource); }

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

        // If image not done loading - listen for load
        if (_image!=null && !_image.isLoaded())
            _image.addLoadListener(() -> imageFinishedLoading());
    }

    /** Called when image finishes loading. */
    void imageFinishedLoading()  { relayoutParent(); repaint(); }

    /**
     * Returns the image name, if loaded from local resource.
     */
    public String getImageName()  { return _iname; }

    /**
     * Sets the image name, if loaded from local resource.
     */
    public void setImageName(String aName)
    {
        if (SnapUtils.equals(aName, _iname)) return;
        firePropChange(ImageName_Prop, _iname, _iname = aName);
    }

    /**
     * Returns whether to resize image to fill view width.
     */
    public boolean isFillWidth()  { return _fillWidth; }

    /**
     * Sets whether to resize image to fill view width.
     */
    public void setFillWidth(boolean aValue)
    {
        if (aValue==_fillWidth) return;
        firePropChange(FillWidth_Prop, _fillWidth, _fillWidth = aValue);
        repaint();
    }

    /**
     * Returns whether to resize image to fill view height.
     */
    public boolean isFillHeight()  { return _fillHeight; }

    /**
     * Sets whether to resize image to fill view height.
     */
    public void setFillHeight(boolean aValue)
    {
        if (aValue==_fillHeight) return;
        firePropChange(FillHeight_Prop, _fillHeight, _fillHeight = aValue);
        repaint();
    }

    /**
     * Returns whether to to preserve aspect ratio of image when resized.
     */
    public boolean isKeepAspect()  { return _keepAspect; }

    /**
     * Sets whether to to preserve aspect ratio of image when resized.
     */
    public void setKeepAspect(boolean aValue)
    {
        if (aValue==_keepAspect) return;
        firePropChange(KeepAspect_Prop, _keepAspect, _keepAspect = aValue);
        repaint();
    }

    /**
     * Returns whether image size can extend outside view size.
     */
    public boolean isAllowBleed()  { return _allowBleed; }

    /**
     * Sets whether image size can extend outside view size.
     */
    public void setAllowBleed(boolean aValue)
    {
        if (aValue==_allowBleed) return;
        firePropChange(AllowBleed_Prop, _allowBleed, _allowBleed = aValue);
        repaint();
        System.err.println("ImageView.setAllowBleed: Not implemented");
    }

    /**
     * Returns the frame.
     */
    public int getFrame()  { return _frame; }

    /**
     * Sets the frame.
     */
    public void setFrame(int anIndex)
    {
        _frame = anIndex%getFrameCount();
        ImageSet iset = _image!=null ? _image.getImageSet() : null; if (iset==null) return;
        setImage(iset.getImage(_frame));
    }

    /**
     * Returns the frame.
     */
    public int getFrameMax()  { return getFrameCount() - 1; }

    /**
     * Returns the frame.
     */
    public int getFrameCount()
    {
        ImageSet iset = _image!=null ? _image.getImageSet() : null; if (iset==null) return 1;
        return iset.getCount();
    }

    /**
     * Returns the image bounds.
     */
    public Rect getImageBounds()
    {
        // Get insets, View width/height, available with/height, image width/height
        Insets ins = getInsetsAll(); if (_image==null) return null;
        double iw = _image.getWidth();
        double ih = _image.getHeight();

        // Get inset bounds
        Rect bnds = new Rect(ins.left, ins.top, getWidth() - ins.left - ins.right, getHeight() - ins.top - ins.bottom);

        // Return image bounds
        Rect ibounds = getImageBounds(iw, ih, bnds, getAlign(), isFillWidth(), isFillHeight(), isKeepAspect());
        return ibounds;
    }

    /**
     * Returns the image bounds in a given rect.
     */
    public static Rect getImageBounds(double aW, double aH, Rect aBnds, Pos anAlign,
        boolean fillWidth, boolean fillHeight, boolean keepAspect)
    {
        // Get w/h based on FillWidth, FillHeight, KeepAspect, image size and available size
        double w = fillWidth || aW>aBnds.width ? aBnds.width : aW;
        double h = fillHeight || aH>aBnds.height ? aBnds.height : aH;

        // If KeepAspect and either axis needs resize, find axis with least scale and ajust other to match
        if (keepAspect && (w!=aW || h!=aH)) {
            double sx = w/aW, sy = h/aH;
            if (sx<=sy) h = aH*sx;
            else w = aW*sy;
        }

        // Calculate x/y based on w/h, avaiable size and alignment
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
    protected double getPrefWidthImpl(double aH)
    {
        // Get insets and given height minus insets (just return insets width if no image)
        Insets ins = getInsetsAll(); if (_image==null) return ins.left + ins.right;
        double h = aH>0 ? aH - ins.top - ins.bottom : aH;

        // PrefWidth is image width. If height is provided, adjust pref width by aspect
        double pw = _image.getWidth();
        if (h>0 && !MathUtils.equals(h, _image.getHeight()))
            pw = h*getAspect();

        // Return PrefWidth plus insets width
        return ins.left + pw + ins.right;
    }

    /**
     * Returns the preferred height.
     */
    protected double getPrefHeightImpl(double aW)
    {
        // Get insets and given width minus insets (just return insets height if no image)
        Insets ins = getInsetsAll(); if (_image==null) return ins.top + ins.bottom;
        double w = aW>=0 ? aW - ins.left - ins.right : aW;

        // PrefHeight is image height. If width is provided, adjust pref height by aspect
        double ph = _image.getHeight();
        if (w>0 && !MathUtils.equals(w, _image.getWidth()))
            ph = w/getAspect();

        // Return PrefHeight plus insets height
        return ins.top + ph + ins.bottom;
    }

    /** Returns the ratio of the width/height. */
    protected double getAspect()  { return _image.getWidth()/_image.getHeight(); }

    /**
     * Paints node.
     */
    protected void paintFront(Painter aPntr)
    {
        // Get image (just return if not set or not loaded)
        Image image = getImage();
        if (image==null || !image.isLoaded())
            return;

        // Get whether to clip to bounds, and if so, do clip
        boolean clipToBounds = isFillWidth() && !isFillHeight();
        if (clipToBounds) { aPntr.save(); aPntr.clip(getBoundsLocal().getInsetRect(getInsetsAll())); }

        // Calcuate text x/y based on insets, font and alignment
        Rect ibnds = getImageBounds(); if (ibnds==null) return;
        double iw = _image.getWidth();
        double ih = _image.getHeight();

        // If drawing at natural size and simple transform, get nearest-neighbor rendering (faster and better for hidpi)
        boolean noResize = ibnds.width==iw && ibnds.height==ih && aPntr.getTransform().isSimple();
        if (noResize)
            aPntr.setImageQuality(0);

        // Draw image
        aPntr.drawImage(_image, ibnds.x, ibnds.y, ibnds.width, ibnds.height);

        // Restore rendering mode
        if (noResize)
            aPntr.setImageQuality(.5);

        // If clipped, restore
        if (clipToBounds)
            aPntr.restore();
    }

    /**
     * Returns the value for given key.
     */
    public Object getPropValue(String aPropName)
    {
        if (aPropName.equals("Frame")) return getFrame();
        return super.getPropValue(aPropName);
    }

    /**
     * Sets the value for given key.
     */
    public void setPropValue(String aPropName, Object aValue)
    {
        if (aPropName.equals("Frame"))
            setFrame(SnapUtils.intValue(aValue));
        else super.setPropValue(aPropName, aValue);
    }

    /**
     * XML archival.
     */
    public XMLElement toXML(XMLArchiver anArchiver)
    {
        // Archive basic shape attributes
        XMLElement e = super.toXML(anArchiver);

        // Get image and image name
        Image image = getImage();
        String iname = getImageName();

        // If image name available, just archive it
        if (iname!=null)
            e.add(ImageName_Prop, iname);

        // Otherwise if image available, archive image bytes as archiver resource
        else if (image!=null) {
            String rname = anArchiver.addResource(image.getBytes(), "" + System.identityHashCode(this));
            e.add("resource", rname);
        }

        // Archive FillWidth, FillHeight, KeepAspect
        if (isFillWidth()) e.add(FillWidth_Prop, true);
        if (isFillHeight()) e.add(FillHeight_Prop, true);
        if (isKeepAspect()) e.add(KeepAspect_Prop, true);

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
        if (iname==null) iname = anElement.getAttributeValue("image");
        if (iname!=null) {
            setImageName(iname);
            Image image = ViewArchiver.getImage(anArchiver, iname);
            if (image!=null)
                setImage(image);
        }

        // Unarchive image resource: get resource bytes, page and set ImageData
        String rname = anElement.getAttributeValue("resource");
        byte bytes[] = rname!=null ? anArchiver.getResource(rname) : null; // Get resource bytes
        if (rname!=null)
            setImage(Image.get(bytes));

        // Unarchive FillWidth, FillHeight, KeepAspect
        if (anElement.hasAttribute(FillWidth_Prop)) setFillWidth(anElement.getAttributeBooleanValue(FillWidth_Prop));
        if (anElement.hasAttribute(FillHeight_Prop)) setFillHeight(anElement.getAttributeBooleanValue(FillHeight_Prop));
        if (anElement.hasAttribute(KeepAspect_Prop)) setKeepAspect(anElement.getAttributeBooleanValue(KeepAspect_Prop));
        return this;
    }
}