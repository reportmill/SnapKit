package snap.javafx;
import snap.gfx.Rect;
import snap.util.PropChange;
import snap.view.*;

/**
 * A ViewHelper for RootView.
 */
public class JFXRootViewHpr <T extends JFXRootView> extends ViewHelper <T> {

    /** Creates the native. */
    protected T createNative()  { return (T)new JFXRootView(); }

    /** Override to set node in SnapPane. */
    public void setView(snap.view.View aView)  { super.setView(aView); get().setView(aView); }
    
    /** Sets the width value. */
    public void setWidth(double aValue)  { get().resize(aValue, get().getHeight()); }

    /** Sets the height value. */
    public void setHeight(double aValue)  { get().resize(get().getWidth(), aValue); }

    /** Sets the cursor. */
    public void setCursor(Cursor aCursor)  { get().setCursor(JFX.get(aCursor)); }
    
    /** Override to trigger repaint in SnapRoot. */
    public void requestPaint(Rect aRect)
    {
        SnapRoot sroot = SnapRoot.get(get().getScene()); if(sroot==null) return;
        if(sroot.getContent()==null) sroot.setContent(getView());
        sroot.paintRoot(aRect);
    }
    
    /** Override to set Min/Pref Width/Height. */
    public void propertyChange(PropChange aPC)
    {
        super.propertyChange(aPC);
        String pname = aPC.getPropertyName();
        if(pname==snap.view.View.MinWidth_Prop) get().setMinWidth((Double)aPC.getNewValue());
        else if(pname==snap.view.View.MinHeight_Prop) get().setMinHeight((Double)aPC.getNewValue());
        else if(pname==snap.view.View.MaxWidth_Prop) get().setMaxWidth((Double)aPC.getNewValue());
        else if(pname==snap.view.View.MaxHeight_Prop) get().setMaxHeight((Double)aPC.getNewValue());
        else if(pname==snap.view.View.PrefWidth_Prop) get().setPrefWidth((Double)aPC.getNewValue());
        else if(pname==snap.view.View.PrefHeight_Prop) get().setPrefHeight((Double)aPC.getNewValue());
    }
}