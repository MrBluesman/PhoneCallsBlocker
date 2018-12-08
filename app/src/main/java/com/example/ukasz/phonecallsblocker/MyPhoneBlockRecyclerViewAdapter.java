package com.example.ukasz.phonecallsblocker;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
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
import com.example.ukasz.phonecallsblocker.phone_number_helper.PhoneNumberHelper;
import com.google.i18n.phonenumbers.PhoneNumberUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Block} and makes a call to the
 */
public class MyPhoneBlockRecyclerViewAdapter extends RecyclerView.Adapter<MyPhoneBlockRecyclerViewAdapter.PhoneBlockHolder>
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
    public class PhoneBlockHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener
    {

        final View mViewContainer;
        final TextView mNrBlocked;
        final TextView mNrBlockedSubContent;
        private ImageView mImgBlock;
        private LinearLayout mPhoneblockContainer;
        private RelativeLayout mIconContainer, mIconBack, mIconFront;

        /**
         * PhoneBlockHolder constructor.
         *
         * @param view single Block view
         */
        PhoneBlockHolder(View view)
        {
            super(view);
            mViewContainer = view;
            mNrBlocked = view.findViewById(R.id.phoneblock_number);
            mNrBlockedSubContent = view.findViewById(R.id.phoneblock_subnumber);
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
        mContext = context;
        mBlockings = blockings;
        mListener = listener;
        selectedItems = new SparseBooleanArray();
        animationItemsIndex = new SparseBooleanArray();
    }

    /**
     * Inflates a {@link View} and attaches it to a {@link PhoneBlockHolder}
     * for single {@link Block} instance.
     * @param parent a parent {@link ViewGroup} for get a context.
     * @param viewType type of {@link View}.
     * @return new {@link PhoneBlockHolder} for single {@link Block} instance {@link View}.
     */
    @NonNull
    @Override
    public PhoneBlockHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.phoneblock_list_row, parent, false);
        return new PhoneBlockHolder(view);
    }

    /**
     * Binds a single {@link} Block to {@link PhoneBlockHolder}.
     * Allows an actions after bind a {@link PhoneBlockHolder}.
     *
     * @param holder {@link PhoneBlockHolder} instance.
     * @param position position of element on the list (One of {@link Block}).
     */
    @Override
    public void onBindViewHolder(@NonNull PhoneBlockHolder holder, int position)
    {
        //Get validator phone number lib to format
        PhoneNumberHelper phoneNumberHelper = new PhoneNumberHelper();
        String contactName = phoneNumberHelper.getContactName(mContext, mBlockings.get(position).getNrBlocked());

        Block block = mBlockings.get(position);
        String phoneNumberFormatted = phoneNumberHelper.formatPhoneNumber(mBlockings.get(position).getNrBlocked(), StartActivity.COUNTRY_CODE, PhoneNumberUtil.PhoneNumberFormat.NATIONAL);

        //displaying text content of blockings
        holder.mNrBlocked.setText(contactName != null ? contactName : phoneNumberFormatted);

        holder.mNrBlockedSubContent.setText(contactName != null ? "(" + phoneNumberFormatted + ")" : "");

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
     * @param holder {@link PhoneBlockHolder holder} for Block at position
     * @param position position of single {@link Block}
     */
    private void applyClickEvents(PhoneBlockHolder holder, final int position)
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
     * Sets up the default block picture for selecting blockings to destroy.
     *
     * @param holder {@link PhoneBlockHolder holder} for Block at position
     * @param block {@link Block block} which picture will be applied
     */
    private void applyBlockPicture(PhoneBlockHolder holder, Block block)
    {
        //Select img block depends on block type (positive or negative)
        if(block.getNrRating()) holder.mImgBlock.setImageResource(R.drawable.bg_circle_negative);
        else holder.mImgBlock.setImageResource(R.drawable.bg_circle_positive);
    }

    /**
     * Animates toggle blocking using static methods from {@link FlipAnimator}.
     *
     * @param holder {@link PhoneBlockHolder holder} for Block at position
     * @param position position of single {@link Block}
     * @param block {@link Block block} which picture icon will be animated
     */
    private void applyIconAnimation(PhoneBlockHolder holder, int position, Block block)
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
     * Resets icon for Y axis.
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
    void resetAnimationIndex()
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
     * @param position item position
     */
    void toggleSelection(int position)
    {
        currentSelectedIndex = position;
        if (selectedItems.get(position, false))
        {
            selectedItems.delete(position);
            animationItemsIndex.delete(position);
        }
        else
        {
            selectedItems.put(position, true);
            animationItemsIndex.put(position, true);
        }
        notifyItemChanged(position);
    }

    /**
     * Clear all selected blockings.
     */
    void clearSelections()
    {
        reverseAllAnimations = true;
        selectedItems.clear();
        notifyDataSetChanged();
    }

    /**
     * Getter for already selected blockings as list.
     *
     * @return {@link List} of positions of selected blockings
     */
    List<Integer> getSelectedItemsAsList()
    {
        List<Integer> items = new ArrayList<>(selectedItems.size());
        for (int i = 0; i < selectedItems.size(); i++)
        {
            items.add(selectedItems.keyAt(i));
        }
        return items;
    }

    /**
     *Getter for already selected blockings as {@link SparseBooleanArray}.
     *
     * @return {@link SparseBooleanArray} of all positions with flag if the position is selected or not
     */
    SparseBooleanArray getSelectedItems()
    {
        return selectedItems;
    }

    /**
     * Getter for the first selected item.
     *
     * @return position of the first selected item
     */
    Integer getSelectedItem()
    {
        if(getSelectedItemCount() > 0) return selectedItems.keyAt(0);
        return null;
    }

    /**
     * Getter for amount of selected blockings.
     *
     * @return amount of selected blockings
     */
    int getSelectedItemCount()
    {
        return selectedItems.size();
    }

    /**
     * Removes blocking at the position from adapter blockings.
     *
     * @param position position of blocking do remove
     */
    void removeData(int position)
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
