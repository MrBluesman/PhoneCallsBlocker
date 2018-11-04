package com.example.ukasz.phonecallsblocker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.ukasz.androidsqlite.Block;
import com.example.ukasz.androidsqlite.DatabaseHandler;
import com.example.ukasz.androidsqlite.RegistryBlock;
import com.example.ukasz.phonecallsblocker.notification_helper.NotificationID;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * Created by Łukasz Parysek on 2017-03-05.
 * This class describes Listener, and Retriever for Incoming and Outgoing calls.
 */
public class CallDetector
{
    /**
     * Local private class which describes Listener for Incoming calls.
     */
    private class CallStateListener extends PhoneStateListener
    {
        /**
         * This method runs when the Listener is working, and call state has changed.
         * Creating a Toast and Notification when the calls incoming.
         * (incoming call).
         *
         * @param state information about state from TelephonyManager
         * @param incomingNumber contains the number of incoming call
         */
        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onCallStateChanged(int state, final String incomingNumber)
        {
            //Settings data
            SharedPreferences data;
            data = ctx.getSharedPreferences("data", Context.MODE_PRIVATE);
            final boolean autoBlockEnabled = data.getBoolean("autoBlockEnabled", false);
            final boolean foreignBlockEnabled = data.getBoolean("foreignBlockEnabled", false);
            final boolean privateBlockEnabled = data.getBoolean("privateBlockEnabled", false);
            final boolean unknownBlockEnabled = data.getBoolean("unknownBlockEnabled", false);

            final boolean notificationBlockEnabled = data.getBoolean("notificationBlockEnabled", false);
            final boolean notificationAllowEnabled = data.getBoolean("notificationAllowEnabled", false);

            final String incomingNumberFormatted = incomingNumber != null ? incomingNumber : "Numer prywatny";
            final String incomingContactName = (unknownBlockEnabled && !incomingNumberFormatted.isEmpty())
                    ? getContactName(ctx, incomingNumberFormatted)
                    : null;

            //Firebase blockings data
            Query blockings = mDatabase
                    .child("blockings")
                    .orderByChild("nrBlocked")
                    .equalTo(incomingNumberFormatted);
            Query myBlockings = mDatabase
                    .child("blockings")
                    .orderByChild("nrDeclarantBlocked")
                    .equalTo(myPhoneNumber + "_" + incomingNumberFormatted);
            blockings.getRef().keepSynced(true);
            myBlockings.getRef().keepSynced(true);

            switch (state)
            {
                case TelephonyManager.CALL_STATE_RINGING:
                {
                    Toast.makeText(ctx, "Połączenie przychodzące: " + incomingNumberFormatted, Toast.LENGTH_LONG).show();
                    Log.e("incomingNumber", incomingNumberFormatted);
                    //database and settings load
                    final DatabaseHandler db = new DatabaseHandler(ctx);

//                    //Global blocking only if auto block (global) is enabled
//                    if(autoBlockEnabled)
//                    {
//                        blockings.addListenerForSingleValueEvent(new ValueEventListener()
//                        {
//                            @Override
//                            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
//                            {
//                                Log.e("BLOK! LICZBA! ", String.valueOf(dataSnapshot.getChildrenCount()));
//                                //Counter for count blocking category to decite whether block or not
//                                int trueAmount = 0;
//                                int falseAmount = 0;
//                                for(DataSnapshot blockSnapshot : dataSnapshot.getChildren())
//                                {
//                                    Block block = blockSnapshot.getValue(Block.class);
//                                    assert block != null;
//                                    if(block.getNrRating()) trueAmount++;
//                                    else falseAmount++;
//                                }
//
//                                //GLOBAL BLOCK CONDITION - TODO: CONSIDER CONDITION!
//                                if(trueAmount > falseAmount) {
//                                    declinePhone(ctx);
//                                    registerPhoneBlock(db, incomingNumberFormatted, true);
//                                }
//                            }
//
//                            @Override
//                            public void onCancelled(@NonNull DatabaseError databaseError)
//                            {
//                                Toast.makeText(ctx, R.string.error, Toast.LENGTH_SHORT).show();
//                            }
//                        });
//                    }


                    //Local blocking
                    myBlockings.addListenerForSingleValueEvent(new ValueEventListener()
                    {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                        {
                            //Actions if local block exists in db
                            if(dataSnapshot.getChildrenCount() > 0)
                            {
                                //get local block
                                Block block = dataSnapshot.getChildren().iterator().next().getValue(Block.class);

                                if(autoBlockEnabled && block != null)
                                {
                                    if(block.getNrRating())
                                    {
                                        //if notification block is enabled - show a notification
                                        if(notificationBlockEnabled) notificationManager.notify(
                                                NotificationID.getID(),
                                                createNotification(incomingNumberFormatted, NOTIFICATION_BLOCKED).build()
                                        );
                                        //decline and register
                                        declinePhone(ctx);
                                        registerPhoneBlock(db, incomingNumberFormatted, true);
                                    }
                                    else //Phone call allowed
                                    {
                                        //if notification allow is enabled - show a notification
                                        if (notificationAllowEnabled) notificationManager.notify(
                                                NotificationID.getID(),
                                                createNotification(incomingNumberFormatted, NOTIFICATION_ALLOWED).build()
                                        );
                                        registerPhoneBlock(db, incomingNumberFormatted, false);
                                    }
                                }
                                else if(block != null) //manual blocking
                                {
                                    if(block.getNrRating())
                                    {
                                        boolean canDrawOverlays = true;
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                                        {
                                            if (!Settings.canDrawOverlays(ctx)) canDrawOverlays = false;
                                        }

                                        if (canDrawOverlays)
                                        {
                                            AlertDialog alertDialog;
                                            //If number is private show dialog box with limited options - only block and allow
                                            if (incomingNumber == null)
                                                alertDialog = createIncomingCallDialogPrivateNumber(incomingNumberFormatted, db);
                                            else
                                            {
                                                //If number is blocked by user show dialog box with possibility to change to positive number
                                                alertDialog = createIncomingCallDialogBlockedNumber(incomingNumberFormatted, db);
                                            }

                                            alertDialog.getWindow().setType(getDialogLayoutFlag());
                                            alertDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                                                    | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                                                    | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                                                    | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
                                            alertDialog.show();
                                        }
                                    }
                                    else //Phone call allowed
                                    {
                                        //if notification allow is enabled - show a notification
                                        if (notificationAllowEnabled) notificationManager.notify(
                                                NotificationID.getID(),
                                                createNotification(incomingNumberFormatted, NOTIFICATION_ALLOWED).build()
                                        );
                                        registerPhoneBlock(db, incomingNumberFormatted, false);
                                    }
                                }
                            }
                            //Condition not relevant to database checks
                            //Check if we should autoblocked (only for negative phone numbers)
                            else if((foreignBlockEnabled && isForeignIncomingCall(incomingNumberFormatted)) //OR phone number is foreign and foreignBlock is enabled
                                    || (privateBlockEnabled && incomingNumber == null) //OR phone number is private and privateBlock is enabled
                                    || (unknownBlockEnabled && incomingContactName == null)) //OR phone number is unknown and uknownBlock is enabled
                            {
                                //if notification block is enabled - show a notification
                                if(notificationBlockEnabled) notificationManager.notify(
                                        NotificationID.getID(),
                                        createNotification(incomingNumberFormatted, NOTIFICATION_BLOCKED).build()
                                );
                                //decline and register
                                declinePhone(ctx);
                                registerPhoneBlock(db, incomingNumberFormatted, true);
                            }
                            else if(!autoBlockEnabled) //unknown number and manual blocking is enabled
                            {
                                boolean canDrawOverlays = true;
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                                {
                                    if (!Settings.canDrawOverlays(ctx)) canDrawOverlays = false;
                                }

                                if (canDrawOverlays)
                                {
                                    AlertDialog alertDialog;
                                    //If number is private show dialog box with limited options - only block and allow
                                    if (incomingNumber == null)
                                        alertDialog = createIncomingCallDialogPrivateNumber(incomingNumberFormatted, db);
                                    else
                                    {
                                        //If number is blocked by user show dialog box with possibility to change to positive number
                                        alertDialog = createIncomingCallDialogNewNumber(incomingNumberFormatted, db, dataSnapshot.getChildrenCount());
                                    }

                                    alertDialog.getWindow().setType(getDialogLayoutFlag());
                                    alertDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                                            | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                                            | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                                            | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
                                    alertDialog.show();
                                }
                            }
                            else //Phone call allowed
                            {
                                //if notification allow is enabled - show a notification
                                if(notificationAllowEnabled) notificationManager.notify(
                                        NotificationID.getID(),
                                        createNotification(incomingNumberFormatted, NOTIFICATION_ALLOWED).build()
                                );
                                registerPhoneBlock(db, incomingNumberFormatted, false);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError)
                        {

                        }
                    });

                    break;
                }
                case TelephonyManager.CALL_STATE_OFFHOOK:
                {
                    Log.e("ABC", "już nie dzwoni");
                    break;
                }
            }
        }

        /**
         * Creates a {@link AlertDialog} for incoming call with number
         * which doesn't exist in local list.
         *
         * @param incomingNumber contains the number of incoming call
         * @param db database for receive number of blockings and allow save to local list
         * @param blockingsCount blockings count to display in {@link android.support.v7.app.AlertDialog}
         * @return created {@link AlertDialog} with options for incoming call dialog for new number
         */
        private AlertDialog createIncomingCallDialogNewNumber(final String incomingNumber, final DatabaseHandler db, final long blockingsCount)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
            builder.setTitle(incomingNumber + " \n liczba zablokowań: " + blockingsCount);
            builder.setItems(R.array.incoming_number_options,
                    new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialog, int which)
                        {
                            switch (which)
                            {
                                //Save and block
                                case 0:
                                    final List<String> categories = db.getAllCategories();
                                    AlertDialog.Builder builder2 = new AlertDialog.Builder(ctx);
                                    builder2.setTitle(R.string.call_detector_choose_category_title);
                                    CharSequence[] categoriesCharSequence = new CharSequence[categories.size()];

                                    //Build categories list
                                    int i=0;
                                    for(String cat:categories)
                                    {
                                        categoriesCharSequence[i] = cat;
                                        i++;
                                    }

                                    builder2.setItems(categoriesCharSequence,
                                            new DialogInterface.OnClickListener()
                                            {

                                                @Override
                                                public void onClick(DialogInterface dialog, int categoryId)
                                                {
                                                    addPhoneBlock(db, incomingNumber, categoryId, true);
                                                    declinePhone(ctx);
                                                    registerPhoneBlock(db, incomingNumber, true);
                                                }
                                            }
                                    );

                                    AlertDialog alertDialog2 = builder2.create();
                                    alertDialog2.getWindow().setType(getDialogLayoutFlag());
                                    alertDialog2.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                                            | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                                            | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                                            | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
                                    alertDialog2.show();

                                    break;
                                //Block
                                case 1:
                                    declinePhone(ctx);
                                    registerPhoneBlock(db, incomingNumber, true);
                                    Toast.makeText(ctx, R.string.call_detector_has_blocked, Toast.LENGTH_SHORT).show();
                                    break;

                                //Save as positive (white list)
                                case 2:
                                    addPhoneBlock(db, incomingNumber, 0, false);
                                    registerPhoneBlock(db, incomingNumber, false);
                                    Toast.makeText(ctx, R.string.call_detector_has_saved_positive, Toast.LENGTH_SHORT).show();

                                //Allow
                                case 3:
                                    registerPhoneBlock(db, incomingNumber, false);
                                    Toast.makeText(ctx, R.string.call_detector_has_allowed, Toast.LENGTH_SHORT).show();
                                    break;
                            }
                        }
                    });

           return builder.create();
        }

        /**
         * Creates a {@link AlertDialog} for incoming call with number
         * which exist in local list.
         *
         * @param incomingNumber contains the number of incoming call
         * @param db database for receive number of blockings and allow update a local list
         * @return created {@link AlertDialog} with options for incoming call dialog for existing number
         */
        private AlertDialog createIncomingCallDialogBlockedNumber(final String incomingNumber, final DatabaseHandler db)
        {
            final AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
            builder.setTitle(incomingNumber + " \n Ten numer jest przez Ciebie blokowany.");

            builder.setItems(R.array.incoming_blocked_number_options,
                    new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            switch(which)
                            {
                                case 0:
                                //Change to positive (white list) - false is positive (not blocked)
                                    updatePhoneBlock(db, incomingNumber, false);
                                    registerPhoneBlock(db, incomingNumber, false);

                                    Toast.makeText(ctx, R.string.call_detector_changed_to_positive, Toast.LENGTH_SHORT).show();
                                    break;

                                //Block
                                case 1:
                                    declinePhone(ctx);
                                    registerPhoneBlock(db, incomingNumber, true);
                                    Toast.makeText(ctx, R.string.call_detector_has_blocked, Toast.LENGTH_SHORT).show();
                                    break;

                                //Allow
                                case 2:
                                    Toast.makeText(ctx, R.string.call_detector_has_allowed, Toast.LENGTH_SHORT).show();
                                    registerPhoneBlock(db, incomingNumber, false);
                                    break;
                            }
                        }
                    });

            return builder.create();
        }

        /**
         * Creates a {@link AlertDialog} for incoming call of private number
         *
         * @param incomingNumber contains the number of incoming call (equals to Private number)
         * @param db database for registering blocking in registry list
         * @return created {@link AlertDialog} with options for incoming call dialog for private number
         */
        private AlertDialog createIncomingCallDialogPrivateNumber(final String incomingNumber, final DatabaseHandler db)
        {
            final AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
            builder.setTitle(incomingNumber);

            builder.setItems(R.array.incoming_private_number_options,
                    new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            switch(which)
                            {
                                //Block
                                case 0:
                                    declinePhone(ctx);
                                    registerPhoneBlock(db, incomingNumber, true);
                                    Toast.makeText(ctx, R.string.call_detector_has_blocked, Toast.LENGTH_SHORT).show();
                                    break;

                                //Allow
                                case 1:
                                    Toast.makeText(ctx, R.string.call_detector_has_allowed, Toast.LENGTH_SHORT).show();
                                    registerPhoneBlock(db, incomingNumber, false);
                                    break;
                            }
                        }
                    });

            return builder.create();
        }

        /**
         * Creates a notification for incoming number.
         *
         * @param incomingNumber contains the number of incoming call
         * @param type type of notification (blocked or allowed call)
         * @return {@link NotificationCompat.Builder} builder with created notification to build and show
         */
        private NotificationCompat.Builder createNotification(final String incomingNumber, int type)
        {
            // Create an explicit intent for an DetailsActivity after click on notification
            Intent detailsBlockIntent = new Intent(ctx, DetailsPhoneBlock.class);
            Bundle b = new Bundle();
            b.putString("phoneNumber", incomingNumber);
            detailsBlockIntent.putExtras(b);
            detailsBlockIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            //instance of pending intent with unique id
            //!!! FLAG_UPDATE_CURRENT - for redirecting to Registry (maybe in future)
            int uniquePendingIntentId = (int) (System.currentTimeMillis() & 0xfffffff);
            PendingIntent pendingIntent = PendingIntent.getActivity(ctx, uniquePendingIntentId, detailsBlockIntent, 0);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(ctx, CHANNEL_CALL_DETECTOR_ID);

            //Notification body depends on notification type (allowed or blocked)
            switch(type)
            {
                case NOTIFICATION_BLOCKED:
                {
                    builder.setSmallIcon(R.drawable.ic_call_end_white_24dp)
                            .setContentText(ctx.getString(R.string.call_detector_has_blocked)+".");
                    break;
                }
                default:
                {
                    builder.setSmallIcon(R.drawable.ic_done_white_24dp)
                            .setContentText(ctx.getString(R.string.call_detector_has_allowed)+".");
                }
            }

            builder.setContentTitle(incomingNumber)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    // Set the intent that will fire when the user taps the notification
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true);

            return builder;
        }

        /**
         * Checks if the incoming number is from foreign country.
         *
         * @param incomingNumber contains the number of incoming call
         * @return true if is from foreign country, false if not
         */
        private boolean isForeignIncomingCall(final String incomingNumber)
        {
            // get country-code from the phoneNumber
            PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
            try
            {
                Phonenumber.PhoneNumber numberProto = phoneUtil.parse(incomingNumber, Locale.getDefault().getCountry());
                if (phoneUtil.isValidNumber(numberProto))
                {
                    return myCountryDialCode != numberProto.getCountryCode();
                }
            }
            catch (NumberParseException e)
            {
                Log.e("CallDetector", "Unable to parse incoming phoneNumber " + e.toString());
            }

            return false;
        }

        /**
         * Gets the contact name of incoming phone call.
         *
         * @param context context of the app for the {@link CallDetector} object.
         * @param incomingNumber contains the number of incoming call
         * @return contact name or null if it's unknown phone number
         */
        private String getContactName(final Context context, final String incomingNumber)
        {
            ContentResolver cr = context.getContentResolver();
            Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(incomingNumber));

            Cursor cursor = cr.query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);
            if (cursor == null) return null;

            String contactName = null;
            if(cursor.moveToFirst())
            {
                contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
            }

            if(!cursor.isClosed()) cursor.close();

            return contactName;
        }
    }

    /**
     * Get the flag for Dialog Layout. Since Android O permission can no longer use the previous
     * system window type TYPE_SYSTEM_ALERT
     *
     * @return dialog layout flag
     */
    private int getDialogLayoutFlag()
    {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY :
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
    }

    /**
     * Adds phoneNumber to the blocking list.
     *
     * @param db {@link DatabaseHandler} to check if phoneNumber exists and add if not exists
     * @param phoneNumber phone number to add to blocking list
     * @param category category of added phone number
     * @param rating rating of added phone number, positive or negative
     */
    /* TODO: Consider refactor, adding phone number in one place! */
    @SuppressLint("HardwareIds")
    private void addPhoneBlock(DatabaseHandler db, String phoneNumber, int category, boolean rating)
    {
        if (ActivityCompat.checkSelfPermission(ctx, Manifest.permission.READ_SMS)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(ctx, Manifest.permission.READ_PHONE_NUMBERS)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(ctx, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) return;

        final Block newBlock = new Block(myPhoneNumber, phoneNumber, category, "", rating);

        //LOCAL SECTION! add to local blockings
        if(!db.existBlock(newBlock))
        {
            db.addBlocking(newBlock);
            //ADD to blocking list to make notify data changed possible for adapter
            Toast.makeText(ctx, R.string.add_phone_block_added, Toast.LENGTH_SHORT).show();
            //TODO: Refresh adapter after add
            PhoneBlockFragment.blockings.add(newBlock);
        }
        else
        {
            Toast.makeText(ctx, R.string.add_phone_block_already_exist, Toast.LENGTH_SHORT).show();
        }

        //GLOBAL SECTION! add to global blockings if sync is enabled
        boolean syncEnabled =  ctx.getSharedPreferences("data", Context.MODE_PRIVATE)
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
                    Toast.makeText(ctx, R.string.error, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    /**
     * Registers a blocking of blocked or passed call.
     *
     * @param db {@link DatabaseHandler} to check if phoneNumber exists and add if not exists
     * @param phoneNumber phone number to add to blocking list
     * @param rating rating of added phone number, positive or negative
     */
    private void registerPhoneBlock(DatabaseHandler db, String phoneNumber, boolean rating)
    {
        Log.e("CallDetector", "registerPhoneBlock - " + phoneNumber);
        db.addBlockingRegistry(new RegistryBlock(phoneNumber, rating, new Date()));
        RegistryFragment.loadRegistryBlockings();
    }

    /**
     * Updates the phone block with new rating value.
     *
     * @param db {@link DatabaseHandler} to make a update phoneNumber exists
     * @param phoneNumber phone number which rating will be updated
     * @param rating new blocking rating
     */
    private void updatePhoneBlock(DatabaseHandler db, final String phoneNumber, final boolean rating)
    {
        final Query blockings = mDatabase
                .child("blockings")
                .orderByChild("nrDeclarantBlocked")
                .equalTo(myPhoneNumber + "_" + phoneNumber);

        blockings.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    HashMap<String, Object> updateData = new HashMap<>();
                    updateData.put("nrRating", rating);
                    dataSnapshot.getChildren().iterator().next().getRef().updateChildren(updateData);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    /**
     * Method which decline/hang out/turn off incoming call.
     *
     * @param context context of application
     */
    private void declinePhone(Context context)
    {
        try
        {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            Method m1 = tm.getClass().getDeclaredMethod("getITelephony");
            m1.setAccessible(true);
            Object iTelephony = m1.invoke(tm);

            Method m2 = iTelephony.getClass().getDeclaredMethod("silenceRinger");
            Method m3 = iTelephony.getClass().getDeclaredMethod("endCall");

            m2.invoke(iTelephony);
            m3.invoke(iTelephony);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.d("declinePhone: ", "Nie mozna zakonczyc polaczenia");
        }
    }

    private Context ctx;
    private TelephonyManager tm;
    private String myPhoneNumber;
    private int myCountryDialCode;
    private CallStateListener callStateListener;
    //CallDetector channel for notification manager
    private static final String CHANNEL_CALL_DETECTOR_ID = "CallDetector";
    private NotificationManagerCompat notificationManager;
    //final static fields for notification type
    private final static int NOTIFICATION_BLOCKED = 0;
    private final static int NOTIFICATION_ALLOWED = 1;
    private DatabaseReference mDatabase;

    /**
     * Constructor.
     * Creating a Listener and Receiver for calls.
     *
     * @param _ctx  param of setting the app context for the CallDetector object
     */
    @SuppressLint("HardwareIds")
    public CallDetector(Context _ctx)
    {
        Log.e("test","CallDetector - construct on creating detector (listener)");
        ctx = _ctx;
        callStateListener = new CallStateListener();
        notificationManager = NotificationManagerCompat.from(ctx);

        /* TODO: refactor to keep all references in one place */
        //Database reference
        mDatabase = FirebaseDatabase.getInstance().getReference();
        //Notification channel
        createNotificationChannel();
    }

    /**
     * This method starts a Telephony service by the Telephony Manager instance on out context.
     * Settings a Listener on listening calls state.
     * Settings a registerReceiver on retrieving a outgoing calls.
     */
    @SuppressLint("HardwareIds")
    public void start()
    {
        Log.e("CallDetector", "start() method");
        tm = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
        tm.listen(callStateListener, PhoneStateListener.LISTEN_CALL_STATE);

        //Save the user phone number (declarant)
        //getMyPhoneNumber
        if (ActivityCompat.checkSelfPermission(ctx, Manifest.permission.READ_PHONE_NUMBERS)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(ctx, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) return;
        myPhoneNumber = !tm.getLine1Number().equals("") ? tm.getLine1Number() : tm.getSubscriberId();
        myPhoneNumber = !myPhoneNumber.equals("") ? myPhoneNumber : tm.getSimSerialNumber();
        myCountryDialCode = PhoneNumberUtil.getInstance().getCountryCodeForRegion(tm.getSimCountryIso().toUpperCase());
        Log.e("PHONE_NUMBER", String.valueOf(myCountryDialCode));
    }

    /**
     *  This method stops a listening incoming calls by set listener state on LISTEN_NONE.
     *  Unregisters receiver for outgoing calls.
     */
    public void stop()
    {
        Log.e("test", "CallDetector - stop() method !!!!!!!!!");
        tm = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
        tm.listen(callStateListener, PhoneStateListener.LISTEN_NONE);
    }

    /**
     * Creates a notification channel for {@link CallDetector} notifications.
     */
    private void createNotificationChannel()
    {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            CharSequence name = ctx.getString(R.string.call_detector_channel_name);
            String description = ctx.getString(R.string.call_detector_channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_CALL_DETECTOR_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = ctx.getSystemService(NotificationManager.class);
            assert notificationManager != null;
            notificationManager.createNotificationChannel(channel);
        }
    }
}
