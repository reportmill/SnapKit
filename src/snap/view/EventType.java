package snap.view;

/**
 * Types for events.
 */
public enum EventType {

    /**
     * Action event.
     */
    Action,

    /**
     * Key events.
     */
    KeyPress, KeyRelease, KeyType,

    /**
     * Mouse events.
     */
    MousePress, MouseDrag, MouseRelease, MouseEnter, MouseMove, MouseExit,

    /**
     * Scroll event.
     */
    Scroll,

    /**
     * Drag events.
     */
    DragEnter, DragOver, DragExit, DragDrop,

    /**
     * DragSource events.
     */
    DragGesture, DragSourceEnter, DragSourceOver, DragSourceExit, DragSourceEnd;

    // Conveniences for common types
    public static final EventType[] KeyEvents = { KeyPress, KeyRelease, KeyType };
    public static final EventType[] MouseEvents = { MousePress, MouseDrag, MouseRelease, MouseEnter, MouseMove, MouseExit };
    public static final EventType[] DragEvents = { DragEnter, DragExit, DragOver, DragDrop };
}
