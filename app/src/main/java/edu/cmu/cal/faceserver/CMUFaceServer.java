package edu.cmu.cal.faceserver;

import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.MultipartContent;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class CMUFaceServer extends AbstractFaceServer {
    private static final String url = "http://cal.ri.cmu.edu:5000/test";
    private static final String uuid = UUID.randomUUID().toString();
    private static final double name_thres = 0.5, gender_thres = 0.5, age_thres = 0.5, gaze_thres = 0.5;
    private static final HttpHeaders imageHeaders = new HttpHeaders().set("Content-Disposition", "form-data; name=\"file\"; filename=\"picture.jpg\"");
    private final Map<String, String> mParameters = new HashMap();

    public CMUFaceServer() {
        super(url);
        mParameters.put("uuid", uuid);
        mParameters.put("phase", "dev");
        mParameters.put("model", "insightface");
        mParameters.put("mode", "recognition");
    }

    @Override
    protected void addFormData(MultipartContent content, HttpContent imageContent) {
        for (Iterator<String> it = mParameters.keySet().iterator(); it.hasNext(); ) {
            String name = it.next();
            HttpHeaders headers = new HttpHeaders().set("Content-Disposition", String.format("form-data; name=\"%s\"", name));
            content.addPart(new MultipartContent.Part(headers, ByteArrayContent.fromString(null, mParameters.get(name))));
        }
        content.addPart(new MultipartContent.Part(imageHeaders, imageContent));
    }

    @Override
    protected void addExtra(HttpRequest request) {
        request.setWriteTimeout(10 * 1000);
    }

    @Override
    public String getSpeakText() {
        JSONObject face = getResultJSON();
        StringBuffer sb = new StringBuffer();
        if (face != null) {
            boolean detected = face.optBoolean("detected");
            if (detected) {
                if (face.optDouble("name_conf", 0) > name_thres) {
                    String name = face.optString("name");
                    if (name != null && !"null".equals(name) && !"{}".equals(name)) {
                        sb.append(String.format("%s,\n", name));
                    }
                }
                if (face.optDouble("gender_conf", 0) > gender_thres) {
                    String gender = face.optString("gender");
                    if ("M".equals(gender)) {
                        sb.append("male,\n");
                    } else if ("F".equals(gender)) {
                        sb.append("female,\n");
                    }
                }
                if (face.optDouble("age_conf", 0) > age_thres) {
                    int age = face.optInt("age", -1);
                    if (age >= 0) {
                        sb.append(String.format("%d years old,\n", age));
                    }
                }
                if (face.optDouble("gaze_conf", 0) > gaze_thres) {
                    boolean gaze = face.optBoolean("gaze");
                    if (gaze) {
                        sb.append("looking at you,\n");
                    }
                }
                double distance = face.optDouble("distance", -1);
                if (distance > 0) {
                    sb.append(String.format("%.1f meters away,\n", distance));
                }
            }
        }
        return sb.toString();
    }
}
