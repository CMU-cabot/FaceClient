package edu.cmu.cal.faceserver;

import android.util.Log;

import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.MultipartContent;

import org.json.JSONException;
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
    private static final String[] KEYS = {"name", "gender", "age", "distance", "position", "gaze"};
    private final Map<String, String> mParameters = new HashMap();
    private JSONObject lastSpeak = new JSONObject();

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
//        request.getHeaders().set("Connection", "close");
    }

    @Override
    public int getRetryCount() {
        return 5;
    }

    @Override
    public int getRepeatDelay() {
        return 0;
    }

    @Override
    public String getTakingText() {
        return ""; // mute
    }

    @Override
    public String getRetryText() {
        return ""; // mute
    }

    @Override
    public String getSpeakText() {
        JSONObject json = buildSpeak();
        try {
            Log.d("CMUFaceServer", json.toString(4));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        StringBuffer sb = new StringBuffer();
        boolean hasText = false;
        for (String key : KEYS) {
            String text = json.optString(key, null);
            if (text != null) {
                hasText = true;
                if (!text.equals(lastSpeak.optString(key))) {
                    sb.append(String.format("%s,\n", text));
                }
            }
        }
        lastSpeak = json;
        if (sb.length() == 0 && hasText) {
            return " ";
        }
        return sb.toString();
    }

    private JSONObject buildSpeak() {
        JSONObject obj = new JSONObject();
        JSONObject face = getResultJSON();
        if (face != null && face.optBoolean("detected")) {
            try {
                if (face.optDouble("name_conf", 0) > name_thres) {
                    String name = face.optString("name", null);
                    if (name != null) {
                        obj.put("name", name);
                    }
                }
                if (!obj.has("name")) {
                    if (face.optDouble("gender_conf", 0) > gender_thres) {
                        String gender = face.optString("gender");
                        if ("M".equals(gender)) {
                            obj.put("gender", "male");
                        } else if ("F".equals(gender)) {
                            obj.put("gender", "female");
                        }
                    }
                    if (face.optDouble("age_conf", 0) > age_thres) {
                        int age = face.optInt("age", -1);
                        if (age >= 0) {
                            obj.put("age", String.format("%d years old", age));
                        }
                    }
                }
                double distance = face.optDouble("distance", -1);
                if (distance > 1.5) {
                    obj.put("distance", String.format("%.0f meters away", distance));
                } else if (distance > 0) {
                    obj.put("distance", String.format("%.1f meters away", distance));
                }
                switch (face.optInt("position", -100)) {
                    case -1:
                        obj.put("position", "on the left");
                        break;
                    case 1:
                        obj.put("position", "on the right");
                        break;
                }
                if (face.optDouble("gaze_conf", 0) > gaze_thres) {
                    boolean gaze = face.optBoolean("gaze");
                    if (gaze) {
                        obj.put("gaze", "looking at you");
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return obj;
    }
}
