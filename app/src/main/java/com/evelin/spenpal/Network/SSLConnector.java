package com.evelin.spenpal.Network;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.net.SocketFactory;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

/**
 * Created by Paulay on 2016/5/10 0010.
 */
public class SSLConnector extends Thread {

    public Looper connectorThread;
    public Handler handler;
    SSLSocket socket;
    String address;
    int port;
    DataInputStream in;
    DataOutputStream out;
    SocketFactory factory;

    private static class ConnectorHolder {
        static final SSLConnector INSTANCE = new SSLConnector();
    }

    private SSLConnector() {
    }

    public static SSLConnector getConnector() {
        return ConnectorHolder.INSTANCE;
    }

    public void setAddress(String addr, int p) {
        address = addr;
        port = p;
    }

    public void initSocket() {
        try {

            KeyStore ks = KeyStore.getInstance("BKS");
            ks.load(new FileInputStream(Environment.getExternalStorageDirectory().getAbsolutePath() + "/SpenPal/certificate/android.bks"), "storespanpal".toCharArray());

            TrustManagerFactory tmf = TrustManagerFactory.getInstance("X509");
            tmf.init(ks);
            TrustManager[] tm = tmf.getTrustManagers();

            SSLContext context = SSLContext.getInstance("TLSV1");
            context.init(null, tm, null);

            factory = context.getSocketFactory();
            socket = (SSLSocket) factory.createSocket(address, port);

            Log.v("SSLConnector", socket != null ? "yes" : "no");


        } catch (IOException | KeyManagementException | KeyStoreException | NoSuchAlgorithmException | CertificateException ignored) {

        }

    }

    public SSLSocket getSocket() {
        return socket;
    }


    public boolean isConnected() {
        return socket != null && socket.isConnected();
    }

    public void disconnect() {
        try {

            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendData(String s) {
        if (socket.isConnected()) {
            try {
                out = new DataOutputStream(socket.getOutputStream());
                out.writeUTF(s);
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String receiveData() {
        if (socket.isConnected()) {
            try {
                in = new DataInputStream(socket.getInputStream());
                String receivedData = in.readUTF();
                Log.v("=ReceivedData=", receivedData);
                return receivedData;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public Bitmap receiveAvatar(String username) {
        URL url;
        Bitmap bmp;
        try {
            url = new URL("http://" + SSLConnector.getConnector().getSocket().getInetAddress().getHostAddress() + "/" + username + ".png");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD");
            if (connection.getResponseCode() == 404) {
                url = new URL("http://" + SSLConnector.getConnector().getSocket().getInetAddress().getHostAddress() + "/nogravatar2.png");
            }
            bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());

            return bmp;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Looper getLooper() {
        return Looper.myLooper();
    }

    public Handler getHandler() {

        return handler;
    }

    @Override
    public void run() {
        this.setName("SSLSocket");
        Looper.prepare();
        connectorThread = Looper.myLooper();
        Log.v("SSLConnectorRunnning", "Running");
        prepareHandler();
        Log.v("SSLConnectorRunnning", "Handler initiated");
        handler.obtainMessage();
        Looper.loop();
    }

    public void prepareHandler() {
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        if (socket == null) {
                            initSocket();
                        }
                        if (socket != null && !socket.isConnected()) {
                            try {
                                socket = (SSLSocket) factory.createSocket(address, port);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }else{
                            initSocket();
                        }
                        break;
                    case 2:
                        if (socket != null && socket.isConnected()) {
                            disconnect();
                        }else{
                            initSocket();
                        }
                        break;
                    case 3:
                        if (socket != null && socket.isConnected()) {
                            sendData((String) msg.obj);
                        }else{
                            initSocket();
                        }
                        break;
                    case 4:
                        if (socket != null && socket.isConnected()) {
                            Handler mainHandler = (Handler) msg.obj;
                            Message data = mainHandler.obtainMessage(1);
                            data.obj = receiveData();
                            mainHandler.sendMessage(data);
                        }else{
                            initSocket();
                        }
                        break;
                    case 5:
                        if (socket != null && socket.isConnected()) {
                            Object[] objs = (Object[]) msg.obj;
                            Handler mainHandler = (Handler) objs[0];
                            Message data = mainHandler.obtainMessage(1);
                            data.obj = receiveAvatar((String) objs[1]);
                            mainHandler.sendMessage(data);
                        }else{
                            initSocket();
                        }
                        break;
                    default:
                        break;
                }
            }
        };
    }
}
