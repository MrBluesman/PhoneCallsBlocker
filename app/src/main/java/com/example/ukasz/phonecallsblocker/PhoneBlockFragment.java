package com.example.ukasz.phonecallsblocker;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.ActionMode;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.ukasz.androidsqlite.Block;
import com.example.ukasz.androidsqlite.DatabaseHandler;
import com.example.ukasz.phonecallsblocker.list_helper.DividerItemDecoration;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A fragment representing a list of Blocks.
 * <p/>
 */
public class PhoneBlockFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener,
        MyPhoneBlockRecyclerViewAdapter.BlockAdapterListener
{

    private static final String ARG_COLUMN_COUNT = "column-count";
    private int mColumnCount = 1;
    private MyPhoneBlockRecyclerViewAdapter adapter;
    private RecyclerView recyclerView;
    public static List<Block> blockings = new ArrayList<>(); //adapter data
    DatabaseHandler db;
    IntentFilter intentFilter;

    private SwipeRefreshLayout swipeRefreshLayout;
    private ActionModeCallback actionModeCallback;
    private android.view.ActionMode actionMode;

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
     *
     * @return new instance of this Fragment
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

    /**
     * Runs on resume application.
     * Notify data set changed.
     */
    public void onResume()
    {
        // TODO Auto-generated method stub
        super.onResume();
        adapter.notifyDataSetChanged();
        Log.e("PhoneBlockFragment", "onResume()");
    }

    /**
     * Runs on creating a this Fragment.
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

        //create a action mode callback
        actionModeCallback = new ActionModeCallback();
    }

    /**
     * Creates a {@link View} using a RecyclerView Adapter
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
        Log.e("PhoneBlockFragment", "onCreateView()");

        //Get the root of the phoneblock list fragment - ConstraintLayout
        View rootView = inflater.inflate(R.layout.fragment_phoneblock_list, container, false);
        //Then get the recyclewView from rootView
        View view = rootView.findViewById(R.id.fragment_phoneblock_list);

        swipeRefreshLayout = rootView.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);

        //Set the adapter
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

            adapter = new MyPhoneBlockRecyclerViewAdapter(context, blockings, this);
            adapter.notifyDataSetChanged();
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            recyclerView.addItemDecoration(new DividerItemDecoration(context, LinearLayoutManager.VERTICAL));
            recyclerView.setAdapter(adapter);

            //Show loader and fetch blockings
            swipeRefreshLayout.post(
                    new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            loadBlockings();
                        }
                    }
            );
        }

        //Refresh list on every minute
        intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_TIME_TICK);

        view.getContext().registerReceiver(new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                adapter.notifyDataSetChanged();
            }
        }, intentFilter);

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
        Log.e("PhoneBlockFragment", "onAttach()");
        super.onAttach(context);
    }

    /**
     * Loads all blockings from database.
     */
    private void loadBlockings()
    {
        swipeRefreshLayout.setRefreshing(true);
        List<Block> blockingsToAddFromDb = db.getAllBlockings();

        blockings.clear();
        blockings.addAll(blockingsToAddFromDb);

        adapter.notifyDataSetChanged();
        swipeRefreshLayout.setRefreshing(false);
    }

    /**
     *  Refresh the list of blockings.
     */
    @Override
    public void onRefresh()
    {
        loadBlockings();
    }

    /**
     * Creates a options menu by inflate a {@link Menu menu} to {@link MenuInflater inflater}.
     *
     * @param menu {@link Menu menu} to inflate.
     * @param inflater {@link MenuInflater inflater}.
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_start, menu);
    }

    /**
     * Catch the selecting the menu options.
     *
     * @param item Selected option.
     * @return This method applied to superclass with this item.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.menu_action_clear_registry)
        {
            Toast.makeText(getActivity(), "Search...", Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Runs on detaching this Fragment from the Activity.
     */
    @Override
    public void onDetach()
    {
        Log.e("PhoneBlockFragment", "onDetach()");
        super.onDetach();
    }

    /**
     * Action after click a icon identified by position.
     *
     * @param position icon position
     */
    @Override
    public void onIconClicked(int position)
    {
        Log.e("onIconClicked", String.valueOf(position));
        enableActionMode(position);
    }

    /**
     * Turns on the action mode (deleting toolbar).
     *
     * @param position position of selected item do toggle
     */
    @SuppressLint("ResourceAsColor")
    private void enableActionMode(int position)
    {
        if (actionMode == null)
        {
            actionMode = Objects.requireNonNull(getActivity()).startActionMode(actionModeCallback);
        }
        toggleSelection(position);
    }


    /**
     * Action after short click (tap) a row of position.
     * Action depends on whether adapter has selected item to delete or for now is selected to
     * delete - don't select then, just run a single Block info.
     *
     * @param position position of the clicked row
     */
    @Override
    public void onBlockRowClicked(int position)
    {
        Log.e("onBlockRowClicked", String.valueOf(position));
        // verify whether action mode is enabled or not
        // if enabled, change the row state to activated
        if (adapter.getSelectedItemCount() > 0)
        {
            enableActionMode(position);
        }
        else
        {
            // read the block which removes bold from the row
            Block block = blockings.get(position);
            Intent detailsBlockIntent = new Intent(getContext(), DetailsPhoneBlock.class);
            Bundle b = new Bundle();
            b.putString("phoneNumber", block.getNrBlocked());
            detailsBlockIntent.putExtras(b);
            startActivity(detailsBlockIntent);
        }
    }

    /**
     * Action after long click a row of position.
     *
     * @param position position of the clicked row
     */
    @Override
    public void onRowLongClicked(int position)
    {
        Log.e("onRowLongClicked", String.valueOf(position));
        // long press is performed, enable action mode
        enableActionMode(position);
    }

    /**
     * Toggle selected position to another state (to delete or not).
     *
     * @param position position of the Block to toggle
     */
    private void toggleSelection(int position)
    {
        adapter.toggleSelection(position);
        int count = adapter.getSelectedItemCount();

        Menu actionMenu = actionMode.getMenu();

        if (count == 0)
        {
            actionMode.finish();
        }
        else
        {
            //Different menu actions depends on selected one or more blockings
            if(count > 1)
            {
                actionMenu.findItem(R.id.menu_action_details).setVisible(false);
                actionMenu.findItem(R.id.menu_action_edit).setVisible(false);
            }
            else
            {
                actionMenu.findItem(R.id.menu_action_details).setVisible(true);
                actionMenu.findItem(R.id.menu_action_edit).setVisible(true);
            }
            actionMode.setTitle(String.valueOf(count));
            actionMode.invalidate();
        }
    }

    /**
     * Inner class for creating a action mode after select blockings.
     */
    private class ActionModeCallback implements ActionMode.Callback
    {

        /**
         * Action on creating a action mode.
         *
         * @param mode {@link ActionMode mode}.
         * @param menu {@link Menu menu}.
         *
         * @return true
         */
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu)
        {
            mode.getMenuInflater().inflate(R.menu.menu_action_mode, menu);
            // disable swipe refresh if action mode is enabled
            swipeRefreshLayout.setEnabled(false);
            return true;
        }

        /**
         * Preparing the action mode.
         *
         * @param mode {@link ActionMode mode}.
         * @param menu {@link Menu menu}.
         *
         * @return false
         */
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu)
        {
            return false;
        }

        /**
         * Catch the action of the toolbar (action bar).
         *
         * @param mode {@link ActionMode mode}.
         * @param item {@link MenuItem item}
         * @return True if delete has clicked, false if hasn't.
         */
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item)
        {
            switch (item.getItemId())
            {
                case R.id.action_delete:
                    //delete all the selected blockings
                    //alert dialog for confirmation
                    confirmDelete(mode).show();
                    return true;
                case R.id.menu_action_set_as_negative:
                    //set all selected blockings as positive (not blocked)
                    setBlockings(true);
                    mode.finish();
                    return true;
                case R.id.menu_action_set_as_positive:
                    //set all selected blockings as positive (not blocked)
                    setBlockings(false);
                    mode.finish();
                    return true;
                default:
                    mode.finish();
                    return false;
            }
        }


        /**
         * Action after destroy action mode - deleting.
         * Resets adapter animation and clear previous selected items.
         *
         * @param mode {@link ActionMode mode}.
         */
        @Override
        public void onDestroyActionMode(ActionMode mode)
        {
            adapter.clearSelections();
            swipeRefreshLayout.setEnabled(true);
            actionMode = null;
            recyclerView.post(new Runnable()
            {
                @Override
                public void run()
                {
                    adapter.resetAnimationIndex();
                    // mAdapter.notifyDataSetChanged();
                }
            });
        }
    }

    /**
     * Deletes the blocking from {@link RecyclerView}.
     */
    private void deleteBlockings()
    {
        adapter.resetAnimationIndex();
        List<Integer> selectedItemPositions =
                adapter.getSelectedItems();
        for (int i = selectedItemPositions.size() - 1; i >= 0; i--)
        {
            int positionToDelete = selectedItemPositions.get(i);
            Block b = blockings.get(positionToDelete);
            db.deleteBlocking(b);
            adapter.removeData(positionToDelete);
        }
        adapter.notifyDataSetChanged();
    }

    /**
     * Sets up the rating of selected blockings.
     * @param rating rating which will be set up for all selected blockings
     *               true if block, false if allow
     */
    private void setBlockings(boolean rating)
    {
        adapter.resetAnimationIndex();
        List<Integer> selectedItemPositions =
                adapter.getSelectedItems();
        for (int i = selectedItemPositions.size() - 1; i >= 0; i--)
        {
            int positionToChange = selectedItemPositions.get(i);
            Block b = blockings.get(positionToChange);
            b.setNrRating(rating);
            db.updateBlocking(b);
        }
        adapter.notifyDataSetChanged();
    }

    /**
     * Creates a confirmation {@link AlertDialog} for deleting selected blockings from list.
     *
     * @param mode {@link ActionMode mode} to finish after positive deleting
     * @return {@link AlertDialog} with confirmation for deleting selected blockings
     */
    private AlertDialog confirmDelete(final ActionMode mode)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        //Add the buttons
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                deleteBlockings();
                mode.finish();
            }
        });

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which) {}
        });

        builder.setMessage(R.string.phone_block_fragment_delete_confirm)
                .setTitle(R.string.phone_block_fragment_delete_confirm_title);

        return builder.create();
    }
}
