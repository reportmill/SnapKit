/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.geom.Insets;
import snap.geom.Pos;
import snap.geom.Rect;
import snap.gfx.*;
import snap.util.*;

import java.util.Objects;

/**
 * A View subclass for images.
 */
public class ImageView extends View {
    
    // The image
    protected Image  _image;

    // The image name, if loaded from local resource
    private String  _imageName;
    
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
    public static final String Frame_Prop = "Frame";

    /**
     * Constructor.
     */
    public ImageView()
    {
        super();
    }

    /**
     * Constructor with given Image.
     */
    public ImageView(Image anImage)
    {
        super();
        setImage(anImage);
    }

    /**
     * Constructor with given Image and FillWidth/FillHeight params.
     */
    public ImageView(Image anImage, boolean isFillWidth, boolean isFillHeight)
    {
        super();
        setImage(anImage);
        setFillWidth(isFillWidth);
        setFillHeight(isFillHeight);
    }

    /**
     * Creates a new ImageNode for given URL.
     */
    public ImageView(Object aSource)
    {
        super();
        _image = Image.get(aSource);
    }

    /**
     * Returns the image.
     */
    public Image getImage()  { return _image; }

    /**
     * Sets the image.
     */
    public void setImage(Image anImage)
    {
        if (anImage == _image) return;
        firePropChange(Image_Prop, _image, _image = anImage);
        relayoutParent();
        repaint();

        // If image not done loading - listen for load
        if (_image != null && !_image.isLoaded())
            _image.addLoadListener(() -> imageFinishedLoading());
    }

    /** Called when image finishes loading. */
    private void imageFinishedLoading()
    {
        relayoutParent();
        repaint();
    }

    /**
     * Returns the image name, if loaded from local resource.
     */
    public String getImageName()  { return _imageName; }

    /**
     * Sets the image name, if loaded from local resource.
     */
    public void setImageName(String aName)
    {
        if (Objects.equals(aName, _imageName)) return;
        firePropChange(ImageName_Prop, _imageName, _imageName = aName);
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
        if (aValue == _fillWidth) return;
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
        if (aValue == _fillHeight) return;
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
        if (aValue == _keepAspect) return;
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
        if (aValue == _allowBleed) return;
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
        // If already set, just return
        int frame = anIndex % getFrameCount();
        if (frame == _frame) return;

        // Set value
        _frame = frame;

        // Get ImageSet
        ImageSet imageSet = _image != null ? _image.getImageSet() : null;
        if (imageSet == null)
            return;

        // Get image for frame and set
        Image frameImage = imageSet.getImage(_frame);
        setImage(frameImage);
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
        ImageSet imageSet = _image != null ? _image.getImageSet() : null;
        if (imageSet == null)
            return 1;
        return imageSet.getCount();
    }

    /**
     * Returns the image bounds.
     */
    public Rect getImageBounds()
    {
        // If no image, return null
        if (_image == null) return null;

        // Get insets, View width/height, available with/height, image width/height
        Insets ins = getInsetsAll();
        double imageW = _image.getWidth();
        double imageH = _image.getHeight();

        // Get inset bounds
        double areaW = getWidth() - ins.getWidth();
        double areaH = getHeight() - ins.getHeight();
        Rect areaBounds = new Rect(ins.left, ins.top, areaW, areaH);

        // Return image bounds
        Pos align = getAlign();
        boolean fillW = isFillWidth();
        boolean fillH = isFillHeight();
        boolean keepAspect = isKeepAspect();
        return getImageBounds(imageW, imageH, areaBounds, align, fillW, fillH, keepAspect);
    }

    /**
     * Returns the image bounds in a given rect.
     */
    public static Rect getImageBounds(double imageW, double imageH, Rect areaBounds, Pos anAlign,
        boolean fillWidth, boolean fillHeight, boolean keepAspect)
    {
        // Get w/h based on FillWidth, FillHeight, KeepAspect, image size and available size
        double paintW = fillWidth || imageW > areaBounds.width ? areaBounds.width : imageW;
        double paintH = fillHeight || imageH > areaBounds.height ? areaBounds.height : imageH;

        // If KeepAspect and either axis needs resize, find axis with least scale and ajust other to match
        if (keepAspect && (paintW != imageW || paintH != imageH)) {
            double scaleX = paintW / imageW;
            double scaleY = paintH / imageH;
            if (scaleX <= scaleY)
                paintH = imageH * scaleX;
            else paintW = imageW * scaleY;
        }

        // Calculate x/y based on w/h, avaiable size and alignment
        double paintX = areaBounds.x + Math.round(ViewUtils.getAlignX(anAlign) * (areaBounds.width - paintW));
        double paintY = areaBounds.y + Math.round(ViewUtils.getAlignY(anAlign) * (areaBounds.height - paintH));
        return new Rect(paintX, paintY, paintW, paintH);
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
        Insets ins = getInsetsAll();
        if (_image == null)
            return ins.getWidth();
        double imageH = aH > 0 ? aH - ins.getHeight() : aH;

        // PrefWidth is image width. If height is provided, adjust pref width by aspect
        double prefW = _image.getWidth();
        if (imageH > 0 && !MathUtils.equals(imageH, _image.getHeight()))
            prefW = imageH * getAspect();

        // Return PrefWidth plus insets width
        return prefW + ins.getWidth();
    }

    /**
     * Returns the preferred height.
     */
    protected double getPrefHeightImpl(double aW)
    {
        // Get insets and given width minus insets (just return insets height if no image)
        Insets ins = getInsetsAll();
        if (_image == null)
            return ins.getHeight();
        double imageW = aW >= 0 ? aW - ins.left - ins.right : aW;

        // PrefHeight is image height. If width is provided, adjust pref height by aspect
        double prefH = _image.getHeight();
        if (imageW > 0 && !MathUtils.equals(imageW, _image.getWidth()))
            prefH = imageW / getAspect();

        // Return PrefHeight plus insets height
        return prefH + ins.getHeight();
    }

    /**
     * Returns the ratio of the width/height.
     */
    protected double getAspect()
    {
        double imageW = _image.getWidth();
        double imageH = _image.getHeight();
        return imageW / imageH;
    }

    /**
     * Paints node.
     */
    protected void paintFront(Painter aPntr)
    {
        // Get image (just return if not set or not loaded)
        Image image = getImage();
        if (image == null || !image.isLoaded())
            return;

        // Get whether to clip to bounds, and if so, do clip
        boolean clipToBounds = isFillWidth() && !isFillHeight();
        if (clipToBounds) {
            aPntr.save();
            Rect clipRect = getBoundsLocal().getInsetRect(getInsetsAll());
            aPntr.clip(clipRect);
        }

        // Calcuate text x/y based on insets, font and alignment
        Rect paintBounds = getImageBounds();
        if (paintBounds == null)
            return;
        double imageW = _image.getWidth();
        double imageH = _image.getHeight();

        // If drawing at natural size and simple transform, get nearest-neighbor rendering (faster and better for hidpi)
        boolean noResize = paintBounds.width == imageW && paintBounds.height == imageH && aPntr.getTransform().isSimple();
        if (noResize)
            aPntr.setImageQuality(0);

        // Draw image
        aPntr.drawImage(_image, paintBounds.x, paintBounds.y, paintBounds.width, paintBounds.height);

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
        switch (aPropName) {

            // Image, ImageName
            case Image_Prop: return getImage();
            case ImageName_Prop: return getImageName();

            // FillWidth, FillHeight, KeepAspect, AllowBleed
            case FillWidth_Prop: return isFillWidth();
            case FillHeight_Prop: return isFillHeight();
            case KeepAspect_Prop: return isKeepAspect();
            case AllowBleed_Prop: return isAllowBleed();

            // Frame
            case Frame_Prop: return getFrame();

            // Do normal version
            default: return super.getPropValue(aPropName);
        }
    }

    /**
     * Sets the value for given key.
     */
    public void setPropValue(String aPropName, Object aValue)
    {
        switch (aPropName) {

            // Image, ImageName
            case Image_Prop: setImage((Image) aValue); break;
            case ImageName_Prop: setImageName(Convert.stringValue(aValue)); break;

            // FillWidth, FillHeight, KeepAspect, AllowBleed
            case FillWidth_Prop: setFillWidth(Convert.boolValue(aValue)); break;
            case FillHeight_Prop: setFillHeight(Convert.boolValue(aValue)); break;
            case KeepAspect_Prop: setKeepAspect(Convert.boolValue(aValue)); break;
            case AllowBleed_Prop: setAllowBleed(Convert.boolValue(aValue)); break;

            // Frame
            case Frame_Prop: setFrame(Convert.intValue(aValue)); break;

            // Do normal version
            default: super.setPropValue(aPropName, aValue); break;
        }
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
        String imageName = getImageName();

        // If image name available, just archive it
        if (imageName != null)
            e.add(ImageName_Prop, imageName);

        // Otherwise if image available, archive image bytes as archiver resource
        else if (image != null) {
            String resourceName = anArchiver.addResource(image.getBytes(), "" + System.identityHashCode(this));
            e.add("resource", resourceName);
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
        String imageName = anElement.getAttributeValue(ImageName_Prop);
        if (imageName == null) imageName = anElement.getAttributeValue("image");
        if (imageName != null) {
            setImageName(imageName);
            Image image = ViewArchiver.getImage(anArchiver, imageName);
            if (image != null)
                setImage(image);
        }

        // Unarchive image resource: get resource bytes, page and set ImageData
        String resourceName = anElement.getAttributeValue("resource");
        byte[] bytes = resourceName != null ? anArchiver.getResource(resourceName) : null; // Get resource bytes
        if (resourceName != null)
            setImage(Image.get(bytes));

        // Unarchive FillWidth, FillHeight, KeepAspect
        if (anElement.hasAttribute(FillWidth_Prop)) setFillWidth(anElement.getAttributeBooleanValue(FillWidth_Prop));
        if (anElement.hasAttribute(FillHeight_Prop)) setFillHeight(anElement.getAttributeBooleanValue(FillHeight_Prop));
        if (anElement.hasAttribute(KeepAspect_Prop)) setKeepAspect(anElement.getAttributeBooleanValue(KeepAspect_Prop));
        return this;
    }
}