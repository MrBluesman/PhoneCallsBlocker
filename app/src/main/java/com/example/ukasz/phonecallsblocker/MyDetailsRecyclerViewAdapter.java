package com.example.ukasz.phonecallsblocker;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ukasz.androidsqlite.Block;
import com.example.ukasz.androidsqlite.DatabaseHandler;
import com.example.ukasz.phonecallsblocker.phone_number_helper.PhoneNumberHelper;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.i18n.phonenumbers.PhoneNumberUtil;

public class MyDetailsRecyclerViewAdapter extends FirebaseRecyclerAdapter<Block, MyDetailsRecyclerViewAdapter.DetailsPhoneBlockHolder>
{
    //Context
    private final Context mContext;
    private DatabaseHandler db;

    public class DetailsPhoneBlockHolder extends RecyclerView.ViewHolder
    {
        final View mViewContainer;
        final TextView mDetailsReasonCategory;
        final TextView mDetailsDate;
        ImageView mDetailsItemIcon;

        /**
         * {@link DetailsPhoneBlockHolder} constructor.
         *
         * @param view single global details blocking view (row)
         */
        public DetailsPhoneBlockHolder(@NonNull View view)
        {
            super(view);
            mViewContainer = view;
            mDetailsReasonCategory = view.findViewById(R.id.details_item_reason_category);
            mDetailsDate = view.findViewById(R.id.details_item_date);
            mDetailsItemIcon = view.findViewById(R.id.details_item_icon);
        }
    }

    /**
     * Constructor for creating a {@link MyDetailsRecyclerViewAdapter} instance.
     *
     * @param options options for {@link FirebaseRecyclerAdapter} including query for selected values
     * @param context context of the application
     * @param db database handler for fetching data from database
     */
    MyDetailsRecyclerViewAdapter(FirebaseRecyclerOptions<Block> options, Context context, DatabaseHandler db)
    {
        super(options);
        mContext = context;
        this.db = db;
    }

    /**
     * Inflates a {@link View} and attaches it to a {@link DetailsPhoneBlockHolder}
     * for single {@link Block} instance.
     *
     * @param parent a parent {@link ViewGroup} for get a context
     * @param viewType type of {@link View}
     * @return new {@link DetailsPhoneBlockHolder} for single global details blocking view (row) instance {@link View}
     */
    @NonNull
    @Override
    public DetailsPhoneBlockHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_details_phone_block_row, parent, false);
        return new DetailsPhoneBlockHolder(view);
    }

    /**
     * Binds a single global details blocking view (row) to {@link DetailsPhoneBlockHolder}.
     * Allows an actions after bind a {@link DetailsPhoneBlockHolder}.
     *
     * @param holder {@link DetailsPhoneBlockHolder} instance
     * @param position position of element on the list (One of global details blocking view (row))
     * @param block {@link Block} instance
     */
    @Override
    protected void onBindViewHolder(@NonNull DetailsPhoneBlockHolder holder, int position, @NonNull Block block)
    {
        //Get category
        holder.mDetailsReasonCategory.setText(db.getCategory(block.getReasonCategory()));

        /* TODO: Add date when will be available */
//        holder.mDetailsDate.setText(String.valueOf(rBlock.getNrBlockingDateFormatted("MM/dd/yyyy HH:mm")));
        holder.mDetailsDate.setText(block.getNrBlocked());

        if(block.getNrRating()) holder.mDetailsItemIcon.setImageResource(R.drawable.bg_circle_negative);
        else holder.mDetailsItemIcon.setImageResource(R.drawable.bg_circle_positive);
    }
}
