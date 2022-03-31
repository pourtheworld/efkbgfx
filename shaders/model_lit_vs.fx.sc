$input a_position a_normal a_bitangent a_tangent a_texcoord0 a_color0
$output v_Color v_UV v_WorldN v_WorldB v_WorldT v_PosP

#include <bgfx_shader.sh>
#include "defines.sh"
uniform mat4 u_mCameraProj;
uniform mat4 u_mModel_Inst[40];
uniform vec4 u_fUV[40];
uniform vec4 u_fModelColor[40];
uniform vec4 u_fLightDirection;
uniform vec4 u_fLightColor;
uniform vec4 u_fLightAmbient;
uniform vec4 u_mUVInversed;


struct VS_Input
{
    vec3 Pos;
    vec3 Normal;
    vec3 Binormal;
    vec3 Tangent;
    vec2 UV;
    vec4 Color;
    uint Index;
};

struct VS_Output
{
    vec4 PosVS;
    vec4 Color;
    vec2 UV;
    vec3 WorldN;
    vec3 WorldB;
    vec3 WorldT;
    vec4 PosP;
};

VS_Output _main(VS_Input Input)
{
    uint index = Input.Index;
    mat4 mModel = u_mModel_Inst[index];
    vec4 uv = u_fUV[index];
    vec4 modelColor = u_fModelColor[index] * Input.Color;
    VS_Output Output = (VS_Output)0;
    vec4 localPos = vec4(Input.Pos.x, Input.Pos.y, Input.Pos.z, 1.0);
    vec4 worldPos = mul(mModel, localPos);
    Output.PosVS = mul(u_mCameraProj, worldPos);
    Output.Color = modelColor;
    vec2 outputUV = Input.UV;
    outputUV.x = (outputUV.x * uv.z) + uv.x;
    outputUV.y = (outputUV.y * uv.w) + uv.y;
    outputUV.y = u_mUVInversed.x + (u_mUVInversed.y * outputUV.y);
    Output.UV = outputUV;
    vec4 localNormal = vec4(Input.Normal.x, Input.Normal.y, Input.Normal.z, 0.0);
    vec4 localBinormal = vec4(Input.Binormal.x, Input.Binormal.y, Input.Binormal.z, 0.0);
    vec4 localTangent = vec4(Input.Tangent.x, Input.Tangent.y, Input.Tangent.z, 0.0);
    vec4 worldNormal = mul(mModel, localNormal);
    vec4 worldBinormal = mul(mModel, localBinormal);
    vec4 worldTangent = mul(mModel, localTangent);
    worldNormal = normalize(worldNormal);
    worldBinormal = normalize(worldBinormal);
    worldTangent = normalize(worldTangent);
    Output.WorldN = worldNormal.xyz;
    Output.WorldB = worldBinormal.xyz;
    Output.WorldT = worldTangent.xyz;
    Output.PosP = Output.PosVS;
    return Output;
}

void main()
{
    VS_Input Input;
    Input.Pos = a_position;
    Input.Normal = a_normal;
    Input.Binormal = a_bitangent;
    Input.Tangent = a_tangent;
    Input.UV = a_texcoord0;
    Input.Color = a_color0;
    Input.Index = uint(gl_InstanceIndex);
    VS_Output flattenTemp = _main(Input);
    vec4 _position = flattenTemp.PosVS;
    gl_Position = _position;
    v_Color = flattenTemp.Color;
    v_UV = flattenTemp.UV;
    v_WorldN = flattenTemp.WorldN;
    v_WorldB = flattenTemp.WorldB;
    v_WorldT = flattenTemp.WorldT;
    v_PosP = flattenTemp.PosP;
}
