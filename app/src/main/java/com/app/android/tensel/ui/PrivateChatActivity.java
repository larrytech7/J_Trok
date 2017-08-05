package com.app.android.tensel.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageButton;

import com.app.android.tensel.R;
import com.app.android.tensel.adapters.ChatBaseAdapter;
import com.app.android.tensel.models.Chat;
import com.app.android.tensel.models.User;
import com.app.android.tensel.utility.Utils;
import com.github.marlonlom.utilities.timeago.TimeAgo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.squareup.picasso.Picasso;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.R.attr.author;

public class PrivateChatActivity extends AppCompatActivity {

    @BindView(R.id.pvRecyclerView)
    RecyclerView mRecyclerView;
    @BindView(R.id.sendChatButton)
    ImageButton btnSendChat;
    @BindView(R.id.pvEditTextview)
    EditText pvEditTextView;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    private FirebaseAnalytics mFirebaseAnalytics;
    private User current_user;
    private FirebaseDatabase qDatabase;
    private String itemId; //the id of the item (Posted item) under consideration in the private chat
    private String targetId; //id of the user to send message to. can be seen as a shared key or common point of chat
    private String profile;
    private String itemAuthorId; //id of author of the item (post/sale)

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

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        qDatabase = FirebaseDatabase.getInstance();

        current_user = Utils.getUserConfig(firebaseAuth.getCurrentUser());

        Intent dataIntent = getIntent();

        if (dataIntent != null ){
            itemId = dataIntent.getStringExtra(Utils.FEED_DETAIL_ID);
            profile = dataIntent.getStringExtra(Utils.PROFILE_IMG);
            itemAuthorId = dataIntent.getStringExtra(Utils.AUTHOR_ID);

            if (TextUtils.equals(itemAuthorId, current_user.getUserId())) {
                //intent launched by author
                //setup actionBar/toolbar
                String userid = dataIntent.getStringExtra(Utils.USER);
                Log.e("PV", "user: "+userid);
                //toolbar.setTitle(userid != null ? user.getUserName() : getString(R.string.chat));
                //toolbar.setSubtitle(TimeAgo.using(user == null ? 0 : user.getLastUpdatedTime()));

                targetId = userid; //user.getUserId();

                mRecyclerView.setAdapter(new ChatBaseAdapter(Chat.class, R.layout.item_chat_pv_incoming,
                        ChatBaseAdapter.ViewHolder.class, qDatabase.getReference(Utils.PV)
                        .child(itemId).child(targetId), current_user,this));

            }else{
                //launched by participant [not Author]
                toolbar.setTitle("Author");
                toolbar.setSubtitle("--");
                targetId = current_user.getUserId();

                mRecyclerView.setAdapter(new ChatBaseAdapter(Chat.class, R.layout.item_chat_outgoing,
                        ChatBaseAdapter.ViewHolder.class, qDatabase.getReference(Utils.PV)
                        .child(itemId).child(targetId), current_user,this));

            }

        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("itemid", itemId );
        outState.putString("target", targetId);
        outState.putString("profile", profile);
        outState.putString("itemAuthor", itemAuthorId);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            itemId = savedInstanceState.getString("itemid");
            targetId = savedInstanceState.getString("target");
            profile = savedInstanceState.getString("profile");
            itemAuthorId = savedInstanceState.getString("itemAuthor");
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

    @Override
    public Intent getSupportParentActivityIntent() {
        return super.getSupportParentActivityIntent();
    }

    @OnClick(R.id.sendChatButton)
    public void sendChatMessage(){
        String text = pvEditTextView.getText().toString();
        if(TextUtils.isEmpty(text)){
            Snackbar.make(pvEditTextView, getString(R.string.err_empty), Snackbar.LENGTH_LONG).show();
            pvEditTextView.setError(getString(R.string.err_empty));
        }else{
            pvEditTextView.setEnabled(false);
            btnSendChat.setEnabled(false);
            //Send message
            if (itemId != null && targetId != null){
                Chat userChat = new Chat(current_user.getUserName(), current_user.getUserId(), current_user.getUserProfilePhoto(),
                        pvEditTextView.getText().toString(), System.currentTimeMillis(), "");
                //TODO: Set itemAuthorId for this item before sending chat
                userChat.setItemAuthorId(itemAuthorId);
                qDatabase.getReference("pvchats")
                        .child(itemId)
                        .child(targetId)
                        .push().setValue(userChat)
                .addOnSuccessListener(this, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        pvEditTextView.setText("");
                    }
                }).addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isComplete())
                            pvEditTextView.setEnabled(true);
                        btnSendChat.setEnabled(true);
                        pvEditTextView.setText("");
                        pvEditTextView.clearFocus();
                        mRecyclerView.scrollToPosition(mRecyclerView.getAdapter().getItemCount() - 1);
                    }
                });
                //update participant
                current_user.setLastUpdatedTime(System.currentTimeMillis());
                qDatabase.getReference("participants/"+itemId)
                        .child(current_user.getUserId())
                        .setValue(current_user);
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

    @Override
    protected void onStart() {
        super.onStart();

        //get profile picture
        new AsyncTask<String, Void, Bitmap>(){

            @Override
            protected Bitmap doInBackground(String... params) {

                Bitmap profileBitmap = null;
                try {
                    profileBitmap = Picasso.with(PrivateChatActivity.this).load(profile).error(R.mipmap.ic_launcher).get();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return profileBitmap;
            }

            @Override
            protected void onPostExecute(Bitmap bmp) {
                super.onPostExecute(bmp);
                try {
                    toolbar.setLogo(new BitmapDrawable(getResources(), bmp));
                } catch (Exception e) {
                    toolbar.setLogo(ResourcesCompat.getDrawable(getResources(), R.mipmap.ic_launcher, null));
                    e.printStackTrace();
                }
            }
        }.execute(profile);

        //Load user/peer profile to the status bar. Catch errors
        qDatabase.getReference().child(Utils.DATABASE_USERS)
                .child(TextUtils.equals(itemAuthorId, current_user.getUserId()) ? targetId : itemAuthorId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        User user = dataSnapshot.getValue(User.class);
                        if (user != null){
                            toolbar.setTitle(user.getUserName());
                            toolbar.setSubtitle(TimeAgo.using(user.getLastUpdatedTime()));
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }
}
