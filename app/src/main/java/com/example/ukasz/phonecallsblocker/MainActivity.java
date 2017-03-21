package com.example.ukasz.phonecallsblocker;

import com.example.ukasz.androidsqlite.Block;
import com.example.ukasz.androidsqlite.DatabaseHandler;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
    //textView TESTOWY
    private TextView textViewTestowy;
    public SharedPreferences data;

    /**
     * Method which runs on activity start and contains listener for switch,
     * which enable or disable phone calls catching
     * @param savedInstanceState saved app instance state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Switch to enable/disable blocking
        blockServiceSwitch = (Switch) findViewById(R.id.switch1_block_service);
        //Testowy text Views
        textViewTestowy = (TextView) findViewById(R.id.textView);

        //load data settings
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
                editDataSettings.commit();
            }
        });


        //DATABASE IMPLEMENTATION TESTING
        DatabaseHandler db = new DatabaseHandler(this);
        //Insertings blocks
        Log.d("Insert: ", "Inserting..");
        //db.addBlocking(new Block("123456789", "234567890", 0, "a", true));
        //db.addBlocking(new Block("123456789", "534567890", 1, "bb", false));

        //Reading all blocks
        Log.d("Read: ", "Reading..");
        List<Block> blockings = db.getAllBlockings();
        TextView textViewTestowy2 = (TextView) findViewById(R.id.textView2);
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
        //Log.e("test","MainActivity - onResume() method");
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
        loadSettingsState();
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
        blockServiceSwitch.setChecked(detectEnabled);
        textViewTestowy.setText(Boolean.toString(detectEnabled));
        //set or unset detecting service by this method
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

        Intent intent = new Intent(this, CallDetectService.class);
        if (enable)
        {
            Log.e("test","MainActivity - START CallDetectService [method call]");
            //restart service
            stopService(intent);
            startService(intent);
        }
        else
        {
            Log.e("test","MainActivity - STOP CallDetectService [method call]");
            stopService(intent);
        }
    }
}
