package snap.util;
import java.util.*;
import snap.view.ViewUtils;

/**
 * An interface to identify classes that have external resources that may not be immediately available.
 */
public interface Loadable {

    /**
     * Returns whether resource is loaded.
     */
    public boolean isLoaded();

    /**
     * Adds a callback to be triggered when resources loaded (cleared automatically when loaded).
     */
    public void addLoadListener(Runnable aRun);
    
/**
 * Returns a combined loadable for given array of loadables.
 */
public static Loadable getAsLoadable(Loadable theLoadables[])  { return new MultiLoadable(theLoadables); }
    
/**
 * Returns a combined loadable for given array of loadables.
 */
public static Loadable getAsLoadable(List <? extends Loadable> theLoadables)
{
    return new MultiLoadable(theLoadables.toArray(new Loadable[theLoadables.size()]));
}
    
/**
 * A class to load a list of loadables.
 */
public static class MultiLoadable implements Loadable {
    
    // The array of loadables
    Loadable          _loadables[];
    
    // Whether loadables are all loaded
    boolean           _loaded;

    // Array of listeners
    Runnable          _loadLsnrs[] = EMPTY_RUNNABLES;
    
    // Constants
    private static Runnable EMPTY_RUNNABLES[] = new Runnable[0];

    /** Creates a MultiLoadable. */
    public MultiLoadable(Loadable[] theLoadables)
    {
        _loadables = theLoadables;
        checkLoaded();
    }
    
    /** Checks whether loadables are all loaded. */
    void checkLoaded()
    {
        // Get last unloaded loadable in list - if not found, setLoaded and return
        Loadable loadable = getUnloadedLoadable();
        if(loadable==null) {
            setLoaded(true); return; }
        
        // Add load listener for next unloaded loadable
        loadable.addLoadListener(() -> ViewUtils.runLater(() -> checkLoaded()));
    }
    
    /** Returns last unloaded Loadable in list. */
    Loadable getUnloadedLoadable()
    {
        for(int i=_loadables.length-1; i>=0; i--) { Loadable loadable = _loadables[i];
            if(!loadable.isLoaded())
                return loadable; }
        return null;
    }
    
    /** Returns whether puppet is loaded. */
    public boolean isLoaded()  { return _loaded; }
    
    /** Sets whether loadable is loaded. */
    protected void setLoaded(boolean aValue)
    {
        if(aValue==_loaded) return;
        _loaded = aValue;
        if(aValue) {
            for(Runnable lsnr : _loadLsnrs)
                lsnr.run();
            _loadLsnrs = EMPTY_RUNNABLES;
        }
    }
    
    /** Adds a load listener (cleared automatically when loader is loaded). */
    public void addLoadListener(Runnable aLoadLsnr)
    {
        if(isLoaded()) { aLoadLsnr.run(); return; }
        _loadLsnrs = Arrays.copyOf(_loadLsnrs, _loadLsnrs.length+1);
        _loadLsnrs[_loadLsnrs.length-1] = aLoadLsnr;
    }
}

}