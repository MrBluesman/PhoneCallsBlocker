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
    private static boolean detectEnabled;
    private Switch blockServiceSwitch;
    private static boolean autoBlockEnabled;
    private Switch autoBlockSwitch;
    //textView TESTOWY
    private TextView textViewTestowy;
    public SharedPreferences data;

    /**
     * Method which runs on activity start and contains listener for switch,
     * which enable or disable phone calls catching
     * @param savedInstanceState saved app instances state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Log.e("test","MainActivity - onStart() method");
        setContentView(R.layout.activity_main);

        //Check permission for alerts over window (MANAGE OVERLAY PERMISSION)
//        checkManageOverlayPermission();

        //Switcher to enable/disable blocking
        blockServiceSwitch = findViewById(R.id.switch1_block_service);
        autoBlockSwitch = findViewById(R.id.switch2_automatic_block);
        //Tests text Views
        textViewTestowy = findViewById(R.id.textView);

        //load data settings (saved in shared preferences)
        loadSettingsState();

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


        //DATABASE IMPLEMENTATION TESTING ---------------------------------------------------------
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

        //----------------------------------------------------------------------------------------
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

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                == PackageManager.PERMISSION_GRANTED)
        {
            Log.e("CZY TAK?", "OH YEAH");
        }

        Intent intent = new Intent(this, CallDetectService.class);
        if (enable)
        {
            Log.e("test","MainActivity - START CallDetectService [method call]");
            startService(intent);
        }
        else
        {
            Log.e("test","MainActivity - STOP CallDetectService [method call]");
            stopService(intent);
        }
    }
}
