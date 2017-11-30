package layout;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Message;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.evelin.spenpal.Network.ConnectorController;
import com.evelin.spenpal.Network.SSLConnector;
import com.evelin.spenpal.R;
import com.evelin.spenpal.SpendListTimeLineAdapter;
import com.evelin.spenpal.ActionSheetDialog;
import com.evelin.spenpal.ActionSheetDialog.OnSheetItemClickListener;
import com.evelin.spenpal.ActionSheetDialog.SheetItemColor;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link TimelineFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link TimelineFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TimelineFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String SeverURL = "http://byebyebymyai.com/share.php";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private String username = "";
    private Boolean isOnline = false;
    public Boolean shareSuccess = false;
    boolean endOfData = false;

    int loopCount = 0;

    private OnDateChangeListener onDateChangeListener;
    private OnDeleteListener onDeleteListener;

    public SQLiteDatabase mainDB;
    private String formattedDate;
    private List<Integer> keyList;
    private ListView spendListView;
    private List<Map<String, Object>> spendingListItems;
    private SpendListTimeLineAdapter timelineAdapter;

    private ImageButton lastDayButton;
    private ImageButton nextDayButton;
    private TextView dateText;

    private Button syncButton;

    private OnFragmentInteractionListener mListener;

    public TimelineFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TimelineFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TimelineFragment newInstance(String param1, String param2) {
        TimelineFragment fragment = new TimelineFragment();
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
        if (!ConnectorController.getController().isConnected()) {
            ConnectorController.getController().connect();
        }

    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
        Date nowDate = new Date(System.currentTimeMillis());
        formattedDate = formatter.format(nowDate);
        View timelineView = inflater.inflate(R.layout.fragment_timeline, container, false);

        spendListView = (ListView) timelineView.findViewById(R.id.spendlist);
        spendListView.setDividerHeight(0);

        dateText = (TextView) timelineView.findViewById(R.id.dateText);
        dateText.setText(formattedDate);

        lastDayButton = (ImageButton) timelineView.findViewById(R.id.lastDayButton);
        lastDayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                formattedDate = getDateString(formattedDate, -1);
                updateUI();
                onDateChangeListener.changeDate(formattedDate);

            }
        });
        nextDayButton = (ImageButton) timelineView.findViewById(R.id.nextDayButton);
        nextDayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                formattedDate = getDateString(formattedDate, 1);
                updateUI();
                onDateChangeListener.changeDate(formattedDate);
            }
        });

        syncButton = (Button) timelineView.findViewById(R.id.syncButton);
        syncButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ConnectorController.getController().isConnected()) {

                    ConnectorController.getController().sendData("111 " + username + " 0");//TODO fill with the data to be sent
                    endOfData = false;
                    mainDB = SQLiteDatabase.openOrCreateDatabase(getContext().getFilesDir().getAbsolutePath().replace("files", "databases") + "spending.db", null);
                    mainDB.execSQL("DROP TABLE IF EXISTS " + username);
                    mainDB.execSQL("CREATE TABLE IF NOT EXISTS " + username + " (id INTEGER, category VARCHAR, amount FLOAT, date VARCHAR, comment VARCHAR, image BLOB, shared INTEGER, PRIMARY KEY(id,date))");
                    Handler handler = new Handler() {
                        String received;

                        public void handleMessage(Message msg) {
                            super.handleMessage(msg);
                            if (ConnectorController.getController().isConnected()) {
                                if (msg.what == 1) {
                                    received = (String) msg.obj;//
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
                                                            endOfData = true;
                                                            mainDB.close();
                                                            updateUI();
                                                        }
                                                        if (!endOfData) {
                                                            try {
                                                                mainDB.execSQL(rec);
                                                            } catch (Exception e) {
                                                                e.printStackTrace();
                                                            }

                                                        }
                                                    }
                                                } else {
                                                    //TODO: Tell user connection is lost
                                                    Log.v("=conn=", "Connection Problem while syncing.");
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
        });

        return timelineView;
    }


    public void onResume() {
        super.onResume();
        mainDB = SQLiteDatabase.openOrCreateDatabase(this.getContext().getFilesDir().getAbsolutePath().replace("files", "databases") + "spending.db", null);
        mainDB.execSQL("CREATE TABLE IF NOT EXISTS " + username + " (id INTEGER , category VARCHAR, amount FLOAT, date VARCHAR, comment VARCHAR, image BLOB, shared INTEGER, PRIMARY KEY(id,date))");
        Cursor cursor = mainDB.rawQuery("SELECT * FROM " + username + " WHERE date = \"" + formattedDate + "\" ", null);
        spendingListItems = new ArrayList<Map<String, Object>>();
        int count = cursor.getCount();
        keyList = new ArrayList<Integer>();
        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndex("id"));
            keyList.add(id);

            Map<String, Object> spendingListItem = new HashMap<String, Object>();
            String category = cursor.getString(cursor.getColumnIndex("category"));
            spendingListItem.put("category", category);
            Float amount = cursor.getFloat(cursor.getColumnIndex("amount"));
            spendingListItem.put("amount", amount);
            String date = cursor.getString(cursor.getColumnIndex("date"));
            spendingListItem.put("date", date);
            String comment = cursor.getString(cursor.getColumnIndex("comment"));
            spendingListItem.put("comment", comment);
            int shared = cursor.getInt(cursor.getColumnIndex("shared"));
            spendingListItem.put("shared", shared == 1 ? true : false);
            spendingListItem.put("uname", username);
            spendingListItems.add(spendingListItem);
            Log.i("=DB=", "Category->" + category + " --- Amount->" + amount);
        }
        cursor.close();
        mainDB.close();
        timelineAdapter = new SpendListTimeLineAdapter(this.getActivity(), spendingListItems);
        spendListView.setAdapter(timelineAdapter);
        spendListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, final View view, final int position, long id) {
                new ActionSheetDialog(getContext())
                        .builder()
                        .setTitle(" What u wanna do with this? ")
                        .setCancelable(false)
                        .setCanceledOnTouchOutside(false)
                        .addSheetItem("Share", SheetItemColor.Blue,
                                new OnSheetItemClickListener() {
                                    @Override
                                    public void onClick(int which) {
                                        if (!isOnline) {
                                            Toast.makeText(getContext(), "Offline now", Toast.LENGTH_LONG);
                                            Log.i("=fail=", "not online");
                                        } else {
                                            mainDB = SQLiteDatabase.openOrCreateDatabase(getContext().getFilesDir().getAbsolutePath().replace("files", "databases") + "spending.db", null);
                                            mainDB.execSQL("UPDATE " + username + " SET shared = 1 WHERE id = ? AND date = ?", new Object[]{position, formattedDate});
                                            Cursor cursor = mainDB.rawQuery("SELECT * FROM " + username + " WHERE id = " + position + " AND date = \"" + formattedDate + "\" ", null);
                                            String category = "", date = "", comment = "";
                                            float amount = 0f;
                                            while (cursor.moveToNext()) {
                                                category = cursor.getString(cursor.getColumnIndex("category"));
                                                amount = cursor.getFloat(cursor.getColumnIndex("amount"));
                                                date = cursor.getString(cursor.getColumnIndex("date"));
                                                comment = cursor.getString(cursor.getColumnIndex("comment"));
                                            }
                                            cursor.close();
                                            mainDB.close();

                                            if (ConnectorController.getController().isConnected()) {
                                                ConnectorController.getController().sendData("121" + " " + username + " " + position + " " + formattedDate); // share
                                            }

                                            spendingListItems.get(position).put("shared", true);

                                            timelineAdapter = new SpendListTimeLineAdapter(getActivity(), spendingListItems);
                                            spendListView.setAdapter(timelineAdapter);
                                            timelineAdapter.notifyDataSetChanged();

                                            Log.i("=suc=", "ID: " + username + "KIND: " + category + "Amount:" + amount);
                                        }
                                    }
                                })
                        .addSheetItem("Delete", SheetItemColor.Red,
                                new OnSheetItemClickListener() {
                                    @Override
                                    public void onClick(int which) {
                                        //Delete Here
                                        mainDB = SQLiteDatabase.openOrCreateDatabase(getContext().getFilesDir().getAbsolutePath().replace("files", "databases") + "spending.db", null);
                                        mainDB.execSQL("DELETE FROM " + username + " WHERE id = ? AND date = ?", new Object[]{position, formattedDate});
                                        mainDB.execSQL("UPDATE " + username + " SET id = id - 1 WHERE id > ? AND date = ?", new Object[]{position, formattedDate});
                                        mainDB.close();

                                        if (ConnectorController.getController().isConnected()) {
                                            ConnectorController.getController().sendData("119" + " " + username + " " + position + " " + formattedDate);//TODO fill with the data to be sent
                                        }

                                        onDeleteListener.updateGraph();
                                        updateUI();
                                    }
                                }).show();
                return false;
            }
        });
        timelineAdapter.notifyDataSetChanged();
    }

    public void setOnlineStatus(String un, Boolean os) {
        username = un;
        isOnline = os;
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
            onDateChangeListener = (OnDateChangeListener) context;
            onDeleteListener = (OnDeleteListener) context;
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

    public void updateUI() {
        mainDB = SQLiteDatabase.openOrCreateDatabase(this.getContext().getFilesDir().getAbsolutePath().replace("files", "databases") + "spending.db", null);
        mainDB.execSQL("CREATE TABLE IF NOT EXISTS " + username + " (id INTEGER PRIMARY KEY, category VARCHAR, amount FLOAT, date VARCHAR, comment VARCHAR, image BLOB, shared INTEGER)");
//        Cursor cursor = mainDB.rawQuery("SELECT * FROM spendings",null);
        Cursor cursor = mainDB.rawQuery("SELECT * FROM " + username + " WHERE date = \"" + formattedDate + "\" ", null);
        spendingListItems = new ArrayList<Map<String, Object>>();
        int count = cursor.getCount();
        keyList = new ArrayList<Integer>();
        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndex("id"));
            keyList.add(id);

            Map<String, Object> spendingListItem = new HashMap<String, Object>();
            String category = cursor.getString(cursor.getColumnIndex("category"));
            spendingListItem.put("category", category);
            Float amount = cursor.getFloat(cursor.getColumnIndex("amount"));
            spendingListItem.put("amount", amount);
            String date = cursor.getString(cursor.getColumnIndex("date"));
            spendingListItem.put("date", date);
            String comment = cursor.getString(cursor.getColumnIndex("comment"));
            spendingListItem.put("comment", comment);
            int shared = cursor.getInt(cursor.getColumnIndex("shared"));
            spendingListItem.put("shared", shared == 1 ? true : false);
            spendingListItem.put("uname", username);
            spendingListItems.add(spendingListItem);
            Log.i("=DB=", "Category->" + category + " --- Amount->" + amount);
        }
        cursor.close();
        mainDB.close();
        timelineAdapter = new SpendListTimeLineAdapter(this.getActivity(), spendingListItems);
        spendListView.setAdapter(timelineAdapter);
        timelineAdapter.notifyDataSetChanged();
        dateText.setText(formattedDate);
    }

    public static String getDateString(String today, int dayAddNum) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
        Date nowDate = null;
        try {
            nowDate = formatter.parse(today);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Date newDate2 = new Date(nowDate.getTime() + dayAddNum * 24 * 60 * 60 * 1000);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd");
        String dateOk = simpleDateFormat.format(newDate2);
        return dateOk;
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */

    public interface OnDateChangeListener {
        void changeDate(String date);
    }

    public interface OnDeleteListener {
        void updateGraph();
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

}
