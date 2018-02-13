/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import java.util.*;
import snap.gfx.*;
import snap.util.*;

/**
 * A View subclass to show multiple children under user selectable tabs.
 */
public class TabView extends ParentView implements View.Selectable <Tab> {
    
    // The tabs
    List <Tab>      _tabs = new ArrayList();
    
    // The tab min size
    double          _tabMinWidth;
    
    // The selected tab index
    int             _sindex = -1;
    
    // The tab shelf
    RowView         _shelf;
    
    // The view to hold content
    BoxView         _contentBox;
    
    // The node to obscure the content cradle border below the selected tab button
    View            _borderBlockerBox = new RectView();
    
    // Hidden kids
    RowView         _hiddenKids = new RowView();
    
    // The default back fill
    static Paint    _backFill = ViewUtils.getBackFill();
    
    // Constants for properties
    public static final String SelectedIndex_Prop = "SelectedIndex";

/**
 * Creates a new TabView.
 */
public TabView()
{
    // Create and configure shelf
    _shelf = new RowView(); _shelf.setFillHeight(true); _shelf.setHeight(32); _shelf.setPrefHeight(32);
    _shelf.setSpacing(1); _shelf.setPadding(5,5,0,5);
    Color c1 = new Color("#d6d6d6"), c2 = new Color("#dddddd");
    _shelf.setFill(new GradientPaint(.5,0,.5,1, GradientPaint.getStops(0,c1,.2,c2,1,c2)));
    
    // Create and configure content cradle
    _contentBox = new BoxView(null, true, true); _contentBox.setFill(_backFill);
    _contentBox.setBorder(Color.LIGHTGRAY, 1);
    
    // Create and configure box node
    _borderBlockerBox.setHeight(1); _borderBlockerBox.setFill(_backFill);
    _hiddenKids.setVisible(false);
    
    // Add shelf and content cradle, enable action event
    setChildren(_hiddenKids, _shelf, _contentBox, _borderBlockerBox);
    enableEvents(Action);
}

/**
 * Returns the number of tabs in pane.
 */
public int getTabCount()  { return _tabs.size(); }

/**
 * Returns the tab at given index.
 */
public Tab getTab(int anIndex)  { return _tabs.get(anIndex); }

/**
 * Adds the given tab shape to tabbed pane shape.
 */
public void addTab(String aTitle, View aView)  { addTab(aTitle, aView, getTabCount()); }

/**
 * Adds the given tab shape to tabbed pane shape.
 */
public void addTab(String aTitle, View aView, int anIndex)
{
    Tab tab = new Tab(); tab.setTitle(aTitle); tab.setContent(aView);
    addTab(tab, anIndex);
}

/**
 * Adds a tab.
 */
public void addTab(Tab aTab)  { addTab(aTab, getTabCount()); }

/**
 * Adds a tab at given index.
 */
public void addTab(Tab aTab, int anIndex)
{
    // Add Tab to Tabs list
    _tabs.add(anIndex, aTab);
    
    // Create ToggleButton for tab and configure
    ToggleButton btn = new ToggleButton(); btn.setText(aTab.getTitle()); btn.getLabel().setPadding(4,7,4,7);
    btn.setMinWidth(getTabMinWidth()); btn.setAlign(Pos.TOP_CENTER); btn.setPosition(Pos.TOP_CENTER);
    
    // Add ToggleButton to Shelf
    _shelf.addChild(btn, anIndex); btn.addEventHandler(e -> shelfButtonPressed(e), Action);
    
    // Add Tab.Content to HiddenKids
    if(aTab.getContent()!=null) _hiddenKids.addChild(aTab.getContent(),0);
    
    // If first tab, select 0
    int selInd = getSelectedIndex();
    if(anIndex<=selInd || selInd<0) {
        setSelectedIndex(-1); setSelectedIndex(selInd+1); }
}

/**
 * Removes the tab at given index.
 */
public void removeTab(int anIndex)
{
    // Remove Tab content and button
    Tab tab = getTab(anIndex);
    ToggleButton btn = getTabButton(anIndex);
    _tabs.remove(anIndex);
    _hiddenKids.removeChild(tab.getContent());
    _shelf.removeChild(btn);
    
    // Reset Selection
    if(anIndex==getSelectedIndex()) {
        setSelectedIndex(anIndex-1); if(anIndex<getTabCount()) setSelectedIndex(anIndex); }
}

/**
 * Returns tab content at index.
 */
public String getTabTitle(int anIndex)  { return getTab(anIndex).getTitle(); }

/**
 * Sets tab content at index.
 */
public void setTabTitle(String aTitle, int anIndex)  { getTab(anIndex).setTitle(aTitle); }

/**
 * Returns tab content at index.
 */
public View getTabContent(int anIndex)  { return getTab(anIndex).getContent(); }

/**
 * Sets tab content at index.
 */
public void setTabContent(View aView, int anIndex)
{
    Tab tab = getTab(anIndex);
    if(tab.getContent()!=null) _hiddenKids.removeChild(tab.getContent());
    tab.setContent(aView);
    if(tab.getContent()!=null) _hiddenKids.addChild(tab.getContent(),0);
    if(anIndex==getSelectedIndex())
        setContent(tab.getContent());
}

/**
 * Returns the tab min width.
 */
public double getTabMinWidth()  { return _tabMinWidth; }

/**
 * Sets the tab min width.
 */
public void setTabMinWidth(double aValue)  { _tabMinWidth = aValue; }

/**
 * Returns the tap pane's selected index.
 */
public int getSelectedIndex()  { return _sindex; }

/**
 * Sets the TabView's selected index.
 */
public void setSelectedIndex(int anIndex)
{
    // If already set, just return
    if(anIndex==_sindex) return;
    
    // Get tab at index
    Tab tab = anIndex>=0 && anIndex<getTabCount()? getTab(anIndex) : null;
    
    // Update old/new ToggleButtons
    ToggleButton ob = getTabButton(_sindex); if(ob!=null) { ob.setSelected(false); ob.setButtonFill(null); }
    ToggleButton nb = getTabButton(anIndex); if(nb!=null) { nb.setSelected(true); nb.setButtonFill(_backFill); }
    
    // Set content to tab.Content
    setContent(tab!=null? tab.getContent() : null);
    
    // FirePropChange and fireActionEvent
    firePropChange(SelectedIndex_Prop, _sindex, _sindex=anIndex);
    fireActionEvent();
}

/**
 * Returns the selected item (tab).
 */
public Tab getSelectedItem()  { return _sindex>=0 && _sindex<getTabCount()? getTab(_sindex) : null; }

/**
 * Sets the selected item (tab).
 */
public void setSelectedItem(Tab aTab)
{
    int index = _tabs.indexOf(aTab);
    setSelectedIndex(index);
}

/**
 * Returns the shelf.
 */
public View getShelf()  { return _shelf; }

/**
 * Returns a tab button.
 */
private ToggleButton getTabButton(int anIndex)
{
    return anIndex>=0 && anIndex<getTabCount()? (ToggleButton)_shelf.getChild(anIndex) : null;
}

/**
 * Returns the selected child.
 */
public View getContent()  { return _contentBox.getContent(); }

/**
 * Sets the current tab content.
 */
protected void setContent(View aView)
{
    View old = getContent();
    _contentBox.setContent(aView);
    
    // If old is a tab content, add back to hidden kids
    boolean isKid = false; for(Tab tab : _tabs) if(tab.getContent()==old) isKid = true;
    if(old!=null && isKid) _hiddenKids.addChild(old,0);
    relayout();
}

/**
 * Returns the preferred width.
 */
protected double getPrefWidthImpl(double aH)
{
    View shelf = getShelf(); View content = getContent();
    double cw = content!=null? content.getBestWidth(-1) : 0;
    double shw = shelf.getPrefWidth();
    return Math.max(shw,cw);
}

/**
 * Returns the preferred height.
 */
protected double getPrefHeightImpl(double aW)
{
    View shelf = getShelf(); View content = getContent();
    double ch = content!=null? content.getBestHeight(-1) : 0;
    return 30 + ch;
}

/**
 * Override to layout children with VBox layout.
 */
protected void layoutImpl()
{
    // Get insets and inner bounds
    Insets ins = getInsetsAll();
    double x = ins.left, y = ins.top+28, w = getWidth() - x - ins.right, h = getHeight() - y - ins.bottom;
    
    // Resize Shelf and ContentBox
    _shelf.setWidth(w);
    _shelf.layout();
    _contentBox.setBounds(x,y,w,h);
    
    // Reset BorderBlockerBox to obscure border under selected tab
    ToggleButton tbtn = getTabButton(_sindex);
    if(tbtn!=null) _borderBlockerBox.setBounds(tbtn.getX()+1,_contentBox.getY(),tbtn.getWidth()-2,1);
    else _borderBlockerBox.setBounds(0,0,0,0);
}

/**
 * Called when shelf button pressed.
 */
protected void shelfButtonPressed(ViewEvent anEvent)
{
    View btn = anEvent.getView();
    int index = -1; for(int i=0;i<_shelf.getChildCount();i++) if(_shelf.getChild(i)==btn) index = i;
    setSelectedIndex(index);
}

/**
 * XML archival.
 */
public XMLElement toXMLView(XMLArchiver anArchiver)
{
    // Archive basic view attributes
    XMLElement e = super.toXMLView(anArchiver);
    if(getSelectedIndex()>0) e.add(SelectedIndex_Prop, getSelectedIndex());
    return e;
}

/**
 * XML archival deep.
 */
public void toXMLChildren(XMLArchiver anArchiver, XMLElement anElement)
{
    // Archive children
    for(int i=0, iMax=getTabCount(); i<iMax; i++) { View child = getTabContent(i); String title = getTabTitle(i);
        XMLElement cxml = anArchiver.toXML(child, this); cxml.add("title", title);
        anElement.add(cxml);
    }    
}

/**
 * XML unarchival for children. Only panels do anything here so far.
 */
public void fromXMLChildren(XMLArchiver anArchiver, XMLElement anElement)
{
    // Iterate over child elements and unarchive shapes
    for(int i=0, iMax=anElement.size(); i<iMax; i++) { XMLElement cxml = anElement.get(i);
        
        // Get child class - if View, unarchive and add
        Class childClass = anArchiver.getClass(cxml.getName());
        if(childClass!=null && View.class.isAssignableFrom(childClass)) {
            View shape = (View)anArchiver.fromXML(cxml, this);
            String title = cxml.getAttributeValue("title"); if(title==null) title = "";
            addTab(title, shape);
        }
    }
    
    // Unarchive SelectedIndex (after children unarchival - otherwise it may be out of bounds)
    if(anElement.hasAttribute(SelectedIndex_Prop))
        setSelectedIndex(anElement.getAttributeIntValue(SelectedIndex_Prop));
}

}