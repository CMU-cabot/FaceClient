package edu.cmu.cal.watsontts;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;

import com.ibm.watson.developer_cloud.android.library.audio.StreamPlayer;
import com.ibm.watson.developer_cloud.http.ServiceCall;
import com.ibm.watson.developer_cloud.service.security.IamOptions;
import com.ibm.watson.developer_cloud.text_to_speech.v1.TextToSpeech;
import com.ibm.watson.developer_cloud.text_to_speech.v1.model.GetVoiceOptions;
import com.ibm.watson.developer_cloud.text_to_speech.v1.model.SynthesizeOptions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import edu.cmu.cal.faceclient.MainActivity;

public class TTS {
    private static final String TAG = "TTS";
    private static TTS instance = null;

    private static final String apikey = "s9Jsq5NR1zuMK-nxRPWsQyT_tBfq84QbZZO6Tdc2ktdB";
    private static final String endpoint = "https://stream.watsonplatform.net/text-to-speech/api";

    public static TTS getInstance(){
        if (TTS.instance == null) {
            TTS.instance = new TTS();
        }
        return TTS.instance;
    }
    private TextToSpeech service;
    private Handler mTTSHandler;
    public TTS(){
        service = new TextToSpeech();
        IamOptions options = new IamOptions.Builder()
                .apiKey(apikey)
                .build();
        service.setIamCredentials(options);

        HandlerThread thread = new HandlerThread("tts");
        thread.start();
        mTTSHandler = new Handler(thread.getLooper());
    }

    private void pipe(InputStream is, FileOutputStream os) throws IOException {
        int n;
        byte[] buffer = new byte[1024];
        while ((n = is.read(buffer)) > -1) {
            os.write(buffer, 0, n);   // Don't allow any extra bytes to creep in, final write
        }
        os.close();
    }

    private String getOutputMediaFilePath(String text){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_MUSIC), "Client");

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d(TAG, "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String prefix = text.replaceAll("[^a-zA-Z0-9]", "_");
        return mediaStorageDir.getPath() + File.separator + prefix + ".wav";
    }

    public boolean speak(final String text, final float playbackSpeed) {
        mTTSHandler.post(new Runnable() {
            public void run() {
                try {
                    StreamPlayer streamPlayer = new StreamPlayer();
                    SynthesizeOptions option =
                            new SynthesizeOptions.Builder()
                                    .text(text)
                                    .voice(SynthesizeOptions.Voice.EN_US_MICHAELVOICE)
                                    .accept("audio/l16;rate=22050")
                                    //.accept(SynthesizeOptions.Accept.AUDIO_FLAC)
                                    //.accept(SynthesizeOptions.Accept.AUDIO_OGG_CODECS_VORBIS)
                                    .build();
                    final String filename = getOutputMediaFilePath(text);
                    try {
                        File file = new File(filename);
                        if (!file.exists()) {
                            ServiceCall<InputStream> call = service.synthesize(option);
                            InputStream in = call.execute();
                            Log.d(TAG, file.getAbsolutePath());
                            Log.d(TAG, filename);
                            FileOutputStream out = new FileOutputStream(file);
                            pipe(in, out);
                            in.close();
                            out.close();
                        }

//
                        /*
                        final SoundPool soundPool = new SoundPool(4, AudioManager.STREAM_MUSIC, 100);

                        final int soundId = soundPool.load(filename, 1);
                        AudioManager mgr = (AudioManager) MainActivity.getAppContext().getSystemService(Context.AUDIO_SERVICE);
                        final float volume = mgr.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

                        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener()
                        {
                            @Override
                            public void onLoadComplete(SoundPool arg0, int arg1, int arg2)
                            {
                                soundPool.play(soundId, 1, 1, 1, 0, playbackSpeed);
                            }
                        });
                        */
                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        return true;
    }
}