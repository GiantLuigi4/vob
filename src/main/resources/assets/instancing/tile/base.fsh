#version 330

uniform sampler2D colortex0;
varying vec2 texCoord;
varying vec4 color;
varying vec4 worldCoord;
varying vec3 normal;

out vec4 fragCol;

void main() {
    fragCol = texture2D(colortex0, texCoord.xy) * color;


    // TODO: check this?
    vec3 l0 = normalize(vec3(0.2, 1.0, -0.7));
    vec3 l1 = normalize(vec3(-0.2, 1.0, 0.7));

    float ambient = 0.4;
    float brightness = 0.6;

    float d0 = max(0.0, dot(normal.xyz, l0));
    float d1 = max(0.0, dot(normal.xyz, l1));

    fragCol = fragCol * min(1.0, (((d0 + d1) * brightness) + ambient));
}
