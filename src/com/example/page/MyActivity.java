package com.example.page;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;

public class MyActivity extends Activity {

    private GLSurfaceView surfaceView;

    private PageRenderer renderer;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        surfaceView=new GLSurfaceView(this);
        this.surfaceView.setEGLContextClientVersion(2);
        renderer=new PageRenderer();
        this.surfaceView.setRenderer(this.renderer);
        this.surfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        this.setContentView(surfaceView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.surfaceView.onResume();

        this.renderer.startAnimation(this.surfaceView);
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.surfaceView.onPause();
    }
}
