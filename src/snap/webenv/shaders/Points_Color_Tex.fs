
precision mediump float;

// Parameters: fragTexture
uniform sampler2D fragTexture;

// Attributes: fragTexCoord
varying vec2 fragTexCoord;

void main()
{
    gl_FragColor = texture2D(fragTexture, fragTexCoord);
}
