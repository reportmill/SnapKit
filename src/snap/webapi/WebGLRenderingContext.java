package snap.webapi;

/**
 * This class is a wrapper for Web API WebGLRenderingContext (https://developer.mozilla.org/en-US/docs/Web/API/WebGLRenderingContext).
 */
public class WebGLRenderingContext extends JSProxy implements GL2 {

    /**
     * Constructor.
     */
    public WebGLRenderingContext(Object contextJS)
    {
        super(contextJS);
    }

    public void activeTexture(int textureId)  { call("activeTexture", textureId); }

    public void attachShader(WebGLProgram aProgram, WebGLShader aShader)  { call("attachShader", aProgram._jsObj, aShader._jsObj); }

    public void bindBuffer(int var1, WebGLBuffer aBuffer)  { call("bindBuffer", var1, aBuffer._jsObj); }

    //void bindAttribLocation(WebGLProgram var1, int var2, String var3);
    //void bindFramebuffer(int var1, WebGLFramebuffer var2);
    //void bindRenderbuffer(int var1, WebGLRenderbuffer var2);
    public void bindTexture(int var1, WebGLTexture aTexture)
    {
        Object textureJS = aTexture != null ? aTexture._jsObj : null;
        call("bindTexture", var1, textureJS);
    }

    public void bufferData(int var1, TypedArray typedArray, int var3)
    {
        call("bufferData", var1, typedArray._jsObj, var3);
    }

    //void bufferData(int var1, int var2, int var3);
    //void bufferData(int var1, ArrayBuffer var2, int var3);
    //void bufferSubData(int var1, int var2, ArrayBufferView var3);
    //void bufferSubData(int var1, int var2, ArrayBuffer var3);

    public void clear(int var1)  { call("clear", var1); }

    public void clearColor(float red, float green, float blue, float alpha)
    {
        call("clearColor", red, green, blue, alpha);
    }

    //void clearDepth(float var1);
    //void clearStencil(int var1);

    public WebGLBuffer createBuffer()
    {
        Object bufferJS = call("createBuffer");
        return new WebGLBuffer(bufferJS);
    }

    //WebGLFramebuffer createFramebuffer();
    public WebGLProgram createProgram()
    {
        Object programJS = call("createProgram");
        return new WebGLProgram(programJS);
    }

    //WebGLRenderbuffer createRenderbuffer();

    public WebGLShader createShader(int var1)
    {
        Object shaderJS = call("createShader", var1);
        return new WebGLShader(shaderJS);
    }

    public WebGLTexture createTexture()
    {
        Object textureJS = call("createTexture");
        return new WebGLTexture(textureJS);
    }

    public void deleteBuffer(WebGLBuffer aBuffer)  { call("deleteBuffer", aBuffer._jsObj); }

    public void disable(int var1)  { call("disable", var1); }

    public void disableVertexAttribArray(int var1)  { call("disableVertexAttribArray", var1); }

    public void drawArrays(int var1, int var2, int var3)  { call("drawArrays", var1, var2, var3); }

    public void drawElements(int var1, int var2, int var3, int var4)  { call("drawElements", var1, var2, var3, var4); }

    public void enable(int var1)  { call("enable", var1); }

    public void enableVertexAttribArray(int var1)  { call("enableVertexAttribArray", var1); }

    public int getAttribLocation(WebGLProgram aProgram, String var2)
    {
        return (int) call("getAttribLocation", aProgram._jsObj, var2);
    }

    //JSObject getUniform(WebGLProgram var1, WebGLUniformLocation var2);

    public WebGLUniformLocation getUniformLocation(WebGLProgram program, String name)
    {
        Object uniformLocationJS = call("getUniformLocation", program._jsObj, name);
        return new WebGLUniformLocation(uniformLocationJS);
    }

    public void uniformMatrix4fv(WebGLUniformLocation uniformLocation, boolean var2, Float32Array floatArray)
    {
        call("uniformMatrix4fv", uniformLocation._jsObj, var2, floatArray._jsObj);
    }

    //void uniformMatrix2fv(WebGLUniformLocation var1, boolean var2, Float32Array var3);
    //void uniformMatrix2fv(WebGLUniformLocation var1, boolean var2, JSArrayReader<JSNumber> var3);
    //void uniformMatrix2fv(WebGLUniformLocation var1, boolean var2, @JSByRef float[] var3);
    //void uniformMatrix3fv(WebGLUniformLocation var1, boolean var2, Float32Array var3);
    //void uniformMatrix3fv(WebGLUniformLocation var1, boolean var2, JSArrayReader<JSNumber> var3);
    //void uniformMatrix3fv(WebGLUniformLocation var1, boolean var2, @JSByRef float[] var3);
    //void uniformMatrix4fv(WebGLUniformLocation var1, boolean var2, JSArrayReader<JSNumber> var3);
    //void uniformMatrix4fv(WebGLUniformLocation var1, boolean var2, @JSByRef float[] var3);

    public void useProgram(WebGLProgram var1)  { call("useProgram", var1._jsObj); }

    public void vertexAttribPointer(int var1, int var2, int var3, boolean var4, int var5, int var6)
    {
        call("vertexAttribPointer", var1, var2, var3, var4, var5, var6);
    }

    public void viewport(int aX, int aY, int aW, int aH)  { call("viewport", aX, aY, aW, aH); }

    //HTMLCanvasElement getCanvas();

    //int getDrawingBufferWidth();
    //int getDrawingBufferHeight();
    //WebGLContextAttributes getContextAttributes();
    //boolean isContextLost();
    //JSArrayReader<JSString> getSupportedExtensions();

    //String[] getSupportedExtensionArray();
    //JSObject getExtension(String var1);
    //void blendColor(float var1, float var2, float var3, float var4);
    //void blendEquation(int var1);
    //void blendEquationSeparate(int var1, int var2);
    //void blendFunc(int var1, int var2);
    //void blendFuncSeparate(int var1, int var2, int var3, int var4);
    //int checkFramebufferStatus(int var1);

    //void colorMask(boolean var1, boolean var2, boolean var3, boolean var4);

    public void compileShader(WebGLShader aShader)  { call("compileShader", aShader._jsObj); }

    //void compressedTexImage2D(int var1, int var2, int var3, int var4, int var5, int var6, ArrayBufferView var7);
    //void compressedTexSubImage2D(int var1, int var2, int var3, int var4, int var5, int var6, int var7, ArrayBufferView var8);
    //void copyTexImage2D(int var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8);
    //void copyTexSubImage2D(int var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8);
    //void cullFace(int var1);
    //void deleteFramebuffer(WebGLFramebuffer var1);
    //void deleteProgram(WebGLProgram var1);
    //void deleteRenderbuffer(WebGLRenderbuffer var1);
    //void deleteShader(WebGLShader var1);
    //void deleteTexture(WebGLTexture var1);

    public void depthFunc(int var1)  { call("depthFunc", var1); }

    //void depthMask(boolean var1);
    //void depthRange(float var1, float var2);
    //void detachShader(WebGLProgram var1, WebGLShader var2);
    //void finish();
    //void flush();
    //void framebufferRenderbuffer(int var1, int var2, int var3, WebGLRenderbuffer var4);
    //void framebufferTexture2D(int var1, int var2, int var3, WebGLTexture var4, int var5);
    //void frontFace(int var1);
    //void generateMipmap(int var1);
    //WebGLActiveInfo getActiveAttrib(WebGLProgram var1, int var2);
    //WebGLActiveInfo getActiveUniform(WebGLProgram var1, int var2);
    //JSArrayReader<WebGLShader> getAttachedShaders(WebGLProgram var1);
    //WebGLShader[] getAttachedShadersArray(WebGLProgram var1);
    //JSObject getBufferParameter(int var1, int var2);
    //JSObject getParameter(int var1);
    //int getParameteri(int var1);
    //String getParameterString(int var1);
    //float getParameterf(int var1);
    //int getError();
    //JSObject getFramebufferAttachmentParameter(int var1, int var2, int var3);
    //JSObject getProgramParameter(WebGLProgram var1, int var2);
    //boolean getProgramParameterb(WebGLProgram var1, int var2);
    //int getProgramParameteri(WebGLProgram var1, int var2);
    //String getProgramInfoLog(WebGLProgram var1);
    //JSObject getRenderbufferParameter(int var1, int var2);
    //JSObject getShaderParameter(WebGLShader var1, int var2);
    //boolean getShaderParameterb(WebGLShader var1, int var2);
    //int getShaderParameteri(WebGLShader var1, int var2);
    //WebGLShaderPrecisionFormat getShaderPrecisionFormat(int var1, int var2);
    //String getShaderInfoLog(WebGLShader var1);
    //String getShaderSource(WebGLShader var1);
    //JSObject getTexParameter(int var1, int var2);
    //JSObject getVertexAttrib(int var1, int var2);
    //int getVertexAttribOffset(int var1, int var2);
    //void hint(int var1, int var2);
    //boolean isBuffer(WebGLBuffer var1);
    //boolean isEnabled(int var1);
    //boolean isFramebuffer(WebGLFramebuffer var1);
    //boolean isProgram(WebGLProgram var1);
    //boolean isRenderbuffer(WebGLRenderbuffer var1);
    //boolean isShader(WebGLShader var1);
    //boolean isTexture(WebGLTexture var1);
    //void lineWidth(float var1);

    public void linkProgram(WebGLProgram aProgram)  { call("linkProgram", aProgram._jsObj); }

    public void pixelStorei(int var1, int var2)  { call("pixelStorei", var1, var2); }

    //void polygonOffset(float var1, float var2);
    //void readPixels(int var1, int var2, int var3, int var4, int var5, int var6, ArrayBufferView var7);
    //void renderbufferStorage(int var1, int var2, int var3, int var4);
    //void sampleCoverage(float var1, boolean var2);
    //void scissor(int var1, int var2, int var3, int var4);

    public void shaderSource(WebGLShader aShader, String var2)  { call("shaderSource", aShader._jsObj, var2); }

    //void stencilFunc(int var1, int var2, int var3);
    //void stencilFuncSeparate(int var1, int var2, int var3, int var4);
    //void stencilMask(int var1);
    //void stencilMaskSeparate(int var1, int var2);
    //void stencilOp(int var1, int var2, int var3);
    //void stencilOpSeparate(int var1, int var2, int var3, int var4);
    //void texImage2D(int var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8, ArrayBufferView var9);
    //void texImage2D(int var1, int var2, int var3, int var4, int var5, ImageData var6);
    //void texImage2D(int var1, int var2, int var3, int var4, int var5, HTMLImageElement var6);

    public void texImage2D(int var1, int var2, int var3, int var4, int var5, HTMLCanvasElement aCanvas)
    {
        call("texImage2D", var1, var2, var3, var4, var5, aCanvas._jsObj);
    }

    //void texParameterf(int var1, int var2, float var3);

    public void texParameteri(int var1, int var2, int var3)  { call("texParameteri", var1, var2, var3); }

    //void texSubImage2D(int var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8, ArrayBufferView var9);
    //void texSubImage2D(int var1, int var2, int var3, int var4, int var5, int var6, ImageData var7);
    //void texSubImage2D(int var1, int var2, int var3, int var4, int var5, int var6, HTMLImageElement var7);
    //void texSubImage2D(int var1, int var2, int var3, int var4, int var5, int var6, HTMLCanvasElement var7);

    public void uniform1f(WebGLUniformLocation uniformLocation, float var2)
    {
        call("uniform1f", uniformLocation._jsObj, var2);
    }

    //void uniform1fv(WebGLUniformLocation var1, Float32Array var2);
    //void uniform1fv(WebGLUniformLocation var1, JSArrayReader<JSNumber> var2);
    //void uniform1fv(WebGLUniformLocation var1, @JSByRef float[] var2);

    public void uniform1i(WebGLUniformLocation uniformLocation, int var2)
    {
        call("uniform1i", uniformLocation._jsObj, var2);
    }

    //void uniform1iv(WebGLUniformLocation var1, Int32Array var2);
    //void uniform1iv(WebGLUniformLocation var1, JSArrayReader<JSNumber> var2);
    //void uniform1iv(WebGLUniformLocation var1, @JSByRef int[] var2);
    //void uniform2f(WebGLUniformLocation var1, float var2, float var3);
    //void uniform2fv(WebGLUniformLocation var1, Float32Array var2);
    //void uniform2fv(WebGLUniformLocation var1, JSArrayReader<JSNumber> var2);
    //void uniform2fv(WebGLUniformLocation var1, @JSByRef float[] var2);
    //void uniform2i(WebGLUniformLocation var1, int var2, int var3);
    //void uniform2iv(WebGLUniformLocation var1, Int32Array var2);
    //void uniform2iv(WebGLUniformLocation var1, JSArrayReader<JSNumber> var2);
    //void uniform2iv(WebGLUniformLocation var1, @JSByRef int[] var2);
    //void uniform3f(WebGLUniformLocation var1, float var2, float var3, float var4);

    public void uniform3f(WebGLUniformLocation uniformLocation, float v0, float v1, float v2)
    {
        call("uniform3f", uniformLocation._jsObj, v0, v1, v2);
    }

    public void uniform3fv(WebGLUniformLocation uniformLocation, float[] floatArray)
    {
        //call("uniform3fv", uniformLocation._jsObj, floatArray); - this isn't working, acts like it gets zeros
        call("uniform3f", uniformLocation._jsObj, floatArray[0], floatArray[1], floatArray[2]);
    }

    public void uniform3fv(WebGLUniformLocation uniformLocation, Float32Array float32Array)
    {
        call("uniform3fv", uniformLocation._jsObj, float32Array._jsObj);
    }

    //void uniform3fv(WebGLUniformLocation var1, JSArrayReader<JSNumber> var2);
    //void uniform3i(WebGLUniformLocation var1, int var2, int var3, int var4);
    //void uniform3iv(WebGLUniformLocation var1, Int32Array var2);
    //void uniform3iv(WebGLUniformLocation var1, JSArrayReader<JSNumber> var2);
    //void uniform3iv(WebGLUniformLocation var1, @JSByRef int[] var2);
    //void uniform4f(WebGLUniformLocation var1, float var2, float var3, float var4, float var5);
    //void uniform4fv(WebGLUniformLocation var1, Float32Array var2);
    //void uniform4fv(WebGLUniformLocation var1, JSArrayReader<JSNumber> var2);
    //void uniform4fv(WebGLUniformLocation var1, @JSByRef float[] var2);
    //void uniform4i(WebGLUniformLocation var1, int var2, int var3, int var4, int var5);
    //void uniform4iv(WebGLUniformLocation var1, Int32Array var2);
    //void uniform4iv(WebGLUniformLocation var1, JSArrayReader<JSNumber> var2);
    //void uniform4iv(WebGLUniformLocation var1, @JSByRef int[] var2);

    public void validateProgram(WebGLProgram aProgram)  { call("validateProgram", aProgram._jsObj); }

    //void vertexAttrib1f(int var1, float var2);
    //void vertexAttrib1fv(int var1, Float32Array var2);
    //void vertexAttrib1fv(int var1, JSArrayReader<JSNumber> var2);
    //void vertexAttrib1fv(int var1, @JSByRef float[] var2);
    //void vertexAttrib2f(int var1, float var2, float var3);
    //void vertexAttrib2fv(int var1, Float32Array var2);
    //void vertexAttrib2fv(int var1, JSArrayReader<JSNumber> var2);
    //void vertexAttrib2fv(int var1, @JSByRef float[] var2);
    //void vertexAttrib3f(int var1, float var2, float var3, float var4);
    //void vertexAttrib3fv(int var1, Float32Array var2);
    //void vertexAttrib3fv(int var1, JSArrayReader<JSNumber> var2);
    //void vertexAttrib3fv(int var1, @JSByRef float[] var2);
    //void vertexAttrib4f(int var1, float var2, float var3, float var4, float var5);
    //void vertexAttrib4fv(int var1, Float32Array var2);
    //void vertexAttrib4fv(int var1, JSArrayReader<JSNumber> var2);
    //void vertexAttrib4fv(int var1, @JSByRef float[] var2);
}
