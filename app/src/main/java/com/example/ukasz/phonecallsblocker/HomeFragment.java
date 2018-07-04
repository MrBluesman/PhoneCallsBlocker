package com.example.ukasz.phonecallsblocker;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

import com.example.ukasz.androidsqlite.Block;
import com.example.ukasz.androidsqlite.DatabaseHandler;

import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link HomeFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment
{
//    // TODO: Rename parameter arguments, choose names that match
//    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
//
//    private static final String ARG_PARAM1 = "param1";
//    private static final String ARG_PARAM2 = "param2";
//
//    // TODO: Rename and change types of parameters
//    private String mParam1;
//    private String mParam2;

    private Switch blockServiceSwitch;
    private Switch autoBlockSwitch;
    //textView TESTOWY
    private TextView textViewTestowy;

    //Apps data
    private SharedPreferences data;

    private OnFragmentInteractionListener mListener;

    public HomeFragment()
    {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment HomeFragment.
     */
//     TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance()
    {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Runs on create this Fragment.
     * @param savedInstanceState Saved instance of this Fragment.
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Log.e("HomeFragment", "onCreate() method");
        if (getArguments() != null)
        {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    /**
     * Creates a view of this Fragment.
     * @param inflater LayoutInflater of this Fragment.
     * @param container ViewGroup container for elements of this Fragment.
     * @param savedInstanceState Saved instance of this Fragment.
     * @return Created View of this Fragment.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        Log.e("HomeFragment", "onCreateView() method");
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

//    // TODO: Rename method, update argument and hook method into UI event
//    public void onButtonPressed(Uri uri)
//    {
//        if (mListener != null)
//        {
//            mListener.onFragmentInteraction(uri);
//        }
//    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        Log.e("HomeFragment","onActivityCreated() method");
        //Switcher to enable/disable blocking
        blockServiceSwitch = getView().findViewById(R.id.home_fragment_switch1_block_service);
        autoBlockSwitch = getView().findViewById(R.id.home_fragment_switch2_automatic_block);
        //Tests text Views
        textViewTestowy = getView().findViewById(R.id.textView);

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
                textViewTestowy.setText(Boolean.toString(detectEnabled));

                //Save setting in SharedPreference
                SharedPreferences.Editor editDataSettings = data.edit();
                editDataSettings.putBoolean("detectEnabled", detectEnabled);
                editDataSettings.apply(); //commit
            }
        });

        //Click listener for enable or disable phone calls catching
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


        //DATABASE IMPLEMENTATION TESTING ---------------------------------------------------------
        DatabaseHandler db = new DatabaseHandler(getActivity());
        //Insertings blocks
        Log.d("Insert: ", "Inserting..");
        //db.addBlocking(new Block("721315333", "665693959", 0, "a", true));
        //db.addBlocking(new Block("721315345", "665693959", 0, "a", true));
        //db.deleteBlocking(new Block("721315778", "665693959", 0, "a", true));
        //db.deleteBlocking(new Block("+48721315778", "665693959", 0, "a", true));
    }

    /**
     * Loads a settings apps state from SharedPreferences.
     * Sets a switcher to positions depends on saved settings.
     */
    private void loadSettingsState()
    {
        boolean  detectEnabled = data.getBoolean("detectEnabled", false);
        boolean autoBlockEnabled = data.getBoolean("autoBlockEnabled", false);
        blockServiceSwitch.setChecked(detectEnabled);
        autoBlockSwitch.setChecked(autoBlockEnabled);
        textViewTestowy.setText(Boolean.toString(detectEnabled));
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
     * @param context Context of Activity which includes a this Fragment.
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
