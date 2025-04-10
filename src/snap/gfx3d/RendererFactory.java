package snap.gfx3d;
import snap.util.ArrayUtils;
import snap.util.SnapEnv;

import java.util.ArrayList;
import java.util.List;

/**
 * Used to create new renderers.
 */
public abstract class RendererFactory {

    // Default RendererFactory
    private static RendererFactory  _defaultFactory;

    // All RendererFactories
    private static RendererFactory[]  _factories;

    /**
     * Returns the renderer name.
     */
    public abstract String getRendererName();

    /**
     * Returns a new default renderer.
     */
    public abstract Renderer newRenderer(Camera aCamera);

    /**
     * Returns a new default renderer.
     */
    public static Renderer newDefaultRenderer(Camera aCamera)
    {
        RendererFactory defaultFactory = getDefaultFactory();
        if (defaultFactory != null)
            return defaultFactory.newRenderer(aCamera);

        return new Renderer2D(aCamera);
    }

    /**
     * Returns the default renderer.
     */
    public static RendererFactory getDefaultFactory()
    {
        // If set, just return
        if (_defaultFactory != null) return _defaultFactory;

        // Try to find factory
        RendererFactory[] knownFactories = getFactories();
        RendererFactory knownFactory = knownFactories.length > 0 ? knownFactories[0] : null;

        // Set and return
        return _defaultFactory = knownFactory;
    }

    /**
     * Sets a default renderer.
     */
    public static void setDefaultFactory(RendererFactory aRenderer)
    {
        _defaultFactory = aRenderer;
    }

    /**
     * Returns all factories.
     */
    public static RendererFactory[] getFactories()
    {
        if (_factories != null) return _factories;

        List<RendererFactory> factories = new ArrayList<>();

        // If desktop, try to add Jogl
        if (SnapEnv.isDesktop) try {
            String JOGL_RENDERER_FACTORY = "snapgl.JGLRenderer$JGLRendererFactory";
            Class<? extends RendererFactory> joglRendererFactoryClass = (Class<? extends RendererFactory>) Class.forName(JOGL_RENDERER_FACTORY);
            RendererFactory joglRendererFactory = joglRendererFactoryClass.newInstance();
            factories.add(joglRendererFactory);
        }
        catch (Exception ignore) { }

        // Add Renderer2D
        RendererFactory renderer2dFactory = new Renderer2D.Renderer2DFactory();
        factories.add(renderer2dFactory);

        // Return
        return _factories = factories.toArray(new RendererFactory[0]);
    }

    /**
     * Adds a factory.
     */
    public static void addFactory(RendererFactory aFactory)
    {
        // If already added, just return
        RendererFactory[] factories = getFactories();
        boolean containsFactory = ArrayUtils.findMatch(factories, factory -> factory.getClass().isInstance(aFactory)) != null;
        if (containsFactory) {
            System.out.println("RenderFactory.addFactory: Factory already added for type: " + aFactory.getRendererName());
            return;
        }

        // Add factory
        _factories = ArrayUtils.addUnique(_factories, aFactory);
    }

    /**
     * Returns the names of all factories.
     */
    public static String[] getFactoryNames()
    {
        RendererFactory[] factories = getFactories();
        String[] names = new String[factories.length];
        for (int i = 0; i < factories.length; i++)
            names[i] = factories[i].getRendererName();
        return names;
    }

    /**
     * Returns the RendererFactory for given name.
     */
    public static RendererFactory getFactoryForName(String aName)
    {
        RendererFactory[] factories = getFactories();
        for (RendererFactory factory : factories)
            if (factory.getRendererName().equals(aName))
                return factory;
        return null;
    }
}
