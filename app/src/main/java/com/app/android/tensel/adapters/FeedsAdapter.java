package com.app.android.tensel.adapters;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.android.tensel.R;
import com.app.android.tensel.models.TradePost;
import com.app.android.tensel.models.User;
import com.app.android.tensel.ui.PostDetailActivity;
import com.app.android.tensel.utility.Utils;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.github.marlonlom.utilities.timeago.TimeAgo;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FeedsAdapter extends FirebaseRecyclerAdapter<TradePost, FeedsAdapter.MyViewHolder> {

    private Context context;
    User mUser;

    /**
     * @param modelClass      Firebase will marshall the data at a location into an instance of a class that you provide
     * @param modelLayout     This is the layout used to represent a single item in the list. You will be responsible for populating an
     *                        instance of the corresponding view with the data from an instance of modelClass.
     * @param viewHolderClass The class that hold references to all sub-views in an instance modelLayout.
     * @param ref             The Firebase location to watch for data changes. Can also be a slice of a location, using some
     *                        combination of {@code limit()}, {@code startAt()}, and {@code endAt()}.
     */
    public FeedsAdapter(Class modelClass, int modelLayout, Class viewHolderClass, Query ref, Context ctx, User user) {
        super(modelClass, modelLayout, viewHolderClass, ref);
        this.context = ctx;
        this.mUser = user;
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class MyViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.card_view)
        public CardView mCardView;
        @BindView(R.id.authorName)
        public TextView mTextView;
        @BindView(R.id.feedsAuthorImageView)
        ImageView feedsAutorImageView;
        @BindView(R.id.priceTagTextView)
        TextView priceTagTextView;
        @BindView(R.id.feedsDateTextView)
        TextView dateTextView;
        @BindView(R.id.feedsImageView)
        ImageView feedsImageView;
        @BindView(R.id.likeButton)
        ImageButton likeButton;
        @BindView(R.id.commentButton)
        ImageButton commentButton;
        @BindView(R.id.shareButton)
        ImageButton shareButton;

        public MyViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }

    }

    @Override
    public int getItemCount() {
        return super.getItemCount();
    }

    @Override
    protected void populateViewHolder(final MyViewHolder viewHolder, final TradePost model, int position) {

        Picasso.with(context)
                .load(Uri.parse(model.getVideoThumbnailUrl()))
                .placeholder(R.drawable.selling3)
                .resize(200, 200)
                .into(viewHolder.feedsImageView);
        Picasso.with(context)
                .load(Uri.parse(model.getAuthorProfileImage()))
                .resize(100,100)
                .into(viewHolder.feedsAutorImageView);

        viewHolder.mTextView.setText(String.format(Locale.ENGLISH, "By %s ", model.getAuthorName()));
        viewHolder.priceTagTextView.setText(String.format(Locale.ENGLISH ,"%d %s", model.getTradeAmount(), model.getCurrency()));
        viewHolder.dateTextView.setText(TimeAgo.using(model.getTradeTime()));

        if (model.getLikes().size() > 0 && model.getLikes().containsKey(mUser.getUserId())){
            //turn like button on
            viewHolder.likeButton.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_like_active, null));
        }
        //interaction listeners
        viewHolder.likeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Map<String, Object> likeMap = new HashMap<>();
                likeMap.put(mUser.getUserId(), model.getLikes().containsKey(mUser.getUserId()) ? null:
                        new HashMap<String, Boolean>().put(mUser.getUserId(), true));
                FirebaseDatabase.getInstance().getReference("trades")
                        .child(model.getTradePostId()).child("likes")
                        .updateChildren(likeMap);
            }
        });
        viewHolder.shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: Share article link via intent as deep link
                String id = model.getTradePostId();
            }
        });
        viewHolder.commentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), PostDetailActivity.class);
                intent.putExtra(Utils.FEED_DETAIL_ID, model.getTradePostId());

                Pair[] pairs = new Pair[3];

                pairs[0] = new Pair<View, String>(viewHolder.feedsImageView, "article_shared");
                pairs[1] = new Pair<View, String>(viewHolder.mTextView, "username_shared");
                pairs[2] = new Pair<View, String>(viewHolder.feedsAutorImageView, "profile_shared");

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ActivityOptions optionsCompat = ActivityOptions.makeSceneTransitionAnimation((Activity)context, pairs);
                    context.startActivity(intent, optionsCompat.toBundle());
                }else{
                    v.getContext().startActivity(intent);
                }
            }
        });
        viewHolder.mCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), PostDetailActivity.class);
                intent.putExtra(Utils.FEED_DETAIL_ID, model.getTradePostId());
                Pair[] pairs = new Pair[3];

                pairs[0] = new Pair<View, String>(viewHolder.feedsImageView, "article_shared");
                pairs[1] = new Pair<View, String>(viewHolder.mTextView, "username_shared");
                pairs[2] = new Pair<View, String>(viewHolder.feedsAutorImageView, "profile_shared");

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ActivityOptions optionsCompat = ActivityOptions.makeSceneTransitionAnimation((Activity)context, pairs);
                    context.startActivity(intent, optionsCompat.toBundle());
                }else{
                    v.getContext().startActivity(intent);
                }
            }
        });
    }
}