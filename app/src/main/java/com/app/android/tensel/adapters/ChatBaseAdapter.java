package com.app.android.tensel.adapters;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.android.tensel.R;
import com.app.android.tensel.models.Chat;
import com.app.android.tensel.models.User;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.github.marlonlom.utilities.timeago.TimeAgo;
import com.google.firebase.database.DatabaseReference;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Larry Akah on 5/22/17.
 */

public class ChatBaseAdapter extends FirebaseRecyclerAdapter<Chat, ChatBaseAdapter.ViewHolder> {

    private Context context;
    private List<Chat> chats;
    private User localUser;
    private final int VIEW_TYPE_INCOMING = 1;
    private final int VIEW_TYPE_OUTGOING = -1;
    private final int VIEW_TYPE_INCOMING_WITH_IMAGE = 2;
    private final int VIEW_TYPE_OUTGOING_WITH_IMAGE = -2;

    public ChatBaseAdapter(Class<Chat> modelClass, int modelLayout,Class<ViewHolder> viewHolderClass,
                           DatabaseReference ref, User me,Context _ctx) {
        super(modelClass, modelLayout, viewHolderClass, ref);
        this.context = _ctx;
        this.chats = null;
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
        }
        return super.onCreateViewHolder(parent, viewType);
    }

    @Override
    protected void populateViewHolder(ViewHolder viewHolder, Chat model, int position) {
        //setviews data here
        viewHolder.userNameTextView.setText(model.getAuthorName());
        viewHolder.chatContentTextView.setText(model.getChatText());
        viewHolder.chatDateTimeTextview.setText(TimeAgo.using(model.getChatDateTime()));

        if (getItemViewType(position) == VIEW_TYPE_INCOMING)
            Picasso.with(context)
                    .load(model.getAuthorProfileImage())
                    .placeholder(R.drawable.app_icon)
                    .resize(70,70)
                    .into(viewHolder.userChatImageView);
    }

    @Override
    public int getItemViewType(int position) {
        //TODO: Will have to add new viewTypes when integrating images into sending chats
        Chat chat = getItem(position);
        return TextUtils.equals(chat.getAuthorId() , localUser.getUserId()) ?
                VIEW_TYPE_OUTGOING : VIEW_TYPE_INCOMING;
    }

    @Override
    public void cleanup() {
        super.cleanup();
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

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}