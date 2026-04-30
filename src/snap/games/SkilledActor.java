package snap.games;
import snap.geom.Point;
import snap.geom.Rect;
import snap.geom.Shape;
import snap.geom.Vector;
import snap.util.ListUtils;
import java.util.List;

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
        if (!getBounds().intersectsShape(anActor._actorView.getBounds()))
            return false;
        Shape thisBoundsInParent = _actorView.localToParent(_actorView.getBoundsShape());
        Shape otherBoundsInParent = anActor._actorView.localToParent(anActor._actorView.getBoundsShape());
        return thisBoundsInParent.intersectsShape(otherBoundsInParent);
    }

    /**
     * Returns the actors intersecting this actor that match given class (class can be null).
     */
    public boolean isIntersectingActor(Class<?> aClass)  { return getIntersectingActor(aClass) != null; }

    /**
     * Returns the actors intersecting this actor that match given class (class can be null).
     */
    public <T> T getIntersectingActor(Class<T> aClass)
    {
        List<Actor> actors = getStageView().getActors();
        return (T) ListUtils.findMatch(actors, actor -> isIntersectingActor(actor, aClass));
    }

    /**
     * Returns the actors intersecting this actor that match given class (class can be null).
     */
    public <T> List<T> getIntersectingActors(Class<T> aClass)
    {
        List<Actor> actors = getStageView().getActors();
        return (List<T>) ListUtils.filter(actors, actor -> isIntersectingActor(actor, aClass));
    }

    /**
     * Returns whether given actor is intersecting and of matching class (class can be null).
     */
    protected boolean isIntersectingActor(Actor anActor, Class<?> aClass)
    {
        if (aClass != null && !aClass.isInstance(anActor))
            return false;
        return intersectsActor(anActor);
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
     * Returns whether at edge.
     */
    public boolean isAtStageEdge()
    {
        StageView stageView = getStageView();
        Rect actorBounds = _actorView.localToParent(_actorView.getBoundsShape(), stageView).getBounds();
        return actorBounds.x <= 0 || actorBounds.y <= 0 ||
                actorBounds.getMaxX() >= stageView.getWidth() || actorBounds.getMaxY() >= stageView.getHeight();
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
