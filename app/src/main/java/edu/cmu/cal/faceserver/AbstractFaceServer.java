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
import com.google.api.client.http.javanet.NetHttpTransport;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;

public abstract class AbstractFaceServer {
    private static final String TAG = "FaceServer";
    private final GenericUrl url;
    private final HttpRequestFactory mRequestFactory = (new NetHttpTransport()).createRequestFactory();
    private final HttpMediaType mediaType = new HttpMediaType("multipart/form-data").setParameter("boundary", "__END_OF_PART__");
    private JSONObject lastResult = null;

    public AbstractFaceServer(String url) {
        this.url = new GenericUrl(url);
    }

    public final JSONObject process(byte[] data) throws Exception {
        long t0 = System.nanoTime();
        MultipartContent content = new MultipartContent().setMediaType(mediaType);
        // do image compression before sending it to a server
        Bitmap image = BitmapFactory.decodeByteArray(data, 0, data.length);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 73, stream);
        HttpContent imageContent = new ByteArrayContent("image/jpeg", stream.toByteArray());
//        HttpContent imageContent = new ByteArrayContent("image/jpeg", data);
        addFormData(content, imageContent);
        long t1 = System.nanoTime();
        HttpRequest request = mRequestFactory.buildPostRequest(url, content);
        addExtra(request);
        HttpResponse response = request.execute();
        int statusCode = response.getStatusCode();
        long t2 = System.nanoTime();
        String str = response.parseAsString();
        Log.d(TAG, String.format("request with image=%,dns, elapsed=%,dns, size=%,dbytes, status=%d\n%s", t1 - t0, t2 - t1, request.getContent().getLength(), statusCode, str));
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

    abstract protected void addFormData(MultipartContent content, HttpContent imageContent);

}
