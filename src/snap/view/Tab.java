/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.geom.Polygon;
import snap.geom.Pos;
import snap.gfx.Border;
import snap.gfx.Color;
import snap.props.PropObject;
import snap.util.ListUtils;
import java.util.List;
import java.util.Objects;

/**
 * A class to represent a TabView tab.
 */
public class Tab extends PropObject {
    
    // The TabBar
    protected TabBar  _tabBar;

    // The tab title
    private String  _title;

    // Whether tab should have close box
    private boolean  _closable;

    // Whether tab is visible
    private boolean  _visible = true;

    // The tab button
    private ToggleButton  _button;

    // The content
    private View  _content;

    // The content owner
    private ViewOwner  _contentOwner;

    // Constants for properties
    private static final String Title_Prop = "Title";
    private static final String Closable_Prop = "Closable";
    private static final String Visible_Prop = "Visible";

    // Constants
    private static Border TAB_CLOSE_BORDER1 = Border.createLineBorder(Color.BLACK, .5);
    private static Border TAB_CLOSE_BORDER2 = Border.createLineBorder(Color.BLACK, 1);

    /**
     * Creates a new Tab.
     */
    public Tab()
    {
        super();
    }

    /**
     * Returns the title.
     */
    public String getTitle()  { return _title; }

    /**
     * Sets the title.
     */
    public void setTitle(String aTitle)
    {
        if (Objects.equals(aTitle, _title)) return;
        firePropChange(Title_Prop, _title, _title = aTitle);

        if (_button != null)
            _button.setText(aTitle);
    }

    /**
     * Returns whether tab should show close box.
     */
    public boolean isClosable()  { return _closable; }

    /**
     * Sets whether tab should show close box.
     */
    public void setClosable(boolean aValue)
    {
        if (aValue == isClosable()) return;
        firePropChange(Closable_Prop, _closable, _closable = aValue);

        if (_button != null) {
            if (aValue)
                addCloseBoxToButton(_button);
            else _button.setGraphicAfter(null);
        }
    }

    /**
     * Returns the button.
     */
    public ToggleButton getButton()
    {
        if (_button != null) return _button;
        ToggleButton button = createButton();
        return _button = button;
    }

    /**
     * Creates the button.
     */
    protected ToggleButton createButton()
    {
        // Create button
        ToggleButton button = new ToggleButton(getTitle());
        button.setVisible(isVisible());
        if (isClosable())
            addCloseBoxToButton(button);

        // Set padding
        button.setPadding(3,7,3,7);

        // If button is for TabView, configure as Tabs
        View tabBarParent = _tabBar != null ? _tabBar.getParent() : null;
        boolean isForClassicTabView = tabBarParent instanceof TabView && ((TabView) tabBarParent).isClassic();
        if (isForClassicTabView) {
            button.setPadding(4,7,2,7);
            button.setAlign(Pos.TOP_CENTER);
            button.setPosition(Pos.TOP_CENTER);
        }

        // Return
        return button;
    }

    /**
     * Returns the content.
     */
    public View getContent()
    {
        // If already set, just return
        if (_content != null) return _content;

        // Create, set, return
        View content = _contentOwner != null ? _contentOwner.getUI() : null;
        return _content = content;
    }

    /**
     * Sets the content.
     */
    public void setContent(View aView)  { _content = aView; }

    /**
     * Returns the content owner.
     */
    public ViewOwner getContentOwner()  { return _contentOwner; }

    /**
     * Sets the content owner.
     */
    public void setContentOwner(ViewOwner aViewOwner)  { _contentOwner = aViewOwner; }

    /**
     * Returns the index of this tab in TabView.
     */
    public int getIndex()
    {
        List<Tab> tabs = _tabBar.getTabs();
        return ListUtils.indexOfId(tabs, this);
    }

    /**
     * Returns whether tab is visible.
     */
    public boolean isVisible()  { return _visible; }

    /**
     * Sets whether tab is visible.
     */
    public void setVisible(boolean aValue)
    {
        // If already set, just return
        if (aValue == isVisible()) return;

        // Set value, fire prop change
        firePropChange(Visible_Prop, _visible, _visible = aValue);

        // Forward to Button
        if (_button != null)
            _button.setVisible(aValue);

        // If this Tab is selected, select next
        if (!aValue && _tabBar.getSelIndex() == getIndex())
            _tabBar.setSelIndex(getIndex() != 0 ? 0 : 1);
    }

    /**
     * Adds close box to button.
     */
    protected void addCloseBoxToButton(ToggleButton tabButton)
    {
        // Create close box polygon
        Polygon poly = new Polygon(0, 2, 2, 0, 5, 3, 8, 0, 10, 2, 7, 5, 10, 8, 8, 10, 5, 7, 2, 10, 0, 8, 3, 5);

        // Create close box ShapeView
        ShapeView closeBox = new ShapeView(poly);
        closeBox.setBorder(TAB_CLOSE_BORDER1);
        closeBox.setPrefSize(11, 11);
        closeBox.addEventFilter(e -> handleTabCloseBoxEvent(e), View.MouseEnter, View.MouseExit, View.MouseRelease);

        // Add to FileTab
        tabButton.setGraphicAfter(closeBox);
    }

    /**
     * Called for events on tab close button.
     */
    private void handleTabCloseBoxEvent(ViewEvent anEvent)
    {
        View closeBox = anEvent.getView();

        // Handle MouseEnter
        if (anEvent.isMouseEnter()) {
            closeBox.setFill(Color.CRIMSON);
            closeBox.setBorder(TAB_CLOSE_BORDER2);
        }

        // Handle MouseExit
        else if (anEvent.isMouseExit()) {
            closeBox.setFill(null);
            closeBox.setBorder(TAB_CLOSE_BORDER1);
        }

        // Handle MouseRemove
        else if (anEvent.isMouseRelease())
            _tabBar.removeTab(this);

        anEvent.consume();
    }

    /**
     * Standard toString implementation.
     */
    @Override
    public String toStringProps()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(Title_Prop).append(':').append(getTitle());
        if (isClosable())
            sb.append(", ").append(Closable_Prop).append(':').append(isClosable());
        sb.append(", Index").append(':').append(getIndex());
        return sb.toString();
    }

    /**
     * A builder class.
     */
    public static class Builder {

        // Ivars
        private Tab  _tab;
        private TabBar  _tabBar;

        /**
         * Constructor.
         */
        public Builder()
        {
            super();
        }

        /**
         * Constructor.
         */
        public Builder(TabBar tabBar)
        {
            super();
            _tabBar = tabBar;
        }

        /**
         * Return tab.
         */
        private Tab _tab()
        {
            if (_tab != null) return _tab;
            return _tab = new Tab();
        }

        // Properties
        public Builder title(String aTitle)  { _tab()._title = aTitle; return this; }
        public Builder closable(boolean aValue)  { _tab()._closable = aValue; return this; }
        public Builder visible(boolean aValue)  { _tab()._visible = aValue; return this; }
        public Builder content(View aValue)  { _tab()._content = aValue; return this; }
        public Builder contentOwner(ViewOwner aValue)  { _tab()._contentOwner = aValue; return this; }

        /**
         * Build.
         */
        public Tab build()
        {
            Tab tab = _tab();
            _tab = null;
            return tab;
        }

        /**
         * Add.
         */
        public Tab add()
        {
            Tab tab = build();
            _tabBar.addTab(tab);
            return tab;
        }
    }
}