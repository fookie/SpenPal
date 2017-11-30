package com.evelin.spenpal;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.evelin.spenpal.Network.ConnectorController;
import com.evelin.spenpal.Network.SSLConnector;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class AASplitActivity extends AppCompatActivity {

    private String username;
    private EditText moneyEditText;
    private TextView resultTextView;
    private TextView usernameTextView;
    private TextView balanceTextView;
    private ListView friendsListView;
    private AAListAdapter aaListAdapter;
    private List<Map<String, Object>> friendsList;
    private FloatingActionButton addNewPayment;
    private Button quitGroup;
    private int loopCount = 0;
    private float avgSpend = 0;
    private int positionOfCreditor = 0;
    private float amountShouldPay = 0;
    private ImageView avatar;
    Map<String, Bitmap> avatarMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aasplit);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        friendsList = new ArrayList<Map<String, Object>>();
        username = getIntent().getStringExtra("username");

        moneyEditText = (EditText) findViewById(R.id.moneyEditText);
        moneyEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId,
                                          KeyEvent event) {
                return true;
            }
        });

        addNewPayment = (FloatingActionButton) findViewById(R.id.aasplit_add);
        resultTextView = (TextView) findViewById(R.id.friendPaidTextView);
        friendsListView = (ListView) findViewById(R.id.aaListView);
        aaListAdapter = new AAListAdapter(this, friendsList, avatarMap);
        usernameTextView = (TextView) findViewById(R.id.aasplit_username);
        balanceTextView = (TextView) findViewById(R.id.aasplit_balance);
        friendsListView.setAdapter(aaListAdapter);
        avatar = (ImageView) findViewById(R.id.headView);
        quitGroup = (Button) findViewById(R.id.quitgroup);

        ConnectorController.getController().setAvatar(username, avatar);


        ConnectorController.getController().sendData("525 " + username + " " + "test");
        attemptRefresh();

        moneyEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() != 0) {
                    addNewPayment.setImageResource(R.drawable.add);
                    addNewPayment.refreshDrawableState();
                } else {
                    addNewPayment.setImageResource(R.drawable.refresh);
                    addNewPayment.refreshDrawableState();
                }
            }
        });

        addNewPayment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewPayment.setEnabled(false);
                if (moneyEditText.length() == 0) {
                    Log.v("AAButton!", "Refresh");
                    ConnectorController.getController().sendData("525 " + username + " " + "test");
                    attemptRefresh();
                    aaListAdapter.notifyDataSetChanged();
                } else {
                    Log.v("AAButton!", "Refresh and add");
                    ConnectorController.getController().sendData("524 " + username + " " + moneyEditText.getText().toString() + " test");
                    attemptRefresh();
                    moneyEditText.setText("");
                }
            }
        });

        quitGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConnectorController.getController().sendData("526 " + username + " test");
                finish();
            }
        });
    }

    public void attemptRefresh() {
        Handler handler = new Handler() {
            String received;

            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (ConnectorController.getController().isConnected()) {
                    if (msg.what == 1) {
                        received = (String) msg.obj;//
                        Log.v("AARefresh", received);
                        if (received.equalsIgnoreCase("") || received == null) {
                            return;
                        }
                        String[] temp = received.split(" ");
                        loopCount = Integer.parseInt(temp[0]);
                        avgSpend = Float.parseFloat(temp[1]);
                        positionOfCreditor = Integer.parseInt(temp[2]);
                        Log.v("Creditor", String.valueOf(positionOfCreditor));
                        amountShouldPay = Float.parseFloat(temp[3]);
                        for (int i = 0; i <= loopCount; i++) {
                            Handler handler = new Handler() {
                                String rec;

                                public void handleMessage(Message msg) {
                                    super.handleMessage(msg);
                                    if (ConnectorController.getController().isConnected()) {
                                        if (msg.what == 1) {
                                            rec = (String) msg.obj;
                                            Log.i("=Handler=", rec);

                                            String listData[] = rec.split(" ");
                                            Map<String, Object> item = new HashMap<String, Object>();
                                            item.put("name", listData[1]);
                                            ConnectorController.getController().storeAvatar(listData[1], avatarMap);
                                            item.put("result", "0");
                                            DecimalFormat formatter = new DecimalFormat("0.00");
                                            if (Integer.parseInt(listData[0]) == positionOfCreditor + 1) {
                                                usernameTextView.setText(listData[1]);
                                                float balance = Float.parseFloat(listData[2]);
                                                if (balance - avgSpend < 0) {
                                                    balanceTextView.setText("Your balance: " + formatter.format(balance) + "$ +" + formatter.format(avgSpend - balance) + "$");
                                                } else if (balance - avgSpend > 0) {
                                                    balanceTextView.setText("Your balance: " + formatter.format(balance) + "$ " + formatter.format(avgSpend - balance) + "$");
                                                } else if (balance - avgSpend == 0) {
                                                    balanceTextView.setText("Your balance: " + formatter.format(balance) + "$ ");
                                                }
                                            }
                                            if (Integer.parseInt(listData[0]) == positionOfCreditor) {
                                                item.put("result", String.valueOf(formatter.format(amountShouldPay)));
                                            }
                                            item.put("paid", Float.parseFloat(listData[2]));
                                            int index = Integer.parseInt(listData[0]);
                                            if (index >= friendsList.size()) {
                                                friendsList.add(index, item);
                                            } else {
                                                friendsList.set(index, item);
                                            }
                                            aaListAdapter.notifyDataSetChanged();

                                        }

                                    } else {
                                        Toast.makeText(AASplitActivity.this, "We lost the connection to server", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(AASplitActivity.this, "We lost the connection to server", Toast.LENGTH_SHORT).show();
                    Log.v("=conn=", "Connection Problem while syncing.");
                }

                addNewPayment.setEnabled(true);
            }

        };

        Handler h = SSLConnector.getConnector().getHandler();

        Message message = h.obtainMessage(4);
        message.obj = handler;
        h.sendMessage(message);
        handler.obtainMessage();

    }

}
