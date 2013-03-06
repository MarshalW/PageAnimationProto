package com.example.page;

import android.graphics.PointF;
import android.graphics.RectF;
import android.opengl.GLES20;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Created with IntelliJ IDEA.
 * User: marshal
 * Date: 13-3-5
 * Time: 下午9:56
 * To change this template use File | Settings | File Templates.
 */
public class PageMesh {

    private float[] top;

    private float[] bottom;

    private int mvpMatrixHandle, positionHandle, colorHandle;

    private FloatBuffer vertexBuffer;

    public PageMesh() {
        this.initShader();
    }

    public void setTopPolygon(float[] polygon) {
        top = polygon;
    }

    public void setBottom(float[] bottom) {
        this.bottom = bottom;
    }

    private void initShader() {
        String vertexShader =
                // This matrix member variable provides a hook to manipulate
                // the coordinates of the objects that use this vertex shader
                "uniform mat4 uMVPMatrix;" +

                        "attribute vec4 vPosition;" +
                        "void main() {" +
                        // the matrix must be included as a modifier of gl_Position
                        "  gl_Position = uMVPMatrix * vPosition;" +
                        "}";

        String fragmentShader =
                "precision mediump float;" +
                        "uniform vec4 vColor;" +
                        "void main() {" +
                        "  gl_FragColor = vColor;" +
                        "}";

        // Load in the vertex shader.
        int vertexShaderHandle = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);

        if (vertexShaderHandle != 0) {
            // Pass in the shader source.
            GLES20.glShaderSource(vertexShaderHandle, vertexShader);

            // Compile the shader.
            GLES20.glCompileShader(vertexShaderHandle);

            // Get the compilation status.
            final int[] compileStatus = new int[1];
            GLES20.glGetShaderiv(vertexShaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

            // If the compilation failed, delete the shader.
            if (compileStatus[0] == 0) {
                GLES20.glDeleteShader(vertexShaderHandle);
                vertexShaderHandle = 0;
            }
        }

        if (vertexShaderHandle == 0) {
            throw new RuntimeException("Error creating vertex shader.");
        }

        // Load in the fragment shader shader.
        int fragmentShaderHandle = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);

        if (fragmentShaderHandle != 0) {
            // Pass in the shader source.
            GLES20.glShaderSource(fragmentShaderHandle, fragmentShader);

            // Compile the shader.
            GLES20.glCompileShader(fragmentShaderHandle);

            // Get the compilation status.
            final int[] compileStatus = new int[1];
            GLES20.glGetShaderiv(fragmentShaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

            // If the compilation failed, delete the shader.
            if (compileStatus[0] == 0) {
                GLES20.glDeleteShader(fragmentShaderHandle);
                fragmentShaderHandle = 0;
            }
        }

        if (fragmentShaderHandle == 0) {
            throw new RuntimeException("Error creating fragment shader.");
        }

        // Create a program object and store the handle to it.
        int programHandle = GLES20.glCreateProgram();

        if (programHandle != 0) {
            // Bind the vertex shader to the program.
            GLES20.glAttachShader(programHandle, vertexShaderHandle);

            // Bind the fragment shader to the program.
            GLES20.glAttachShader(programHandle, fragmentShaderHandle);

            // Link the two shaders together into a program.
            GLES20.glLinkProgram(programHandle);

            // Get the link status.
            final int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(programHandle, GLES20.GL_LINK_STATUS, linkStatus, 0);

            // If the link failed, delete the program.
            if (linkStatus[0] == 0) {
                GLES20.glDeleteProgram(programHandle);
                programHandle = 0;
            }
        }

        if (programHandle == 0) {
            throw new RuntimeException("Error creating program.");
        }

        // Set program handles. These will later be used to pass in values to the program.
        mvpMatrixHandle = GLES20.glGetUniformLocation(programHandle, "uMVPMatrix");
        positionHandle = GLES20.glGetAttribLocation(programHandle, "vPosition");
        colorHandle = GLES20.glGetUniformLocation(programHandle, "vColor");

        // Tell OpenGL to use this program when rendering.
        GLES20.glUseProgram(programHandle);
    }

    private void initVertexBuffer() {
        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(
                // (number of coordinate values * 4 bytes per float)
                12 * 4 * 2);
        // use the device hardware's native byte order
        bb.order(ByteOrder.nativeOrder());

        // create a floating point buffer from the ByteBuffer
        vertexBuffer = bb.asFloatBuffer();

        // add the coordinates to the FloatBuffer
        vertexBuffer.put(top);
        vertexBuffer.put(bottom);

        // set the buffer to read the first coordinate
        vertexBuffer.position(0);
    }

    public void draw(PageRenderer renderer) {

        this.initVertexBuffer();

        //设置顶点handle
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false,
                3 * 4, vertexBuffer);
        GLES20.glEnableVertexAttribArray(positionHandle);

        //设置颜色handle
        GLES20.glUniform4fv(colorHandle, 1, new float[]{0.63671875f, 0.76953125f, 0.22265625f, 1.0f}, 0);
        GLES20.glEnableVertexAttribArray(colorHandle);

        GLES20.glUniformMatrix4fv(this.mvpMatrixHandle, 1, false, renderer.getProjectionMatrix(), 0);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        GLES20.glUniform4fv(colorHandle, 1, new float[]{0.5f, 0.5f, 0.2f, 1.0f}, 0);
        GLES20.glEnableVertexAttribArray(colorHandle);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 4, 4);
    }
}
