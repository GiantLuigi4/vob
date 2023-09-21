#version 400

//#define gl_InstanceID 0

//@defines
#define MAX_INSTANCES 100
#define CUBE_COUNT 7
#define CUBE_ID clamp(gl_VertexID / (6 * 6), 0, CUBE_COUNT)
#define ENTITY_ID gl_InstanceID

// entity data
uniform vec2[MAX_INSTANCES] facings;
uniform vec4[MAX_INSTANCES] colors;
uniform mat4[MAX_INSTANCES] matrices;

// x=limbSwing, y=limbYaw
uniform vec2[MAX_INSTANCES] limbData;
// x=tickCount, y=dayTime, z=hurtTime
uniform vec3[MAX_INSTANCES] tickData;

// cube data
uniform vec3[CUBE_COUNT] pivots;

in vec4 Vertex;
in vec3 Normal;
in vec2 TexCoord;

out gl_PerVertex {
    vec4 gl_Position;
};

out vec4 color;
out vec3 normal;
out vec2 texCoord;
out vec4 worldCoord;

uniform mat4 projMat;
uniform mat4 modelViewMat;

// https://gist.github.com/yiwenl/3f804e80d0930e34a0b33359259b556c
mat4 rotationMatrix(vec3 axis, float angle) {
    axis = normalize(axis);
    float s = sin(angle);
    float c = cos(angle);
    float oc = 1.0 - c;

    //@formatter:off
    return mat4(oc * axis.x * axis.x + c,           oc * axis.x * axis.y - axis.z * s,  oc * axis.z * axis.x + axis.y * s,  0.0,
                oc * axis.x * axis.y + axis.z * s,  oc * axis.y * axis.y + c,           oc * axis.y * axis.z - axis.x * s,  0.0,
                oc * axis.z * axis.x - axis.y * s,  oc * axis.y * axis.z + axis.x * s,  oc * axis.z * axis.z + c,           0.0,
                0.0,                                0.0,                                0.0,                                1.0);
    //@formatter:on
}

mat4 calcMatr(vec3 rotation) {
    //@formatter:off
    return
            rotationMatrix(vec3(1.0, 0.0, 0.0), rotation.x) *
            rotationMatrix(vec3(0.0, 1.0, 0.0), rotation.y) *
            rotationMatrix(vec3(0.0, 0.0, 1.0), rotation.z);
    //@formatter:on
}

mat4 ncalcMatr(vec3 rotation) {
    //@formatter:off
    return
            rotationMatrix(vec3(1.0, 0.0, 0.0), rotation.x) *
            rotationMatrix(vec3(0.0, 1.0, 0.0), rotation.y) *
            rotationMatrix(vec3(0.0, 0.0, 1.0), -rotation.z);
    //@formatter:on
}

void main() {
    vec4 center = vec4(pivots[CUBE_ID], 0.0);

    vec3 scale = vec3(1.0, 1.0, 1.0);
    vec3 offset = vec3(0.0, 0.0, 0.0);
    vec3 rotation = vec3(0.0, 0.0, 0.0);

    // setup transformations

    //@formatter:off
    worldCoord =
                ((Vertex * vec4(scale, 1.0) + vec4(offset, 0.0)) * calcMatr(rotation) + center) *
                    matrices[ENTITY_ID];
    gl_Position = worldCoord * modelViewMat * projMat;
    color = colors[ENTITY_ID];

    mat4 mr = ncalcMatr(rotation) * matrices[ENTITY_ID];
    mr[0][3] = 0;
    mr[1][3] = 0;
    mr[2][3] = 0;
    mr[3][3] = 1;

    vec3 nrmScl = vec3(1, -1, -1);
    normal = normalize(
        (vec4(((Normal - 0.5) * 2) * nrmScl, 1.0) * mr).xyz
    );

    texCoord = TexCoord.xy;
    //@formatter:on
}
