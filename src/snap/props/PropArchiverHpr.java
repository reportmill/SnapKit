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
import snap.util.SnapUtils;

/**
 * This class helps PropArchiver archive some common SnapKit classes.
 */
public class PropArchiverHpr {

    // The PropArchiver associated with this instance
    private PropArchiver  _archiver;

    // Add classes
    static
    {
        Prop.addRelationClass(Font.class);
        Prop.addRelationClass(Paint.class);
        Prop.addRelationClass(TextFormat.class);
        Prop.addRelationClass(Image.class);
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
        if (anObj instanceof Font)
            return new FontProxy((Font) anObj);
        if (anObj instanceof Color)
            return new ColorProxy((Color) anObj);
        if (anObj instanceof NumberFormat)
            return new NumberFormatProxy((NumberFormat) anObj);
        if (anObj instanceof Image)
            return new ImageProxy((Image) anObj);
        return null;
    }

    /**
     * Returns a PropObjectProxy for given class, if supported.
     */
    public PropObject getProxyForClass(Class<?> aClass)
    {
        if (Image.class.isAssignableFrom(aClass))
            return new ImageProxy(null);
        return null;
    }

    /**
     * A PropObjectProxy subclass for Font.
     */
    private static class FontProxy extends PropObjectProxy {

        // The font
        private Font  _font;

        // Constants for properties
        public static final String Name_Prop = Font.Name_Prop;
        public static final String Size_Prop = Font.Size_Prop;

        /**
         * Constructor.
         */
        public FontProxy(Font aFont)
        {
            _font = aFont;
        }

        /**
         * Override to return Font.
         */
        @Override
        public Object getReal()  { return _font; }

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
                case Name_Prop: return _font.getNameEnglish();
                case Size_Prop: return _font.getSize();
                default: return super.getPropValue(aPropName);
            }
        }

        @Override
        public void setPropValue(String aPropName, Object aValue)
        {
            switch (aPropName) {
                case Name_Prop: _font = new Font(SnapUtils.stringValue(aValue), 12); break;
                case Size_Prop: _font = _font.deriveFont(SnapUtils.doubleValue(aValue)); break;
                default: super.setPropValue(aPropName, aValue);
            }
        }
    }

    /**
     * A PropObjectProxy subclass for Color.
     */
    private static class ColorProxy extends PropObjectProxy {

        // The color
        private Color  _color;

        // Constants for properties
        public static final String Color_Prop = "Color";

        /**
         * Constructor.
         */
        public ColorProxy(Color aColor)
        {
            _color = aColor;
        }

        /**
         * Override to return Color.
         */
        @Override
        public Object getReal()  { return _color; }

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
                case Color_Prop: return '#' + _color.toHexString();
                default: return super.getPropValue(aPropName);
            }
        }

        @Override
        public void setPropValue(String aPropName, Object aValue)
        {
            switch (aPropName) {
                case Color_Prop: _color = Color.get(SnapUtils.stringValue(aValue)); break;
                default: super.setPropValue(aPropName, aValue);
            }
        }
    }

    /**
     * A PropObjectProxy subclass for NumberFormat.
     */
    private static class NumberFormatProxy extends PropObjectProxy {

        // The number format
        private NumberFormat  _format;

        // Constants for properties
        public static final String Pattern_Prop = NumberFormat.Pattern_Prop;
        public static final String ExpStyle_Prop = NumberFormat.ExpStyle_Prop;

        // Constants for defaults
        private static final String DEFAULT_PATTERN = "";
        private static final NumberFormat.ExpStyle DEFAULT_EXP_STYLE = NumberFormat.ExpStyle.None;

        /**
         * Constructor.
         */
        public NumberFormatProxy(NumberFormat aFormat)
        {
            _format = aFormat;
        }

        /**
         * Override to return Format.
         */
        @Override
        public Object getReal()  { return _format; }

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
                case Pattern_Prop: return _format.getPattern();
                case ExpStyle_Prop: return _format.getExpStyle();
                default: return super.getPropValue(aPropName);
            }
        }

        @Override
        public void setPropValue(String aPropName, Object aValue)
        {
            switch (aPropName) {
                case Pattern_Prop:
                    String pattern = SnapUtils.stringValue(aValue);
                    _format.setPattern(pattern);
                    break;
                case ExpStyle_Prop:
                    NumberFormat.ExpStyle expStyle = (NumberFormat.ExpStyle) aValue;
                    _format = _format.copyForProps(ExpStyle_Prop, expStyle);
                    break;
                default: super.setPropValue(aPropName, aValue);
            }
        }
    }

    /**
     * A PropObjectProxy subclass for Image.
     */
    private class ImageProxy extends PropObjectProxy {

        // The image
        private Image  _image;

        // The image name
        private String  _name;

        // Constants for properties
        public static final String Name_Prop = "Name";

        /**
         * Constructor.
         */
        public ImageProxy(Image anImage)
        {
            _image = anImage;

            if (_image != null) {
                _name = _image.getName();
                if (_name == null)
                    _name = String.valueOf(System.identityHashCode(_image));
                byte[] bytes = _image.getBytes();
                _archiver.addResource(_name, bytes);
            }
        }

        /**
         * Override to return Image.
         */
        @Override
        public Object getReal()
        {
            // If already set, just return
            if (_image != null) return _image;

            // Get named resource
            PropArchiver.Resource resource = _archiver.getResourceForName(_name);
            if (resource == null) {
                System.out.println("PropArchiverHpr.ImageProxy: Archiver resource not found for image name: " + _name);
                return null;
            }

            // Get byte and image
            byte[] bytes = resource.getBytes();
            Image image = Image.getImageForBytes(bytes);

            // Set/return
            return _image = image;
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
                case Name_Prop: _name = SnapUtils.stringValue(aValue); break;
                default: super.setPropValue(aPropName, aValue);
            }
        }
    }
}
