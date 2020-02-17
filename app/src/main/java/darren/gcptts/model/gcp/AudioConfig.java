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

import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: Changemyminds.
 * Date: 2018/6/22.
 * Description:
 * Reference:
 */
public class AudioConfig implements VoiceParameter {
    private EAudioEncoding mEAudioEncoding;
    private float mSpeakingRate;            // range: 0.25 ~ 4.00
    private float mPitch;                   // range: -20.00 ~ 20.00
    private int mVolumeGainDb;
    private int mSampleRateHertz;

    private AudioConfig() {
        mEAudioEncoding = EAudioEncoding.LINEAR16;
        mSpeakingRate = 1.0f;
        mPitch = 0.0f;
        mVolumeGainDb = 0;
        mSampleRateHertz = 0;
    }

    public static class Builder {
        private AudioConfig mAudioConfig;

        public Builder() {
            mAudioConfig = new AudioConfig();
        }

        public Builder addAudioEncoding(EAudioEncoding EAudioEncoding) {
            mAudioConfig.mEAudioEncoding = EAudioEncoding;
            return this;
        }

        public Builder addSpeakingRate(float speakingRate) {
            mAudioConfig.mSpeakingRate = speakingRate;
            return this;
        }

        public Builder addPitch(float pitch) {
            mAudioConfig.mPitch = pitch;
            return this;
        }

        public Builder addVolumeGainDb(int volumeGainDb) {
            mAudioConfig.mVolumeGainDb = volumeGainDb;
            return this;
        }

        public Builder addSampleRateHertz(int sampleRateHertz) {
            mAudioConfig.mSampleRateHertz = sampleRateHertz;
            return this;
        }

        public AudioConfig build() {
            return mAudioConfig;
        }
    }

    @Override
    public String getJSONHeader() {
        return "audioConfig";
    }

     @Override
    public JSONObject toJSONObject(){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("audioEncoding", mEAudioEncoding.toString());
            jsonObject.put("speakingRate", String.valueOf(mSpeakingRate));
            jsonObject.put("pitch", getPitch());
            return jsonObject;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Deprecated
    @Override
    public String toString() {
        String text = "'audioConfig':{";
        text += "'audioEncoding':'" + mEAudioEncoding.toString() + "',";
        text += "'speakingRate':'" + String.valueOf(mSpeakingRate) + "',";
        text += "'pitch':'" + String.valueOf(mPitch) + "'";
        text += (mVolumeGainDb == 0) ? "" : ",'" + String.valueOf(mVolumeGainDb) + "'";
        text += (mSampleRateHertz == 0) ? "" : ",'" + String.valueOf(mSampleRateHertz) + "'";
        text += "}";
        return text;
    }

    private String getPitch(){
        List<String> pitchList = new ArrayList<>();
        pitchList.add(String.valueOf(mPitch));
        if ((mVolumeGainDb != 0)) {
            pitchList.add(String.valueOf(mVolumeGainDb));
        }
        if (mSampleRateHertz != 0) {
            pitchList.add(String.valueOf(mSampleRateHertz));
        }
        return pitchList.toString().replace("[", "").replace("]", "");
    }
}
