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
package darren.gcptts.model.android;

import java.util.Locale;

/**
 * Author: Changemyminds.
 * Date: 2018/6/24.
 * Description:
 * Reference:
 */
public class AndroidVoice {
    private Locale mLocale;
    private float mSpeakingRate;            // range: 0.0 ~ 2.0
    private float mPitch;                   // range: 1.0

    private AndroidVoice() {
        mLocale = Locale.ENGLISH;
        mSpeakingRate = 1.0f;
        mPitch = 1.0f;
    }

    private Locale translate(String language) {
        language = language.toLowerCase();
        Locale locale = Locale.ENGLISH;
        switch (language) {
            case "eng":
                locale = Locale.ENGLISH;
                break;
            case "cht":
                locale = Locale.TAIWAN;
                break;
            case "chi":
                locale = Locale.CHINA;
                break;
        }

        return locale;
    }

    public static class Builder {
        private AndroidVoice mAndroidVoice;

        public Builder() {
            mAndroidVoice = new AndroidVoice();
        }

        public Builder addPitch(float pitch) {
            mAndroidVoice.mPitch = pitch;
            return this;
        }

        public Builder addSpeakingRate(float speakingRate) {
            mAndroidVoice.mSpeakingRate = speakingRate;
            return this;
        }

        public Builder addLanguage(Locale locale) {
            mAndroidVoice.mLocale = locale;
            return this;
        }

        public Builder addLanguage(String language) {
            mAndroidVoice.mLocale = mAndroidVoice.translate(language);
            return this;
        }

        public AndroidVoice build() {
            return mAndroidVoice;
        }
    }


    public float getSpeakingRate() {
        return mSpeakingRate;
    }

    public float getPitch() {
        return mPitch;
    }

    public Locale getLocale() {
        return mLocale;
    }
}
