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
 * This TextArea subclass has methods to act like a terminal/console.
 */
public class ConsoleTextArea extends TextArea {

    // The location of the start of next input
    private int _inputCharIndex;
    
    // List of previous commands
    private List <String> _cmdHistory = new ArrayList<>();
    
    // Index of command
    private int _cmdHistoryIndex;
    
    // The prompt
    private String _prompt;

    /**
     * Constructor.
     */
    public ConsoleTextArea()
    {
        super(true);
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
        if (getTextModel().isEmpty())
            append(getPrompt());
    }

    /**
     * Returns the location of the end of the last text appended to console.
     */
    public int getInputCharIndex()  { return _inputCharIndex; }

    /**
     * Handles key events.
     */
    @Override
    protected void keyPressed(ViewEvent anEvent)
    {
        // Get key info
        int keyCode = anEvent.getKeyCode();
        int inputLoc = getInputCharIndex();

        // Handle delete at or before input location
        if ((keyCode == KeyCode.BACK_SPACE || keyCode == KeyCode.DELETE) && getSelStart() <= inputLoc) {
            ViewUtils.beep();
            return;
        }

        // Handle command-k
        if (keyCode == KeyCode.K && anEvent.isShortcutDown()) {
            clearConsole();
            return;
        }

        // Handle special keys
        else switch (keyCode) {
            case KeyCode.UP -> { setCommandHistoryPrevious(); anEvent.consume(); }
            case KeyCode.DOWN -> { setCommandHistoryNext(); anEvent.consume(); }
            default -> super.keyPressed(anEvent);
        }

        // Reset input location
        _inputCharIndex = inputLoc;

        // Handle Enter action
        if (keyCode == KeyCode.ENTER)
            handleEnterAction();
    }

    /**
     * Called when a key is typed.
     */
    @Override
    protected void keyTyped(ViewEvent anEvent)
    {
        // Get keyCode and input location
        int inputLoc = getInputCharIndex();

        // Handle cursor out of range
        if (getSelStart() < inputLoc)
            setSel(length());

        super.keyTyped(anEvent);
        _inputCharIndex = inputLoc;
    }

    /**
     * Called when enter is hit.
     */
    protected void handleEnterAction()
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
    @Override
    public void addCharsWithStyle(CharSequence theChars, TextStyle textStyle, int charIndex)
    {
        super.addCharsWithStyle(theChars, textStyle, charIndex);
        _inputCharIndex = length();
    }

    /**
     * Override to update input location.
     */
    @Override
    public void replaceCharsWithStyle(CharSequence theChars, TextStyle textStyle, int aStart, int anEnd)
    {
        super.replaceCharsWithStyle(theChars, textStyle, aStart, anEnd);
        _inputCharIndex = length();
    }

    /**
     * Gets input String from console starting at current input location.
     */
    public String getInput()
    {
        String input = getText().subSequence(getInputCharIndex(), length()).toString();
        _inputCharIndex = length();
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
            value = Convert.getBigDecimal(value);
        return value.toString();
    }

    /**
     * Clears the console.
     */
    public void clearConsole()
    {
        clear(); _inputCharIndex = 0;
        append(_prompt);
    }

    /**
     * Sets a command from history.
     */
    public void setCommandHistoryPrevious()
    {
        _cmdHistoryIndex = MathUtils.clamp(_cmdHistoryIndex-1, 0, _cmdHistory.size());
        String command = _cmdHistoryIndex<_cmdHistory.size() ? _cmdHistory.get(_cmdHistoryIndex) : "";
        replaceCharsWithStyle(command, null, getInputCharIndex(), length());
    }

    /**
     * Sets a command from history.
     */
    public void setCommandHistoryNext()
    {
        _cmdHistoryIndex = MathUtils.clamp(_cmdHistoryIndex+1, 0, _cmdHistory.size());
        String command = _cmdHistoryIndex<_cmdHistory.size() ? _cmdHistory.get(_cmdHistoryIndex) : "";
        replaceCharsWithStyle(command, null, getInputCharIndex(), length());
    }
}