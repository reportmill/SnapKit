
precision mediump float;

// Parameters: ProjMatrix, ViewMatrix, VertColor
uniform mat4 projMatrix;
uniform mat4 viewMatrix;
uniform vec3 vertColor;

// Attributes: VertPoint, vertTexCoord
attribute vec3 vertPoint;
attribute vec2 vertTexCoord;

// Output: fragColor
varying vec2 fragTexCoord;

void main()
{
    gl_Position = projMatrix * viewMatrix * vec4(vertPoint, 1.0);
    fragTexCoord = vertTexCoord;
}
