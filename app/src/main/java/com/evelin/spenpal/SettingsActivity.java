package com.evelin.spenpal;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.evelin.spenpal.Network.ConnectorController;
import com.evelin.spenpal.Network.SSLConnector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

public class SettingsActivity extends AppCompatActivity {

    private List<Map<String, Object>> settingsList, optionsList;
    private ListView settingsListView;
    private ListView profileListView;
    private SettingsListAdapter settingsAdapter, optionsAdapter;
    private String username;
    private static String theUsername;
    Bitmap avatar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        username = getIntent().getStringExtra("username");
        if (username != null) {
            theUsername = username;

            Handler handler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    if (msg.what == 1) {
                        avatar = (Bitmap) msg.obj;
                        settingsList = new ArrayList<Map<String, Object>>();
                        Map<String, Object> settingItem = new HashMap<String, Object>();
                        settingItem.put("label", theUsername);
                        settingItem.put("icon", avatar);              //   Edit the ICON here !!
                        settingsList.add(settingItem);
                        settingsAdapter = new SettingsListAdapter(getApplicationContext(), settingsList);

                        profileListView = (ListView) findViewById(R.id.profileListView);
                        profileListView.setAdapter(settingsAdapter);
                        profileListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                Intent toSharingIntent = new Intent();
                                toSharingIntent.setClass(SettingsActivity.this, SharingActivity.class);
                                toSharingIntent.putExtra("username", theUsername);
                                startActivity(toSharingIntent);
                            }
                        });

                        optionsList = new ArrayList<Map<String, Object>>();
                        Map<String, Object> optionItem = new HashMap<String, Object>();
                        optionItem.put("label", "Go AA Split!");
                        optionItem.put("icon", BitmapFactory.decodeResource(getResources(), R.drawable.aa_calc));
                        optionsList.add(optionItem);
                        Map<String, Object> optionItem2 = new HashMap<String, Object>();
                        optionItem2.put("label", "Friends");
                        optionItem2.put("icon", BitmapFactory.decodeResource(getResources(), R.drawable.contact));
                        optionsList.add(optionItem2);
                        optionsAdapter = new SettingsListAdapter(getApplicationContext(), optionsList);
                        settingsListView = (ListView) findViewById(R.id.settingListView);
                        settingsListView.setAdapter(optionsAdapter);
                        settingsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                if (position == 0) {
                                    Intent toAAIntent = new Intent();
                                    toAAIntent.setClass(SettingsActivity.this, AASplitActivity.class);
                                    toAAIntent.putExtra("username", theUsername);
                                    startActivity(toAAIntent);
                                } else {
                                    Intent toFriendsIntent = new Intent();
                                    toFriendsIntent.setClass(SettingsActivity.this, FriendsActivity.class);
                                    toFriendsIntent.putExtra("username", theUsername);
                                    ConnectorController.getController().sendData("201 " + theUsername);
                                    startActivity(toFriendsIntent);
                                }

                            }
                        });
                    }
                }
            };
            Object[] pack = new Object[2];
            Message message = SSLConnector.getConnector().getHandler().obtainMessage(5);
            pack[0] = handler;
            pack[1] = theUsername;
            message.obj = pack;
            SSLConnector.getConnector().getHandler().sendMessage(message);
            handler.obtainMessage();
        } else {
            settingsList = new ArrayList<Map<String, Object>>();
            Map<String, Object> settingItem = new HashMap<String, Object>();
            settingItem.put("label", "Login to see more features!");
            settingItem.put("icon", null);              //   Edit the ICON here !!
            settingsList.add(settingItem);
            settingsAdapter = new SettingsListAdapter(this, settingsList);

            profileListView = (ListView) findViewById(R.id.profileListView);
            profileListView.setAdapter(settingsAdapter);
            profileListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent toSharingIntent = new Intent();
                    toSharingIntent.setClass(SettingsActivity.this, SharingActivity.class);
                    toSharingIntent.putExtra("username", theUsername);
                    startActivity(toSharingIntent);
                }
            });

        }


    }

}
