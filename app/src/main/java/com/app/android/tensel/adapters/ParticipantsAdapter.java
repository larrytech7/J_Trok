package com.app.android.tensel.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.app.android.tensel.R;
import com.app.android.tensel.models.User;
import com.app.android.tensel.ui.PrivateChatActivity;
import com.app.android.tensel.utility.Utils;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.github.marlonlom.utilities.timeago.TimeAgo;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Larry Akah on 6/27/17.
 */

public class ParticipantsAdapter extends FirebaseRecyclerAdapter<User, ParticipantsAdapter.MyViewHolder> {

    /**
     * @param modelClass      Firebase will marshall the data at a location into an instance of a class that you provide
     * @param modelLayout     This is the layout used to represent a single item in the list. You will be responsible for populating an
     *                        instance of the corresponding view with the data from an instance of modelClass.
     * @param viewHolderClass The class that hold references to all sub-views in an instance modelLayout.
     * @param ref             The Firebase location to watch for data changes. Can also be a slice of a location, using some
     *                        combination of {@code limit()}, {@code startAt()}, and {@code endAt()}.
     */
    private Context context;
    private String itemId;

    public ParticipantsAdapter(Context c, Class<User> modelClass, int modelLayout, Class<MyViewHolder> viewHolderClass, Query ref) {
        super(modelClass, modelLayout, viewHolderClass, ref);
        this.context = c;
    }

    @Override
    protected void populateViewHolder(MyViewHolder viewHolder, User model, int position) {

        final User user = model;
        Picasso.with(context)
                .load(Uri.parse(model.getUserProfilePhoto()))
                .placeholder(R.drawable.app_icon)
                .resize(200, 200)
                .into(viewHolder.profileImageView);
        viewHolder.usernameTextView.setText(model.getUserName());
        viewHolder.dateTextView.setText(TimeAgo.using(model.getLastUpdatedTime()));
        //listener
        viewHolder.profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //start chat activity
                Intent intent = new Intent(context, PrivateChatActivity.class);
                intent.putExtra(Utils.USER, user.getUserId());
                intent.putExtra(Utils.FEED_DETAIL_ID, itemId);
                context.startActivity(intent);
            }
        });
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{

        @BindView(R.id.profileImageView)
        CircleImageView profileImageView;
        @BindView(R.id.usernameTextView)
        TextView usernameTextView;
        @BindView(R.id.dateTextview)
        TextView dateTextView;

        public MyViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
