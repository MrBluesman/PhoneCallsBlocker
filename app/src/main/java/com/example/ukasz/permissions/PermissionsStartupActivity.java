package com.example.ukasz.permissions;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ukasz.phonecallsblocker.MainActivity;
import com.example.ukasz.phonecallsblocker.R;

import java.security.Permission;

/**
 * Class for grant permissions for the first run of app or when
 * user will take away the permissions from Apps settings
 */
public class PermissionsStartupActivity extends AppCompatActivity
{

    //Clickable buttons
    private Button grantPermissionsButton;
    private CheckBox phoneStateCheckBoxPerm;
    private CheckBox allowWindowsCheckBoxPerm;
    //Hidden information
    private ImageView phoneStateWarningImage;
    private TextView phoneStateWarningText;
    private ImageView allowWindowsWarningImage;
    private TextView allowWindowsWarningText;
    //const Permissions Codes
    final private static int ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE = 5555;
    final private static int READ_PHONE_STATE_PERMISSION_REQUEST_CODE = 5556;

    /**
     * Method which runs on activity start and contains listener for button and checkbocks,
     * which are responsible for granting a permissions
     * Without permissions user can't create a Phone Calls Blocking Activity
     * (phonecallsblocker.MainActivity)
     * @param savedInstanceState saved app instances state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        //View.VISIBLE || View.GONE
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permissions_startup);

        //If permissions is granted we can open a MainActivity
        if(hasGrantedPermissions())
        {
            openMainActivity();
        }
        else //If it's not we have to request for permissions
        {
            phoneStateCheckBoxPerm = findViewById(R.id.permissions_startup_activity_phone_checkbox);
            allowWindowsCheckBoxPerm = findViewById(R.id.permissions_startup_activity_windows_checkbox);

            grantPermissionsButton = findViewById(R.id.permissions_startup_activity_accept_button);
            grantPermissionsButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if(phoneStateCheckBoxPerm.isChecked())
                    {
                        requestReadPhoneStatePermission();
                    }
                    else
                    {
                        ImageView phoneStateWarningImage = findViewById(R.id.imageView4);
                        TextView phoneStateWarningText = findViewById(R.id.permissions_startup_activity_phone_checkbox_warning);
                        phoneStateWarningImage.setVisibility(View.VISIBLE);
                        phoneStateWarningText.setVisibility(View.VISIBLE);
                    }
                }
            });
        }

    }

    /**
     * Opens a windows to ask for a permissions to read a phone state
     */
    public void requestReadPhoneStatePermission()
    {
        //Request the permission
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_PHONE_STATE},
                READ_PHONE_STATE_PERMISSION_REQUEST_CODE);
    }

    /**
     * Checks if the read phone state permissions is granted
     * @return True if it's granted, false if it's not
     */
    public boolean hasGrantedPermissions()
    {
        return ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_PHONE_STATE)
                == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Opens a new activity with NO_HISTORY flag and destroys this startup activity
     */
    public void openMainActivity()
    {
        Intent i = new Intent(new Intent(getApplicationContext(), MainActivity.class));
        i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY); // Adds the FLAG_ACTIVITY_NO_HISTORY flag
//            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }

    /**
     * Runs as a result of requesting for a grant a permissions
     * @param requestCode code of the request, identify a request
     * @param permissions array of permissions
     * @param grantResults array of granted permissions
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults)
    {
        switch (requestCode)
        {
            case READ_PHONE_STATE_PERMISSION_REQUEST_CODE:
                {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    // permission was granted, we can open a Main Activity
                    Log.e("Permission", "Granted!");
                    openMainActivity();
                }
                else
                {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Log.e("Permission", "NOT Granted!");
                    ImageView phoneStateWarningImage = findViewById(R.id.imageView4);
                    TextView phoneStateWarningText = findViewById(R.id.permissions_startup_activity_phone_checkbox_warning);
                    phoneStateWarningImage.setVisibility(View.VISIBLE);
                    phoneStateWarningText.setVisibility(View.VISIBLE);
                }
            }
        }
    }
}
