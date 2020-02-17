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
package darren.gcptts.model.gcp;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: Changemyminds.
 * Date: 2018/6/22.
 * Description:
 * Reference:
 */
public class VoiceList {
    private static final String TAG = VoiceList.class.getName();

    private List<IVoiceListener> mVoiceListeners = new ArrayList<>();

    public VoiceList() {
    }

    public void start() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient okHttpClient = new OkHttpClient();
                Request request = new Request.Builder()
                        .url(Config.VOICES_ENDPOINT)
                        .addHeader(Config.API_KEY_HEADER, Config.API_KEY)
                        .build();
                try {
                    Response response = okHttpClient.newCall(request).execute();
                    if (response.isSuccessful()) {
                        onSuccess(response.body().string());
                    } else {
                        throw new IOException(String.valueOf(response.code()));
                    }
                } catch (IOException IoEx) {
                    IoEx.printStackTrace();
                    onError(IoEx.getMessage());
                }
            }
        }).start();
    }

    public void addVoiceListener(IVoiceListener voiceListener) {
        mVoiceListeners.add(voiceListener);
    }

    private void onSuccess(String text) {
        for (IVoiceListener voiceListener : mVoiceListeners) {
            voiceListener.onResponse(text);
        }
    }

    private void onError(String error) {
        for (IVoiceListener voiceListener : mVoiceListeners) {
            voiceListener.onFailure(error);
        }
    }

    public interface IVoiceListener {
        void onResponse(String text);

        void onFailure(String error);
    }
}
