package comn.example.user.j_trok.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.DimenRes;
import android.support.annotation.Dimension;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.view.ViewGroupCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.easyvideoplayer.EasyVideoCallback;
import com.afollestad.easyvideoplayer.EasyVideoPlayer;

import comn.example.user.j_trok.R;

/**
 * Created by USER on 05/05/2017.
 */
public class ItemDetailActivity extends AppCompatActivity implements EasyVideoCallback {

    private static final String TAG = "ItemDetailActivity";
    private EasyVideoPlayer player;
    private BottomSheetBehavior<View> bottomSheetBehavior;
    private boolean isSheetShown = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // Grabs a reference to the player view
        player = (EasyVideoPlayer) findViewById(R.id.player);
        player.setRestartDrawableRes(R.drawable.ic_refresh);
        player.setBottomLabelText(getString(R.string.loading));
        player.setRightAction(EasyVideoPlayer.RIGHT_ACTION_SUBMIT);
        player.setSubmitText("Chat");
//        player.setCustomLabelText("Stop");
        player.setLoop(true);

        // Sets the callback to this Activity, since it inherits EasyVideoCallback
        player.setCallback(this);

        // Sets the source to the HTTP URL held in the TEST_URL variable.
        // To play files, you can use Uri.fromFile(new File("..."))
        player.setSource(Uri.parse("http://www.quirksmode.org/html5/videos/big_buck_bunny.mp4"));
        Log.e("VideoFile", ""+getIntent().getStringExtra("url"));

        // From here, the player view will show a progress indicator until the player is prepared.
        // Once it's prepared, the progress indicator goes away and the controls become enabled for the user

        //setup bottom sheet
        View bottomView = findViewById(R.id.comments_bottom_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomView);
        bottomSheetBehavior.setPeekHeight(0);
        //bottomSheetBehavior.setSkipCollapsed(true);
        bottomSheetBehavior.setHideable(true);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState){
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        bottomSheetBehavior.setPeekHeight(bottomSheet.getHeight() / 2);
                        break;
                    case BottomSheetBehavior.STATE_SETTLING | BottomSheetBehavior.STATE_DRAGGING:
                        bottomSheetBehavior.setPeekHeight(bottomSheet.getHeight() / 2);
                        break;
                    case BottomSheetBehavior.STATE_HIDDEN:
                        bottomSheetBehavior.setPeekHeight(0);
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            }

        });

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
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.action_like:
                return true;
            case R.id.action_chat:
                toggleBottomSheet();
                return true;
            case R.id.action_share:
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT,""+" \n http://traveler.cm/news");
                startActivity(Intent.createChooser(shareIntent, getString(R.string.share)));
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

    @Override
    public void onStarted(EasyVideoPlayer player) {
        Log.d(TAG, "Started");
        player.setBottomLabelText(getString(R.string.playing));
    }

    @Override
    public void onPaused(EasyVideoPlayer player) {
        Log.d(TAG, "Paused");
        player.setBottomLabelText(getString(R.string.pause));
    }

    @Override
    public void onPreparing(EasyVideoPlayer player) {
        Log.d(TAG, "Preparing");
    }

    @Override
    public void onPrepared(EasyVideoPlayer player) {
        Log.d(TAG, "Prepared");
    }

    @Override
    public void onBuffering(int percent) {
        Log.d(TAG, "buffering "+percent);

    }

    @Override
    public void onError(EasyVideoPlayer player, Exception e) {
        Log.d(TAG, "Error");
        player.setBottomLabelText(getString(R.string.error_loading_video));
        player.stop();
    }

    @Override
    public void onCompletion(EasyVideoPlayer player) {
        Log.d(TAG, "Completion");
    }

    @Override
    public void onRetry(EasyVideoPlayer player, Uri source) {
        Log.d(TAG, "Retry: "+source.getPath());
        player.setBottomLabelText(getString(R.string.retry_loading));
    }

    @Override
    public void onSubmit(final EasyVideoPlayer player, Uri source) {
        Log.d(TAG, "Submit: "+source.getPath());
        //bring up bottomSheet to show chats and allow chatting between parties
        toggleBottomSheet();
    }

}
