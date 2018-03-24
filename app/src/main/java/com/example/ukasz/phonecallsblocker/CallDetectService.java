package com.example.ukasz.phonecallsblocker;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by Łukasz on 2017-03-05.
 * Service class which allows working app in background.
 * Allows start and stop detecting calls.
 */
public class CallDetectService extends Service
{
    private CallDetector callDetector;

    public CallDetectService()
    {

    }

    /**
     * This method runs on creating the service of detecting calls.
     * Creating a CallDetector object, and calls a start() method for it.
     *
     * @param intent intent which runs a service.
     * @param flags flags of service.
     * @param startId id of service.
     * @return res.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Log.e("test","CallDetectService - onStartCommand() method");
        callDetector = new CallDetector(this);
        int res = super.onStartCommand(intent, flags, startId);
        callDetector.start();
        return res;
    }

    /**
     * This method runs when we disable the service.
     * Calls a stop() method for CallDetector object.
     */
    @Override
    public void onDestroy()
    {
        super.onDestroy();
        Log.e("test","CallDetectService - onDestroy() method");
        callDetector.stop();
        Toast.makeText(this, "service onDestroy", Toast.LENGTH_SHORT).show();


    }



    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
