package com.example.ukasz.phonecallsblocker;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.ukasz.androidsqlite.Block;
import com.example.ukasz.androidsqlite.DatabaseHandler;

import java.lang.reflect.Method;
import java.util.List;

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
         * @param state             Information about state from TelephonyManager
         * @param incomingNumber    Contains the number of incoming call
         */
        @Override
        public void onCallStateChanged(int state, final String incomingNumber)
        {
            switch(state)
            {

                case TelephonyManager.CALL_STATE_RINGING:
                {
                    Log.e("test", "CallDetector - onCallStateChanged() method in CALL STATE LISTENER");
                    Toast.makeText(ctx,"Połączenie przychodzące: "+incomingNumber, Toast.LENGTH_LONG).show();
                    createNotification(incomingNumber);

                    final DatabaseHandler db = new DatabaseHandler(ctx);

                    SharedPreferences data;
                    data = ctx.getSharedPreferences("data", Context.MODE_PRIVATE);
                    boolean autoBlockEnabled = data.getBoolean("autoBlockEnabled", false);
                    //blockServiceSwitch.setChecked(detectEnabled);
                    //autoBlockSwitch.setChecked(autoBlockEnabled);
                    //textViewTestowy.setText(Boolean.toString(detectEnabled));

                    if(autoBlockEnabled & db.getNumberBlockingsCount(incomingNumber)>0)
                    {
                        try {
                            declinePhone(ctx);
                            Toast.makeText(ctx, "Zablokowano", Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    else
                    {
                        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
                        builder.setTitle(incomingNumber + " \n liczba zablokowań: " + db.getNumberBlockingsCount(incomingNumber));
                        //builder.setMessage("aaa");
                        builder.setItems(new CharSequence[]
                                        {"Zablokuj i zapisz", "Zablokuj", "Przepuść"},
                                new DialogInterface.OnClickListener()
                                {
                                    public void onClick(DialogInterface dialog, int which)
                                    {
                                        switch (which)
                                        {
                                            case 0:
                                                final List<String> categories = db.getAllCategories();
                                                AlertDialog.Builder builder2 = new AlertDialog.Builder(ctx);
                                                builder2.setTitle("Wybierz kategorię:");
                                                CharSequence[] categoriesCharSequence = new CharSequence[categories.size()];

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
                                                            public void onClick(DialogInterface dialog, int which2)
                                                            {
                                                                if(db.existBlock(new Block(tm.getLine1Number(), incomingNumber, which2, "a", true)))
                                                                {
                                                                    Toast.makeText(ctx, "Numer został już zablokowany!", Toast.LENGTH_SHORT).show();
                                                                }
                                                                else db.addBlocking(new Block(tm.getLine1Number(), incomingNumber, which2, "a", true));
                                                                try
                                                                {
                                                                    declinePhone(ctx);
                                                                    Toast.makeText(ctx, "Zablokowano", Toast.LENGTH_SHORT).show();
                                                                }
                                                                catch (Exception e)
                                                                {
                                                                    e.printStackTrace();
                                                                }
                                                            }
                                                        }
                                                );

                                                AlertDialog alertDialog2 = builder2.create();
                                                alertDialog2.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                                                alertDialog2.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                                                        | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                                                        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                                                        | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
                                                alertDialog2.show();

                                                break;
                                            case 1:
                                                try
                                                {
                                                    declinePhone(ctx);
                                                    Toast.makeText(ctx, "Zablokowano", Toast.LENGTH_SHORT).show();
                                                }
                                                catch (Exception e)
                                                {
                                                    e.printStackTrace();
                                                }
                                                break;
                                            case 2:
                                                Toast.makeText(ctx, "Przepuszczono", Toast.LENGTH_SHORT).show();
                                                break;
                                        }
                                    }
                                });



                        AlertDialog alertDialog = builder.create();
                        alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                        alertDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
                        alertDialog.show();
                    }
                }
            }
        }
    }

    /**
     * Method which decline/hang out/turn off incoming call
     *
     * @param context       Context of application
     * @throws Exception    Exception, when app cannot decline incoming call
     */
    private void declinePhone(Context context) throws Exception
    {

        try
        {
            String serviceManagerName = "android.os.ServiceManager";
            String serviceManagerNativeName = "android.os.ServiceManagerNative";
            String telephonyName = "com.android.internal.telephony.ITelephony";
            Class<?> telephonyClass;
            Class<?> telephonyStubClass;
            Class<?> serviceManagerClass;
            Class<?> serviceManagerNativeClass;
            Method telephonyEndCall;
            Object telephonyObject;
            Object serviceManagerObject;
            telephonyClass = Class.forName(telephonyName);
            telephonyStubClass = telephonyClass.getClasses()[0];
            serviceManagerClass = Class.forName(serviceManagerName);
            serviceManagerNativeClass = Class.forName(serviceManagerNativeName);
            Method getService = // getDefaults[29];
                    serviceManagerClass.getMethod("getService", String.class);
            Method tempInterfaceMethod = serviceManagerNativeClass.getMethod("asInterface", IBinder.class);
            Binder tmpBinder = new Binder();
            tmpBinder.attachInterface(null, "fake");
            serviceManagerObject = tempInterfaceMethod.invoke(null, tmpBinder);
            IBinder retbinder = (IBinder) getService.invoke(serviceManagerObject, "phone");
            Method serviceMethod = telephonyStubClass.getMethod("asInterface", IBinder.class);
            telephonyObject = serviceMethod.invoke(null, retbinder);
            telephonyEndCall = telephonyClass.getMethod("endCall");
            telephonyEndCall.invoke(telephonyObject);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.d("declinePhone: ", "Nie mozna zakonczyc polaczenia");
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
         * @param context   app context
         * @param intent    intent of outgoing call action
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
     * @param _ctx  param of setting the app context for the CallDetector object
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
     * @param phoneNumber   phone number which we wants to show in notification
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
