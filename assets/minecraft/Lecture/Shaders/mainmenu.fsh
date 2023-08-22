/*
 * Original shader from: https://www.shadertoy.com/view/dlc3DN
 */

#ifdef GL_ES
precision highp float;
#endif

// glslsandbox uniforms
uniform float time;
uniform vec2 resolution;

// shadertoy emulation
#define iTime time
#define iResolution resolution

// --------[ Original ShaderToy begins here ]---------- //
//----por jorge2017a1
// 25-abril-2023
/// es una version incompleta de goku niño...falta el pelo...son mas operaciones uoopss!!!
//referencia de idea https://www.shadertoy.com/view/cltGzr....Kali
// The High Priestess


float dot2( in vec2 v ) { return dot(v,v); }
float dot2( in vec3 v ) { return dot(v,v); }
float ndot( in vec2 a, in vec2 b ) { return a.x*b.x - a.y*b.y; }


float sdBezier( in vec2 pos, in vec2 A, in vec2 B, in vec2 C )
{
    vec2 a = B - A;
    vec2 b = A - 2.0*B + C;
    vec2 c = a * 2.0;
    vec2 d = A - pos;
    float kk = 1.0/dot(b,b);
    float kx = kk * dot(a,b);
    float ky = kk * (2.0*dot(a,a)+dot(d,b)) / 3.0;
    float kz = kk * dot(d,a);
    float res = 0.0;
    float p = ky - kx*kx;
    float p3 = p*p*p;
    float q = kx*(2.0*kx*kx-3.0*ky) + kz;
    float h = q*q + 4.0*p3;
    if( h >= 0.0)
    {
        h = sqrt(h);
        vec2 x = (vec2(h,-h)-q)/2.0;
        vec2 uv = sign(x)*pow(abs(x), vec2(1.0/3.0));
        float t = clamp( uv.x+uv.y-kx, 0.0, 1.0 );
        res = dot2(d + (c + b*t)*t);
    }
    else
    {
        float z = sqrt(-p);
        float v = acos( q/(p*z*2.0) ) / 3.0;
        float m = cos(v);
        float n = sin(v)*1.732050808;
        vec3  t = clamp(vec3(m+m,-n-m,n-m)*z-kx,0.0,1.0);
        res = min( dot2(d+(c+b*t.x)*t.x),
                   dot2(d+(c+b*t.y)*t.y) );
        // the third root cannot be the closest
        // res = min(res,dot2(d+(c+b*t.z)*t.z));
    }
    return sqrt( res );
}

#define antialiasing(n) n/min(iResolution.y,iResolution.x)
#define S(d,b) smoothstep(antialiasing(0.8),b,d)
#define S2(d,b) smoothstep(6.0*antialiasing(1.0),b,d)

vec3 DrawFigBorde(vec3 pColObj, vec3 colOut, float distObj )
{ colOut = mix(colOut,pColObj ,S2( distObj,0.0));
colOut = mix(colOut,vec3(0.0) ,S2(abs( distObj)-0.005,0.0));
return colOut;
}
vec3 DrawFigBordeCol(vec3 pColObj, vec3 colOut, float distObj , vec3 colBorde )
{ colOut = mix(colOut,pColObj ,S2( distObj,0.0));
  colOut = mix(colOut,colBorde ,S2(abs( distObj)-0.01,0.0));
  return colOut;
}

float intersectSDF(float distA, float distB)
	{ return max(distA, distB);}
float unionSDF(float distA, float distB)
	{ return min(distA, distB);}
float differenceSDF(float distA, float distB)
	{ return max(distA, -distB);}


float sdCircle( vec2 p, float r )
{return length(p) - r; }

vec3 cabeza(vec2 p, vec3 colOut )
{
   vec2 p1,p2;
   float r1, r2;
   float d=100.0;
   p.y=6.0-p.y;
    p1=vec2(436.,314.)/d;r1=85./d;
    p2=vec2(433.,266.)/d;r2=95./d;
    float d1= sdCircle( p-p1,r1);
    float d2= sdCircle( p-p2,r2);
    float df=unionSDF(d1,d2);
    vec3 carne=vec3(0.9882, 0.8156, 0.7058);
    vec3 col= DrawFigBorde(carne, colOut, df);
    return col;
}


vec3 ojogokuDer(vec2 uv, vec3 colOut)
{
vec3 col=colOut;
vec2 p1, p2, p3, p4,p5,p6,p7;
float r1,r2, r3, r4, r5,r6,r7;

float d=100.0;
uv.y=6.0-uv.y;

p1=vec2(519,318)/d;r1=70./d;
p2=vec2(427,295)/d;r2=75./d;
p3=vec2(483,402)/d;r3=80./d;
p4=vec2(491,178)/d;r4=95./d;
p5=vec2(481,283)/d;r5=18.7/d;

p6=vec2(4.65,3.1);r6=0.08;
p7=vec2(4.53,3.3);r7=0.10;


float d1=sdCircle(uv-p1,r1);
float d2=sdCircle(uv-p2,r2);
float d3=sdCircle(uv-p3,r3);
float d4=sdCircle(uv-p4,r4);
float d5=sdCircle(uv-p5,r5);
float d6=sdCircle(uv-p6,r6);
float d7=sdCircle(uv-p7,r7);


float df;
df=intersectSDF(d1,d2);
df=differenceSDF(df,d3);
df=differenceSDF(df,d4);
df=unionSDF(df,d5);
col= DrawFigBorde(vec3(1.0,1.0,1.0), col,df);
col= DrawFigBorde(vec3(0.0,0.0,0.0), col, d6 );

vec3 carne=vec3(0.9882, 0.8156, 0.7058);
col= DrawFigBordeCol(carne, col, d7 ,carne);

    return col;
}

vec3 boca(vec2 p, vec3 colOut)
{
float d=300.0;
p.y=2.75-p.y;
p.x=2.5+p.x;
p=p*0.5;

vec2 A=vec2(419.,356.)/d;
vec2 B=vec2(447.,362.)/d;
vec2 C=vec2(462.,339.)/d;


    float d1= sdBezier(p, A, B,C );
    vec3 col=colOut;
    col= DrawFigBorde(vec3(0.0,0.0,0.0), col, d1);

    //--------ceja izq
p.y+=0.2;
A=vec2(356.,256.)/d;
B=vec2(382.,240.)/d;
C=vec2(407.,270.)/d;


    d1= sdBezier(p, A, B,C );
    col= DrawFigBorde(vec3(0.0,0.0,0.0), col, d1);



A=vec2(455.,275.)/d;
B=vec2(477.,238.)/d;
C=vec2(501.,249.)/d;
    d1= sdBezier(p, A, B,C );
    col= DrawFigBorde(vec3(0.0,0.0,0.0), col, d1);

p.y-=0.225;
A=vec2(437.,367.)/d;
B=vec2(440.,359.)/d;
C=vec2(447.,365.)/d;
    d1= sdBezier(p, A, B,C );
    col= DrawFigBorde(vec3(0.0,0.0,0.0), col, d1);


    //nariz
p.y+=0.05;
A=vec2(433.,340.)/d;
B=vec2(437.,331.)/d;
C=vec2(446.,337.)/d;
    d1= sdBezier(p, A, B,C );
    col= DrawFigBorde(vec3(0.0,0.0,0.0), col, d1);


    return col;
}




vec3 ojogokuIzq(vec2 uv, vec3 colOut)
{
vec3 col=colOut;
vec2 p1, p2, p3, p4,p5,p6,p7;
float r1,r2, r3, r4, r5,r6,r7;


float d=100.0;
uv.y=6.0-uv.y;

p1=vec2(362,339)/d;r1=70./d;
p2=vec2(429,299)/d;r2=65./d;
p3=vec2(395,411)/d;r3=85./d;
p4=vec2(364,217)/d;r4=65./d;
p5=vec2(383,294)/d;r5=20./d;
p6=vec2(4.1,3.1);r6=0.08;
p7=vec2(4.3,3.3);r7=0.10;


float d1=sdCircle(uv-p1,r1);
float d2=sdCircle(uv-p2,r2);
float d3=sdCircle(uv-p3,r3);
float d4=sdCircle(uv-p4,r4);
float d5=sdCircle(uv-p5,r5);
float d6=sdCircle(uv-p6,r6);
float d7=sdCircle(uv-p7,r7);


float df;
df=intersectSDF(d1,d2);
df=differenceSDF(df,d3);
df=differenceSDF(df,d4);
df=unionSDF(df,d5);
col= DrawFigBorde(vec3(1.0,1.0,1.0), col,df);
col= DrawFigBorde(vec3(0.0,0.0,0.0), col, d6 );
vec3 carne=vec3(0.9882, 0.8156, 0.7058);
col= DrawFigBordeCol(carne, col, d7 ,carne);
    return col;
}


vec3 orejaIzq(vec2 uv, vec3 colOut)
{
vec3 col=colOut;
vec2 p1, p2, p3, p4,p5,p6,p7;
float r1,r2, r3, r4, r5,r6,r7;


float d=100.0;
uv*=1.2;
uv.y=4.0-uv.y;
uv.x=uv.x+3.95;


p1=vec2(375,298)/d;r1=70./d;
p2=vec2(294,347)/d;r2=70./d;
p3=vec2(282,203)/d;r3=90./d;
p4=vec2(317,292)/d;r4=10./d;

float d1=sdCircle(uv-p1,r1);
float d2=sdCircle(uv-p2,r2);
float d3=sdCircle(uv-p3,r3);
float d4=sdCircle(uv-p4,r4);


float df;
df=intersectSDF(d1,d2);
df=differenceSDF(df,d3);
df=unionSDF(df,d4);

vec3 carne=vec3(0.9882, 0.8156, 0.7058);
col= DrawFigBorde(carne, col,df);
return col;
}


vec3 rostroGoku(vec2 uv, vec3 col)
{vec2 pos=vec2(-4.0,-2.);
col= orejaIzq(uv,col);
col= orejaIzq(vec2(-uv.x,uv.y)-vec2(-0.73,0.05),col);
col= cabeza(uv-pos,col);
col= ojogokuDer(uv-pos, col);
col= ojogokuIzq(uv-pos, col);
col= boca(uv, col);
return col;
}

void mainImage( out vec4 fragColor, in vec2 fragCoord )
{ vec2 uv = -1.0 + 2.0 * fragCoord.xy/iResolution.xy;
uv.x *= iResolution.x/iResolution.y;
uv-=vec2(0.0,-0.5);
vec2 uv0=uv;
uv*=2.0;

vec3 col =0.5 + 0.5*cos(iTime+uv.xyx+vec3(0,2,4));
float t=iTime;
uv*=abs(1.5*sin(t));
float s=sin(t);
float c=cos(t);

col= rostroGoku((uv-vec2(-1.0,-3.0))*0.25,col);
col= rostroGoku(vec2(uv.x,uv.y)*0.5,col);
col= rostroGoku(uv-vec2(1.0*s,1.0*c),col);

fragColor = vec4(col,1.0);
}
// --------[ Original ShaderToy ends here ]---------- //

void main(void)
{
    mainImage(gl_FragColor, gl_FragCoord.xy);
}