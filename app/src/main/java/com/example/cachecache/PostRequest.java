package com.example.cachecache;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Callable;

class PostRequest implements Callable<JSONObject> {
    private final String urlString;
    private final JSONObject request;
    private JSONObject response;

    PostRequest(String url, JSONObject request) {
        this.urlString = url;
        this.request = request;
        response = null;
    }

    @Override
    public JSONObject call() {
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            conn.setRequestProperty("Accept","application/json");
            conn.setDoOutput(true);
            conn.setDoInput(true);

            Log.i("JSON", request.toString());

            DataOutputStream os = new DataOutputStream(conn.getOutputStream());
            os.writeBytes(request.toString());

            os.flush();
            os.close();
            readResponse(conn);
            conn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    private void readResponse(HttpURLConnection conn) throws IOException, JSONException {
        StringBuilder sb = new StringBuilder();
        String output;
        BufferedReader br;
        Log.i("STATUS", String.valueOf(conn.getResponseCode()));
        Log.i("MSG" , conn.getResponseMessage());

        if (200 <= conn.getResponseCode() && conn.getResponseCode() <= 299) {
            br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        } else {
            br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
        }

        while ((output = br.readLine()) != null) {
            sb.append(output);
        }
        response = new JSONObject(sb.toString());
    }
}
