package com.example.ukasz.phonecallsblocker;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.ukasz.androidsqlite.RegistryBlock;


import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link RegistryBlock} and makes a call to the
 */
public class MyRegistryRecyclerViewAdapter extends RecyclerView.Adapter<MyRegistryRecyclerViewAdapter.ViewHolder>
{

    private final List<RegistryBlock> mRegistryBlockings;
    private final RegistryAdapterListener mListener;
    //Context
    private final Context mContext;

    /**
     * ViewHolder class for single {@link RegistryBlock} view.
     */
    public class ViewHolder extends RecyclerView.ViewHolder
    {
        final View mViewContainer;
        //        final TextView mIdView;
        final TextView mNrRegisteredBlock;
        final TextView mNrRating;



        /**
         * ViewHolder constructor.
         *
         * @param view single Block view
         */
        ViewHolder(View view)
        {
            super(view);
            mViewContainer = view;
//            mIdView = (TextView) view.findViewById(R.id.item_number);
            mNrRegisteredBlock = view.findViewById(R.id.registry_item_phone_number);
            mNrRating = view.findViewById(R.id.registry_item_rating);
        }


        /**
         * Converts to {@link String text}.
         *
         * @return Stringified Block number
         */
        @Override
        public String toString()
        {
            return super.toString() + " '" + mNrRegisteredBlock.getText() + "'";
        }

    }

    /**
     * Constructor for creating a {@link MyPhoneBlockRecyclerViewAdapter} instance.
     *
     * @param context context of the application
     * @param registryBlockings list of registry blockings (registered blockings)
     * @param listener {@link OnListFragmentInteractionListener listener} for catching events
     */
    MyRegistryRecyclerViewAdapter(Context context, List<RegistryBlock> registryBlockings, RegistryAdapterListener listener)
    {
        mContext = context;
        mRegistryBlockings = registryBlockings;
        mListener = listener;

    }

    /**
     * Inflates a {@link View} and attaches it to a {@link ViewHolder}
     * for single {@link RegistryBlock} instance.
     * @param parent a parent {@link ViewGroup} for get a context.
     * @param viewType type of {@link View}.
     * @return new {@link ViewHolder} for single {@link RegistryBlock} instance {@link View}.
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_registry, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Allows an actions after bind a {@link MyPhoneBlockRecyclerViewAdapter.ViewHolder}.
     *
     * @param holder {@link MyRegistryRecyclerViewAdapter.ViewHolder} instance.
     * @param position position of element on the list (One of Blocks).
     */
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position)
    {
        RegistryBlock rBlock = mRegistryBlockings.get(position);

//displaying text content of blockings
//        holder.mIdView.setText(mValues.get(position).id);
//        holder.mContentView.setText(mRegistryBlockings.get(position).getNrBlocked());

        holder.mViewContainer.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (null != mListener)
                {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
//                    mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });
    }

    @Override
    public int getItemCount()
    {
        return mRegistryBlockings.size();
    }

    /**
     * Interface for BlockAdapterListener.
     */
    public interface RegistryAdapterListener
    {
        void onBlockRowClicked(int position);

        void onRowLongClicked(int position);
    }
}
