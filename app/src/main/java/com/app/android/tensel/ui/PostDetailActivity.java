package com.app.android.tensel.ui;

import android.Manifest;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.android.tensel.R;
import com.app.android.tensel.adapters.ChatBaseAdapter;
import com.app.android.tensel.adapters.ParticipantsAdapter;
import com.app.android.tensel.models.Chat;
import com.app.android.tensel.models.TradePost;
import com.app.android.tensel.models.User;
import com.app.android.tensel.utility.PrefManager;
import com.app.android.tensel.utility.Utils;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.popalay.tutors.TutorialListener;
import com.popalay.tutors.Tutors;
import com.popalay.tutors.TutorsBuilder;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import berlin.volders.rxdownload.RxDownloadManager;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.functions.Action1;
import uk.co.jakelee.vidsta.VidstaPlayer;
import uk.co.jakelee.vidsta.listeners.FullScreenClickListener;
import uk.co.jakelee.vidsta.listeners.VideoStateListeners;

/**
 * Created by Larry Akah on 05/05/2017.
 */
public class PostDetailActivity extends AppCompatActivity implements VideoStateListeners.OnVideoErrorListener, FullScreenClickListener,
        VideoStateListeners.OnVideoBufferingListener, Action1<Uri>, TutorialListener {

    private static final String TAG = "PostDetailActivity";
    private static final int GRANT_WRITE_PERMISSION = 1;
    private boolean isSheetShown = false;
    private FirebaseAuth firebaseAuth;
    private BottomSheetBehavior bottomSheetBehavior;

    @BindView(R.id.player)
    VidstaPlayer player;
    @BindView(R.id.chatsRecyclerView)
    RecyclerView chatRecyclerView;
    @BindView(R.id.chatHeadsRecyclerView)
    RecyclerView participantsRecyclerView;
    @BindView(R.id.authorImageView)
    ImageView authorImageView;
    @BindView(R.id.authorNameTextView)
    TextView authorNameTextView;
    @BindView(R.id.articleDescriptionTextView)
    TextView articleDescriptionTextView;
    @BindView(R.id.chatEditTextview)
    EditText chatEditTextView;
    @BindView(R.id.bottomSheet)
    View bottomSheetView;
    /*@BindView(R.id.vplayer)
    VideoView videoView;*/

    private User user, author;
    private FirebaseDatabase qDatabase;
    private TradePost tradePost;
    private FirebaseAnalytics mFirebaseAnalytics;
    private RxDownloadManager rxDownloadManager;
    private Tutors tutors;
    private Iterator<Map.Entry<String, View>> iterator;
    //private HttpProxyCacheServer proxyCacheServer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail_main);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        firebaseAuth = FirebaseAuth.getInstance();
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

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
        chatRecyclerView.setItemAnimator(new DefaultItemAnimator());
        chatRecyclerView.setAdapter(new ChatBaseAdapter(Chat.class, R.layout.item_chat_outgoing,
                ChatBaseAdapter.ViewHolder.class, qDatabase.getReference("feeds/feed_id/chats/"), user, this));
        //proxyCacheServer = SevenApp.getProxy(PostDetailActivity.this);
        // Grabs a reference to the player view
        //player.setVideoSource("http://www.quirksmode.org/html5/videos/big_buck_bunny.mp4");
        player.setAutoLoop(false);
        player.setAutoPlay(true);
        player.setOnVideoErrorListener(this);
        player.setOnVideoBufferingListener(this);
        player.setOnFullScreenClickListener(this);
        // From here, the player view will show a progress indicator until the player is prepared.
        // Once it's prepared, the progress indicator goes away and the controls become enabled for the user

        // ATTENTION: This was auto-generated to handle app links.
        Intent appLinkIntent = getIntent();
        String appLinkAction = appLinkIntent.getAction();
        if (TextUtils.equals(appLinkAction, Intent.ACTION_VIEW)) {
            Uri appLinkData = appLinkIntent.getData();
            Log.d("PostDetail", appLinkData.getPath());
            appLinkIntent.putExtra(Utils.FEED_DETAIL_ID, appLinkData.getLastPathSegment());
         }
        loadDetails(appLinkIntent);
        getUser();

    }

    /**
     * Get list of participants and display in recycler view bottom sheet
     */
    private void getParticipants(String itemId) {
        ParticipantsAdapter participantsAdapter = new ParticipantsAdapter(this, User.class, R.layout.participant_layout,
                ParticipantsAdapter.MyViewHolder.class,
                qDatabase.getReference("participants/" + tradePost.getTradePostId()));
        participantsAdapter.setItemId(itemId);
        participantsRecyclerView.setAdapter(participantsAdapter);
    }

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
            String key = intent.getStringExtra(Utils.FEED_DETAIL_ID);
            qDatabase.getReference("trades")
                    .child(key)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            tradePost = dataSnapshot.getValue(TradePost.class);
                            try {
                                if (tradePost != null) {
                                    //player.setVideoSource(Utils.getCleanUri(tradePost.getTradeVideoUrl()));
                                    authorNameTextView.setText(tradePost.getAuthorName());
                                    articleDescriptionTextView.setText(tradePost.getTradeDescription());
                                    Picasso.with(PostDetailActivity.this)
                                            .load(Uri.parse(tradePost.getAuthorProfileImage()))
                                            .placeholder(R.drawable.selling3)
                                            .into(authorImageView);
                                    rxDownloadManager = RxDownloadManager.from(PostDetailActivity.this);
                                    String videoName = Uri.parse(tradePost.getTradeVideoUrl()).getLastPathSegment();
                                    //Log.d("VName", Utils.getDownloadedVideo(videoName).toString());

                                    if (!Utils.isVideoDownloaded(videoName)) { //download video if not yet downloaded
                                        //FOR ANDROID 6.0+ REQUEST PERMISSION TO STORE STUFF ON SECONDARY STORAGE
                                        if (ActivityCompat.checkSelfPermission(PostDetailActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                                == PackageManager.PERMISSION_GRANTED) {
                                            rxDownloadManager.download(RxDownloadManager.request(Utils.getCleanUri(tradePost.getTradeVideoUrl()),
                                                    videoName).setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN))
                                                    .subscribe(PostDetailActivity.this, new Action1<Throwable>() {
                                                        @Override
                                                        public void call(Throwable throwable) {
                                                            throwable.printStackTrace();
                                                        }
                                                    });
                                        }else{
                                            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, GRANT_WRITE_PERMISSION);
                                        }
                                    }else{
                                        call(Utils.getDownloadedVideo(videoName));
                                    }
                                    getParticipants(tradePost.getTradePostId());
                                    getAuthor(tradePost);
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
            chatRecyclerView.setAdapter(new ChatBaseAdapter(Chat.class, R.layout.item_chat_outgoing,
                    ChatBaseAdapter.ViewHolder.class, qDatabase.getReference("chats/"+key), user,this));


        }
    }

    private void getAuthor(TradePost tradePost) {
        qDatabase.getReference(Utils.DATABASE_USERS)
                .child(tradePost.getAuthorId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                author = dataSnapshot.getValue(User.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        tutors = new TutorsBuilder()
                .textColorRes(android.R.color.white)
                .shadowColorRes(R.color.shadow)
                .textSizeRes(R.dimen.textNormal)
                .completeIconRes(R.drawable.ic_cross_24_white)
                .nextButtonTextRes(R.string.action_next)
                .completeButtonTextRes(R.string.action_got_it)
                .spacingRes(R.dimen.spacingNormal)
                .lineWidthRes(R.dimen.lineWidth)
                .cancelable(true)
                .build();
        tutors.setListener(this);
        HashMap<String, View> tutorials = new HashMap<>();
        tutorials.put(getString(R.string.pv_hint), authorImageView);
        iterator = tutorials.entrySet().iterator();

        if (new PrefManager(this).getShowPvHints()) {
            showHint(iterator);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        player.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        player.stop();
    }

    @Override
    public Intent getSupportParentActivityIntent() {
        return super.getSupportParentActivityIntent();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.details_item_menu, menu);
        if (tradePost != null){
            //setup like icon
            if (tradePost.getLikes().get(user.getUserId()) == null ? false : tradePost.getLikes().get(user.getUserId())){
                //turn like button on
                menu.getItem(0).setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_like_active, null));
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.action_like:
                //Perform like on Post
                Map<String, Object> likeMap = new HashMap<>();
                likeMap.put(user.getUserId(), tradePost.getLikes().get(user.getUserId()) == null || !tradePost.getLikes().get(user.getUserId()));
                FirebaseDatabase.getInstance().getReference("trades")
                        .child(tradePost.getTradePostId()).child("likes")
                        .updateChildren(likeMap);
                item.setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_like_active, null));
                return true;
            case R.id.action_share:
                //Add correct link for app deep linking of this post page
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_string, tradePost.getAuthorName(),
                        " https://app.tensel.com/sell/"+tradePost.getTradePostId()));
                startActivity(Intent.createChooser(shareIntent, getString(R.string.share)));
                return true;
            case R.id.action_call:
                //Implement dialing author's number
                try {
                    Intent callIntent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", author.getUserPhoneNumber() == null ? "0" : author.getUserPhoneNumber().trim(), ""));
                    startActivity(callIntent);
                } catch (ActivityNotFoundException e) {
                    e.printStackTrace();
                    FirebaseCrash.report(e.getCause());
                    Utils.showMessage(this, getString(R.string.call_error));
                }
                return true;
            case R.id.action_sms:
                //Send SMS to author's phone number
                try {
                    Intent smsIntent = new Intent(Intent.ACTION_VIEW, Uri.fromParts("sms",author.getUserPhoneNumber() == null ? "0" : author.getUserPhoneNumber().trim(), ""));
                    smsIntent.putExtra("sms_body", getString(R.string.sms_message));
                    startActivity(smsIntent);
                } catch (ActivityNotFoundException e) {
                    e.printStackTrace();
                    FirebaseCrash.report(e.getCause());
                    Utils.showMessage(this, getString(R.string.sms_error));
                }
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
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

    /*@OnClick(R.id.takePhotoButton)
    public void takePhoto(View view){
        //TODO. ADD photo to message chat
    }*/

    @OnClick(R.id.authorImageView)
    public void showChatHeads(){
        // Load users/participants if author, else go to chat room
        if (tradePost.getAuthorId().equals(user.getUserId())){
            toggleBottomSheet();
        }else{
            //GOTO Chat room
            Intent intent = new Intent(this, PrivateChatActivity.class);
            intent.putExtra(Utils.FEED_DETAIL_ID, tradePost != null ? tradePost.getTradePostId() : "");
            startActivity(intent);
        }
    }

    @OnClick(R.id.sendChatButton)
    public void sendChatMessage(View view){
        //Send Chat message
        String message = chatEditTextView.getText().toString();
        if (!TextUtils.isEmpty(message)){
            //push chat
            Chat userChat = new Chat(user.getUserName(), user.getUserId(), user.getUserProfilePhoto(),
                    message, System.currentTimeMillis(), "");
            qDatabase.getReference("chats/"+tradePost.getTradePostId())
            .push().setValue(userChat);

            chatEditTextView.setText("");
            //play sound for this
            if (!player.isPlaying()){
                MediaPlayer mp = MediaPlayer.create(this, R.raw.send_sound);
                mp.setVolume(0.3f,0.4f);
                mp.start();
            }
            //subscribe FCM for messages
            FirebaseMessaging.getInstance().subscribeToTopic(tradePost.getTradePostId());
            //NOW FIRE EVENT FOR THIS CHAT
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, Utils.ANALYTICS_PARAM_CHATS_ID);
            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, Utils.ANALYTICS_PARAM_CHAT_NAME);
            bundle.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, Utils.ANALYTICS_PARAM_CHAT_CATEGORY);
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "text");
            mFirebaseAnalytics.logEvent(Utils.CHAT_EVENT, bundle);

        }else{
            chatEditTextView.setError(getString(R.string.obligatory_field));
            chatEditTextView.requestFocus();
        }
    }

    @Override
    public void OnVideoError(int what, int extra) {
        //show error
        Utils.showMessage(this, getString(R.string.error_loading_video));
    }

    @Override
    public void onToggleClick(boolean isFullscreen) {
        //full screen toggle
    }

    @Override
    public void OnVideoBuffering(VidstaPlayer evp, int buffPercent) {
        if (buffPercent == 100){
            evp.start();
        }
    }

    @Override
    public void call(Uri uri) {
        try {
            if (uri != null){
                player.setVideoSource(uri);
            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == GRANT_WRITE_PERMISSION){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                //download the video
                rxDownloadManager.download(RxDownloadManager.request(Utils.getCleanUri(tradePost.getTradeVideoUrl()),
                        Uri.parse(tradePost.getTradeVideoUrl()).getLastPathSegment())
                        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN))
                        .subscribe(PostDetailActivity.this, new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {
                                throwable.printStackTrace();
                            }
                        });
            }
        }
    }

    private void showHint(Iterator<Map.Entry<String, View>> iterator) {
        if (iterator == null){
            return;
        }
        if (iterator.hasNext()) {
            Map.Entry<String, View> data = iterator.next();
            tutors.show(getSupportFragmentManager(), data.getValue(),
                    data.getKey(),
                    !iterator.hasNext());
        }
    }

    @Override
    public void onNext() {
        showHint(iterator);
    }

    @Override
    public void onComplete() {
        tutors.close();
        new PrefManager(this).setShowPvHints(false);
    }

    @Override
    public void onCompleteAll() {
        tutors.close();
        new PrefManager(this).setShowPvHints(false);
    }
}
