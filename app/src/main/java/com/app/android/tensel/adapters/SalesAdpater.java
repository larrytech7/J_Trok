package com.app.android.tensel.adapters;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.android.tensel.R;
import com.app.android.tensel.models.SalePost;
import com.app.android.tensel.models.User;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.Query;

import butterknife.BindView;
import butterknife.ButterKnife;


public class SalesAdpater extends FirebaseRecyclerAdapter<SalePost, SalesAdpater.MyViewHolder> {

    private Context context;
    User mUser;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class MyViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.sellCard)
        CardView salesCardView;
        @Nullable @BindView(R.id.authorNameTextView)
        TextView authorNameTextView;
        @BindView(R.id.itemDescriptionTextView)
        TextView itemTextView;
        @BindView(R.id.dateTextView)
        TextView dateTextView;
        //@Nullable @BindView (R.id.itemForSale)
        //ImageView item;

        public MyViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }
    }

    @Override
    public int getItemCount() {
        return super.getItemCount();
    }

    // Create new views (invoked by the layout manager)
    @Override
    public SalesAdpater.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                                      int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_for_sale, parent, false);

        // set the view's size, margins, paddings and layout parameters
        return new MyViewHolder(v);
    }

    public SalesAdpater(Context contxt, User usr, Class<SalePost> modelClass, int modelLayout, Class<MyViewHolder> viewHolderClass, Query ref) {
        super(modelClass, modelLayout, viewHolderClass, ref);
        this.context = contxt;
        this.mUser = usr;
    }

    @Override
    protected void populateViewHolder(MyViewHolder viewHolder, SalePost model, int position) {

    }


}