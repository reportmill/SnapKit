/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import java.util.*;
import java.util.function.*;
import snap.geom.Polygon;
import snap.geom.Pos;
import snap.gfx.*;
import snap.props.PropSet;
import snap.util.*;

/**
 * A View that combines a TextField, Button and List to allow selection of list items in the space of a TextField
 * and/or button. The default form just shows a button that triggers a popup list. The ShowTextField option adds a
 * TextField for editing. This form can also be attached to any ListView to conveniently find items in a long list.
 */
public class ComboBox <T> extends ParentView implements Selectable<T> {

    // The TextField (if showing)
    private TextField _textField;
    
    // The Button to show popup (and maybe text)
    private Button  _button;
    
    // The ListView
    private ListView <T> _listView;
    
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

    // Constants for properties
    public static final String ShowTextField_Prop = "ShowTextField";
    public static final String ShowButton_Prop = "ShowButton";
    public static final String FilterList_Prop = "FilterList";

    /**
     * Constructor.
     */
    public ComboBox()
    {
        super();
        _align = Pos.CENTER_LEFT;

        setActionable(true);
        getButton();
        comboChanged();
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
        if (_listView != null)
            _listView.setItemTextFunction(aFunc);
    }

    /**
     * Returns method to configure list cells.
     */
    public Consumer<ListCell<T>> getCellConfigure()
    {
        return getListView().getCellConfigure();
    }

    /**
     * Sets method to configure list cells.
     */
    public void setCellConfigure(Consumer<ListCell<T>> cellConfigure)
    {
        if (cellConfigure == getCellConfigure()) return;
        getListView().setCellConfigure(cellConfigure);
    }

    /**
     * Returns the TextField.
     */
    public TextField getTextField()
    {
        if (_textField != null) return _textField;

        _textField = new TextField();
        _textField.setGrowWidth(true);
        _textField.setColCount(0);
        _textField.addEventHandler(this::handleTextFieldActionEvent, Action);
        _textField.addEventFilter(this::handleTextFieldKeyPressEvent, KeyPress);
        _textField.addEventFilter(e -> ViewUtils.runLater(() -> handleTextFieldKeyTypeEvent(e)), KeyType);
        _textField.addPropChangeListener(pc -> handleTextFieldFocusChanged(), View.Focused_Prop);
        return _textField;
    }

    /**
     * Returns the button.
     */
    public Button getButton()
    {
        if (_button != null) return _button;
        _button = new Button();
        _button.setAlign(getAlign());
        _button.addEventHandler(e -> showPopup(), MousePress);
        addChild(_button);
        return _button;
    }

    /**
     * Returns the ListView.
     */
    public ListView <T> getListView()
    {
        if (_listView == null)
            setListView(createListView());
        return _listView;
    }

    /**
     * Sets the ListView.
     */
    public void setListView(ListView <T> aListView)
    {
        // Set List
        _listView = aListView;

        // Start listening to Action and SelIndex changes
        _listView.addEventHandler(e -> listViewFiredAction(), Action);
        _listView.addPropChangeListener(pce -> listViewSelectionChanged(), ListView.Sel_Prop);

        // If not PopupList, turn off button and start listening to TextField KeyType events
        if (!(aListView instanceof PopupList))
            setShowButton(false);
    }

    /**
     * Creates a ListView for this ComboBox (PopupList by default).
     */
    protected ListView <T> createListView()
    {
        PopupList<T> popupList = new PopupList<>();
        popupList.setAltRowColor(null);
        popupList.setTargeting(true);
        popupList.setItemTextFunction(getItemTextFunction());
        return popupList;
    }

    /**
     * Returns whether to show TextField.
     */
    public boolean isShowTextField()  { return _textField !=null && _textField.getParent()!=null; }

    /**
     * Sets whether to show TextField.
     */
    public void setShowTextField(boolean aValue)
    {
        if (aValue == isShowTextField()) return;
        if (aValue)
            addChild(getTextField());
        else removeChild(getTextField());
        comboChanged();
    }

    /**
     * Returns whether to show button.
     */
    public boolean isShowButton()  { return _button.getParent() != null; }

    /**
     * Sets whether to show button.
     */
    public void setShowButton(boolean aValue)
    {
        if (aValue == isShowButton()) return;
        if (aValue)
            addChild(_button);
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
    public void setPrefixFunction(Function <String,List<T>> aFunc)
    {
        if (aFunc == getPrefixFunction()) return;
        _prefixFunction = aFunc;
    }

    /**
     * Called when Button/TextField changes.
     */
    protected void comboChanged()
    {
        // If ShowTextField, configure small popup button
        if (isShowTextField()) {
            _button.setPrefSize(14,20);
            _button.setGrowWidth(false);
            _button.setImage(getArrowImage());
            _button.setGraphicAfter(null);
        }

        // Otherwise, configure wide popup button
        else {
            _button.setMinWidth(18);
            _button.setGrowWidth(true);
            _button.getLabel().setGrowWidth(true);
            _button.getLabel().setPadding(0,2,0,4);
            _button.setImageAfter(getArrowImage());
            _button.getGraphicAfter().setLean(Pos.CENTER_RIGHT);
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
    public PopupList <T> getPopupList()
    {
        return getListView() instanceof PopupList? (PopupList<T>) _listView : null;
    }

    /**
     * Makes combobox popup visible.
     */
    public void showPopup()
    {
        // Get/configure popup list
        PopupList<T> popupList = getPopupList();
        popupList.setMinWidth(getWidth());
        popupList.setFont(getFont());

        // Get X/Y at bottom right (if button only, shift so that selected item is over button text)
        double x = 0;
        double y = getHeight();
        if (!isShowTextField()) {
            x = getWidth() - popupList.getBestWidth(-1) - 8;
            y = getHeight() - (getSelIndex() + 1) * popupList.getRowHeight() + 4;
        }

        // Show list
        popupList.show(this, x, y);
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
    @Override
    public List <T> getItems()  { return getListView().getItems(); }

    /**
     * Sets the items.
     */
    @Override
    public void setItems(List <T> theItems)  { getListView().setItems(theItems); }

    /**
     * Returns the selected index.
     */
    @Override
    public int getSelIndex()  { return getListView().getSelIndex(); }

    /**
     * Sets the selected index.
     */
    @Override
    public void setSelIndex(int anIndex)  { getListView().setSelIndex(anIndex); }

    /**
     * Returns the selected item.
     */
    @Override
    public T getSelItem()  { return getListView().getSelItem(); }

    /**
     * Sets the selected index.
     */
    @Override
    public void setSelItem(T anItem)  { getListView().setSelItem(anItem); }

    /**
     * Return combo box text.
     */
    @Override
    public String getText()
    {
        // If selected Item, use it's text (potentially corrects case issues)
        T selItem = getSelItem();
        if (selItem != null)
            return getText(selItem);

        // Otherwise, if ShowTextField, return actual text
        if (isShowTextField())
            return getTextField().getText();
        return getListView().getText();
    }

    /**
     * Set combo box text.
     */
    @Override
    public void setText(String aString)
    {
        // If matching item, set in ListView
        ListView<T> listView = getListView();
        T item = listView.getItemForText(aString);
        if (item != null)
            listView.setSelItem(item);

        // Set in TextField or Button
        String str = item != null ? listView.getText(item) : aString;
        if (isShowTextField())
            _textField.setText(str);
        else _button.setText(str);
    }

    /**
     * Returns text for item.
     */
    public String getText(T anItem)
    {
        if (anItem == null)
            return null;
        if (_itemTextFunc != null)
            return _itemTextFunc.apply(anItem);
        return anItem.toString();
    }

    /**
     * Called when TextField focus changed.
     */
    protected void handleTextFieldFocusChanged()
    {
        // On focus gained: SelectAll, get copy of current items,
        if (_textField.isFocused()) {
            _textField.selectAll();
            _items = new ArrayList<>(getListView().getItems());
            if (isPopup() && getItemCount()>0)
                showPopup();
        }

        // On focus lost: Restore list items after delay (twice, in case textFieldFiredAction with scrollSelToVisible)
        else getEnv().runLater(() -> getEnv().runLater(() -> { _listView.setItems(_items); _items = null; }));
    }

    /**
     * Called before TextField has KeyPress.
     */
    protected void handleTextFieldKeyPressEvent(ViewEvent anEvent)
    {
        // Handle UpArrow/DownArrow: send to list
        if (anEvent.isUpArrow())  {
            getListView().selectUp();
            anEvent.consume();
        }
        else if (anEvent.isDownArrow()) {
            getListView().selectDown();
            anEvent.consume();
        }

        // Handle EscapeKey
        else if (anEvent.isEscapeKey())
            handleTextFieldEscapeKeyPressEvent(anEvent);
    }

    /**
     * Called to handle escape key press.
     */
    private void handleTextFieldEscapeKeyPressEvent(ViewEvent anEvent)
    {
        // If value has changed, reset to focus gained values
        if (_textField.isEdited()) {
            _listView.setItems(_items);
            _listView.setText(_textField._focusGainedText);
            _textField.selectAll();
            if (getItemCount() == 0 && isPopup() && isPopupShowing())
                getPopupList().hide();
            anEvent.consume();
        }

        // Otherwise hide popup and resign focus
        else {
            if (isPopup() && isPopupShowing())
                getPopupList().hide();
            getWindow().requestFocus(null);
            if (getWindow().getFocusedView() != null)
                anEvent.consume();
        }
    }

    /**
     * Called after TextField has KeyType.
     */
    protected void handleTextFieldKeyTypeEvent(ViewEvent anEvent)
    {
        // Get prefix text and current selection
        String text = _textField.getText();
        int selStart = _textField.getSelStart();

        // Not sure if/why we needed this
        if (!_textField.isSelEmpty()) { System.err.println("ComboBox.textFieldKeyTyped: not SelEmpty?"); return; }
        //text = text.substring(0, selStart);

        // Get items for prefix
        List <T> items = getItemsForPrefix(selStart>0? text : "");
        T item = !items.isEmpty() ? items.get(0) : null;

        // What to do if empty text?
        if (text.isEmpty() && !items.isEmpty())
            item = getSelItem();

        // Set ListView Items, SelItem
        if (isFilterList()) _listView.setItems(items);
        _listView.setSelItem(item);
        if (items.size() <= 1 && isPopup() && isPopupShowing())
            getPopupList().hide();

        // Reset text and selection (since List.setSelItem changes it)
        _textField.setText(text);
        _textField.setSel(selStart);

        // If completion item available, set completion text
        if (item != null) {
            String ctext = getText(item);
            _textField.setCompletionText(ctext);
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
        if (_prefixFunction != null)
            return _prefixFunction.apply(aStr);

        // If no string, return all items
        if (aStr.isEmpty())
            return _items;

        // Otherwise, return items that start with prefix
        List <T> itemsForPrefix = new ArrayList<>();
        for (T item : _items) {
            if (StringUtils.startsWithIC(_listView.getText(item), aStr))
                itemsForPrefix.add(item);
        }

        // Return
        return itemsForPrefix;
    }

    /**
     * Called when TextField fires action.
     */
    protected void handleTextFieldActionEvent(ViewEvent anEvent)
    {
        if (isPopup() && isPopupShowing())
            getPopupList().hide();
        fireActionEvent(anEvent);
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
        if (isShowTextField()) { _textField.setText(str); _textField.selectAll(); }
        else _button.setText(str);
    }

    /**
     * Override to send to text/button.
     */
    public void setAlign(Pos aPos)
    {
        super.setAlign(aPos);
        if (isShowTextField())
            _textField.setAlign(aPos);
        else _button.setAlign(aPos);
    }

    /**
     * Override to send to button.
     */
    public void setDisabled(boolean aValue)
    {
        super.setDisabled(aValue);
        if (_textField != null)
            _textField.setDisabled(aValue);
        _button.setDisabled(aValue);
    }

    /**
     * Returns Value property name.
     */
    public String getValuePropName()
    {
        T item = getSelItem();
        return item != null ? SelItem_Prop : Text_Prop;
    }

    /**
     * Override to return row layout.
     */
    @Override
    protected ViewLayout getViewLayoutImpl()  { return new RowViewLayout(this, true); }

    /**
     * Override to focus text or button.
     */
    public void requestFocus()
    {
        if (isShowTextField())
            getTextField().requestFocus();
        else getButton().requestFocus();
    }

    /**
     * Override to support properties for this class.
     */
    @Override
    protected void initProps(PropSet aPropSet)
    {
        super.initProps(aPropSet);

        // ShowTextField, ShowButton, FilterList
        aPropSet.addPropNamed(ShowTextField_Prop, boolean.class);
        aPropSet.addPropNamed(ShowButton_Prop, boolean.class, true);
        aPropSet.addPropNamed(FilterList_Prop, boolean.class);
    }

    /**
     * Override to support properties for this class.
     */
    @Override
    public Object getPropValue(String aPropName)
    {
        switch (aPropName) {

            // ShowTextField, ShowButton, FilterList
            case ShowTextField_Prop: return isShowTextField();
            case ShowButton_Prop: return isShowButton();
            case FilterList_Prop: return isFilterList();

            // Do normal version
            default: return super.getPropValue(aPropName);
        }
    }

    /**
     * Override to support properties for this class.
     */
    @Override
    public void setPropValue(String aPropName, Object aValue)
    {
        switch (aPropName) {

            // ShowTextField, ShowButton, FilterList
            case ShowTextField_Prop: setShowTextField(Convert.boolValue(aValue)); break;
            case ShowButton_Prop: setShowButton(Convert.boolValue(aValue)); break;
            case FilterList_Prop: setFilterList(Convert.boolValue(aValue)); break;

            // Do normal version
            default: super.setPropValue(aPropName, aValue); break;
        }
    }

    /**
     * XML archival.
     */
    public XMLElement toXMLView(XMLArchiver anArchiver)
    {
        // Archive basic view attributes
        XMLElement e = super.toXMLView(anArchiver);

        // Archive ShowTextField, ShowButton, FilterList
        if (isShowTextField()) e.add(ShowTextField_Prop, true);
        if (isShowButton()!=isPopup()) e.add(ShowButton_Prop, isShowButton());
        if (isFilterList()) e.add(FilterList_Prop, true);

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
        if (anElement.hasAttribute(ShowTextField_Prop))
            setShowTextField(anElement.getAttributeBooleanValue(ShowTextField_Prop));
        if (anElement.hasAttribute(ShowButton_Prop))
            setShowButton(anElement.getAttributeBooleanValue(ShowButton_Prop));
        if (anElement.hasAttribute(FilterList_Prop))
            setFilterList(anElement.getAttributeBooleanValue(FilterList_Prop));
    }

    /**
     * Returns an Icon of a down arrow.
     */
    public static Image getArrowImage()
    {
        if (_arrowImg!=null) return _arrowImg;
        Image img = Image.getImageForSize(9,7,true);
        Painter pntr = img.getPainter();
        Polygon poly = new Polygon(1.5, 1.5, 7.5, 1.5, 4.5, 5.5);
        pntr.setColor(new Color("#FFFFFF99"));
        pntr.drawLine(4.5,8,2,2);
        pntr.drawLine(4.5,8,7,2);
        pntr.setColor(Color.DARKGRAY);
        pntr.draw(poly);
        pntr.fill(poly);
        pntr.flush();
        return _arrowImg = img;
    }
}