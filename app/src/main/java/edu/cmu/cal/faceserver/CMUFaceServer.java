package edu.cmu.cal.faceserver;

import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.MultipartContent;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class CMUFaceServer extends AbstractFaceServer {
    private static final String url = "http://cal.ri.cmu.edu:5000/test";
    private static final HttpHeaders imageHeaders = new HttpHeaders().set("Content-Disposition", "form-data; name=\"file\"; filename=\"picture.jpg\"");
    Map<String, String> mParameters = new HashMap();

    public CMUFaceServer() {
        super(url);
        mParameters.put("uuid", "1234");
        mParameters.put("phase", "dev");
        mParameters.put("model", "insightface");
        mParameters.put("mode", "recognition");
    }

    @Override
    protected void addFormData(MultipartContent content, HttpContent imageContent) {
        for (Iterator<String> it = mParameters.keySet().iterator(); it.hasNext(); ) {
            String name = it.next();
            HttpHeaders headers = new HttpHeaders().set("Content-Disposition", String.format("form-data; name=\"%s\"", name));
            content.addPart(new MultipartContent.Part(headers, new ByteArrayContent(null, mParameters.get(name).getBytes())));
        }
        content.addPart(new MultipartContent.Part(imageHeaders, imageContent));
    }

    @Override
    public String getSpeakText() {
        JSONObject face = getResultJSON();
        StringBuffer sb = new StringBuffer();
        if (face != null) {
            boolean detected = face.optBoolean("detected");
            if (detected) {
                String name = face.optString("name");
                String gender = face.optString("gender");
                int age = face.optInt("age", -1);
                double distance = face.optDouble("distance", -1);
                double conf = face.optDouble("conf", -1);
                if (name != null && !"null".equals(name) && !"{}".equals(name)) {
                    sb.append(String.format("%s,\n", name));
                }
                if ("M".equals(gender)) {
                    sb.append("male,\n");
                } else if ("F".equals(gender)) {
                    sb.append("female,\n");
                }
                if (age >= 0) {
                    sb.append(String.format("%d years old,\n", age));
                }
                if (distance > 0) {
                    sb.append(String.format("%.2f meters away,\n", distance));
                }
                if (conf > 0) {
                    sb.append(String.format("confidence %.2f%%.\n", conf * 100));
                }
            }
        }
        return sb.toString();
    }
}
