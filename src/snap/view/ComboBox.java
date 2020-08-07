/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import java.util.*;
import java.util.function.*;

import snap.geom.Polygon;
import snap.geom.Pos;
import snap.gfx.*;
import snap.util.*;

/**
 * A View that combines a TextField, Button and List to allow selection of list items in the space of a TextField
 * and/or button. The default form just shows a button that triggers a popup list. The ShowTextField option adds a
 * TextField for editing. This form can also be attached to any ListView to conveniently find items in a long list.
 */
public class ComboBox <T> extends ParentView implements Selectable<T> {

    // The TextField (if showing)
    private TextField  _text;
    
    // The Button to show popup (and maybe text)
    private Button  _button;
    
    // The ListView
    private ListView <T>  _list;
    
    // The function to format text
    private Function <T,String>  _itemTextFunc;
    
    // Whether text entered in TextField should filter ListView items
    private boolean  _filterList;
    
    // A function to return filtered items from a prefix
    private Function <String,List<T>>  _prefixFunction;

    // The Last list of all items set
    private List <T>  _items;

    // The arrow image
    private static Image  _arrowImg;
    
    /**
     * Creates a new ComboBox.
     */
    public ComboBox()
    {
        getButton();
        comboChanged();
        enableEvents(Action);
    }

    /**
     * Returns function for deteriming text for an item.
     */
    public Function <T,String> getItemTextFunction()  { return _itemTextFunc; }

    /**
     * Sets function for deteriming text for an item.
     */
    public void setItemTextFunction(Function <T,String> aFunc)
    {
        _itemTextFunc = aFunc;
        if (_list!=null) _list.setItemTextFunction(aFunc);
    }

    /**
     * Returns method to configure list cells.
     */
    public Consumer<ListCell<T>> getCellConfigure()  { return getListView().getCellConfigure(); }

    /**
     * Sets method to configure list cells.
     */
    public void setCellConfigure(Consumer<ListCell<T>> aCC)  { getListView().setCellConfigure(aCC); }

    /**
     * Returns the TextField.
     */
    public TextField getTextField()
    {
        if (_text!=null) return _text;
        _text = new TextField(); _text.setGrowWidth(true); _text.setColCount(0);
        _text.addEventHandler(e -> textFieldFiredAction(), Action);
        _text.addEventFilter(e -> textFieldKeyPressed(e), KeyPress);
        _text.addEventFilter(e -> ViewUtils.runLater(() -> textFieldKeyTyped(e)), KeyType);
        _text.addPropChangeListener(pce -> textFieldFocusChanged(), View.Focused_Prop);
        return _text;
    }

    /**
     * Returns the button.
     */
    public Button getButton()
    {
        if (_button!=null) return _button;
        _button = new Button();
        _button.addEventHandler(e -> showPopup(), MousePress);
        addChild(_button);
        return _button;
    }

    /**
     * Returns the ListView.
     */
    public ListView <T> getListView()
    {
        if (_list==null) setListView(createListView());
        return _list;
    }

    /**
     * Sets the ListView.
     */
    public void setListView(ListView <T> aListView)
    {
        // Set List
        _list = aListView;

        // Start listening to Action and SelIndex changes
        _list.addEventHandler(e -> listViewFiredAction(), Action);
        _list.addPropChangeListener(pce -> listViewSelectionChanged(), ListView.Sel_Prop);

        // If not PopupList, turn off button and start listening to TextField KeyType events
        if (!(aListView instanceof PopupList))
            setShowButton(false);
    }

    /**
     * Creates a ListView for this ComboBox (PopupList by default).
     */
    protected ListView <T> createListView()
    {
        PopupList plist = new PopupList();
        plist.setAltPaint(null); plist.setTargeting(true);
        plist.setItemTextFunction(getItemTextFunction());
        return plist;
    }

    /**
     * Returns whether to show TextField.
     */
    public boolean isShowTextField()  { return _text!=null && _text.getParent()!=null; }

    /**
     * Sets whether to show TextField.
     */
    public void setShowTextField(boolean aValue)
    {
        if (aValue==isShowTextField()) return;
        if (aValue) addChild(getTextField());
        else removeChild(getTextField());
        comboChanged();
    }

    /**
     * Returns whether to show button.
     */
    public boolean isShowButton()  { return _button.getParent()!=null; }

    /**
     * Sets whether to show button.
     */
    public void setShowButton(boolean aValue)
    {
        if (aValue==isShowButton()) return;
        if (aValue) addChild(_button);
        else removeChild(_button);
    }

    /**
     * Returns Whether text entered in TextField should filter ListView items.
     */
    public boolean isFilterList()  { return _filterList; }

    /**
     * Returns Whether text entered in TextField should filter ListView items.
     */
    public void setFilterList(boolean aValue)  { _filterList = aValue; }

    /**
     * Returns the function to return filtered items from a prefix
     */
    public Function <String,List<T>> getPrefixFunction()  { return _prefixFunction; }

    /**
     * Sets the function to return filtered items from a prefix
     */
    public void setPrefixFunction(Function <String,List<T>> aFunc)  { _prefixFunction = aFunc; }

    /**
     * Called when Button/TextField changes.
     */
    protected void comboChanged()
    {
        // If ShowTextField, configure small popup button
        if (isShowTextField()) {
            _button.setPrefSize(14,20); _button.setGrowWidth(false);
            _button.setImage(getArrowImage()); _button.setGraphicAfter(null);
        }

        // Otherwse, configure wide popup button
        else {
            _button.setMinWidth(18); _button.setGrowWidth(true);
            _button.getLabel().setGrowWidth(true); _button.getLabel().setPadding(0,2,0,4);
            _button.setImageAfter(getArrowImage()); _button.getGraphicAfter().setLean(Pos.CENTER_RIGHT);
        }
    }

    /**
     * Returns whether ComboBox shows a PopupList.
     */
    public boolean isPopup()  { return getListView() instanceof PopupList; }

    /**
     * Returns whether ComboBox popup is showing on screen.
     */
    public boolean isPopupShowing()  { return getListView().isShowing(); }

    /**
     * Returns ComboBox PopupList.
     */
    public PopupList <T> getPopupList()  { return getListView() instanceof PopupList? (PopupList)_list : null; }

    /**
     * Makes combobox popup visible.
     */
    public void showPopup()
    {
        // Get/configure popup list
        PopupList plist = getPopupList();
        plist.setMinWidth(getWidth()); plist.setFont(getFont());

        // Get X/Y at bottom right (if button only, shift so that selected item is over button text)
        double x = 0, y = getHeight();
        if (!isShowTextField()) {
            x = getWidth() - plist.getBestWidth(-1) - 8;
            y = getHeight() - (getSelIndex()+1)*plist.getRowHeight() + 4;
        }

        // Show list
        plist.show(this,x,y);
    }

    /**
     * Returns the number of items.
     */
    public int getItemCount()  { return getListView().getItemCount(); }

    /**
     * Returns the individual item at given index.
     */
    public T getItem(int anIndex)  { return getListView().getItem(anIndex); }

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
    public int getSelIndex()  { return getListView().getSelIndex(); }

    /**
     * Sets the selected index.
     */
    public void setSelIndex(int anIndex)  { getListView().setSelIndex(anIndex); }

    /**
     * Returns the selected item.
     */
    public T getSelItem()  { return getListView().getSelItem(); }

    /**
     * Sets the selected index.
     */
    public void setSelItem(T anItem)  { getListView().setSelItem(anItem); }

    /**
     * Return combo box text.
     */
    public String getText()
    {
        // If selected Item, use it's text (potentially corrects case issues)
        T selItem = getSelItem();
        if (selItem!=null)
            return getText(selItem);

        // Otherwise, if ShowTextField, return actual text
        if (isShowTextField())
            return getTextField().getText();
        return getListView().getText();
    }

    /**
     * Set combo box text.
     */
    public void setText(String aString)
    {
        // If matching item, set in ListView
        T item = getListView().getListArea().getItemForText(aString);
        if (item!=null)
            getListView().setSelItem(item);

        // Set in TextField or Button
        String str = item!=null? getListView().getText(item) : aString;
        if (isShowTextField()) _text.setText(aString);
        else _button.setText(aString);
    }

    /**
     * Returns text for item.
     */
    public String getText(T anItem)
    {
        if (anItem==null) return null;
        if (_itemTextFunc!=null) return _itemTextFunc.apply(anItem);
        return anItem.toString();
    }

    /**
     * Called when TextField focus changed.
     */
    protected void textFieldFocusChanged()
    {
        // On focus gained: SelectAll, get copy of current items,
        if (_text.isFocused()) {
            _text.selectAll();
            _items = new ArrayList(getListView().getItems());
            if (isPopup() && getItemCount()>0)
                showPopup();
        }

        // On focus lost: Restore list items after delay (twice, in case textFieldFiredAction with scrollSelToVisible)
        else getEnv().runLater(() -> getEnv().runLater(() -> { _list.setItems(_items); _items = null; }));
    }

    /**
     * Called before TextField has KeyPress.
     */
    protected void textFieldKeyPressed(ViewEvent anEvent)
    {
        // Handle UpArrow/DownArrow: send to list
        if (anEvent.isUpArrow())  { getListView().selectUp(); anEvent.consume(); }
        else if (anEvent.isDownArrow()) { getListView().selectDown(); anEvent.consume(); }

        // Handle EscapeKey
        else if (anEvent.isEscapeKey()) {

            // If value has changed, reset to focus gained values
            if (_text.isEdited()) {
                _list.setItems(_items);
                _list.setText(_text._focusGainedText);
                _text.selectAll();
                if (getItemCount()==0 && isPopup() && isPopupShowing()) getPopupList().hide();
            }

            // Otherwise have RootView.FocusedViewLast request focus
            else if (getWindow().getFocusedViewLast()!=null) {
                if (isPopup() && isPopupShowing()) getPopupList().hide();
                getWindow().getFocusedViewLast().requestFocus();
            }

            anEvent.consume();
        }
    }

    /**
     * Called after TextField has KeyType.
     */
    protected void textFieldKeyTyped(ViewEvent anEvent)
    {
        // Get prefix text and current selection
        String text = _text.getText();
        int selStart = _text.getSelStart();

        // Not sure if/why we needed this
        if (!_text.isSelEmpty()) { System.err.println("ComboBox.textFieldKeyTyped: not SelEmpty?"); return; }
        //text = text.substring(0, selStart);

        // Get items for prefix
        List <T> items = getItemsForPrefix(selStart>0? text : "");
        T item = items.size()>0? items.get(0) : null;

        // What to do if empty text?
        if (text.length()==0 && items.size()>0)
            item = getSelItem();

        // Set ListView Items, SelItem
        if (isFilterList()) _list.setItems(items);
        _list.setSelItem(item);
        if (items.size()<=1 && isPopup() && isPopupShowing())
            getPopupList().hide();

        // Reset text and selection (since List.setSelItem changes it)
        _text.setText(text);
        _text.setSel(selStart);

        // If completion item available, set completion text
        if (item!=null) {
            String ctext = getText(item);
            _text.setCompletionText(ctext);
        }

        // Handle KeyPress with no PopupShowing
        if (isPopup() && !isPopupShowing() && getItemCount()>1)
            showPopup();
    }

    /**
     * Called to return items for prefix.
     */
    protected List <T> getItemsForPrefix(String aStr)
    {
        // If PrefixFunction, use it instead
        if (_prefixFunction!=null)
            return _prefixFunction.apply(aStr);

        // If no string, return all items
        if (aStr.length()==0) return _items;

        // Otherwise, return items that start with prefix
        List <T> list = new ArrayList();
        for (T itm : _items)
            if (StringUtils.startsWithIC(_list.getText(itm), aStr))
                list.add(itm);
        return list;
    }

    /**
     * Called when TextField fires action.
     */
    protected void textFieldFiredAction()
    {
        if (isPopup() && isPopupShowing())
            getPopupList().hide();
        //getListView().setText(_text.getText());
        fireActionEvent(null);
    }

    /**
     * Called when ListView fires action.
     */
    protected void listViewFiredAction()
    {
        if (getListView() instanceof PopupList)
            fireActionEvent(null);
    }

    /**
     * Called when referenced PopupList selection changes.
     */
    protected void listViewSelectionChanged()
    {
        T item = getSelItem();
        String str = getText(item);
        if (isShowTextField()) { _text.setText(str); _text.selectAll(); }
        else _button.setText(str);
    }

    /**
     * Override to send to text/button.
     */
    public void setAlign(Pos aPos)
    {
        super.setAlign(aPos);
        if (isShowTextField()) _text.setAlign(aPos);
        else _button.setAlign(aPos);
    }

    /**
     * Returns the default alignment.
     */
    public Pos getDefaultAlign()  { return Pos.CENTER_LEFT; }

    /**
     * Override to send to button.
     */
    public void setDisabled(boolean aValue)
    {
        super.setDisabled(aValue);
        if (_text!=null) _text.setDisabled(aValue);
        _button.setDisabled(aValue);
    }

    /**
     * Returns Value property name.
     */
    public String getValuePropName()
    {
        T item = getSelItem();
        return item!=null? SelItem_Prop : Text_Prop;
    }

    /**
     * Returns the preferred width.
     */
    protected double getPrefWidthImpl(double aH)  { return RowView.getPrefWidth(this, -1); }

    /**
     * Returns the preferred height.
     */
    protected double getPrefHeightImpl(double aW)  { return RowView.getPrefHeight(this, -1); }

    /**
     * Layout children.
     */
    protected void layoutImpl()
    {
        boolean fillHeight = !isShowTextField();
        RowView.layout(this, fillHeight);
    }

    /**
     * Override to focus text or button.
     */
    public void requestFocus()
    {
        if (isShowTextField()) getTextField().requestFocus();
        else getButton().requestFocus();
    }

    /**
     * XML archival.
     */
    public XMLElement toXMLView(XMLArchiver anArchiver)
    {
        // Archive basic view attributes
        XMLElement e = super.toXMLView(anArchiver);

        // Archive ShowTextField, ShowButton, FilterList
        if (isShowTextField()) e.add("ShowTextField", true);
        if (isShowButton()!=isPopup()) e.add("ShowButton", isShowButton());
        if (isFilterList()) e.add("FilterList", true);

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

        // Unarchive ShowTextField, ShowButton, FilterList
        if (anElement.hasAttribute("ShowTextField"))
            setShowTextField(anElement.getAttributeBooleanValue("ShowTextField"));
        if (anElement.hasAttribute("ShowButton"))
            setShowButton(anElement.getAttributeBooleanValue("ShowButton"));
        if (anElement.hasAttribute("FilterList"))
            setFilterList(anElement.getAttributeBooleanValue("FilterList"));
    }

    /**
     * Returns an Icon of a down arrow.
     */
    public static Image getArrowImage()
    {
        if (_arrowImg!=null) return _arrowImg;
        Image img = Image.get(9,7,true); Painter pntr = img.getPainter();
        Polygon poly = new Polygon(1.5, 1.5, 7.5, 1.5, 4.5, 5.5);
        pntr.setColor(new Color("#FFFFFF99")); pntr.drawLine(4.5,8,2,2); pntr.drawLine(4.5,8,7,2);
        pntr.setColor(Color.DARKGRAY); pntr.draw(poly); pntr.fill(poly); pntr.flush();
        return _arrowImg = img;
    }
}