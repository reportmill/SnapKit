/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;

/**
 * A ChildView subclass to show overlapping children.
 */
public class StackView extends ChildView {

    /**
     * Constructor.
     */
    public StackView()
    {
        super();
    }

    /**
     * Override to return stack layout.
     */
    @Override
    protected ViewLayout getViewLayoutImpl()  { return new StackViewLayout(this); }
}