/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.games;
import snap.geom.Point;
import snap.geom.Size;
import snap.gfx.*;
import snap.props.PropChange;
import snap.util.ListUtils;
import snap.view.*;
import java.util.List;

/**
 * This class is the main view for games and manages Actors.
 */
public class Stage {

    // The stage view
    protected StageView _stageView;

    // The current list of actors
    protected List<Actor> _actors;

    // The current list of actors reversed
    private List<Actor> _actorsReversed;

    /**
     * Constructor.
     */
    public Stage()  { this(800, 600); }

    /**
     * Constructor.
     */
    public Stage(double width, double height)
    {
        super();
        _stageView = new ProxyStageView(this);
    }

    /**
     * Returns the stage view.
     */
    public StageView getStageView()  { return _stageView; }

    /**
     * Returns the game controller.
     */
    public GameController getController()  { return _stageView.getController(); }

    /**
     * Returns the stage width.
     */
    public double getWidth() { return _stageView.getWidth(); }

    /**
     * Returns the stage height.
     */
    public double getHeight() { return _stageView.getHeight(); }

    /**
     * Returns the stage size.
     */
    public Size getSize()  { return _stageView.getSize(); }

    /**
     * Sets the stage size.
     */
    public void setSize(Size aSize)  { setSize(aSize.width, aSize.height); }

    /**
     * Sets the stage size.
     */
    public void setSize(double aWidth, double aHeight)
    {
        _stageView.setSize(aWidth, aHeight);
        _stageView.setPrefSize(aWidth, aHeight);
    }

    /**
     * Returns the background image.
     */
    public Image getImage()  { return _stageView.getImage(); }

    /**
     * Sets the background image.
     */
    public void setImage(Image anImage)  { _stageView.setImage(anImage); }

    /**
     * Sets the image for given name.
     */
    public void setImageForName(String imageName)  { _stageView.setImageForName(imageName); }

    /**
     * Returns whether to draw coordinate grid.
     */
    public boolean isShowCoords()  { return _stageView.getShowCoords(); }

    /**
     * Sets whether to draw coordinate grid.
     */
    public void setShowCoords(boolean aValue)  { _stageView.setShowCoords(aValue); }

    /**
     * Returns the actors.
     */
    public List<Actor> getActors()
    {
        if (_actors != null) return _actors;
        List<ProxyActorView> actorViews = ListUtils.filterByClass(_stageView.getChildren(), ProxyActorView.class);
        return _actors = ListUtils.map(actorViews, ProxyActorView::getActor);
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
     * Adds a new actor to stage view at given XY point.
     */
    public void addActorAtXY(Actor anActor, double anX, double aY)
    {
        anActor.setXY(anX, aY);
        _stageView.addChild(anActor.getActorView());
    }

    /**
     * Returns the actor with given name.
     */
    public Actor getActorForName(String aName)
    {
        return ListUtils.findMatch(getActors(), actor -> actor.getName().equals(aName));
    }

    /**
     * Returns the actors for given class.
     */
    public <T> List<T> getActorsForClass(Class<T> aClass)
    {
        return ListUtils.filterByClass(getActors(), aClass);
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
        Point point = anActor.getActorView().parentToLocal(aX, aY, _stageView);
        return anActor.getActorView().contains(point.x, point.y);
    }

    /**
     * Removes an actor.
     */
    public void removeActor(int anIndex)  { _stageView.removeChild(anIndex); }

    /**
     * Removes an actor.
     */
    public void removeActor(Actor anActor)  { _stageView.removeChild(anActor.getActorView()); }

    /**
     * Removes all actors.
     */
    public void removeActors()  { _stageView.removeChildren(); }

    /**
     * Sets the stage view fill color for given color string.
     */
    public void setFillColor(String aString)
    {
        Color color = Color.get(aString);
        if (color != null)
            _stageView.setFill(color);
        else System.err.println("SetColor: Don't recognize color: " + aString);
    }

    /**
     * Returns whether the mouse button is down.
     */
    public boolean isMouseDown()  { return _stageView.isMouseDown(); }

    /**
     * Returns the mouse X.
     */
    public double getMouseX()  { return _stageView.getMouseX(); }

    /**
     * Returns the mouse Y.
     */
    public double getMouseY()  { return _stageView.getMouseY(); }

    /**
     * Returns whether the mouse was clicked on this frame.
     */
    public boolean isMouseClicked()  { return _stageView.isMouseClicked(); }

    /**
     * Returns the mouse click count.
     */
    public int getMouseClickCount()  { return _stageView.getMouseClickCount(); }

    /**
     * Returns whether a given key is pressed.
     */
    public boolean isKeyDown(String aKey)  { return _stageView.isKeyDown(aKey); }

    /**
     * Returns whether a given key was pressed in the current frame.
     */
    public boolean isKeyClicked(String aKey)  { return _stageView.isKeyClicked(aKey); }

    /**
     * Returns whether game is playing.
     */
    public boolean isPlaying()  { return _stageView.isPlaying(); }

    /**
     * Sets whether game is playing.
     */
    public void setPlaying(boolean aValue)  { _stageView.setPlaying(aValue); }

    /**
     * Starts the game timer.
     */
    public void play()  { _stageView.play(); }

    /**
     * Stops the game timer.
     */
    public void stop()  { _stageView.stop(); }

    /**
     * Steps the game forward a frame.
     */
    protected void stepGameFrame()  { }
}