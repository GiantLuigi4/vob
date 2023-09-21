#version 120

uniform sampler2D colortex0;
varying vec2 texCoord;
varying vec4 color;
varying vec4 worldCoord;
varying vec3 normal;

varying out vec4 fragCol;

//struct gl_LightSourceParameters {
//    vec4 ambient; // Acli
//    vec4 diffuse; // Dcli
//    vec4 specular; // Scli
//    vec4 position; // Ppli
//    vec4 halfVector; // Derived: Hi
//    vec3 spotDirection; // Sdli
//    float spotExponent; // Srli
//    float spotCutoff; // Crli
//    // (range: [0.0,90.0], 180.0)
//    float spotCosCutoff; // Derived: cos(Crli)
//    // (range: [1.0,0.0],-1.0)
//    float constantAttenuation; // K0
//    float linearAttenuation; // K1
//    float quadraticAttenuation;// K2
//};
//uniform gl_LightSourceParameters gl_LightSource[gl_MaxLights];

void main() {
    fragCol = texture2D(colortex0, texCoord.xy) * color;



    // TODO: figure this out
    vec3 l0 = normalize(vec3(0.2, 1.0, -0.7));
    vec3 l1 = normalize(vec3(-0.2, 1.0, 0.7));

    float ambient = 0.4;
    float brightness = 0.6;

    float d0 = max(0.0, dot(normal.xyz, l0));
    float d1 = max(0.0, dot(normal.xyz, l1));

    fragCol = fragCol * min(1.0, (((d0 + d1) * brightness) + ambient));
}
