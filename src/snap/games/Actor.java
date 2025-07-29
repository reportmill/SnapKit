/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.games;
import snap.geom.Point;
import snap.gfx.Image;
import snap.util.ListUtils;
import snap.view.ImageView;
import java.util.List;

/**
 * This class represents a game character in a GameView.
 */
public class Actor extends ImageView {

    /**
     * Constructor.
     */
    public Actor()
    {
        super();
        addPropChangeListener(pc -> handleImageChange(), Image_Prop);

        // Initialize name to class
        if (getClass() != Actor.class)
            setName(getClass().getSimpleName());

        // Get default image and set
        Image defaultImage = getDefaultImage();
        if (defaultImage != null)
            setImage(defaultImage);
    }

    /**
     * Returns the GameView.
     */
    public GameView getGameView()  { return getParent(GameView.class); }

    /**
     * Move actor forward.
     */
    public void moveBy(double aCount)
    {
        double newX = getX() + aCount * Math.cos(Math.toRadians(getRotate()));
        double newY = getY() + aCount * Math.sin(Math.toRadians(getRotate()));
        setXY(newX, newY);
    }

    /**
     * Move actor to a point using setRotation and moveBy (so pen drawing works).
     */
    public void moveTo(double anX, double aY)
    {
        setRotate(getAngleToXY(anX, aY));
        moveBy(getDistanceToXY(anX, aY));
    }

    /**
     * Turn actor by given degrees.
     */
    public void turnBy(double theDegrees)
    {
        setRotate(getRotate() + theDegrees);
    }

    /**
     * Scale actor by given amount.
     */
    public void scaleBy(double aScale)
    {
        setScaleX(getScaleX() + aScale);
        setScaleY(getScaleY() + aScale);
    }

    /**
     * Returns whether actor intersects given rect in local coords.
     */
    public boolean intersects(double aX, double aY, double aW, double aH)
    {
        double maxX = aX + aW;
        double maxY = aY + aH;
        return 0 < maxX && aX < getWidth() && 0 < maxY && aY < getHeight();
    }

    /**
     * Returns the actors in range.
     */
    public <T extends Actor> List<T> getActorsInRange(double aRadius, Class<T> aClass)
    {
        List<Actor> actorsReversed = getGameView().getActorsReversed();
        return (List<T>) ListUtils.filter(actorsReversed, actor -> isActorInRange(actor, aRadius, aClass));
    }

    /**
     * Returns whether given actor is in range and of matching class.
     */
    private boolean isActorInRange(Actor anActor, double aRadius, Class<?> aClass)
    {
        if (aClass != null && !aClass.isInstance(anActor))
            return false;
        return getDistanceToActor(anActor) <= aRadius;
    }

    /**
     * Returns the angle of the line from this actor center point to given point.
     */
    public double getAngleToXY(double aX, double aY)  { return Point.getAngle(getMidX(), getMidY(), aX, aY); }

    /**
     * Returns the angle of the line from this actor center point to given point.
     */
    public double getAngleToActor(Actor anActor)  { return getAngleToXY(anActor.getMidX(), anActor.getMidY()); }

    /**
     * Returns the angle of the line from this actor center point to current mouse point.
     */
    public double getAngleToMouse()
    {
        GameView gameView = getGameView();
        return getAngleToXY(gameView.getMouseX(), gameView.getMouseY());
    }

    /**
     * Returns the distance from this actor center point to given point.
     */
    public double getDistanceToXY(double aX, double aY)  { return Point.getDistance(getMidX(), getMidY(), aX, aY); }

    /**
     * Returns the distance from this actor center point to given actor center point.
     */
    public double getDistanceToActor(Actor anActor)  { return getDistanceToXY(anActor.getMidX(), anActor.getMidY()); }

    /**
     * Returns the default image for this actor.
     */
    public Image getDefaultImage()
    {
        Image defaultImage = Image.getImageForClassResource(getClass(), getName() + ".png");
        if (defaultImage == null)
            defaultImage = Image.getImageForClassResource(getClass(), "images/" + getName() + ".png");
        if (defaultImage != null && !defaultImage.isLoaded())
            defaultImage.waitForImageLoad();
        return defaultImage;
    }

    /**
     * The act method.
     */
    protected void act()  { }

    /**
     * Called when image changes.
     */
    protected void handleImageChange()
    {
        Image image = getImage();
        if (image != null) {
            if (!image.isLoaded())
                image.waitForImageLoad();
            setSize(image.getWidth(), image.getHeight());
        }
    }
}