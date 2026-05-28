/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.games;
import snap.geom.*;
import snap.gfx.Image;
import snap.gfx.Paint;
import snap.util.MathUtils;
import java.util.List;
import java.util.stream.Stream;

/**
 * This class represents a game character in a StageView.
 */
public class Actor {

    // The ActorView
    protected ActorView _actorView;

    // A class that this actor can collide with
    protected Class<? extends Actor> _hitClass;

    /**
     * Constructor.
     */
    public Actor()
    {
        super();
        _actorView = new ProxyActorView(this);
    }

    /**
     * Returns the actor view.
     */
    public ActorView getActorView()  { return _actorView; }

    /**
     * Returns the Stage.
     */
    public Stage getStage()  { return _actorView.getStageView() instanceof ProxyStageView stageView ? stageView.getStage() : null; }

    /**
     * Returns the StageView as given class.
     */
    public <T extends Stage> T getStage(Class<? extends Stage> aClass)
    {
        Stage stage = getStage();
        return aClass.isInstance(stage) ? (T) stage : null;
    }

    /**
     * Returns the actor name.
     */
    public String getName()  { return _actorView.getName(); }

    /**
     * Sets the actor name.
     */
    public void setName(String aName)  { _actorView.setName(aName); }

    /**
     * Returns the image.
     */
    public Image getImage()  { return _actorView.getImage(); }

    /**
     * Sets the image.
     */
    public void setImage(Image anImage)  { _actorView.setImage(anImage); }

    /**
     * Returns the image name.
     */
    public String getImageName()  { return _actorView.getImageName(); }

    /**
     * Sets the image for given name.
     */
    public void setImageForName(String imageName)  { _actorView.setImageForName(imageName); }

    /**
     * Returns the X location of the view.
     */
    public double getX()  { return _actorView.getX(); }

    /**
     * Sets the X location of the view.
     */
    public void setX(double aValue)  { _actorView.setX(aValue); }

    /**
     * Returns the Y location of the view.
     */
    public double getY()  { return _actorView.getY(); }

    /**
     * Sets the Y location of the view.
     */
    public void setY(double aValue)  { _actorView.setY(aValue); }

    /**
     * Returns the width of the view.
     */
    public double getWidth()  { return _actorView.getWidth(); }

    /**
     * Sets the width of the view.
     */
    public void setWidth(double aValue)  { _actorView.setWidth(aValue); }

    /**
     * Returns the height of the view.
     */
    public double getHeight()  { return _actorView.getHeight(); }

    /**
     * Sets the height of the view.
     */
    public void setHeight(double aValue)  { _actorView.setHeight(aValue); }

    /**
     * Returns the mid x.
     */
    public double getMidX()  { return _actorView.getMidX(); }

    /**
     * Returns the mid y.
     */
    public double getMidY()  { return _actorView.getMidY(); }

    /**
     * Returns the view x/y.
     */
    public Point getXY()  { return _actorView.getXY(); }

    /**
     * Sets the view x/y.
     */
    public void setXY(double aX, double aY)  { _actorView.setXY(aX, aY); }

    /**
     * Returns the view size.
     */
    public Size getSize()  { return _actorView.getSize(); }

    /**
     * Sets the size.
     */
    public void setSize(Size aSize)  { _actorView.setSize(aSize); }

    /**
     * Sets the size.
     */
    public void setSize(double aW, double aH)  { _actorView.setSize(aW, aH); }

    /**
     * Returns the rotation of the view in degrees.
     */
    public double getRotate()  { return _actorView.getRotate(); }

    /**
     * Turn to given angle.
     */
    public void setRotate(double theDegrees)  { _actorView.setRotate(theDegrees); }

    /**
     * Returns the scale of this view.
     */
    public double getScale()  { return _actorView.getScale(); }

    /**
     * Sets the scale of this view from Y.
     */
    public void setScale(double aValue)  {  _actorView.setScale(aValue); }

    /**
     * Sets the fill.
     */
    public void setFill(Paint aPaint)  { _actorView.setFill(aPaint); }

    /**
     * Move actor forward.
     */
    public void moveBy(double aDistance)
    {
        double distX = aDistance * Math.cos(Math.toRadians(getRotate()));
        double distY = aDistance * Math.sin(Math.toRadians(getRotate()));
        moveByXY(distX, distY);
    }

    /**
     * Move actor by given distance X and Y.
     */
    public void moveByXY(double distX, double distY)
    {
        double newX = getX() + distX;
        double newY = getY() + distY;
        moveToXY(newX, newY);
    }

    /**
     * Moves this actor to given XY from current location, stopping if it hits collidable actor.
     */
    public void moveToXY(double newX, double newY)
    {
        // Store previous XY, try new XY and return if no collision
        double prevX = getX();
        double prevY = getY();
        setXY(newX, newY);
        if (!isHitActor())
            return;

        // Get values
        double dx = newX - prevX;
        double dy = newY - prevY;
        double distX = Math.abs(dx);
        double distY = Math.abs(dy);
        int signX = MathUtils.sign(dx);
        int signY = MathUtils.sign(dy);

        // Restore XY
        setXY(prevX, prevY);

        // Move to new X incrementally as long as no hit
        for (double incr = 0; incr < distX; incr++) {
            setX(getX() + signX);
            if (isHitActor()) {
                setX(getX() - signX);
                break;
            }
        }

        // Move to new Y incrementally as long as no hit
        for (int incr = 0; incr < distY; incr++) {
            setY(getY() + signY);
            if (isHitActor()) {
                setY(getY() - signY);
                break;
            }
        }
    }

    /**
     * Turn actor by given degrees.
     */
    public void turnBy(double theDegrees)
    {
        setRotate(getRotate() + theDegrees);
    }

    /**
     * Returns the angle of the line from this actor center point to current mouse point.
     */
    public double getAngleToMouse()
    {
        Stage stage = getStage();
        return Point.getAngle(getMidX(), getMidY(), stage.getMouseX(), stage.getMouseY());
    }

    /**
     * Returns the class that this actor can collide with.
     */
    public Class<? extends Actor> getHitClass()  { return _hitClass; }

    /**
     * Sets the class that this actor can collide with.
     */
    public void setHitClass(Class<? extends Actor> aClass)  { _hitClass = aClass; }

    /**
     * Returns whether this actor hits another actor of configured hit class.
     */
    public boolean isHitActor()  { return _hitClass != null && isHitActorForClass(_hitClass); }

    /**
     * Returns the first actor of configured hit class hit by this actor.
     */
    public Actor getHitActor()  { return _hitClass != null ? getHitActorForClass(_hitClass) : null; }

    /**
     * Returns whether this actor hits another actor of given class (class can be null for any actor).
     */
    public boolean isHitActorForClass(Class<? extends Actor> aClass)  { return getHitActorForClass(aClass) != null; }

    /**
     * Returns the actors intersecting this actor that match given class (class can be null for any actor).
     */
    public <T extends Actor> T getHitActorForClass(Class<T> aClass)
    {
        Stream<T> actorStream = (Stream<T>) getStage().getActors().stream().filter(actor -> actor != this);
        if (aClass != null)
            actorStream = actorStream.filter(aClass::isInstance);
        return actorStream.filter(this::intersectsActor).findFirst().orElse(null);
    }

    /**
     * Returns the actors intersecting this actor that match given class (class can be null for any actor).
     */
    public <T extends Actor> List<T> getHitActorsForClass(Class<T> aClass)
    {
        Stream<T> actorStream = (Stream<T>) getStage().getActors().stream();
        if (aClass != null)
            actorStream = actorStream.filter(aClass::isInstance);
        return actorStream.filter(this::intersectsActor).toList();
    }
    
    /**
     * Returns whether this actor intersects given actor.
     */
    public boolean intersectsActor(Actor anActor)
    {
        return _actorView.intersectsActor(anActor._actorView);
    }

    /**
     * The act method.
     */
    protected void act()  { }
}