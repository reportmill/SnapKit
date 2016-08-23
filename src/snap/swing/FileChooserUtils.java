package snap.swing;
import java.awt.Component;
import java.awt.event.*;
import java.io.File;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import snap.util.*;
import snap.view.*;

/**
 * FileChooser utilities.
 */
public class FileChooserUtils {

/**
 * Runs a file chooser that remembers last open file and size.
 */
public static String showChooser(boolean save, View aView, String aDesc, String ... theExtensions)
{
    // Get component
    RootView rpane = aView!=null? aView.getRootView() : null;
    JComponent aComp = rpane!=null? rpane.getNative(JComponent.class) : null;
    
    // Declare local variable for whether this is an open
    boolean open = !save;
    
    // Declare local variable for chooser
    JFileChooser chooser = null;
    
    // Try to create new chooser multiple times to work around Windows JVM bugs with javax.swing.ImageIcon.<init> NPE
    for(int i=0; i<20 && chooser==null; i++)
        try { chooser = new SnapFileChooser(); }
        catch(Throwable t) { }
    
    // Add file filter to chooser
    chooser.setFileFilter(new UIUtilsFileFilter(theExtensions, aDesc));
    
    // Get last chosen file name from preferences for first given extension
    String path = PrefsUtils.prefs().get("MostRecentDocument" + theExtensions[0], System.getProperty("user.home"));
    
    // Get last chosen file as File
    File file = new File(path);
    
    // Initialize chooser to last chosen directory and/or file
    if(file.isDirectory())
        chooser.setCurrentDirectory(file);
    else chooser.setSelectedFile(file);

    // Run chooser (use showDialog instead of showOpenDialog, because that version has no textfield)
    int option = save? chooser.showSaveDialog(aComp) : chooser.showDialog(aComp, "Open");
    
    // If user hit cancel, just return
    if(option!=JFileChooser.APPROVE_OPTION)
        return null;

    // Get file and path of selection and save to preferences
    file = chooser.getSelectedFile();
    path = file.getAbsolutePath();
    
    // If "~", replace with user.home
    if(path.indexOf("~")>=0)
        file = new File(path = System.getProperty("user.home") + File.separator + path.substring(path.indexOf('~')+1));

    // Get path extension
    String ext = "." + StringUtils.getPathExtension(path);
    if(ext.equals("."))
        ext = theExtensions[0];
    
    // Save selected filename in preferences for it's type (extension)
    PrefsUtils.prefsPut("MostRecentDocument" + ext, path, true);
            
    // If user chose a directory, just run again
    if(file.isDirectory())
        return showChooser(save, aView, aDesc, theExtensions);
    
    // If opening a file that doesn't exists, see if it just needs an extension
    if(open && !file.exists()) {
        
        // If path doesn't contain an extension, add the first extension
        if(path.indexOf(".") < 0)
            file = new File(path += theExtensions[0]);

        // If file doesn't exist, run chooser again
        if(!file.exists())
            return showChooser(save, aView, aDesc, theExtensions);
    }
    
    // The open case can return file with invalid ext since we really run showDialog, so make sure path is OK
    if(open && !StringUtils.containsIC(theExtensions, "." + StringUtils.getPathExtension(path)))
        return null;

    // If saving, make sure path has extension
    if(save && path.indexOf(".") < 0)
        file = new File(path += theExtensions[0]);

    // If user is trying to save over an existing file, warn them
    if(save && file.exists()) {
        
        // Run option panel for whether to overwrite
        DialogBox dbox = new DialogBox("Replace File");
        dbox.setWarningMessage("The file " + path + " already exists. Replace it?");
        dbox.setOptions("Replace", "Cancel");
        int answer = dbox.showOptionDialog(aView, "Replace");
        
        // If user chooses cancel, re-run chooser
        if(answer==1)
            return showChooser(save, aView, aDesc, theExtensions);
    }
        
    // Give focus back to original component
    if(save && SwingUtils.getWindow(aComp)!=null)
        SwingUtils.getWindow(aComp).requestFocus();

    // Return path
    return path;
}

/**
 * A JFileChooser subclass that gets (and saves) its size to preferences.
 */
private static class SnapFileChooser extends JFileChooser {

    /** Overrides JFileChooser method to set dialog size. */
    protected JDialog createDialog(Component parent)
    {
        // Get normal dialog
        JDialog dialog = super.createDialog(parent);
        
        // Get previous window size string from preferences
        String size = PrefsUtils.prefs().get("FileChooserSize", " "); // Used to be: "FileChooserSize" + exts[0]
        
        // Get previous window width and height from size string
        final int width = StringUtils.intValue(size);
        final int height = (int)StringUtils.doubleValue(size, size.indexOf(" "));
        
        // Set dialog size
        dialog.setSize(Math.max(width, dialog.getWidth()), Math.max(height, dialog.getHeight()));
        
        // Add component listener to catch window resize
        dialog.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) { Component c = e.getComponent();
                PrefsUtils.prefsPut("FileChooserSize", c.getWidth() + " " + c.getHeight()); }});
        
        // Return dialog
        return dialog;
    }
}

/**
 * A private FileFiter implementation to handle multiple extensions.
 */
private static class UIUtilsFileFilter extends FileFilter {

    // Extensions to filter, description for filter
    String _exts[]; String _desc;
    
    /** Creates a new UIUtilsFileFilter. */
    public UIUtilsFileFilter(String exts[], String aDesc)  { _exts = exts; _desc = aDesc; }
    
    /** Returns whether file filter accepts file. */
    public boolean accept(File aFile)
    {
        // Accept all directories
        if(aFile.isDirectory())
            return true;
        
        // Get lowercase filename
        String fileName = aFile.getName().toLowerCase();
        
        // Iterate over extensions and return true if filename ends with any of them
        for(int i=0; i<_exts.length; i++)
            if(fileName.endsWith(_exts[i]))
                return true;
        
        // Return false if extension not found 
        return false;
    }
    
    /** Returns description. */
    public String getDescription() { return _desc; }
}
    
}