package com.example.ukasz.phonecallsblocker;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.ukasz.androidsqlite.Block;
import com.example.ukasz.phonecallsblocker.list_helper.FlipAnimator;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Block} and makes a call to the
 */
public class MyPhoneBlockRecyclerViewAdapter extends RecyclerView.Adapter<MyPhoneBlockRecyclerViewAdapter.ViewHolder>
{

    //List values
    private final List<Block> mBlockings;
    //Listener
//    private final OnListFragmentInteractionListener mListener;
    private final BlockAdapterListener mListener;
    //Context
    private final Context mContext;
    //Selected blockings
    private SparseBooleanArray selectedItems;

    // array used to perform multiple animation at once
    private SparseBooleanArray animationItemsIndex;
    private boolean reverseAllAnimations = false;

    // index is used to animate only the selected row
    private static int currentSelectedIndex = -1;

    /**
     * ViewHolder class for single Block view.
     */
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener
    {

        final View mViewContainer;
        //        final TextView mIdView;
        final TextView mNrBlocked;
        private ImageView mImgBlock;
        private LinearLayout mPhoneblockContainer;
        private RelativeLayout mIconContainer, mIconBack, mIconFront;


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
            mNrBlocked = view.findViewById(R.id.phoneblock_number);
            mImgBlock = view.findViewById(R.id.icon_block);
            mPhoneblockContainer = view.findViewById(R.id.phoneblock_container);
            mIconContainer = view.findViewById(R.id.icon_container);
            mIconBack = view.findViewById(R.id.icon_back);
            mIconFront = view.findViewById(R.id.icon_front);
            view.setOnLongClickListener(this);
        }


        /**
         * Converts to {@link String text}.
         *
         * @return Stringified Block number
         */
        @Override
        public String toString()
        {
            return super.toString() + " '" + mNrBlocked.getText() + "'";
        }

        /**
         * Action after long click on single Block view.
         *
         * @param v single Block view
         * @return true
         */
        @Override
        public boolean onLongClick(View v)
        {
            mListener.onRowLongClicked(getAdapterPosition());
            v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
            return true;
        }
    }

    /**
     * Constructor for creating a {@link MyPhoneBlockRecyclerViewAdapter} instance.
     *
     * @param context context of the application
     * @param blockings list of blockings (blocked numbers)
     * @param listener {@link BlockAdapterListener listener} for catching events
     */
    MyPhoneBlockRecyclerViewAdapter(Context context, List<Block> blockings, BlockAdapterListener listener)
    {
//        Log.e("blockings", blockings.toString());
        mContext = context;
        mBlockings = blockings;
        mListener = listener;
        selectedItems = new SparseBooleanArray();
        animationItemsIndex = new SparseBooleanArray();
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
                .inflate(R.layout.phoneblock_list_row, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Allows an actions after bind a {@link ViewHolder}.
     *
     * @param holder {@link ViewHolder} instance.
     * @param position position of element on the list (One of Blocks).
     */
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position)
    {
        Block block = mBlockings.get(position);

        //displaying text content of blockings
//        holder.mIdView.setText(mValues.get(position).id);
        holder.mNrBlocked.setText(mBlockings.get(position).getNrBlocked());

        //change the row state to activated
        holder.itemView.setActivated(selectedItems.get(position, false));

        //display block image
        applyBlockPicture(holder, block);

        //handle icon animation
        applyIconAnimation(holder, position, block);

        //apply click events
        applyClickEvents(holder, position);

    }

    /**
     * Applier for click events.
     * Sets up the listeners to catch click events.
     *
     * @param holder {@link ViewHolder holder} for Block at position
     * @param position position of single Block
     */
    private void applyClickEvents(ViewHolder holder, final int position)
    {
        holder.mIconContainer.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                mListener.onIconClicked(position);
            }
        });


        holder.mPhoneblockContainer.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                mListener.onBlockRowClicked(position);
            }
        });

        holder.mPhoneblockContainer.setOnLongClickListener(new View.OnLongClickListener()
        {
            @Override
            public boolean onLongClick(View view)
            {
                mListener.onRowLongClicked(position);
                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                return true;
            }
        });
    }

    /**
     * TODO: Different icons depends on blocked or allowed number (or category?)
     * Sets up the default block picture for selecting blockings to destroy.
     *
     * @param holder {@link ViewHolder holder} for Block at position
     * @param block {@link Block block} which picture will be applied
     */
    private void applyBlockPicture(ViewHolder holder, Block block)
    {
        //Select img block depends on block type (positive or negative)
        if(block.getNrRating()) holder.mImgBlock.setImageResource(R.drawable.bg_circle_negative);
        else holder.mImgBlock.setImageResource(R.drawable.bg_circle_positive);
    }

    /**
     * Animates toggle blocking using static methods from {@link FlipAnimator}.
     *
     * @param holder {@link ViewHolder holder} for Block at position
     * @param position position of single Block
     * @param block {@link Block block} which picture icon will be animated
     */
    private void applyIconAnimation(ViewHolder holder, int position, Block block)
    {
        if (selectedItems.get(position, false))
        {
            holder.mImgBlock.setImageResource(R.drawable.bg_circle);
            holder.mIconFront.setVisibility(View.GONE);
            resetIconYAxis(holder.mIconBack);
            holder.mIconBack.setVisibility(View.VISIBLE);
            holder.mIconBack.setAlpha(1);
            if (currentSelectedIndex == position)
            {
                FlipAnimator.flipView(mContext, holder.mIconBack, holder.mIconFront, true);
                resetCurrentIndex();
            }
        }
        else
        {
            //Select img block depends on block type (positive or negative)
            if(block.getNrRating()) holder.mImgBlock.setImageResource(R.drawable.bg_circle_negative);
            else holder.mImgBlock.setImageResource(R.drawable.bg_circle_positive);

            holder.mIconBack.setVisibility(View.GONE);
            resetIconYAxis(holder.mIconFront);
            holder.mIconFront.setVisibility(View.VISIBLE);
            holder.mIconFront.setAlpha(1);
            if ((reverseAllAnimations && animationItemsIndex.get(position, false)) || currentSelectedIndex == position)
            {
                FlipAnimator.flipView(mContext, holder.mIconBack, holder.mIconFront, false);
                resetCurrentIndex();
            }
        }
    }

    /**
     * Reset icon for Y axis.
     * As the views will be reused, sometimes the icon appears as
     * flipped because older view is reused. Reset the Y-axis to 0.
     *
     * @param view {@link View view} where icon has to be reset
     */
    private void resetIconYAxis(View view)
    {
        if (view.getRotationY() != 0)
        {
            view.setRotationY(0);
        }
    }

    /**
     * Resets the animation index.
     * Also setting reversing all animations to false.
     */
    public void resetAnimationIndex()
    {
        reverseAllAnimations = false;
        animationItemsIndex.clear();
    }

    /**
     * Resets current selected index.
     * Resets value is -1.
     */
    private void resetCurrentIndex()
    {
        currentSelectedIndex = -1;
    }

    /**
     * Size of {@link Block} list getter.
     *
     * @return size of {@link Block} list
     */
    @Override
    public int getItemCount()
    {
        return mBlockings.size();
    }

    /**
     * Toggles blocking at the pos.
     * Notifies item changes.
     *
     * @param pos
     */
    public void toggleSelection(int pos)
    {
        currentSelectedIndex = pos;
        if (selectedItems.get(pos, false))
        {
            selectedItems.delete(pos);
            animationItemsIndex.delete(pos);
        }
        else
        {
            selectedItems.put(pos, true);
            animationItemsIndex.put(pos, true);
        }
        notifyItemChanged(pos);
    }

    /**
     * Clear all selected blockings.
     */
    public void clearSelections()
    {
        reverseAllAnimations = true;
        selectedItems.clear();
        notifyDataSetChanged();
    }

    /**
     * Getter for already selected blockings.
     *
     * @return {@link List} of positions of selected blockings
     */
    public List<Integer> getSelectedItems()
    {
        List<Integer> items = new ArrayList<>(selectedItems.size());
        for (int i = 0; i < selectedItems.size(); i++)
        {
            items.add(selectedItems.keyAt(i));
        }
        return items;
    }

    /**
     * Select all blockings from list in {@link MyPhoneBlockRecyclerViewAdapter}.
     */
    public void selectAllItems()
    {
        for(int i = 0; i < mBlockings.size(); i++)
        {
            if(!selectedItems.get(i)) toggleSelection(i);
        }
    }

    /**
     * Getter for the first selected item.
     *
     * @return position of the first selected item
     */
    public Integer getSelectedItem()
    {
        if(getSelectedItemCount() > 0) return selectedItems.keyAt(0);
        return null;
    }

    /**
     * Getter for amount of selected blockings.
     *
     * @return amount of selected blockings
     */
    public int getSelectedItemCount()
    {
        return selectedItems.size();
    }

    /**
     * Removes blocking at the position from adapter blockings.
     *
     * @param position position of blocking do remove
     */
    public void removeData(int position)
    {
        mBlockings.remove(position);
        resetCurrentIndex();
    }

    /**
     * Interface for BlockAdapterListener.
     */
    public interface BlockAdapterListener
    {
        void onIconClicked(int position);

        void onBlockRowClicked(int position);

        void onRowLongClicked(int position);
    }

}
