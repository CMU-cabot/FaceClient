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

/**
 * Author: Changemyminds.
 * Date: 2018/6/23.
 * Description:
 * Reference:
 */
public class GCPVoice implements VoiceParameter {
    private String mLanguageCode;
    private String mName;
    private ESSMLlVoiceGender mESSMLlGender;
    private int mNaturalSampleRateHertz;

    public GCPVoice(String languageCode, String name) {
        mLanguageCode = languageCode;
        mName = name;
        mESSMLlGender = ESSMLlVoiceGender.NONE;
        mNaturalSampleRateHertz = 0;
    }

    public GCPVoice(String languageCode, String name, ESSMLlVoiceGender eSSMLlGender) {
        mLanguageCode = languageCode;
        mName = name;
        mESSMLlGender = eSSMLlGender;
        mNaturalSampleRateHertz = 0;
    }

    public GCPVoice(String languageCode, String name, ESSMLlVoiceGender eSSMLlGender,
                    int naturalSampleRateHertz) {
        mLanguageCode = languageCode;
        mName = name;
        mESSMLlGender = eSSMLlGender;
        mNaturalSampleRateHertz = naturalSampleRateHertz;
    }

    @Override
    public String getJSONHeader() {
        return "voice";
    }

    public JSONObject toJSONObject(){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("languageCode", mLanguageCode);
            jsonObject.put("name", mName);
            if (mESSMLlGender != ESSMLlVoiceGender.NONE){
                jsonObject.put("name", mESSMLlGender.toString());
            }
            if ((mNaturalSampleRateHertz != 0)) {
                jsonObject.put("naturalSampleRateHertz", String.valueOf(mNaturalSampleRateHertz));
            }
            return jsonObject;
        } catch (JSONException e) {
//            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return null;
    }


    @Deprecated
    @Override
    public String toString() {
        String text = "'voice':{";
        text += "'languageCode':'" + mLanguageCode + "',";
        text += "'name':'" + mName + "'";
        text += (mESSMLlGender == ESSMLlVoiceGender.NONE) ? "" :
                ",'ssmlGender':'" + mESSMLlGender.toString() + "'";
        text += (mNaturalSampleRateHertz == 0) ? "" :
                ",'naturalSampleRateHertz':'" + String.valueOf(mNaturalSampleRateHertz) + "'";
        text += "}";
        return text;
    }

    public String getLanguageCode() {
        return mLanguageCode;
    }

    public String getName() {
        return mName;
    }

    public ESSMLlVoiceGender getESSMLlGender() {
        return mESSMLlGender;
    }

    public int getNaturalSampleRateHertz() {
        return mNaturalSampleRateHertz;
    }
}
