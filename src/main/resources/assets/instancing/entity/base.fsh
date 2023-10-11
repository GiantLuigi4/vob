#version 330

#define MAX_INSTANCES 100

uniform sampler2D colortex0;
uniform vec3[MAX_INSTANCES] tickData;

in vec2 texCoord;
in vec4 color;
in vec4 worldCoord;
in vec3 normal;
flat in int entityId;

out vec4 fragCol;


vec4 blend(vec4 src, vec4 dst) {
    if (src.a == 0) return dst;
    if (dst.a == 0) return dst;

    float final_alpha = src.a + dst.a * (1.0 - src.a);
    return vec4(
        (dst.rgb * (1.0 - src.a)) +
        (src.rgb * src.a),
        final_alpha
    );
}

void main() {
    fragCol = texture2D(colortex0, texCoord.xy) * color;


    // TODO: check this?
    vec3 l0 = normalize(vec3(0.2, 1.0, -0.7));
    vec3 l1 = normalize(vec3(-0.2, 1.0, 0.7));

    float ambient = 0.4;
    float brightness = 0.6;

    float d0 = max(0.0, dot(normal.xyz, l0));
    float d1 = max(0.0, dot(normal.xyz, l1));

    if (tickData[entityId].z > 0.0) {
        fragCol = blend(
            vec4(1, 0, 0, 0.4),
            fragCol
        );
    }

    fragCol = fragCol * min(1.0, (((d0 + d1) * brightness) + ambient));
}
