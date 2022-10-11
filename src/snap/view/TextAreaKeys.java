/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import snap.text.TextSel;
import snap.util.SnapUtils;

/**
 * This class is a helper for JavaTextArea to handle key processing.
 */
public class TextAreaKeys {

    // The TextArea
    protected TextArea  _textArea;

    /**
     * Constructor.
     */
    public TextAreaKeys(TextArea aJTA)
    {
        _textArea = aJTA;
    }

    /** TextArea method. */
    public boolean isSelEmpty()  { return _textArea.isSelEmpty(); }

    /** TextArea method. */
    public TextSel getSel()  { return _textArea.getSel(); }

    /** TextArea method. */
    public void setSel(int charIndex)  { _textArea.setSel(charIndex); }

    /** TextArea method. */
    public int getSelStart()  { return _textArea.getSelStart(); }

    /** TextArea method. */
    public int length()  { return _textArea.length(); }

    /** TextArea method. */
    public char charAt(int charIndex)  { return _textArea.charAt(charIndex); }

    /**
     * Called when a key is pressed.
     */
    protected void keyPressed(ViewEvent anEvent)
    {
        // Get event info
        int keyCode = anEvent.getKeyCode();
        boolean commandDown = anEvent.isShortcutDown(), controlDown = anEvent.isControlDown();
        boolean emacsDown = SnapUtils.isWindows ? anEvent.isAltDown() : controlDown;
        boolean shiftDown = anEvent.isShiftDown();
        _textArea.setCaretAnim(false);
        _textArea.setShowCaret(_textArea.isCaretNeeded());

        // Handle command keys
        if (commandDown) {

            // If shift-down, just return
            if (shiftDown && keyCode != KeyCode.Z) return;

            // Handle common command keys
            switch(keyCode) {
                case KeyCode.X: _textArea.cut(); anEvent.consume(); break; // Handle command-x cut
                case KeyCode.C: _textArea.copy(); anEvent.consume(); break; // Handle command-c copy
                case KeyCode.V: _textArea.paste(); anEvent.consume(); break; // Handle command-v paste
                case KeyCode.A: _textArea.selectAll(); anEvent.consume(); break; // Handle command-a select all
                case KeyCode.Z:
                    if(shiftDown)
                        _textArea.redo();
                    else _textArea.undo();
                    anEvent.consume(); break; // Handle command-z undo
                default: return; // Any other command keys just return
            }
        }

        // Handle control keys (not applicable on Windows, since they are handled by command key code above)
        else if (emacsDown) {

            // If shift down, just return
            if (shiftDown) return;

            // Handle common emacs key bindings
            switch (keyCode) {
                case KeyCode.F: _textArea.selectForward(false); break; // Handle control-f key forward
                case KeyCode.B: _textArea.selectBackward(false); break; // Handle control-b key backward
                case KeyCode.P: _textArea.selectUp(); break; // Handle control-p key up
                case KeyCode.N: _textArea.selectDown(); break; // Handle control-n key down
                case KeyCode.A: _textArea.selectLineStart(); break; // Handle control-a line start
                case KeyCode.E: _textArea.selectLineEnd(); break; // Handle control-e line end
                case KeyCode.D: _textArea.deleteForward(); break; // Handle control-d delete forward
                case KeyCode.K: _textArea.deleteToLineEnd(); break; // Handle control-k delete line to end
                default: return; // Any other control keys, just return
            }
        }

        // Handle supported non-character keys
        else switch (keyCode) {

                // Handle Tab
                case KeyCode.TAB:
                    _textArea.replaceChars("\t");
                    anEvent.consume();
                    break;

                // Handle Enter
                case KeyCode.ENTER:
                    if (_textArea.isFireActionOnEnterKey()) {
                        _textArea.selectAll();
                        _textArea.fireActionEvent(anEvent);
                    } else {
                        _textArea.replaceChars("\n");
                        anEvent.consume();
                    }
                    break;

                // Handle Left arrow
                case KeyCode.LEFT:
                    _textArea.selectBackward(shiftDown);
                    anEvent.consume();
                    break;

                // Handle Right arrow
                case KeyCode.RIGHT:
                    _textArea.selectForward(shiftDown);
                    anEvent.consume();
                    break;

                // Handle Up arrow
                case KeyCode.UP:
                    _textArea.selectUp();
                    anEvent.consume();
                    break;

                // Handle down arrow
                case KeyCode.DOWN:
                    _textArea.selectDown();
                    anEvent.consume();
                    break;

                // Handle Home key
                case KeyCode.HOME:
                    _textArea.selectLineStart();
                    break;

                // Handle End key
                case KeyCode.END:
                    _textArea.selectLineEnd();
                    break;

                // Handle Backspace key
                case KeyCode.BACK_SPACE:
                    _textArea.deleteBackward();
                    anEvent.consume();
                    break;

                // Handle Delete key
                case KeyCode.DELETE:
                    _textArea.deleteForward();
                    anEvent.consume();
                    break;

                // Handle Space key
                case KeyCode.SPACE:
                    anEvent.consume();
                    break;

                // Handle any other non-character key: Just return
                default: return;
            }

        // Consume the event
        //anEvent.consume();
    }

    /**
     * Called when a key is typed.
     */
    protected void keyTyped(ViewEvent anEvent)
    {
        // Get event info
        String keyChars = anEvent.getKeyString();
        char keyChar = keyChars.length() > 0 ? keyChars.charAt(0) : 0;
        boolean charDefined = keyChar != KeyCode.CHAR_UNDEFINED && !Character.isISOControl(keyChar);
        boolean commandDown = anEvent.isShortcutDown();
        boolean controlDown = anEvent.isControlDown();
        boolean emacsDown = SnapUtils.isWindows ? anEvent.isAltDown() : controlDown;

        // If actual text entered, replace
        if (charDefined && !commandDown && !controlDown && !emacsDown) {
            _textArea.replaceChars(keyChars);
            _textArea.hideCursor(); //anEvent.consume();
        }
    }

    /**
     * Called when a key is released.
     */
    protected void keyReleased(ViewEvent anEvent)
    {
        _textArea.setCaretAnim();
    }
}