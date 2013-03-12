package com.example.page;

import android.animation.ValueAnimator;
import android.graphics.RectF;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;
import android.view.animation.DecelerateInterpolator;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created with IntelliJ IDEA.
 * User: marshal
 * Date: 13-3-5
 * Time: 下午9:46
 * To change this template use File | Settings | File Templates.
 */
public class PageRenderer implements GLSurfaceView.Renderer {

    private PageMesh mesh;

    private float[] projectionMatrix = new float[16];

    private float factor;

    float ratio;

    private RectF targetRect;

    public float[] getProjectionMatrix() {
        return projectionMatrix;
    }

    public float getFactor() {
        return factor;
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 0.5f);
        this.mesh = new PageMesh();
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        ratio = (float) width / height;
        Matrix.orthoM(this.projectionMatrix, 0, -ratio, ratio, -1, 1, -10, 10);

        //set top rect for page mesh
        targetRect = new RectF();
        targetRect.left = -ratio;
        targetRect.right = ratio;
        targetRect.top = 0.5f;
        targetRect.bottom = -0.1f;
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        this.setPolygons();
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        this.mesh.draw(this);
    }

    private void setPolygons() {
        //设置顶部多边形
        float y = (float) (targetRect.top - Math.sin(this.factor * Math.PI / 2) * (targetRect.top - targetRect.bottom));
        float z = this.factor - 1;
        float k = (z - 5) / (-ratio);
        float x = -5 / k; //TODO 这个值不对，只能算近似解，有空改进

        float _y = targetRect.top - 2 * (targetRect.top - y);

        //如果屏蔽这行就是从上往下折叠
        float deltaY=targetRect.top-targetRect.bottom-(targetRect.top-_y)+targetRect.top;
//        float deltaY=0;

        float[] polygonTop = new float[]{
                targetRect.left, targetRect.top-deltaY, 0,
                x, y-deltaY, 0,
                targetRect.right, targetRect.top-deltaY, 0,
                -x, y-deltaY, 0
        };

        //设置底部多边形

        float[] polygonBottom = new float[]{
                x, y-deltaY, 0,
                targetRect.left, _y-deltaY, 0,
                -x, y-deltaY, 0,
                targetRect.right, _y-deltaY, 0
        };

        this.mesh.setTopPolygon(polygonTop);
        this.mesh.setBottom(polygonBottom);

        //设置阴影多边形
        this.mesh.setShadow(polygonBottom);
    }

    public void startAnimation(final GLSurfaceView view) {
        ValueAnimator animator = ValueAnimator.ofFloat(0, 1);
        animator.setDuration(500);
        animator.setStartDelay(500);
        animator.setInterpolator(new DecelerateInterpolator());

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                factor = (Float) valueAnimator.getAnimatedValue();
                view.requestRender();
            }
        });

        animator.start();
    }
}
