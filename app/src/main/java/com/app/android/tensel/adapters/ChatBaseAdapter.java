package com.app.android.tensel.adapters;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.android.tensel.R;
import com.app.android.tensel.models.Chat;
import com.app.android.tensel.models.User;
import com.app.android.tensel.utility.Utils;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.github.marlonlom.utilities.timeago.TimeAgo;
import com.google.firebase.database.DatabaseReference;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Larry Akah on 5/22/17.
 */

public class ChatBaseAdapter extends FirebaseRecyclerAdapter<Chat, ChatBaseAdapter.ViewHolder> {

    private Context context;
    private User localUser;
    private final int VIEW_TYPE_INCOMING = 1;
    private final int VIEW_TYPE_OUTGOING = -1;
    private final int VIEW_TYPE_INCOMING_WITH_IMAGE = 2;
    private final int VIEW_TYPE_OUTGOING_WITH_IMAGE = -2;
    private RecyclerView hostView;

    public ChatBaseAdapter(Class<Chat> modelClass, int modelLayout,Class<ViewHolder> viewHolderClass,
                           DatabaseReference ref, User me,Context _ctx) {
        super(modelClass, modelLayout, viewHolderClass, ref);
        this.context = _ctx;
        this.localUser = me;
        setHasStableIds(true);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType){
            case VIEW_TYPE_INCOMING: //return incoming view layout
                return new ViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_chat_incoming, parent, false));
            case VIEW_TYPE_OUTGOING: //return outgoing view layout
                return new ViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_chat_outgoing, parent, false));
            case VIEW_TYPE_INCOMING_WITH_IMAGE:
                return new ViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_chat_incoming_image, parent, false));
            case VIEW_TYPE_OUTGOING_WITH_IMAGE:
                return new ViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_chat_outgoing_image, parent, false));
        }
        return super.onCreateViewHolder(parent, viewType);
    }

    @Override
    protected void populateViewHolder(final ViewHolder viewHolder, Chat model, int position) {
        //set views data here
        viewHolder.userNameTextView.setText(model.getAuthorName());
        viewHolder.chatContentTextView.setText(model.getChatText());
        viewHolder.chatDateTimeTextview.setText(TimeAgo.using(model.getChatDateTime()));

        if (getItemViewType(position) == VIEW_TYPE_INCOMING)
            Picasso.with(context)
                    .load(model.getAuthorProfileImage())
                    .placeholder(R.drawable.app_icon)
                    .resize(70,70)
                    .into(viewHolder.userChatImageView);

        if (model.isHasImage()){
            Picasso.with(context)
                    .load(Uri.parse(model.getChatExtraImageUrl()))
                    .placeholder(R.drawable.loader)
                    //.centerCrop()
                    .resize(300, 300)
                    .into(viewHolder.itemImageView);
            /*viewHolder.itemImageView.setOnClickListener(new View.OnClickListener(){

                @Override
                public void onClick(View v) {
                    ImageView imgThumb = (ImageView) v;
                    Utils.zoomImageFromThumb(imgThumb, imgThumb.getDrawable(), getHostView(),
                            context.getResources().getInteger(android.R.integer.config_shortAnimTime));
                }
            });*/
        }

        try {
            viewHolder.userChatImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ImageView iView = (ImageView) v;
                    Utils.zoomImageFromThumb(iView, iView.getDrawable(), viewHolder.frameLayout,
                            context.getResources().getInteger(android.R.integer.config_shortAnimTime));
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemViewType(int position) {
        //Add new viewTypes when integrating images into sending chats
        Chat chat = getItem(position);
        return TextUtils.equals(chat.getAuthorId() , localUser.getUserId()) ?
                chat.isHasImage() ? VIEW_TYPE_OUTGOING_WITH_IMAGE : VIEW_TYPE_OUTGOING
                :
                chat.isHasImage() ? VIEW_TYPE_INCOMING_WITH_IMAGE : VIEW_TYPE_INCOMING;
    }

    @Override
    public void cleanup() {
        super.cleanup();
    }

    public RecyclerView getHostView() {
        return hostView;
    }

    public ChatBaseAdapter setHostView(RecyclerView hostView) {
        this.hostView = hostView;
        return this;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        @BindView(R.id.chatUsernameTextView)
        TextView userNameTextView;
        @BindView(R.id.chatContentTextView)
        TextView chatContentTextView;
        @BindView(R.id.chatDateTimeTextView)
        TextView chatDateTimeTextview;
        @Nullable @BindView(R.id.userChatPhoto)
        ImageView userChatImageView;
        @Nullable @BindView(R.id.chatItemImageView)
        ImageView itemImageView;
        @Nullable @BindView(R.id.chatFrameLayout)
        FrameLayout frameLayout;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}