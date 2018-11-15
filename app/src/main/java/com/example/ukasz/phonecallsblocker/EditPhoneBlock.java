package com.example.ukasz.phonecallsblocker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.example.ukasz.androidsqlite.Block;
import com.example.ukasz.androidsqlite.DatabaseHandler;

import java.util.List;
import java.util.Objects;

public class EditPhoneBlock extends AppCompatActivity implements AdapterView.OnItemSelectedListener
{
    private Toolbar mActionBar;
    private TextView nrBlocked;
    private Switch isPositiveSwitch;
    private Spinner category;
    private EditText description;
    private Button editButton;
    private String myPhoneNumber;
    private TelephonyManager tm;

    //Editing blocking
    Block block;

    //Database handler
    private DatabaseHandler db;


    /**
     * Initialize var instances and view for start {@link EditPhoneBlock} activity.
     *
     * @param savedInstanceState instance state
     */
    @Override
    @SuppressLint("HardwareIds")
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_phone_block);

        db = new DatabaseHandler(getApplicationContext());

        // TODO: Refactor: Consider keeping myPhoneNumber in external common place
        //getMyPhoneNumber
        tm = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_NUMBERS)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) return;
        myPhoneNumber = !tm.getLine1Number().equals("") ? tm.getLine1Number() : tm.getSubscriberId();
        myPhoneNumber = !myPhoneNumber.equals("") ? myPhoneNumber : tm.getSimSerialNumber();

        //set toolbar
        mActionBar = findViewById(R.id.edit_phone_block_toolbar);
        setSupportActionBar(mActionBar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //nr info ---------------------------------------------------------------------------
        nrBlocked = findViewById(R.id.edit_phone_block_nr_blocked_textView);
        isPositiveSwitch = findViewById(R.id.edit_phone_block_is_positive_switch);
        description = findViewById(R.id.edit_phone_block_descriptionEditText);

        //spinner --------------------------------------------------------------------------
        category = findViewById(R.id.edit_phone_block_spinner);
        loadCategoriesToSpinner(category);
        category.setOnItemSelectedListener(this);

        //set the fields as number info
        Bundle b = getIntent().getExtras();
        String phoneNumber = "";
        if(b != null) phoneNumber = b.getString("phoneNumber");
        block = getBlock(phoneNumber);

        nrBlocked.setText(block.getNrBlocked());
        isPositiveSwitch.setChecked(!block.getNrRating());
        description.setText(block.getReasonDescription());
        category.setSelection(block.getReasonCategory());
    }

    /**
     * Loads categories from database to spinner.
     *
     * @param spinner spinner which will have a set adapter with loaded categories
     */
    public void loadCategoriesToSpinner(Spinner spinner)
    {
        DatabaseHandler db = new DatabaseHandler(this);
        List<String> categories = db.getAllCategories();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    /**
     * Gets {@link Block} from local database by passed blocked phone number.
     *
     * @param phoneNumber blocked number
     * @return {@link Block} if exist blocked number number
     */
    private Block getBlock(String phoneNumber)
    {
        return db.getBlocking(myPhoneNumber, phoneNumber);
    }

    /**
     * Catch the arrow back action as one of {@link MenuItem} item.
     *
     * @param item {@link MenuItem} selected menu item
     * @return this method applied to superclass with this item
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home)
        {
            onBackPressed(); // close this activity and return to previous activity (if there is any)
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
    {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent)
    {

    }
}
