
precision mediump float;

// Parameters: ProjMatrix, ViewMatrix
uniform mat4 projMatrix;
uniform mat4 viewMatrix;

// Attributes: VertPoint, VertColor
attribute vec3 vertPoint;
attribute vec3 vertColor;

// Output: fragColor
varying vec3 fragColor;

void main()
{
    gl_Position = projMatrix * viewMatrix * vec4(vertPoint, 1.0);
    fragColor = vertColor;
}
