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
    public static final double METER = 1, FEET = 0.3048;
    public static final int MODE_A = 0, MODE_B = 1;
    public static final int FRIEND = 0, NO_FRIEND = 1;
//    private static final String url = "http://192.168.11.2:5000/test";
    private static final String url = "http://cal.ri.cmu.edu:5000/test";
//    private static final String uuid = UUID.randomUUID().toString();
    private static final String uuid = "pp00";
    private static final double name_thres = 0.5, gender_thres = 0.7, age_thres = 0.5, gaze_thres = 0.7;
//    private static final HttpHeaders imageHeaders = new HttpHeaders().set("Content-Disposition", "form-data; name=\"file\"; filename=\"picture.jpg\"");
    private static final String[] KEYS = {"name", "gender", "age", "distance", "position", "gaze"};
    public static double metersPerUnit = FEET;
    public static int mode = MODE_A;
    public static int f_mode = NO_FRIEND;
    private final Map<String, String> mParameters = new HashMap();
    private JSONObject lastSpeak = new JSONObject();

    public CMUFaceServer() {
        super(url);
        mParameters.put("uuid", uuid);
//        mParameters.put("phase", "dev");
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
        // set phase according to the mode (MODE_A/MODE_B)
        final String phase = (mode == MODE_A) ? "MODE_A" : "MODE_B";
        HttpHeaders headers = new HttpHeaders().set("Content-Disposition", "form-data; name=\"phase\"");
        content.addPart(new MultipartContent.Part(headers, ByteArrayContent.fromString(null, phase)));
        // include timestamp in an image name to log it on a server
        final HttpHeaders imageHeaders = new HttpHeaders().set("Content-Disposition", String.format("form-data; name=\"%s\"; filename=\"%d.jpg\"", "file", System.currentTimeMillis()));
        content.addPart(new MultipartContent.Part(imageHeaders, imageContent));
    }

    @Override
    protected void addExtra(HttpRequest request) {
        request.setWriteTimeout(10 * 1000);
//        request.getHeaders().set("Connection", "close");
    }

    @Override
    public int getRetryCount() {
        return 1000;
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
    public void reset() {
        lastSpeak = new JSONObject();
    }

    @Override
    public String getSpeakText() {
        lastSpeak.remove("age");
        lastSpeak.remove("gender");
        lastSpeak.remove("name");
        lastSpeak.remove("distance");
        lastSpeak.remove("position");
        lastSpeak.remove("gaze");
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
                if (f_mode == FRIEND) {
                    String name = face.optString("name", null);
                    if (name != null) {
                        obj.put("name", name);
                    }
                }
                if (f_mode == NO_FRIEND && !obj.has("name")) {
                    String gender = null;
                    if (face.optDouble("gender_conf", 0) > gender_thres) {
                        gender = face.optString("gender");
                        if ("M".equals(gender)) {
                            obj.put("gender", "male");
                        } else if ("F".equals(gender)) {
                            obj.put("gender", "female");
                        }
                    }
                    if (face.optDouble("age_conf", 0) > age_thres) {
                        int age = face.optInt("age", -1);
                        if (age >= 0) {
//                            obj.put("age", String.format("%d years old", age));
                            if (mode == MODE_B) {
                                if (age < 35) {
                                    obj.put("age", "young");
                                } else if (age < 55) {
                                    obj.put("age", "middle-age");
                                } else {
                                    obj.put("age", "old");
                                }
                            } else {
                                switch (age / 10) {
//                                    case 0:
//                                        if ("M".equals(gender)) {
//                                            obj.put("age", "boy");
//                                        } else if ("F".equals(gender)) {
//                                            obj.put("age", "girl");
//                                        } else {
//                                            obj.put("age", "child");
//                                        }
//                                        break;
//                                    case 1:
//                                        obj.put("age", "teenager");
//                                        break;
                                    case 2:
                                        obj.put("age", "twenties");
                                        break;
                                    case 3:
                                        obj.put("age", "thirties");
                                        break;
                                    case 4:
                                        obj.put("age", "forties");
                                        break;
                                    case 5:
                                        obj.put("age", "fifties");
                                        break;
                                    case 6:
                                        obj.put("age", "sixties");
                                        break;
                                    case 7:
                                        obj.put("age", "seventies");
                                        break;
                                    case 8:
                                        obj.put("age", "eighties");
                                        break;
                                    case 9:
                                        obj.put("age", "nineties");
                                        break;
                                    default:
                                        obj.put("age", "twenties");
                                        break;
                                }
                            }
                        }
                    }
                }
                double distance = face.optDouble("distance", -1) / metersPerUnit;
                if (distance > 0) {
                    // adjustment due to the latency between the server and client
                    distance = (distance > 1) ? distance - 1 : 0;
                    if (mode == MODE_B) {
                        if (distance > 5) {
                            obj.put("distance", "far");
                        } else {
                            obj.put("distance", "near");
                        }
                    } else {
                        if (metersPerUnit == METER) {
                            obj.put("distance", String.format(distance > 1.5 ? "%.0f meters" : "%.1f meters", distance));
                        } else {
                            obj.put("distance", String.format(distance > 5.5 ? "%.0f feet" : "%.1f feet", distance));
                        }
                    }
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
                    } else{
                        obj.put("gaze", "not looking at you");
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return obj;
    }
}
