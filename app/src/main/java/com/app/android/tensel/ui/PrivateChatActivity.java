package com.app.android.tensel.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.app.android.tensel.R;
import com.app.android.tensel.adapters.ChatBaseAdapter;
import com.app.android.tensel.models.Chat;
import com.app.android.tensel.models.User;
import com.app.android.tensel.utility.Utils;
import com.github.marlonlom.utilities.timeago.TimeAgo;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PrivateChatActivity extends AppCompatActivity {

    @BindView(R.id.pvRecyclerView)
    RecyclerView mRecyclerView;
    @BindView(R.id.sendChatButton)
    ImageButton btnSendChat;
    @BindView(R.id.pvEditTextview)
    EditText pvEditTextView;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    private FirebaseAuth firebaseAuth;
    private FirebaseAnalytics mFirebaseAnalytics;
    private User current_user;
    private FirebaseDatabase qDatabase;
    private String itemId; //the id of the item (Posted item) under consideration in the private chat
    private String targetId; //id of the user to send message to. can be seen as a shared key or common point of chat

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_private_chat);
        ButterKnife.bind(this);

        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        firebaseAuth = FirebaseAuth.getInstance();
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        qDatabase = FirebaseDatabase.getInstance();

        current_user = Utils.getUserConfig(firebaseAuth.getCurrentUser());

        Intent dataIntent = getIntent();

        if (dataIntent != null ){
            itemId = dataIntent.getStringExtra(Utils.FEED_DETAIL_ID);

            if (dataIntent.hasExtra(Utils.USER)) {
                //intent launched by author
                //setup actionBar/toolbar
                User user = (User) dataIntent.getSerializableExtra(Utils.USER);
                toolbar.setTitle(user != null ? user.getUserName() : getString(R.string.chat));
                toolbar.setSubtitle(TimeAgo.using(user == null ? 0 : user.getLastUpdatedTime()));
                ImageView img = new ImageView(this);
                img.setAdjustViewBounds(true);
                img.setLayoutParams(new ViewGroup.LayoutParams(200, 200));
                img.setMaxWidth(200);
                try {
                    Bitmap bitmap = Picasso.with(this)
                            .load(Uri.parse(user.getUserProfilePhoto()))
                            .placeholder(R.drawable.app_icon)
                            .error(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_clear, null))
                            .resize(200, 200)
                            .get();
                    toolbar.setLogo(new BitmapDrawable(getResources(), bitmap));
                } catch (Exception ex) {
                    ex.printStackTrace();
                    FirebaseCrash.report(ex.getCause());
                }
                mRecyclerView.setAdapter(new ChatBaseAdapter(Chat.class, R.layout.item_chat_pv_incoming,
                        ChatBaseAdapter.ViewHolder.class, qDatabase.getReference("pvchats")
                        .child(itemId).child(user.getUserId()), current_user,this));
                targetId = user.getUserId();
            }else{
                //launched by participant [not Author]
                mRecyclerView.setAdapter(new ChatBaseAdapter(Chat.class, R.layout.item_chat_outgoing,
                        ChatBaseAdapter.ViewHolder.class, qDatabase.getReference("pvchats")
                        .child(itemId).child(current_user.getUserId()), current_user,this));
                targetId = current_user.getUserId();

            }
            //setup recyclerView

        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.sendChatButton)
    public void sendChatMessage(){
        String text = pvEditTextView.getText().toString();
        if(TextUtils.isEmpty(text)){
            Snackbar.make(pvEditTextView, getString(R.string.err_empty), Snackbar.LENGTH_LONG).show();
            pvEditTextView.setError(getString(R.string.err_empty));
        }else{
            //Send message
            if (itemId != null && targetId != null){
                Chat userChat = new Chat(current_user.getUserName(), current_user.getUserId(), current_user.getUserProfilePhoto(),
                        pvEditTextView.getText().toString(), System.currentTimeMillis(), "");
                qDatabase.getReference("pvchats")
                        .child(itemId)
                        .child(targetId)
                        .push().setValue(userChat)
                .addOnSuccessListener(this, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        pvEditTextView.setText("");
                    }
                });
                //subscribe FCM for messages
                FirebaseMessaging.getInstance().subscribeToTopic(targetId);
                //NOW FIRE EVENT FOR THIS CHAT
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, Utils.ANALYTICS_PARAM_PV_CHATS_ID);
                bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, Utils.ANALYTICS_PARAM_PV_CHAT_NAME);
                bundle.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, Utils.ANALYTICS_PARAM_CHAT_CATEGORY);
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "text");
                mFirebaseAnalytics.logEvent(Utils.CHAT_EVENT, bundle);
            }
        }
    }

}
