package com.example.ukasz.phonecallsblocker;

import com.example.ukasz.androidsqlite.Block;
import com.example.ukasz.androidsqlite.DatabaseHandler;


import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Łukasz Parysek on 2017-03-05.
 * Basic activity off the application
 */
public class MainActivity extends AppCompatActivity
{
    /**
     * MainActivity local variables
     */
    private boolean detectEnabled;
    private Switch blockServiceSwitch;
    private boolean autoBlockEnabled;
    private Switch autoBlockSwitch;
    //textView TESTOWY
    private TextView textViewTestowy;
    public SharedPreferences data;
    //const Permissions
    final private static int ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE = 5555;
    final private static int READ_PHONE_STATE_PERMISSION_REQUEST_CODE = 5556;

    /**
     * Method which runs on activity start and contains listener for switch,
     * which enable or disable phone calls catching
     * @param savedInstanceState saved app instance state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Log.e("test","MainActivity - onStart() method");
        setContentView(R.layout.activity_main);

        //checkPermission for alerts over window (MANAGE OVERLAY PERMISSION)
        checkManageOverlayPermission();

        //Switch to enable/disable blocking
        blockServiceSwitch = findViewById(R.id.switch1_block_service);
        autoBlockSwitch = findViewById(R.id.switch2_automatic_block);
        //Testowy text Views
        textViewTestowy = findViewById(R.id.textView);

        //load data settings
        loadSettingsState();

        Intent intent = new Intent(this, CallDetectService.class);
        stopService(intent);
        setDetectEnabled(detectEnabled);

        //Click listener for enable or disable phone calls catching
        blockServiceSwitch.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                setDetectEnabled(!detectEnabled);

                blockServiceSwitch.setChecked(detectEnabled);
                textViewTestowy.setText(Boolean.toString(detectEnabled));

                //Save setting in SharedPreferences
                data = getSharedPreferences("data", Context.MODE_PRIVATE);
                SharedPreferences.Editor editDataSettings = data.edit();
                editDataSettings.putBoolean("detectEnabled", detectEnabled);
                editDataSettings.apply(); //commit
            }
        });

        //Click listener for enable or disable phone calls catching
        autoBlockSwitch.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                autoBlockSwitch.setChecked(!autoBlockEnabled);
                autoBlockEnabled = !autoBlockEnabled;
                //textViewTestowy.setText(Boolean.toString(detectEnabled));

                //Save setting in SharedPreferences
                data = getSharedPreferences("data", Context.MODE_PRIVATE);
                SharedPreferences.Editor editDataSettings = data.edit();
                editDataSettings.putBoolean("autoBlockEnabled", autoBlockEnabled);
                editDataSettings.apply(); //commit
            }
        });


        //DATABASE IMPLEMENTATION TESTING
        DatabaseHandler db = new DatabaseHandler(this);
        //Insertings blocks
        Log.d("Insert: ", "Inserting..");
        //db.addBlocking(new Block("721315333", "665693959", 0, "a", true));
        //db.addBlocking(new Block("721315345", "665693959", 0, "a", true));
        //db.deleteBlocking(new Block("721315778", "665693959", 0, "a", true));
        //db.deleteBlocking(new Block("+48721315778", "665693959", 0, "a", true));

        //Reading all blocks
        Log.d("Read: ", "Reading..");
        List<Block> blockings = db.getAllBlockings();
        TextView textViewTestowy2 = findViewById(R.id.textView2);
        String caly = "";

        for(Block b: blockings)
        {
            String log = "Blokujący: " + b.getNrDeclarant() + ", Blokowany: " + b.getNrBlocked() +
                    ", Kategoria: " + b.getReasonCategory() + ", Opis: " + b.getReasonDescription()
                    + ", czyNegatywny: " + b.getNrRating();

            Log.d("Name: ", log);
            caly+= log + "\n";
            textViewTestowy2.setText(caly);
        }

        //db.updateCategories();
//
//        List<String> categories = db.getAllCategories();
//        for(String c: categories)
//        {
//            caly+= c + "\n";
//            textViewTestowy2.setText(caly);
//        }
    }

    /**
     * Method which runs on resume activity
     */
    @Override
    protected void onResume()
    {
        super.onResume();
        Log.e("test","MainActivity - onResume() method");
        //loadSettingsState();
    }

    /**
     * Method which runs on activity restart
     */
    @Override
    protected void onRestart()
    {
        super.onRestart();
        Log.e("test","MainActivity - onRestart() method");
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
        blockServiceSwitch.setChecked(detectEnabled);
        autoBlockSwitch.setChecked(autoBlockEnabled);
        textViewTestowy.setText(Boolean.toString(detectEnabled));
        //set or unset detecting service by this method;
    }

    /**
     * This method runs after activity destroy
     * Stop or restart detecting service depends on detectEnabled
     */
    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        Log.e("test","MainActivity - onDestroy() method");
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
    private void setDetectEnabled(boolean enable)
    {
        detectEnabled = enable;
        Log.e("setDetectEnabled", "method enabled");

        checkReadPhoneStatePermission();

        Intent intent = new Intent(this, CallDetectService.class);
        if (enable)
        {
            Log.e("test","MainActivity - START CallDetectService [method call]");
            //restart service
            //stopService(intent);
            startService(intent);
        }
        else
        {
            Log.e("test","MainActivity - STOP CallDetectService [method call]");
            stopService(intent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults)
    {
        switch (requestCode) {
            case READ_PHONE_STATE_PERMISSION_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                }
                else
                {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
            }
        }
    }

    public void checkReadPhoneStatePermission()
    {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED)
        {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_PHONE_STATE))
            {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            }
            else
            {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_PHONE_STATE},
                        READ_PHONE_STATE_PERMISSION_REQUEST_CODE);
            }
        }
    }
    public void checkManageOverlayPermission()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            if (!Settings.canDrawOverlays(this))
            {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE);
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE)
        {
            if (!Settings.canDrawOverlays(this))
            {
                // You don't have permission
                checkManageOverlayPermission();

            }
        }
    }

}
