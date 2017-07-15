package com.app.android.tensel.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.app.android.tensel.R;
import com.app.android.tensel.adapters.ChatBaseAdapter;
import com.app.android.tensel.adapters.ParticipantsAdapter;
import com.app.android.tensel.models.Chat;
import com.app.android.tensel.models.SalePost;
import com.app.android.tensel.models.User;
import com.app.android.tensel.utility.Utils;
import com.github.marlonlom.utilities.timeago.TimeAgo;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;

public class SalesPostDetails extends AppCompatActivity {

    @BindView(R.id.chatsRecyclerView)
    RecyclerView chatsRecyclerview;
    @BindView(R.id.authorNameTextView)
    TextView authorNameTextView;
    @BindView(R.id.dateDetailTextView)
    TextView textViewDate;
    @BindView(R.id.contentTextView)
    TextView contentTextView;
    @BindView(R.id.authorImageView)
    CircleImageView authorImageView;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.bottomSheet)
    View bottomSheetView;

    private FirebaseAuth firebaseAuth;
    private FirebaseAnalytics mFirebaseAnalytics;
    private User user;
    private SalePost salePost;
    private FirebaseDatabase qDatabase;
    @BindView(R.id.chatHeadsRecyclerView)
    RecyclerView participantsRecyclerView;
    private BottomSheetBehavior<View> bottomSheetBehavior;
    private boolean isSheetShown = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sales_post_details);

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //firebase
        firebaseAuth = FirebaseAuth.getInstance();
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        qDatabase = FirebaseDatabase.getInstance();

        user = Utils.getUserConfig(firebaseAuth.getCurrentUser());

        //configure bottom sheet
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetView);
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_COLLAPSED)
                    bottomSheetBehavior.setPeekHeight(0);
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        //setup comments RecyclerView
        qDatabase = FirebaseDatabase.getInstance();
        chatsRecyclerview.setAdapter(new ChatBaseAdapter(Chat.class, R.layout.item_chat_outgoing,
                ChatBaseAdapter.ViewHolder.class, qDatabase.getReference("feeds/feed_id/chats/"), user, this));

        Intent appLinkIntent = getIntent();
        String appLinkAction = appLinkIntent.getAction();
        if (TextUtils.equals(appLinkAction, Intent.ACTION_VIEW)) {
            Uri appLinkData = appLinkIntent.getData();
            appLinkIntent.putExtra(Utils.SELL_DETAIL_ID, appLinkData.getLastPathSegment());
        }
        loadDetails(appLinkIntent);
        getUser();
    }

    @Override
    public Intent getSupportParentActivityIntent() {
        return super.getSupportParentActivityIntent();
    }

    /**
     * Get list of participants and display in recycler view bottom sheet
     */
    private void getParticipants(String itemId) {
        ParticipantsAdapter participantsAdapter = new ParticipantsAdapter(this, User.class, R.layout.participant_layout,
                ParticipantsAdapter.MyViewHolder.class,
                qDatabase.getReference("participants/" + salePost.getPostId()));
        participantsAdapter.setItemId(itemId);
        participantsRecyclerView.setAdapter(participantsAdapter);
    }

    /**
     * retrieve current user and update fields
     */
    private void getUser() {
        qDatabase.getReference("users/"+user.getUserId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User muser = dataSnapshot.getValue(User.class);
                try {
                    user.setUserPhoneNumber(muser.getUserPhoneNumber());
                    user.setBuys(muser.getBuys());
                    user.setSells(muser.getSells());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void loadDetails(Intent intent) {
        if (intent != null){
            String key = intent.getStringExtra(Utils.SELL_DETAIL_ID);
            qDatabase.getReference(Utils.FIREBASE_SELLS)
                    .child(key)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            salePost = dataSnapshot.getValue(SalePost.class);
                            try {
                                if (salePost != null) {
                                    authorNameTextView.setText(salePost.getAuthorName());
                                    contentTextView.setText(salePost.getContent());
                                    textViewDate.setText(TimeAgo.using(salePost.getTimestamp()));

                                    getParticipants(salePost.getPostId());
                                    getAuthor(salePost.getAuthorId());
                                }
                            }catch (NullPointerException uriEx){
                                uriEx.printStackTrace();
                                FirebaseCrash.report(uriEx.getCause());
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
            chatsRecyclerview.setAdapter(new ChatBaseAdapter(Chat.class, R.layout.item_chat_outgoing,
                    ChatBaseAdapter.ViewHolder.class, qDatabase.getReference("chats/"+key), user,this));


        }
    }

    private void getAuthor(String nameID) {
        qDatabase.getReference(Utils.DATABASE_USERS)
                .child(nameID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        user = dataSnapshot.getValue(User.class);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void toggleBottomSheet() {
        if(isSheetShown){
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            isSheetShown = false;
        }else{
            isSheetShown = true;
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
    }

    @OnClick(R.id.authorImageView)
    public void showChatHeads(){
        // Load users/participants if author, else go to chat room
        if (salePost.getAuthorId().contentEquals(user.getUserId())){
            toggleBottomSheet();
        }else{
            //GOTO Chat room
            Intent intent = new Intent(this, PrivateChatActivity.class);
            intent.putExtra(Utils.SELL_DETAIL_ID, salePost != null ? salePost.getPostId() : "");
            startActivity(intent);
        }
    }

}
