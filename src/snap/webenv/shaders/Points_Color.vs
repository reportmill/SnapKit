
precision mediump float;

// Parameters: ProjMatrix, ViewMatrix, VertColor
uniform mat4 projMatrix;
uniform mat4 viewMatrix;
uniform vec3 vertColor;

// Attributes: VertPoint
attribute vec3 vertPoint;

// Output: fragColor
varying vec3 fragColor;

void main()
{
    gl_Position = projMatrix * viewMatrix * vec4(vertPoint, 1.0);
    fragColor = vertColor;
}
