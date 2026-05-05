package snap.games;
import snap.util.XMLArchiver;
import snap.util.XMLElement;
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
     * Override to archive X/Y and ImageName.
     */
    @Override
    protected XMLElement toXMLView(XMLArchiver anArchiver)
    {
        XMLElement xml = super.toXMLView(anArchiver);
        xml.setName("ActorView");

        // Archive Actor class name
        if (_actorClassName != null && !_actorClassName.equals(Actor.class.getName()))
            xml.add("ActorClass", _actorClassName);

        // Return
        return xml;
    }

    /**
     * Override to support image name.
     */
    @Override
    protected void fromXMLView(XMLArchiver anArchiver, XMLElement anElement)
    {
        // Create Actor class
        _actorClassName = anElement.getAttributeValue("ActorClass");
        if (_actorClassName != null) {
            _actor = (Actor) getInstanceForClassName(anArchiver.getOwnerClass(), _actorClassName);
            if (_actor == null) _actor = new Actor();
            _actor._actorView = this;
        }

        super.fromXMLView(anArchiver, anElement);
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
