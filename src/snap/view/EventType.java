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

    /**
     * Convenience interface for common events.
     */
    public interface AllTypes {

        // Action
        EventType Action = EventType.Action;

        // Key events
        EventType KeyPress = EventType.KeyPress;
        EventType KeyRelease = EventType.KeyRelease;
        EventType KeyType = EventType.KeyType;

        // Mouse events
        EventType MousePress = EventType.MousePress;
        EventType MouseDrag = EventType.MouseDrag;
        EventType MouseRelease = EventType.MouseRelease;
        EventType MouseEnter = EventType.MouseEnter;
        EventType MouseMove = EventType.MouseMove;
        EventType MouseExit = EventType.MouseExit;

        // Drag events
        EventType DragEnter = EventType.DragEnter;
        EventType DragOver = EventType.DragOver;
        EventType DragExit = EventType.DragExit;
        EventType DragDrop = EventType.DragDrop;

        // Drag source events
        EventType DragGesture = EventType.DragGesture;
        EventType DragSourceEnd = EventType.DragSourceEnd;

        // Scroll
        EventType Scroll = EventType.Scroll;

        // Common groups
        EventType[] KeyEvents = EventType.KeyEvents;
        EventType[] MouseEvents = EventType.MouseEvents;
        EventType[] DragEvents = EventType.DragEvents;
    }
}
