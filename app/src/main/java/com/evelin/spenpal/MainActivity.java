package com.evelin.spenpal;

import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.PagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;

import layout.AddingFragment;
import layout.GeneralFragment;
import layout.TimelineFragment;

public class MainActivity extends AppCompatActivity implements TimelineFragment.OnFragmentInteractionListener, AddingFragment.OnFragmentInteractionListener, GeneralFragment.OnFragmentInteractionListener, AddingFragment.OnCompleteListener, TimelineFragment.OnDateChangeListener, TimelineFragment.OnDeleteListener{

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    private CirclePageIndicator circlePageIndicator;

    public TimelineFragment tlf;
    public AddingFragment addf;
    public GeneralFragment gf;
    String username = "";
    static String theUsername;
    static boolean theStatus;
    TextView titleView;

    private Boolean isOnline = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent loginIntent = getIntent();
//        isOnline = loginIntent.getBooleanExtra("online", false);
//        if(isOnline!=null){
//            theStatus = isOnline;
//        }
        username = loginIntent.getStringExtra("username");
        if(username!=null){
            theUsername = username;
            isOnline = loginIntent.getBooleanExtra("online", false);
            theStatus = isOnline;
        }
        titleView = (TextView) findViewById(R.id.titleView);
        titleView.setText(theUsername);

        tlf = new TimelineFragment();
        tlf.setOnlineStatus(theUsername, theStatus);

        addf = new AddingFragment();
        addf.setOnlineStatus(theUsername, theStatus);

        gf = new GeneralFragment();
        gf.setOnlineStatus(theUsername);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setCurrentItem(1);

        circlePageIndicator = (CirclePageIndicator) findViewById(R.id.indicator);
        circlePageIndicator.setViewPager(mViewPager);


//      FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.floatingButton);
//      fab.setOnClickListener(new View.OnClickListener() {
//          @Override
//          public void onClick(View view) {
//              Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                      .setAction("Action", null).show();
//          }
//      });

    }

    protected void onResume(){
        super.onResume();
        Log.i("=status=", theUsername  + " " + theStatus);
        tlf.setOnlineStatus(theUsername, theStatus);

        addf.setOnlineStatus(theUsername, theStatus);

        gf.setOnlineStatus(theUsername);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent settingIntent = new Intent();
            settingIntent.setClass(MainActivity.this, SettingsActivity.class);
            settingIntent.putExtra("username", username);
            startActivity(settingIntent);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void jumpToTimeline() {
        mViewPager.setCurrentItem(1);
        tlf.updateUI();
        gf.updateUI();
    }

    @Override
    public void changeDate(String date) {
        addf.changeAddingDate(date);
    }

    @Override
    public void updateGraph() {
        gf.updateUI();
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

//        @Override
//        public int getItemPosition(Object object){
//            return PagerAdapter.POSITION_NONE;
//        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
//            return Placeholde   rFragment.newInstance(position + 1);
//            switch (position) {
//                case 0:
//                    return new AddingFragment();
//                case 1:
//                    return new TimelineFragment();
//                case 2:
//                    return new GeneralFragment();
//
//            }
//            return null;
            switch (position) {
                case 0:
                    return addf;
                case 1:
                    return tlf;
                case 2:
                    return gf;

            }
            return null;

        }


        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "A New Spending?";
                case 1:
                    return "Timeline";
                case 2:
                    return "General View";
            }
            return null;
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_timeline, container, false);
            return rootView;
        }
    }
}
