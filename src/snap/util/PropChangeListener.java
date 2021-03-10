/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.util;

/**
 * An interface to respond to PropChange objects.
 */
public interface PropChangeListener {

    /** Called when there is a property change. */
    void propertyChange(PropChange aPC);

    /**
     * A PropChangeListener that removes itself when fired.
     */
    public static class OneShot implements PropChangeListener {

        // The real listener
        PropChangeListener  _lsnr;

        /** Creates a OneShot. */
        public OneShot(PropChangeListener aLsnr)  { _lsnr = aLsnr; }

        /** Called when there is a property change. */
        public void propertyChange(PropChange aPC)  { _lsnr.propertyChange(aPC); }
    }

    /**
     * Returns a OneShot.
     */
    public static OneShot getOneShot(PropChangeListener aLsnr)
    {
        return new OneShot(aLsnr);
    }
}