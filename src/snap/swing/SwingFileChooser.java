package snap.swing;
import snap.view.*;

/**
 * A custom class.
 */
public class SwingFileChooser extends FileChooser {


/**
 * Shows the panel.
 */
public String showOpenPanel(View aView)
{
    return FileChooserUtils.showChooser(false, aView, getDesc(), getExts());
}

/**
 * Shows the panel.
 */
public String showSavePanel(View aView)
{
    return FileChooserUtils.showChooser(true, aView, getDesc(), getExts());
}

}