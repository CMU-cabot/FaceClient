package edu.cmu.cal.faceclient;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.TextView;

import com.vuzix.hud.actionmenu.ActionMenuActivity;
import com.vuzix.hud.actionmenu.SwitchActionMenuItemView;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import edu.cmu.cal.cameraview.CameraView;
import edu.cmu.cal.faceserver.AbstractFaceServer;
import edu.cmu.cal.faceserver.CMUFaceServer;

public class MainActivity extends ActionMenuActivity {

    private static final String TAG = "MainActivity";
    private static final int REQUEST_CAMERA_PERMISSION = 1;
    MenuItem mDetectMenu, mModeMenu, mFeetMenu;
    //    private AbstractFaceServer faceServer = new WatsonVisualRecognition();
    private AbstractFaceServer faceServer = new CMUFaceServer();
    private int mTakeCounter = 0;
    private CameraView mCameraView;
    private TextView mInfoView;
    private Handler mHandler, mBackgroundHandler;
    private TextToSpeech mTTS;
    private final Runnable repeatRunnable = new Runnable() {
        @Override
        public void run() {
            if (getCurrentMenuIndex() == 1) {
                if (mTTS.isSpeaking()) {
                    mHandler.postDelayed(this, 10);
                } else {
                    onDetectMenu(null);
                }
            }
        }
    };
    private File mFilesDir;
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
            mBackgroundHandler.post(new Runnable() {
                @Override
                public void run() {
                    mTakeCounter--;
                    String str, speakStr = null;
                    try {
                        JSONObject result = processPicture(cameraView, data);
                        str = faceServer.getSpeakText();
                    } catch (Exception e) {
                        e.printStackTrace();
                        speakStr = " ";//e.getMessage();
                        str = e.toString();
                    }
                    if (str.isEmpty()) {
                        str = getString(R.string.no_faces);
                    } else {
                        mTakeCounter = 0;
                    }
                    final String message = str;
                    final String speakMessage = speakStr;
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (mTakeCounter > 0 && getCurrentMenuIndex() == 1) {
                                showMessage(getString(R.string.retrying), faceServer.getRetryText());
                                mCameraView.takePicture();
                            } else {
                                showMessage(message, speakMessage);
                                mDetectMenu.setEnabled(true);
                                if (getCurrentMenuIndex() == 1) {
                                    mHandler.postDelayed(repeatRunnable, faceServer.getRepeatDelay());
                                }
                            }
                        }
                    });
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
        mFilesDir = getExternalFilesDir(null);
        mHandler = new Handler();
        mBackgroundHandler = new Handler(thread.getLooper());
        mCameraView = findViewById(R.id.camera);
        mInfoView = findViewById(R.id.info);
        if (mCameraView != null) {
            mCameraView.addCallback(mCallback);
        }
        mTTS = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                mTTS.setSpeechRate(1.75f);
            }
        });
        takeKeyEvents(true);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY) {
            onDetectMenu(null);
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mTTS != null) {
            mTTS.shutdown();
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
        mDetectMenu = menu.findItem(R.id.detect_menu);
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        mModeMenu = menu.findItem(R.id.mode_menu);
        setMode(prefs.getBoolean("mode", false));
        mModeMenu.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                boolean checked = !menuItem.isChecked();
                setMode(checked);
                prefs.edit().putBoolean("mode", checked).apply();
                return false;
            }
        });
        mFeetMenu = menu.findItem(R.id.feet_menu);
        setFeet(prefs.getBoolean("feet", true));
        mFeetMenu.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                boolean checked = !menuItem.isChecked();
                setFeet(checked);
                prefs.edit().putBoolean("feet", checked).apply();
                return false;
            }
        });
        return true;
    }

    private void setMode(boolean checked) {
        String title = checked ? "Mode B" : "Mode A";
        mModeMenu.setChecked(checked);
        mModeMenu.setTitle(title);
        SwitchActionMenuItemView view = (SwitchActionMenuItemView) mModeMenu.getActionView();
        if (view != null) {
            view.setChecked(checked);
            view.setTitle(title);
        }
        CMUFaceServer.mode = checked ? CMUFaceServer.MODE_B : CMUFaceServer.MODE_A;
    }

    private void setFeet(boolean checked) {
        String title = checked ? "Feet" : "Meter";
        mFeetMenu.setChecked(checked);
        mFeetMenu.setTitle(title);
        SwitchActionMenuItemView view = (SwitchActionMenuItemView) mFeetMenu.getActionView();
        if (view != null) {
            view.setChecked(checked);
            view.setTitle(title);
        }
        CMUFaceServer.metersPerUnit = checked ? CMUFaceServer.FEET : CMUFaceServer.METER;
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
        mHandler.removeCallbacks(repeatRunnable);
        if (mDetectMenu.isEnabled()) {
            mDetectMenu.setEnabled(false);
            mTakeCounter = faceServer.getRetryCount();
            showMessage(getString(R.string.taking), faceServer.getTakingText());
            mCameraView.takePicture();
        }
    }

    public void onSingleMenu(MenuItem item) {
        faceServer.reset();
        onDetectMenu(item);
    }

    private void showMessage(String message, String speakText) {
        mInfoView.setText(message);
        mTTS.speak(speakText != null ? speakText : message, TextToSpeech.QUEUE_FLUSH, null, null);
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
//        saveFile(data);
        return faceServer.process(data);
    }

    private void saveFile(final byte[] data) throws Exception {
        try (OutputStream os = new FileOutputStream(new File(mFilesDir, "picture.jpg"))) {
            os.write(data);
        }
    }
}
