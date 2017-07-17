package com.app.android.tensel.adapters;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.app.android.tensel.R;
import com.app.android.tensel.models.SalePost;
import com.app.android.tensel.models.User;
import com.app.android.tensel.ui.SalesPostDetails;
import com.app.android.tensel.utility.Utils;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.github.marlonlom.utilities.timeago.TimeAgo;
import com.google.firebase.database.Query;

import butterknife.BindView;
import butterknife.ButterKnife;


public class SalesAdpater extends FirebaseRecyclerAdapter<SalePost, SalesAdpater.MyViewHolder>{

    private Context context;
    private User mUser;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class MyViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.sellCard)
        CardView salesCardView;
        @BindView(R.id.authorNameTextView)
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
    protected void populateViewHolder(final MyViewHolder viewHolder, final SalePost model, int position) {


        viewHolder.authorNameTextView.setText(model.getAuthorName());
        viewHolder.itemTextView.setText(model.getContent());
        viewHolder.dateTextView.setText(TimeAgo.using(model.getTimestamp()));

        viewHolder.itemTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), SalesPostDetails.class);
                intent.putExtra(Utils.FEED_DETAIL_ID, model.getPostId());
                Pair[] pairs = new Pair[3];

                pairs[0] = new Pair<View, String>(viewHolder.itemTextView, "content_shared");
                pairs[1] = new Pair<View, String>(viewHolder.authorNameTextView, "author_shared");
                pairs[2] = new Pair<View, String>(viewHolder.dateTextView, "date_shared");

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ActivityOptions optionsCompat = ActivityOptions.makeSceneTransitionAnimation((Activity)context, pairs);
                    context.startActivity(intent, optionsCompat.toBundle());
                }else{
                    v.getContext().startActivity(intent);
                }
            }
        });
        viewHolder.salesCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), SalesPostDetails.class);
                intent.putExtra(Utils.FEED_DETAIL_ID, model.getPostId());
                Pair[] pairs = new Pair[3];

                pairs[0] = new Pair<View, String>(viewHolder.itemTextView, "content_shared");
                pairs[1] = new Pair<View, String>(viewHolder.authorNameTextView, "author_shared");
                pairs[2] = new Pair<View, String>(viewHolder.dateTextView, "date_shared");

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ActivityOptions optionsCompat = ActivityOptions.makeSceneTransitionAnimation((Activity)context, pairs);
                    context.startActivity(intent, optionsCompat.toBundle());
                }else{
                    v.getContext().startActivity(intent);
                }
            }
        });

        viewHolder.salesCardView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            @Override
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                Utils.showMessage(context, "Context created");
            }
        });

        //TODO: Enable authors delete their posts
    }


}