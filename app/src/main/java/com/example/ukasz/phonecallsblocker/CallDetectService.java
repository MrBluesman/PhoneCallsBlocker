package com.example.ukasz.phonecallsblocker;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by Łukasz on 2017-03-05.
 * Service class which allows working app in background.
 * Allows start and stop detecting calls.
 */
public class CallDetectService extends JobService
{
    private CallDetector callDetector;

    /**
     * This method runs on starting the job service of detecting calls.
     * Creates a {@link CallDetector} object and calls a start method for it.
     *
     * @param params {@link JobParameters} parameters of the job to schedule
     * @return true
     */
    @Override
    public boolean onStartJob(JobParameters params)
    {
        Log.e("test","CallDetectService - onStartJob() method");
        if(callDetector == null)
        {
            callDetector = new CallDetector(this);
        }
        else
        {
            callDetector.stop();

        }
        callDetector.start();
        return true;
    }

    /**
     *
     * This method runs when we disable a job service of detecting calls.
     * Calls a stop method for {@link CallDetector} object.
     *
     * @param params {@link JobParameters} parameters of the job to finish
     * @return false
     */
    @Override
    public boolean onStopJob(JobParameters params)
    {
        Log.e("test","CallDetectService - onStopJob() method");
        SharedPreferences data = getApplicationContext().getSharedPreferences("data", Context.MODE_PRIVATE);
        boolean callDetectorEnabled = data.getBoolean("detectEnabled", false);
        //Stop only if call detection service is not enabled
        if(callDetector != null && !callDetectorEnabled)
        {
            callDetector.stop();
            callDetector = null;
        }
        jobFinished(params, false);
        return false;
    }
}
