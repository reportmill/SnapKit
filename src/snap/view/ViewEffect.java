package snap.view;
import snap.gfx.*;

/**
 * A class to manage an effect for a view.
 */
class ViewEffect {
    
    // The View
    View               _view;
    
    // The Effect
    Effect             _eff;

    // A PainterDVR to hold plain render from last pass for checking cache on future renders
    PainterDVR         _pdvr1;
    
    // A PainterDVR to hold cached effect render from last pass
    PainterDVR         _pdvr2;
    
    // The Focus Effect
    static Effect      _focEff;
    
    // The Focus ViewEffect
    static ViewEffect  _focVEff;
    
    // The Focused color
    public static Color    FOCUSED_COLOR = Color.get("#039ed3");
    
/**
 * Creates a ViewEffect for a given view and effect.
 */
public ViewEffect(View aView, Effect anEff)
{
    _view = aView; _eff = anEff;
}

/**
 * Main paint method.
 */
protected void paintAll(Painter aPntr)
{
    // Do normal painting to new PainterDVR
    PainterDVR pdvr = new PainterDVR();
    _view.paintBack(pdvr);
    _view.paintFront(pdvr);
    
    // If effect should include child views, paint children
    if(!isSimpleShadow()) {
        ((ParentView)_view).paintChildren(pdvr);
        ((ParentView)_view).paintAbove(pdvr);
    }
    
    // If painting has changed since last pass, render and cache effect of painting to second PainterDVR
    if(_pdvr1==null || !pdvr.equals(_pdvr1)) {
        _pdvr1 = pdvr; _pdvr2 = new PainterDVR();
        _eff.applyEffect(pdvr, _pdvr2, _view.getBoundsLocal());
    }
    
    // Execute PainterDVR to given painter
    _pdvr2.exec(aPntr);
}

/**
 * Returns whether effect should include children.
 */
public boolean isSimpleShadow()
{
    if(!(_view instanceof ParentView)) return true;
    if(!(_eff instanceof ShadowEffect)) return false;
    if(_view.getFill()==null) return false;
    if(!(_view.getBoundsShape() instanceof Rect)) return false;
    return true;
}

/**
 * Returns the focus effect for given view.
 */
public static Effect getFocusEffect()
{
    if(_focEff!=null) return _focEff;
    return _focEff = new ShadowEffect(5, FOCUSED_COLOR, 0, 0);
}

/**
 * Returns the focus effect for given view.
 */
public static ViewEffect getFocusViewEffect(View aView)
{
    if(_focVEff!=null && _focVEff._view==aView) return _focVEff;
    return _focVEff = new ViewEffect(aView, getFocusEffect());
}

}