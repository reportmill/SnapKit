/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.geom.Pos;
import snap.props.PropObject;

/**
 * A class to represent a TabView tab.
 */
public class Tab extends PropObject {
    
    // The TabBar
    protected TabBar  _tabBar;

    // The tab button
    private ToggleButton  _button;
    
    // The content
    private View  _content;
    
    /**
     * Creates a new Tab.
     */
    public Tab()
    {
        _button = new ToggleButton();
        _button.getLabel().setPadding(4,7,2,7);
        _button.setAlign(Pos.TOP_CENTER);
        _button.setPosition(Pos.TOP_CENTER);
    }

    /**
     * Returns the title.
     */
    public String getTitle()  { return _button.getText(); }

    /**
     * Sets the title.
     */
    public void setTitle(String aTitle)  { _button.setText(aTitle); }

    /**
     * Returns the button.
     */
    public ToggleButton getButton()  { return _button; }

    /**
     * Returns the content.
     */
    public View getContent()  { return _content; }

    /**
     * Sets the content.
     */
    public void setContent(View aView)  { _content = aView; }

    /**
     * Returns the index of this tab in TabView.
     */
    public int getIndex()
    {
        for (int i = 0; i < _tabBar.getTabCount(); i++)
            if (_tabBar.getTab(i) == this)
                return i;
        return -1;
    }

    /**
     * Returns whether tab is visible.
     */
    public boolean isVisible()  { return _button.isVisible(); }

    /**
     * Sets whether tab is visible.
     */
    public void setVisible(boolean aValue)
    {
        if (aValue == isVisible()) return;

        _button.setVisible(aValue);

        if (!aValue && _tabBar.getSelIndex() == getIndex())
            _tabBar.setSelIndex(getIndex() != 0 ? 0 : 1);
    }

    /**
     * Standard toString implementation.
     */
    public String toString()
    {
        String str = "Tab { ";
        str += "Title:" + getTitle();
        str += ", Index:" + getIndex();
        return str + " }";
    }
}