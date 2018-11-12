package com.hyq.hm.openglexo;

import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Created by 海米 on 2017/8/16.
 */

public class GLFramebuffer {
    int left, top, right, bottom;
    //矩阵 上下颠倒
    private final float[] vertexData = {
            1f, -1f, 0f,
            -1f, -1f, 0f,
            1f, 1f, 0f,
            -1f, 1f, 0f
    };
    //矩阵 180度旋转
    private final float[] textureVertexData = {
            1f, 0f,
            0f, 0f,
            1f, 1f,
            0f, 1f
    };
    //矩阵
    private FloatBuffer vertexBuffer;
    //矩阵
    private FloatBuffer textureVertexBuffer;

    //着色器程序容器
    private int programId = -1;

    private int aPositionHandle;//着色器顶点
    private int uTextureSamplerHandle;//Uniform变量的索引值

    private int aTextureCoordHandle;//着色器顶点
    private int uSTMMatrixHandle;//Uniform变量的索引值

    private float[] mSTMatrix = new float[16];

    private int[] textures;


    private int[] vertexBuffers;

    private SurfaceTexture surfaceTexture;

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public void setBlurX(int blurX) {
        this.blurX = blurX;
    }

    public void setBlurY(int blurY) {
        this.blurY = blurY;
    }

    public void setTrans(double trans) {
        this.trans = trans;
    }

    private float radius = 12.0f;//偏移量
    private int blurX = 4, blurY = 15;//X方向, Y方向
    private double trans = 0.005d;//透明度

    //黑白滤镜shader backup
    /*private String fragmentShader = "#extension GL_OES_EGL_image_external : require\n" +
            "varying vec2 vTexCoord;\n" +
            "uniform samplerExternalOES sTexture;\n" +
            "void main() {\n" +
            "    highp vec4 centralColor = texture2D(sTexture, vec2(vTexCoord.x, vTexCoord.y));\n" +
            "    gl_FragColor = vec4(centralColor);\n" +
            "}";*/
    //高斯模糊滤镜
    private String fragmentShader = "#extension GL_OES_EGL_image_external : require\n" +
            "precision mediump float;\n" +
            "varying vec2 vTexCoord;\n" +
            "uniform samplerExternalOES sTexture;\n" +
            "const float resolution = 1024.0;\n" +
            "const float radius = "+radius+";\n" +
            "vec2 dir = vec2(1.0,1.0);\n" +
            "void main() {\n" +
            "   highp vec4 centralColor = vec4(0.0);\n" +
            "   float blur = radius/resolution;\n" +
            "   float hstep = dir.x;\n" +
            "   float vstep = dir.y;\n" +
            "   int x = "+blurX+";int y = "+blurY+";\n" +
            "   for(int i = x; i > 0; i--){\n" +
            "       for(int j = y; j > 0; j--){\n" +
            "           centralColor += texture2D(sTexture, vec2(vTexCoord.x + float(i)*blur*hstep, vTexCoord.y +float(j)*blur*vstep))*"+trans+";" +
            "           centralColor += texture2D(sTexture, vec2(vTexCoord.x - float(i)*blur*hstep, vTexCoord.y +float(j)*blur*vstep))*"+trans+";" +
            "           centralColor += texture2D(sTexture, vec2(vTexCoord.x - float(i)*blur*hstep, vTexCoord.y -float(j)*blur*vstep))*"+trans+";" +
            "           centralColor += texture2D(sTexture, vec2(vTexCoord.x + float(i)*blur*hstep, vTexCoord.y -float(j)*blur*vstep))*"+trans+";" +
            "       }\n" +
            "   }\n" +
            "   gl_FragColor = vec4(centralColor);\n" +
            "}";

    private  String vertexShader = "uniform mat4 uMVPMatrix;\n" +
            "uniform mat4 uSTMatrix;\n" +
            "attribute vec4 aPosition;\n" +
            "attribute vec4 aTexCoord;\n" +
            "varying vec2 vTexCoord;\n" +
            "void main() {\n" +
            "    vTexCoord = (uSTMatrix * aTexCoord).xy;\n" +
            "    gl_Position = aPosition;\n" +
            "}";


    //自定义
    public GLFramebuffer(float radius, int blurX, int blurY, double trans){
        vertexBuffer = ByteBuffer.allocateDirect(vertexData.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(vertexData);
        vertexBuffer.position(0);

        textureVertexBuffer = ByteBuffer.allocateDirect(textureVertexData.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(textureVertexData);
        textureVertexBuffer.position(0);

        this.radius = radius;
        this.blurX = blurX;
        this.blurY = blurY;
        this.trans = trans;

        fragmentShader = "#extension GL_OES_EGL_image_external : require\n" +
                "precision mediump float;\n" +
                "varying vec2 vTexCoord;\n" +
                "uniform samplerExternalOES sTexture;\n" +
                "const float resolution = 1024.0;\n" +
                "const float radius = "+radius+";\n" +
                "vec2 dir = vec2(1.0,1.0);\n" +
                "void main() {\n" +
                "   highp vec4 centralColor = vec4(0.0);\n" +
                "   float blur = radius/resolution;\n" +
                "   float hstep = dir.x;\n" +
                "   float vstep = dir.y;\n" +
                "   int x = "+blurX+";int y = "+blurY+";\n" +
                "   for(int i = x; i > 0; i--){\n" +
                "       for(int j = y; j > 0; j--){\n" +
                "           centralColor += texture2D(sTexture, vec2(vTexCoord.x + float(i)*blur*hstep, vTexCoord.y +float(j)*blur*vstep))*"+trans+";" +
                "           centralColor += texture2D(sTexture, vec2(vTexCoord.x - float(i)*blur*hstep, vTexCoord.y +float(j)*blur*vstep))*"+trans+";" +
                "           centralColor += texture2D(sTexture, vec2(vTexCoord.x - float(i)*blur*hstep, vTexCoord.y -float(j)*blur*vstep))*"+trans+";" +
                "           centralColor += texture2D(sTexture, vec2(vTexCoord.x + float(i)*blur*hstep, vTexCoord.y -float(j)*blur*vstep))*"+trans+";" +
                "       }\n" +
                "   }\n" +
                "   gl_FragColor = vec4(centralColor);\n" +
                "}";
        vertexShader = "uniform mat4 uMVPMatrix;\n" +
                "uniform mat4 uSTMatrix;\n" +
                "attribute vec4 aPosition;\n" +
                "attribute vec4 aTexCoord;\n" +
                "varying vec2 vTexCoord;\n" +
                "void main() {\n" +
                "    vTexCoord = (uSTMatrix * aTexCoord).xy;\n" +
                "    gl_Position = aPosition;\n" +
                "}";
    }

    //默认高斯模糊效果
    public GLFramebuffer(){
        vertexBuffer = ByteBuffer.allocateDirect(vertexData.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(vertexData);
        vertexBuffer.position(0);

        textureVertexBuffer = ByteBuffer.allocateDirect(textureVertexData.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(textureVertexData);
        textureVertexBuffer.position(0);

    }

    private void viewportSize(int videoWidth,int videoHeight,int screenWidth,int screenHeight){
        int left,top,viewWidth,viewHeight;
        float sh = screenWidth*1.0f/screenHeight;
        float vh = videoWidth*1.0f/videoHeight;
        if(sh < vh){
            left = 0;
            viewWidth = screenWidth;
            viewHeight = (int)(videoHeight*1.0f/videoWidth*viewWidth);
            top = (screenHeight - viewHeight)/2;
        }else{
            top = 0;
            viewHeight = screenHeight;
            viewWidth = (int)(videoWidth*1.0f/videoHeight*viewHeight);
            left = (screenWidth - viewWidth)/2;
        }
        this.left = left;
        this.top = top;
        this.right = viewWidth;
        this.bottom = viewHeight;
    }

    public void initFramebuffer(int screenWidth,int screenHeight,int videoWidth,int videoHeight){

        viewportSize(videoWidth,videoHeight,screenWidth,screenHeight);
        //相当于glCreateProgram();
        programId = ShaderUtils.createProgram(vertexShader, fragmentShader);

        aPositionHandle = GLES20.glGetAttribLocation(programId, "aPosition");
        uSTMMatrixHandle = GLES20.glGetUniformLocation(programId, "uSTMatrix");
        uTextureSamplerHandle = GLES20.glGetUniformLocation(programId, "sTexture");
        aTextureCoordHandle = GLES20.glGetAttribLocation(programId, "aTexCoord");

        vertexBuffers = new int[2];

        GLES20.glGenBuffers(2,vertexBuffers,0);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexBuffers[0]);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vertexData.length*4, vertexBuffer,GLES20.GL_STATIC_DRAW);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexBuffers[1]);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, textureVertexData.length*4, textureVertexBuffer,GLES20.GL_STATIC_DRAW);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);


        textures = new int[1];

        GLES20.glGenTextures(1, textures, 0);


        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textures[0]);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

    }

    public SurfaceTexture getSurfaceTexture(){
        surfaceTexture = new SurfaceTexture(textures[0]);
        return surfaceTexture;
    }

    public void drawFrame(){

        surfaceTexture.updateTexImage();
        surfaceTexture.getTransformMatrix(mSTMatrix);

        /**
         * 清除缓冲
         * GL_COLOR_BUFFER_BIT: 当前可写的颜色缓冲
         * GL_DEPTH_BUFFER_BIT: 深度缓冲
         * GL_ACCUM_BUFFER_BIT: 累积缓冲
         * GL_STENCIL_BUFFER_BIT: 模板缓冲
         */
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
        //视图坐标什么的
        GLES20.glViewport(left, top, right, bottom);
        //加载 使用链接程序 参数(int)
        GLES20.glUseProgram(programId);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexBuffers[0]);
        GLES20.glEnableVertexAttribArray(aPositionHandle);
        GLES20.glVertexAttribPointer(aPositionHandle, 3, GLES20.GL_FLOAT, false,
                12, 0);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexBuffers[1]);
        GLES20.glEnableVertexAttribArray(aTextureCoordHandle);
        GLES20.glVertexAttribPointer(aTextureCoordHandle, 2, GLES20.GL_FLOAT, false, 8, 0);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);


        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);
        GLES20.glUniform1i(uTextureSamplerHandle,0);
        GLES20.glUniformMatrix4fv(uSTMMatrixHandle, 1, false, mSTMatrix, 0);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
    }

//    public void drawFrame(){
//        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
//        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);
//    }

}
