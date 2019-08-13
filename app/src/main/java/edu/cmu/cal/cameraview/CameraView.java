package edu.cmu.cal.cameraview;

import android.content.Context;
import android.content.res.TypedArray;
import android.hardware.Camera;
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
    }

    public void start() {
        Log.d(TAG, "start");
        if (mCamera != null) {
            stop();
        }
        mPreview.cameraChanged(mCamera = Camera.open());
        for (Callback callback : mCallbacks) {
            callback.onCameraOpened(this);
        }
        mCamera.startPreview();
    }

    public void stop() {
        Log.d(TAG, "stop");
        if (mCamera != null) {
            mCamera.release();
            mPreview.cameraChanged(mCamera = null);
            for (Callback callback : mCallbacks) {
                callback.onCameraClosed(this);
            }
        }
    }

    public void addCallback(@NonNull Callback callback) {
        mCallbacks.add(callback);
    }

    public void takePicture() {
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
        }
        camera.setParameters(params);
    }
}

