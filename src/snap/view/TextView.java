/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.props.PropSet;
import snap.text.TextAdapter;
import snap.util.Convert;

/**
 * This view subclass wraps a TextArea in a ScrollView.
 */
public class TextView extends TextArea {

    // Constants for properties
    public static final String Editable_Prop = TextAdapter.Editable_Prop;

    /**
     * Constructor.
     */
    public TextView()
    {
        this(false);
    }

    /**
     * Constructor with option for RichText.
     */
    public TextView(boolean isRichText)
    {
        super(isRichText);
        setEditable(true);
        setUndoActivated(true);
        setOverflow(Overflow.Scroll);
    }

    /**
     * Returns whether Text shape is editable.
     */
    public boolean isEditable()  { return _textAdapter.isEditable(); }

    /**
     * Sets whether Text shape is editable.
     */
    public void setEditable(boolean aValue)  { _textAdapter.setEditable(aValue); }

    /**
     * Override to set default editable value.
     */
    @Override
    protected void initProps(PropSet aPropSet)
    {
        super.initProps(aPropSet);
        aPropSet.addPropNamed(Editable_Prop, boolean.class, true);
    }

    /**
     * Override to support properties for this class.
     */
    @Override
    public Object getPropValue(String propName)
    {
        if (propName.equals(Editable_Prop))
            return isEditable();
        return super.getPropValue(propName);
    }

    /**
     * Override to support properties for this class.
     */
    @Override
    public void setPropValue(String propName, Object aValue)
    {
        if (propName.equals(Editable_Prop))
            setEditable(Convert.boolValue(aValue));
        else super.setPropValue(propName, aValue);
    }

    /**
     * Override to check for Overflow scroll.
     */
    @Override
    protected void layoutImpl()
    {
        super.layoutImpl();

        if (getOverflow() == View.Overflow.Scroll)
            ViewUtils.checkWantsScrollView(this);
    }

    /**
     * Override to set ScrollView.FillWidth.
     */
    @Override
    protected void setParent(ParentView aPar)
    {
        super.setParent(aPar);
        if (aPar instanceof Scroller && isWrapLines())
            ((Scroller) aPar).setFillWidth(true);
    }
}