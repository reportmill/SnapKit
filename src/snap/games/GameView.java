/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.games;
import snap.geom.Point;
import snap.geom.Rect;
import snap.gfx.Color;
import snap.gfx.Font;
import snap.gfx.Painter;
import snap.gfx.Stroke;
import snap.view.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class is the main view for games and manages Actors.
 */
public class GameView extends ChildView {

    // Whether to draw the grid
    private boolean _showCoords;

    // The frame rate
    private double _frameRate = 24;

    // Whether mouse is down
    private ViewEvent _mouseDown;

    // Whether mouse was clicked on this frame
    private ViewEvent _mouseClicked;

    // The mouse location
    private double _mouseX, _mouseY;

    // The pressed key
    private Set<Integer> _keyDowns = new HashSet<>();

    // The key typed in current frame
    private Set<Integer> _keyClicks = new HashSet<>();

    // Whether to auto start
    private boolean _autoStart = true;

    // The animation timer    
    private ViewTimer _timer = new ViewTimer(this::doAct, getFrameDelay());

    /**
     * Constructor.
     */
    public GameView()
    {
        super();
        setPrefSize(800, 600);
        setFill(Color.WHITE);
        setBorder(Color.BLACK, 1);
        enableEvents(MouseEvents);
        enableEvents(KeyEvents);
        setFocusable(true);
        setFocusWhenPressed(true);
    }

    /**
     * Returns the number of cells available on X axis.
     */
    public int getCellsWide()
    {
        return (int) Math.round(getWidth());
    }

    /**
     * Returns the number of cells available on Y axis.
     */
    public int getCellsHigh()
    {
        return (int) Math.round(getHeight());
    }

    /**
     * Returns the cell width.
     */
    public double getCellWidth()
    {
        return getWidth() / getCellsWide();
    }

    /**
     * Returns the cell height.
     */
    public double getCellHeight()
    {
        return getHeight() / getCellsHigh();
    }

    /**
     * Adds a new actor to game view at given XY point.
     */
    public void addActorAtXY(Actor anActor, double anX, double aY)
    {
        // Update actor location and ensure UI is loaded and updated
        anActor.setXY(anX, aY);

        // Get index (if there is a PaintOrder, index might not be at end)
    /*int index = _actors.size();
    if(_paintOrder!=null) {
        SnapActor actors[] = _actors.toArray(new SnapActor[_actors.size()]);
        ActorClassComparator comp = new ActorClassComparator(actors, _paintOrder);
        index = -Arrays.binarySearch(actors, anActor, comp) - 1;
    }*/

        //_actors.add(index, anActor); anActor.setParent(this);
        //getActorPane().getChildren().add(index*2, anActor.getUI());
        //getActorPane().getChildren().add(index, anActor._pen._pathGroup); _actArray = null;
        addChild(anActor);
    }

    /**
     * Returns the actor with given name.
     */
    public Actor getActor(String aName)
    {
        View child = getChildForName(aName);
        return child instanceof Actor ? (Actor) child : null;
    }

    /**
     * Returns the child actor intersecting given point in local coords.
     */
    public <T extends Actor> T getActorAt(double aX, double aY, Class<T> aClass)
    {
        for (View child : getChildren()) {
            if (!(child instanceof Actor)) continue;
            if (aClass == null || aClass.isInstance(child)) {
                Point point = child.parentToLocal(aX, aY);
                if (child.contains(point.getX(), point.getY()))
                    return (T) child;
            }
        }
        return null;
    }

    /**
     * Returns the child actor intersecting given point in local coords.
     */
    public <T extends Actor> List<T> getActorsAt(double aX, double aY, Class<T> aClass)
    {
        List<T> actors = new ArrayList<>();
        for (View child : getChildren()) {
            if (!(child instanceof Actor)) continue;
            if (aClass == null || aClass.isInstance(child)) {
                Point point = child.parentToLocal(aX, aY);
                if (child.contains(point.getX(), point.getY()))
                    actors.add((T) child);
            }
        }
        return actors;
    }

    /**
     * Removes an actor.
     */
    public Actor removeActor(int anIndex)
    {
        return (Actor) removeChild(anIndex);
    }

    /**
     * Removes an actor.
     */
    public int removeActor(Actor anActor)
    {
        int index = indexOfChild(anActor);
        if (index >= 0) removeActor(index);
        return index;
    }

    /**
     * Returns the frame rate.
     */
    public double getFrameRate()  { return _frameRate; }

    /**
     * Sets the frame rate.
     */
    public void setFrameRate(double aValue)
    {
        _frameRate = aValue;
        _timer.setPeriod(getFrameDelay());
    }

    /**
     * Returns the frame delay in milliseconds.
     */
    public int getFrameDelay()
    {
        return _frameRate <= 0 ? Integer.MAX_VALUE : (int) Math.round(1000 / _frameRate);
    }

    /**
     * Returns whether to draw coordinate grid.
     */
    public boolean getShowCoords()  { return _showCoords; }

    /**
     * Sets whether to draw coordinate grid.
     */
    public void setShowCoords(boolean aValue)
    {
        _showCoords = aValue;
        repaint();
    }

    /**
     * Sets the color by text.
     */
    public void setColor(String aString)
    {
        Color color = Color.get(aString);
        if (color != null)
            setFill(color);
        else System.err.println("SetColor: Don't recognize color: " + aString);
    }

    /**
     * Returns the x value of given named actor (or "Mouse").
     */
    public double getX(String aName)
    {
        if (aName.equalsIgnoreCase("mouse"))
            return getMouseX();
        Actor actor = getActor(aName);
        return actor != null ? actor.getX() : 0;
    }

    /**
     * Returns the y value of given named actor (or "Mouse").
     */
    public double getY(String aName)
    {
        if (aName.equalsIgnoreCase("mouse"))
            return getMouseY();
        Actor actor = getActor(aName);
        return actor != null ? actor.getY() : 0;
    }

    /**
     * Returns whether the mouse button is down.
     */
    public boolean isMouseDown()  { return _mouseDown != null; }

    /**
     * Returns whether the mouse was clicked on this frame.
     */
    public boolean isMouseClicked()  { return _mouseClicked != null; }

    /**
     * Returns the mouse X.
     */
    public double getMouseX()  { return _mouseX; }

    /**
     * Returns the mouse Y.
     */
    public double getMouseY()  { return _mouseY; }

    /**
     * Returns the mouse button.
     */
    public int getMouseButton()
    {
        ViewEvent me = _mouseDown != null ? _mouseDown : _mouseClicked;
        return 0;//me!=null? me.getButton().ordinal() : 0;
    }

    /**
     * Returns the mouse click count.
     */
    public int getMouseClickCount()
    {
        return _mouseClicked != null ? _mouseClicked.getClickCount() : 0;
    }

    /**
     * Returns whether a given key is pressed.
     */
    public boolean isKeyDown(String aKey)
    {
        int keyCode = KeyCode.get(aKey.toUpperCase());
        return _keyDowns.contains(keyCode);
    }

    /**
     * Returns whether a given key was pressed in the current frame.
     */
    public boolean isKeyClicked(String aKey)
    {
        int keyCode = KeyCode.get(aKey.toUpperCase());
        return _keyClicks.contains(keyCode);
    }

    /**
     * Starts the animation.
     */
    public void start()
    {
        _timer.start();
    }

    /**
     * Stops the animation.
     */
    public void stop()
    {
        _timer.stop();
    }

    /**
     * Returns whether game is playing.
     */
    public boolean isPlaying()
    {
        return _timer.isRunning();
    }

    /**
     * Returns whether game autostarts.
     */
    public boolean isAutoStart()  { return _autoStart; }

    /**
     * Sets whether game autostarts.
     */
    public void setAutoStart(boolean aValue)
    {
        _autoStart = aValue;
    }

    /**
     * Override to start/stop animation.
     */
    @Override
    protected void setShowing(boolean aValue)
    {
        // Do normal version
        if (aValue == isShowing()) return;
        super.setShowing(aValue);

        // If showing, autostart
        if (aValue) {
            if (_autoStart)
                _timer.start(800);
            _autoStart = false;
        }

        // If hiding, stop playing
        else {
            _autoStart = isPlaying();
            stop();
        }
    }

    /**
     * Process event.
     */
    protected void processEvent(ViewEvent anEvent)
    {
        // Handle MouseEvent
        if (anEvent.isMouseEvent()) {
            if (anEvent.isMousePress()) _mouseDown = anEvent;
            else if (anEvent.isMouseRelease()) _mouseDown = null;
            else if (anEvent.isMouseClick()) _mouseClicked = anEvent;
            else if (anEvent.isMouseMove() && getShowCoords())
                repaint(Rect.getRectForPoints(anEvent.getPoint(), new Point(_mouseX, _mouseY)).getInsetRect(-80, -25));
            _mouseX = anEvent.getX();
            _mouseY = anEvent.getY();
        }

        // Handle KeyEvent: Update KeyDowns and KeyClicks for event
        else if (anEvent.isKeyEvent()) {
            int kcode = anEvent.getKeyCode();
            if (anEvent.isKeyPress()) {
                _keyDowns.add(kcode);
                _keyClicks.add(kcode);
            }
            else if (anEvent.isKeyRelease()) _keyDowns.remove(kcode);
        }
    }

    /**
     * Calls the act method and actors act methods.
     */
    void doAct()
    {
        try {
            act();
            for (View child : getChildren())
                if (child instanceof Actor)
                    ((Actor) child).act();
            _mouseClicked = null;
            _keyClicks.clear();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * The act method.
     */
    protected void act()
    {
    }

    /**
     * Starts thread for calling Actors main.
     */
    void startActor(Actor anActor)
    {
        new Thread(() -> anActor.main()).start();
    }

    /**
     * Paints the canvas.
     */
    @Override
    protected void paintFront(Painter aPntr)
    {
        // Paint grid
        if (getShowCoords())
            paintGrid(aPntr);

        // Iterate over children and paint pen paths
        for (View child : getChildren()) {

            if (child instanceof PenActor penActor)
                penActor.paintPen(aPntr);

            // Return stroke
            aPntr.setStroke(Stroke.Stroke1);
        }
    }

    /**
     * Draws the coordinate system.
     */
    private void paintGrid(Painter aPntr)
    {
        double viewW = getWidth();
        double viewH = getHeight();

        // Paint grid
        aPntr.setPaint(Color.LIGHTGRAY);
        aPntr.setStroke(Stroke.Stroke1);
        for (int i = 20; i <= viewW; i += 20)
            aPntr.drawLine(i + .5, 0, i + .5, viewH);
        for (int i = 20; i <= viewH; i += 20)
            aPntr.drawLine(0, i + .5, viewW, i + .5);

        // Paint coords in corners
        aPntr.setFont(Font.Arial14);
        aPntr.setPaint(Color.BLACK);
        aPntr.drawString("( " + (int) viewW + ", 0 )", viewW - 70, 18);
        aPntr.drawString("( 0, " + (int) viewH + " )", 5, viewH - 10);
        if (_mouseX > 0 && _mouseY > 0 && _mouseX < viewW && _mouseY < viewH) {
            int x = (int) _mouseX, y = (int) _mouseY;
            aPntr.drawString("( " + x + ", " + y + " )", x + 5, y + 20);
        }
    }
}