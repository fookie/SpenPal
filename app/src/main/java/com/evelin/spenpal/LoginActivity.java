package com.evelin.spenpal;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.evelin.spenpal.Network.ConnectorController;
import com.evelin.spenpal.Network.SSLConnector;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import cn.jpush.android.api.JPushInterface;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;
    private static final String SeverURL = "http://byebyebymyai.com/login.php";
    // UI references.
    private AutoCompleteTextView usernameView;
    private EditText passwordView;
    private View mProgressView;

    private String username = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

//        ConnectorController.init("172.21.70.62", 12313);
//        ConnectorController.init("128.199.130.45", 12313);
        ConnectorController.init("192.168.23.1", 12313);

        //generate certificate
        File cert = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/SpenPal/certificate/android.bks");
        if (!cert.exists()) {
            try {
                (new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/SpenPal/certificate")).mkdirs();
                cert.createNewFile();
                InputStream cerImport = getResources().openRawResource(R.raw.android);
                FileOutputStream cerExport = new FileOutputStream(cert);
                byte[] bytes = new byte[945];
                cerImport.read(bytes);
                cerExport.write(bytes);
                cerExport.flush();
                cerExport.close();
                cerImport.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        super.onCreate(savedInstanceState);

        JPushInterface.setDebugMode(true); // push notification
        JPushInterface.init(this);

        setContentView(R.layout.activity_login);
        // Set up the login form.
        usernameView = (AutoCompleteTextView) findViewById(R.id.usernameBox);
        populateAutoComplete();

        passwordView = (EditText) findViewById(R.id.passwordBox);
        passwordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(usernameView.getWindowToken(),0);
        imm.hideSoftInputFromWindow(passwordView.getWindowToken(),0);

//        usernameView.setText("laowang");
//        passwordView.setText("000000");

        ConnectorController.getController().connect();

        Button signInButton = (Button) findViewById(R.id.signInButton);
        signInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        Button exitButton = (Button) findViewById(R.id.exitButton);
        exitButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent loginIntent = new Intent();
                loginIntent.setClass(LoginActivity.this, MainActivity.class);
                loginIntent.putExtra("online", false);
                startActivity(loginIntent);
            }
        });

        mProgressView = findViewById(R.id.login_progress);
    }

    private void populateAutoComplete() {
        if (!mayRequestContacts()) {
            return;
        }

        getLoaderManager().initLoader(0, null, this);
    }

    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(usernameView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
                        }
                    });
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        return false;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete();
            }
        }
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        usernameView.setError(null);
        passwordView.setError(null);

        // Store values at the time of the login attempt.
        final String username = usernameView.getText().toString();
        String password = passwordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            passwordView.setError(getString(R.string.error_invalid_password));
            focusView = passwordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(username)) {
            usernameView.setError(getString(R.string.error_field_required));
            focusView = usernameView;
            cancel = true;
        } else if (!isUsernameValid(username)) {
            usernameView.setError("This username is not valid.");
            focusView = usernameView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);

            if (ConnectorController.getController().isConnected()) {
                ConnectorController.getController().sendData("101 " + username + " " + password);
                Log.v("Connect", "try to login...");
                Handler handler = new Handler() {
                    String received;

                    public void handleMessage(Message msg) {
                        super.handleMessage(msg);
                        if (ConnectorController.getController().isConnected()) {
                            if (msg.what == 1) {
                                received = (String) msg.obj;
                                showProgress(false);
                                Log.v("Handler", received);
                                if (received.equals("success")) {
                                    Intent loginIntent = new Intent();
                                    loginIntent.setClass(LoginActivity.this, MainActivity.class);
                                    loginIntent.putExtra("username", username);
                                    loginIntent.putExtra("online", true);
                                    startActivity(loginIntent);
                                } else {
                                    passwordView.setError(getString(R.string.error_incorrect_password));
                                    passwordView.requestFocus();
                                }

                            }
                        } else {
                            //TODO: Tell user connection is lost
                        }
                    }

                };
                Message message = SSLConnector.getConnector().getHandler().obtainMessage(4);
                message.obj = handler;
                SSLConnector.getConnector().getHandler().sendMessage(message);
                handler.obtainMessage();
            }

//            mAuthTask = new UserLoginTask(username, password);
//            mAuthTask.execute(SeverURL);
        }
    }

    private boolean isUsernameValid(String username) {
        //TODO: Replace this with your own logic
        return username.length() > 2;
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 2;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
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

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addUsernameAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private void addUsernameAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        usernameView.setAdapter(adapter);
    }


    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<String, Void, String> {

        private final String mUsername;
        private final String mPassword;
        private String record;

        UserLoginTask(String username, String password) {
            mUsername = username;
            mPassword = password;
        }

        @Override
        protected String doInBackground(String... urls) {
            // TODO: attempt authentication against a network service.

            try {
                return getStringDataFrom(urls[0]);
//                String responseFromServer = getStringDataFrom(urls[0]);
//                if(responseFromServer.equalsIgnoreCase("granted")){
//                    return "granted";
//                }
//                Toast.makeText(getApplicationContext(), "Password incorrect.",Toast.LENGTH_LONG);
//                return "denied";
            } catch (IOException e) {
                return "Connection failed by Thread.";
            }

            // TODO: register the new account here.
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
                    requestJson.put("username", usernameView.getText().toString());
                    requestJson.put("password", passwordView.getText().toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                String requestStr = requestJson.toString();
                record = requestStr;
                outputStreamWriter.write("login=" + requestStr);
                outputStreamWriter.flush();
                outputStreamWriter.close();

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
        protected void onPostExecute(String result) {
            mAuthTask = null;
            showProgress(false);

            JSONObject confirm = null;
            String username = usernameView.getText().toString(), permission = "Nothing received.";
            try {
                confirm = new JSONObject(result);
//                username = confirm.get("username").toString();
//                password = confirm.get("password").toString();
//                Toast.makeText(getApplicationContext(), "NewID: "+username+" PW: " +password, Toast.LENGTH_LONG);
                permission = confirm.get("permission").toString();
                if (permission.equalsIgnoreCase("granted")) {
                    Intent loginIntent = new Intent();
                    loginIntent.setClass(LoginActivity.this, MainActivity.class);
                    loginIntent.putExtra("username", username);
                    loginIntent.putExtra("online", true);
                    startActivity(loginIntent);
                } else {
                    passwordView.setError(getString(R.string.error_incorrect_password));
                    passwordView.requestFocus();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}

