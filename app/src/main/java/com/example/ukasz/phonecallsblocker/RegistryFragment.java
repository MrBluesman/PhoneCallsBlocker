package com.example.ukasz.phonecallsblocker;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.example.ukasz.androidsqlite.DatabaseHandler;
import com.example.ukasz.androidsqlite.RegistryBlock;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * A fragment representing a list of Items.
 * <p/>
 */
public class RegistryFragment extends Fragment implements MyRegistryRecyclerViewAdapter.RegistryAdapterListener
{

    private static final String ARG_COLUMN_COUNT = "column-count";
    private int mColumnCount = 1;
    private MyRegistryRecyclerViewAdapter adapter;
    private RecyclerView recyclerView;
    public static List<RegistryBlock> registryBlockings = new ArrayList<>(); //adapter data
    DatabaseHandler db;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public RegistryFragment()
    {
    }

    /**
     * Instance of {@link RegistryFragment} initiator.
     * @param columnCount amount of columns which will be show on the list view
     *
     * @return new instance of this Fragment
     */
    public static RegistryFragment newInstance(int columnCount)
    {
        RegistryFragment fragment = new RegistryFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Runs on resume application.
     * Notify data set changed.
     */
    public void onResume()
    {
        super.onResume();
        adapter.notifyDataSetChanged();
        Log.e("RegistryFragment", "onResume()");
    }

    /**
     * Runs on creating this Fragment.
     *
     * @param savedInstanceState saved instance state of this Fragment
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if (getArguments() != null)
        {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }

        //set up the DatabaseHandler
        db = new DatabaseHandler(getActivity());
    }

    /**
     * Creates a {@link View} using a {@link RecyclerView} Adapter
     * and a Linear or Grid Layout depends on {@param mColumnCount}.
     *
     * @param inflater {@link LayoutInflater} which will be used to inflate a {@link View}
     * @param container {@link ViewGroup} container
     * @param savedInstanceState saved state of instance this Fragment
     * @return created {@link View}
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        Log.e("RegistryFragment", "onCreateView()");

        //Get the root of the registry list fragment - ConstraintLayout
        View rootView = inflater.inflate(R.layout.fragment_registry_list, container, false);
        //Then get the recyclewView from rootView
        View view = rootView.findViewById(R.id.fragment_registry_list);

        // Set the adapter
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

            adapter = new MyRegistryRecyclerViewAdapter(context, registryBlockings, this);
            recyclerView.setAdapter(adapter);
            try
            {
                loadRegistryBlockings();
                adapter.notifyDataSetChanged();
            }
            catch (ParseException e)
            {
                e.printStackTrace();
            }
        }

        return rootView;
    }

    /**
     * Runs when this Fragment is attaching to the Activity.
     *
     * @param context App context.
     */
    @Override
    public void onAttach(Context context)
    {
        Log.e("RegistryFragment", "onAttach()");
        super.onAttach(context);
    }

    /**
     * Loads all blockings from database.
     */
    private void loadRegistryBlockings() throws ParseException
    {
        Log.e("RegistryFragment", "loadRegistryBlockings()");
        List<RegistryBlock> registryBlockingsToAddFromDb = db.getAllRegistryBlockings();
        Log.e("Tresc", String.valueOf(registryBlockings.size()));
        registryBlockings.clear();
        registryBlockings.addAll(registryBlockingsToAddFromDb);

        adapter.notifyDataSetChanged();
//        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
    }

    @Override
    public void onBlockRowClicked(int position)
    {

    }

    @Override
    public void onRowLongClicked(int position)
    {

    }
}
