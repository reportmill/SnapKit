/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.viewx;
import java.util.*;
import snap.gfx.Color;
import snap.text.TextStyle;
import snap.util.*;
import snap.view.*;

/**
 * A TextView subclass with methods to act like a terminal/console.
 */
public class ConsoleView extends TextArea {

    // The location of the start of next input
    private int  _inputLoc;
    
    // List of previous commands
    private List <String>  _cmdHistory = new ArrayList();
    
    // Index of command
    private int  _cmdHistoryIndex;
    
    // The prompt
    private String  _prompt;

    /**
     * Creates new ConsoleView.
     */
    public ConsoleView()
    {
        setFill(Color.WHITE);
        setEditable(true);
    }

    /**
     * Returns the prompt.
     */
    public String getPrompt()  { return _prompt; }

    /**
     * Sets the prompt.
     */
    public void setPrompt(String aPrompt)
    {
        _prompt = aPrompt;
        if (getTextBox().length()==0)
            append(getPrompt());
    }

    /**
     * Returns the location of the end of the last text appended to console.
     */
    public int getInputLocation()  { return _inputLoc; }

    /**
     * Handles key events.
     */
    protected void keyPressed(ViewEvent anEvent)
    {
        // Get key info
        int keyCode = anEvent.getKeyCode();
        int inputLoc = getInputLocation();

        // Handle delete at or before input location
        if ((keyCode==KeyCode.BACK_SPACE || keyCode==KeyCode.DELETE) && getSelStart()<=inputLoc) {
            ViewUtils.beep();
            return;
        }

        // Handle command-k
        if (keyCode==KeyCode.K && anEvent.isMetaDown()) {
            clearConsole();
            return;
        }

        // Handle special keys
        else switch (keyCode) {
            case KeyCode.UP: setCommandHistoryPrevious(); anEvent.consume(); break;
            case KeyCode.DOWN: setCommandHistoryNext(); anEvent.consume(); break;
            default: super.keyPressed(anEvent);
        }

        // Reset input location
        _inputLoc = inputLoc;

        // Handle Enter action
        if (keyCode==KeyCode.ENTER)
            processEnterAction();
    }

    /**
     * Called when a key is typed.
     */
    protected void keyTyped(ViewEvent anEvent)
    {
        // Get keyCode and input location
        int keyCode = anEvent.getKeyCode();
        int inputLoc = getInputLocation();

        // Handle cursor out of range
        if (getSelStart()<inputLoc)
            setSel(length());

        super.keyTyped(anEvent);
        _inputLoc = inputLoc;
    }

    /**
     * Called when enter is hit.
     */
    protected void processEnterAction()
    {
        // Get command string
        String cmd = getInput().trim();

        // Execute command
        String results = executeCommand(cmd);

        // Append results and new prompt
        append(results);
        if (!results.endsWith("\n"))
            append("\n");
        append(getPrompt());
    }

    /**
     * Appends a string.
     */
    public void append(String aString)  { addChars(aString); }

    /**
     * Override to update input location.
     */
    public void replaceChars(String aString, TextStyle aStyle, int aStart, int anEnd, boolean doUpdateSel)
    {
        super.replaceChars(aString, aStyle, aStart, anEnd, doUpdateSel);
        _inputLoc = length();
    }

    /**
     * Gets input String from console starting at current input location.
     */
    public String getInput()
    {
        String input = getText().subSequence(getInputLocation(), length()).toString();
        _inputLoc = length();
        return input;
    }

    /**
     * Executes command.
     */
    public String executeCommand(String aCommand)
    {
        // Trim command
        aCommand = aCommand.trim();

        // Do real execute command
        String response = executeCommandImpl(aCommand);
        if (response==null)
            response = "Command not found";

        // Append last command
        _cmdHistory.add(aCommand);
        _cmdHistoryIndex = _cmdHistory.size();

        // Return response
        return response;
    }

    /**
     * Executes command.
     */
    protected String executeCommandImpl(String aCommand)
    {
        // Handle help
        if (aCommand.startsWith("help"))
            return executeHelp(aCommand.substring(4));

        // Handle print command
        if (aCommand.startsWith("print "))
            return executePrint(aCommand.substring(6));
        if (aCommand.startsWith("p "))
            return executePrint(aCommand.substring(2));

        // Handle clear
        if (aCommand.startsWith("clear")) {
            getEnv().runLater(() -> clearConsole());
            return "";
        }

        // Return null since command not found
        return null;
    }

    /**
     * Execute a help command.
     */
    public String executeHelp(String aCommand)
    {
        return "print [ expression ]\nclear";
    }

    /**
     * Executes a print command.
     */
    public String executePrint(String aCommand)
    {
        Object value = KeyChain.getValue(new Object(), aCommand);
        if (value instanceof Number)
            value = SnapUtils.getBigDecimal(value);
        return value.toString();
    }

    /**
     * Clears the console.
     */
    public void clearConsole()
    {
        clear(); _inputLoc = 0;
        append(_prompt);
    }

    /**
     * Sets a command from history.
     */
    public void setCommandHistoryPrevious()
    {
        _cmdHistoryIndex = MathUtils.clamp(_cmdHistoryIndex-1, 0, _cmdHistory.size());
        String command = _cmdHistoryIndex<_cmdHistory.size() ? _cmdHistory.get(_cmdHistoryIndex) : "";
        replaceChars(command, null, getInputLocation(), length(), true);
    }

    /**
     * Sets a command from history.
     */
    public void setCommandHistoryNext()
    {
        _cmdHistoryIndex = MathUtils.clamp(_cmdHistoryIndex+1, 0, _cmdHistory.size());
        String command = _cmdHistoryIndex<_cmdHistory.size() ? _cmdHistory.get(_cmdHistoryIndex) : "";
        replaceChars(command, null, getInputLocation(), length(), true);
    }
}