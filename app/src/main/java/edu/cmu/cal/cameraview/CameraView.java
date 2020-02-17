/*******************************************************************************
 * Copyright (c) 2019  Carnegie Mellon University
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *******************************************************************************/
package edu.cmu.cal.cameraview;

import android.content.Context;
import android.content.res.TypedArray;
import android.hardware.Camera;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.FrameLayout;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.cmu.cal.faceclient.R;

@SuppressWarnings("deprecation")
public class CameraView extends FrameLayout {
    private static final String TAG = "CameraView";
    private final ArrayList<Callback> mCallbacks = new ArrayList<>();
    private Camera mCamera;
    private PreviewSurface mPreview;
    private Handler mCameraHandler;

    public CameraView(Context context) {
        this(context, null);
    }

    public CameraView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CameraView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (!isInEditMode()) {
            TypedArray array = attrs == null ? null :
                    context.obtainStyledAttributes(attrs, R.styleable.CameraView, defStyleAttr, R.style.Widget_CameraView);
            addView(mPreview = new PreviewSurface(context, array));
        }

        HandlerThread thread = new HandlerThread("camera");
        thread.start();
        mCameraHandler = new Handler(thread.getLooper());
    }

    private boolean opening = false;
    public void start() {
        Log.d(TAG, "start");
        if (mCamera != null) {
            stop();
        }
        opening = true;
        mCameraHandler.post(new Runnable() {
            @Override
            public void run() {
                mCamera = Camera.open();
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        mPreview.cameraChanged(mCamera);
                        for (Callback callback : mCallbacks) {
                            callback.onCameraOpened(CameraView.this);
                        }
                        mCamera.startPreview();
                        opening = false;
                    }
                });
            }
        });
    }

    public void stop() {
        if (opening) {
            return;
        }
        Log.d(TAG, "stop");
        if (mCamera != null) {
            mCameraHandler.post(new Runnable() {
                @Override
                public void run() {
                    mCamera.release();
                    mCamera = null;
                }
            });
            mPreview.cameraChanged(null);
            for (Callback callback : mCallbacks) {
                callback.onCameraClosed(CameraView.this);
            }
        }
    }

    public void addCallback(@NonNull Callback callback) {
        mCallbacks.add(callback);
    }

    public void takePicture() {
        mCameraHandler.post(new Runnable() {
            @Override
            public void run() {
                final long t0 = System.nanoTime();
                mCamera.takePicture(null, null, new Camera.PictureCallback() {
                    @Override
                    public void onPictureTaken(byte[] data, Camera camera) {
                        long t1 = System.nanoTime();
                        for (Callback callback : mCallbacks) {
                            callback.onPictureTaken(CameraView.this, data);
                        }
                        long t2 = System.nanoTime();
                        mCamera.startPreview();
                        long t3 = System.nanoTime();
                        Log.d(TAG, String.format("takePicture=%,dns, startPreview=%,dns\n", t1 - t0, t3 - t2));
                    }
                });
            }
        });
    }

    public interface Callback {
        void onCameraOpened(CameraView cameraView);

        void onCameraClosed(CameraView cameraView);

        void onPictureTaken(CameraView cameraView, byte[] data);
    }
}

@SuppressWarnings("deprecation")
class PreviewSurface extends SurfaceView implements SurfaceHolder.Callback {
    private static final Pattern patSize = Pattern.compile("^(\\d+)x(\\d+)$");
    private final String TAG = "PreviewSurface";
    private boolean mReady;
    private Camera mPreviewCamera;
    private int[] previewSize, pictureSize;

    public PreviewSurface(Context context, TypedArray array) {
        super(context);
        getHolder().addCallback(this);
        if (array != null) {
            previewSize = parseSize(array.getString(R.styleable.CameraView_previewSize));
            pictureSize = parseSize(array.getString(R.styleable.CameraView_pictureSize));
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(TAG, "surfaceDestroyed");
        mReady = false;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        Log.d(TAG, "surfaceChanged");
        mReady = true;
        if (!ViewCompat.isInLayout(this)) {
            cameraChanged(mPreviewCamera);
        }
    }

    public void cameraChanged(Camera camera) {
        mPreviewCamera = camera;
        if (mReady && camera != null) {
            setCameraParameters(camera);
            try {
                camera.setPreviewDisplay(getHolder());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private int[] parseSize(String str) {
        if (str != null) {
            Matcher m = patSize.matcher(str);
            if (m.matches()) {
                return new int[]{Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2))};
            }
        }
        return null;
    }

    private void setCameraParameters(Camera camera) {
        Camera.Parameters params = camera.getParameters();
        StringBuffer sbPreviewSize = new StringBuffer();
        for (Camera.Size size : params.getSupportedPreviewSizes()) {
            sbPreviewSize.append(" " + size.width + "x" + size.height);
        }
        Log.d(TAG, "Supported preview size:" + sbPreviewSize);
        // Supported preview size: 176x144 320x240 640x480 720x480 800x480 1280x720 1920x1080
        StringBuffer sbPictureSize = new StringBuffer();
        for (Camera.Size size : params.getSupportedPictureSizes()) {
            sbPictureSize.append(" " + size.width + "x" + size.height);
        }
        Log.d(TAG, "Supported picture size:" + sbPictureSize);
        // Supported picture size: 640x480 1600x1200 2048x1536 2592x1944 3264x2448
        if (previewSize != null) {
            Log.d(TAG, "Using preview size:" + previewSize[0] + "x" + previewSize[1]);
            params.setPreviewSize(previewSize[0], previewSize[1]);
        }
        if (pictureSize != null) {
//            Log.d(TAG, "Using picture size:" + pictureSize[0] + "x" + pictureSize[1]);
//            params.setPictureSize(pictureSize[0], pictureSize[1]);
            params.setPictureSize(3264, 2448);
            params.setJpegQuality(73);
        }
        camera.setParameters(params);
    }
}

