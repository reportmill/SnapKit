/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.gfx.*;
import snap.util.ArrayUtils;

/**
 * A class to run a dialog box.
 */
public class DialogBox extends FormBuilder {

    // The main message to display
    String           _message;
    
    // The title of the dialog box
    String           _title;
    
    // The type of box
    Type             _type = Type.Message;
    
    // The type of message
    MessageType      _messageType = MessageType.Plain;
    
    // The options for buttons
    String           _options[];
    
    // The image for dialog box
    Image            _image;
    
    // The content for dialog box
    View             _content;

    // The FormBuilder
    FormBuilder      _builder = this;
    
    // Whether stage was cancelled
    boolean          _cancelled;

    // Index of selected option
    int              _index = DialogBox.CANCEL_OPTION;

    // Constants for DialogBox type
    public enum Type { Message, Confirm, Option, Input }
    
    // Constants for tone of dialog box
    public enum MessageType { Plain, Question, Information, Warning, Error }
    
    // Standard Options
    public static final String[] OPTIONS_OK = { "OK" };
    public static final String[] OPTIONS_OK_CANCEL = { "OK", "Cancel" };
    public static final String[] OPTIONS_YES_NO_CANCEL = { "Yes", "No", "Cancel" };
    
    // Return values
    public static final int OK_OPTION = 0; /** Return value form class method if OK is chosen. */
    public static final int YES_OPTION = 0; /** Return value form class method if YES is chosen. */
    public static final int NO_OPTION = 1;  /** Return value from class method if NO is chosen. */
    public static final int CANCEL_OPTION = 2;   /** Return value from class method if CANCEL is chosen. */
    
    // Get InfoImage
    public static Image questImage = Image.get(DialogBox.class, "Question.png");
    public static Image infoImage = Image.get(DialogBox.class, "Information.png");
    public static Image warnImage = Image.get(DialogBox.class, "Warning.png");
    public static Image errorImage = Image.get(DialogBox.class, "Error.png");

/**
 * Creates a new JFXDialogBox.
 */
public DialogBox()  { }

/**
 * Creates a new SwingDialogBox with given title.
 */
public DialogBox(String aTitle)  { setTitle(aTitle); }

/**
 * Returns the message to display.
 */
public String getMessage()  { return _message; }

/**
 * Sets the message to display.
 */
public void setMessage(String aMessage)  { _message = aMessage; }

/**
 * Sets the message to display.
 */
public void setErrorMessage(String aMessage)  { setMessage(aMessage); setMessageType(MessageType.Error); }

/**
 * Sets the message to display.
 */
public void setWarningMessage(String aMessage)  { setMessage(aMessage); setMessageType(MessageType.Warning); }

/**
 * Sets the message to display.
 */
public void setQuestionMessage(String aMessage)  { setMessage(aMessage); setMessageType(MessageType.Question); }

/**
 * Returns the title of the dialog box.
 */
public String getTitle()  { return _title; }

/**
 * Sets the title of the dialog box.
 */
public void setTitle(String aTitle)  { _title = aTitle; }

/**
 * Returns the type of the box.
 */
public Type getType()  { return _type; }

/**
 * Sets the type of the box.
 */
public void setType(Type aType)  { _type = aType; }

/**
 * Returns the message type of the box.
 */
public MessageType getMessageType()  { return _messageType; }

/**
 * Sets the message type of the box.
 */
public void setMessageType(MessageType aMessageType)  { _messageType = aMessageType; }

/**
 * Returns the options strings.
 */
public String[] getOptions()
{
    if(_options!=null) return _options;
    if(getType()==Type.Message) return OPTIONS_OK;
    return OPTIONS_OK_CANCEL;
}

/**
 * Sets the option strings.
 */
public void setOptions(String ... theOptions)  { _options = theOptions; }

/**
 * Returns the image.
 */
public Image getImage()  { return _image!=null? _image : getImageDefault(); }

/**
 * Sets the Image.
 */
public void setImage(Image anImage)  { _image = anImage; }

/**
 * Returns the image.
 */
public Image getImageDefault()
{
    // If there is actual Content UI, there is no default image
    if(getContent()!=null) return null;
    
    // Return standard image for message types
    switch(getMessageType()) {
        case Question: return questImage;
        case Information: return infoImage;
        case Warning: return warnImage;
        case Error: return errorImage;
        default: return infoImage;
    }
}

/**
 * Returns the content for dialog box.
 */
public View getContent()  { return _content; }

/**
 * Sets the content for dialog box.
 */
public void setContent(View aView)  { _content = aView; }

/**
 * Runs the panel.
 */
public void showMessageDialog(View aView)
{
    // Set type
    setType(Type.Message);
    
    // Configure
    if(getMessage()!=null) _builder.addLabel(getMessage());  // Add Message label
    if(getContent()!=null) _builder.addView(getContent());  // Add custom content
    
    // Show dialog
    showPanel(aView);
}

/**
 * Show an option dialog.
 */
public boolean showConfirmDialog(View aView)
{
    // Set type
    setType(Type.Confirm);
    
    // Configure
    if(getMessage()!=null) _builder.addLabel(getMessage());  // Add Message label
    if(getContent()!=null) _builder.addView(getContent());  // Add custom content
    
    // Show dialog
    return showPanel(aView);
}

/**
 * Show an option dialog.
 */
public int showOptionDialog(View aView, String aDefault)
{
    // Set type
    setType(Type.Option);
    
    // Configure
    if(getMessage()!=null) _builder.addLabel(getMessage());  // Add Message label
    if(getContent()!=null) _builder.addView(getContent());  // Add custom content
    
    // Show panel
    if(!showPanel(aView)) return -1;
    return _index;
}

/**
 * Shows an input panel.
 */
public String showInputDialog(View aView, String aDefault)
{
    // Set type
    setType(Type.Input);
    
    // Configure
    Label mlabel = null;
    if(getMessage()!=null) mlabel = _builder.addLabel(getMessage());  // Add Message label
    if(getContent()!=null) _builder.addView(getContent());  // Add custom content
    TextField tfield = _builder.addTextField("InputText", aDefault); tfield.setMinWidth(150);
    _builder.setFirstFocus(tfield);
    
    // Bind TextField width to MessageLabel width
    //if(mlabel!=null) mlabel.widthProperty().addListener((obs,oval,nval) -> tfield.setPrefWidth(nval.doubleValue()));

    // Show panel
    if(!showPanel(aView)) return null;
    return _builder.getViewStringValue("InputText");
}

/**
 * Show Dialog.
 */
protected boolean showPanel(View aView)
{
    // Configure window
    WindowView win = getWindow();
    win.setTitle(getTitle());
    win.setType(WindowView.TYPE_UTILITY);
    win.setModal(true);
    
    // Make sure stage and Builder.FirstFocus are focused
    _builder.runLater(() -> notifyDidShow());
    
    // Show window and return
    Point pnt = win.getScreenLocation(aView, Pos.CENTER, 0, 0);
    win.show(aView, pnt.x, pnt.y);
    return !_cancelled;
}

/**
 * Hide dialog.
 */
protected void hide()  { getWindow().hide(); }

/**
 * Hides the dialog box.
 */
public void confirm()  { _cancelled = false; _index = 0; hide(); }

/**
 * Cancels the dialog box.
 */
public void cancel()  { _cancelled = true; hide(); }

/**
 * Adds the option buttons for dialog panel.
 */
protected HBox addOptionButtons()
{
    // If there is only a label in RootView, make it at least an inch tall
    if(_builder._pane.getChildCount()==1 && _builder._pane.getChild(0) instanceof Label)
        _builder._pane.getChild(0).setMinHeight(60);

    // Add OK/Cancel buttons
    String titles[] = getOptions(), rtitles[] = titles.clone(); ArrayUtils.reverse(rtitles);
    HBox bbox = _builder.addButtons(rtitles, rtitles);
    bbox.setAlign(Pos.CENTER_RIGHT); bbox.setPadding(15,15,15,15);
    for(View btn : bbox.getChildren()) { btn.setMinWidth(100); btn.setMinHeight(24); }
    
    // Set DefaultButton (and maybe FirstFocus)
    Button dbutton = (Button)bbox.getChild(titles.length-1);
    dbutton.setDefaultButton(true); if(getFirstFocus()==null) setFirstFocus(dbutton);
    return bbox;
}

/**
 * Called after dialog box shows itself (via runLater).
 */
protected void notifyDidShow()
{
    // Set FirstFocus from content if available
    ViewOwner owner = getContent()!=null? getContent().getOwner() : null;
    if(owner!=null && owner.getFirstFocus()!=null)
        owner.requestFocus(owner.getFirstFocus());
    else if(_builder.getFirstFocus()!=null)
        _builder.requestFocus(_builder.getFirstFocus());
}

/**
 * Creates the UI.
 */
protected View createUI()
{
    ParentView view = (ParentView)super.createUI(); view.setMinWidth(300);
    
    // If image provided, reset pane to hbox containing image and original root pane
    if(getImage()!=null) {
        HBox hbox = new HBox(); hbox.setPadding(15,0,0,20); hbox.setSpacing(20);
        hbox.setAlign(Pos.TOP_CENTER);
        hbox.addChild(new ImageView(getImage()));
        hbox.addChild(view); view = hbox;
    }
    else view.setPadding(25, 30, 8, 30);
    
    // Add OK/Cancel buttons
    HBox bbox = addOptionButtons();
    
    // Wrap buttons and view in BorderView and return
    BorderView bview = new BorderView(); bview.setCenter(view); bview.setBottom(bbox);
    return bview;
}

/**
 * Initialize the UI.
 */
protected void initUI()
{
    super.initUI();  // Do normal version
    addKeyActionEvent("EscapeAction", "ESCAPE");  // Add Escape key binding
}

/**
 * RespondUI.
 */
public void respondUI(ViewEvent anEvent)
{
    // Handle Option buttons
    if(anEvent.getView() instanceof Button) { View button = anEvent.getView();
        String name = button.getName(), options[] = getOptions();
        for(int i=0; i<options.length; i++)
            if(name.equals(options[i])) {
                _index = i; hide(); }
        if(options[0].equals("Cancel")) return; // Bogus
    }
    
    // Handle Cancel, EscapeAction
    if(anEvent.equals("Cancel") || anEvent.equals("EscapeAction")) { cancel(); }
    
    // Do normal version
    super.respondUI(anEvent);
}

/**
 * Show a dialog panel for confirm message.
 */
public static boolean showConfirmDialog(View aView, String aTitle, String aMessage)
{
    DialogBox dbox = new DialogBox(aTitle); dbox.setQuestionMessage(aMessage);
    return dbox.showConfirmDialog(aView);
}

/**
 * Show a dialog panel for error message.
 */
public static void showErrorDialog(View aView, String aTitle, String aMessage)
{
    DialogBox dbox = new DialogBox(aTitle); dbox.setErrorMessage(aMessage);
    dbox.showMessageDialog(aView);
}

/**
 * Show a dialog panel for warning message.
 */
public static void showWarningDialog(View aView, String aTitle, String aMessage)
{
    DialogBox dbox = new DialogBox(aTitle); dbox.setWarningMessage(aMessage);
    dbox.showMessageDialog(aView);
}

/**
 * Show a dialog panel for exception.
 */
public static void showExceptionDialog(View aView, String aTitle, Throwable e)
{
    DialogBox dbox = new DialogBox(aTitle); dbox.setErrorMessage(e.toString());
    dbox.showMessageDialog(aView); e.printStackTrace();
}

/**
 * Show a dialog panel for exception.
 */
public static String showInputDialog(View aView, String aTitle, String aMessage, String aDefault)
{
    DialogBox dbox = new DialogBox(aTitle); dbox.setQuestionMessage(aMessage);
    return dbox.showInputDialog(aView, aDefault);
}

}