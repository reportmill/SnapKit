package snap.games;
import snap.props.*;
import snap.view.ViewArchiver;

/**
 * This actor view class holds an actor.
 */
public class ProxyActorView extends ActorView {

    // The actor
    private Actor _actor;

    // The actor class name
    private String _actorClassName;

    /**
     * Constructor.
     */
    public ProxyActorView()
    {
        super();
    }

    /**
     * Constructor.
     */
    public ProxyActorView(Actor actor)
    {
        super();
        _actor = actor;
        _actorClassName = actor.getClass().getName();
    }

    /**
     * Returns the actor.
     */
    public Actor getActor()  { return _actor; }

    /**
     * Override to forward to actor.
     */
    @Override
    protected void act()
    {
        _actor.act();
    }

    /**
     * Override to support Actor class name.
     */
    @Override
    protected PropMap getPropMapForArchiver(PropArchiver propArchiver)
    {
        PropMap propMap = super.getPropMapForArchiver(propArchiver);
        if (_actorClassName != null && !_actorClassName.equals(Actor.class.getName()))
            propMap.setPropValue("ActorClass", _actorClassName);
        return propMap;
    }

    /**
     * Override to support Actor class name.
     */
    @Override
    protected void setPropMapForArchiver(PropArchiver propArchiver, PropMap propMap)
    {
        super.setPropMapForArchiver(propArchiver, propMap);
        _actorClassName = (String) propMap.getPropValue("ActorClass");
        if (_actorClassName != null) {
            _actor = (Actor) getInstanceForClassName(propArchiver.getOwnerClass(), _actorClassName);
            if (_actor == null) _actor = new Actor();
            _actor._actorView = this;
        }
    }

    /**
     * Returns an actor instance for given class name.
     */
    public static Object getInstanceForClassName(Class<?> ownerClass, String className)
    {
        ClassLoader classLoader = ownerClass != null ? ownerClass.getClassLoader() : ViewArchiver.class.getClassLoader();
        try { return Class.forName(className, false, classLoader).getConstructor().newInstance(); }
        catch (Exception ignored) { }
        try { return Class.forName("snap.games." + className, false, classLoader).getConstructor().newInstance(); }
        catch (Exception e) { System.err.println("ActorView: Can't find actor class: " + className); return null; }
    }
}
