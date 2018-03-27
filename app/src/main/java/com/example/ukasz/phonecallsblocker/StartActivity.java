package com.example.ukasz.phonecallsblocker;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;

import com.example.ukasz.androidsqlite.Block;

public class StartActivity extends AppCompatActivity implements HomeFragment.OnFragmentInteractionListener,
        PhoneBlockFragment.OnListFragmentInteractionListener
{

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

    /**
     * The {@link SharedPreferences} where are saved a app settings.
     */
    private SharedPreferences data;

    private boolean detectEnabled;
    private boolean autoBlockEnabled;

    /**
     * Method which runs on activity start.
     * @param savedInstanceState saved instance of Activity state.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        //load data settings (saved in shared preferences)
        loadSettingsState();

        Toolbar toolbar = findViewById(R.id.start_activity_toolbar);
        setSupportActionBar(toolbar);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = findViewById(R.id.start_activity_container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // Set up the listeners
        TabLayout tabLayout = findViewById(R.id.start_activity_tabs);
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));

        FloatingActionButton fab = findViewById(R.id.start_activity_fab);
        fab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    /**
     * Method which runs on resume activity.
     */
    @Override
    protected void onResume()
    {
        super.onResume();
        Log.e("StartActivity","onResume() method");
        //loadSettingsState();
    }

    /**
     * Method which runs on activity restart.
     */
    @Override
    protected void onRestart()
    {
        super.onRestart();
        Log.e("StartActivity","onRestart() method");
        //loadSettingsState();
    }

    /**
     * This method loads a settings data from Shared Preferences
     * And set a correct state of service by call {setDetectEnabled(bool enable)} method
     */
    private void loadSettingsState()
    {
        //load data settings
        data = getSharedPreferences("data", Context.MODE_PRIVATE);
        detectEnabled = data.getBoolean("detectEnabled", false);
        autoBlockEnabled = data.getBoolean("autoBlockEnabled", false);
//        blockServiceSwitch.setChecked(detectEnabled);
//        autoBlockSwitch.setChecked(autoBlockEnabled);
//        textViewTestowy.setText(Boolean.toString(detectEnabled));

        Log.e("Loading data","MainActivity - loadSettingsState() method");

        setDetectEnabled(detectEnabled);
    }

    /**
     * This method runs after activity destroy
     * Stop or restart detecting service depends on detectEnabled
     */
    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        Log.e("StartActivity","onDestroy() method");
        Intent intent = new Intent(this, CallDetectService.class);
        if(!detectEnabled) stopService(intent);
        else
        {
            //restart service after killing app
            stopService(intent);
            startService(intent);
        }
    }

    /**
     *  This method start or restart the calls detecting service
     *  Depends on {@param enable}
     *
     * @param enable state of detecting calls, depends on switch position
     */
    public void setDetectEnabled(boolean enable)
    {
        detectEnabled = enable;
        Log.e("setDetectEnabled", "method enabled");

        Intent intent = new Intent(this, CallDetectService.class);
        if (enable)
        {
            Log.e("StartActivity","START CallDetectService [method call]");
            startService(intent);
        }
        else
        {
            Log.e("StartActivity","STOP CallDetectService [method call]");
            stopService(intent);
        }
    }

    /**
     * Creates a right side options menu (expandable).
     * @param menu Menu which will be created.
     * @return true if created, false if it's not.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate right side the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_start, menu);
        return true;
    }

    /**
     * Handles right side options menu item clicks.
     * Actions depends on item choose.
     * @param item Selected right side options item.
     * @return true if actions ran correctly, false if it didn't.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.menu_action_settings)
//        R.id.start
        {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Actions on fragment interaction.
     * @param uri Uri.
     */
    @Override
    public void onFragmentInteraction(Uri uri)
    {
        Log.e("bla", "bla JEST INTERAKCJA");
    }

    /**
     * Checks detectEnabled boolean value.
     * @return detectEnabled state.
     */
    public boolean isDetectEnabled()
    {
        return detectEnabled;
    }

    /**
     * Checks autoBlockEnabled boolean value.
     * @return autoBlockEnabled state.
     */
    public boolean isAutoBlockEnabled()
    {
        return autoBlockEnabled;
    }

    /**
     * detectEnabled getter.
     * @return value of detectEnabled.
     */
    public boolean getDetectEnabled()
    {
        return detectEnabled;
    }

    /**
     * autoBlockEnabled getter.
     * @return value of autoBlockEnabled.
     */
    public boolean getAutoBlockEnabled()
    {
        return autoBlockEnabled;
    }

    @Override
    public void onListFragmentInteraction(Block item)
    {

    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment
    {
        /**
         * The fragment argument representing the section number for this.
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment()
        {

        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber)
        {
            Log.e("sectionNumber:" , String.valueOf(sectionNumber));
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState)
        {
            View rootView = inflater.inflate(R.layout.fragment_start, container, false);
            TextView textView = rootView.findViewById(R.id.section_label);
            textView.setText(getString(R.string.start_activity_section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
            return rootView;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter
    {
        /**
         * SectionsPagerAdapter constructor.
         * @param fm A FragmentMenager to assign with this SectionsPagerAdapter.
         */
        public SectionsPagerAdapter(FragmentManager fm)
        {
            super(fm);
        }

        /**
         * Chooses a fragment which instance will be create and show.
         * @param position Position of fragment on the top sliding tabs.
         * @return Instance of selected Fragment depends on @param position
         */
        @Override
        public Fragment getItem(int position)
        {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).

            switch(position)
            {
                case 0: return HomeFragment.newInstance();
                case 1: return PhoneBlockFragment.newInstance(1);
                default: return PlaceholderFragment.newInstance(position + 1);
            }
        }

        /**
         * Sliding tabs amount getter/
         * @return Sliding tabs amount.
         */
        @Override
        public int getCount()
        {
            // Show 3 total pages.
            return 3;
        }
    }
}
