package snap.styler;
import snap.gfx.Border;
import snap.gfx.Borders;
import snap.gfx.Color;
import snap.view.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides UI to edit borders.
 */
public class BorderTool extends StylerOwner {

    // Map of tool instances by shape class
    private Map<Class, StylerOwner> _tools = new HashMap<>();

    /**
     * Creates BorderTool.
     */
    public BorderTool(Styler aStyler)
    {
        setStyler(aStyler);
    }

    /**
     * Initialize UI panel.
     */
    protected void initUI()
    {
        // Configure Borders
        Label l1 = getView("LineBdrButton", ButtonBase.class).getLabel();
        l1.setPrefSize(16, 16);
        l1.setBorder(Color.BLACK, 1);
        Label l2 = getView("LowerBdrButton", ButtonBase.class).getLabel();
        l2.setPrefSize(16, 16);
        l2.setBorder(new Borders.BevelBorder(0));
        Label l3 = getView("RaiseBdrButton", ButtonBase.class).getLabel();
        l3.setPrefSize(16, 16);
        l3.setBorder(new Borders.BevelBorder(1));
        Label l4 = getView("EtchBdrButton", ButtonBase.class).getLabel();
        l4.setPrefSize(16, 16);
        l4.setBorder(new Borders.EtchBorder());
        Label l5 = getView("EdgeBdrButton", ButtonBase.class).getLabel();
        l5.setPrefSize(16, 16);
        l5.setBorder(new Borders.EdgeBorder());
    }

    /**
     * Reset UI controls from current selection.
     */
    public void resetUI()
    {
        // Get current border (or default, if not available)
        Styler styler = getStyler();
        Border border = styler.getBorder();
        if (border==null) border = Border.blackBorder();

        // Update StrokeCheckBox
        Border bdr = styler.getBorder();
        setViewValue("StrokeCheckBox", bdr!=null);

        // Update Border Buttons
        setViewValue("LineBdrButton", bdr instanceof Borders.LineBorder);
        setViewValue("LowerBdrButton", bdr instanceof Borders.BevelBorder && ((Borders.BevelBorder)bdr).getType()==0);
        setViewValue("RaiseBdrButton", bdr instanceof Borders.BevelBorder && ((Borders.BevelBorder)bdr).getType()==1);
        setViewValue("EtchBdrButton", bdr instanceof Borders.EtchBorder);
        setViewValue("EdgeBdrButton", bdr instanceof Borders.EdgeBorder);

        // Get stroke tool, install tool UI in stroke panel and ResetUI
        StylerOwner btool = getTool(border);
        getView("StrokePane", BoxView.class).setContent(btool.getUI());
        btool.resetLater();
    }

    /**
     * Updates border from UI controls.
     */
    public void respondUI(ViewEvent anEvent)
    {
        // Get styler
        Styler styler = getStyler();

        // Handle StrokeCheckBox: Add border if not there or remove if there
        if (anEvent.equals("StrokeCheckBox")) {
            boolean selected = anEvent.getBoolValue();
            Border border = selected ? Border.blackBorder() : null;
            styler.setBorder(border);
        }

        // Handle LineBdrButton, LowerBdrButton, RaiseBdrButton, EtchBdrButton
        if (anEvent.equals("LineBdrButton")) styler.setBorder(Border.blackBorder());
        if (anEvent.equals("LowerBdrButton")) styler.setBorder(new Borders.BevelBorder(0));
        if (anEvent.equals("RaiseBdrButton")) styler.setBorder(new Borders.BevelBorder(1));
        if (anEvent.equals("EtchBdrButton")) styler.setBorder(new Borders.EtchBorder());
        if (anEvent.equals("EdgeBdrButton")) styler.setBorder(new Borders.EdgeBorder());
    }

    /**
     * Returns the specific tool for a given fill.
     */
    public StylerOwner getTool(Object anObj)
    {
        // Get tool from tools map - just return if present
        Class cls = anObj instanceof Class? (Class)anObj : anObj.getClass();
        StylerOwner tool = _tools.get(cls);
        if(tool==null) {
            _tools.put(cls, tool=getToolImpl(cls));
            tool.setStyler(getStyler());
        }
        return tool;
    }

    /**
     * Returns the specific tool for a given fill.
     */
    private StylerOwner getToolImpl(Class aClass)
    {
        if(aClass==Borders.EdgeBorder.class) return new EdgeBorderTool();
        if(Border.class.isAssignableFrom(aClass)) return new LineBorderTool();
        throw new RuntimeException("BorderTool.getToolImpl: Unknown border class: " + aClass);
    }
}
