package comn.example.user.j_trok.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import comn.example.user.j_trok.R;
import comn.example.user.j_trok.adapters.ChatBaseAdapter;
import comn.example.user.j_trok.models.Chat;
import comn.example.user.j_trok.models.User;
import comn.example.user.j_trok.utility.Utils;
import uk.co.jakelee.vidsta.VidstaPlayer;
import uk.co.jakelee.vidsta.listeners.FullScreenClickListener;
import uk.co.jakelee.vidsta.listeners.VideoStateListeners;

/**
 * Created by USER on 05/05/2017.
 */
public class PostDetailActivity extends AppCompatActivity implements VideoStateListeners.OnVideoErrorListener, FullScreenClickListener {

    private static final String TAG = "PostDetailActivity";
    private boolean isSheetShown = false;
    private FirebaseAuth firebaseAuth;
    @BindView(R.id.player)
    VidstaPlayer player;
    @BindView(R.id.chatsRecyclerView)
    RecyclerView chatRecyclerView;
    private User user;

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
        user = Utils.getUserConfig(firebaseAuth.getCurrentUser());

        //setup chats RecyclerView
        FirebaseDatabase qDatabase = FirebaseDatabase.getInstance();
        chatRecyclerView.setItemAnimator(new DefaultItemAnimator());
        chatRecyclerView.setAdapter(new ChatBaseAdapter(Chat.class, R.layout.item_chat_outgoing,
                ChatBaseAdapter.ViewHolder.class, qDatabase.getReference("feeds/feed_id/chats/"), user,this));
        // Grabs a reference to the player view
        player.setVideoSource("http://www.quirksmode.org/html5/videos/big_buck_bunny.mp4");
        player.setAutoLoop(true);
        player.setAutoPlay(true);
        player.setOnVideoErrorListener(this);
        player.setOnFullScreenClickListener(this);
        // From here, the player view will show a progress indicator until the player is prepared.
        // Once it's prepared, the progress indicator goes away and the controls become enabled for the user

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
                //TODO. Perform like on Post
                return true;
            case R.id.action_chat:
                toggleBottomSheet();
                return true;
            case R.id.action_share:
                //TODO. Add correct link for app deep linking of this post page
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT,""+" \n http://traveler.cm/news");
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

    @OnClick(R.id.takePhotoButton)
    public void takePhoto(View view){
        //TODO. ADD photo to message chat
    }

    @OnClick(R.id.sendChatButton)
    public void sendChatMessage(View view){
        //TODO. Send Chat message
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
}
