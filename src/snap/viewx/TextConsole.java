package snap.viewx;
import java.util.*;
import snap.util.*;
import snap.view.*;

/**
 * A TextView that is a console.
 */
public class TextConsole extends TextView {

    // The location of the start of next input
    int               _inputLoc;
    
    // List of previous commands
    List <String>     _cmdHistory = new ArrayList();
    
    // Index of command
    int               _cmdHistoryIndex;
    
    // The prompt
    String            _prompt;
    
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
    if(getTextBox().length()==0)
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
    
    // Handle cursor out of range
    if(getSelStart()<getInputLocation())
        setSel(length());
    
    // Handle delete at or before input location
    if((keyCode==KeyCode.BACK_SPACE || keyCode==KeyCode.DELETE) && getSelStart()<=getInputLocation())
        return;
    
    // Handle command-k
    if(keyCode==KeyCode.K && anEvent.isMetaDown())
        clearConsole();
                
    // Handle special keys
    else switch(keyCode) {
        case KeyCode.UP: setCommandHistoryPrevious(); break; //if(keyTyped) 
        case KeyCode.DOWN: setCommandHistoryNext(); break; //if(keyTyped) 
        case KeyCode.ENTER: processEnterAction(); break;
        default: super.keyPressed(anEvent); return;
    }
    
    // Consume event
    anEvent.consume();
}

/**
 * Called when enter is hit.
 */
protected void processEnterAction()
{
    // Get command string
    String command = getInput();
    
    // Execute command
    String results = executeCommand(command);
    
    // Append results and new prompt
    append("\n" + results);
    if(!results.endsWith("\n")) append("\n");
    append(getPrompt());
}

/**
 * Appends a string.
 */
public void append(String aString)  { addChars(aString); _inputLoc = length(); }

/**
 * Gets input String from console starting at current input location.
 */
public String getInput()
{
    String input = getText().subSequence(getInputLocation(), length()).toString().trim();
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
    if(response==null)
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
    if(aCommand.startsWith("help"))
        return executeHelp(aCommand.substring(4));
    
    // Handle print command
    if(aCommand.startsWith("print "))
        return executePrint(aCommand.substring(6));
    if(aCommand.startsWith("p "))
        return executePrint(aCommand.substring(2));
    
    // Handle clear
    if(aCommand.startsWith("clear")) {
        getEnv().runLater(() -> clearConsole());
        return "";
    }
    
    // Return null since command not found
    return null;
}

/**
 * Execute a help command.
 */
public String executeHelp(String aCommand)  { return "print [ expression ]\nclear"; }

/**
 * Executes a print command.
 */
public String executePrint(String aCommand)
{
    Object value = KeyChain.getValue(new Object(), aCommand);
    if(value instanceof Number) value = SnapUtils.getBigDecimal(value);
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
    String command = _cmdHistoryIndex<_cmdHistory.size()? _cmdHistory.get(_cmdHistoryIndex) : "";
    replaceChars(command, null, getInputLocation(), length(), true);
}

/**
 * Sets a command from history.
 */
public void setCommandHistoryNext()
{
    _cmdHistoryIndex = MathUtils.clamp(_cmdHistoryIndex+1, 0, _cmdHistory.size());
    String command = _cmdHistoryIndex<_cmdHistory.size()? _cmdHistory.get(_cmdHistoryIndex) : "";
    replaceChars(command, null, getInputLocation(), length(), true);
}

}