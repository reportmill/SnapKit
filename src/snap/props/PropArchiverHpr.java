/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.props;
import snap.gfx.Color;
import snap.gfx.Font;
import snap.gfx.Image;
import snap.gfx.Paint;
import snap.text.NumberFormat;
import snap.text.TextFormat;
import snap.util.Convert;
import java.util.HashMap;
import java.util.Map;

/**
 * This class helps PropArchiver archive some common SnapKit classes.
 */
public class PropArchiverHpr {

    // The PropArchiver associated with this instance
    private PropArchiver  _archiver;

    // A Map of PropObjectProxy classes.
    private static Map<Class<?>, Class<? extends PropObjectProxy<?>>> _proxyClasses = new HashMap<>();

    // Add classes
    static
    {
        setProxyClassForClass(Font.class, FontProxy.class);
        setProxyClassForClass(Paint.class, ColorProxy.class);
        setProxyClassForClass(TextFormat.class, NumberFormatProxy.class);
        setProxyClassForClass(Image.class, ImageProxy.class);
    }

    /**
     * Constructor.
     */
    public PropArchiverHpr(PropArchiver anArchiver)
    {
        _archiver = anArchiver;
    }

    /**
     * Returns a PropObjectProxy for given object, if supported.
     */
    public PropObject getProxyForObject(Object anObj)
    {
        Class<?> objClass = anObj.getClass();
        PropObjectProxy<Object> proxy = getProxyForClass(objClass);
        if (proxy != null)
            proxy.setReal(anObj);
        return proxy;
    }

    /**
     * Returns a PropObjectProxy for given class, if supported.
     */
    public PropObjectProxy<Object> getProxyForClass(Class<?> aClass)
    {
        Class<? extends PropObjectProxy<?>> propObjClass = getProxyClassForClass(aClass);
        if (propObjClass == null)
            return null;

        // Return new instance
        try { return (PropObjectProxy<Object>) propObjClass.newInstance(); }
        catch (InstantiationException | IllegalAccessException e) { throw new RuntimeException(e + " for: " + aClass); }
    }

    /**
     * Returns a ProxyPropObject class for given class, if set.
     */
    public static Class<? extends PropObjectProxy<?>> getProxyClassForClass(Class<?> aClass)
    {
        // Iterate over key classes - if matching class, get proxy
        for (Class<?> cls : _proxyClasses.keySet()) {
            if (cls.isAssignableFrom(aClass)) {
                Class<? extends PropObjectProxy<?>> proxyClass = _proxyClasses.get(cls);
                return proxyClass;
            }
        }

        // Return not found
        return null;
    }

    /**
     * Sets a ProxyPropObject class for given class, if set.
     */
    public static void setProxyClassForClass(Class<?> aClass, Class<? extends PropObjectProxy<?>> aProxyClass)
    {
        _proxyClasses.put(aClass, aProxyClass);
    }

    /**
     * A PropObjectProxy subclass for Font.
     */
    private static class FontProxy extends PropObjectProxy<Font> {

        // Constants for properties
        public static final String Name_Prop = Font.Name_Prop;
        public static final String Size_Prop = Font.Size_Prop;

        /**
         * Constructor.
         */
        public FontProxy()
        {
            super();
        }

        @Override
        protected void initProps(PropSet aPropSet)
        {
            super.initProps(aPropSet);
            aPropSet.addPropNamed(Name_Prop, String.class, null);
            aPropSet.addPropNamed(Size_Prop, double.class, null);
        }

        @Override
        public Object getPropValue(String aPropName)
        {
            switch (aPropName) {
                case Name_Prop: return _real.getNameEnglish();
                case Size_Prop: return _real.getSize();
                default: return super.getPropValue(aPropName);
            }
        }

        @Override
        public void setPropValue(String aPropName, Object aValue)
        {
            switch (aPropName) {
                case Name_Prop: _real = new Font(Convert.stringValue(aValue), 12); break;
                case Size_Prop: _real = _real.deriveFont(Convert.doubleValue(aValue)); break;
                default: super.setPropValue(aPropName, aValue);
            }
        }
    }

    /**
     * A PropObjectProxy subclass for Color.
     */
    private static class ColorProxy extends PropObjectProxy<Color> {

        // Constants for properties
        public static final String Color_Prop = "Color";

        /**
         * Constructor.
         */
        public ColorProxy()
        {
            super();
        }

        @Override
        protected void initProps(PropSet aPropSet)
        {
            super.initProps(aPropSet);
            aPropSet.addPropNamed(Color_Prop, String.class, null);
        }

        @Override
        public Object getPropValue(String aPropName)
        {
            switch (aPropName) {
                case Color_Prop: return '#' + _real.toHexString();
                default: return super.getPropValue(aPropName);
            }
        }

        @Override
        public void setPropValue(String aPropName, Object aValue)
        {
            switch (aPropName) {
                case Color_Prop: _real = Color.get(Convert.stringValue(aValue)); break;
                default: super.setPropValue(aPropName, aValue);
            }
        }
    }

    /**
     * A PropObjectProxy subclass for NumberFormat.
     */
    private static class NumberFormatProxy extends PropObjectProxy<NumberFormat> {

        // The pattern
        private String  _pattern;

        // The Exponent style
        private NumberFormat.ExpStyle  _expStyle;

        // Constants for properties
        public static final String Pattern_Prop = NumberFormat.Pattern_Prop;
        public static final String ExpStyle_Prop = NumberFormat.ExpStyle_Prop;

        // Constants for defaults
        private static final String DEFAULT_PATTERN = "";
        private static final NumberFormat.ExpStyle DEFAULT_EXP_STYLE = NumberFormat.ExpStyle.None;

        /**
         * Constructor.
         */
        public NumberFormatProxy()
        {
            super();
        }

        @Override
        protected void initProps(PropSet aPropSet)
        {
            super.initProps(aPropSet);
            aPropSet.addPropNamed(Pattern_Prop, String.class, DEFAULT_PATTERN);
            aPropSet.addPropNamed(ExpStyle_Prop, NumberFormat.ExpStyle.class, DEFAULT_EXP_STYLE);
        }

        @Override
        public Object getPropValue(String aPropName)
        {
            switch (aPropName) {
                case Pattern_Prop: return _real.getPattern();
                case ExpStyle_Prop: return _real.getExpStyle();
                default: return super.getPropValue(aPropName);
            }
        }

        @Override
        protected NumberFormat getRealImpl()
        {
            return new NumberFormat(_pattern, _expStyle);
        }

        @Override
        public void setPropValue(String aPropName, Object aValue)
        {
            switch (aPropName) {
                case Pattern_Prop:
                    _pattern = Convert.stringValue(aValue);
                    break;
                case ExpStyle_Prop:
                    _expStyle = (NumberFormat.ExpStyle) aValue;
                    break;
                default: super.setPropValue(aPropName, aValue);
            }
        }
    }

    /**
     * A PropObjectProxy subclass for Image.
     */
    private class ImageProxy extends PropObjectProxy<Image> {

        // The image name
        private String  _name;

        // Constants for properties
        public static final String Name_Prop = "Name";

        /**
         * Constructor.
         */
        public ImageProxy()
        {
            super();
        }

        /**
         * Override to return Image.
         */
        @Override
        public Image getRealImpl()
        {
            // Get named resource
            PropArchiver.Resource resource = _archiver.getResourceForName(_name);
            if (resource == null) {
                System.out.println("PropArchiverHpr.ImageProxy: Archiver resource not found for image name: " + _name);
                return null;
            }

            // Get byte and image
            byte[] bytes = resource.getBytes();
            Image image = Image.getImageForBytes(bytes);

            // Return
            return image;
        }

        @Override
        public void setReal(Image aReal)
        {
            // Do normal version
            super.setReal(aReal);
            if (aReal == null) return;

            // Get Image Name
            _name = aReal.getName();
            if (_name == null)
                _name = String.valueOf(System.identityHashCode(aReal));

            // Add Image Bytes to Archiver.Resources
            byte[] bytes = aReal.getBytes();
            _archiver.addResource(_name, bytes);
        }

        @Override
        protected void initProps(PropSet aPropSet)
        {
            super.initProps(aPropSet);
            aPropSet.addPropNamed(Name_Prop, String.class, null);
        }

        @Override
        public Object getPropValue(String aPropName)
        {
            switch (aPropName) {
                case Name_Prop: return _name;
                default: return super.getPropValue(aPropName);
            }
        }

        @Override
        public void setPropValue(String aPropName, Object aValue)
        {
            switch (aPropName) {
                case Name_Prop: _name = Convert.stringValue(aValue); break;
                default: super.setPropValue(aPropName, aValue);
            }
        }
    }
}
