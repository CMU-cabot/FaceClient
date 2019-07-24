package edu.cmu.cal.faceclient;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.vuzix.hud.actionmenu.ActionMenuActivity;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import edu.cmu.cal.cameraview.CameraView;
import edu.cmu.cal.faceserver.AbstractFaceServer;
import edu.cmu.cal.faceserver.WatsonVisualRecognition;

public class MainActivity extends ActionMenuActivity {

    private static final String TAG = "MainActivity";
    private static final int REQUEST_CAMERA_PERMISSION = 1, TAKE_COUNT = 1, TAKE_DELAY = 3 * 1000;
    private AbstractFaceServer faceServer = new WatsonVisualRecognition();
    private int mTakeCounter = 0;
    private CameraView mCameraView;
    private TextView mInfoView;
    private Handler mHandler, mBackgroundHandler;
    private CameraView.Callback mCallback = new CameraView.Callback() {

        @Override
        public void onCameraOpened(CameraView cameraView) {
            Log.d(TAG, "onCameraOpened");
        }

        @Override
        public void onCameraClosed(CameraView cameraView) {
            Log.d(TAG, "onCameraClosed");
        }

        @Override
        public void onPictureTaken(final CameraView cameraView, final byte[] data) {
            Log.d(TAG, "onPictureTaken " + data.length);
            mInfoView.setText("Processing picture...");
            mBackgroundHandler.post(new Runnable() {
                @Override
                public void run() {
                    String str;
                    try {
                        JSONObject result = processPicture(cameraView, data);
//                        str = result.toString(2);
                        str = faceServer.getSpeakText();
                    } catch (Exception e) {
                        e.printStackTrace();
                        str = e.toString();
                    }
                    final String message = str;
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mInfoView.setText(message);
                        }
                    });
                    if (--mTakeCounter > 0) {
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mCameraView.takePicture();
                            }
                        }, TAKE_DELAY);
                    }
                }
            });
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        HandlerThread thread = new HandlerThread("background");
        thread.start();
        mHandler = new Handler();
        mBackgroundHandler = new Handler(thread.getLooper());
        mCameraView = findViewById(R.id.camera);
        mInfoView = findViewById(R.id.info);
        if (mCameraView != null) {
            mCameraView.addCallback(mCallback);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            mCameraView.start();
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
            showPermissionDialog();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA_PERMISSION);
        }
    }

    @Override
    protected void onPause() {
        mCameraView.stop();
        super.onPause();
    }

    @Override
    protected boolean onCreateActionMenu(Menu menu) {
        super.onCreateActionMenu(menu);
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    protected boolean alwaysShowActionMenu() {
        return true;
    }

    @Override
    protected int getDefaultAction() {
        return 1;
    }

    public void onDetectMenu(MenuItem item) {
        mTakeCounter = TAKE_COUNT;
        mCameraView.takePicture();
    }

    private void showPermissionDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.camera_permission_title)
                .setMessage(R.string.camera_permission_message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                    }
                })
                .create()
                .show();
    }

    private JSONObject processPicture(CameraView cameraView, final byte[] data) throws Exception {
        saveFile(data);
        return faceServer.process(data);
    }

    private void saveFile(final byte[] data) throws Exception {
        try (OutputStream os = new FileOutputStream(new File(getExternalFilesDir(null), "picture.jpg"))) {
            os.write(data);
        }
    }
}
