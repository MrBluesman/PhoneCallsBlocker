package com.example.ukasz.phonecallsblocker;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.ukasz.androidsqlite.Block;
import com.example.ukasz.androidsqlite.DatabaseHandler;
import com.example.ukasz.phonecallsblocker.list_helper.DividerItemDecoration;
import com.example.ukasz.phonecallsblocker.phone_number_helper.PhoneNumberHelper;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.i18n.phonenumbers.PhoneNumberUtil;

import java.util.Objects;

public class DetailsPhoneBlock extends AppCompatActivity
{
    private Toolbar mActionBar;

    //global reason category blickings list
    private int mColumnCount = 1;
    private MyDetailsRecyclerViewAdapter adapter;
    private RecyclerView recyclerView;
    private DatabaseReference databaseRef;
    private Query blockingsRef;
    FirebaseRecyclerOptions<Block> detailsBlockRecyclerOptions;
    DatabaseHandler db;

    private TextView phoneNumberTextView;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public DetailsPhoneBlock()
    {

    }

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

        //set up the DatabaseHandler
        db = new DatabaseHandler(getApplicationContext());

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

        //Set action bar title to formatted version
        //Get validator phone number lib to format
        PhoneNumberHelper phoneNumberHelper = new PhoneNumberHelper();

        String contactName = phoneNumberHelper.getContactName(getApplicationContext(), phoneNumber);
        String phoneNumberFormatted = phoneNumberHelper.formatPhoneNumber(phoneNumber, StartActivity.COUNTRY_CODE, PhoneNumberUtil.PhoneNumberFormat.NATIONAL);
        mActionBar.setTitle(contactName != null ? contactName + " (" + phoneNumberFormatted + ")" : phoneNumberFormatted);

        // ----------------------------------------------------------------------------------------
        View view = findViewById(R.id.details_phone_block_list);

        //Firebase realtime database references
        //TODO: initialize keep Synced when is really needed and remember to unSync it
        databaseRef = FirebaseDatabase.getInstance().getReference();
        blockingsRef = databaseRef.child("blockings").orderByChild("nrBlocked").equalTo(phoneNumber);
        blockingsRef.getRef().keepSynced(true);

        //Set the adapter
        if (view instanceof RecyclerView)
        {
            Context context = view.getContext();
            recyclerView = (RecyclerView) view;
            if (mColumnCount <= 1)
            {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            }
            else
            {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }

            detailsBlockRecyclerOptions = new FirebaseRecyclerOptions.Builder<Block>()
                    .setQuery(blockingsRef, Block.class)
                    .build();
            adapter = new MyDetailsRecyclerViewAdapter(detailsBlockRecyclerOptions, context, db);

            recyclerView.setItemAnimator(new DefaultItemAnimator());
            recyclerView.addItemDecoration(new DividerItemDecoration(context, LinearLayoutManager.VERTICAL));
            recyclerView.setAdapter(adapter);

        }
    }

    /**
     * Runs on resume application.
     * Notify data set changed.
     */
    @Override
    public void onResume()
    {
        super.onResume();
    }

    /**
     * Runs on start of {@link DetailsPhoneBlock}.
     * Starts listening the adapter.
     */
    @Override
    public void onStart()
    {
        super.onStart();
        adapter.startListening();
    }

    /**
     * Runs on stop of {@link DetailsPhoneBlock}.
     * Stops listening the adapter.
     */
    @Override
    public void onStop()
    {
        super.onStop();
        adapter.stopListening();
    }

    /**
     * Catch the arrow back action as one of {@link MenuItem} item.
     *
     * @param item {@link MenuItem} selected menu item
     * @return This method applied to superclass with this item
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
