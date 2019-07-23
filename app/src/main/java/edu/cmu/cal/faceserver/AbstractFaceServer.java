package edu.cmu.cal.faceserver;

import com.google.api.client.http.HttpMediaType;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.MultipartContent;
import com.google.api.client.http.javanet.NetHttpTransport;

import org.json.JSONObject;

public abstract class AbstractFaceServer {
    protected HttpRequestFactory mRequestFactory = (new NetHttpTransport()).createRequestFactory();
    protected HttpMediaType mediaType = new HttpMediaType("multipart/form-data").setParameter("boundary", "__END_OF_PART__");

    abstract JSONObject process(MultipartContent content, byte[] data) throws Exception;

    public JSONObject process(byte[] data) throws Exception {
        return process(new MultipartContent().setMediaType(mediaType), data);
    }
}
