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
package darren.gcptts.model;

import android.content.Context;
import java.util.ArrayList;
import java.util.List;
import darren.gcptts.model.android.AndroidTTS;

/**
 * Author: Changemyminds.
 * Date: 2018/6/24.
 * Description:
 * Reference:
 */
public class AndroidTTSAdapter extends AndroidTTS implements ISpeech, AndroidTTS.ISpeakListener {
    private List<ISpeechListener> mSpeechListeners;

    public AndroidTTSAdapter(Context context) {
        super(context);
        mSpeechListeners = new ArrayList<>();
        addSpeakListener(this);
    }

    @Override
    public void start(String text) {
        speak(text);
    }

    @Override
    public void resume() {
        // not implements
    }

    @Override
    public void pause() {
        // not implements
    }

    @Override
    public void stop() {
        super.stop();
    }

    @Override
    public boolean isSpeaking() {
        return false;
    }

    @Override
    public void exit() {
        // release resources
        super.exit();
        removeSpeakListener(this);
        mSpeechListeners.clear();
    }

    @Override
    public void addSpeechListener(ISpeechListener speechListener) {
        mSpeechListeners.add(speechListener);
    }

    @Override
    public void removeSpeechListener(ISpeechListener speechListener) {
        mSpeechListeners.remove(speechListener);
    }

    @Override
    public void onSuccess(String message) {
        for (ISpeechListener speechListener : mSpeechListeners) {
            speechListener.onSuccess(message);
        }
    }

    @Override
    public void onFailure(String errorMessage) {
        for (ISpeechListener speechListener : mSpeechListeners) {
            speechListener.onFailure(null, new Exception(errorMessage));
        }
    }
}
