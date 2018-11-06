package com.example.ukasz.phonecallsblocker;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.Toast;

import com.example.ukasz.androidsqlite.Block;
import com.example.ukasz.androidsqlite.DatabaseHandler;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;


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
    //Blockings settings switches
    private Switch blockServiceSwitch;
    private Switch autoBlockSwitch;
    private Switch foreignBlockSwitch;
    private Switch privateBlockSwitch;
    private Switch unknownBlockSwitch;

    //Sync settings switches
    private Switch syncSwitch;

    //Notifications settings switches
    private Switch notificationBlockSwitch;
    private Switch notificationAllowSwitch;

    //Apps data
    private SharedPreferences data;

    //request unique codes
    private final int READ_CONTACTS_PERMISSION_REQUEST_CODE = 1112;

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
        Log.e("SettingsFragment", "onCreate() method");
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
        Log.e("SettingsFragment", "onCreateView() method");
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        Log.e("SettingsFragment","onActivityCreated() method");
        //Switches to enable/disable blocking options
        blockServiceSwitch = getView().findViewById(R.id.settings_fragment_switch1_block_service);
        autoBlockSwitch = getView().findViewById(R.id.settings_fragment_switch2_automatic_block);
        foreignBlockSwitch = getView().findViewById(R.id.settings_fragment_switch3_foreign_block);
        privateBlockSwitch = getView().findViewById(R.id.settings_fragment_switch4_private_block);
        unknownBlockSwitch = getView().findViewById(R.id.settings_fragment_switch5_unknown_block);

        //Switches to enable/disable sync local and global blockings lists
        syncSwitch = getView().findViewById(R.id.settings_fragment_switch6_allow_sync);

        //Switches to enable/disable notifications options
        notificationBlockSwitch = getView().findViewById(R.id.settings_fragment_switch7_notification_block);
        notificationAllowSwitch = getView().findViewById(R.id.settings_fragment_switch8_notification_allow);

        loadSettingsState();

        //Click listener for enable or disable phone calls catching
        blockServiceSwitch.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //get detectEnabled from data Shared Preferences
                boolean detectEnabled = !data.getBoolean("detectEnabled", false);

                blockServiceSwitch.setChecked(detectEnabled);

                //rest of switch block options depends on detectEnabled
                autoBlockSwitch.setEnabled(detectEnabled);
                foreignBlockSwitch.setEnabled(detectEnabled);
                privateBlockSwitch.setEnabled(detectEnabled);
                unknownBlockSwitch.setEnabled(detectEnabled);

                //Save setting in SharedPreference
                SharedPreferences.Editor editDataSettings = data.edit();
                editDataSettings.putBoolean("detectEnabled", detectEnabled);
                editDataSettings.apply(); //commit

                //setDetectEnabled with changed value of detectEnabled
                StartActivity sA = ((StartActivity)getActivity());
                assert sA != null;
                sA.setDetectEnabled(detectEnabled);
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

        //Click listener for enable or disable private numbers blocking
        privateBlockSwitch.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //get foreignBlockEnabled from data SharedPreferences
                boolean privateBlockEnabled = !data.getBoolean("privateBlockEnabled", false);

                privateBlockSwitch.setChecked(privateBlockEnabled);

                //Save setting in SharedPreferences
                SharedPreferences.Editor editDataSettings = data.edit();
                editDataSettings.putBoolean("privateBlockEnabled", privateBlockEnabled);
                editDataSettings.apply(); //commit
            }
        });

        //Click listener for enable or disable unknown numbers blocking
        unknownBlockSwitch.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //get foreignBlockEnabled from data SharedPreferences
                boolean unknownBlockEnabled = !data.getBoolean("unknownBlockEnabled", false);

                if(unknownBlockEnabled && !hasGrantedReadContactsPermission()) requestReadContactsPermission();
                else
                {
                    unknownBlockSwitch.setChecked(unknownBlockEnabled);

                    //Save setting in SharedPreferences
                    SharedPreferences.Editor editDataSettings = data.edit();
                    editDataSettings.putBoolean("unknownBlockEnabled", unknownBlockEnabled);
                    editDataSettings.apply(); //commit
                }
            }
        });

        //Click listener for enable or disable sync blockings lists
        syncSwitch.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //get syncEnabled from data SharedPreferences
                boolean syncEnabled = !data.getBoolean("syncEnabled", false);

                //Sync after turn off synchronization
                if(syncEnabled) syncFirebase();

                syncSwitch.setChecked(syncEnabled);

                //Save settings in SharedPreferences
                SharedPreferences.Editor editDataSettings = data.edit();
                editDataSettings.putBoolean("syncEnabled", syncEnabled);
                editDataSettings.apply(); //commit
            }
        });

        //Click listener for enable or disable block notification
        notificationBlockSwitch.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //get notificationBlockEnabled from data SharedPreferences
                boolean notificationBlockEnabled = !data.getBoolean("notificationBlockEnabled", false);

                notificationBlockSwitch.setChecked(notificationBlockEnabled);

                //Save setting in SharedPreferences
                SharedPreferences.Editor editDataSettings = data.edit();
                editDataSettings.putBoolean("notificationBlockEnabled", notificationBlockEnabled);
                editDataSettings.apply(); //commit
            }
        });

        //Click listener for enable or disable allow call notification
        notificationAllowSwitch.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //get notificationAllowEnabled from data SharedPreferences
                boolean notificationAllowEnabled = !data.getBoolean("notificationAllowEnabled", false);

                notificationAllowSwitch.setChecked(notificationAllowEnabled);

                //Save setting in Shared Preferences
                SharedPreferences.Editor editDataSettings = data.edit();
                editDataSettings.putBoolean("notificationAllowEnabled", notificationAllowEnabled);
                editDataSettings.apply();
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
        boolean privateBlockEnabled = data.getBoolean("privateBlockEnabled", false);
        boolean unknownBlockEnabled = data.getBoolean("unknownBlockEnabled", false);
        boolean syncEnabled = data.getBoolean("syncEnabled", false);
        boolean notificationBlockEnabled = data.getBoolean("notificationBlockEnabled", false);
        boolean notificationAllowEnabled = data.getBoolean("notificationAllowEnabled", false);

        //block settings
        blockServiceSwitch.setChecked(detectEnabled);
        autoBlockSwitch.setChecked(autoBlockEnabled);
        foreignBlockSwitch.setChecked(foreignBlockEnabled);
        privateBlockSwitch.setChecked(privateBlockEnabled);

        //sync settings
        syncSwitch.setChecked(syncEnabled);

        //notification settings
        notificationBlockSwitch.setChecked(notificationBlockEnabled);
        notificationAllowSwitch.setChecked(notificationAllowEnabled);

        //rest of switch block options depends on detectEnabled
        autoBlockSwitch.setEnabled(detectEnabled);
        foreignBlockSwitch.setEnabled(detectEnabled);
        privateBlockSwitch.setEnabled(detectEnabled);
        unknownBlockSwitch.setEnabled(detectEnabled);

        //SwitchOff unknownBLockEnabled if permissions are disabled
        if(unknownBlockEnabled && !hasGrantedReadContactsPermission())
        {
            unknownBlockSwitch.setChecked(false);
            //Save setting in SharedPreferences
            SharedPreferences.Editor editDataSettings = data.edit();
            editDataSettings.putBoolean("unknownBlockEnabled", false);
            editDataSettings.apply(); //commit
        }
        else unknownBlockSwitch.setChecked(unknownBlockEnabled);
    }

    /**
     * Synchronizes firebase blockings with actual local blockings.
     */
    private void syncFirebase()
    {
        DatabaseHandler db = new DatabaseHandler(getActivity());
        List<Block> myBlockings = db.getAllBlockings();

        final DatabaseReference databaseRef = FirebaseDatabase
            .getInstance()
            .getReference();

        for (final Block block: myBlockings)
        {
            //Update firebase
            Query blockingToUpdateRef = databaseRef
                    .child("blockings")
                    .orderByChild("nrDeclarantBlocked")
                    .equalTo(block.getNrDeclarant() + "_" + block.getNrBlocked())
                    .limitToFirst(1);

            blockingToUpdateRef.addListenerForSingleValueEvent(new ValueEventListener()
            {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                {
                    if(dataSnapshot.getChildren().iterator().hasNext())
                    {
                        HashMap<String, Object> updateData = new HashMap<>();
                        updateData.put("nrRating", block.getNrRating());
                        dataSnapshot.getChildren().iterator().next().getRef().updateChildren(updateData);
                    }
                    else
                    {
                        databaseRef.child("blockings").push().setValue(block);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError)
                {
                    Toast.makeText(getContext(), R.string.error, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    /**
     * Opens a window to ask for a permission to read contacts.
     */
    public void requestReadContactsPermission()
    {
        //Request the permission
        Log.e("ReadContacts", "true");
        requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, READ_CONTACTS_PERMISSION_REQUEST_CODE);
    }

    /**
     * Checks if the read contacts permission is granted.
     *
     * @return true if it is granted, false if it's are not
     */
    public boolean hasGrantedReadContactsPermission()
    {
        return ContextCompat.checkSelfPermission(Objects.requireNonNull(getActivity()), Manifest.permission.READ_CONTACTS)
                == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Runs as a result of requesting for a grant a permissions.
     *
     * @param requestCode code of the request, identify a request
     * @param permissions array of permissions
     * @param grantResults array of granted permissions
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode)
        {
            case READ_CONTACTS_PERMISSION_REQUEST_CODE:
            {
                boolean unknownBlockEnabled = false;
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    //permission was granted, we can save a allow read contacts setting in
                    //SharedPreferences
                    if (hasGrantedReadContactsPermission())
                    {
                        //Set read contact setting as true
                        unknownBlockEnabled = true;
                    }
                }
                else Toast.makeText(getActivity(), "Do blokowania nieznanych numerów potrzebujemy Twojej zgody na odczyt listy kontaktów.",
                        Toast.LENGTH_LONG).show();

                //Save setting in SharedPreferences
                unknownBlockSwitch.setChecked(unknownBlockEnabled);
                SharedPreferences.Editor editDataSettings = data.edit();
                editDataSettings.putBoolean("unknownBlockEnabled", unknownBlockEnabled);
                editDataSettings.apply(); //commit
                break;
            }
        }
    }

    /**
     * Runs on fragment resume.
     */
    @Override
    public void onResume()
    {
        super.onResume();
        Log.e("SettingsFragment","onResume() method");

        //SwitchOff unknownBLockEnabled if permissions are disabled
        boolean unknownBlockEnabled = data.getBoolean("unknownBlockEnabled", false);
        if(unknownBlockEnabled && !hasGrantedReadContactsPermission())
        {
            unknownBlockSwitch.setChecked(false);
            //Save setting in SharedPreferences
            SharedPreferences.Editor editDataSettings = data.edit();
            editDataSettings.putBoolean("unknownBlockEnabled", false);
            editDataSettings.apply(); //commit
        } else unknownBlockSwitch.setChecked(unknownBlockEnabled);
    }

    /**
     * Run on attach a this Fragment to Activity.
     *
     * @param context {@link Context} of Activity which includes a this Fragment
     */
    @Override
    public void onAttach(Context context)
    {
        Log.e("SettingsFragment","onAttach() method");

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
        Log.e("SettingsFragment","onDetach() method");
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
