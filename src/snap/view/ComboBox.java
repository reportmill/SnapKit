/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import java.util.*;
import java.util.function.Consumer;
import snap.gfx.*;
import snap.util.*;

/**
 * An RMShape subclass for JComboBox.
 */
public class ComboBox <T> extends ParentView implements View.Selectable <T> {

    // The selected item
    T                      _selItem;
    
    // Whether combo box is editable
    boolean                _editable;
    
    // The menu button to show popup
    Button                 _btn = new Button();
    
    // The popup list
    PopupList <T>          _plist = new PopupList();
    
    // The text field (if editable)
    TextField              _tfield;

    // The HBox layout
    ViewLayout.HBoxLayout  _layout = new ViewLayout.HBoxLayout(this);
    
    // The arrow image
    Image                  _arrowImg;
    
    // Constants for properties
    public static final String Items_Prop = "Items";
    public static final String SelectedItem_Prop = "SelectedItem";
    public static final String SelectedIndex_Prop = "SelectedIndex";

/**
 * Creates a new combobox node.
 */
public ComboBox()
{
    addChild(_btn);
    _btn.setMinWidth(18); _layout.setFillHeight(true); _btn.getLabel().setGrowWidth(true);
    _btn.setImageAfter(getArrowImage()); _btn.setGrowWidth(true); _btn.getLabel().setPadding(0,2,0,4);
    _btn.getLabel().getGraphicAfter().setLean(Pos.CENTER_RIGHT); //_btn.getLabel().getGraphic().setPadding(2,2,2,2);
    _btn.addEventHandler(e -> showPopup(), MousePress);
    _plist.setAltPaint(null); _plist.setTargeting(true);
    _plist.addEventHandler(e -> handleChildActionEvent(_plist), Action);
    enableEvents(Action);
} 

/**
 * Returns whether combo box popup is visible.
 */
public boolean isPopupVisible()  { return _plist.isVisible(); }

/**
 * Makes combobox popup visible.
 */
public void showPopup()
{
    _plist.setMinWidth(getWidth()); _plist.setFont(getFont());
    double x = 0; if(!isEditable()) x = getWidth() - _plist.getBestWidth(-1) - 8;
    double y = getHeight(); if(!isEditable()) y -= (getSelectedIndex()+1)*_plist.getRowHeight() + 4;
    _plist.show(this,x,y);
}

/**
 * Returns whether combo box is editable.
 */
public boolean isEditable()  { return _editable; }

/**
 * Sets whether combo box is editable.
 */
public void setEditable(boolean aValue)
{
    if(aValue==isEditable()) return;
    if(aValue) addChild(getTextField(),0);
    else removeChild(_tfield); _btn.setGrowWidth(!aValue);
    firePropChange("Editable", _editable, _editable=aValue);
}

/**
 * Returns the popup list.
 */
public PopupList <T> getPopup()  { return _plist; }

/**
 * Returns the textfield.
 */
public TextField getTextField()
{
    if(_tfield!=null) return _tfield;
    _tfield = new TextField();
    _tfield.setGrowWidth(true);
    _tfield.addEventHandler(e -> handleChildActionEvent(_tfield), Action);
    return _tfield;
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
public void setSelectedIndex(int anIndex)
{
    if(anIndex==getSelectedIndex()) return;
    _plist.setSelectedIndex(anIndex);
    setSelectedItem(_plist.getSelectedItem());
}

/**
 * Returns the selected item.
 */
public T getSelectedItem()  { return _selItem; }

/**
 * Sets the selected index.
 */
public void setSelectedItem(T anItem)
{
    // If value already set, just return
    if(SnapUtils.equals(anItem, _selItem)) return;
    
    // Set value
    _selItem = anItem;
    
    // Update SelectedIndex
    int index = getItems().indexOf(anItem);
    _plist.setSelectedIndex(index);
    
    // Update text node
    String str = anItem instanceof String? (String)anItem : null;
    if(str==null) str = _plist.getText(anItem);
    if(isEditable()) getTextField().setText(str);
    else _btn.setText(str);
}

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
public String getText()  { return isEditable()? getTextField().getText(): _btn.getText(); }

/**
 * Set combo box text.
 */
public void setText(String aString)
{
    // Get item for string
    T item = null;
    for(T itm : getItems()) if(SnapUtils.equals(aString, _plist.getText(itm))) { item = itm; break; }
    if(item==null && getItems().size()>0) { T itm = getItems().get(0);
        if(itm instanceof String) item = (T)aString;
        else if(itm instanceof Integer) item = (T)SnapUtils.getInteger(aString);
        else if(itm instanceof Float) item = (T)SnapUtils.getFloat(aString);
        else if(itm instanceof Double) item = (T)SnapUtils.getDouble(aString);
    }
    
    // Set selected item
    setSelectedItem(item);
}

/**
 * Called when PopupList or TextField fire action.
 */
protected void handleChildActionEvent(View aView)
{
    // Handle PopupList action
    if(aView==_plist)
        setSelectedItem(_plist.getSelectedItem());
    
    // Handle TextField action
    else setText(_tfield.getText());

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
 * Returns an Icon of a down arrow.
 */
private Image getArrowImage()
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
    
    // Archive Editable
    if(isEditable()) e.add("Editable", true);
    
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
    
    // Unarchive Editable
    if(anElement.getAttributeBoolValue("Editable")) setEditable(true);
    
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