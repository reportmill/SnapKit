package snap.games;
import snap.geom.Point;
import snap.geom.Rect;
import snap.geom.Vector;
import snap.util.ListUtils;
import snap.util.MathUtils;
import java.util.List;
import java.util.stream.Stream;

/**
 * This actor subclass provides advanced functionality like velocity, hit detection, edge wrapping.
 */
public class SkilledActor extends Actor {

    // Whether this actor wraps at stage view edges
    protected boolean _wrapAtEdges;

    // The velocity vector
    private Vector _velocity;

    /**
     * Constructor.
     */
    public SkilledActor()
    {
        super();
        _velocity = Vector.ZERO;
    }

    /**
     * Returns whether actor wraps at stage view edges.
     */
    public boolean isWrapAtEdges()  { return _wrapAtEdges; }

    /**
     * Sets whether actor wraps at stage view edges.
     */
    public void setWrapAtEdges(boolean aValue)  { _wrapAtEdges = aValue; }

    /**
     * Returns the velocity vector.
     */
    public Vector getVelocity()  { return _velocity; }

    /**
     * Sets the velocity vector.
     */
    public void setVelocity(Vector aVector)
    {
        _velocity = aVector;
    }

    /**
     * Increases the velocity of this actor by given amount in pixels per frame.
     */
    public void addVelocity(double velocityAmount)
    {
        Vector velocityVector = Vector.getVectorForAngleAndLength(getRotate(), velocityAmount);
        setVelocity(_velocity.add(velocityVector));
    }

    /**
     * Turn actor to given point X/Y.
     */
    public void turnToXY(double aX, double aY)
    {
        double angle = getAngleToXY(aX, aY);
        turnBy(angle - getRotate());
    }

    /**
     * Turn actor to given point X/Y.
     */
    public void turnToActor(Actor anActor)
    {
        StageView stageView = getStageView();
        ActorView otherActorView = anActor._actorView;
        Point otherCenterInParent = otherActorView.localToParent(otherActorView.getMidX(), otherActorView.getMidY(), stageView);
        Point otherCenterInLocal = _actorView.parentToLocal(otherCenterInParent.x, otherCenterInParent.y, stageView);
        turnToXY(otherCenterInLocal.x, otherCenterInLocal.y);
    }

    /**
     * Returns the bounds.
     */
    public Rect getBounds()  { return _actorView.getBounds(); }

    /**
     * Returns whether this actor intersects given actor.
     */
    public boolean intersectsActor(Actor anActor)
    {
        return _actorView.intersectsActor(anActor._actorView);
    }

    /**
     * Returns the actors intersecting this actor that match given class (class can be null).
     */
    public boolean isIntersectingActor(Class<? extends Actor> aClass)  { return getIntersectingActor(aClass) != null; }

    /**
     * Returns the actors intersecting this actor that match given class (class can be null).
     */
    public <T extends Actor> T getIntersectingActor(Class<T> aClass)
    {
        List<Actor> actors = getStageView().getActors();
        Stream<T> actorStream = (Stream<T>) actors.stream();
        if (aClass != null)
            actorStream = actorStream.filter(aClass::isInstance);
        return actorStream.filter(this::intersectsActor).findFirst().orElse(null);
    }

    /**
     * Returns the actors intersecting this actor that match given class (class can be null).
     */
    public <T extends Actor> List<T> getIntersectingActors(Class<T> aClass)
    {
        List<Actor> actors = getStageView().getActors();
        Stream<T> actorStream = (Stream<T>) actors.stream();
        if (aClass != null)
            actorStream = actorStream.filter(aClass::isInstance);
        return actorStream.filter(this::intersectsActor).toList();
    }

    /**
     * Returns the first actor in given range radius that match given class (class can be null).
     */
    public <T> T getActorInRange(double aRadius, Class<T> aClass)
    {
        List<Actor> actors = getStageView().getActors();
        return (T) ListUtils.findMatch(actors, actor -> isActorInRange(actor, aRadius, aClass));
    }

    /**
     * Returns the actors in given range radius that match given class (class can be null).
     */
    public <T> List<T> getActorsInRange(double aRadius, Class<T> aClass)
    {
        List<Actor> actors = getStageView().getActors();
        return (List<T>) ListUtils.filter(actors, actor -> isActorInRange(actor, aRadius, aClass));
    }

    /**
     * Returns whether given actor is in range and of matching class (class can be null).
     */
    protected boolean isActorInRange(Actor anActor, double aRadius, Class<?> aClass)
    {
        if (aClass != null && !aClass.isInstance(anActor))
            return false;
        return getDistanceToActor(anActor) <= aRadius;
    }

    /**
     * Returns the first actor hit by given point that match given class (class can be null).
     */
    public <T> T getActorAtXY(double aX, double aY, Class<T> aClass)
    {
        StageView stageView = getStageView();
        Point gameXY = _actorView.localToParent(aX, aY, stageView);
        List<Actor> actors = getStageView().getActors();
        return (T) ListUtils.findMatch(actors, actor -> actor != this && stageView.isActorAtXY(actor, gameXY.x, gameXY.y, aClass));
    }

    /**
     * Returns the actors hit by given point that match given class (class can be null).
     */
    public <T> List<T> getActorsAtXY(double aX, double aY, Class<T> aClass)
    {
        StageView stageView = getStageView();
        Point gameXY = _actorView.localToParent(aX, aY, stageView);
        List<Actor> actors = getStageView().getActors();
        return (List<T>) ListUtils.filter(actors, actor -> actor != this && stageView.isActorAtXY(actor, gameXY.x, gameXY.y, aClass));
    }

    /**
     * Moves this actor to given XY from current location, stopping if it hits actor of given class.
     */
    public void moveByXyNoCollision(double moveX, double moveY, Class<? extends Actor> hitClass)
    {
        if (moveX != 0 || moveY != 0)
            moveToXyNoCollision(getX() + moveX, getY() + moveY, hitClass);
    }

    /**
     * Moves this actor to given XY from current location, stopping if it hits actor of given class.
     */
    public void moveToXyNoCollision(double newX, double newY, Class<? extends Actor> hitClass)
    {
        // Store previous XY, try new XY and return if no collision
        double prevX = getX();
        double prevY = getY();
        setXY(newX, newY);
        if (!isIntersectingActor(hitClass))
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
            if (isIntersectingActor(hitClass)) {
                setX(getX() - signX);
                break;
            }
        }

        // Move to new Y incrementally as long as no hit
        for (int incr = 0; incr < distY; incr++) {
            setY(getY() + signY);
            if (isIntersectingActor(hitClass)) {
                setY(getY() - signY);
                break;
            }
        }
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
     * Returns the angle of the line from this actor center point to given point.
     */
    public double getAngleToXY(double aX, double aY)  { return Point.getAngle(getMidX(), getMidY(), aX, aY); }

    /**
     * Returns the angle of the line from this actor center point to given point.
     */
    public double getAngleToActor(Actor anActor)  { return getAngleToXY(anActor.getMidX(), anActor.getMidY()); }

    /**
     * Override to handle velocity and wrapping at edges.
     */
    @Override
    protected void act()
    {
        // Get new XY for velocity
        double newX = getX() + _velocity.x;
        double newY = getY() + _velocity.y;

        // Wrap to opposite edge if out of bounds
        if (isWrapAtEdges()) {
            StageView stageView = getStageView();
            double gameW = stageView.getWidth();
            double gameH = stageView.getHeight();
            if (newX >= gameW) newX = 0;
            if (newX < 0) newX = gameW - 1;
            if (newY >= gameH) newY = 0;
            if (newY < 0) newY = gameH - 1;
        }

        // Set XY
        setXY(newX, newY);
    }
}
