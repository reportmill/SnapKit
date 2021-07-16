/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.styler;
import snap.view.*;

/**
 * This class provides UI for editing fills, borders, effects, transparency.
 */
public class StylerPane extends StylerOwner {

    // The FontTool
    private FontTool _fontTool;

    // The PaintTool
    private PaintTool _fillTool;

    // The BorderTool
    private BorderTool _borderTool;

    // The EffectTool
    private EffectTool _effectTool;
    
    /**
     * Creates StylerPane.
     */
    public StylerPane(Styler aStyler)
    {
        setStyler(aStyler);
    }

    /**
     * Initialize UI panel.
     */
    protected void initUI()
    {
        // Get Main UI
        ColView mainView = getUI(ColView.class);

        // Get MiscPane and remove
        View miscPane = mainView.getChild(0);
        mainView.removeChild(miscPane);

        // Install FontTool
        _fontTool = new FontTool();
        _fontTool.setStyler(getStyler());
        mainView.addChild(_fontTool.getUI());
        Collapser.createCollapserAndLabel(_fontTool.getUI(), "Text / Font");

        // Create/install FillTool
        _fillTool = new PaintTool();
        _fillTool.setStyler(getStyler());
        mainView.addChild(_fillTool.getUI());
        Collapser.createCollapserAndLabel(_fillTool.getUI(), "Fill / Paint");

        // Create/install BorderTool
        _borderTool = new BorderTool(getStyler());
        mainView.addChild(_borderTool.getUI());
        Collapser.createCollapserAndLabel(_borderTool.getUI(), "Border / Stroke");

        // Create/install EffectTool
        _effectTool = new EffectTool();
        _effectTool.setStyler(getStyler());
        mainView.addChild(_effectTool.getUI());
        Collapser.createCollapserAndLabel(_effectTool.getUI(), "Effect").setCollapsed(true);

        // Add MiscPane
        mainView.addChild(miscPane);
        Collapser.createCollapserAndLabel(miscPane, "Transparency").setCollapsed(true);
    }

    /**
     * Reset UI controls from current selection.
     */
    public void resetUI()
    {
        // Reset FillTool, BorderTool, EffectTool
        _fillTool.resetLater();
        _borderTool.resetLater();
        _effectTool.resetLater();

        // Reset FontTool
        _fontTool.resetLater();

        // Update TransparencySlider, TransparencyText (transparency is opposite of opacity and on 0-100 scale)
        Styler styler = getStyler();
        double transparency = 100 - styler.getOpacity()*100;
        setViewValue("TransparencySlider", transparency);
        setViewValue("TransparencyText", transparency);
    }

    /**
     * Updates currently selected shapes from UI controls.
     */
    public void respondUI(ViewEvent anEvent)
    {
        // Get styler
        Styler styler = getStyler();

        // Handle Transparency Slider and Text
        if (anEvent.equals("TransparencySlider") || anEvent.equals("TransparencyText")) {
            double eval = anEvent.equals("TransparencySlider")? anEvent.getIntValue() : anEvent.getFloatValue();
            double val = 1 - eval/100;
            styler.setOpacity(val);
        }
    }

    /**
     * Returns the display name for the inspector.
     */
    public String getWindowTitle()  { return "Paint/Fill Inspector"; }
}