/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.viewx;
import snap.geom.HPos;
import snap.gfx.*;
import snap.util.ArrayUtils;
import snap.view.*;

/**
 * A class to run a dialog box.
 */
public class DialogBox extends FormBuilder {

    // The main message to display
    private String  _message;

    // The title of the dialog box
    private String  _title;

    // The type of box
    private Type  _type = Type.Message;

    // The type of message
    private MessageType  _messageType = MessageType.Plain;

    // The options for buttons
    private String[]  _options;

    // The image for dialog box
    private Image  _image;

    // The content for dialog box
    private View  _content;

    // The prompt text for input dialog
    private String _inputPromptText;

    // Whether dialog box can be confirmed (confirm button is enabled)
    private boolean  _confirmEnabled = true;

    // Whether to trigger confirm when enter key is pressed
    private boolean  _confirmOnEnter = true;

    // Whether stage was cancelled
    protected boolean  _cancelled;

    // The confirm button
    private Button _confirmButton;

    // The cancel button
    private Button _cancelButton;

    // The box to hold the buttons
    private RowView  _buttonBox;

    // Index of selected option
    private int _selOptionIndex = DialogBox.CANCEL_OPTION;

    // Constants for DialogBox type
    public enum Type { Message, Confirm, Option, Input }

    // Constants for tone of dialog box
    public enum MessageType { Plain, Question, Information, Warning, Error }

    // Standard Options
    public static final String[] OPTIONS_OK = {"OK"};
    public static final String[] OPTIONS_OK_CANCEL = {"OK", "Cancel"};
    public static final String[] OPTIONS_YES_NO_CANCEL = {"Yes", "No", "Cancel"};

    // Return values
    public static final int OK_OPTION = 0; /** Return value form class method if OK is chosen. */
    public static final int YES_OPTION = 0; /** Return value form class method if YES is chosen. */
    public static final int NO_OPTION = 1;  /** Return value from class method if NO is chosen. */
    public static final int CANCEL_OPTION = 2;   /** Return value from class method if CANCEL is chosen. */

    // Get InfoImage
    public static Image questImage = Image.getImageForClassResource(DialogBox.class, "Dialog_Question.png");
    public static Image infoImage = Image.getImageForClassResource(DialogBox.class, "Dialog_Info.png");
    public static Image warnImage = Image.getImageForClassResource(DialogBox.class, "Dialog_Warning.png");
    public static Image errorImage = Image.getImageForClassResource(DialogBox.class, "Dialog_Error.png");

    /**
     * Constructor.
     */
    public DialogBox()
    {
    }

    /**
     * Constructor with given title.
     */
    public DialogBox(String aTitle)
    {
        setTitle(aTitle);
    }

    /**
     * Returns the message to display.
     */
    public String getMessage()  { return _message; }

    /**
     * Sets the message to display.
     */
    public void setMessage(String aMessage)
    {
        _message = aMessage;
    }

    /**
     * Sets the message to display.
     */
    public void setErrorMessage(String aMessage)
    {
        setMessage(aMessage);
        setMessageType(MessageType.Error);
    }

    /**
     * Sets the message to display.
     */
    public void setWarningMessage(String aMessage)
    {
        setMessage(aMessage);
        setMessageType(MessageType.Warning);
    }

    /**
     * Sets the message to display.
     */
    public void setQuestionMessage(String aMessage)
    {
        setMessage(aMessage);
        setMessageType(MessageType.Question);
    }

    /**
     * Returns the title of the dialog box.
     */
    public String getTitle()  { return _title; }

    /**
     * Sets the title of the dialog box.
     */
    public void setTitle(String aTitle)
    {
        _title = aTitle;
    }

    /**
     * Returns the type of the box.
     */
    public Type getType()  { return _type; }

    /**
     * Sets the type of the box.
     */
    public void setType(Type aType)
    {
        _type = aType;
    }

    /**
     * Returns the message type of the box.
     */
    public MessageType getMessageType()  { return _messageType; }

    /**
     * Sets the message type of the box.
     */
    public void setMessageType(MessageType aMessageType)
    {
        _messageType = aMessageType;
    }

    /**
     * Returns the options strings.
     */
    public String[] getOptions()
    {
        if (_options != null)
            return _options;
        if (getType() == Type.Message)
            return OPTIONS_OK;
        return OPTIONS_OK_CANCEL;
    }

    /**
     * Sets the option strings.
     */
    public void setOptions(String ... theOptions)
    {
        _options = theOptions;
    }

    /**
     * Returns the image.
     */
    public Image getImage()
    {
        if (_image != null) return _image;
        return getImageDefault();
    }

    /**
     * Sets the Image.
     */
    public void setImage(Image anImage)
    {
        _image = anImage;
    }

    /**
     * Returns the image.
     */
    public Image getImageDefault()
    {
        // If there is actual Content UI, there is no default image
        if (getContent() != null) return null;

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
    public void setContent(View aView)
    {
        _content = aView;
    }

    /**
     * Returns the prompt text for input dialog.
     */
    public String getInputPromptText()  { return _inputPromptText; }

    /**
     * Sets the prompt text for input dialog.
     */
    public void setInputPromptText(String aValue)  { _inputPromptText = aValue; }

    /**
     * Returns whether dialog box can be confirmed (confirm button is enabled).
     */
    public boolean isConfirmEnabled()  { return _confirmEnabled; }

    /**
     * Sets whether dialog box can be confirmed (confirm button is enabled).
     */
    public void setConfirmEnabled(boolean aValue)
    {
        if (aValue == isConfirmEnabled()) return;
        _confirmEnabled = aValue;
        if (isUISet())
            _confirmButton.setEnabled(aValue);
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
        if (aValue == isConfirmOnEnter()) return;
        _confirmOnEnter = aValue;
    }

    /**
     * Returns the confirm button.
     */
    public Button getConfirmButton()  { return _confirmButton; }

    /**
     * Returns the cancel button.
     */
    public Button getCancelButton()  { return _cancelButton; }

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

        // Add Message and Content
        addMessageAndContent();

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

        // Add Message and Content
        addMessageAndContent();

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

        // Add Message and Content
        addMessageAndContent();

        // Show panel
        if (!showPanel(aView))
            return -1;
        return _selOptionIndex;
    }

    /**
     * Shows an input panel.
     */
    public String showInputDialog(View aView, String aDefault)
    {
        // Set type
        setType(Type.Input);

        // Add Message and Content
        addMessageAndContent();

        // Add InputText
        TextField textField = addTextField("InputText", aDefault);
        textField.setMinWidth(150);
        setFirstFocus(textField);
        if (getInputPromptText() != null)
            textField.setPromptText(getInputPromptText());

        // Listen for InputText KeyRelease events to update ConfirmEnabled
        setConfirmEnabled(aDefault != null && !aDefault.isEmpty());
        textField.addEventFilter(e -> runLater(() -> setConfirmEnabled(!textField.getText().isEmpty())), KeyRelease);

        // Show panel
        if (!showPanel(aView))
            return null;

        // Return TextField.Text
        return textField.getText();
    }

    /**
     * Show Dialog.
     */
    protected boolean showPanel(View aView)
    {
        // Configure window
        WindowView window = getWindow();
        window.setTitle(getTitle());
        window.setType(WindowView.TYPE_UTILITY);
        window.setModal(true);

        // Make sure UI fits
        View rootView = window.getRootView();
        if (rootView.getPrefWidth() + 20 > GFXEnv.getEnv().getScreenBoundsInset().getWidth())
            rootView.setMaxWidth(GFXEnv.getEnv().getScreenBoundsInset().getWidth() - 20);
        if (rootView.getPrefHeight() + 40 > GFXEnv.getEnv().getScreenBoundsInset().getHeight())
            rootView.setMaxHeight(GFXEnv.getEnv().getScreenBoundsInset().getHeight() - 40);

        // Make sure stage and Builder.FirstFocus are focused
        runLater(() -> notifyDidShow());

        // Show window
        window.showCentered(aView);

        // Return
        return !_cancelled;
    }

    /**
     * Hide dialog.
     */
    protected void hide()
    {
        WindowView window = getWindow();
        window.hide();
    }

    /**
     * Hides the dialog box.
     */
    public void confirm()
    {
        _cancelled = false;
        _selOptionIndex = 0;
        hide();
    }

    /**
     * Cancels the dialog box.
     */
    public void cancel()
    {
        _cancelled = true;
        hide();
    }

    /**
     * Returns whether dialog was cancelled.
     */
    public boolean isCancelled()  { return _cancelled; }

    /**
     * Adds the Message and Content, if available.
     */
    protected void addMessageAndContent()
    {
        // Add Message Text
        String message = getMessage();
        if (message != null)
            addTextArea(message);

        // Add content view
        View content = getContent();
        if (content != null)
            addView(content);
    }

    /**
     * Adds the option buttons for dialog panel.
     */
    protected void addOptionButtons()
    {
        // If there is only a label in RootView, make it at least an inch tall
        ViewList formViewChildren = _formView.getChildren();
        if (formViewChildren.getFirst() instanceof Label)
            formViewChildren.getFirst().setMinHeight(60);

        // Add OK/Cancel buttons
        String[] titles = getOptions();
        String[] titlesReversed = titles.clone();
        ArrayUtils.reverse(titlesReversed);
        _buttonBox = addButtons(titlesReversed, titlesReversed);
        _buttonBox.setPadding(15, 15, 15, 15); //_buttonBox.setAlign(Pos.CENTER_RIGHT);
        ViewList buttonBoxButtons = _buttonBox.getChildren();
        for (View button : buttonBoxButtons) {
            button.setMinWidth(100);
            button.setMinHeight(24);
            button.setLeanX(HPos.RIGHT);
        }

        // Set ConfirmButton (and maybe FirstFocus)
        _confirmButton = (Button) _buttonBox.getChild(titles.length - 1);
        _confirmButton.setDefaultButton(true);
        if (getFirstFocus() == null)
            setFirstFocus(_confirmButton);
        _confirmButton.setEnabled(isConfirmEnabled());

        // Set CancelButton
        _cancelButton = _buttonBox.getChildCount() > 1 ? (Button) _buttonBox.getChild(0) : null;
    }

    /**
     * Called after dialog box shows itself (via runLater).
     */
    protected void notifyDidShow()
    {
        // Request focus on  Content.Owner.FirstFocus if available
        View content = getContent();
        ViewOwner owner = content != null ? content.getOwner() : null;
        if (owner != null && owner.getFirstFocus() != null)
            owner.requestFocus(owner.getFirstFocus());
    }

    /**
     * Creates the UI.
     */
    protected View createUI()
    {
        View formView = super.createUI();
        formView.setMinWidth(300);

        // If image provided, reset pane to RowView containing image and original root pane
        Image dialogImage = getImage();
        if (dialogImage != null) {
            ImageView dialogImageView = new ImageView(dialogImage);
            RowView rowView = new RowView();
            rowView.setPropsString("Padding:15,0,0,20; Spacing:20; Align:TOP_CENTER;");
            rowView.setChildren(dialogImageView, formView);
            formView = rowView;
        }

        // Otherwise, just set FormView.Padding
        else formView.setPadding(25, 30, 8, 30);

        // Add OK/Cancel buttons
        addOptionButtons();

        // Wrap view and button box in BorderView
        BorderView borderView = new BorderView();
        borderView.setCenter(formView);
        borderView.setBottom(_buttonBox);

        // Return
        return borderView;
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
        if (anEvent.getView() == _confirmButton || anEvent.equals("EnterAction"))
            handleConfirmOrEnterAction(anEvent);

        // Handle Cancel, EscapeAction
        else if (anEvent.equals("Cancel") || anEvent.equals("EscapeAction")) {
            cancel();
            anEvent.consume();
        }

        // Handle Option buttons
        else if (anEvent.getView() instanceof Button)
            handleOptionButtonActionEvent(anEvent);

        // Handle TextFields: If original event was Enter key and ConfirmEnabled, confirm
        else if (anEvent.getView() instanceof TextField)
            handleTextFieldActionEvent(anEvent);

        // Do normal version
        else super.respondUI(anEvent);
    }

    /**
     * Called on ConfirmButton or EnterAction.
     */
    private void handleConfirmOrEnterAction(ViewEvent anEvent)
    {
        // If confirm not possible, just beep and return
        if (!isConfirmEnabled()) {
            beep();
            return;
        }

        // Confirm and consume event
        confirm();
        anEvent.consume();
    }

    /**
     * Called when TextField gets Action event.
     */
    private void handleTextFieldActionEvent(ViewEvent anEvent)
    {
        // If Action event parent was EnterKey, handle confirm
        for (ViewEvent viewEvent = anEvent; viewEvent != null; viewEvent = viewEvent.getParentEvent()) {
            if (viewEvent.isEnterKey()) {
                handleConfirmOrEnterAction(viewEvent);
                return;
            }
        }
    }

    /**
     * Called when OptionButton gets Action event.
     */
    private void handleOptionButtonActionEvent(ViewEvent anEvent)
    {
        View optionButton = anEvent.getView();
        String optionName = optionButton.getName();
        String[] options = getOptions();
        int matchIndex = ArrayUtils.findMatchIndex(options, option -> optionName.equals(option));
        if (matchIndex >= 0) {
            _selOptionIndex = matchIndex;
            hide();
        }
    }

    /**
     * Show a dialog panel for confirm message.
     */
    public static boolean showConfirmDialog(View aView, String aTitle, String aMessage)
    {
        DialogBox dialogBox = new DialogBox(aTitle);
        dialogBox.setQuestionMessage(aMessage);
        return dialogBox.showConfirmDialog(aView);
    }

    /**
     * Show a dialog panel for error message.
     */
    public static void showErrorDialog(View aView, String aTitle, String aMessage)
    {
        DialogBox dialogBox = new DialogBox(aTitle);
        dialogBox.setErrorMessage(aMessage);
        dialogBox.showMessageDialog(aView);
    }

    /**
     * Show a dialog panel for warning message.
     */
    public static void showWarningDialog(View aView, String aTitle, String aMessage)
    {
        DialogBox dialogBox = new DialogBox(aTitle);
        dialogBox.setWarningMessage(aMessage);
        dialogBox.showMessageDialog(aView);
    }

    /**
     * Show a dialog panel for exception.
     */
    public static void showExceptionDialog(View aView, String aTitle, Throwable e)
    {
        DialogBox dialogBox = new DialogBox(aTitle);
        dialogBox.setErrorMessage(e.toString());
        dialogBox.showMessageDialog(aView);
        e.printStackTrace();
    }

    /**
     * Show a dialog panel for exception.
     */
    public static String showInputDialog(View aView, String aTitle, String aMessage, String aDefault)
    {
        DialogBox dialogBox = new DialogBox(aTitle);
        dialogBox.setQuestionMessage(aMessage);
        return dialogBox.showInputDialog(aView, aDefault);
    }
}