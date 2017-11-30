package com.evelin.spenpal;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;
import org.json.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
/**
 * Created by LIn on 2016/4/19.
 */
public class ShareTask extends AsyncTask<String, Void, String>{

    private String record;
    private String username = "";
    private String category = "";
    private float amount = 0f;
    private String date = "";
    private String comment = "";

    public ShareTask(String un, String ca, float am, String dt, String cm){
        username = un;
        category = ca;
        amount = am;
        date = dt;
        comment = cm;
    }

    @Override
    protected String doInBackground(String... urls) {
        try {
            return getStringDataFrom(urls[0]);
        } catch (IOException e) {
            return "Unable to connect.(by Net Thread)";
        }
    }

    private String getStringDataFrom(String myurl) throws IOException {
        InputStream inputStream = null;
        int length = 500;


        try {
            URL url = new URL(myurl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
//            conn.setRequestMethod("GET");
            conn.setRequestMethod("POST");
//            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.connect();

            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");
//            outputStreamWriter.write("id=1");
            JSONObject requestJson = new JSONObject();
            try {
                requestJson.put("username", username);
                requestJson.put("category", category);
                requestJson.put("amount", amount);
                requestJson.put("date", date);
                requestJson.put("comment", comment);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            String requestStr = requestJson.toString();
            record = requestStr;
            outputStreamWriter.write("share="+requestStr);
            outputStreamWriter.flush();
            outputStreamWriter.close();

            Log.i("=http=", "ID: " + username + "KIND: " + category + "Amount:" + amount);

            int response = conn.getResponseCode();
            Log.d("=Response=", "The response is: " + response);
            inputStream = conn.getInputStream();

            // Convert the InputStream into a string
            String contentAsString = convertInputStreamToString(inputStream, length);
            return contentAsString;
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }

    public String convertInputStreamToString(InputStream stream, int length) throws IOException {
        Reader reader = null;
        reader = new InputStreamReader(stream, "UTF-8");
        char[] buffer = new char[length];
        reader.read(buffer);
        return new String(buffer);
    }

    @Override
    protected void onPostExecute(String result) {   // setting the UI
        JSONObject confirm = null;
        //RESULT HERE

    }
}
