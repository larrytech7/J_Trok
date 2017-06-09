package com.app.android.tensel.ui;

import android.app.DownloadManager;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
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
import com.app.android.tensel.models.Chat;
import com.app.android.tensel.models.TradePost;
import com.app.android.tensel.models.User;
import com.app.android.tensel.utility.Utils;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.squareup.picasso.Picasso;

import berlin.volders.rxdownload.RxDownloadManager;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.functions.Action1;
import uk.co.jakelee.vidsta.VidstaPlayer;
import uk.co.jakelee.vidsta.listeners.FullScreenClickListener;
import uk.co.jakelee.vidsta.listeners.VideoStateListeners;

/**
 * Created by USER on 05/05/2017.
 */
public class PostDetailActivity extends AppCompatActivity implements VideoStateListeners.OnVideoErrorListener, FullScreenClickListener,
        VideoStateListeners.OnVideoBufferingListener, Action1<Uri>{

    private static final String TAG = "PostDetailActivity";
    private boolean isSheetShown = false;
    private FirebaseAuth firebaseAuth;

    @BindView(R.id.player)
    VidstaPlayer player;
    @BindView(R.id.chatsRecyclerView)
    RecyclerView chatRecyclerView;
    @BindView(R.id.authorImageView)
    ImageView authorImageView;
    @BindView(R.id.authorNameTextView)
    TextView authorNameTextView;
    @BindView(R.id.articleDescriptionTextView)
    TextView articleDescriptionTextView;
    @BindView(R.id.chatEditTextview)
    EditText chatEditTextView;
    /*@BindView(R.id.vplayer)
    VideoView videoView;*/

    private User user;
    private FirebaseDatabase qDatabase;
    private TradePost tradePost;
    private FirebaseAnalytics mFirebaseAnalytics;
    private RxDownloadManager rxDownloadManager;
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

        //setup chats RecyclerView
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
                                    Log.d("VName", Utils.getDownloadedVideo(videoName).toString());

                                    if (!Utils.isVideoDownloaded(videoName)) { //download video if not yet downloaded
                                        rxDownloadManager.download(RxDownloadManager.request(Utils.getCleanUri(tradePost.getTradeVideoUrl()),
                                                videoName).setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN))
                                                .subscribe(PostDetailActivity.this, new Action1<Throwable>() {
                                                    @Override
                                                    public void call(Throwable throwable) {
                                                        throwable.printStackTrace();
                                                    }
                                                });
                                    }else{
                                        call(Utils.getDownloadedVideo(videoName));
                                    }
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

    @Override
    protected void onPause() {
        super.onPause();
        player.pause();
        //proxyCacheServer.unregisterCacheListener(this);
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
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.action_like:
                //TODO. Perform like on Post
                return true;
            case R.id.action_share:
                //Add correct link for app deep linking of this post page
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_string, tradePost.getAuthorName(),
                        " https://app.tensel.com/sell/"+tradePost.getTradePostId()));
                startActivity(Intent.createChooser(shareIntent, getString(R.string.share)));
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void toggleBottomSheet() {
        if(isSheetShown){
            //bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            isSheetShown = false;
        }else{
            isSheetShown = true;
            //bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
    }

    /*@OnClick(R.id.takePhotoButton)
    public void takePhoto(View view){
        //TODO. ADD photo to message chat
    }*/

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
        if (uri != null){
            player.setVideoSource(uri);
        }
    }

}
