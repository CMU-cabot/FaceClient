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
package edu.cmu.cal.faceclient;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;

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
import java.util.HashMap;
import java.util.Map;

import darren.gcptts.model.SpeechManager;
import edu.cmu.cal.cameraview.CameraView;
import edu.cmu.cal.faceserver.AbstractFaceServer;
import edu.cmu.cal.faceserver.CMUFaceServer;
import edu.cmu.cal.watsontts.TTS;
import darren.gcptts.model.GCPTTSAdapter;
import darren.gcptts.model.gcp.AudioConfig;
import darren.gcptts.model.gcp.EAudioEncoding;
import darren.gcptts.model.gcp.GCPVoice;

public class MainActivity extends ActionMenuActivity {

    private static Context context;

    public static Context getAppContext() {
        return MainActivity.context;
    }

    private static final String TAG = "MainActivity";
    private static final int REQUEST_CAMERA_PERMISSION = 1;
    private final float RATE_15 = 2.0f, RATE_18 = 3.0f;
    MenuItem mDetectMenu, mModeMenu, mFriendMenu, mRateMenu, mFeetMenu;
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
    private long mStartTime = 0;
    private long mInitTime = -1;
    private long mShutterCount = 0;
    private double mTakingPictureTime = 0;
    private double mTotalTime = 0;
    private float mSpeechRate = RATE_15;

    private SpeechManager mSpeechManager;
    private GCPTTSAdapter tts;

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
                    mTakingPictureTime = (System.nanoTime() - mStartTime)/1000000000.0;
                    mShutterCount++;
                    mTakeCounter--;
                    String str, speakStr = null, fn = null;
                    try {
                        JSONObject result = processPicture(cameraView, data);
                        str = faceServer.getSpeakText();
                        fn = result.getString("filename");
                    } catch (Exception e) {
                        e.printStackTrace();
                        speakStr = "";//e.getMessage();
                        str = e.toString();
                    }
                    if (str.isEmpty()) {
                        str = getString(R.string.no_faces);
                    } else {
                        mTakeCounter = 0;
                        speakStr = str;
                    }
                    final String message = str;
                    final String speakMessage = speakStr;
                    final String filename = fn;
                    mTotalTime = (System.nanoTime() - mStartTime)/1000000000.0;
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (mTakeCounter > 0 && getCurrentMenuIndex() == 1) {
                                mStartTime = System.nanoTime();
                                mCameraView.takePicture();
                                showMessage(getString(R.string.retrying), faceServer.getRetryText(), filename);
                            } else {
                                showMessage(message, speakMessage, filename);
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
        MainActivity.context = getApplicationContext();
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
                mTTS.setSpeechRate(mSpeechRate);
                /*
                List<TextToSpeech.EngineInfo> engines = mTTS.getEngines();
                for (TextToSpeech.EngineInfo engine: engines) {
                    Log.d(TAG, String.format("label: %s, name: %s", engine.label, engine.name));
                }
                */
            }
        });
        takeKeyEvents(true);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        mSpeechManager = new SpeechManager();
        tts = new GCPTTSAdapter();
        mSpeechManager.setSpeech(tts);
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
        // mode
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
        // friend mode
        mFriendMenu = menu.findItem(R.id.friend_menu);
        setFriend(prefs.getBoolean("friend", true));
        mFriendMenu.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                boolean checked = !menuItem.isChecked();
                setFriend(checked);
                prefs.edit().putBoolean("friend", checked).apply();
                return false;
            }
        });
        // speech mode (A: 1.5 / B: 1.8)
        mRateMenu = menu.findItem(R.id.rate_menu);
        setRate(prefs.getBoolean("rate", false));
        mRateMenu.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                boolean checked = !menuItem.isChecked();
                setRate(checked);
                prefs.edit().putBoolean("rate", checked).apply();
                return false;
            }
        });
        // feet mode
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

    private void setFriend(boolean checked) {
        String title = checked ? "Friend" : "NoFriend";
        mFriendMenu.setChecked(checked);
        mFriendMenu.setTitle(title);
        SwitchActionMenuItemView view = (SwitchActionMenuItemView) mFriendMenu.getActionView();
        if (view != null) {
            view.setChecked(checked);
            view.setTitle(title);
        }
        CMUFaceServer.f_mode = checked ? CMUFaceServer.FRIEND : CMUFaceServer.NO_FRIEND;
    }

    private void setRate(boolean checked) {
        String title = checked ? "fast" : "slow";
        mRateMenu.setChecked(checked);
        mRateMenu.setTitle(title);
        SwitchActionMenuItemView view = (SwitchActionMenuItemView) mRateMenu.getActionView();
        if (view != null) {
            view.setChecked(checked);
            view.setTitle(title);
        }
        mSpeechRate = checked ? RATE_18 : RATE_15;
        mTTS.setSpeechRate(mSpeechRate);
        Log.d(TAG, String.format("SpeechRate: %f", mSpeechRate));
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
            if (mInitTime < 0) {
                mInitTime = System.nanoTime();
                mShutterCount = 0;
            }
            mStartTime = System.nanoTime();
            mCameraView.takePicture();
            showMessage(getString(R.string.taking), faceServer.getTakingText(), null);
        }
    }

    public void onSingleMenu(MenuItem item) {
        //faceServer.reset();
        //onDetectMenu(item);
        //TTS.getInstance().speak("Hello World", mSpeechRate);
        speak("Male, 20s, 20 feet, not looking at you.", null);
    }

    private void showMessage(String message, String speakText, String filename) {
        double fps = mShutterCount / ((System.nanoTime()-mInitTime)/1000000000.0);
        mInfoView.setText(message + String.format(" TP:%.2f TT:%.2f FPS:%.2f", mTakingPictureTime, mTotalTime, fps));
        //mTTS.speak(speakText != null ? speakText : message, TextToSpeech.QUEUE_FLUSH, null, null);
        if(speakText != null && speakText.length() > 0) {
            speak(speakText, filename);
        }
    }
    private void speak(final String text, final String filename) {

        if (mSpeechManager.isSpeaking()) {
            return;
        }

        if (filename != null) {
            mBackgroundHandler.post(new Runnable() {
                @Override
                public void run() {
                    Map<String, String> log = new HashMap<>();
                    log.put("filename", filename);
                    log.put("message", text);
                    log.put("timestamp", "" + System.currentTimeMillis());
                    log.put("type", "message");
                    try {
                        faceServer.writeLog(log);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        String languageCode = "en-US";
        String name = "en-US-Standard-C";
        //String name = "en-US-Wavenet-A";
        float pitch = 0f;

        GCPVoice gcpVoice = new GCPVoice(languageCode, name);
        AudioConfig audioConfig = new AudioConfig.Builder()
                .addAudioEncoding(EAudioEncoding.MP3)
                .addSpeakingRate(mSpeechRate)
                .addPitch(pitch)
                .build();

        tts.setGCPVoice(gcpVoice);
        tts.setAudioConfig(audioConfig);
        mSpeechManager.startSpeak(text);
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
