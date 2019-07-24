package edu.cmu.cal.faceserver;

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

public abstract class AbstractFaceServer {
    private final GenericUrl url;
    private final HttpRequestFactory mRequestFactory = (new NetHttpTransport()).createRequestFactory();
    private final HttpMediaType mediaType = new HttpMediaType("multipart/form-data").setParameter("boundary", "__END_OF_PART__");
    private JSONObject lastResult = null;

    public AbstractFaceServer(String url) {
        this.url = new GenericUrl(url);
    }

    public final JSONObject process(byte[] data) throws Exception {
        MultipartContent content = new MultipartContent().setMediaType(mediaType);
        HttpContent imageContent = new ByteArrayContent("image/jpeg", data);
        addFormData(content, imageContent);
        HttpRequest request = mRequestFactory.buildPostRequest(url, content);
        addExtra(request);
        HttpResponse response = request.execute();
        int statusCode = response.getStatusCode();
        String str = response.parseAsString();
        Log.d("AbstractFaceServer", statusCode + ": " + str);
        return lastResult = statusCode == 200 ? new JSONObject(str) : new JSONObject().put("error", statusCode).put("content", str);
    }

    public final JSONObject getResultJSON() {
        return lastResult;
    }

    protected void addExtra(HttpRequest request) {
    }

    abstract public String getSpeakText();

    abstract protected void addFormData(MultipartContent content, HttpContent imageContent);

}
