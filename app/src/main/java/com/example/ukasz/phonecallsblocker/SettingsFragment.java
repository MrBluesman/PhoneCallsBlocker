package com.example.ukasz.phonecallsblocker;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;

import com.example.ukasz.androidsqlite.DatabaseHandler;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SettingsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingsFragment extends Fragment
{
    private Switch blockServiceSwitch;
    private Switch autoBlockSwitch;
    private Switch foreignBlockSwitch;

    //Apps data
    private SharedPreferences data;

    private OnFragmentInteractionListener mListener;

    public SettingsFragment()
    {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment HomeFragment.
     */
    public static SettingsFragment newInstance()
    {
        SettingsFragment fragment = new SettingsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Runs on create this Fragment.
     *
     * @param savedInstanceState saved instance of this Fragment
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Log.e("HomeFragment", "onCreate() method");
    }

    /**
     * Creates a view of this Fragment.
     *
     * @param inflater {@link LayoutInflater} of this Fragment
     * @param container {@link ViewGroup} container for elements of this Fragment
     * @param savedInstanceState saved instance of this Fragment
     * @return created View of this Fragment
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        Log.e("HomeFragment", "onCreateView() method");
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        Log.e("HomeFragment","onActivityCreated() method");
        //Switcher to enable/disable blocking
        blockServiceSwitch = getView().findViewById(R.id.settings_fragment_switch1_block_service);
        autoBlockSwitch = getView().findViewById(R.id.settings_fragment_switch2_automatic_block);
        foreignBlockSwitch = getView().findViewById(R.id.settings_fragment_switch3_foreign_block);

        loadSettingsState();

        //Click listener for enable or disable phone calls catching
        blockServiceSwitch.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //get detectEnabled from data Shared Preferences
                boolean detectEnabled = !data.getBoolean("detectEnabled", false);
                //setDetectEnabled with changed value of detectEnabled
                StartActivity sA = ((StartActivity)getActivity());
                assert sA != null;
                sA.setDetectEnabled(detectEnabled);

                blockServiceSwitch.setChecked(detectEnabled);

                //rest of switch block options depends on detectEnabled
                autoBlockSwitch.setEnabled(detectEnabled);
                foreignBlockSwitch.setEnabled(detectEnabled);

                //Save setting in SharedPreference
                SharedPreferences.Editor editDataSettings = data.edit();
                editDataSettings.putBoolean("detectEnabled", detectEnabled);
                editDataSettings.apply(); //commit
            }
        });

        //Click listener for enable or disable auto blocking
        autoBlockSwitch.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //get autoBlockEnabled from data SharedPreferences
                boolean autoBlockEnabled = !data.getBoolean("autoBlockEnabled", false);

                autoBlockSwitch.setChecked(autoBlockEnabled);

                //Save setting in SharedPreferences
                SharedPreferences.Editor editDataSettings = data.edit();
                editDataSettings.putBoolean("autoBlockEnabled", autoBlockEnabled);
                editDataSettings.apply(); //commit
            }
        });

        //Click listener for enable or disable foreign numbers blocking
        foreignBlockSwitch.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //get foreignBlockEnabled from data SharedPreferences
                boolean foreignBlockEnabled = !data.getBoolean("foreignBlockEnabled", false);

                foreignBlockSwitch.setChecked(foreignBlockEnabled);

                //Save setting in SharedPreferences
                SharedPreferences.Editor editDataSettings = data.edit();
                editDataSettings.putBoolean("foreignBlockEnabled", foreignBlockEnabled);
                editDataSettings.apply(); //commit
            }
        });
    }

    /**
     * Loads a settings apps state from SharedPreferences.
     * Sets a switcher to positions depends on saved settings.
     */
    private void loadSettingsState()
    {
        boolean detectEnabled = data.getBoolean("detectEnabled", false);
        boolean autoBlockEnabled = data.getBoolean("autoBlockEnabled", false);
        boolean foreignBlockEnabled = data.getBoolean("foreignBlockEnabled", false);

        blockServiceSwitch.setChecked(detectEnabled);
        autoBlockSwitch.setChecked(autoBlockEnabled);
        foreignBlockSwitch.setChecked(foreignBlockEnabled);

        //rest of switch block options depends on detectEnabled
        autoBlockSwitch.setEnabled(detectEnabled);
        foreignBlockSwitch.setEnabled(detectEnabled);
    }

    /**
     * Runs on fragment resume.
     */
    @Override
    public void onResume()
    {
        super.onResume();
        Log.e("HomeFragment","onResume() method");
        //loadSettingsState();
    }

    /**
     * Run on attach a this Fragment to Activity.
     *
     * @param context {@link Context} of Activity which includes a this Fragment
     */
    @Override
    public void onAttach(Context context)
    {
        Log.e("HomeFragment","onAttach() method");

        data = context.getSharedPreferences("data", Context.MODE_PRIVATE);
        super.onAttach(context);

        if (context instanceof OnFragmentInteractionListener)
        {
            mListener = (OnFragmentInteractionListener) context;
        }
        else
        {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    /**
     * Runs on detach a this Fragment from its Activity.
     */
    @Override
    public void onDetach()
    {
        Log.e("HomeFragment","onDetach() method");
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener
    {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
