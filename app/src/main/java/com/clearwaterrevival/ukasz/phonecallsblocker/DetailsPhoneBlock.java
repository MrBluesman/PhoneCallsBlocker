package com.clearwaterrevival.ukasz.phonecallsblocker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.clearwaterrevival.ukasz.androidsqlite.Block;
import com.clearwaterrevival.ukasz.androidsqlite.DatabaseHandler;
import com.clearwaterrevival.ukasz.phonecallsblocker.list_helper.DividerItemDecoration;
import com.clearwaterrevival.ukasz.phonecallsblocker.phone_number_helper.PhoneNumberHelper;
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
    private String myPhoneNumber;
    private TelephonyManager tm;
    DatabaseHandler db;

    //Details blocking
    Block block;

    //phone number labels
    private TextView phoneNumberTextView;
    private TextView phoneNumberTextView2;

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
    @SuppressLint("HardwareIds")
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details_phone_block);

        //set up the DatabaseHandler
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
        mActionBar = findViewById(R.id.details_phone_block_toolbar);
        setSupportActionBar(mActionBar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //nr info ---------------------------------------------------------------------------
        phoneNumberTextView = findViewById(R.id.details_phone_block_phone_number);
        phoneNumberTextView2 = findViewById(R.id.details_phone_block_phone_number2);

        //Set action bar title to formatted version
        //set the fields as number info --------------------------------------------------------------------------
        Bundle b = getIntent().getExtras();
        String phoneNumber = "";
        if(b != null) phoneNumber = b.getString("phoneNumber");
        block = getBlock(phoneNumber);

        //Get validator phone number lib to format
        PhoneNumberHelper phoneNumberHelper = new PhoneNumberHelper();

        if(block != null)
        {
            String contactName = phoneNumberHelper.getContactName(getApplicationContext(), block.getNrBlocked());
            String phoneNumberFormatted = phoneNumberHelper.formatPhoneNumber(block.getNrBlocked(), StartActivity.COUNTRY_CODE, PhoneNumberUtil.PhoneNumberFormat.NATIONAL);

            if(contactName != null)
            {
                phoneNumberTextView.setText(contactName);
                phoneNumberTextView2.setText(phoneNumberFormatted);
            }
            else
            {
                phoneNumberTextView.setText(phoneNumberFormatted);
                phoneNumberTextView2.setVisibility(View.GONE);
            }
        }
        else
        {
            String phoneNumberFormatted = phoneNumberHelper.formatPhoneNumber(phoneNumber, StartActivity.COUNTRY_CODE, PhoneNumberUtil.PhoneNumberFormat.NATIONAL);

            phoneNumberTextView.setText(phoneNumberFormatted);
            phoneNumberTextView2.setVisibility(View.GONE);
        }


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
     * Gets {@link Block} from local database by passed blocked phone number.
     *
     * @param phoneNumber blocked number
     * @return {@link Block} if exist blocked number number
     */
    private Block getBlock(String phoneNumber)
    {
        Log.e("NUMBER", myPhoneNumber);
        return db.getBlocking(myPhoneNumber, phoneNumber);
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
