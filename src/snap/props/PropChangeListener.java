/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.props;

/**
 * An interface to respond to PropChange objects.
 */
public interface PropChangeListener {

    /** Called when there is a property change. */
    void handlePropChange(PropChange propChange);

    /**
     * A PropChangeListener that removes itself when fired.
     */
    class OneShot implements PropChangeListener {

        // The real listener
        private PropChangeListener _lsnr;

        /** Creates a OneShot. */
        public OneShot(PropChangeListener aLsnr)  { _lsnr = aLsnr; }

        /** Called when there is a property change. */
        public void handlePropChange(PropChange propChange)  { _lsnr.handlePropChange(propChange); }
    }

    /**
     * Returns a OneShot.
     */
    static OneShot getOneShot(PropChangeListener aLsnr)
    {
        return new OneShot(aLsnr);
    }
}