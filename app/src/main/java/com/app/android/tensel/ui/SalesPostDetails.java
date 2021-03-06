package com.app.android.tensel.ui;

import android.app.ActivityOptions;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
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
import com.google.firebase.messaging.FirebaseMessaging;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;

public class SalesPostDetails extends AppCompatActivity {

    @BindView(R.id.chatsRecyclerView)
    RecyclerView chatsRecyclerview;
    @BindView(R.id.authorNameTextView)
    TextView authorNameTextView;
    @BindView(R.id.sendChatButton)
    ImageButton sendChatButton;
    @BindView(R.id.contentTextView)
    TextView contentTextView;
    @BindView(R.id.authorImageView)
    CircleImageView authorImageView;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.commentEditTextview)
    EditText commentEditText;
    @BindView(R.id.bottomSheet)
    View bottomSheetView;

    private FirebaseAuth firebaseAuth;
    private FirebaseAnalytics mFirebaseAnalytics;
    private User author, currentUser;
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

        currentUser = Utils.getUserConfig(firebaseAuth.getCurrentUser());

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
                ChatBaseAdapter.ViewHolder.class, qDatabase.getReference("/c/chats/sells"), currentUser, this));

        Intent appLinkIntent = getIntent();
        String appLinkAction = appLinkIntent.getAction();
        if (TextUtils.equals(appLinkAction, Intent.ACTION_VIEW)) {
            Uri appLinkData = appLinkIntent.getData();
            appLinkIntent.putExtra(Utils.FEED_DETAIL_ID, appLinkData.getLastPathSegment());
        }
        loadDetails(appLinkIntent);
        getUser();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
        //TODO: add menu for removing item
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home){
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
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
        participantsAdapter.setItemAuthorId(salePost.getAuthorId());
        participantsRecyclerView.setAdapter(participantsAdapter);

        if (!TextUtils.equals(salePost.getAuthorId(), currentUser.getUserId()))
            //subscribe FCM for messages
            FirebaseMessaging.getInstance().subscribeToTopic(currentUser.getUserId());
    }

    /**
     * retrieve current user and update fields
     */
    private void getUser() {
        qDatabase.getReference("users/"+currentUser.getUserId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User muser = dataSnapshot.getValue(User.class);
                try {
                    currentUser.setUserPhoneNumber(muser.getUserPhoneNumber());
                    currentUser.setBuys(muser.getBuys());
                    currentUser.setSells(muser.getSells());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /**
     * Load the detail information for this model
     * @param intent intent containing model key or parameters that can be used to retrieve the model
     */
    private void loadDetails(Intent intent) {
        if (intent != null){
            String key = intent.getStringExtra(Utils.FEED_DETAIL_ID);
            authorImageView.setEnabled(false);
            authorImageView.setActivated(false);
            qDatabase.getReference(Utils.FIREBASE_SELLS)
                    .child(key)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            salePost = dataSnapshot.getValue(SalePost.class);
                            try {
                                if (salePost != null) {
                                    //subscribe FCM for messages
                                    FirebaseMessaging.getInstance().subscribeToTopic(salePost.getPostId());
                                    //load author image profile
                                    try {
                                        Picasso.with(SalesPostDetails.this)
                                                .load(Uri.parse(salePost.getAuthorProfileImage()))
                                                .placeholder(R.drawable.app_icon)
                                                .into(authorImageView);
                                    } catch (IllegalArgumentException e) {
                                        e.printStackTrace();
                                    }
                                    authorNameTextView.setText(
                                            String.format("%s - %s", salePost.getAuthorName(),
                                                    TimeAgo.using(salePost.getTimestamp()) ));
                                    contentTextView.setText(salePost.getContent());
                                    //textViewDate.setText(TimeAgo.using(salePost.getTimestamp()));

                                    getParticipants(salePost.getPostId());
                                    getAuthor(salePost.getAuthorId());
                                }
                            }catch (NullPointerException uriEx){
                                uriEx.printStackTrace();
                                FirebaseCrash.report(uriEx.getCause());
                            }
                            authorImageView.setEnabled(true);
                            authorImageView.setActivated(true);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
            chatsRecyclerview.setAdapter(new ChatBaseAdapter(Chat.class, R.layout.item_chat_outgoing,
                    ChatBaseAdapter.ViewHolder.class, qDatabase.getReference("chats/sells/"+key), currentUser,this));

        }
    }

    /**
     * Retrieve user who is author of this post
     * @param nameID user id to retrieve user from
     */
    private void getAuthor(String nameID) {
        qDatabase.getReference(Utils.DATABASE_USERS)
                .child(nameID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        author = dataSnapshot.getValue(User.class);
                        Log.d("SalesPOSTUSER", "User: "+author.toString());
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

        if (TextUtils.equals(salePost.getAuthorId(), currentUser.getUserId())){
            toggleBottomSheet();
        }else{
            //GOTO Chat room
            Intent intent = new Intent(this, PrivateChatActivity.class);
            intent.putExtra(Utils.PROFILE_IMG, author.getUserProfilePhoto());
            intent.putExtra(Utils.AUTHOR_ID, author.getUserId());
            intent.putExtra(Utils.FEED_DETAIL_ID, salePost != null ? salePost.getPostId() : "");
            //prepare content transition
            Pair[] pairs = new Pair[2];
            pairs[0] = new Pair<View, String>(sendChatButton, "button_shared");
            pairs[1] = new Pair<View, String>(toolbar, "profile_shared");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                ActivityOptions optionsCompat = ActivityOptions.makeSceneTransitionAnimation(this, pairs);
                startActivity(intent, optionsCompat.toBundle());
            }else {
                startActivity(intent);
            }
        }
    }

    @OnClick(R.id.sendChatButton)
    public void sendCommentMessage(View view){
        //Send Chat message
        String message = commentEditText.getText().toString();
        if (!TextUtils.isEmpty(message)){
            //push chat
            Chat userChat = new Chat(currentUser.getUserName(), currentUser.getUserId(), currentUser.getUserProfilePhoto(),
                    message, System.currentTimeMillis(), "");
            qDatabase.getReference("chats/sells/"+salePost.getPostId())
                    .push().setValue(userChat);

                MediaPlayer mp = MediaPlayer.create(this, R.raw.send_sound);
                mp.setVolume(0.3f,0.4f);
                mp.start();


            //NOW FIRE EVENT FOR THIS CHAT
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, Utils.ANALYTICS_PARAM_CHATS_ID);
            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, Utils.ANALYTICS_PARAM_CHAT_NAME);
            bundle.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, Utils.ANALYTICS_PARAM_CHAT_CATEGORY);
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "text");
            mFirebaseAnalytics.logEvent(Utils.CHAT_EVENT, bundle);

            commentEditText.setText("");
            commentEditText.clearFocus();
        }else{
            commentEditText.setError(getString(R.string.obligatory_field));
            commentEditText.requestFocus();
        }
    }

}
