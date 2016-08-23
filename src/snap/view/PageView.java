package snap.view;
import snap.gfx.*;
import snap.util.*;

/**
 * A custom class.
 */
public class PageView extends SpringView {

/**
 * XML Archival.
 */
public void fromXMLView(XMLArchiver anArchiver, XMLElement anElement)
{
    super.fromXMLView(anArchiver, anElement);
    setFill(Color.WHITE);
    setBorder(Border.createLineBorder(Color.BLACK, 1));
    setEffect(new ShadowEffect());
}

}