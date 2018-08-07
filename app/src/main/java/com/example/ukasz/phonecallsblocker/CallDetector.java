package com.example.ukasz.phonecallsblocker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.telephony.PhoneNumberUtils;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.ukasz.androidsqlite.Block;
import com.example.ukasz.androidsqlite.DatabaseHandler;
import com.example.ukasz.androidsqlite.RegistryBlock;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.lang.reflect.Method;
import java.text.ParseException;
import java.util.Date;
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
            switch (state)
            {
                case TelephonyManager.CALL_STATE_RINGING:
                    {
                        isForeignIncomingCall(incomingNumber);
//-------------------------------------------------------------------------
                    Log.e("test", "CallDetector - onCallStateChanged() method in CALL STATE LISTENER");
                    Toast.makeText(ctx,"Połączenie przychodzące: "+incomingNumber, Toast.LENGTH_LONG).show();
//                    createNotification(incomingNumber);
                    Log.e("incomingNumber", incomingNumber);

                    final DatabaseHandler db = new DatabaseHandler(ctx);

                    SharedPreferences data;
                    data = ctx.getSharedPreferences("data", Context.MODE_PRIVATE);
                    boolean autoBlockEnabled = data.getBoolean("autoBlockEnabled", false);
                    boolean foreignBlockEnabled = data.getBoolean("foreignBlockEnabled", false);

                    //Checks if we should autoblocked (only for negative phone numbers)
                    if((autoBlockEnabled && db.getNumberBlockingsCount(incomingNumber, true) > 0)
                            || (foreignBlockEnabled && isForeignIncomingCall(incomingNumber)))
                    {
                        declinePhone(ctx);
                        registerPhoneBlock(db, incomingNumber, true);
                    }
                    else if(!db.existBlock(myPhoneNumber, incomingNumber, false))
                    {
                        //Can draw overlays depends on SDK version
                        boolean canDrawOverlays = true;
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                        {
                            if(!Settings.canDrawOverlays(ctx)) canDrawOverlays = false;
                        }

                        if(canDrawOverlays)
                        {
                            //If number is blocked by user show dialog box with possibility to change to positive number
                            AlertDialog alertDialog = db.existBlock(myPhoneNumber, incomingNumber, true)
                                    ? createIncomingCallDialogBlockedNumber(incomingNumber, db)
                                    : createIncomingCallDialogNewNumber(incomingNumber, db);


                            alertDialog.getWindow().setType(getDialogLayoutFlag());
                            alertDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                                    | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                                    | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                                    | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
                            alertDialog.show();
                        }
                    }
                    else
                    {
                        registerPhoneBlock(db, incomingNumber, false);
                    }
                }
                case TelephonyManager.CALL_STATE_OFFHOOK:
                {
                    Log.e("ABC", "już nie dzwoni");
                }
            }
        }

        /**
         * Creates a {@link AlertDialog} for incoming call with number
         * which doesn't exist in local list.
         *
         * @param incomingNumber contains the number of incoming call
         * @param db database for receive number of blockings and allow save to local list
         * @return created {@link AlertDialog} with options for incoming call dialog for new number
         */
        private AlertDialog createIncomingCallDialogNewNumber(final String incomingNumber, final DatabaseHandler db)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
            builder.setTitle(incomingNumber + " \n liczba zablokowań: " + db.getNumberBlockingsCount(incomingNumber));
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
    @SuppressLint("HardwareIds")
    private void addPhoneBlock(DatabaseHandler db, String phoneNumber, int category, boolean rating)
    {
        if (ActivityCompat.checkSelfPermission(ctx, Manifest.permission.READ_SMS)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(ctx, Manifest.permission.READ_PHONE_NUMBERS)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(ctx, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) return;

        if(db.existBlock(new Block(myPhoneNumber, phoneNumber, category, "", rating)))
        {
            Toast.makeText(ctx, R.string.call_detector_already_blocked, Toast.LENGTH_SHORT).show();
        }
        else db.addBlocking(new Block(myPhoneNumber, phoneNumber, category, "", rating));
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
        try
        {
            db.addBlockingRegistry(new RegistryBlock(phoneNumber, rating, new Date()));

            RegistryFragment.loadRegistryBlockings();
        }
        catch(ParseException e)
        {
            e.printStackTrace();
        }

    }

    /**
     * Updates the phone block with new rating value.
     *
     * @param db {@link DatabaseHandler} to make a update phoneNumber exists
     * @param phoneNumber phone number which rating will be updated
     * @param rating new blocking rating
     */
    private void updatePhoneBlock(DatabaseHandler db, String phoneNumber, boolean rating)
    {
        Block updatedBlock = db.getBlocking(myPhoneNumber, phoneNumber);
        updatedBlock.setNrRating(rating);
        db.updateBlocking(updatedBlock);
    }


    /**
     * Method which decline/hang out/turn off incoming call.
     *
     * @param context       Context of application.
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

    /**
     * Local private class which describes Receiver for Outgoing calls.
     */
    private class OutgoingReceiver extends BroadcastReceiver
    {
        public OutgoingReceiver() {}

        /**
         * This method runs, when we receiving a outgoing call in app context.
         * Creating a Toast and Notification when the calls outgoing.
         * (outgoing call).
         *
         * @param context   app context
         * @param intent    intent of outgoing call action
         */
        @Override
        public void onReceive(Context context, Intent intent)
        {
            Log.e("test", "CallDetector - onReceive() method on OUTGOING RECEIVER");
            String outgoingNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
            Toast.makeText(ctx,"Połączenie wychodzące: "+outgoingNumber, Toast.LENGTH_LONG).show();
//            createNotification(outgoingNumber);
        }
    }

    private Context ctx;
    private TelephonyManager tm;
    private String myPhoneNumber;
    private int myCountryDialCode;
    private CallStateListener callStateListener;
    private OutgoingReceiver outgoingReceiver;

    /**
     * Constructor.
     * Creating a Listener and Receiver for calls.
     *
     * @param _ctx  param of setting the app context for the CallDetector object.
     */
    @SuppressLint("HardwareIds")
    public CallDetector(Context _ctx)
    {
        Log.e("test","CallDetector - construct on creating detector (listener)");
        ctx = _ctx;
        callStateListener = new CallStateListener();
        outgoingReceiver = new OutgoingReceiver();
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
        if (ActivityCompat.checkSelfPermission(ctx, Manifest.permission.READ_SMS)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(ctx, Manifest.permission.READ_PHONE_NUMBERS)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(ctx, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }


        myPhoneNumber = !tm.getLine1Number().equals("") ? tm.getLine1Number() : tm.getSimSerialNumber();
        myCountryDialCode = PhoneNumberUtil.getInstance().getCountryCodeForRegion(tm.getSimCountryIso().toUpperCase());
        Log.e("PHONE_NUMBER", String.valueOf(myCountryDialCode));

        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_NEW_OUTGOING_CALL);
        ctx.registerReceiver(outgoingReceiver, intentFilter);
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
        //Log.e("test",);

        //callStateListener = null;
        ctx.unregisterReceiver(outgoingReceiver);
    }



//    /**
//     *  This method creating a notification
//     *
//     * @param phoneNumber   phone number which we wants to show in notification
//     */
//    private void createNotification(String phoneNumber)
//    {
//        //Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
//
//        Notification noti = new NotificationCompat.Builder(ctx)
//                .setContentTitle("Nowe połączenie")
//                .setContentText(phoneNumber)
//                //.setTicker("Masz wiadomość")
//                .setSmallIcon(android.R.drawable.ic_dialog_info)
//                //.setLargeIcon(icon)
//                .setAutoCancel(true)
//                //.setContentIntent(pIntent)
//                .build();
//
//        NotificationManager notificationManager = (NotificationManager) ctx.getSystemService(NOTIFICATION_SERVICE);
//
//
//        notificationManager.notify(0, noti);
//    }
}
