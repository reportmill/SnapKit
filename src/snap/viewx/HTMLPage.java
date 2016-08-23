package snap.viewx;
import java.io.File;
import javafx.scene.web.*;
import snap.view.View;
import snap.web.*;

/**
 * A WebPage for HTMLFile.
 */
public class HTMLPage extends WebPage {

    // The JavaFX node for viewing a web page
    WebView         _webView;

/**
 * Creates the Scene Root node.
 */
protected View createUI()
{
    WebFile dfile = getFile();
    WebURL durl = dfile.getURL();
    String urls = durl.getString(); // This will only work for HTTP and FILE
    
    // If File data source, get file
    File file = dfile.getStandardFile();
    if(file!=null)
        urls = "file:" + file.getAbsolutePath();
    
    _webView = new WebView();
    _webView.getEngine().load(urls);
    return getView(_webView);
}

}