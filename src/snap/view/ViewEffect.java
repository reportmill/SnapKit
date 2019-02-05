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

    // A PainterDVR to hold cached effect render from last pass (and one to hold plain render for compare)
    PainterDVR         _pdvr, _pdvrX;
    
    // The view size when DVR was cached
    double             _vw, _vh;
    
    // The Focus Effect
    static Effect      _focEff;
    
    // The Focus ViewEffect
    static ViewEffect  _focVEff;
    
    // The Focused color
    public static Color    FOCUSED_COLOR = Color.get("#039ed3");
    
/**
 * Creates a ViewEffect for a given view and effect.
 */
public ViewEffect(View aView, Effect anEff)  { _view = aView; _eff = anEff; }

/**
 * Main paint method.
 */
protected void paintAll(Painter aPntr)
{
    // If view has changed since last cache, update cached PainterDVR for effect
    double vw = _view.getWidth(), vh = _view.getHeight();
    if(_pdvr==null || vw!=_vw || vh!=_vh || _view.isNeedsRepaint() || isNeedsRepaintDeep(_view) && !isSimpleShadow())
        updateEffectPainterDVR();
    
    // Execute PainterDVR to given painter
    _pdvr.exec(aPntr);
}

/**
 * Updates the cached effect PainterDVR.
 */
void updateEffectPainterDVR()
{
    // Do normal painting to new PainterDVR
    PainterDVR pdvr = new PainterDVR();
    _view.paintBack(pdvr);
    _view.paintFront(pdvr);
    
    // If effect should include child views, paint children
    if(!isSimpleShadow()) { ParentView pv = (ParentView)_view;
        pv.paintChildren(pdvr);
        pv.paintAbove(pdvr);
    }
    
    // If painting hasn't changed since last cache, just return
    double vw = _view.getWidth(), vh = _view.getHeight();
    if(vw==_vw && vh==_vh && pdvr.equals(_pdvrX)) return;
    
    // Render and cache effect of painting to second PainterDVR
    _pdvr = new PainterDVR();
    _eff.applyEffect(pdvr, _pdvr, _view.getBoundsLocal());
    _vw = vw; _vh = vh; _pdvrX = pdvr;
}

/**
 * Returns whether effect should include children.
 */
public boolean isSimpleShadow()
{
    if(_eff instanceof ShadowEffect && ((ShadowEffect)_eff).isSimple()) return true;
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

/** Convenience. */
private static final boolean isNeedsRepaintDeep(View aView)  {
    return aView instanceof ParentView &&  ((ParentView)aView).isNeedsRepaintDeep(); }

}