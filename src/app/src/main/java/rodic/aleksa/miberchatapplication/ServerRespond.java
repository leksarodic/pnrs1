package rodic.aleksa.miberchatapplication;

import android.util.SparseArray;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ServerRespond {
    int code;
    private String message;
    private String sessionId;
    private String jsonString;

    ServerRespond(int code, String message) {
        this.code = code;
        this.message = message;
    }

    ServerRespond(int code, String message, String sessionId) {
        this.code = code;
        this.message = message;
        this.sessionId = sessionId;
    }

    ServerRespond(int code, String message, String sessionId, String jsonString) {
        this.code = code;
        this.message = message;
        this.sessionId = sessionId;
        this.jsonString = jsonString;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getJsonString() {
        return jsonString;
    }

    public JSONObject getJsonObject() throws JSONException {
        return new JSONObject(this.jsonString);
    }

    public JSONArray getJsonArray() throws JSONException {
        return new JSONArray(this.jsonString);
    }
}
