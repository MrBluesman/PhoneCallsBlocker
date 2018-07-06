package com.example.ukasz.phonecallsblocker;

import android.content.Context;
import android.content.Intent;
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

import com.example.ukasz.androidsqlite.Block;
import com.example.ukasz.androidsqlite.DatabaseHandler;

import java.util.List;

/**
 * A fragment representing a list of Blocks.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class PhoneBlockFragment extends Fragment
{

    private static final String ARG_COLUMN_COUNT = "column-count";
    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;
    public static RecyclerView.Adapter adapter;
    public static List<Block> blockings; //adapter data
    DatabaseHandler db;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public PhoneBlockFragment()
    {
    }

    /**
     * Instance of PhoneBlockFragment initiator.
     * @param columnCount amount of columns which will be show on the list view.
     * @return new instance of this Fragment.
     */
    @SuppressWarnings("unused")
    public static PhoneBlockFragment newInstance(int columnCount)
    {
        PhoneBlockFragment fragment = new PhoneBlockFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    public void onResume()
    {
        // TODO Auto-generated method stub
        super.onResume();
        Log.e("PhoneBlockFragment", "onResume()");
    }

    /**
     * Runs on creating a this Fragment.
     * @param savedInstanceState saved instance state of this Fragment.
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
        {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }

        //get blockings
        db = new DatabaseHandler(getActivity());
        blockings = db.getAllBlockings();
    }

    /**
     * Creates a {@link View} using a RecyclerView Adapter
     * and a Linear or Grid Layout depends on {@param mColumnCount}.
     * @param inflater {@link LayoutInflater} which will be used to inflate a {@link View}.
     * @param container {@link ViewGroup} container.
     * @param savedInstanceState saved state of instance this Fragment.
     * @return created {@link View}.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        Log.e("PhoneBlockFragment", "onCreateView()");
        View view = inflater.inflate(R.layout.fragment_phoneblock_list, container, false);


        // Set the adapter
        if (view instanceof RecyclerView)
        {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            if (mColumnCount <= 1)
            {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            }
            else
            {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }

            adapter = new MyPhoneBlockRecyclerViewAdapter(blockings, mListener);
            adapter.notifyDataSetChanged();
            recyclerView.setAdapter(adapter);
        }

        return view;
    }

    /**
     * Runs when this Fragment is attaching to the Activity.
     * @param context App context.
     */
    @Override
    public void onAttach(Context context)
    {
        Log.e("PhoneBlockFragment", "onAttach()");
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener)
        {
            mListener = (OnListFragmentInteractionListener) context;
        }
        else
            {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }

    }

    /**
     * Runs on detaching this Fragment from the Activity.
     * disable a {@link OnListFragmentInteractionListener} for Fragment.
     */
    @Override
    public void onDetach()
    {
        Log.e("PhoneBlockFragment", "onDetach()");
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener
    {
        // TODO: Update argument type and name
        void onListFragmentInteraction(Block item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        blockings.clear();
        adapter.notifyDataSetChanged();
    }
}
