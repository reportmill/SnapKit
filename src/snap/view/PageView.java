package snap.view;
import snap.gfx.*;
import snap.util.*;

/**
 * A custom class.
 */
public class PageView extends SpringView {
    
    // Constant
    static final Border PAGE_VIEW_BORDER = Border.createLineBorder(Color.BLACK, 1);

/**
 * XML Archival.
 */
public void fromXMLView(XMLArchiver anArchiver, XMLElement anElement)
{
    super.fromXMLView(anArchiver, anElement);
    setFill(Color.WHITE);
    setBorder(PAGE_VIEW_BORDER);
    setEffect(new ShadowEffect());
}

/**
 * Override to return white.
 */
public Paint getDefaultFill()  { return Color.WHITE; }

/**
 * Returns the default border.
 */
public Border getDefaultBorder()  { return PAGE_VIEW_BORDER; }

}