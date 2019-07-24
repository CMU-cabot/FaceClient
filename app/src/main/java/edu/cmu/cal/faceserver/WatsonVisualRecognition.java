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
    private static final String key = "0ZG50T7jhQYjHcPbvSQr5f2q7TIQqyX0JQD_AWYbT6xN";
    private static final HttpHeaders imageHeaders = new HttpHeaders().set("Content-Disposition", "form-data; name=\"images_file\"; filename=\"picture.jpg\"");

    public WatsonVisualRecognition() {
        super(url);
    }

    @Override
    protected void addFormData(MultipartContent content, HttpContent imageContent) {
        content.addPart(new MultipartContent.Part(imageHeaders, imageContent));
    }

    @Override
    protected void addExtra(HttpRequest request) {
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
                        sb.append(String.format("\n%s. %s to %s years old.\n", gender.opt("gender_label"), age.opt("min"), age.opt("max")));
//                        sb.append(String.format("score: gender %s, age %s.\n", gender.opt("score"), age.opt("score")));
                    }
                }
            }
        }
        return sb.toString();
    }
}

