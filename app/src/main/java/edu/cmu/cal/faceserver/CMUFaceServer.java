package edu.cmu.cal.faceserver;

import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.MultipartContent;

import org.json.JSONObject;

import java.util.Iterator;

public class CMUFaceServer extends AbstractFaceServer {
    private static final String url = "http://faceserver.cal.cmu.edu:5000/test?uuid=1234&phase=dev&model=insightface&mode=recognition";
    private static final HttpHeaders imageHeaders = new HttpHeaders().set("Content-Disposition", "form-data; name=\"file\"; filename=\"picture.jpg\"");

    public CMUFaceServer() {
        super(url);
    }

    @Override
    protected void addFormData(MultipartContent content, HttpContent imageContent) {
        content.addPart(new MultipartContent.Part(imageHeaders, imageContent));
    }

    @Override
    public String getSpeakText() {
        JSONObject face = getResultJSON();
        StringBuffer sb = new StringBuffer();
        if (face != null) {
            for (Iterator<String> it = face.keys(); it.hasNext(); ) {
                String key = it.next();
                sb.append(String.format("%s is %s\n", key, face.opt(key)));
            }
        }
        return sb.toString();
    }
}
