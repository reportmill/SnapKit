/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.games;
import snap.geom.Point;
import snap.geom.Rect;
import snap.gfx.*;
import snap.util.ListUtils;
import snap.util.XMLArchiver;
import snap.util.XMLAttribute;
import snap.util.XMLElement;
import snap.view.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class is the main view for games and manages Actors.
 */
public class GameView extends ChildView {

    // The controller
    protected GameController _controller;

    // Whether to draw the grid
    private boolean _showCoords;

    // Image name set in archival
    private String _imageName;

    // The current list of actors
    private List<Actor> _actors;

    // The current list of actors reversed
    private List<Actor> _actorsReversed;

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

    /**
     * Constructor.
     */
    public GameView()  { this(800, 600); }

    /**
     * Constructor.
     */
    public GameView(double width, double height)
    {
        super();
        setPrefSize(width, height);
        setFill(Color.WHITE);
        setClipToBounds(true);
        enableEvents(MouseEvents);
        enableEvents(KeyEvents);
        setFocusable(true);
        setFocusWhenPressed(true);

        // Get default image for class and set
        Image defaultClassImage = Game.getImageForClass(getClass());
        if (defaultClassImage != null)
            setImage(defaultClassImage);
    }

    /**
     * Returns the game controller.
     */
    public GameController getController()
    {
        if (_controller != null) return _controller;
        return _controller = new GameController(this);
    }

    /**
     * Returns the background image.
     */
    public Image getImage()
    {
        Paint fill = getFill();
        return fill instanceof ImagePaint imageFill ? imageFill.getImage() : null;
    }

    /**
     * Sets the background image.
     */
    public void setImage(Image anImage)
    {
        if (anImage == getImage()) return;
        if (anImage != null) {
            anImage.waitForImageLoad();
            ImagePaint imageFill = new ImagePaint(anImage);
            setFill(imageFill);
        }
        else setFill(null);
    }

    /**
     * Returns the image for given name.
     */
    private Image getImageForName(String imageName)
    {
        if (imageName.contains("."))
            return Game.getImageForClassResource(getClass(), imageName);
        return Game.getLibraryImageForName(imageName);
    }

    /**
     * Sets the image for given name.
     */
    public void setImageForName(String imageName)
    {
        _imageName = imageName;
        Image image = getImageForName(imageName);
        if (image != null)
            setImage(image);
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
     * Returns the actors.
     */
    public List<Actor> getActors()
    {
        if (_actors != null) return _actors;
        return _actors = ListUtils.filterByClass(getChildren(), Actor.class);
    }

    /**
     * Returns the actors reversed.
     */
    public List<Actor> getActorsReversed()
    {
        if (_actorsReversed != null) return _actorsReversed;
        return _actorsReversed = ListUtils.getReverse(getActors());
    }

    /**
     * Adds a new actor to game view at given XY point.
     */
    public void addActorAtXY(Actor anActor, double anX, double aY)
    {
        anActor.setXY(anX, aY);
        addChild(anActor);
    }

    /**
     * Returns the actor with given name.
     */
    public Actor getActorForName(String aName)
    {
        return ListUtils.findMatch(getActors(), actor -> actor.getName().equals(aName));
    }

    /**
     * Returns the first actor hit by given point in local coords.
     */
    public <T extends Actor> T getActorAtXY(double aX, double aY, Class<T> aClass)
    {
        return (T) ListUtils.findMatch(getActors(), actor -> isActorAtXY(actor, aX, aY, aClass));
    }

    /**
     * Returns the actors hit by given point in local coords.
     */
    public <T extends Actor> List<T> getActorsAtXY(double aX, double aY, Class<T> aClass)
    {
        return (List<T>) ListUtils.filter(getActors(), actor -> isActorAtXY(actor, aX, aY, aClass));
    }

    /**
     * Returns whether given actor is at given XY and of matching class.
     */
    protected boolean isActorAtXY(Actor anActor, double aX, double aY, Class<?> aClass)
    {
        if (aClass != null && !aClass.isInstance(anActor))
            return false;
        Point point = anActor.parentToLocal(aX, aY, this);
        return anActor.contains(point.x, point.y);
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
     * Sets the game view fill color for given color string.
     */
    public void setFillColor(String aString)
    {
        Color color = Color.get(aString);
        if (color != null)
            setFill(color);
        else System.err.println("SetColor: Don't recognize color: " + aString);
    }

    /**
     * Returns whether the mouse button is down.
     */
    public boolean isMouseDown()  { return _mouseDown != null; }

    /**
     * Returns the mouse X.
     */
    public double getMouseX()  { return _mouseX; }

    /**
     * Returns the mouse Y.
     */
    public double getMouseY()  { return _mouseY; }

    /**
     * Returns whether the mouse was clicked on this frame.
     */
    public boolean isMouseClicked()  { return _mouseClicked != null; }

    /**
     * Returns the mouse click count.
     */
    public int getMouseClickCount()  { return _mouseClicked != null ? _mouseClicked.getClickCount() : 0; }

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
     * Returns whether game is playing.
     */
    public boolean isPlaying()  { return getController().isPlaying(); }

    /**
     * Sets whether game is playing.
     */
    public void setPlaying(boolean aValue)  { getController().setPlaying(aValue); }

    /**
     * Starts the game timer.
     */
    public void play()  { getController().playGame(); }

    /**
     * Stops the game timer.
     */
    public void stop()  { getController().stopGame(); }

    /**
     * Steps the game forward a frame.
     */
    protected void stepGameFrame()
    {
        try {
            getActors().forEach(Actor::act);
            _mouseClicked = null;
            _keyClicks.clear();
        }

        catch (Exception e) {
            stop();
            throw new RuntimeException(e);
        }
    }

    /**
     * Paints the game view.
     */
    @Override
    protected void paintFront(Painter aPntr)
    {
        // Paint grid
        if (getShowCoords())
            paintGrid(aPntr);

        // Iterate over children and paint pen paths
        for (View child : getActors()) {
            if (child instanceof PenActor penActor) {
                penActor.paintPen(aPntr);
                aPntr.setStroke(Stroke.Stroke1);
            }
        }
    }

    /**
     * Paints the coordinate grid.
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
            int mouseX = (int) _mouseX;
            int mouseY = (int) _mouseY;
            aPntr.drawString("( " + mouseX + ", " + mouseY + " )", mouseX + 5, mouseY + 20);
        }
    }

    /**
     * Process event.
     */
    protected void processEvent(ViewEvent anEvent)
    {
        // Handle MouseEvent
        if (anEvent.isMouseEvent()) {
            if (anEvent.isMousePress())
                _mouseDown = anEvent;
            else if (anEvent.isMouseRelease())
                _mouseDown = null;
            else if (anEvent.isMouseClick())
                _mouseClicked = anEvent;
            else if (anEvent.isMouseMove() && getShowCoords())
                repaint(Rect.getRectForPoints(anEvent.getPoint(), new Point(_mouseX, _mouseY)).getInsetRect(-80, -25));
            _mouseX = anEvent.getX();
            _mouseY = anEvent.getY();
        }

        // Handle KeyEvent: Update KeyDowns and KeyClicks for event
        else if (anEvent.isKeyEvent()) {
            int keyCode = anEvent.getKeyCode();
            if (anEvent.isKeyPress()) {
                _keyDowns.add(keyCode);
                _keyClicks.add(keyCode);
            }
            else if (anEvent.isKeyRelease())
                _keyDowns.remove(keyCode);
        }
    }

    /**
     * Override to clear actors list.
     */
    @Override
    public void addChild(View aChild, int anIndex)
    {
        super.addChild(aChild, anIndex);
        _actors = null;
    }

    /**
     * Override to clear actors list.
     */
    @Override
    public View removeChild(int anIndex)
    {
        View child = super.removeChild(anIndex);
        _actors = null;
        return child;
    }

    /**
     * Override to archive ImageName.
     */
    @Override
    protected XMLElement toXMLView(XMLArchiver anArchiver)
    {
        XMLElement xml = super.toXMLView(anArchiver);

        // Archive ImageName
        if (_imageName != null && !_imageName.isEmpty())
            xml.add("ImageName", _imageName);

        // Return
        return xml;
    }

    /**
     * Override to support image name.
     */
    @Override
    protected void fromXMLView(XMLArchiver anArchiver, XMLElement anElement)
    {
        super.fromXMLView(anArchiver, anElement);
        if (anElement.hasAttribute("ImageName")) {
            String imageName = anElement.getAttributeValue("ImageName");
            setImageForName(imageName);
        }
    }
}