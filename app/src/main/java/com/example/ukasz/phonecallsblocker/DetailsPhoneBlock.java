package com.example.ukasz.phonecallsblocker;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.Objects;

public class DetailsPhoneBlock extends AppCompatActivity
{
    private Toolbar mActionBar;
    private TextView phoneNumberTextView;

    /**
     * Initialize var instances and view for start {@link DetailsPhoneBlock} activity.
     *
     * @param savedInstanceState Instance state.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details_phone_block);

        //set toolbar
        mActionBar = findViewById(R.id.details_phone_block_toolbar);
        setSupportActionBar(mActionBar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //Get extras param from Intent (phone number)
        Bundle b = getIntent().getExtras();
        String phoneNumber = "";
        if(b != null) phoneNumber = b.getString("phoneNumber");
        phoneNumberTextView = findViewById(R.id.details_phone_block_phone_number);
        phoneNumberTextView.setText(phoneNumber);
    }

    /**
     * Catch the arrow back action as one of {@link MenuItem} item.
     *
     * @param item {@link MenuItem} item - selected category.
     * @return This method applied to superclass with this item.
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
}
