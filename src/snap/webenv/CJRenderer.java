/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.webenv;
import snap.gfx.Color;
import snap.gfx.Image;
import snap.gfx.Painter;
import snap.gfx3d.*;
import snap.util.SnapUtils;
import snap.webapi.*;
import java.util.HashMap;
import java.util.Map;

/**
 * This Renderer subclass supports WebGL rendering.
 */
public class CJRenderer extends Renderer {

    // The canvas
    private HTMLCanvasElement _canvas;

    // The WebGLRenderingContext
    protected WebGLRenderingContext _gl;

    // A map of shader programs
    private Map<String,WebGLProgram>  _programs = new HashMap<>();

    // A map of vertex shaders
    private Map<String, WebGLShader>  _vertShaders = new HashMap<>();

    // A map of fragment shaders
    private Map<String,WebGLShader>  _fragShaders = new HashMap<>();

    // A map of textures
    private Map<Texture, WebGLTexture>  _textures = new HashMap<>();

    // Canvas size in points
    private int  _canvasW, _canvasH;

    // Wrapper image
    private Image _image;

    /**
     * Constructor.
     */
    public CJRenderer(Camera aCamera)
    {
        super(aCamera);
    }

    /**
     * Initialize canvas and context (HTMLCanvasElement and WebGLRenderingContext).
     */
    private void initCanvas(CJPainter aPainter)
    {
        // If Canvas already created, just bail (shouldn't be possible)
        if (_canvas != null) return;

        // Create canvas and size
        _canvas = (HTMLCanvasElement) HTMLDocument.getDocument().createElement("canvas");
        resizeCanvas(aPainter);

        // Get WebGL context (if missing, complain and return)
        _gl = (WebGLRenderingContext) _canvas.getContext("webgl");
        if (_gl == null) {
            System.err.println("CJRenderer.initRenderer: canvas getContext() returned null");
            return;
        }

        // Initialize OpenGL
        _gl.clearColor(0f, 0f, 0f, 0f);
        _gl.enable(_gl.DEPTH_TEST);
        _gl.enable(_gl.CULL_FACE);
    }

    /**
     * Resize Canvas.
     */
    private void resizeCanvas(CJPainter aPainter)
    {
        // If Camera.ViewSize matches canvas size, just return - Should probably be checking scale, too
        Camera camera = getCamera();
        int viewW = (int) Math.round(camera.getViewWidth());
        int viewH = (int) Math.round(camera.getViewHeight());
        if (viewW == _canvasW && viewH == _canvasH)
            return;

        // Get Canvas size in points and pixels
        _canvasW = viewW;
        _canvasH = viewH;
        int scale = aPainter._scale;
        int canvasPixW = _canvasW * scale;
        int canvasPixH = _canvasH * scale;

        // Set Canvas size in points and pixels
        _canvas.setWidth(canvasPixW);
        _canvas.setHeight(canvasPixH);
        _canvas.getStyle().setProperty("width", _canvasW + "px");
        _canvas.getStyle().setProperty("height", _canvasH + "px");

        // If Context already around, resize viewport
        if (_gl != null)
            _gl.viewport(0, 0, canvasPixW, canvasPixH);

        // Set image
        _image = new CJImage(_canvas, _canvasW, _canvasH, aPainter._scale);
    }

    /**
     * Override to return name.
     */
    @Override
    public String getName()  { return "WebGL"; }

    /**
     * Override to render.
     */
    @Override
    public void renderAndPaint(Painter aPainter)
    {
        CJPainter painter;
        if (aPainter instanceof CJPainter2)
            painter = ((CJPainter2) aPainter).getPainter();
        else painter = (CJPainter) aPainter;

        // Make sure OpenGL is initialized
        if (_gl == null) {
            initCanvas(painter);
            if (_gl == null)
                return;
        }

        // Make sure canvas is still right size
        else resizeCanvas(painter);

        // Get GL and clear
        _gl.clear(_gl.COLOR_BUFFER_BIT | _gl.DEPTH_BUFFER_BIT);

        // Iterate over scene shapes and render each
        Scene3D scene = getScene();
        renderShape3D(scene);

        // Paint WebGL canvas to painter
        aPainter.drawImage(_image, 0, 0);
    }

    /**
     * Renders a Shape3D.
     */
    protected void renderShape3D(Shape3D aShape3D)
    {
        // If shape not visible, just return
        if (!aShape3D.isVisible())
            return;

        // Handle Parent: Iterate over children and recurse
        if (aShape3D instanceof ParentShape) {
            ParentShape parentShape = (ParentShape) aShape3D;
            Shape3D[] children = parentShape.getChildren();
            for (Shape3D child : children)
                renderShape3D(child);
        }

        // Handle child: Get VertexArray and render
        else {
            VertexArray triangleArray = aShape3D.getTriangleArray();
            while (triangleArray != null) {
                renderTriangleArray(triangleArray);
                triangleArray = triangleArray.getNext();
            }
        }
    }

    /**
     * Renders a VertexBuffer of triangles.
     */
    protected void renderTriangleArray(VertexArray aTriangleArray)
    {
        // If VertexArray.DoubleSided, disable face culling
        boolean doubleSided = aTriangleArray.isDoubleSided();
        if (doubleSided)
            _gl.disable(_gl.CULL_FACE);

        // Get ShaderProgram
        WebGLProgram program = getProgram(aTriangleArray);

        // Use this program
        _gl.useProgram(program);

        // Set program Projection Matrix (was program.setProjectionMatrix(projMatrix) )
        Camera camera = getCamera();
        double[] projMatrix = camera.getCameraToClipArray();
        Float32Array matrix4fv = new Float32Array(projMatrix);
        WebGLUniformLocation projMatrixUniform = _gl.getUniformLocation(program, "projMatrix");
        _gl.uniformMatrix4fv(projMatrixUniform, false, matrix4fv);

        // Set program ViewMatrix (was program.setViewMatrix(viewMatrix) )
        double[] sceneToCamera = camera.getSceneToCameraArray();
        Float32Array viewMatrix4fv = new Float32Array(sceneToCamera);
        WebGLUniformLocation viewMatrixUniform = _gl.getUniformLocation(program, "viewMatrix");
        _gl.uniformMatrix4fv(viewMatrixUniform, false, viewMatrix4fv);

        // Create/bind pointBuffer
        WebGLBuffer pointBuffer = _gl.createBuffer();
        _gl.bindBuffer(_gl.ARRAY_BUFFER, pointBuffer);

        // Buffer pointArray
        float[] pointArray = aTriangleArray.getPointArray();
        _gl.bufferData(_gl.ARRAY_BUFFER, new Float32Array(pointArray), _gl.STATIC_DRAW);

        // Get, configure and enable vertPoint attribute
        int pointsAttrLoc = _gl.getAttribLocation(program, "vertPoint");
        _gl.vertexAttribPointer(pointsAttrLoc, 3, _gl.FLOAT, false, 3 * 4, 0);
        _gl.enableVertexAttribArray(pointsAttrLoc);

        // If color array present, set colors
        WebGLBuffer colorBuffer = null;
        int colorAttrLoc = 0;
        if (aTriangleArray.isColorArraySet()) {

            // Create/bind colorBuffer
            colorBuffer = _gl.createBuffer();
            _gl.bindBuffer(_gl.ARRAY_BUFFER, colorBuffer);

            // Buffer colorArray
            float[] colorArray = aTriangleArray.getColorArray();
            _gl.bufferData(_gl.ARRAY_BUFFER, new Float32Array(colorArray), _gl.STATIC_DRAW);

            // Get, configure and enable vertColor attribute
            colorAttrLoc = _gl.getAttribLocation(program, "vertColor");
            _gl.vertexAttribPointer(colorAttrLoc, 3, _gl.FLOAT, false, 3 * 4, 0);
            _gl.enableVertexAttribArray(colorAttrLoc);
        }

        // Otherwise, set VertexArray color (was program.setColor(color) )
        else {
            Color color = aTriangleArray.getColor(); if (color == null) color = Color.RED;
            WebGLUniformLocation vertColorLoc = _gl.getUniformLocation(program, "vertColor");
            _gl.uniform3f(vertColorLoc, (float) color.getRed(), (float) color.getGreen(), (float) color.getBlue());
        }

        // Set VertexShader texture coords
        WebGLBuffer texCoordBuffer = null;
        int texCoordAttrLoc = 0;
        if (aTriangleArray.isTextureSetAndReady()) {

            // Get Texture, WebGLTexture
            Texture texture = aTriangleArray.getTexture();
            WebGLTexture wglTexture = getTexture(texture);

            // Bind and activate texture
            _gl.bindTexture(_gl.TEXTURE_2D, wglTexture);
            _gl.activeTexture(_gl.TEXTURE0);

            // Map texture
            WebGLUniformLocation textureUniform = _gl.getUniformLocation(program, "fragTexture");
            _gl.uniform1i(textureUniform, 0);

            // Create/bind texCoordBuffer
            texCoordBuffer = _gl.createBuffer();
            _gl.bindBuffer(_gl.ARRAY_BUFFER, texCoordBuffer);

            // Buffer texCoordArray
            float[] texCoordArray = aTriangleArray.getTexCoordArray();
            _gl.bufferData(_gl.ARRAY_BUFFER, new Float32Array(texCoordArray), _gl.STATIC_DRAW);

            // Get, configure and enable vertTexCoord attribute
            texCoordAttrLoc = _gl.getAttribLocation(program, "vertTexCoord");
            _gl.vertexAttribPointer(texCoordAttrLoc, 2, _gl.FLOAT, false, 2 * 4, 0);
            _gl.enableVertexAttribArray(texCoordAttrLoc);
        }

        // Set IndexArray
        WebGLBuffer indexBuffer = null;
        if (aTriangleArray.isIndexArraySet()) {

            // Create/bind indexBuffer
            indexBuffer = _gl.createBuffer();
            _gl.bindBuffer(_gl.ELEMENT_ARRAY_BUFFER, indexBuffer);

            // Buffer indexArray
            int[] indexArray = aTriangleArray.getIndexArray();
            _gl.bufferData(_gl.ELEMENT_ARRAY_BUFFER, new Uint16Array(indexArray), _gl.STATIC_DRAW);

            // Draw elements
            _gl.drawElements(_gl.TRIANGLES, indexArray.length, _gl.UNSIGNED_SHORT, 0);
        }

        // Run program
        else {
            int vertexCount = pointArray.length / 3;
            _gl.drawArrays(_gl.TRIANGLES, 0, vertexCount);
        }

        // Delete buffers
        _gl.deleteBuffer(pointBuffer);
        _gl.disableVertexAttribArray(pointsAttrLoc);
        if (colorBuffer != null) {
            _gl.deleteBuffer(colorBuffer);
            _gl.disableVertexAttribArray(colorAttrLoc);
        }
        if (texCoordBuffer != null) {
            _gl.deleteBuffer(texCoordBuffer);
            _gl.disableVertexAttribArray(texCoordAttrLoc);
        }
        if (indexBuffer != null)
            _gl.deleteBuffer(indexBuffer);

        // Restore
        if (doubleSided)
            _gl.enable(_gl.CULL_FACE);
    }

    /**
     * Returns a ShaderProgram for VertexArray.
     */
    public WebGLProgram getProgram(VertexArray aVertexArray)
    {
        // If shader exists, return
        String name = getShaderString(aVertexArray);
        WebGLProgram program = _programs.get(name);
        if (program != null)
            return program;

        // Create, set and return
        program = _gl.createProgram();
        WebGLShader vertexShader = getVertexShader(name);
        WebGLShader fragmentShader = getFragmentShader(name);
        _gl.attachShader(program, vertexShader);
        _gl.attachShader(program, fragmentShader);

        // Link Program
        _gl.linkProgram(program);

        // Validate
        _gl.validateProgram(program);
        //JSObject linkStatus = _gl.getProgramParameter(program, _gl.LINK_STATUS);
        //if ( ! linkStatus) {
        //    var info = gl.getProgramInfoLog(program);
        //    throw 'Could not compile WebGL program. \n\n' + info;
        //}

        _programs.put(name, program);
        return program;
    }

    /**
     * Returns a VertexShader for given VertexArray.
     */
    public WebGLShader getVertexShader(String name)
    {
        // If shader exists, return
        WebGLShader shader = _vertShaders.get(name);
        if (shader != null)
            return shader;

        // Get shader source
        String sourceText = getSourceText(_gl.VERTEX_SHADER, name);

        // Create WebGLShader and set source
        shader = _gl.createShader(_gl.VERTEX_SHADER);
        _gl.shaderSource(shader, sourceText);

        // Compile
        _gl.compileShader(shader);

        // Add to VertShaders map and return
        _vertShaders.put(name, shader);
        return shader;
    }

    /**
     * Returns a Fragment Shader for given VertexArray.
     */
    public WebGLShader getFragmentShader(String name)
    {
        // If shader exists, return
        WebGLShader shader = _fragShaders.get(name);
        if (shader != null)
            return shader;

        // Get shader source
        String sourceText = getSourceText(_gl.FRAGMENT_SHADER, name);

        // Create WebGLShader and set source
        shader = _gl.createShader(_gl.FRAGMENT_SHADER);
        _gl.shaderSource(shader, sourceText);

        // Compile
        _gl.compileShader(shader);

        // Add to VertShaders map and return
        _fragShaders.put(name, shader);
        return shader;
    }

    /**
     * Returns a WebGL texture for given Snap texture.
     */
    public WebGLTexture getTexture(Texture aTexture)
    {
        // Get from Textures map (Just return if found)
        WebGLTexture wglTexture = _textures.get(aTexture);
        if (wglTexture != null)
            return wglTexture;

        // Get BufferedImage and flip for OpenGL
        CJImage image = (CJImage) aTexture.getImage();
        HTMLCanvasElement canvas = image.getCanvas();
        _gl.pixelStorei(_gl.UNPACK_FLIP_Y_WEBGL, 1); //ImageUtil.flipImageVertically(canvas);

        // Create texture for canvas
        wglTexture = _gl.createTexture();
        _gl.bindTexture(_gl.TEXTURE_2D, wglTexture);
        _gl.texParameteri(_gl.TEXTURE_2D, _gl.TEXTURE_WRAP_S, _gl.CLAMP_TO_EDGE);
        _gl.texParameteri(_gl.TEXTURE_2D, _gl.TEXTURE_WRAP_T, _gl.CLAMP_TO_EDGE);
        _gl.texParameteri(_gl.TEXTURE_2D, _gl.TEXTURE_MIN_FILTER, _gl.LINEAR);
        _gl.texParameteri(_gl.TEXTURE_2D, _gl.TEXTURE_MAG_FILTER, _gl.LINEAR);
        _gl.texImage2D(_gl.TEXTURE_2D, 0, _gl.RGBA, _gl.RGBA, _gl.UNSIGNED_BYTE, canvas);

        // Unbind
        _gl.bindTexture(_gl.TEXTURE_2D, null);

        // Add to textures map and return
        _textures.put(aTexture, wglTexture);
        return wglTexture;
    }

    /**
     * Returns a unique string.
     */
    public String getShaderString(VertexArray aVertexArray)
    {
        // Handle Textures
        boolean hasTexCoords = aVertexArray.getTexCoordArray().length > 0;
        if (hasTexCoords)
            return "Points_Color_Tex";

        // Handle Color/Colors
        boolean hasColors = aVertexArray.isColorArraySet();
        return hasColors ? "Points_Colors" : "Points_Color";
    }

    /**
     * Returns the full text string of shader file.
     */
    public String getSourceText(int aType, String aName)
    {
        String sourcePath = "shaders/" + getSourceName(aType, aName);
        String sourceText = SnapUtils.getText(getClass(), sourcePath);
        if (sourceText == null || sourceText.length() == 0)
            System.err.println("CJRenderer.getSourceText: shader source not found: " + sourcePath);
        return sourceText;
    }

    /**
     * Returns the shader file name.
     */
    public String getSourceName(int aType, String aName)
    {
        // Handle Vertex Shaders:
        if (aType == _gl.VERTEX_SHADER) {
            switch (aName) {
                case "Points_Color": return "Points_Color.vs";
                case "Points_Colors": return "Points_Colors.vs";
                case "Points_Color_Tex": return "Points_Color_Tex.vs";
            }
        }

        // Handle Fragment Shaders
        if (aType == _gl.FRAGMENT_SHADER) {
            if (aName.equals("Points_Color_Tex"))
                return "Points_Color_Tex.fs";
            return "General.fs";
        }

        // Something went wrong
        return null;
    }

    /**
     * Registers factory.
     */
    public static void registerFactory()
    {
        // If already set, just return
        for (RendererFactory factory : RendererFactory.getFactories())
            if (factory.getClass() == CJRendererFactory.class)
                return;

        // Create, add and setDefault
        RendererFactory joglFactory = new CJRendererFactory();
        RendererFactory.addFactory(joglFactory);
        RendererFactory.setDefaultFactory(joglFactory);
    }

    /**
     * A RendererFactory implementation for RendererJogl.
     */
    public static class CJRendererFactory extends RendererFactory {

        /**
         * Returns the renderer name.
         */
        public String getRendererName()  { return "WebGL"; }

        /**
         * Returns a new default renderer.
         */
        public Renderer newRenderer(Camera aCamera)
        {
            return new CJRenderer(aCamera);
        }
    }
}
