package layout;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.ThumbnailUtils;
import android.os.Environment;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.evelin.spenpal.Network.ConnectorController;
import com.evelin.spenpal.Network.SSLConnector;
import com.evelin.spenpal.R;


import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AddingFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AddingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddingFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final int RESULT_OK = -1;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private TextView amountView;
    private String amountString = "";
    private double amountDecimalValue = 0.00;
    private float amountValue;
    private boolean isDecimal = false;
    private String choosedCategory = "";
    private String formattedDate;

    private ImageButton transportButton;
    private ImageButton defaultButton;
    private ImageButton foodButton;
    private ImageButton drinkButton;
    private ImageButton shoppingButton;
    private ImageButton hobbyButton;
    private ImageButton dailyButton;
    private ImageButton personalButton;
    private ImageButton entertainmentButton;
    private ImageButton movieButton;
    private ImageButton socialButton;

    private ImageButton dotButton;
    private ImageButton num0Button;
    private ImageButton num1Button;
    private ImageButton num2Button;
    private ImageButton num3Button;
    private ImageButton num4Button;
    private ImageButton num5Button;
    private ImageButton num6Button;
    private ImageButton num7Button;
    private ImageButton num8Button;
    private ImageButton num9Button;

    private ImageButton cancelButton;
    private ImageButton completeButton;

    private SQLiteDatabase db;
    private OnFragmentInteractionListener mListener;
    private OnCompleteListener completeListener;

    private MediaRecorder audioRecorder = null;
    private MediaPlayer audioPlayer = null;
    private String recordPath = null;//path of records
    private String photoPath = null;//path of photos
    private File photoDirectory = null;
    private boolean playing = false, endOfData = false;
    private String username = "";
    private boolean isOnline = false;
    int loopCount = 0;

    ImageView photoPreview;
    View addPhotoPanel;
    Button photoButton;

    public AddingFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AddingFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AddingFragment newInstance(String param1, String param2) {
        AddingFragment fragment = new AddingFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
        Date nowDate = new Date(System.currentTimeMillis());
        formattedDate = formatter.format(nowDate);

        View addView = inflater.inflate(R.layout.fragment_adding, container, false);
        amountView = (TextView) addView.findViewById(R.id.newSpendingAmountText);

        transportButton = (ImageButton) addView.findViewById(R.id.transportButton);
        transportButton.setOnClickListener(new categoryButtonClickListener("Transport") {

        });
        defaultButton = (ImageButton) addView.findViewById(R.id.defaultButton);
        defaultButton.setOnClickListener(new categoryButtonClickListener("Default") {

        });
        drinkButton = (ImageButton) addView.findViewById(R.id.drinkButton);
        drinkButton.setOnClickListener(new categoryButtonClickListener("Drink") {

        });
        foodButton = (ImageButton) addView.findViewById(R.id.foodButton);
        foodButton.setOnClickListener(new categoryButtonClickListener("Food") {

        });

        socialButton = (ImageButton) addView.findViewById(R.id.socialButton);
        socialButton.setOnClickListener(new categoryButtonClickListener("Social"));

        shoppingButton = (ImageButton) addView.findViewById(R.id.shoppingButton);
        shoppingButton.setOnClickListener(new categoryButtonClickListener("Shopping"));

        hobbyButton = (ImageButton) addView.findViewById(R.id.hobbyButton);
        hobbyButton.setOnClickListener(new categoryButtonClickListener("Hobby"));

        dailyButton = (ImageButton) addView.findViewById(R.id.dailyButton);
        dailyButton.setOnClickListener(new categoryButtonClickListener("Daily"));

        personalButton = (ImageButton) addView.findViewById(R.id.personalButton);
        personalButton.setOnClickListener(new categoryButtonClickListener("Personal"));

        entertainmentButton = (ImageButton) addView.findViewById(R.id.entertainmentButton);
        entertainmentButton.setOnClickListener(new categoryButtonClickListener("Entertainment"));

        movieButton = (ImageButton) addView.findViewById(R.id.movieButton);
        movieButton.setOnClickListener(new categoryButtonClickListener("Movie"));

        dotButton = (ImageButton) addView.findViewById(R.id.dotButton);
        dotButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isDecimal) {
                    amountString = amountString + ".";
                    amountView.setText(amountString + "￥");
                    isDecimal = true;
                }
            }
        });
        num0Button = (ImageButton) addView.findViewById(R.id.number0);
        num0Button.setOnClickListener(new numButtonClickListener(0) {
        });

        num1Button = (ImageButton) addView.findViewById(R.id.number1);
        num1Button.setOnClickListener(new numButtonClickListener(1) {
        });

        num2Button = (ImageButton) addView.findViewById(R.id.number2);
        num2Button.setOnClickListener(new numButtonClickListener(2) {
        });

        num3Button = (ImageButton) addView.findViewById(R.id.number3);
        num3Button.setOnClickListener(new numButtonClickListener(3) {
        });

        num4Button = (ImageButton) addView.findViewById(R.id.number4);
        num4Button.setOnClickListener(new numButtonClickListener(4) {
        });

        num5Button = (ImageButton) addView.findViewById(R.id.number5);
        num5Button.setOnClickListener(new numButtonClickListener(5) {
        });

        num6Button = (ImageButton) addView.findViewById(R.id.number6);
        num6Button.setOnClickListener(new numButtonClickListener(6) {
        });

        num7Button = (ImageButton) addView.findViewById(R.id.number7);
        num7Button.setOnClickListener(new numButtonClickListener(7) {
        });

        num8Button = (ImageButton) addView.findViewById(R.id.number8);
        num8Button.setOnClickListener(new numButtonClickListener(8) {
        });

        num9Button = (ImageButton) addView.findViewById(R.id.number9);
        num9Button.setOnClickListener(new numButtonClickListener(9) {
        });

        cancelButton = (ImageButton) addView.findViewById(R.id.undoButton);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                amountString = "0.00";
                amountValue = (float) 0.00;
                amountView.setText(amountString + "￥");
            }
        });

        completeButton = (ImageButton) addView.findViewById(R.id.completeButton);   // here still need to build the listener for formattedDate for Adding Fragment from Main Fragment
        completeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(v.getContext().getFilesDir().getAbsolutePath().replace("files", "databases") + "spending.db", null);
                Cursor cursor = db.rawQuery("SELECT * FROM " + username + " WHERE date = \"" + formattedDate + "\" ", null);
                int count = cursor.getCount();
                db.execSQL("INSERT INTO " + username + " VALUES(?,?,?,?,?,?,?)", new Object[]{count, choosedCategory, amountValue, formattedDate, "Testing", null, 0});
                cursor.close();
                db.close();

                if (ConnectorController.getController().isConnected()) {
                    ConnectorController.getController().sendData("110 " + username + " " + count + " " + choosedCategory + " " + amountValue + " " + formattedDate + " " + "Some Comment");//TODO fill with the data to be sent
                }
                Handler handler = new Handler();
                Message message = SSLConnector.getConnector().getHandler().obtainMessage(4);
                message.obj = handler;
                SSLConnector.getConnector().getHandler().sendMessage(message);
                handler.obtainMessage();
                completeListener.jumpToTimeline();
                Toast.makeText(getContext(), "" + formattedDate, Toast.LENGTH_LONG).show();
                amountString = "";
                amountValue = 0;
            }
        });
        //record processing
        final Button recordButton = (Button) addView.findViewById(R.id.addRecord);
        final View recordPanel = addView.findViewById(R.id.addtaskRecordPanel);
        recordPanel.setVisibility(View.GONE);
        final Button recordDeleteButton = (Button) addView.findViewById(R.id.addTaskRecordDelete);
        final Button recordPlayButton = (Button) addView.findViewById(R.id.addTaskRecordPlay);
        recordButton.setOnClickListener(new View.OnClickListener() {//record
            private boolean recording = false;

            @Override
            public void onClick(View v) {

                if (!recording) {
                    audioRecorder = new MediaRecorder();
                    audioRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                    audioRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_WB);
                    audioRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB);
                    File recordfile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/SpenPal/records/");
                    if (!recordfile.exists()) {
                        //noinspection ResultOfMethodCallIgnored
                        recordfile.mkdirs();
                    }
                    SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmm");
                    recordPath = recordfile.getPath() + File.separator + df.format(new Date()) + ".amr";
                    audioRecorder.setOutputFile(recordPath);
                    try {
                        audioRecorder.prepare();
                        audioRecorder.start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    recordButton.setText("Stop");
                    recording = !recording;
                    if (recordPanel.getVisibility() == View.GONE) {
                        recordPanel.setVisibility(View.VISIBLE);
                    }
                } else {
                    audioRecorder.stop();
                    audioRecorder.release();
                    audioRecorder = null;
                    recordButton.setText("Add Record");
                    recordDeleteButton.setEnabled(true);//enable other buttons
                    recordPlayButton.setEnabled(true);
                    recordButton.setEnabled(false);//disable this button
                    recording = !recording;
                }
            }

        });
        recordPlayButton.setEnabled(false);
        recordPlayButton.setOnClickListener(new View.OnClickListener() {//previewing record
            @Override
            public void onClick(View v) {
                if (!playing) {
                    audioPlayer = new MediaPlayer();
                    audioPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    recordDeleteButton.setEnabled(false);//prevent from deleting during playing
                    audioPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            recordPlayButton.setText("Play");
                            recordDeleteButton.setEnabled(true);
                            playing = false;
                        }
                    });
                    try {
                        audioPlayer.setDataSource(recordPath);
                        audioPlayer.prepare();
                        audioPlayer.start();
                        playing = true;
                        recordPlayButton.setText("Stop");
                    } catch (IOException e) {//generally this is due to file loss
                        Toast.makeText(getContext(), "Record not found", Toast.LENGTH_SHORT).show();
                        recordButton.setEnabled(true);
                        recordPlayButton.setEnabled(false);
                        recordDeleteButton.setEnabled(false);
                        e.printStackTrace();
                    }
                } else {
                    audioPlayer.stop();
                    audioPlayer.release();
                    audioPlayer = null;
                    recordDeleteButton.setEnabled(true);
                    recordPlayButton.setText("Play");
                    playing = false;
                }
            }
        });
        recordDeleteButton.setOnClickListener(new View.OnClickListener() {//delete record
            @Override
            public void onClick(View v) {
                File recordFile = new File(recordPath);
                recordFile.delete();
                recordButton.setEnabled(true);
                recordPlayButton.setEnabled(false);
                recordDeleteButton.setEnabled(false);
                recordPanel.setVisibility(View.GONE);
                recordPath = "";
            }
        });
        photoButton = (Button) addView.findViewById(R.id.addPhoto);
        final Button photoDeleteButton = (Button) addView.findViewById(R.id.addTaskPhotoDelete);
        addPhotoPanel = addView.findViewById(R.id.addtaskPhotoPanel);
        addPhotoPanel.setVisibility(View.GONE);
        //photo taking
        View.OnClickListener photoListener = new View.OnClickListener() {//expand photoDirectory panel
            @Override
            public void onClick(View v) {
                if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                    Toast.makeText(getContext(), "Cannot access storage", Toast.LENGTH_SHORT).show();
                } else {
                    SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmm");
                    photoPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/SpenPal/photos/" + df.format(new Date()) + ".jpg";
                    photoDirectory = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/SpenPal/photos/");
                    photoDirectory.mkdirs();
                    String photoName = df.format(new Date()) + ".jpg";
                    File photo = new File(photoDirectory, photoName);
                    Intent takePic = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    photoDeleteButton.setEnabled(true);
                    takePic.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photo));
                    startActivityForResult(takePic, 1);
                }
            }
        };
        photoButton.setOnClickListener(photoListener);

        photoPreview = (ImageView) addView.findViewById(R.id.addTaskPhotoPreview);
        //photo deleting
        photoDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File photo = new File(photoPath);
                photoPreview.setImageBitmap(null);
                photo.delete();
                photoPath = "";
                photoDeleteButton.setEnabled(false);
                addPhotoPanel.setVisibility(View.GONE);
                photoButton.setEnabled(true);
            }
        });
        return addView;
    }

    public interface OnCompleteListener {
        void jumpToTimeline();
    }

    public void setOnlineStatus(String un, Boolean os) {
        username = un;
        isOnline = os;
    }

    public void changeAddingDate(String date) {
        formattedDate = date;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
            completeListener = (OnCompleteListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }


    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    class numButtonClickListener implements View.OnClickListener {
        int number;

        public numButtonClickListener(int num) {
            number = num;
        }

        @Override
        public void onClick(View v) {
            if (amountString.equalsIgnoreCase("0.00")) {
                amountString = "";
            }
            amountString = amountString + number;
            amountValue = Float.parseFloat(amountString);
            amountView.setText(amountString + "￥");
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Bitmap bmp = BitmapFactory.decodeFile(photoPath);
            photoPreview.setImageBitmap(ThumbnailUtils.extractThumbnail(bmp, bmp.getWidth() / 5, bmp.getHeight() / 5, ThumbnailUtils.OPTIONS_RECYCLE_INPUT));
            addPhotoPanel.setVisibility(View.VISIBLE);
            photoButton.setEnabled(false);

        }
    }

    class categoryButtonClickListener implements View.OnClickListener {
        String category;

        public categoryButtonClickListener(String kind) {
            category = kind;
        }

        @Override
        public void onClick(View v) {
            choosedCategory = category;
        }
    }
}
