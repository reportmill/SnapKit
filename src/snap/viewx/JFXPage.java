package snap.viewx;
import snap.gfx.*;
import snap.view.*;

/**
 * A WebPage subclass for JFX files.
 */
public class JFXPage extends WebPage {

/**
 * Returns the JavaFX Scene Root node.
 */
protected View createUI()
{
    ViewArchiver.setUseRealClass(false);
    View superUI = super.createUI();
    ViewArchiver.setUseRealClass(true);
    if(!(superUI instanceof DocView)) {
        superUI.setFill(ViewUtils.getBackFill());
        superUI.setBorder(Border.createLineBorder(Color.BLACK,1));
        superUI.setEffect(new ShadowEffect());
        BorderView bpane = new BorderView(); bpane.setFillCenter(false); bpane.setCenter(superUI);
        bpane.setFill(ViewUtils.getBackDarkFill());
        superUI = bpane;
    }
    return new ScrollView(superUI);
}
    
/**
 * Override to return UI file.
 */
public Object getUISource()  { return getFile(); }

/** Sets the node value for the given binding from the key value. */
protected void setBindingViewValue(Binding aBinding) { }

/** Override to suppress bindings. */
protected void setBindingModelValue(Binding aBinding)  { }

}