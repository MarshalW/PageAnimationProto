package com.example.page;

import android.opengl.GLES20;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: marshal
 * Date: 13-3-5
 * Time: 下午9:56
 * To change this template use File | Settings | File Templates.
 */
public class PageMesh {

    private float[] top, bottom, shadow;

    private FloatBuffer vertexBuffer, shadowColorBuffer;

    private Shader contentShader, shadowShader;

    public PageMesh() {
        this.initShader();
    }

    public void setTopPolygon(float[] polygon) {
        top = polygon;
    }

    public void setBottom(float[] bottom) {
        this.bottom = bottom;
    }

    public void setShadow(float[] shadow) {
        this.shadow = shadow;
    }

    private void initShader() {
        String vertexShader =
                // This matrix member variable provides a hook to manipulate
                // the coordinates of the objects that use this vertex shader
                "uniform mat4 uMVPMatrix;" +
//                        "attribute vec4 aColor;" +
//                        "varying vec4 vColor;" +
                        "attribute vec4 vPosition;" +
                        "void main() {" +
                        // the matrix must be included as a modifier of gl_Position
//                        "vColor = aColor;" +
                        "  gl_Position = uMVPMatrix * vPosition;" +
                        "}";

        String fragmentShader =
                "precision mediump float;" +
                        "uniform vec4 vColor;" +
//                        "varying vec4 vColor;" +
                        "void main() {" +
                        "  gl_FragColor = vColor;" +
                        "}";

        contentShader = new Shader();
        contentShader.setProgram(vertexShader, fragmentShader);
//        contentShader.useProgram();

        vertexShader =
                // This matrix member variable provides a hook to manipulate
                // the coordinates of the objects that use this vertex shader
                "uniform mat4 uMVPMatrix;" +
                        "attribute vec4 aColor;" +
                        "varying vec4 vColor;" +
                        "attribute vec4 vPosition;" +
                        "void main() {" +
                        // the matrix must be included as a modifier of gl_Position
                        "  vColor = aColor;" +
                        "  gl_Position = uMVPMatrix * vPosition;" +
                        "}";
        fragmentShader =
                "precision mediump float;" +
//                            "uniform vec4 vColor;" +
                        "varying vec4 vColor;" +
                        "void main() {" +
                        "  gl_FragColor = vColor;" +
                        "}";
        shadowShader = new Shader();
        shadowShader.setProgram(vertexShader, fragmentShader);
    }

    private void initVertexBuffer() {
        /**
         * 设置顶点
         */
        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(
                // (number of coordinate values * 4 bytes per float)
                12 * 4 * 3);
        // use the device hardware's native byte order
        bb.order(ByteOrder.nativeOrder());

        // create a floating point buffer from the ByteBuffer
        vertexBuffer = bb.asFloatBuffer();

        // add the coordinates to the FloatBuffer
        vertexBuffer.put(top);//设置顶部顶点
        vertexBuffer.put(bottom);//设置底部顶点

        vertexBuffer.put(shadow);//设置阴影顶点

        // set the buffer to read the first coordinate
        vertexBuffer.position(0);

    }

    private void initColorBuffer(PageRenderer renderer){
        float factor=renderer.getFactor();

        /**
         * 设置顶点颜色
         */
        ByteBuffer bb = ByteBuffer.allocateDirect(
                // (number of coordinate values * 4 bytes per float)
                16 * 4);
        // use the device hardware's native byte order
        bb.order(ByteOrder.nativeOrder());

        shadowColorBuffer = bb.asFloatBuffer();
        shadowColorBuffer.put(new float[]{
                factor, factor, factor, 1-factor,
                1, 1, 1, 0,
                factor, factor, factor, 1-factor,
                1, 1, 1, 0
        });
        shadowColorBuffer.position(0);
    }

    public void draw(PageRenderer renderer) {

        this.initVertexBuffer();
        this.initColorBuffer(renderer);

        /**
         * 画内容部分
         */
        contentShader.useProgram();

        //设置顶点handle
        GLES20.glVertexAttribPointer(contentShader.getHandle("vPosition"), 3, GLES20.GL_FLOAT, false,
                3 * 4, vertexBuffer);
        GLES20.glEnableVertexAttribArray(contentShader.getHandle("vPosition"));

        //设置颜色handle
        GLES20.glUniform4fv(contentShader.getHandle("vColor"), 1, new float[]{0.63671875f, 0.76953125f, 0.22265625f, 1.0f}, 0);
        GLES20.glEnableVertexAttribArray(contentShader.getHandle("vColor"));

        //设置投影矩阵
        GLES20.glUniformMatrix4fv(contentShader.getHandle("uMVPMatrix"), 1, false, renderer.getProjectionMatrix(), 0);
        //画顶部多边形
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        //画底部多边形
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 4, 4);

        /**
         * 画阴影部分
         */
        //设置阴影顶点
        shadowShader.useProgram();
        vertexBuffer.position(12*2);
        GLES20.glVertexAttribPointer(shadowShader.getHandle("vPosition"), 3, GLES20.GL_FLOAT, false,
                3 * 4, vertexBuffer);
        GLES20.glEnableVertexAttribArray(shadowShader.getHandle("vPosition"));

        //设置顶点颜色
        GLES20.glVertexAttribPointer(shadowShader.getHandle("aColor"), 4, GLES20.GL_FLOAT, false, 0,
                this.shadowColorBuffer);
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        //设置投影矩阵
        GLES20.glUniformMatrix4fv(shadowShader.getHandle("uMVPMatrix"), 1, false, renderer.getProjectionMatrix(), 0);
        //画阴影多边形
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);


//        GLES20.glDisable(GLES20.GL_BLEND);

    }

    class Shader {
        private int program;
        private final HashMap<String, Integer> shaderHandleMap = new HashMap<String, Integer>();
        String vertexShader;

        public int getHandle(String name) {
            if (shaderHandleMap.containsKey(name)) {
                return shaderHandleMap.get(name);
            }
            int handle = GLES20.glGetAttribLocation(program, name);
            if (handle == -1) {
                handle = GLES20.glGetUniformLocation(program, name);
            }
            if (handle == -1) {
                throw new RuntimeException("get handle error, handle name:" + name + "\nshader:" + this.vertexShader);
            } else {
                shaderHandleMap.put(name, handle);
            }
            return handle;
        }

        private int loadShader(int shaderType, String source) {
            int shader = GLES20.glCreateShader(shaderType);
            if (shader == 0) {
                throw new RuntimeException("can not load shader");
            }
            GLES20.glShaderSource(shader, source);
            GLES20.glCompileShader(shader);
            int[] compiled = new int[1];
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
            if (compiled[0] == 0) {
                String error = GLES20.glGetShaderInfoLog(shader);
                GLES20.glDeleteShader(shader);
                throw new RuntimeException(error);
            }
            return shader;
        }

        public void setProgram(String vertexSource, String fragmentSource) {
            this.vertexShader = vertexSource;

            int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource);
            int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER,
                    fragmentSource);
            int program = GLES20.glCreateProgram();
            if (program == 0) {
                throw new RuntimeException("can not create shader program.");
            }
            GLES20.glAttachShader(program, vertexShader);
            GLES20.glAttachShader(program, fragmentShader);
            GLES20.glLinkProgram(program);
            int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
            if (linkStatus[0] != GLES20.GL_TRUE) {
                String error = GLES20.glGetProgramInfoLog(program);
                GLES20.glDeleteProgram(program);
                throw new RuntimeException(error);
            }
            this.program = program;
            shaderHandleMap.clear();
        }

        public void useProgram() {
            GLES20.glUseProgram(program);
        }
    }
}
