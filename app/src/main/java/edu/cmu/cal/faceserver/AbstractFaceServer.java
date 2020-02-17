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

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpMediaType;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.MultipartContent;
import com.google.api.client.http.UrlEncodedContent;
import com.google.api.client.http.javanet.NetHttpTransport;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.Map;

public abstract class AbstractFaceServer {
    private static final String TAG = "FaceServer";
    private final GenericUrl url;
    private final GenericUrl logUrl;
    private final HttpRequestFactory mRequestFactory = (new NetHttpTransport()).createRequestFactory();
    private final HttpMediaType mediaType = new HttpMediaType("multipart/form-data").setParameter("boundary", "__END_OF_PART__");
    private final HttpMediaType formType = new HttpMediaType("application/x-www-form-urlencoded");
    private JSONObject lastResult = null;

    public AbstractFaceServer(String url, String logUrl) {
        this.url = new GenericUrl(url);
        this.logUrl = new GenericUrl(logUrl);
    }

    public final JSONObject process(byte[] data) throws Exception {
        long t0 = System.nanoTime();
        MultipartContent content = new MultipartContent().setMediaType(mediaType);
        // do image compression before sending it to a server
        //Bitmap image = BitmapFactory.decodeByteArray(data, 0, data.length);
        //ByteArrayOutputStream stream = new ByteArrayOutputStream();
        //image.compress(Bitmap.CompressFormat.JPEG, 73, stream);
        //HttpContent imageContent = new ByteArrayContent("image/jpeg", stream.toByteArray());
        HttpContent imageContent = new ByteArrayContent("image/jpeg", data);
        String filename = String.format("%d.jpg", System.currentTimeMillis());
        addFormData(content, imageContent, filename);
        long t1 = System.nanoTime();
        HttpRequest request = mRequestFactory.buildPostRequest(url, content);
        addExtra(request);
        HttpResponse response = request.execute();
        int statusCode = response.getStatusCode();
        long t2 = System.nanoTime();
        String str = response.parseAsString();
        Log.d(TAG, String.format("request with image=%,dns, elapsed=%,dns, size=%,dbytes, status=%d\n%s", t1 - t0, t2 - t1, request.getContent().getLength(), statusCode, str));
        response.disconnect();

        JSONObject result = new JSONObject(str);
        result.put("filename", filename);

        return lastResult = statusCode == 200 ? result : new JSONObject().put("error", statusCode).put("content", str);
    }

    public final JSONObject writeLog(Map<String, String> data) throws Exception {
        long t0 = System.nanoTime();
        UrlEncodedContent content = new UrlEncodedContent(data);
        HttpRequest request = mRequestFactory.buildPostRequest(logUrl, content);
        addExtra(request);
        HttpResponse response = request.execute();
        int statusCode = response.getStatusCode();
        long t1 = System.nanoTime();
        String str = response.parseAsString();
        Log.d(TAG, String.format("logging request: elapsed=%,dns, size=%,dbytes, status=%d\n%s", t1 - t0, request.getContent().getLength(), statusCode, str));
        response.disconnect();
        return lastResult = statusCode == 200 ? new JSONObject(str) : new JSONObject().put("error", statusCode).put("content", str);
    }

    public final JSONObject getResultJSON() {
        return lastResult;
    }

    public int getRetryCount() {
        return 3;
    }

    public int getRepeatDelay() {
        return 10 * 1000;
    }

    public String getTakingText() {
        return null;
    }

    public String getRetryText() {
        return null;
    }

    public void reset() {
    }

    protected void addExtra(HttpRequest request) {
    }

    abstract public String getSpeakText();

    abstract protected void addFormData(MultipartContent content, HttpContent imageContent, String filename);

}
