package com.example.ukasz.phonecallsblocker;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.NotificationCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Created by Łukasz Parysek on 2017-03-05.
 * This class describes Listener, and Retriever for Incoming and Outging calls
 */
public class CallDetector
{
    /**
     * Local private class which describes Listener for Incoming colls
     */
    private class CallStateListener extends PhoneStateListener
    {
        /**
         * This method runs when the Listener is working, and call state has changed
         * Creating a Toast and Notification when the calls incoming
         * (incoming call)
         *
         * @param state Information about state from TelephonyManager
         * @param incomingNumber Contains the number of incoming call
         */
        @Override
        public void onCallStateChanged(int state, String incomingNumber)
        {
            switch(state)
            {
                case TelephonyManager.CALL_STATE_RINGING:
                {
                    Log.e("test", "CallDetector - onCallStateChanged() method in CALL STATE LISTENER");
                    Toast.makeText(ctx,"Połączenie przychodzące: "+incomingNumber, Toast.LENGTH_LONG).show();
                    createNotification(incomingNumber);
                }
            }
        }
    }

    /**
     * Local private class which describes Receiver for Outgoing calls
     */
    private class OutgoingReceiver extends BroadcastReceiver
    {
        public OutgoingReceiver() {
        }

        /**
         * This method runs, when we receiving a outgoing call in app context
         * Creating a Toast and Notification when the calls outcoming
         * (outcoming call)
         *
         * @param context app context
         * @param intent intent of outgoing call action
         */
        @Override
        public void onReceive(Context context, Intent intent)
        {
            Log.e("test", "CallDetector - onReceive() method on OUTGOING RECEIVER");
            String outgoingNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
            Toast.makeText(ctx,"Połączenie wychodzące: "+outgoingNumber, Toast.LENGTH_LONG).show();
            createNotification(outgoingNumber);
        }
    }

    private Context ctx;
    private TelephonyManager tm;
    private CallStateListener callStateListener;
    private OutgoingReceiver outgoingReceiver;

    /**
     * Constructor
     * Creating a Listener and Receiver for calls
     *
     * @param _ctx param of setting the app context for the CallDetector object
     */
    public CallDetector(Context _ctx)
    {
        Log.e("test","CallDetector - construct on creating detector (listener)");
        ctx = _ctx;
        callStateListener = new CallStateListener();
        outgoingReceiver = new OutgoingReceiver();
    }

    /**
     * This method starts a Telephony service by the Telephony Manager instance on out context
     * Settings a Listener on listening calls state
     * Settings a registerReceiver on retrieving a outgoing calls
     */
    public void start()
    {
        Log.e("test", "CallDetector - start() method");
        tm = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
        tm.listen(callStateListener, PhoneStateListener.LISTEN_CALL_STATE);

        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_NEW_OUTGOING_CALL);
        ctx.registerReceiver(outgoingReceiver, intentFilter);
    }

    /**
     *  This method stops a listening incoming calls by set listener state on LISTEN_NONE
     *  Unregisters receiver for outgoing calls
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



    /**
     *  This method creating a notification
     *
     * @param phoneNumber phone number which we wants to show in notification
     */
    private void createNotification(String phoneNumber)
    {
        //Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);

        Notification noti = new NotificationCompat.Builder(ctx)
                .setContentTitle("Nowe połączenie")
                .setContentText(phoneNumber)
                //.setTicker("Masz wiadomość")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                //.setLargeIcon(icon)
                .setAutoCancel(true)
                //.setContentIntent(pIntent)
                .build();

        NotificationManager notificationManager = (NotificationManager) ctx.getSystemService(NOTIFICATION_SERVICE);


        notificationManager.notify(0, noti);
    }



}
