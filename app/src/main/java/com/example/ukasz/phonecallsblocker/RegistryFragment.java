package com.example.ukasz.phonecallsblocker;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
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
import android.widget.PopupMenu;

import com.example.ukasz.androidsqlite.DatabaseHandler;
import com.example.ukasz.androidsqlite.RegistryBlock;
import com.example.ukasz.phonecallsblocker.list_helper.DividerItemDecoration;

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
    private static MyRegistryRecyclerViewAdapter adapter;
    private RecyclerView recyclerView;
    public static List<RegistryBlock> registryBlockings = new ArrayList<>(); //adapter data
    private static DatabaseHandler db;

    private boolean hasItemMenuOpened;

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

        //Options available in this fragment
        setHasOptionsMenu(true);

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
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            recyclerView.addItemDecoration(new DividerItemDecoration(context, LinearLayoutManager.VERTICAL));
            recyclerView.setAdapter(adapter);

            //Refresh data
            loadRegistryBlockings();
            adapter.notifyDataSetChanged();
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
     * Creates a options menu by inflate a {@link Menu menu} to {@link MenuInflater inflater}.
     * Options menu dedicated only for {@link RegistryFragment}.
     *
     * @param menu {@link Menu menu} to inflate
     * @param inflater {@link MenuInflater inflater}
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        menu.clear();
        inflater.inflate(R.menu.menu_registry, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    /**
     * Catch the selected options menu.
     *
     * @param item selected option
     * @return true
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        Log.d("OPTIONS SELECTED:", "Fragment.onOptionsItemSelected");
        //noinspection SimplifiableIfStatement
        if (id == R.id.menu_action_clear_registry)
        {
            confirmClearRegistryBlockings().show();
            return true;
        }
        return true;
    }


    /**
     * Loads all blockings from database.
     */
    public static void loadRegistryBlockings()
    {
        Log.e("RegistryFragment", "loadRegistryBlockings()");
        List<RegistryBlock> registryBlockingsToAddFromDb = null;
        try
        {
            registryBlockingsToAddFromDb = db.getAllRegistryBlockings();
        }
        catch (ParseException e)
        {
            e.printStackTrace();
        }
        registryBlockings.clear();
        if (registryBlockingsToAddFromDb != null) registryBlockings.addAll(registryBlockingsToAddFromDb);
        adapter.notifyDataSetChanged();
    }

    /**
     * Clears a registry blockings.
     */
    public void clearRegistryBlockings()
    {
        db.clearRegistryBlockings();
        RegistryFragment.loadRegistryBlockings();
    }

    /**
     * Creates a confirmation {@link android.app.AlertDialog} for clear registry blockings..
     *
     * @return {@link android.app.AlertDialog} with confirmation for deleting selected blockings
     */
    private android.app.AlertDialog confirmClearRegistryBlockings()
    {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getActivity());

        //Add the buttons
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                clearRegistryBlockings();
            }
        });

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which) {}
        });

        builder.setMessage(R.string.phone_block_fragment_clear_registry_confirm)
                .setTitle(R.string.phone_block_fragment_clear_registry_confirm_title);

        return builder.create();
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
    }

    /**
     * Action after short click (tap) a row of position.
     *
     * @param position position of the clicked row
     */
    public void onRegistryRowClicked(int position)
    {
        // read the block which removes bold from the row
        RegistryBlock registryBlock = registryBlockings.get(position);
        startDetailsActivityForBlocking(registryBlock.getNrBlocked());
    }

    /**
     * Action after long click a row of position.
     *
     * @param position position of the clicked row
     * @param view view of the item clicked (container)
     */
    @Override
    public void onRowLongClicked(int position, View view)
    {
        //create and show menu for registry item
        createRegistryItemMenu(position, getContext(), view).show();
    }

    /**
     * Action after click a options menu for the row identified by position.
     *
     * @param position icon position
     * @param view view of the item clicked (container)
     */
    @Override
    public void onOptionsClicked(int position, View view)
    {
        //create and show menu for registry item
        createRegistryItemMenu(position, getContext(), view).show();
    }

    /**
     * Builds a {@link PopupMenu} for single registry item {@link RegistryBlock}.
     *
     * @param position position of selected registry item on the list
     * @param ctx Context of the app
     * @param view view of the item clicked (container)
     * @return built {@link PopupMenu} for single registry item {@link RegistryBlock}
     */
    private PopupMenu createRegistryItemMenu(final int position, Context ctx, final View view)
    {
        //toggle item activation state
        adapter.toggleActivation(position);

        PopupMenu registerItemPopupMenu = new PopupMenu(ctx, view);

        //inflating menu from xml resource
        registerItemPopupMenu.inflate(R.menu.menu_registry_item);
        //adding click listener
        registerItemPopupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
        {
            RegistryBlock registryBlock = registryBlockings.get(position);
            @Override
            public boolean onMenuItemClick(MenuItem item)
            {
                switch (item.getItemId())
                {
                    case R.id.menu_action_details:
                        startDetailsActivityForBlocking(registryBlock.getNrBlocked());
                        //toggle item activation state
                        adapter.toggleActivation(position);
                        break;
                    case R.id.menu_action_delete:
                        db.deleteRegistryBlocking(registryBlock);
                        RegistryFragment.loadRegistryBlockings();
                        break;
                    case R.id.menu_action_delete_all_related:
                        db.deleteRegistryBlockings(registryBlock);
                        RegistryFragment.loadRegistryBlockings();
                        break;
                }

                //toggle item activation state
                adapter.toggleActivation(position);

                return false;
            }
        });

        //On dismiss/cancel listener to toggle item activation after dismiss menu
        registerItemPopupMenu.setOnDismissListener(new PopupMenu.OnDismissListener()
        {
            @Override
            public void onDismiss(PopupMenu menu)
            {
                //toggle item activation state
                adapter.toggleActivation(position);
            }
        });

        return registerItemPopupMenu;
    }

    /**
     * Starts a {@link DetailsPhoneBlock} activity for the selected registry blocking.
     *
     * @param phoneNumber phone number of the selected blocking
     */
    private void startDetailsActivityForBlocking(String phoneNumber)
    {
        Intent detailsBlockIntent = new Intent(getContext(), DetailsPhoneBlock.class);
        Bundle b = new Bundle();
        b.putString("phoneNumber", phoneNumber);
        detailsBlockIntent.putExtras(b);
        startActivity(detailsBlockIntent);
    }
}

