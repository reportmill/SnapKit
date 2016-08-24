package snap.viewx;
import java.util.*;
import snap.gfx.*;
import snap.view.*;
import snap.web.WebFile;

/**
 * A ImageChooser component.
 */
public class ImageChooser extends ViewOwner {

    // The files
    List <WebFile>  _files;
    
    // The selected file
    WebFile         _sfile;
    
    // The FilesPane
    TilePane      _filesPane;
    
    // The dialog panel
    DialogBox     _dbox;

/**
 * Creates a new ImageChooser.
 */
public ImageChooser(WebFile ... theFiles)
{
    if(theFiles.length==1 && theFiles[0].isDir())
        _files = theFiles[0].getFiles();
    else _files = Arrays.asList(theFiles);
}

/**
 * Shows panel.
 */
public WebFile showDialogPanel(View aView) { return showDialogPanel(aView, "Select File", "Select File"); }

/**
 * Shows panel.
 */
public WebFile showDialogPanel(View aView, String aTitle, String aMessage)
{
    _dbox = new DialogBox(aTitle); _dbox.setContent(getUI()); _dbox.setMessage(aMessage);
    boolean confirmed = _dbox.showConfirmDialog(aView);
    return confirmed? _sfile : null;
}

/**
 * Returns the Root file.
 */
public List <WebFile> getFiles()  { return _files; }

/**
 * Returns the selected URL.
 */
public WebFile getSelectedFile()  { return _sfile; }

/**
 * CreateUI.
 */
protected View createUI()
{
    // Create TilePane
    _filesPane = new TilePane(); _filesPane.setPrefSize(450, 350);
    _filesPane.setFill(Color.WHITE); _filesPane.setBorder(Border.createLineBorder(Color.BLACK, 1));
    return _filesPane;
}

/**
 * Initialize UI.
 */
protected void initUI()
{
    // Add files for root
    for(WebFile file : getFiles())
        addFilesPaneFile(file);
}

/**
 * Adds a files pane file.
 */
protected void addFilesPaneFile(WebFile aFile)
{
    // Create Graphic
    Image img = aFile.getType().equals("png")? Image.get(aFile) : Image.get(getClass(), "Info.png");
    ImageView iview = new ImageView(img); //iview.setSmooth(true); iview.setCache(true);
    iview.setPrefSize(64,64); //iview.setFitWidth(64); iview.setFitHeight(64); iview.setPreserveRatio(true);
    //StackView sview = new StackView(); sview.setPrefSize(64, 64); sview.addChild(iview);
    
    // Create label
    Label label = new Label(aFile.getSimpleName()); label.setName(aFile.getSimpleName());
    VBox vbox = new VBox(); vbox.setAlign(Pos.TOP_CENTER); vbox.setChildren(iview,label);
    vbox.setPrefSize(100,100); vbox.setProp("File", aFile);
    initUI(vbox);
    enableEvents(vbox, MouseClicked);
    _filesPane.addChild(vbox);
}

/**
 * Respond to UI.
 */
public void respondUI(ViewEvent anEvent)
{
    // Handle Label MouseClicked
    if(anEvent.isMouseClicked()) {
        
        // Handle double-click
        if(anEvent.getClickCount()>1) {
            _dbox.confirm(); return; }
        
        // Update label nodes
        for(View nd : _filesPane.getChildren()) nd.setFill(null);
        anEvent.getView().setFill(Color.LIGHTBLUE);
        
        // Select page
        _sfile = (WebFile)anEvent.getView().getProp("File");
    }
}

/**
 * A class to layout images.
 */
private static class TilePane extends ChildView {
    
    /** Layout children. */
    protected void layoutChildren()
    {
        Insets ins = getInsetsAll();
        double x = ins.left, y = ins.top;
        for(View child : getChildren()) {
            child.setBounds(x,y,100,100); x += 105;
            if(x>getWidth()) { x = ins.left; y += 105; }
        }
    }
}

}