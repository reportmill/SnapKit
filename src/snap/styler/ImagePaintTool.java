/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.styler;
import snap.gfx.Image;
import snap.gfx.ImagePaint;
import snap.gfx.Paint;
import snap.geom.Rect;
import snap.view.*;
import snap.viewx.FilePanel;
import snap.web.WebURL;

/**
 * UI editing for ImagePaint.
 */
public class ImagePaintTool extends StylerOwner {

    // The default image fill
    private static ImagePaint  _imageFill;

    /**
     * Updates the UI controls from the currently selected shape.
     */
    public void resetUI()
    {
        // Get currently selected fill and image fill (if none, use default instance)
        Paint sfill = getStyler().getFill();
        ImagePaint fill = sfill instanceof ImagePaint ? (ImagePaint)sfill : _imageFill;

        // Update TiledCheckBox
        setViewValue("TiledCheckBox", fill.isTiled());

        // Update XSpinner, YSpinner, ScaleXSpinner and ScaleYSpinner
        setViewValue("XSpinner", fill.getX());
        setViewValue("YSpinner", fill.getY());
        setViewValue("ScaleXSpinner", fill.getScaleX());
        setViewValue("ScaleYSpinner", fill.getScaleY());
    }

    /**
     * Updates the currently selected shape from the UI controls.
     */
    public void respondUI(ViewEvent anEvent)
    {
        // Get currently selected fill and image fill (if none, use default instance)
        Paint sfill = getStyler().getFill();
        ImagePaint fill = sfill instanceof ImagePaint ? (ImagePaint)sfill : _imageFill;

        // Handle TiledCheckBox
        if (anEvent.equals("TiledCheckBox"))
            fill = fill.copyTiled(anEvent.getBoolValue());

        // Handle XSpinner, YSpinner, ScaleXSpinner, ScaleYSpinner
        if (anEvent.equals("XSpinner")) {
            Rect rect = new Rect(anEvent.getFloatValue(), fill.getY(), fill.getWidth(), fill.getHeight());
            fill = fill.copyForRectAndTile(rect, fill.isAbsolute());
        }
        if (anEvent.equals("YSpinner")) {
            Rect rect = new Rect(fill.getX(), anEvent.getFloatValue(), fill.getWidth(), fill.getHeight());
            fill = fill.copyForRectAndTile(rect, fill.isAbsolute());
        }
        if (anEvent.equals("ScaleXSpinner"))
            fill = fill.copyForScale(anEvent.getFloatValue(), fill.getScaleY());
        if (anEvent.equals("ScaleYSpinner"))
            fill = fill.copyForScale(fill.getScaleX(), anEvent.getFloatValue());

        // Handle ChooseButton
        if (anEvent.equals("ChooseButton")) {
            String path = FilePanel.showOpenPanel(getUI(), "Image File", "png", "jpg", "gif");
            if (path!=null) {
                WebURL url = WebURL.getURL(path);
                Image img = Image.get(url);
                fill = img!=null ? new ImagePaint(img) : null;
            }
        }

        // Set new fill
        Styler styler = getStyler();
        styler.setFill(fill);
    }

    /**
     * Returns the string used for the inspector window title.
     */
    public String getWindowTitle()  { return "Fill Inspector (Texture)"; }

    /**
     * Returns the default ImagePaint.
     */
    public static ImagePaint getDefault()
    {
        if (_imageFill!=null) return _imageFill;
        Image img = Image.get(ImagePaintTool.class, "pkg.images/Clouds.jpg");
        return _imageFill = new ImagePaint(img);
    }
}