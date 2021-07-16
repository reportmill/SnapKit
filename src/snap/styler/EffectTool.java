/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.styler;
import java.util.*;
import snap.gfx.*;
import snap.view.BoxView;
import snap.view.ViewEvent;

/**
 * Provides a tool for editing Snap Effects.
 */
public class EffectTool extends StylerOwner {

    // Map of tool instances by shape class
    private Map<Class,StylerOwner>  _tools = new HashMap<>();
    
    // List of known effects
    static Effect  _effects[] = { new ShadowEffect(), new ReflectEffect(), new BlurEffect(), new EmbossEffect() };
    
    /**
     * Creates EffectTool.
     */
    public EffectTool()
    {
        super();
    }

    /**
     * Returns the number of known effects.
     */
    public int getEffectCount()  { return _effects.length; }

    /**
     * Returns an individual effect at given index.
     */
    public Effect getEffect(int anIndex)  { return _effects[anIndex]; }

    /**
     * Returns the current styler effect.
     */
    public Effect getEffect()
    {
        return getStyler().getEffect();
    }

    /**
     * Sets the current styler effect.
     */
    public void setEffect(Effect anEffect)
    {
        Styler styler = getStyler();
        styler.setEffect(anEffect);
    }

    /**
     * Initialize UI panel.
     */
    protected void initUI()
    {
        // Initialize EffectComboBox
        int ecount = getEffectCount();
        Object enames[] = new String[ecount];
        for (int i=0;i<ecount;i++) enames[i] = getEffect(i).getName();
        setViewItems("EffectComboBox", enames);
    }

    /**
     * Reset UI controls from current selection.
     */
    public void resetUI()
    {
        // Get current effect (or default, if not available)
        Styler styler = getStyler();
        Effect effect = styler.getEffect();
        if (effect==null) effect = new ShadowEffect();

        // Update EffectCheckBox, EffectComboBox
        setViewValue("EffectCheckBox", styler.getEffect()!=null);
        setViewValue("EffectComboBox", effect.getName());

        // Get effect tool, install tool UI in effect panel and ResetUI
        StylerOwner etool = getTool(effect);
        getView("EffectPane", BoxView.class).setContent(etool.getUI());
        etool.resetLater();
    }

    /**
     * Updates styler from UI controls.
     */
    public void respondUI(ViewEvent anEvent)
    {
        // Get styler
        Styler styler = getStyler();

        // Handle EffectCheckBox: Iterate over shapes and add effect if not there or remove if there
        if (anEvent.equals("EffectCheckBox")) {
            boolean selected = anEvent.getBoolValue();
            Effect eff = selected ? new ShadowEffect() : null;
            styler.setEffect(eff);
        }

        // Handle EffectComboBox: Get selected effect instance and iterate over shapes and add effect if not there
        if (anEvent.equals("EffectComboBox")) {
            Effect eff = getEffect(anEvent.getSelIndex());
            styler.setEffect(eff);
        }
    }

    /**
     * Returns the specific tool for a given shape.
     */
    public StylerOwner getTool(Object anObj)
    {
        // Get tool from tools map - just return if present
        Class cls = anObj instanceof Class? (Class)anObj : anObj.getClass();
        StylerOwner tool = _tools.get(cls);
        if (tool==null) {
            _tools.put(cls, tool=getToolImpl(cls));
            tool.setStyler(getStyler());
        }
        return tool;
    }

    /**
     * Returns the specific tool for a given effect.
     */
    private static StylerOwner getToolImpl(Class aClass)
    {
        if (aClass==ShadowEffect.class) return new ShadowEffectTool();
        if (aClass==ReflectEffect.class) return new ReflectEffectTool();
        if (aClass==BlurEffect.class) return new BlurEffectTool();
        if (aClass==EmbossEffect.class) return new EmbossEffectTool();
        System.err.println("EffectTool.getToolImpl: Can't find tool for: " + aClass);
        return new ShadowEffectTool();
    }
}