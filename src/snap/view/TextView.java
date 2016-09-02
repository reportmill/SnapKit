package snap.view;
import snap.gfx.*;
import snap.util.*;

/**
 * A view subclass for editing rich text.
 */
public class TextView extends TextViewBase {

    // Whether to send action on return
    boolean         _sendActionOnReturn;

/**
 * Creates a new TextView.
 */
public TextView()
{
    setFill(Color.WHITE);
    setEditable(true);
    setFocusKeysEnabled(false);
}

/**
 * Returns whether text area sends action on return.
 */
public boolean getSendActionOnReturn()  { return _sendActionOnReturn; }

/**
 * Sets whether text area sends action on return.
 */
public void setSendActionOnReturn(boolean aValue)
{
    if(aValue==_sendActionOnReturn) return;
    if(_sendActionOnReturn) enableEvents(Action);
    else getEventAdapter().disableEvents(this, Action);
    firePropChange("SendActionOnReturn", _sendActionOnReturn, _sendActionOnReturn = aValue);
}

/**
 * Override to return white.
 */
public Paint getDefaultFill()  { return Color.WHITE; }

/**
 * XML archival.
 */
public XMLElement toXMLView(XMLArchiver anArchiver)
{
    // Archive text component attributes
    XMLElement e = super.toXMLView(anArchiver);
    
    // Archive SendActionOnReturn
    if(getSendActionOnReturn()) e.add("SendActionOnReturn", true);
    return e;
}

/**
 * XML unarchival.
 */
public void fromXMLView(XMLArchiver anArchiver, XMLElement anElement)
{
    // Unarchive text component attributes
    super.fromXMLView(anArchiver, anElement);

    // Unarchive SendActionOnReturn
    if(anElement.hasAttribute("SendActionOnReturn") || anElement.hasAttribute("send-action-on-return"))
        setSendActionOnReturn(anElement.getAttributeBoolValue("SendActionOnReturn", true));
}

}