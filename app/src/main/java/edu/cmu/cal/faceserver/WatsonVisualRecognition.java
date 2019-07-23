package edu.cmu.cal.faceserver;

import android.util.Log;

import com.google.api.client.http.BasicAuthentication;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.MultipartContent;

import org.json.JSONObject;

public class WatsonVisualRecognition extends AbstractFaceServer {
    private static final String watson_apikey = "0ZG50T7jhQYjHcPbvSQr5f2q7TIQqyX0JQD_AWYbT6xN";
    private static final GenericUrl watson_url = new GenericUrl("https://gateway.watsonplatform.net/visual-recognition/api/v3/detect_faces?version=2018-03-19");

    @Override
    JSONObject process(MultipartContent content, byte[] data) throws Exception {
        HttpHeaders partHeaders = new HttpHeaders().set("Content-Disposition", "form-data; name=\"images_file\"; filename=\"picture.jpg\"");
        content.addPart(new MultipartContent.Part(partHeaders, new ByteArrayContent("image/jpeg", data)));
        HttpRequest request = mRequestFactory.buildPostRequest(watson_url, content).setInterceptor(new BasicAuthentication("apikey", watson_apikey));
        HttpResponse response = request.execute();
        JSONObject result = new JSONObject().put("status", response.getStatusCode());
        if (response.getStatusCode() == 200) {
            String str = response.parseAsString();
            Log.d("WatsonVisualRecognition", str);
            result = new JSONObject(str);
        }
        return result;
    }
}

