package com.example.ukasz.phonecallsblocker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;
import android.widget.Toast;

import com.example.ukasz.androidsqlite.Block;
import com.example.ukasz.androidsqlite.DatabaseHandler;

public class StartActivity extends AppCompatActivity implements SettingsFragment.OnFragmentInteractionListener
{

    /*
    The {@link android.support.v4.view.PagerAdapter} that will provide
    fragments for each of the sections. We use a
    {@link FragmentPagerAdapter} derivative, which will keep every
    loaded fragment in memory. If this becomes too memory intensive, it
    may be best to switch to
    a{@link android.support.v4.app.FragmentStatePagerAdapter}.*/
    private SectionsPagerAdapter mSectionsPagerAdapter;


    //The {@link ViewPager} that will host the section contents.
    private ViewPager mViewPager;

    //The {@link SharedPreferences} where are saved a app settings.
    private SharedPreferences data;

    //The {@link TelephonyManager} for fetch user phone number
    private TelephonyManager tm;

    private boolean detectEnabled;
    private boolean autoBlockEnabled;

    //The {@link com.github.clans.fab.FloatingActionMenu} instance.
    com.github.clans.fab.FloatingActionMenu fab;

    //request unique codes
    private final int ACTION_CONTACTS_CONTRACT_REQUEST_CODE = 1111;
    private final int READ_CONTACTS_PERMISSION_REQUEST_CODE = 1112;
    private final int READ_CALL_LOG_PERMISSION_REQUEST_CODE = 1113;

    /**
     * Method which runs on activity start.
     *
     * @param savedInstanceState saved instance of Activity state
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

        //Create the adapter that will return a fragment for each of the three
        //primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        //Set up the ViewPager with the sections adapter.
        mViewPager = findViewById(R.id.start_activity_container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        //Set telephony manager for fetch user phone number
        tm = (TelephonyManager) StartActivity.this.getSystemService(Context.TELEPHONY_SERVICE);

        //Set up the listeners
        TabLayout tabLayout = findViewById(R.id.start_activity_tabs);
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));

        //Floating Action Button Menu
        fab = findViewById(R.id.start_activity_fab);
        com.github.clans.fab.FloatingActionButton fab_add_manually = findViewById(R.id.start_activity_add_manually);
        fab_add_manually.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent addBlockIntent = new Intent(getApplicationContext(), AddPhoneBlock.class);
                startActivity(addBlockIntent);
            }
        });


        com.github.clans.fab.FloatingActionButton fab_add_contacts = findViewById(R.id.start_activity_add_contacts);
        fab_add_contacts.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent contactsIntent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                if (!hasGrantedReadContactsPermission())
                {
                    requestReadContactsPermission();
                }
                else startActivityForResult(contactsIntent, ACTION_CONTACTS_CONTRACT_REQUEST_CODE);
            }
        });

        com.github.clans.fab.FloatingActionButton fab_add_calls_registry = findViewById(R.id.start_activity_add_calls_registry);
        fab_add_calls_registry.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (!hasGrantedReadCallLogPermission())
                {
                    requestReadCallLogPermission();
                }
                else getPhoneFromCallLog();
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
        if (fab.isOpened()) fab.toggle(true);
        Log.e("StartActivity", "onResume() method");
        //loadSettingsState();
    }

    /**
     * Method which runs on activity restart.
     */
    @Override
    protected void onRestart()
    {
        super.onRestart();
        if (fab.isOpened()) fab.toggle(true);
        Log.e("StartActivity", "onRestart() method");
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

        Log.e("Loading data", "MainActivity - loadSettingsState() method");

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
        Log.e("StartActivity", "onDestroy() method");
    }

    /**
     * Schedule for calls detecting service in the background.
     */
    private void scheduleDetectingJob()
    {
        JobScheduler jobScheduler = (JobScheduler) getApplicationContext()
                .getSystemService(JOB_SCHEDULER_SERVICE);

        ComponentName componentName = new ComponentName(this, CallDetectService.class);

        JobInfo jobInfo = new JobInfo.Builder(1, componentName)
                .setPersisted(true)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .build();

        //Schedule detecting only Job Scheduler object is set up
        if (jobScheduler != null) jobScheduler.schedule(jobInfo);
    }

    /**
     * Cancel scheduled calls detecting service.
     */
    private void cancelDetectingJob()
    {
        JobScheduler jobScheduler = (JobScheduler) getApplicationContext()
                .getSystemService(JOB_SCHEDULER_SERVICE);

        //Cancel only Job Scheduler object is set up
        if (jobScheduler != null) jobScheduler.cancel(1);
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

        if (enable)
        {
            Log.e("StartActivity", "START CallDetectService [method call]");
            scheduleDetectingJob();
        }
        else
        {
            Log.e("StartActivity", "STOP CallDetectService [method call]");
            cancelDetectingJob();
        }
    }

    /**
     * Creates a right side options menu (expandable).
     * @param menu Menu which will be created.
     *
     * @return true if created, false if it's not
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
     *
     * @param item selected right side options item
     * @return true if actions ran correctly, false if it didn't
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
     *
     * @param uri Uri
     */
    @Override
    public void onFragmentInteraction(Uri uri)
    {
        Log.e("bla", "bla JEST INTERAKCJA");
    }

    /**
     * Checks detectEnabled boolean value.
     *
     * @return detectEnabled state
     */
    public boolean isDetectEnabled()
    {
        return detectEnabled;
    }

    /**
     * Checks autoBlockEnabled boolean value.
     *
     * @return autoBlockEnabled state
     */
    public boolean isAutoBlockEnabled()
    {
        return autoBlockEnabled;
    }

    /**
     * detectEnabled getter.
     *
     * @return value of detectEnabled
     */
    public boolean getDetectEnabled()
    {
        return detectEnabled;
    }

    /**
     * autoBlockEnabled getter.
     *
     * @return value of autoBlockEnabled
     */
    public boolean getAutoBlockEnabled()
    {
        return autoBlockEnabled;
    }

//
//    /**
//     * A placeholder fragment containing a simple view.
//     */
//    public static class PlaceholderFragment extends Fragment
//    {
//        /**
//         * The fragment argument representing the section number for this.
//         * fragment.
//         */
//        private static final String ARG_SECTION_NUMBER = "section_number";
//
//        public PlaceholderFragment()
//        {
//
//        }
//
//        /**
//         * Returns a new instance of this fragment for the given section
//         * number.
//         */
//        public static PlaceholderFragment newInstance(int sectionNumber)
//        {
//            Log.e("sectionNumber:", String.valueOf(sectionNumber));
//            PlaceholderFragment fragment = new PlaceholderFragment();
//            Bundle args = new Bundle();
//            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
//            fragment.setArguments(args);
//            return fragment;
//        }
//
//        @Override
//        public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                                 Bundle savedInstanceState)
//        {
//            View rootView = inflater.inflate(R.layout.fragment_start, container, false);
//            TextView textView = rootView.findViewById(R.id.section_label);
//            textView.setText(getString(R.string.start_activity_section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
//            return rootView;
//        }
//    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter
    {
        /**
         * SectionsPagerAdapter constructor.
         *
         * @param fm a FragmentMenager to assign with this SectionsPagerAdapter
         */
        SectionsPagerAdapter(FragmentManager fm)
        {
            super(fm);
        }

        /**
         * Chooses a fragment which instance will be create and show.
         *
         * @param position position of fragment on the top sliding tabs
         * @return instance of selected Fragment depends on @param position
         */
        @Override
        public Fragment getItem(int position)
        {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).

            switch (position)
            {
                case 0:
                    return RegistryFragment.newInstance(1);
                case 1:
                    return PhoneBlockFragment.newInstance(1);
                case 2:
                    return SettingsFragment.newInstance();
            }
            return null;
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

    /**
     * Opens a window to ask for a permission to read contacts.
     */
    public void requestReadContactsPermission()
    {
        //Request the permission
        Log.e("ReadContacts", "true");
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_CONTACTS},
                READ_CONTACTS_PERMISSION_REQUEST_CODE);
    }

    /**
     * Opens a window to ask for a permission to read call log.
     */
    public void requestReadCallLogPermission()
    {
        //Request the permission
        Log.e("ReadCallLog", "true");
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_CALL_LOG},
                READ_CALL_LOG_PERMISSION_REQUEST_CODE);
    }

    /**
     * Runs as a result of requesting for a grant a permissions.
     * Opens a {@link StartActivity} if all permissions are granted.
     *
     * @param requestCode code of the request, identify a request
     * @param permissions array of permissions
     * @param grantResults array of granted permissions
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode)
        {
            case READ_CONTACTS_PERMISSION_REQUEST_CODE:
                {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    // permission was granted, we can open a Main Activity
                    if (hasGrantedReadContactsPermission())
                    {
                        Intent contactsIntent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                        startActivityForResult(contactsIntent, ACTION_CONTACTS_CONTRACT_REQUEST_CODE);
                    }
                }
                else
                {
                    Toast.makeText(StartActivity.this, "Do dodawania numerów z listy kontaktów potrzebujemy Twojej zgody na ich odczyt.",
                            Toast.LENGTH_LONG).show();
                }
                break;
            }
            case READ_CALL_LOG_PERMISSION_REQUEST_CODE:
                {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    // permission was granted, we can open a Main Activity
                    if (hasGrantedReadCallLogPermission())
                    {
                        getPhoneFromCallLog();
                    }
                }
                else
                {
                    Toast.makeText(StartActivity.this, "Do dodawania numerów z rejestru połączeń potrzebujemy Twojej zgody na ich odczyt.",
                            Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }

    /**
     * Checks if the read contacts permission is granted.
     *
     * @return true if it is granted, false if it's are not
     */
    public boolean hasGrantedReadContactsPermission()
    {
        return ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_CONTACTS)
                == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Checks if the read call log permission is granted.
     *
     * @return true if it is granted, false if it's are not
     */
    public boolean hasGrantedReadCallLogPermission()
    {
        return ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_CALL_LOG)
                == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Runs on back from the Activity specified by requestCode.
     * Operates on the result of this Activity.
     *
     * @param requestCode unique request code of the Activity
     * @param resultCode result code of the Activity
     * @param data data returned by the Activity
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode)
        {
            case ACTION_CONTACTS_CONTRACT_REQUEST_CODE:
                {
                if (resultCode == Activity.RESULT_OK && hasGrantedReadContactsPermission())
                {
                    Uri contactData = data.getData();

                    if (contactData != null)
                    {
                        Cursor c = getContentResolver()
                                .query(contactData, null, null, null, null);

                        if (c != null)
                        {
                            if (c.moveToFirst())
                            {
                                String contactId = c.getString(c.getColumnIndex(ContactsContract.Contacts._ID));
                                String hasNumber = c.getString(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));

                                String nrBlocked;

                                if (Integer.valueOf(hasNumber) == 1)
                                {
                                    Cursor numbers = getContentResolver()
                                            .query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                                                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId,
                                                    null, null);

                                    if (numbers != null)
                                    {
                                        while (numbers.moveToNext())
                                        {

                                            nrBlocked = numbers.getString(numbers.getColumnIndex
                                                    (ContactsContract.CommonDataKinds.Phone.NUMBER));

                                            createRatingDialog(nrBlocked).show();
                                        }

                                        numbers.close();
                                    }
                                }
                            }
                            c.close();
                        }
                    }
                }
                break;
            }
        }
    }

    public void getPhoneFromCallLog()
    {
        String[] callLogFields = {
                android.provider.CallLog.Calls._ID,
                android.provider.CallLog.Calls.NUMBER,
                android.provider.CallLog.Calls.CACHED_NAME
        };

        //DESC order
        String viaOrder = android.provider.CallLog.Calls.DATE + " DESC";
        //filter out private/unknown numbers
        String WHERE = android.provider.CallLog.Calls.NUMBER + " >0";


        @SuppressLint("MissingPermission") final Cursor c = StartActivity.this.getContentResolver().query(
                android.provider.CallLog.Calls.CONTENT_URI, callLogFields,
                WHERE, null, viaOrder + " LIMIT 30");

        AlertDialog.Builder callLogDialog = new AlertDialog.Builder(
                StartActivity.this);

        android.content.DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialogInterface, int item)
            {
                if (c != null)
                {
                    c.moveToPosition(item);
                    createRatingDialog(c.getString(c.getColumnIndex(android.provider.CallLog.Calls.NUMBER))).show();
                    c.close();
                }
            }
        };

        callLogDialog.setCursor(c, listener, CallLog.Calls.NUMBER);
        callLogDialog.setTitle(R.string.start_activity_call_log_choose_dialog_title);
        callLogDialog.create().show();
    }

    /**
     * Creates a {@link AlertDialog} for choose rating for adding blocking.
     *
     * @param nrBlocked phone number to add to blocking list after dialog action
     * @return created {@link AlertDialog}
     */
    private Dialog createRatingDialog(final String nrBlocked)
    {
        final AlertDialog.Builder builder = new AlertDialog.Builder(StartActivity.this);
        builder.setTitle(R.string.start_activity_dialog_rating_title)
                .setItems(R.array.blocking_rating_options, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        //0 - positive, 1 - negative
                        boolean rating = which != 0;
                        addPhoneBlock(nrBlocked, rating);
                    }
                });


        return builder.create();
    }

    /**
     * Adds nrBlocked to the blocking list.
     *
     * @param nrBlocked phone number to add to blocking list
     * @param rating rating of added phone number, positive or negative
     */
    private void addPhoneBlock(String nrBlocked, boolean rating)
    {
        DatabaseHandler db = new DatabaseHandler(StartActivity.this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_NUMBERS)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) return;

        @SuppressLint("HardwareIds")
        Block newBlock = new Block(tm.getLine1Number(), nrBlocked,
                1, "", rating);


        if(!db.existBlock(newBlock))
        {
            db.addBlocking(newBlock);
            //ADD to bloicking list to make notify data changed possible for adapter
            Toast.makeText(StartActivity.this, "Numer dodany", Toast.LENGTH_SHORT).show();
            PhoneBlockFragment.blockings.add(newBlock);
        }
        else
        {
            Toast.makeText(StartActivity.this, "Numer jest już na liście", Toast.LENGTH_SHORT).show();
        }
    }
}
