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

/**
 * Author: Changemyminds.
 * Date: 2019/4/14.
 * Description:
 * Reference:
 */
public class SpeechManager extends AbstractSpeechManager implements ISpeech.ISpeechListener {
    private boolean mSpeakSuccess;
    private ISpeech mSpeech;

    public void setSpeech(ISpeech speech) {
        mSpeakSuccess = false;
        mSpeech = speech;

        if (mSpeech != null) {
            mSpeech.addSpeechListener(this);
        }
    }

    @Override
    public void startSpeak(String text) {
        if (mSpeech != null) {
            mSpeech.start(text);
        } else {
            if (mSpeechManger != null) {
                mSpeechManger.startSpeak(text);
            }
        }
    }

    @Override
    public void stopSpeak() {
        if (mSpeech != null && mSpeakSuccess) {
            mSpeech.stop();
        } else {
            if (mSpeechManger != null) {
                mSpeechManger.stopSpeak();
            }
        }
    }

    @Override
    public boolean isSpeaking() {
        if (mSpeech != null && mSpeakSuccess) {
            return mSpeech.isSpeaking();
        } else {
            if (mSpeechManger != null) {
                return mSpeechManger.isSpeaking();
            }
        }
        return false;
    }

    @Override
    public void pause() {
        if (mSpeech != null && mSpeakSuccess) {
            mSpeech.pause();
        } else {
            if (mSpeechManger != null) {
                mSpeechManger.pause();
            }
        }
    }

    @Override
    public void resume() {
        if (mSpeech != null && mSpeakSuccess) {
            mSpeech.resume();
        } else {
            if (mSpeechManger != null) {
                mSpeechManger.resume();
            }
        }
    }

    @Override
    public void onSuccess(String message) {
        mSpeakSuccess = true;
    }

    @Override
    public void onFailure(String message, Exception e) {
        mSpeakSuccess = false;
        if (super.mSpeechManger != null) {
            mSpeechManger.startSpeak(message);
        }
    }

    @Override
    public void dispose() {
        if (mSpeech != null) {
            mSpeech.exit();
        }

        if (mSpeechManger != null) {
            mSpeechManger.dispose();
        }
    }
}
