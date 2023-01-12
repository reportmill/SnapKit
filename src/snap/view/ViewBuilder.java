/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.gfx.Font;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * A class to facilitate building views quickly.
 */
public class ViewBuilder<T extends View> {

    // The default class
    private Class<T>  _defaultClass;

    // Ivars
    private Class<? extends View>  _class;
    private String  _name;
    private String  _text;
    private Font  _font;
    private String  _toolTip;

    // Array of Views for save
    private List<T> _viewsList;

    /**
     * Constructor.
     */
    public ViewBuilder(Class<T> aClass)
    {
        super();
        _defaultClass = aClass;
    }

    /**
     * Set class.
     */
    public ViewBuilder<T> cls(Class<? extends View> aClass)
    {
        _class = aClass;
        return this;
    }

    /**
     * Set name.
     */
    public ViewBuilder<T> name(String aName)
    {
        _name = aName;
        return this;
    }

    /**
     * Set text.
     */
    public ViewBuilder<T> text(String aString)
    {
        _text = aString;
        return this;
    }

    /**
     * Set font.
     */
    public ViewBuilder<T> font(Font aFont)
    {
        _font = aFont;
        return this;
    }

    /**
     * Set font.
     */
    public ViewBuilder<T> toolTip(String aString)
    {
        _toolTip = aString;
        return this;
    }

    /**
     * Resets ViewBuilder.
     */
    public void reset()
    {
        _class = null;
        _name = null;
        _text = null;
        _font = null;
    }

    /**
     * Builds and save current view.
     */
    public T save()
    {
        if (_viewsList == null)
            _viewsList = new ArrayList<>();
        T view = build();
        _viewsList.add(view);
        return view;
    }

    /**
     * Builds the current view.
     */
    public T build()
    {
        // Create View
        T view;
        Class<? extends View>  cls = _class != null ? _class : _defaultClass;
        try { view = (T) cls.newInstance(); }
        catch (Exception e) { throw new RuntimeException(e); }

        // Configure props
        if (_name != null)
            view.setName(_name);
        if (_text != null)
            view.setText(_text);
        if (_font != null)
            view.setFont(_font);
        if (_toolTip != null)
            view.setToolTip(_toolTip);

        // Reset and return
        reset();
        return view;
    }

    /**
     * Returns all views.
     */
    public T[] buildAll()
    {
        T[] array = (T[]) Array.newInstance(_defaultClass, _viewsList.size());
        return _viewsList.toArray(array);
    }

    /**
     * Returns menu with all menu items.
     */
    public Menu buildMenu(String aName, String aText)
    {
        Menu menu = new Menu();
        menu.setName(aName);
        menu.setText(aText);
        T[] menuItems = buildAll();
        for (T menuItem : menuItems)
            menu.addItem((MenuItem) menuItem);
        return menu;
    }
}
