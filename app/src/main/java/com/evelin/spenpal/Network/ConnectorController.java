package com.evelin.spenpal.Network;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by Paulay on 2016/5/11 0011.
 */

/**
 * Used to control SSLConnector to establish a connection to the server
 */
public class ConnectorController {
    static Handler mainHandler;

    private ConnectorController() {
    }

    private static class Holder {
        static final ConnectorController INSTANCE = new ConnectorController();
    }

    public static ConnectorController getController() {
        return Holder.INSTANCE;
    }

    /**
     * Initialize the connector
     *
     * @param ip   Server IP
     * @param port Server Port
     * @return if success
     */
    public static boolean init(String ip, int port) {
        if (!SSLConnector.getConnector().isAlive()) {
            SSLConnector.getConnector().start();
        }
        SSLConnector.getConnector().setAddress(ip, port);
        mainHandler = new Handler(Looper.getMainLooper());
        return SSLConnector.getConnector().isAlive();
    }

    /**
     * Connect to the server. The controller must be initiated first.
     */
    public void connect() {
        Message message = SSLConnector.getConnector().getHandler().obtainMessage(1);
        SSLConnector.getConnector().getHandler().sendMessage(message);
    }

    /**
     * Disconnect from the server.
     */

    public void disconnect() {
        Message message = SSLConnector.getConnector().getHandler().obtainMessage(2);
        SSLConnector.getConnector().getHandler().sendMessage(message);
    }

    /**
     * Check connectivity
     *
     * @return if is connected
     */
    public boolean isConnected() {
        return SSLConnector.getConnector().isConnected();
    }

    /**
     * Register
     *
     * @param name   User name
     * @param passwd Password
     */
    public void register(String name, String passwd) {
        if (isConnected()) {
            String request = "100 " + name + " " + passwd;
            sendData(request);
        }
    }

    public boolean login(String name, String passwd) {
        if (isConnected()) {
            sendData("101 " + name + " " + passwd);
            Log.v("Connect", "try to login...");
            Receiver receiver = new Receiver();
            Message message = SSLConnector.getConnector().getHandler().obtainMessage(4);
            message.obj = receiver;
            SSLConnector.getConnector().getHandler().sendMessage(message);
            receiver.obtainMessage();


        }
        return true;
    }

    /**
     * Send a string to server.
     *
     * @param s String to be sent
     */

    public void sendData(String s) {
        Message message = SSLConnector.getConnector().getHandler().obtainMessage(3);
        Log.v("=DataSent=", s);
        message.obj = s;
        SSLConnector.getConnector().getHandler().sendMessage(message);
    }

    public void setAvatar(String username, final ImageView slot) {
        Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == 1) {
                    slot.setImageBitmap((Bitmap) msg.obj);
                }

            }
        };
        Object[] pack = new Object[2];
        Message message = SSLConnector.getConnector().getHandler().obtainMessage(5);
        pack[0] = handler;
        pack[1] = username;
        message.obj = pack;
        SSLConnector.getConnector().getHandler().sendMessage(message);
        handler.obtainMessage();
    }

    public void storeAvatar(final String username, final Map<String, Bitmap> map) {
        Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == 1) {
                    map.put(username, (Bitmap) msg.obj);
                }

            }
        };
        Object[] pack = new Object[2];
        Message message = SSLConnector.getConnector().getHandler().obtainMessage(5);
        pack[0] = handler;
        pack[1] = username;
        message.obj = pack;
        SSLConnector.getConnector().getHandler().sendMessage(message);
        handler.obtainMessage();
    }

    public class Receiver extends Handler {
        String received;

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                received = (String) msg.obj;
                Log.v("+Handler+", received);
            }
        }

        public String extractMessage() {
            return received;
        }
    }
}
