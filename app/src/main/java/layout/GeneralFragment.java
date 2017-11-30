package layout;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.graphics.Typeface;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lecho.lib.hellocharts.animation.ChartAnimationListener;
import lecho.lib.hellocharts.formatter.AxisValueFormatter;
import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.listener.LineChartOnValueSelectListener;
import lecho.lib.hellocharts.listener.PieChartOnValueSelectListener;
import lecho.lib.hellocharts.model.PieChartData;
import lecho.lib.hellocharts.model.SliceValue;
import lecho.lib.hellocharts.util.ChartUtils;
import lecho.lib.hellocharts.view.Chart;
import lecho.lib.hellocharts.view.PieChartView;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PieChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.ValueShape;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.util.ChartUtils;
import lecho.lib.hellocharts.view.Chart;
import lecho.lib.hellocharts.view.LineChartView;
import lecho.lib.hellocharts.view.PieChartView;

import com.evelin.spenpal.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link GeneralFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link GeneralFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GeneralFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public SQLiteDatabase mainDB;
    private String formattedDate, dayOfWeek;

    private LineChartView lineChart;
    private LineChartData lineData;
    private int numberOfLines = 1;
    private int maxNumberOfLines = 4;
    private int numberOfPoints = 7;

    float[][] spendingDataTab = new float[maxNumberOfLines][numberOfPoints];
    float maximumDayTotalSpending = 100f; // default

    private boolean hasAxes = true;
    private boolean hasAxesNames = true;
    private boolean hasLines = true;
    private boolean hasPoints = true;
    private ValueShape shape = ValueShape.CIRCLE;
    private boolean isFilled = false;
    private boolean hasLabels = false;
    private boolean isCubic = false;
    private boolean hasLabelForSelected = false;
    private boolean pointsHaveDifferentColor = false;


    private PieChartView pieChart;
    private PieChartData pieData;
    private boolean hasLabelsOutside = false;
    private boolean hasCenterCircle = true;
    private boolean hasCenterText1 = true;
    private boolean hasCenterText2 = true;
    private boolean isExploded = false;

    private String username = "";

    private OnFragmentInteractionListener mListener;

    public GeneralFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment GeneralFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static GeneralFragment newInstance(String param1, String param2) {
        GeneralFragment fragment = new GeneralFragment();
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
        View graphView = inflater.inflate(R.layout.fragment_general, container, false);

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
        Date nowDate = new Date(System.currentTimeMillis());
        formattedDate = formatter.format(nowDate);

        SimpleDateFormat formatter2 = new SimpleDateFormat("EEE");
        Date nowDate2 = new Date(System.currentTimeMillis());
        dayOfWeek = formatter2.format(nowDate2);

        lineChart = (LineChartView) graphView.findViewById(R.id.lineChart);
        lineChart.setOnValueTouchListener(new ValueTouchListener());
        getDataFromSpending();
        generateLineData();
        lineChart.setViewportCalculationEnabled(true); // Y Axis Unit!
//        resetViewport();

        pieChart = (PieChartView) graphView.findViewById(R.id.pieChart);
        pieChart.setOnValueTouchListener(new PieValueTouchListener());
        generatePieData();

        return graphView;
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
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    private void generateLineData() {

        List<Line> lines = new ArrayList<Line>();
        for (int i = 0; i < numberOfLines; ++i) {

            List<PointValue> values = new ArrayList<PointValue>();
            for (int j = 0; j < numberOfPoints; ++j) {
                values.add(new PointValue(j, spendingDataTab[i][j]));
            }

            Line line = new Line(values);
            line.setColor(ChartUtils.COLORS[i]);
            line.setShape(shape);
            line.setCubic(isCubic);
            line.setFilled(isFilled);
            line.setHasLabels(hasLabels);
            line.setHasLabelsOnlyForSelected(hasLabelForSelected);
            line.setHasLines(hasLines);
            line.setHasPoints(hasPoints);
            if (pointsHaveDifferentColor){
                line.setPointColor(ChartUtils.COLORS[(i + 1) % ChartUtils.COLORS.length]);
            }
            lines.add(line);
        }

        lineData = new LineChartData(lines);

        if (hasAxes) {
            Axis axisX = new Axis();
            Axis axisY = new Axis().setHasLines(true);
            if (hasAxesNames) {
                // axisX.setName("Week");
                List<AxisValue> unit = new ArrayList<AxisValue>();

                AxisValue sunday = new AxisValue(0);
                unit.add(sunday.setLabel("Sun"));
                AxisValue monday = new AxisValue(1);
                unit.add(monday.setLabel("Mon"));
                AxisValue tuesday = new AxisValue(2);
                unit.add(tuesday.setLabel("Tue"));
                AxisValue wednesday = new AxisValue(3);
                unit.add(wednesday.setLabel("Wed"));
                AxisValue thursday = new AxisValue(4);
                unit.add(thursday.setLabel("Thu"));
                AxisValue friday = new AxisValue(5);
                unit.add(friday.setLabel("Fri"));
                AxisValue saturday = new AxisValue(6);
                unit.add(saturday.setLabel("Sat"));

                axisX.setValues(unit);

                axisY.setName("Money");
            }
            lineData.setAxisXBottom(axisX);
            lineData.setAxisYLeft(axisY);
        } else {
            lineData.setAxisXBottom(null);
            lineData.setAxisYLeft(null);
        }

        lineData.setBaseValue(Float.NEGATIVE_INFINITY);
        lineChart.setLineChartData(lineData);

    }

    private void generatePieData() {

        int numValues = 11; //  until drinks currently, that's 11 kinds in total

        List<SliceValue> values = new ArrayList<SliceValue>();
//        for (int i = 0; i < numValues; ++i) {
//            SliceValue sliceValue = new SliceValue((float) Math.random() * 30 + 15, ChartUtils.pickColor());
//            values.add(sliceValue);
//        }

        mainDB = SQLiteDatabase.openOrCreateDatabase(this.getContext().getFilesDir().getAbsolutePath().replace("files", "databases") + "spending.db", null);
        Cursor defaultCursor = mainDB.rawQuery("SELECT SUM(amount),category FROM " + username + " GROUP BY category",null);
//        Cursor transportCursor = mainDB.rawQuery("SELECT SUM(amount) FROM spendings WHERE category = \" default \"  ",null);
        while(defaultCursor.moveToNext()){
            float eachCategoryTotal = defaultCursor.getFloat(0);
            String categoryName = defaultCursor.getString(1);
            SliceValue partValue = new SliceValue(eachCategoryTotal, ChartUtils.pickColor());
            values.add(partValue.setLabel(categoryName));
        }
        defaultCursor.close();
        mainDB.close();

        pieData = new PieChartData(values);
        pieData.setHasLabels(!hasLabels);
        pieData.setHasLabelsOnlyForSelected(hasLabelForSelected);
        pieData.setHasLabelsOutside(hasLabelsOutside);
        pieData.setHasCenterCircle(hasCenterCircle);

        if (isExploded) {
            pieData.setSlicesSpacing(24);
        }

        if (hasCenterText1) {
            pieData.setCenterText1("SpenPal");

            // Get roboto-italic font.
//            Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "Roboto-Italic.ttf");
//            pieData.setCenterText1Typeface(tf);

            // Get font size from dimens.xml and convert it to sp(library uses sp values).
            pieData.setCenterText1FontSize(ChartUtils.px2sp(getResources().getDisplayMetrics().scaledDensity,
                    (int) getResources().getDimension(R.dimen.pie_chart_text1_size)));
        }

        if (hasCenterText2) {
            pieData.setCenterText2(formattedDate);

//            Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "Roboto-Italic.ttf");
//            pieData.setCenterText2Typeface(tf);

            pieData.setCenterText2FontSize(ChartUtils.px2sp(getResources().getDisplayMetrics().scaledDensity,
                    (int) getResources().getDimension(R.dimen.pie_chart_text2_size)));
        }

        pieChart.setPieChartData(pieData);
    }

    private void getDataFromSpending() {
        //for testing use
//        for (int i = 0; i < maxNumberOfLines; ++i) {
//            for (int j = 0; j < numberOfPoints; ++j) {
//                spendingDataTab[i][j] = (float) Math.random() * 100f;
//            }
//        }

        mainDB = SQLiteDatabase.openOrCreateDatabase(this.getContext().getFilesDir().getAbsolutePath().replace("files", "databases") + "spending.db", null);
//        mainDB.execSQL("CREATE TABLE IF NOT EXISTS spendings (id INTEGER PRIMARY KEY, category VARCHAR, amount FLOAT, date VARCHAR, comment VARCHAR, image BLOB)");
        int i = 0;
        int weekLength = dayNumOfWeek(dayOfWeek);
        int j = weekLength + 1;
        while(i<=weekLength){
            String dayToCheck = getDateString(formattedDate, i-weekLength);
            Cursor cursor = mainDB.rawQuery("SELECT SUM(amount) FROM " + username + " WHERE date = \"" + dayToCheck + "\"  ",null);
            int count = cursor.getCount();
            while(cursor.moveToNext()){
                float dayTotal = cursor.getFloat(0);// index 0 stands for sum(amount)
                spendingDataTab[0][i] = dayTotal;
            }
            Log.i("=test=","dayToCheck: " + dayToCheck + "dayOfWeek: " + dayOfWeek +  "data: " + spendingDataTab[0][i]);
            cursor.close();
            i++;
        }

        while(j<7){
            spendingDataTab[0][j] = 0f;
            j++;
        }
        mainDB.close();

    }

    private int dayNumOfWeek(String dayOfweek){
        if(dayOfweek.equalsIgnoreCase("SUN")||dayOfweek.equalsIgnoreCase("星期日")){
            return 0;
        } else if(dayOfweek.equalsIgnoreCase("MON")||dayOfweek.equalsIgnoreCase("星期一")) {
            return 1;
        } else if(dayOfweek.equalsIgnoreCase("TUE")||dayOfweek.equalsIgnoreCase("星期二")) {
            return 2;
        } else if(dayOfweek.equalsIgnoreCase("WED")||dayOfweek.equalsIgnoreCase("星期三")) {
            return 3;
        } else if(dayOfweek.equalsIgnoreCase("THU")||dayOfweek.equalsIgnoreCase("星期四")) {
            return 4;
        } else if(dayOfweek.equalsIgnoreCase("FRI")||dayOfweek.equalsIgnoreCase("星期五")) {
            return 5;
        } else if(dayOfweek.equalsIgnoreCase("SAT")||dayOfweek.equalsIgnoreCase("星期六")) {
            return 6;
        }

        return 0;
    }

    public static String getDateString(String today,int dayAddNum) {
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

    private void resetViewport() { // manually set the Y-Axis Range

        final Viewport v = new Viewport(lineChart.getMaximumViewport());
        v.bottom = 0;
        v.top = 100;
        v.left = 0;
        v.right = numberOfPoints - 1;
        lineChart.setMaximumViewport(v);
        lineChart.setCurrentViewport(v);
    }

    public void updateUI(){
        getDataFromSpending();
        generateLineData();
        generatePieData();
//        lineChart.startDataAnimation();    !! The animation happens during the switching period so we can't see, haven't find a way to solve it.
//        pieChart.startDataAnimation();
    }

    public void setOnlineStatus(String un) {
        username = un;
    }

    private class ValueTouchListener implements LineChartOnValueSelectListener {

        @Override
        public void onValueSelected(int lineIndex, int pointIndex, PointValue value) {
            Toast.makeText(getActivity(), "Selected: " + value, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onValueDeselected() {
            // TODO Auto-generated method stub

        }
    }

    private class PieValueTouchListener implements PieChartOnValueSelectListener {

        @Override
        public void onValueSelected(int arcIndex, SliceValue value) {
            Toast.makeText(getActivity(), "Selected: " + value, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onValueDeselected() {
            // TODO Auto-generated method stub

        }

    }
}
