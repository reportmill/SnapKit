/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.viewx;
import snap.geom.HPos;
import snap.geom.Pos;
import snap.gfx.*;
import snap.util.ArrayUtils;
import snap.view.*;

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
    
    // Whether dialog box can be confirmed (confirm button is enabled)
    boolean          _confirmEnabled = true;
    
    // Whether to trigger confirm when enter key is pressed
    boolean          _confirmOnEnter = true;

    // The FormBuilder
    FormBuilder      _builder = this;
    
    // Whether stage was cancelled
    boolean          _cancelled;
    
    // The confirm button
    Button           _confirmBtn;

    // The cancel button
    Button           _cancelBtn;
    
    // The box to hold the buttons
    RowView          _buttonBox;

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
    public static Image questImage = Image.get(DialogBox.class, "Dialog_Question.png");
    public static Image infoImage = Image.get(DialogBox.class, "Dialog_Info.png");
    public static Image warnImage = Image.get(DialogBox.class, "Dialog_Warning.png");
    public static Image errorImage = Image.get(DialogBox.class, "Dialog_Error.png");

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
    public void setErrorMessage(String aMessage)
    {
        setMessage(aMessage); setMessageType(MessageType.Error);
    }

    /**
     * Sets the message to display.
     */
    public void setWarningMessage(String aMessage)
    {
        setMessage(aMessage); setMessageType(MessageType.Warning);
    }

    /**
     * Sets the message to display.
     */
    public void setQuestionMessage(String aMessage)
    {
        setMessage(aMessage); setMessageType(MessageType.Question);
    }

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
        if (_options!=null) return _options;
        if (getType()==Type.Message) return OPTIONS_OK;
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
        if (getContent()!=null) return null;

        // Return standard image for message types
        switch (getMessageType()) {
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
     * Returns whether dialog box can be confirmed (confirm button is enabled).
     */
    public boolean isConfirmEnabled()  { return _confirmEnabled; }

    /**
     * Sets whether dialog box can be confirmed (confirm button is enabled).
     */
    public void setConfirmEnabled(boolean aValue)
    {
        if (aValue==isConfirmEnabled()) return;
        _confirmEnabled = aValue;
        if (isUISet())
            _confirmBtn.setEnabled(aValue);
    }

    /**
     * Returns whether to trigger confirm when enter key is pressed.
     */
    public boolean isConfirmOnEnter()  { return _confirmOnEnter; }

    /**
     * Sets whether to trigger confirm when enter key is pressed.
     */
    public void setConfirmOnEnter(boolean aValue)
    {
        if (aValue==isConfirmOnEnter()) return;
        _confirmOnEnter = aValue;
    }

    /**
     * Returns the confirm button.
     */
    public Button getConfirmButton()  { return _confirmBtn; }

    /**
     * Returns the cancel button.
     */
    public Button getCancelButton()  { return _cancelBtn; }

    /**
     * Returns the button box.
     */
    public RowView getButtonBox()  { return _buttonBox; }

    /**
     * Runs the panel.
     */
    public void showMessageDialog(View aView)
    {
        // Set type
        setType(Type.Message);

        // Configure: Add MessageLabel, Content
        if (getMessage()!=null)
            _builder.addTextArea(getMessage());
        if (getContent()!=null)
            _builder.addView(getContent());

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

        // Configure: Add MessageLabel, Content
        if (getMessage()!=null)
            _builder.addTextArea(getMessage());
        if (getContent()!=null)
            _builder.addView(getContent());

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

        // Configure: Add MessageLabel, Content
        if (getMessage()!=null)
            _builder.addTextArea(getMessage());
        if (getContent()!=null)
            _builder.addView(getContent());

        // Show panel
        if (!showPanel(aView))
            return -1;
        return _index;
    }

    /**
     * Shows an input panel.
     */
    public String showInputDialog(View aView, String aDefault)
    {
        // Set type
        setType(Type.Input);

        // Add Message Text
        if (getMessage()!=null) {
            TextArea msgText = _builder.addTextArea(getMessage());
            msgText.setGrowWidth(true);
        }

        // Add content
        if (getContent()!=null)
            _builder.addView(getContent());

        // Add InputText
        TextField tfield = _builder.addTextField("InputText", aDefault);
        tfield.setMinWidth(150);
        _builder.setFirstFocus(tfield);

        // Show panel
        if (!showPanel(aView))
            return null;
        return tfield.getText();
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
        win.showCentered(aView);
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
    protected RowView addOptionButtons()
    {
        // If there is only a label in RootView, make it at least an inch tall
        if (_builder._pane.getChildCount()==1 && _builder._pane.getChild(0) instanceof Label)
            _builder._pane.getChild(0).setMinHeight(60);

        // Add OK/Cancel buttons
        String titles[] = getOptions();
        String rtitles[] = titles.clone(); ArrayUtils.reverse(rtitles);
        _buttonBox = _builder.addButtons(rtitles, rtitles);
        _buttonBox.setPadding(15,15,15,15); //_buttonBox.setAlign(Pos.CENTER_RIGHT);
        for (View btn : _buttonBox.getChildren()) {
            btn.setMinWidth(100); btn.setMinHeight(24); btn.setLeanX(HPos.RIGHT);
        }

        // Set ConfirmButton (and maybe FirstFocus)
        _confirmBtn = (Button)_buttonBox.getChild(titles.length-1);
        _confirmBtn.setDefaultButton(true);
        if (getFirstFocus()==null) setFirstFocus(_confirmBtn);
        _confirmBtn.setEnabled(isConfirmEnabled());

        // Set CancelButton
        _cancelBtn = _buttonBox.getChildCount()>1 ? (Button)_buttonBox.getChild(0) : null;

        // Return button box
        return _buttonBox;
    }

    /**
     * Called after dialog box shows itself (via runLater).
     */
    protected void notifyDidShow()
    {
        // Set FirstFocus from content if available
        ViewOwner owner = getContent()!=null ? getContent().getOwner() : null;
        if (owner!=null && owner.getFirstFocus()!=null)
            owner.requestFocus(owner.getFirstFocus());
        else if (_builder.getFirstFocus()!=null)
            _builder.requestFocus(_builder.getFirstFocus());
    }

    /**
     * Creates the UI.
     */
    protected View createUI()
    {
        ParentView view = (ParentView)super.createUI(); view.setMinWidth(300);

        // If image provided, reset pane to hbox containing image and original root pane
        if (getImage()!=null) {
            RowView row = new RowView();
            row.setPadding(15,0,0,20);
            row.setSpacing(20);
            row.setAlign(Pos.TOP_CENTER);
            row.addChild(new ImageView(getImage()));
            row.addChild(view); view = row;
        }
        else view.setPadding(25, 30, 8, 30);

        // Add OK/Cancel buttons
        addOptionButtons();

        // Wrap view and button box in BorderView and return
        BorderView bview = new BorderView(); bview.setCenter(view); bview.setBottom(_buttonBox);
        return bview;
    }

    /**
     * Initialize the UI.
     */
    protected void initUI()
    {
        // Do normal version
        super.initUI();

        // Add Enter, Escape key bindings
        if (isConfirmOnEnter())
            addKeyActionHandler("EnterAction", "ENTER");
        addKeyActionHandler("EscapeAction", "ESCAPE");
    }

    /**
     * RespondUI.
     */
    public void respondUI(ViewEvent anEvent)
    {
        // Handle Okay, EnterAction
        if (anEvent.equals(_confirmBtn) || anEvent.equals("EnterAction")) {
            if (!isConfirmEnabled()) { beep(); return; }
            confirm(); anEvent.consume();
        }

        // Handle Cancel, EscapeAction
        else if (anEvent.equals("Cancel") || anEvent.equals("EscapeAction")) {
            cancel(); anEvent.consume();
        }

        // Handle Option buttons
        else if (anEvent.getView() instanceof Button) { View btn = anEvent.getView();
            String name = btn.getName(), options[] = getOptions();
            for (int i=0; i<options.length; i++)
                if (name.equals(options[i])) {
                    _index = i; hide(); }
        }

        // Handle TextFields: If original event was Enter key and ConfirmEnabled, confirm
        else if (anEvent.getView() instanceof TextField) {
            boolean enterAction = false;
            for (ViewEvent e=anEvent;e!=null;e=e.getParentEvent())
                if (e.isEnterKey()) enterAction = true;
            if (!enterAction) return;
            if (!isConfirmEnabled()) { beep(); return; }
            confirm();
            anEvent.consume();
        }

        // Do normal version
        else super.respondUI(anEvent);
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