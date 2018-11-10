package com.example.ukasz.phonecallsblocker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
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
import android.os.Build;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;

import android.widget.Toast;

import com.example.ukasz.androidsqlite.Block;
import com.example.ukasz.androidsqlite.DatabaseHandler;
import com.example.ukasz.permissions.PermissionsStartupActivity;
import com.example.ukasz.phonecallsblocker.tab_layout_helper.CustomViewPager;
import com.example.ukasz.phonecallsblocker.phone_number_helper.PhoneNumberHelper;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.i18n.phonenumbers.PhoneNumberUtil;

import java.util.ArrayList;
import java.util.List;

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
    private CustomViewPager mViewPager;
    private TabLayout tabLayout;

    //The {@link SharedPreferences} where are saved a app settings.
    private SharedPreferences data;

    //The {@link TelephonyManager} for fetch user phone number
    private TelephonyManager tm;

    //phone owner number
    private String myPhoneNumber;

    //Blocking options
    private boolean detectEnabled;
    private boolean autoBlockEnabled;
    private boolean foreignBlockEnabled;
    private boolean privateBlockEnabled;
    private boolean unknownBlockEnabled;

    //SYnc options
    private boolean syncEnabled;

    //Notifications options
    private boolean notificationBlockEnabled;
    private boolean notificationAllowEnabled;

    //The {@link com.github.clans.fab.FloatingActionMenu} instance.
    com.github.clans.fab.FloatingActionMenu fab;

    //request unique codes
    private final int ACTION_CONTACTS_CONTRACT_REQUEST_CODE = 1111;
    private final int READ_CONTACTS_PERMISSION_REQUEST_CODE = 1112;
    private final int READ_CALL_LOG_PERMISSION_REQUEST_CODE = 1113;

    //job unique ids for job scheduler
    private static final int CALL_DETECT_JOB_ID = 2000;

    //static country code
    final static String COUNTRY_CODE = "+48";

    /**
     * Method which runs on activity start.
     *
     * @param savedInstanceState saved instance of Activity state
     */
    @SuppressLint("HardwareIds")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        Toolbar toolbar = findViewById(R.id.start_activity_toolbar);
        setSupportActionBar(toolbar);

        //Create the adapter that will return a fragment for each of the three
        //primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // TODO: Refactor: Consider keeping myPhoneNumber in external common place
        //getMyPhoneNumber
        tm = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_NUMBERS)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) return;
        myPhoneNumber = !tm.getLine1Number().equals("") ? tm.getLine1Number() : tm.getSubscriberId();
        myPhoneNumber = !myPhoneNumber.equals("") ? myPhoneNumber : tm.getSimSerialNumber();

        //Apply first run actions
        applyFirstRunActions();

        //load data settings (saved in shared preferences)
        loadSettingsState();

        //Set up the ViewPager with the sections adapter.
        mViewPager = findViewById(R.id.start_activity_container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        //Set telephony manager for fetch user phone number
        tm = (TelephonyManager) StartActivity.this.getSystemService(Context.TELEPHONY_SERVICE);

        //Set up the listeners
        tabLayout = findViewById(R.id.start_activity_tabs);
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager)
        {
            //floating action menu available depends on selected tabs
            @Override
            public void onTabSelected(TabLayout.Tab tab)
            {
                super.onTabSelected(tab);
                int position = tab.getPosition();
                switch(position)
                {
                    case 0:
                    {
                        getFab().showMenu(true);
                        break;
                    }
                    case 1:
                    {
                        getFab().showMenu(true);
                        break;
                    }
                    case 2:
                    {
                        getFab().hideMenu(true);
                        break;
                    }
                    default:
                    {
                        getFab().showMenu(true);
                    }
                }
            }
        });

        //Floating Action Button Menu
        fab = findViewById(R.id.start_activity_fab);

        //Add manualy click listener
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

        //Add from contacts listener
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

        //Add from registry listener
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
    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onResume()
    {
        super.onResume();

        //Check if app has permissions to run
        checkPermissions();

        if (fab != null)
        {
            if(fab.isOpened()) fab.toggle(true);
        }
        Log.e("StartActivity", "onResume() method");
    }

    /**
     * Method which runs on activity restart.
     */
    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onRestart()
    {
        super.onRestart();

        //Check if app has permissions to run
        checkPermissions();

        if(fab != null)
        {
            if (fab.isOpened()) fab.toggle(true);
        }
        Log.e("StartActivity", "onRestart() method");
    }

    /**
     * Checks if this {@link StartActivity} has permissions to work.
     * If hasn't - run {@link PermissionsStartupActivity} to manage missing permissions.
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkPermissions()
    {
        PermissionsStartupActivity startupActivity = new PermissionsStartupActivity();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
        {
            if (!startupActivity.hasGrantedPermissions(this))
            {
                openPermissionsActivity();
            }
        }
        else //Android SDK >= M
        {
            if (!startupActivity.hasGrantedPermissions(this) || !startupActivity.hasGrantedManageOverlayPermission(this))
            {
                openPermissionsActivity();
            }
        }
    }

    /**
     * Opens a {@link PermissionsStartupActivity}.
     */
    private void openPermissionsActivity()
    {
        Intent i = new Intent(new Intent(getApplicationContext(), PermissionsStartupActivity.class));
        startActivity(i);
        finish();
    }

    /**
     * This method loads a settings data from Shared Preferences
     * And set a correct state of service by call {setDetectEnabled(bool enable)} method
     */
    private void loadSettingsState()
    {
        //load data settings:
        //blocking settings
        data = getSharedPreferences("data", Context.MODE_PRIVATE);
        detectEnabled = data.getBoolean("detectEnabled", false);
        autoBlockEnabled = data.getBoolean("autoBlockEnabled", false);
        foreignBlockEnabled = data.getBoolean("foreignBlockEnabled", false);
        privateBlockEnabled = data.getBoolean("privateBlockEnabled", false);
        unknownBlockEnabled = data.getBoolean("unknownBlockEnabled", false);

        //sync settings
        syncEnabled = data.getBoolean("syncEnabled", false);

        //notification settings
        notificationBlockEnabled = data.getBoolean("notificationBlockEnabled", false);
        notificationAllowEnabled = data.getBoolean("notificationAllowEnabled", false);

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

        JobInfo jobCallDetectInfo = new JobInfo.Builder(CALL_DETECT_JOB_ID, componentName)
                .setPersisted(true)
                .setOverrideDeadline(0)
                .build();

        //Schedule detecting only Job Scheduler object is set up and job is not scheduled
        if (jobScheduler != null && !isJobScheduled(getApplicationContext(), CALL_DETECT_JOB_ID)) jobScheduler.schedule(jobCallDetectInfo);
    }


    /**
     * Check whether selected job has been already scheduled.
     *
     * @param context context of the app
     * @param jobId job unique id
     * @return true if has been scheduled, false if hasn't
     */
    public static boolean isJobScheduled(Context context, int jobId)
    {
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE) ;

        boolean hasBeenScheduled = false;

        if (jobScheduler != null)
        {
            for (JobInfo jobInfo : jobScheduler.getAllPendingJobs())
            {
                if (jobInfo.getId() == jobId)
                {
                    hasBeenScheduled = true ;
                    break ;
                }
            }
        }

        return hasBeenScheduled ;
    }

    /**
     * Cancel scheduled calls detecting service.
     */
    private void cancelDetectingJob()
    {
        JobScheduler jobScheduler = (JobScheduler) getApplicationContext()
                .getSystemService(JOB_SCHEDULER_SERVICE);

        //Cancel only Job Scheduler object is set up
        if (jobScheduler != null) jobScheduler.cancel(CALL_DETECT_JOB_ID);
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
     * Actions on fragment interaction.
     *
     * @param uri UriactionBarMenu
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

    /**
     * foreignBlockEnabled getter.
     *
     * @return value of foreignBlockEnabled
     */
    public boolean getForeignBlockEnabled()
    {
        return foreignBlockEnabled;
    }

    /**
     * privateBlockEnabled getter.
     *
     * @return value of privateBlockEnabled
     */
    public boolean getPrivateBlockEnabled()
    {
        return privateBlockEnabled;
    }

    /**
     * unknownBlockEnabled getter.
     *
     * @return value of unknownBlockEnabled
     */
    public boolean getUnknownBlockEnabled()
    {
        return unknownBlockEnabled;
    }

    /**
     * syncEnabled getter.
     *
     * @return value of syncEnabled;
     */
    public boolean getSyncEnabled()
    {
        return syncEnabled;
    }
    /**
     * notificationBlockEnabled getter.
     *
     * @return value of notificationBlockEnabled
     */
    public boolean getNotificationBlockEnabled()
    {
        return notificationBlockEnabled;
    }

    /**
     * notificationAllowEnabled getter.
     *
     * @return value of notificationAllowEnabled
     */
    public boolean getNotificationAllowEnabled()
    {
        return notificationAllowEnabled;
    }

    /**
     * mViewPager getter.
     *
     * @return value of mViewPager
     */
    public CustomViewPager getMViewPager()
    {
        return mViewPager;
    }

    /**
     * tabLayout getter.
     *
     * @return value of tabLayout
     */
    public TabLayout getTabLayout()
    {
        return tabLayout;
    }

    /**
     * fab action menu getter.
     *
     * @return value of fab
     */
    public com.github.clans.fab.FloatingActionMenu getFab()
    {
        return fab;
    }

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
                                        List<String> usedNumbers = new ArrayList<>();

                                        while (numbers.moveToNext())
                                        {

                                            nrBlocked = numbers.getString(numbers.getColumnIndex
                                                    (ContactsContract.CommonDataKinds.Phone.NUMBER));

                                            if(!usedNumbers.contains(nrBlocked))
                                            {
                                                usedNumbers.add(nrBlocked);
                                                Dialog ratingDialog = createRatingDialog(nrBlocked);
                                                if(ratingDialog != null) ratingDialog.show();
                                            }
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

                    String phoneNumber = c.getString(c.getColumnIndex(android.provider.CallLog.Calls.NUMBER));
                    Dialog ratingDialog = createRatingDialog(phoneNumber);

                    if(ratingDialog!= null) ratingDialog.show();
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
        PhoneNumberHelper validator = new PhoneNumberHelper();

        if(COUNTRY_CODE.length() > 0 && nrBlocked.length() > 0)
        {
            if(validator.isValidPhoneNumber(nrBlocked))
            {
                //Format phone number
                final String internationalFormat = validator.formatPhoneNumber(nrBlocked, COUNTRY_CODE,PhoneNumberUtil.PhoneNumberFormat.E164);

                boolean status = validator.validateUsingLibphonenumber(COUNTRY_CODE, nrBlocked);
                if(status)
                {
                    //Good - create rating dialog
                    builder.setTitle(nrBlocked)
                            .setItems(R.array.blocking_rating_options, new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialog, int which)
                                {
                                    //0 - positive, 1 - negative
                                    boolean rating = which != 0;
                                    addPhoneBlock(internationalFormat, rating);
                                }
                            });
                }
                else
                {
                    Toast.makeText(StartActivity.this,
                            StartActivity.this.getText(R.string.add_phone_block_error_invalid) + ": " + internationalFormat,
                            Toast.LENGTH_LONG).show();
                    return null;
                }
            }
            else
            {
                Toast.makeText(StartActivity.this,
                        StartActivity.this.getText(R.string.add_phone_block_error_invalid) + ": " + nrBlocked,
                        Toast.LENGTH_LONG).show();
                return null;
            }
        }
        else
        {
            Toast.makeText(StartActivity.this,
                    StartActivity.this.getText(R.string.add_phone_block_error_empty),
                    Toast.LENGTH_SHORT).show();
            return null;
        }

        return builder.create();
    }

    /**
     * TODO: Refactor! If possible keep adding phone number method in one common place
     * Adds nrBlocked to the blocking list.
     *
     * @param nrBlocked phone number to add to blocking list
     * @param rating rating of added phone number, positive or negative
     */
    private void addPhoneBlock(String nrBlocked, boolean rating)
    {
        DatabaseHandler db = new DatabaseHandler(StartActivity.this);

        final Block newBlock = new Block(myPhoneNumber, nrBlocked,
                1, "", rating);

        //LOCAL SECTION! add to local blockings
        if(!db.existBlock(newBlock))
        {
            db.addBlocking(newBlock);
            //ADD to blocking list to make notify data changed possible for adapter
            Toast.makeText(StartActivity.this, R.string.add_phone_block_added, Toast.LENGTH_SHORT).show();
            //TODO: Refresh adapter after add
            PhoneBlockFragment.blockings.add(newBlock);
        }
        else
        {
            Toast.makeText(StartActivity.this, R.string.add_phone_block_already_exist, Toast.LENGTH_SHORT).show();
        }

        //GLOBAL SECTION! add to global blockings if sync is enabled
        boolean syncEnabled =  getSharedPreferences("data", Context.MODE_PRIVATE)
                .getBoolean("syncEnabled", false);

        if(syncEnabled)
        {
            final DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference();
            Query newBlockingRef = databaseRef
                    .child("blockings")
                    .orderByChild("nrDeclarantBlocked")
                    .equalTo(newBlock.getNrDeclarant() + "_" + newBlock.getNrBlocked())
                    .limitToFirst(1);

            newBlockingRef.addListenerForSingleValueEvent(new ValueEventListener()
            {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                {
                    if(!dataSnapshot.exists())
                    {
                        Log.e("TEST_ISTNIEJE", "NIE");
                        databaseRef.child("blockings").push().setValue(newBlock);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError)
                {
                    Toast.makeText(StartActivity.this, R.string.error, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    /**
     * Runs actions only after first run of the application.
     */
    private void applyFirstRunActions()
    {
        //First run actions
        SharedPreferences sharedPreferences = this.getSharedPreferences("data", Context.MODE_PRIVATE);
        // Check if we need perform first run actions
        if (!sharedPreferences.getBoolean("firstRun", false))
        {
            loadDefaultSettings();
            loadBlockingsFromFirebase();

            SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
            //Disable first run flag
            sharedPreferencesEditor.putBoolean("firstRun", true);
            sharedPreferencesEditor.apply();
        }
    }

    /**
     * Loads default settings state.
     */
    private void loadDefaultSettings()
    {
        SharedPreferences sharedPreferences = this.getSharedPreferences("data", Context.MODE_PRIVATE);
        SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();

        //Default settings
        sharedPreferencesEditor.putBoolean("detectEnabled", true);
        sharedPreferencesEditor.putBoolean("autoBlockEnabled", true);
        sharedPreferencesEditor.putBoolean("syncEnabled", true);
        sharedPreferencesEditor.putBoolean("notificationBlockEnabled", true);

        sharedPreferencesEditor.apply();
    }

    /**
     * Loads blockings from {@link FirebaseDatabase} to local database.
     */
    private void loadBlockingsFromFirebase()
    {
        //Fetch blockings from firebase
        Query myBlockings = FirebaseDatabase.getInstance().getReference()
                .child("blockings")
                .orderByChild("nrDeclarant")
                .equalTo(myPhoneNumber);

        myBlockings.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                for (DataSnapshot blockSnapshot : dataSnapshot.getChildren())
                {
                    Block newBlock = blockSnapshot.getValue(Block.class);
                    DatabaseHandler db = new DatabaseHandler(getApplicationContext());
                    if(newBlock != null && !db.existBlock(newBlock))
                    {
                        db.addBlocking(newBlock);
                        //ADD to blockings list to make notify data changed possible for adapter
                        PhoneBlockFragment.blockings.add(newBlock);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {
                Toast.makeText(getApplicationContext(), R.string.error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
