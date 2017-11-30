package com.evelin.spenpal;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.evelin.spenpal.Network.ConnectorController;
import com.evelin.spenpal.Network.SSLConnector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

public class SharingActivity extends AppCompatActivity {

    private static final String SeverURL = "http://byebyebymyai.com/refresh.php";

    private List<Map<String, Object>> sharingList;
    private ListView sharingListView;
    private SharingListAdapter sharingListAdapter;

    private Map<String, Bitmap> avatarMap;

    private View mProgressView;
    private String username;
    private boolean endOfData = false;
    SQLiteDatabase mainDB;
    private int loopCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sharing);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        username = getIntent().getStringExtra("username");
        sharingListView = (ListView) findViewById(R.id.sharingListView);
//        sharingListView.setDividerHeight(0);

        mProgressView = findViewById(R.id.refresh_process);
        // here to set the scroll mode for ListView
        sharingList = new ArrayList<Map<String, Object>>();
        Map<String, Object> sharingItem = new HashMap<String, Object>();
        String category = "Testing";
        sharingItem.put("category", category);
        Float amount = 0.00f;
        sharingItem.put("amount", amount);
        String date = "2016/04/14";
        sharingItem.put("date", date);
        String comment = "Sample Sample";
        sharingItem.put("comment", comment);
        sharingItem.put("shared", false);
        avatarMap = new HashMap<>();
        sharingListAdapter = new SharingListAdapter(this, sharingList, avatarMap);
        sharingListView.setAdapter(sharingListAdapter);

        attemptRefresh();

        FloatingActionButton refreshButton = (FloatingActionButton) findViewById(R.id.refreshButton);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptRefresh();
                Snackbar.make(view, "Refreshed.", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    public void attemptRefresh() {
        if (!ConnectorController.getController().isConnected()) {
            return;
        }
        // Show a progress spinner, and kick off a background task to
        // perform the user login attempt.
//        showProgress(true);
        if (ConnectorController.getController().isConnected()) {
            ConnectorController.getController().sendData("120 " + username);//TODO fill with the data to be sent
            endOfData = false;
            mainDB = SQLiteDatabase.openOrCreateDatabase(getApplicationContext().getFilesDir().getAbsolutePath().replace("files", "databases") + "spending.db", null);
            mainDB.execSQL("DROP TABLE IF EXISTS sharings");
            mainDB.execSQL("CREATE TABLE IF NOT EXISTS sharings (id INTEGER, user VARCHAR, category VARCHAR, amount FLOAT, date VARCHAR, comment VARCHAR, image BLOB)");
            Handler handler = new Handler() {
                String received;

                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    if (ConnectorController.getController().isConnected()) {
                        if (msg.what == 1) {
                            received = (String) msg.obj;//
                            if (received.equalsIgnoreCase("") || received == null) {
                                return;
                            }
                            loopCount = Integer.parseInt(received);
                            for (int i = 0; i <= loopCount + 1; i++) {
                                Handler handler = new Handler() {
                                    String rec;

                                    public void handleMessage(Message msg) {
                                        super.handleMessage(msg);
                                        if (ConnectorController.getController().isConnected()) {
                                            if (msg.what == 1) {
                                                rec = (String) msg.obj;
                                                Log.i("=Handler=", rec);
                                                if (rec.equals("end")) {
                                                    mainDB.close();
                                                    endOfData = true;
                                                    visualizeData();
                                                }
                                                if (!endOfData) {

                                                    try {
                                                        mainDB.execSQL(rec);


                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                        Log.v("=rec=", "problem: " + e.toString());
                                                    }

                                                }
                                            }

                                        } else {
                                            //TODO: Tell user connection is lost
                                            Log.i("=conn=", "Connection Problem while syncing.");
                                        }
                                    }

                                };
                                Message message = SSLConnector.getConnector().getHandler().obtainMessage(4);
                                message.obj = handler;
                                SSLConnector.getConnector().getHandler().sendMessage(message);
                                handler.obtainMessage();

                            }

                        }
                    } else {
                        //TODO: Tell user connection is lost
                        Log.i("=conn=", "Connection Problem while syncing.");
                    }

                }

            };

            Message message = SSLConnector.getConnector().getHandler().obtainMessage(4);
            message.obj = handler;
            SSLConnector.getConnector().getHandler().sendMessage(message);
            handler.obtainMessage();

        }

    }

    private void visualizeData() {
        if (endOfData) {
            mainDB = SQLiteDatabase.openOrCreateDatabase(getApplicationContext().getFilesDir().getAbsolutePath().replace("files", "databases") + "spending.db", null);
            Cursor cursor = mainDB.rawQuery("SELECT * FROM sharings", null);
            Log.i("=db=", "DB1");
            sharingList = new ArrayList<Map<String, Object>>();
            while (cursor.moveToNext()) {
                Map<String, Object> sharingListItem = new HashMap<String, Object>();
                String category = cursor.getString(cursor.getColumnIndex("category"));
                sharingListItem.put("category", category);
                Float amount = cursor.getFloat(cursor.getColumnIndex("amount"));
                sharingListItem.put("amount", amount);
                String date = cursor.getString(cursor.getColumnIndex("date"));
                sharingListItem.put("date", date);
                String comment = cursor.getString(cursor.getColumnIndex("comment"));
                sharingListItem.put("comment", comment);
                String uname = cursor.getString(cursor.getColumnIndex("user"));
                sharingListItem.put("username", uname);
                ConnectorController.getController().storeAvatar(uname, avatarMap);
                sharingListItem.put("reverse", uname.equals(username) ? 1 : 0);
                sharingList.add(sharingListItem);
                Log.i("=list=", "Category->" + category + " --- Amount->" + amount);
            }

            cursor.close();
            mainDB.close();
            Log.i("=db=", "DB2");
            sharingListAdapter = new SharingListAdapter(getApplicationContext(), sharingList, avatarMap);
            sharingListView.setAdapter(sharingListAdapter);
            sharingListAdapter.notifyDataSetChanged();

            Log.i("=db=", "DB3");
        }
    }

}









