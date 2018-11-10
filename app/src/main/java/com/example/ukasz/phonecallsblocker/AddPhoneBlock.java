package com.example.ukasz.phonecallsblocker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import com.example.ukasz.androidsqlite.Block;
import com.example.ukasz.androidsqlite.DatabaseHandler;
import com.example.ukasz.phonecallsblocker.validator.PhoneNumberValidator;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.util.List;
import java.util.Objects;

public class AddPhoneBlock extends AppCompatActivity implements AdapterView.OnItemSelectedListener
{
    private Toolbar mActionBar;
    private EditText nrBlocked;
    private Switch isPositiveSwitch;
    private Spinner category;
    private EditText description;
    private Button addButton;
    private String myPhoneNumber;
    private TelephonyManager tm;

    /**
     * Initializes var instances and view for start {@link AddPhoneBlock} activity.
     *
     * @param savedInstanceState Instance state
     */
    @Override
    @SuppressLint("HardwareIds")
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_phone_block);

        // TODO: Refactor: Consider keeping myPhoneNumber in external common place
        //getMyPhoneNumber
        tm = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_NUMBERS)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) return;
        myPhoneNumber = !tm.getLine1Number().equals("") ? tm.getLine1Number() : tm.getSubscriberId();
        myPhoneNumber = !myPhoneNumber.equals("") ? myPhoneNumber : tm.getSimSerialNumber();

        //set toolbar
        mActionBar = findViewById(R.id.add_phone_block_toolbar);
        setSupportActionBar(mActionBar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //nr info ---------------------------------------------------------------------------
        nrBlocked = findViewById(R.id.add_phone_block_nr_blocked_editText);
        isPositiveSwitch = findViewById(R.id.add_phone_block_is_positive_switch);
        description = findViewById(R.id.add_phone_block_descriptionEditText);

        //spinner --------------------------------------------------------------------------
        category = findViewById(R.id.add_phone_block_spinner);
        loadCategoriesToSpinner(category);
        category.setOnItemSelectedListener(this);

        //Switch view depends on blocking type (positive or negative)
        isPositiveSwitch.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (isPositiveSwitch.isChecked())
                {
                    category.setVisibility(View.GONE);
                }
                else
                {
                    category.setVisibility(View.VISIBLE);
                }
            }
        });

        addButton = findViewById(R.id.add_phone_block_addButton);
        addButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(final View v)
            {

                String countryCode = "+48";
                String phoneNumber = nrBlocked.getText().toString().trim();

                PhoneNumberValidator validator = new PhoneNumberValidator();

                if(countryCode.length() > 0 && phoneNumber.length() > 0)
                {
                    if(validator.isValidPhoneNumber(phoneNumber))
                    {
                        //Format phone number
                        String internationalFormat = validator.formatPhoneNuber(phoneNumber, countryCode,PhoneNumberUtil.PhoneNumberFormat.NATIONAL);

                        boolean status = validator.validateUsingLibphonenumber(countryCode, phoneNumber);
                        if(status)
                        {
                            //Good - add phone number
                            addPhoneBlock(internationalFormat);
                            finish();
                        }
                        else
                        {
                            Toast.makeText(v.getContext(),
                                    v.getContext().getText(R.string.add_phone_block_error_invalid) + ": " + internationalFormat,
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                    else
                    {
                        Toast.makeText(v.getContext(),
                                v.getContext().getText(R.string.add_phone_block_error_invalid) + ": " + phoneNumber,
                                Toast.LENGTH_LONG).show();
                    }
                }
                else
                {
                    Toast.makeText(v.getContext(),
                            v.getContext().getText(R.string.add_phone_block_error_empty),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
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
     * Catches the selected category as one of {@link MenuItem} item.
     *
     * @param item {@link MenuItem} item - selected category
     * @return This method applied to superclass with this item
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home)
        {
            finish(); // close this activity and return to previous activity (if there is any)
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
    {
//        CharSequence elem = (CharSequence) parent.getItemAtPosition(position);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent)
    {

    }

    /**
     * Adds phoneNumber to the blocking list.
     *
     * @param phoneNumber phone number to add to blocking list
     */
    private void addPhoneBlock(String phoneNumber)
    {
        // TODO: Refactor! If possible keep adding phone number method in one common place
        //Block data depends on isPositiveSwitch
        final Block newBlock = isPositiveSwitch.isChecked() ? new Block(myPhoneNumber, phoneNumber,
                0, description.getText().toString(), false)
                : new Block(myPhoneNumber, phoneNumber,
                category.getSelectedItemPosition(), description.getText().toString(), true);

        //LOCAL SECTION! add to local blockings
        DatabaseHandler db = new DatabaseHandler(getApplicationContext());
        if(!db.existBlock(newBlock))
        {
            db.addBlocking(newBlock);
            //ADD to blockings list to make notify data changed possible for adapter
            Toast.makeText(getApplicationContext(), R.string.add_phone_block_added, Toast.LENGTH_SHORT).show();
            PhoneBlockFragment.blockings.add(newBlock);
        }
        else
        {
            Toast.makeText(getApplicationContext(), R.string.add_phone_block_already_exist, Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(getApplicationContext(), R.string.error, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
