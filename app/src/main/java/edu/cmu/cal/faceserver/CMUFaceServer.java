package edu.cmu.cal.faceserver;

import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.MultipartContent;

import org.json.JSONObject;

public class CMUFaceServer extends AbstractFaceServer {
    private static final String uuid = "1234";
    private static final String phase = "dev";
    private static final String model = "insightface";
    private static final String mode = "recognition";
    private static final String url = "http://faceserver.cal.cmu.edu:5000/test?uuid=" + uuid + "&phase=" + phase + "&model=" + model + "&mode=" + mode;
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
//            for (Iterator<String> it = face.keys(); it.hasNext(); ) {
//                String key = it.next();
//                sb.append(String.format("%s %s. ", key, face.opt(key)));
//            }
            Object age = face.opt("age");
            Object gender = face.opt("gender");
            Object distance = face.opt("distance");
            Object name = face.opt("name");
            Object conf = face.opt("conf");
            if (name != null) {
                sb.append(name + ", ");
            }
            if ("M".equals(gender)) {
                sb.append("male, ");
            } else if ("F".equals(gender)) {
                sb.append("female, ");
            }
            if (age != null) {
                sb.append(age + " years old, ");
            }
            if (distance != null) {
                sb.append(distance + " meters away, ");
            }
            if (conf != null) {
                sb.append("confidence " + conf);
            }
        }
        return sb.toString();
    }
}
