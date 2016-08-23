package snap.view;

/**
 * A custom class.
 */
public abstract class FileChooser extends Object {
    
    // Whether choosing file for save
    boolean         _saving;
    
    // The extensions
    String          _exts[];
    
    // The description
    String          _desc;

/**
 * Shows the panel.
 */
public abstract String showOpenPanel(View aView);

/**
 * Shows the panel.
 */
public abstract String showSavePanel(View aView);

/**
 * Returns whether is saving.
 */
public boolean isSaving()  { return _saving; }

/**
 * Sets whether is saving.
 */
public void setSaving(boolean aValue)  { _saving = aValue; }

/**
 * Returns the extensions.
 */
public String[] getExts()  { return _exts; }

/**
 * Sets the extensions.
 */
public void setExts(String ... theExts)  { _exts = theExts; }

/**
 * Returns the description.
 */
public String getDesc()  { return _desc; }

/**
 * Sets the descrption.
 */
public void setDesc(String aValue)  { _desc = aValue; }

}