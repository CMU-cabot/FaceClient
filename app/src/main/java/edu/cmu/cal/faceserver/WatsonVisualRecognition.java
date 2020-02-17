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
package edu.cmu.cal.faceserver;

import com.google.api.client.http.BasicAuthentication;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.MultipartContent;

import org.json.JSONArray;
import org.json.JSONObject;

public class WatsonVisualRecognition extends AbstractFaceServer {
    private static final String url = "https://gateway.watsonplatform.net/visual-recognition/api/v3/detect_faces?version=2018-03-19";
    private static final String log_url = "";
    private static final String key = "0ZG50T7jhQYjHcPbvSQr5f2q7TIQqyX0JQD_AWYbT6xN";

    public WatsonVisualRecognition() {
        super(url, log_url);
    }

    @Override
    protected void addFormData(MultipartContent content, HttpContent imageContent, String filename) {
        HttpHeaders imageHeaders = new HttpHeaders().set("Content-Disposition", String.format("form-data; name=\"images_file\"; filename=\"%s\"", filename));
        content.addPart(new MultipartContent.Part(imageHeaders, imageContent));
    }

    @Override
    protected void addExtra(HttpRequest request) {
        request.setWriteTimeout(10 * 1000);
        request.setInterceptor(new BasicAuthentication("apikey", key));
    }

    @Override
    public String getSpeakText() {
        JSONObject json = getResultJSON();
        StringBuffer sb = new StringBuffer();
        if (json != null) {
            JSONArray images = json.optJSONArray("images");
            if (images != null && images.length() > 0) {
                JSONArray faces = images.optJSONObject(0).optJSONArray("faces");
                if (faces != null && faces.length() > 0) {
                    if (faces.length() > 1) {
                        sb.append(faces.length() + " faces detected.\n");
                    }
                    for (int i = 0; i < faces.length(); i++) {
                        JSONObject gender = faces.optJSONObject(i).optJSONObject("gender");
                        JSONObject age = faces.optJSONObject(i).optJSONObject("age");
                        sb.append(String.format("\n%s, %s to %s years old.\n", gender.opt("gender_label"), age.opt("min"), age.opt("max")));
//                        sb.append(String.format("score: gender %s, age %s.\n", gender.opt("score"), age.opt("score")));
                    }
                }
            }
        }
        return sb.toString();
    }
}

