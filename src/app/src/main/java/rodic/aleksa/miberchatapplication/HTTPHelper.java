package rodic.aleksa.miberchatapplication;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class HTTPHelper {

    // POST
    public ServerRespond postJSONObject(String urlAddress, JSONObject jsonObject, String sessionId) throws IOException {
        HttpURLConnection httpURLConnection = null;
        java.net.URL url = new URL(urlAddress);
        httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setRequestMethod("POST");
        httpURLConnection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
        httpURLConnection.setRequestProperty("Accept", "application/json");
        if (sessionId != null) {
            // Session ID used for logout
            httpURLConnection.setRequestProperty("sessionid", sessionId);
        }
        httpURLConnection.setDoOutput(true);
        httpURLConnection.setDoInput(true);

        ServerRespond serverRespond;

        // Trying to connect on server
        try {
            httpURLConnection.connect();
        } catch (IOException e) {
            return null;
        }

        // Writing/sending data to server
        DataOutputStream dataOutputStream = new DataOutputStream(httpURLConnection.getOutputStream());

        dataOutputStream.writeBytes(jsonObject.toString());
        dataOutputStream.flush();
        dataOutputStream.close();

        // Handling server respond
        int serverReturnCode = httpURLConnection.getResponseCode();
        String serverReturnMessage = httpURLConnection.getResponseMessage();
        List<String> serverReturnSessionId = httpURLConnection.getHeaderFields().get("sessionid");

        if (serverReturnSessionId == null) {
            serverRespond = new ServerRespond(serverReturnCode, serverReturnMessage);
        } else {
            String serverReturnSessionIdString = serverReturnSessionId.toString().substring(1, serverReturnSessionId.toString().length() - 1);  // remove [] from session id
            serverRespond = new ServerRespond(serverReturnCode, serverReturnMessage, serverReturnSessionIdString);
        }

        // Closing connection
        httpURLConnection.disconnect();

        return serverRespond;
    }

    // GET
    public ServerRespond getJSON(String urlAddress, String sessionId) throws IOException, JSONException {
        HttpURLConnection httpURLConnection = null;
        java.net.URL url = new URL(urlAddress);
        httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setRequestMethod("GET");
        httpURLConnection.setRequestProperty("Accept", "application/json");
        httpURLConnection.setRequestProperty("sessionid", sessionId);
        httpURLConnection.setReadTimeout(10000 /* milliseconds */);
        httpURLConnection.setConnectTimeout(15000 /* milliseconds */);

        ServerRespond serverRespond;

        // Trying to connect on server
        try {
            httpURLConnection.connect();
        } catch (IOException e) {
            return null;
        }

        // Crafting string for sending
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
        StringBuilder stringBuilder = new StringBuilder();

        String line;

        while ((line = bufferedReader.readLine()) != null) {
            stringBuilder.append(line).append("\n");
        }

        bufferedReader.close();

        // Handling server respond
        int serverReturnCode = httpURLConnection.getResponseCode();
        String serverReturnMessage = httpURLConnection.getResponseMessage();
        String jsonString = stringBuilder.toString();
        serverRespond = new ServerRespond(serverReturnCode, serverReturnMessage, sessionId, jsonString);

        // Closing connection
        httpURLConnection.disconnect();

        return serverRespond;
    }

    // DELETE
    public ServerRespond deleteJSON(String urlAddress, Message message, String sessionId) throws IOException, JSONException {
        HttpURLConnection httpURLConnection = null;
        java.net.URL url = new URL(urlAddress);
        httpURLConnection = (HttpURLConnection) url.openConnection();

        httpURLConnection.setRequestMethod("DELETE");
        httpURLConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        httpURLConnection.setRequestProperty("Accept","application/json");
        httpURLConnection.setRequestProperty("sessionid", sessionId);
        httpURLConnection.setDoOutput(true);
        httpURLConnection.setDoInput(true);

        ServerRespond serverRespond;

        // Crafting JSON object
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("receiver", message.getReceiverId());
        jsonObject.put("sender", message.getSenderId());
        jsonObject.put("data", message.getMessage());

        // Trying to connect on server
        try {
            httpURLConnection.connect();
        } catch (IOException e) {
            return null;
        }

        // Writing/sending data to server
        DataOutputStream dataOutputStream = new DataOutputStream(httpURLConnection.getOutputStream());

        dataOutputStream.writeBytes(jsonObject.toString());
        dataOutputStream.flush();
        dataOutputStream.close();

        // Handling server respond
        int serverReturnCode = httpURLConnection.getResponseCode();
        String serverReturnMessage = httpURLConnection.getResponseMessage();

        serverRespond = new ServerRespond(serverReturnCode, serverReturnMessage, null);

        // Closing connection
        httpURLConnection.disconnect();

        return serverRespond;
    }

}
