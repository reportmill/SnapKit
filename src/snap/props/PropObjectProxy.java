/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.props;

/**
 * This class is meant to stand in for archival objects that aren't PropObject.
 */
public abstract class PropObjectProxy extends PropObject {

    /**
     * Returns the real object.
     */
    public abstract Object getReal();
}
