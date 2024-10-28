/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;

/**
 * This view subclass wraps a TextArea in a ScrollView.
 */
public class TextView extends TextArea {

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