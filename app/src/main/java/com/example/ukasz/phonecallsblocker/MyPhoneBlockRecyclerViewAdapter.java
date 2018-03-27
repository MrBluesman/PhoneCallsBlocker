package com.example.ukasz.phonecallsblocker;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.ukasz.androidsqlite.Block;
import com.example.ukasz.phonecallsblocker.PhoneBlockFragment.OnListFragmentInteractionListener;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Block} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class MyPhoneBlockRecyclerViewAdapter extends RecyclerView.Adapter<MyPhoneBlockRecyclerViewAdapter.ViewHolder>
{

    //List values
    private final List<Block> mValues;
    //Listener
    private final OnListFragmentInteractionListener mListener;

    /**
     * Constructor for creating a {@link MyPhoneBlockRecyclerViewAdapter} instance.
     * @param items List of Blocks which should be displayed on list by this Adapter.
     * @param listener {@link OnListFragmentInteractionListener}.
     */
    MyPhoneBlockRecyclerViewAdapter(List<Block> items, OnListFragmentInteractionListener listener)
    {
        mValues = items;
        mListener = listener;
    }

    /**
     * Inflates a {@link View} and attaches it to a {@link ViewHolder}
     * for single {@link Block} instance.
     * @param parent a parent {@link ViewGroup} for get a context.
     * @param viewType type of {@link View}.
     * @return new {@link ViewHolder} for single {@link Block} instance {@link View}.
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_phoneblock, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Allows an actions after bind a {@link ViewHolder}.
     * @param holder {@link ViewHolder} instance.
     * @param position position of element on the list (One of Blocks).
     */
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position)
    {
        holder.mItem = mValues.get(position);
//        holder.mIdView.setText(mValues.get(position).id);
//        holder.mItem
        holder.mNrBlocked.setText(mValues.get(position).getNrBlocked());

        holder.mView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (null != mListener)
                {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });
    }

    /**
     * Size of {@link Block} list getter.
     * @return size of {@link Block} list.
     */
    @Override
    public int getItemCount()
    {
        return mValues.size();
    }

    /**
     * ViewHolder class for single Block view.
     */
    public class ViewHolder extends RecyclerView.ViewHolder
    {

        final View mView;
//        final TextView mIdView;
        final TextView mNrBlocked;
        public Block mItem;

        ViewHolder(View view)
        {
            super(view);
            mView = view;
//            mIdView = (TextView) view.findViewById(R.id.item_number);
            mNrBlocked = view.findViewById(R.id.fragment_phoneblock_nr_blocked);
        }

        @Override
        public String toString()
        {
            return super.toString() + " '" + mNrBlocked.getText() + "'";
        }
    }
}
