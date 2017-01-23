/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import java.util.*;
import java.util.function.Consumer;
import snap.gfx.*;
import snap.util.*;

/**
 * A view that combines a TextField and a ListView for selecting/entering an item from a list.
 * If ListView not explicitly set, ListText creates and uses a PopupList.
 */
public class ListText <T> extends ParentView implements View.Selectable <T> {

    // The TextField
    TextField              _text;
    
    // The ListView
    ListView <T>           _list;

    // The Button to show popup
    Button                 _button;
    
    // Whether to show popup button
    boolean                _showButton = true;
    
    // The HBox layout
    ViewLayout.HBoxLayout  _layout = new ViewLayout.HBoxLayout(this);
    
    // The arrow image
    static Image           _arrowImg;
    
/**
 * Creates a new ListText.
 */
public ListText()
{
    getTextField();
    getButton();
    enableEvents(Action);
} 

/**
 * Returns the TextField.
 */
public TextField getTextField()
{
    if(_text!=null) return _text;
    _text = new TextField(); _text.setGrowWidth(true); _text.setColumnCount(0);
    _text.addEventHandler(e -> handleChildActionEvent(_text), Action);
    _text.addPropChangeListener(pce -> textFieldFocusChanged(), View.Focused_Prop);
    addChild(_text);
    return _text;
}

/**
 * Returns the button.
 */
public Button getButton()
{
    if(_button!=null) return _button;
    _button = new Button(); _button.setPrefSize(14,20);
    _button.setImage(getArrowImage());
    _button.addEventHandler(e -> showPopup(), MousePress);
    addChild(_button);
    return _button;
}

/**
 * Returns whether to show poup button.
 */
public boolean isShowButton()  { return _showButton; }

/**
 * Sets whether to show popup button.
 */
public void setShowButton(boolean aValue)
{
    if(aValue==_showButton) return;
    _showButton = aValue;
    if(aValue) addChild(_button);
    else removeChild(_button);
}

/**
 * Returns the ListView.
 */
public ListView <T> getListView()
{
    if(_list==null) setListView(createListView());
    return _list;
}

/**
 * Sets the ListView.
 */
public void setListView(ListView <T> aListView)
{
    _list = aListView;
    _list.addEventHandler(e -> handleChildActionEvent(_list), Action);
    _list.addPropChangeListener(pce -> listViewSelectionChanged(), ListView.SelectedIndex_Prop);
    
    // Reset ShowButton based on whether ListView is PopupList
    if(!(aListView instanceof PopupList))
        setShowButton(false);
}

/**
 * Creates a ListView (PopupList by default) for this ListText (if ListView not previously set).
 */
protected ListView <T> createListView()
{
    PopupList list = new PopupList();
    list.setAltPaint(null); list.setTargeting(true);
    return list;
}

/**
 * Returns whether ListText ListView is PopupList.
 */
public boolean isPopup()  { return _list instanceof PopupList; }

/**
 * Returns whether ListText ListView is PopupList.
 */
public PopupList <T> getPopupList()  { return getListView() instanceof PopupList? (PopupList)_list : null; }

/**
 * Returns whether ListText popup is visible.
 */
public boolean isPopupVisible()  { return getListView().isVisible(); }

/**
 * Makes ListText PopupList visible.
 */
public void showPopup()
{
    PopupList plist = getPopupList();
    plist.setMinWidth(getWidth()); plist.setFont(getFont());
    plist.show(this, 0,getHeight());
}

/**
 * Returns the items.
 */
public List <T> getItems()  { return getListView().getItems(); }

/**
 * Sets the items.
 */
public void setItems(List <T> theItems)  { getListView().setItems(theItems); }

/**
 * Sets the items.
 */
public void setItems(T ... theItems)  { getListView().setItems(theItems); }

/**
 * Returns the selected index.
 */
public int getSelectedIndex()  { return getListView().getSelectedIndex(); }

/**
 * Sets the selected index.
 */
public void setSelectedIndex(int anIndex)  { getListView().setSelectedIndex(anIndex); }

/**
 * Returns the selected item.
 */
public T getSelectedItem()  { return getListView().getSelectedItem(); }

/**
 * Sets the selected index.
 */
public void setSelectedItem(T anItem)  { getListView().setSelectedItem(anItem); }

/**
 * Called to set method for rendering.
 */
public Consumer<ListCell<T>> getCellConfigure()  { return getListView().getCellConfigure(); }

/**
 * Called to set method for rendering.
 */
public void setCellConfigure(Consumer<ListCell<T>> aCC)  { getListView().setCellConfigure(aCC); }

/**
 * Override to get from TextField.
 */
public String getText()  { return getTextField().getText(); }

/**
 * Override to set in TextField and ListView.
 */
public void setText(String aString)
{
    getTextField().setText(aString);
    getListView().setText(aString);
}

/**
 * Called when TextField focus changed.
 */
protected void textFieldFocusChanged()
{
    if(!_text.isFocused()) return;
    _text.selectAll();
    if(isPopup())
        showPopup();
}

/**
 * Called when referenced ListView selection changes.
 */
protected void listViewSelectionChanged()
{
    String str = getListView().getText();
    getTextField().setText(str);
}

/**
 * Called when PopupList or TextField fire action.
 */
protected void handleChildActionEvent(View aView)
{
    // Handle PopupList action
    //if(aView==getListView()) setSelectedItem(getListView().getSelectedItem());
    
    // Handle TextField action
    if(aView==getTextField())
        getListView().setText(_text.getText());

    // Fire action    
    fireActionEvent();
}

/**
 * Returns the selection start.
 */
public int getSelStart()  { return getTextField().getSelStart(); }

/**
 * Sets the selection start.
 */
public void setSelStart(int aValue)  { getTextField().setSelStart(aValue); }

/**
 * Returns the selection end.
 */
public int getSelEnd()  { return getTextField().getSelStart(); }

/**
 * Sets the selection end.
 */
public void setSelEnd(int aValue)  { getTextField().setSelEnd(aValue); }

/**
 * Selects whole text.
 */
public void selectAll()  { getTextField().selectAll(); }

/**
 * Returns the preferred width.
 */
protected double getPrefWidthImpl(double aH)  { return _layout.getPrefWidth(aH); }

/**
 * Returns the preferred height.
 */
protected double getPrefHeightImpl(double aW)  { return _layout.getPrefHeight(aW); }

/**
 * Layout children.
 */
protected void layoutChildren()  { _layout.layoutChildren(); }

/**
 * Returns an Icon of a down arrow.
 */
protected static Image getArrowImage()
{
    // If down arrow icon hasn't been created, create it
    if(_arrowImg!=null) return _arrowImg;
    Image img = Image.get(9,7,true); Painter pntr = img.getPainter();
    Polygon poly = new Polygon(1.5, 1.5, 7.5, 1.5, 4.5, 5.5);
    pntr.setColor(new Color("#FFFFFF99")); pntr.drawLine(4.5,8,2,2); pntr.drawLine(4.5,8,7,2);
    pntr.setColor(Color.DARKGRAY); pntr.draw(poly); pntr.fill(poly); pntr.flush();
    return _arrowImg = img;
}

/**
 * Override to use button.
 */
public Pos getAlign()  { return _text.getLabel().getAlign(); }

/**
 * Override to send to button.
 */
public void setAlign(Pos aPos)  { _text.getLabel().setAlign(aPos); }

/**
 * Override to send to button.
 */
public void setDisabled(boolean aValue)
{
    super.setDisabled(aValue); _text.setDisabled(aValue); _button.setDisabled(aValue);
}

/**
 * Returns Value property name.
 */
public String getValuePropName()
{
    T item = getSelectedItem();
    return item!=null? SelectedItem_Prop : Text_Prop;
}

/**
 * XML archival.
 */
public XMLElement toXMLView(XMLArchiver anArchiver)
{
    // Archive basic view attributes
    XMLElement e = super.toXMLView(anArchiver);
    
    // Archive ShowButton
    if(isShowButton()!=isPopup())
        e.add("ShowButton", isShowButton());
    
    // Return element
    return e;
}

/**
 * XML unarchival.
 */
public void fromXMLView(XMLArchiver anArchiver, XMLElement anElement)
{
    // Unarchive basic view attributes
    super.fromXMLView(anArchiver, anElement);
    
    // Unarchive ShowButton
    if(anElement.hasAttribute("ShowButton"))
        setShowButton(anElement.getAttributeBooleanValue("ShowButton"));
}

}