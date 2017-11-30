package com.evelin.spenpal;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.evelin.spenpal.Network.ConnectorController;
import com.evelin.spenpal.Network.SSLConnector;
import com.evelin.spenpal.pulltorefresh.interfaces.IXListViewListener;
import com.evelin.spenpal.swipemenu.bean.SwipeMenu;
import com.evelin.spenpal.swipemenu.bean.SwipeMenuItem;
import com.evelin.spenpal.swipemenu.interfaces.OnMenuItemClickListener;
import com.evelin.spenpal.swipemenu.interfaces.OnSwipeListener;
import com.evelin.spenpal.swipemenu.interfaces.SwipeMenuCreator;
import com.evelin.spenpal.util.RefreshTime;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class FriendsActivity extends AppCompatActivity implements IXListViewListener {

    private List<String> mAppList;
    private FriendsAdapter mAdapter;
    private PullToRefreshSwipeMenuListView mListView;
    private int loopCount;
    private Map<String, Bitmap> avatarMap;
    private LinkedList<Map<String, Object>> friendsList;
    private String username;
    private EditText searchText;
    private ImageButton searchButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        if (!ConnectorController.getController().isConnected()) {
            ConnectorController.getController().connect();
        }
        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        username = getIntent().getStringExtra("username");

        mAppList = new LinkedList<>();
        avatarMap = new HashMap<>();
        mListView = (PullToRefreshSwipeMenuListView) findViewById(R.id.friendsListView);
        mAdapter = new FriendsAdapter(getApplicationContext(), mAppList, avatarMap);
        mListView.setAdapter(mAdapter);
        mListView.setPullLoadEnable(false);
        mListView.setPullRefreshEnable(false);
        mListView.setXListViewListener(this);
        searchText = (EditText) findViewById(R.id.searchEditText);
        attemptRefresh();

        searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() != 0) {
                    fab.setImageResource(R.drawable.add);
                    fab.refreshDrawableState();
                } else {
                    fab.setImageResource(R.drawable.refresh);
                    fab.refreshDrawableState();
                }
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!ConnectorController.getController().isConnected()) {
                    ConnectorController.getController().connect();
                }
                if (!searchText.getText().toString().equalsIgnoreCase("")) {
                    ConnectorController.getController().sendData("200 " + username + " " + searchText.getText().toString());

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
                                    String[] temp = received.split(" ");
                                    if (temp[0].equals("success")) {
                                        Toast.makeText(FriendsActivity.this, "Friend added", Toast.LENGTH_SHORT).show();
                                        searchText.setText("");
                                    } else if (temp[0].equals("failed")) {
                                        Toast.makeText(FriendsActivity.this, "User not found", Toast.LENGTH_SHORT).show();
                                        searchText.selectAll();
                                    } else {
                                        Toast.makeText(FriendsActivity.this, "Error", Toast.LENGTH_SHORT).show();
                                    }

                                }
                            } else {
                                //TODO: Tell user connection is lost
                                Log.v("=conn=", "Connection Problem while syncing.");
                            }

                        }

                    };

                    Handler h = SSLConnector.getConnector().getHandler();
                    Message message = h.obtainMessage(4);
                    message.obj = handler;
                    h.sendMessage(message);
                    handler.obtainMessage();

                } else {
                    ConnectorController.getController().sendData("201 " + username);
                    attemptRefresh();
                }
            }

        });

        // step 1. create a MenuCreator
        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {
                // create "open" item
                SwipeMenuItem openItem = new SwipeMenuItem(getApplicationContext());
                // set item background
                openItem.setBackground(new ColorDrawable(Color.rgb(0xC9, 0xC9, 0xCE)));
                // set item width
                openItem.setWidth(dp2px(90));
                // set item title
                openItem.setTitle("Open");
                // set item title fontsize
                openItem.setTitleSize(18);
                // set item title font color
                openItem.setTitleColor(Color.WHITE);
                // add to menu
                menu.addMenuItem(openItem);

                // create "delete" item
                SwipeMenuItem deleteItem = new SwipeMenuItem(getApplicationContext());
                // set item background
                deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9, 0x3F, 0x25)));
                // set item width
                deleteItem.setWidth(dp2px(90));
                // set a icon
                deleteItem.setIcon(R.drawable.ic_delete);
                // add to menu
                menu.addMenuItem(deleteItem);
            }
        };
        // set creator
        mListView.setMenuCreator(creator);
        // step 2. listener item click event
        mListView.setOnMenuItemClickListener(new OnMenuItemClickListener() {
            @Override
            public void onMenuItemClick(int position, SwipeMenu menu, int index) {
                String item = mAppList.get(position);
                switch (index) {
                    case 0:
                        // open
//                        open(item);
                        Log.v("ShowName", item);
                        break;
                    case 1:
                        // delete
                        // delete(item);

                        ConnectorController.getController().sendData("202 " + username + " " + item);
                        Toast.makeText(FriendsActivity.this, "Friend removed", Toast.LENGTH_SHORT).show();
                        mAppList.remove(position);
                        mAdapter.notifyDataSetChanged();
                        break;
                }
            }
        });

        // set SwipeListener
        mListView.setOnSwipeListener(new OnSwipeListener() {

            @Override
            public void onSwipeStart(int position) {
                // swipe start
            }

            @Override
            public void onSwipeEnd(int position) {
                // swipe end
            }
        });

        // test item long click
        mListView.setOnItemLongClickListener(new OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getApplicationContext(), position + " long click", Toast.LENGTH_SHORT).show();
                return false;
            }
        });

    }


    public void attemptRefresh() {
        mAppList.clear();
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
                        String[] temp = received.split(" ");
                        loopCount = Integer.parseInt(temp[0]);
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
                                            if (!listData[0].equals("end")) {
                                                mAppList.add(listData[0]);
                                                ConnectorController.getController().storeAvatar(listData[0], avatarMap);
                                                mAdapter.notifyDataSetChanged();
                                            }
                                        }

                                    } else {
                                        Toast.makeText(FriendsActivity.this, "We lost the connection to server", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(FriendsActivity.this, "We lost the connection to server", Toast.LENGTH_SHORT).show();
                    Log.v("=conn=", "Connection Problem while syncing.");
                }

            }

        };

        Handler h = SSLConnector.getConnector().getHandler();
        Message message = h.obtainMessage(4);
        message.obj = handler;
        h.sendMessage(message);
        handler.obtainMessage();

    }

    private void onLoad() {
        mListView.setRefreshTime(RefreshTime.getRefreshTime(getApplicationContext()));
        mListView.stopRefresh();

        mListView.stopLoadMore();

    }

    public void onRefresh() {
    }

    public void onLoadMore() {
    }

    private void open(String item) {
//        }
    }

    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }


}
