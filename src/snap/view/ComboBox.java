/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import java.util.*;
import java.util.function.Consumer;
import snap.gfx.*;
import snap.util.*;

/**
 * A Button that shows a list when pressed.
 */
public class ComboBox <T> extends ParentView implements View.Selectable <T> {

    // The selected item
    T                      _selItem;
    
    // The menu button to show popup
    Button                 _btn = new Button();
    
    // The popup list
    PopupList <T>          _plist;
    
    // The HBox layout
    ViewLayout.HBoxLayout  _layout = new ViewLayout.HBoxLayout(this);
    
/**
 * Creates a new ComboBox.
 */
public ComboBox()
{
    // Create/configure button
    _btn.setMinWidth(18); _layout.setFillHeight(true); _btn.getLabel().setGrowWidth(true);
    _btn.setImageAfter(ListText.getArrowImage()); _btn.setGrowWidth(true); _btn.getLabel().setPadding(0,2,0,4);
    _btn.getLabel().getGraphicAfter().setLean(Pos.CENTER_RIGHT);
    _btn.addEventHandler(e -> showPopup(), MousePress);
    addChild(_btn);
    
    // Create/configure PopupList
    getPopup();
    
    // Enable Action event
    enableEvents(Action);
}

/**
 * Returns the PopupList.
 */
public PopupList <T> getPopup()
{
    if(_plist!=null) return _plist;
    _plist = new PopupList();
    _plist.setAltPaint(null); _plist.setTargeting(true);
    _plist.addEventHandler(e -> fireActionEvent(), Action);
    _plist.addPropChangeListener(pce -> popupListSelectionChanged(), ListView.SelectedIndex_Prop);
    return _plist;
}

/**
 * Returns whether PopupList is visible.
 */
public boolean isPopupVisible()  { return _plist.isVisible(); }

/**
 * Makes combobox popup visible.
 */
public void showPopup()
{
    _plist.setMinWidth(getWidth()); _plist.setFont(getFont());
    double x = getWidth() - _plist.getBestWidth(-1) - 8;
    double y = getHeight() - (getSelectedIndex()+1)*_plist.getRowHeight() + 4;
    _plist.show(this,x,y);
}

/**
 * Returns the items.
 */
public List <T> getItems()  { return _plist.getItems(); }

/**
 * Sets the items.
 */
public void setItems(List <T> theItems)  { _plist.setItems(theItems); }

/**
 * Sets the items.
 */
public void setItems(T ... theItems)  { _plist.setItems(theItems); }

/**
 * Returns the selected index.
 */
public int getSelectedIndex()  { return _plist.getSelectedIndex(); }

/**
 * Sets the selected index.
 */
public void setSelectedIndex(int anIndex)  { _plist.setSelectedIndex(anIndex); }

/**
 * Returns the selected item.
 */
public T getSelectedItem()  { return _plist.getSelectedItem(); }

/**
 * Sets the selected index.
 */
public void setSelectedItem(T anItem)  { _plist.setSelectedItem(anItem); }

/**
 * Called to set method for rendering.
 */
public Consumer<ListCell<T>> getCellConfigure()  { return getPopup().getCellConfigure(); }

/**
 * Called to set method for rendering.
 */
public void setCellConfigure(Consumer<ListCell<T>> aCC)  { getPopup().setCellConfigure(aCC); }

/**
 * Return combo box text.
 */
public String getText()  { return _plist.getText(); }

/**
 * Set combo box text.
 */
public void setText(String aString)
{
    _btn.setText(aString);
    _plist.setText(aString);
}

/**
 * Returns the preferred width.
 */
protected double getPrefWidthImpl(double aH)  { return _layout.getPrefWidth(-1); }

/**
 * Returns the preferred height.
 */
protected double getPrefHeightImpl(double aW)  { return _layout.getPrefHeight(-1); }

/**
 * Layout children.
 */
protected void layoutChildren()  { _layout.layoutChildren(); }

/**
 * Called when referenced PopupList selection changes.
 */
protected void popupListSelectionChanged()
{
    String str = getPopup().getText();
    _btn.setText(str);
}

/**
 * Override to use button.
 */
public Pos getAlign()  { return _btn.getLabel().getAlign(); }

/**
 * Override to send to button.
 */
public void setAlign(Pos aPos)  { _btn.getLabel().setAlign(aPos); }

/**
 * Override to send to button.
 */
public void setDisabled(boolean aValue)  { super.setDisabled(aValue); _btn.setDisabled(aValue); }

/**
 * Returns Value property name.
 */
public String getValuePropName()  { return "SelectedItem"; }

/**
 * XML archival.
 */
public XMLElement toXMLView(XMLArchiver anArchiver)
{
    // Archive basic view attributes
    XMLElement e = super.toXMLView(anArchiver);
    
    // Archive Items
    List items = getItems();
    if(items!=null) for(int i=0, iMax=items.size(); i<iMax; i++) {
        XMLElement item = new XMLElement("item");
        item.add("text", getItems().get(i).toString());
        e.add(item);
    }
    
    // Archive SelectedIndex 
    if(getSelectedIndex()>=0)
        e.add("SelectedIndex", getSelectedIndex());
    
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
    
    // Unarchive items
    List items = new ArrayList();
    for(int i=anElement.indexOf("item"); i>=0; i=anElement.indexOf("item", i+1))
        items.add(anElement.get(i).getAttributeValue("text"));
    if(items.size()>0) setItems(items);
    
    // Unarchive SelectedIndex, ItemKey
    if(anElement.hasAttribute("SelectedIndex"))
        setSelectedIndex(anElement.getAttributeIntValue("SelectedIndex"));
}

}