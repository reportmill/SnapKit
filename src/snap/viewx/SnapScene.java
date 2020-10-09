/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.viewx;
import java.util.*;

import snap.geom.Point;
import snap.geom.Rect;
import snap.gfx.*;
import snap.view.*;

/**
 * The parent and background for SnapActors.
 */
public class SnapScene extends ChildView {

    // The pane that holds actors, console and background
    StackView          _scenePane;
    
    // Whether to draw the grid
    boolean            _showCoords;
    
    // The frame rate
    double             _frameRate = 24;
    
    // Whether mouse is down
    ViewEvent          _mouseDown;
    
    // Whether mouse was clicked on this frame
    ViewEvent          _mouseClicked;
    
    // The mouse location
    double             _mx, _my;
    
    // The pressed key
    Set <Integer>      _keyDowns = new HashSet();
    
    // The key typed in current frame
    Set <Integer>      _keyClicks = new HashSet();
    
    // Whether to auto start
    boolean            _autoStart = true;

    // The animation timer    
    ViewTimer          _timer = new ViewTimer(getFrameDelay(), t -> doAct());

/**
 * Create new SnapScene.
 */
public SnapScene()
{
     setPrefSize(720, 405);
     setFill(Color.WHITE); setBorder(Color.BLACK, 1);
     enableEvents(MouseEvents); enableEvents(KeyEvents);
     setFocusable(true); setFocusWhenPressed(true);
}

/**
 * Returns the number of cells available on X axis.
 */
public int getCellsWide()  { return (int)Math.round(getWidth()); }

/**
 * Returns the number of cells available on Y axis.
 */
public int getCellsHigh()  { return (int)Math.round(getHeight()); }

/**
 * Returns the cell width.
 */
public double getCellWidth()  { return getWidth()/getCellsWide(); }

/**
 * Returns the cell height.
 */
public double getCellHeight()  { return getHeight()/getCellsHigh(); }

/**
 * Adds a new actor to scene.
 */
public void addActor(SnapActor anActor, double anX, double aY)
{
    // Update actor location and ensure UI is loaded and updated
    anActor.setXY(anX, aY); if(anActor.getPen().isPenDown()) anActor.getPen().penDown(anActor.getPenPoint());
    
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
public SnapActor getActor(String aName)
{
    View child = getChild(aName);
    return child instanceof SnapActor? (SnapActor)child : null;
}

/**
 * Returns the child actor intersecting given point in local coords.
 */
public <T extends SnapActor> T getActorAt(double aX, double aY, Class <T> aClass)
{
    for(View child : getChildren()) { if(!(child instanceof SnapActor)) continue;
        if(aClass==null || aClass.isInstance(child)) { Point point = child.parentToLocal(aX, aY);
            if(child.contains(point.getX(), point.getY()))
                return (T)child; } }
    return null;
}

/**
 * Returns the child actor intersecting given point in local coords.
 */
public <T extends SnapActor> List<T> getActorsAt(double aX, double aY, Class <T> aClass)
{
    List <T> actors = new ArrayList();
    for(View child : getChildren()) { if(!(child instanceof SnapActor)) continue;
        if(aClass==null || aClass.isInstance(child)) { Point point = child.parentToLocal(aX, aY);
            if(child.contains(point.getX(), point.getY()))
                actors.add((T)child); } }
    return actors;
}

/**
 * Removes an actor.
 */
public SnapActor removeActor(int anIndex)  { return (SnapActor)removeChild(anIndex); }

/**
 * Removes an actor.
 */
public int removeActor(SnapActor anActor)
{
    int index = indexOfChild(anActor);
    if(index>=0) removeActor(index);
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
public int getFrameDelay()  { return _frameRate<=0? Integer.MAX_VALUE : (int)Math.round(1000/_frameRate); }

/**
 * Returns whether to draw coordinate grid.
 */
public boolean getShowCoords()  { return _showCoords; }

/**
 * Sets whether to draw coordinate grid.
 */
public void setShowCoords(boolean aValue)  { _showCoords = aValue; repaint(); }

/**
 * Sets the color by text.
 */
public void setColor(String aString)
{
    Color c = Color.get(aString);
    if(c!=null) setFill(c);
    else System.err.println("SetColor: Don't recognize color: " + aString);
}

/**
 * Returns the x value of given named actor (or "Mouse").
 */
public double getX(String aName)
{
    if(aName.equalsIgnoreCase("mouse")) return getMouseX();
    SnapActor actor = getActor(aName);
    return actor!=null? actor.getX() : 0;
}

/**
 * Returns the y value of given named actor (or "Mouse").
 */
public double getY(String aName)
{
    if(aName.equalsIgnoreCase("mouse")) return getMouseY();
    SnapActor actor = getActor(aName);
    return actor!=null? actor.getY() : 0;
}

/**
 * Returns whether the mouse button is down.
 */
public boolean isMouseDown()  { return _mouseDown!=null; }

/**
 * Returns whether the mouse was clicked on this frame.
 */
public boolean isMouseClicked()  { return _mouseClicked!=null; }

/**
 * Returns the mouse X.
 */
public double getMouseX()  { return _mx; }

/**
 * Returns the mouse Y.
 */
public double getMouseY()  { return _my; }

/**
 * Returns the mouse button.
 */
public int getMouseButton()
{
    ViewEvent me = _mouseDown!=null? _mouseDown : _mouseClicked;
    return 0;//me!=null? me.getButton().ordinal() : 0;
}

/**
 * Returns the mouse click count.
 */
public int getMouseClickCount()  { return _mouseClicked!=null? _mouseClicked.getClickCount() : 0; }

/**
 * Returns whether a given key is pressed.
 */
public boolean isKeyDown(String aKey)
{
    int kp = KeyCode.get(aKey.toUpperCase());
    return _keyDowns.contains(kp);
}

/**
 * Returns whether a given key was pressed in the current frame.
 */
public boolean isKeyClicked(String aKey)
{
    int kp = KeyCode.get(aKey.toUpperCase());
    return _keyClicks.contains(kp);
}

/**
 * Starts the animation.
 */
public void start()  { _timer.start(); }

/**
 * Stops the animation.
 */
public void stop()  { _timer.stop(); }

/**
 * Whether scene is playing.
 */
public boolean isPlaying()  { return _timer.isRunning(); }

/**
 * Whether scene autostarts.
 */
public boolean isAutoStart()  { return _autoStart; }

/**
 * Sets whether scene autostarts.
 */
public void setAutoStart(boolean aValue)  { _autoStart = aValue; }

/**
 * Override to start/stop animation.
 */
protected void setShowing(boolean aValue)
{
    if(aValue==isShowing()) return; super.setShowing(aValue);
    if(aValue) { if(_autoStart) _timer.start(800); _autoStart = false; }
    else { _autoStart = isPlaying(); stop(); }
}

/**
 * Process event.
 */
protected void processEvent(ViewEvent anEvent)
{
    // Handle MouseEvent
    if(anEvent.isMouseEvent()) {
        if(anEvent.isMousePress()) _mouseDown = anEvent;
        else if(anEvent.isMouseRelease()) _mouseDown = null;
        else if(anEvent.isMouseClick()) _mouseClicked = anEvent;
        else if(anEvent.isMouseMove() && getShowCoords())
            repaint(Rect.getRectForPoints(anEvent.getPoint(), new Point(_mx,_my)).getInsetRect(-80,-25));
        _mx = anEvent.getX(); _my = anEvent.getY();
    }
    
    // Handle KeyEvent: Update KeyDowns and KeyClicks for event
    else if(anEvent.isKeyEvent()) {
        int kcode = anEvent.getKeyCode();
        if(anEvent.isKeyPress()) { _keyDowns.add(kcode); _keyClicks.add(kcode); }
        else if(anEvent.isKeyRelease()) _keyDowns.remove(kcode);
    }
}

/**
 * Calls the act method and actors act methods.
 */
void doAct()
{
    try {
        act();
        for(View child : getChildren()) if(child instanceof SnapActor) ((SnapActor)child).doAct();
        _mouseClicked = null; _keyClicks.clear();
    }
    catch(Exception e) { throw new RuntimeException(e); }
}

/**
 * The act method.
 */
protected void act()  { }

/**
 * Starts thread for calling Actors main.
 */
void startActor(SnapActor anActor)  { new Thread(() -> anActor.main()).start(); }

/**
 * Paints the canvas.
 */
protected void paintFront(Painter aPntr)
{
    if(getShowCoords()) paintGrid(aPntr);
    for(View child : getChildren()) { if(!(child instanceof SnapActor)) continue; SnapActor actr = (SnapActor)child;
        for(SnapPen.PenPath pp : actr.getPen()._paths) {
            aPntr.setColor(pp.getColor()); aPntr.setStroke(new Stroke(pp.getWidth())); aPntr.draw(pp); }
        aPntr.setStroke(Stroke.Stroke1);
    }
}

/**
 * Draws the coordinate system.
 */
void paintGrid(Painter aPntr)
{
    aPntr.setPaint(Color.LIGHTGRAY); aPntr.setStroke(Stroke.Stroke1);
    for(int i=20; i<=getWidth(); i+=20) aPntr.drawLine(i+.5, 0, i+.5, getHeight());
    for(int i=20; i<=getHeight(); i+=20) aPntr.drawLine(0, i+.5, getWidth(), i+.5);
    aPntr.setFont(Font.Arial14); aPntr.setPaint(Color.BLACK);
    aPntr.drawString("( " + (int)getWidth() + ", 0 )", getWidth() - 70, 18);
    aPntr.drawString("( 0, " + (int)getHeight() + " )", 5, getHeight() - 10);
    if(_mx>0 && _my>0 && _mx<getWidth() && _my<getHeight()) { int x = (int)_mx, y = (int)_my;
        aPntr.drawString("( " + x + ", " + y + " )", x + 5, y + 20); }
}

}