/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.games;
import snap.gfx.Image;
import snap.view.ImageView;
import snap.view.View;
import java.util.ArrayList;
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
        // Initialize name to class
        if (getClass() != Actor.class)
            setName(getClass().getSimpleName());

        // Initialize image
        Image defaultImage = Image.getImageForClassResource(getClass(), getName() + ".png");
        if (defaultImage == null)
            defaultImage = Image.getImageForClassResource(getClass(), "images/" + getName() + ".png");
        if (defaultImage != null) {
            setImage(defaultImage);
            setSize(defaultImage.getWidth(), defaultImage.getHeight());
        }
    }

    /**
     * Returns the center X.
     */
    public double getCenterX()
    {
        return getX() + getWidth() / 2;
    }

    /**
     * Returns the center Y.
     */
    public double getCenterY()
    {
        return getY() + getHeight() / 2;
    }

    /**
     * Returns the cell X.
     */
    public int getCellX()
    {
        GameView gameView = getGameView();
        double cellW = gameView != null ? gameView.getCellWidth() : 1;
        return (int) Math.floor(getX() / cellW);
    }

    /**
     * Returns the cell Y.
     */
    public int getCellY()
    {
        GameView gameView = getGameView();
        double cellH = gameView != null ? gameView.getCellHeight() : 1;
        return (int) Math.floor(getY() / cellH);
    }

    /**
     * Move sprite forward.
     */
    public void moveBy(double aCount)
    {
        // Do move
        double newX = getX() + aCount * Math.cos(Math.toRadians(getRotate()));
        double newY = getY() + aCount * Math.sin(Math.toRadians(getRotate()));
        setXY(newX, newY);
    }

    /**
     * Move sprite to a point using setRotation and moveBy (so pen drawing works).
     */
    public void moveTo(double anX, double aY)
    {
        setRotate(getAngle(anX, aY));
        moveBy(getDistance(anX, aY));
    }

    /**
     * Turn sprite by given degrees.
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

    /** Returns the intersecting actors of given class. */
    /*public <T extends SnapActor> T getIntersectingActor(Class <T> aClass)
    { return _parent.getIntersectingActor(getBoundsInParent(), aClass); }*/
    /** Returns the intersecting actors of given class. */
    /*public <T extends SnapActor> List <T> getIntersectingActors(Class <T> aClass)
    { return _parent.getIntersectingActors(getBoundsInParent(), aClass); }*/

    /**
     * Returns the actors in range.
     */
    public <T extends Actor> List<T> getActorsInRange(double aRadius, Class<T> aClass)
    {
        List<T> actors = new ArrayList<>();
        for (int i = getGameView().getChildCount() - 1; i >= 0; i--) {
            View child = getGameView().getChild(i);
            if (aClass == null || aClass.isInstance(child)) {
                if (getDistance((Actor) child) <= aRadius)
                    actors.add((T) child);
            }
        }

        // Return
        return actors;
    }

    /**
     * Returns the angle to the mouse point.
     */
    public double getAngle(String aName)
    {
        return getAngle(getGameView().getX(aName), getGameView().getY(aName));
    }

    /**
     * Returns the angle to the mouse point.
     */
    public double getAngle(double mx, double my)
    {
        double x = getCenterX(), y = getCenterY();
        double dx = mx - x, dy = my - y;
        double angle = Math.toDegrees(Math.atan(dy / Math.abs(dx)));
        if (dx < 0) angle = 180 - angle;
        else if (dx == 0) angle = dy > 0 ? -90 : dy < 0 ? 90 : getRotate();
        return angle;
    }

    /**
     * Returns the distance to the mouse point.
     */
    public double getDistance(String aName)
    {
        return getDistance(getGameView().getX(aName), getGameView().getY(aName));
    }

    /**
     * Returns the distance to the given actor.
     */
    public double getDistance(Actor anActor)
    {
        return getDistance(anActor.getCenterX(), anActor.getCenterY());
    }

    /**
     * Returns the distance to the given point.
     */
    public double getDistance(double x2, double y2)
    {
        double x1 = getCenterX(), y1 = getCenterY();
        double dx = x2 - x1, dy = y2 - y1;
        return Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * Returns the GameView.
     */
    public GameView getGameView()
    {
        return getParent(GameView.class);
    }

    /**
     * The main execution loop.
     */
    protected void main()  { }

    /**
     * The act method.
     */
    protected void act()  { }
}