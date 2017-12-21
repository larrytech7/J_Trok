package com.app.android.tensel.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.app.android.tensel.R;
import com.app.android.tensel.fragments.BuyingFragment;
import com.app.android.tensel.fragments.ProfileFragment;
import com.app.android.tensel.fragments.SellingFragment;
import com.app.android.tensel.models.User;
import com.app.android.tensel.utility.PrefManager;
import com.app.android.tensel.utility.Utils;
import com.google.android.gms.appinvite.AppInvite;
import com.google.android.gms.appinvite.AppInviteInvitationResult;
import com.google.android.gms.appinvite.AppInviteReferral;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.joanfuentes.hintcase.HintCase;
import com.joanfuentes.hintcaseassets.contentholderanimators.FadeInContentHolderAnimator;
import com.joanfuentes.hintcaseassets.contentholderanimators.SlideInFromRightContentHolderAnimator;
import com.joanfuentes.hintcaseassets.contentholderanimators.SlideOutFromRightContentHolderAnimator;
import com.joanfuentes.hintcaseassets.hintcontentholders.SimpleHintContentHolder;
import com.joanfuentes.hintcaseassets.shapeanimators.RevealCircleShapeAnimator;
import com.joanfuentes.hintcaseassets.shapeanimators.UnrevealCircleShapeAnimator;
import com.joanfuentes.hintcaseassets.shapes.CircularShape;
import com.popalay.tutors.Tutors;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private static final String LOGTAG = "MAinActivity";
    Map<String, Fragment> fragments = new HashMap<>();
    private Tutors tutors;
    private Iterator<Map.Entry<String, View>> iterator;
    private BottomNavigationView bottomNavigationView;
    private FirebaseAnalytics mFirebaseAnalytics;
    private FirebaseAuth mAuth;
    private GoogleApiClient mgoogleApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bottomNavigationView = (BottomNavigationView)
                findViewById(R.id.navigation);
        mAuth = FirebaseAuth.getInstance();
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        bottomNavigationView.setOnNavigationItemSelectedListener
                (new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        Fragment selectedFragment;
                        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                        String fragment = "";
                        //toggleIcon(item);
                        switch (item.getItemId()) {
                            case R.id.action_buying:
                                fragment = "buying";
                                item.setIcon(R.drawable.ic_shop_selected);
                                break;
                            case R.id.action_selling:
                                fragment = "selling";
                                item.setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_sell_selected, null));
                                break;
                            case R.id.action_profile:
                                fragment = "profile";
                                item.setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_profile_selected, null));
                                break;
                        }
                        selectedFragment = fragments.get(fragment);
                        transaction.replace(R.id.frame_layout, selectedFragment, fragment)
                                .commit();
                        return true;
                    }
                });

        fragments = new HashMap<>();
        fragments.put("selling", SellingFragment.newInstance(mAuth.getCurrentUser()));
        fragments.put("buying", BuyingFragment.newInstance(mAuth.getCurrentUser()));
        fragments.put("profile", ProfileFragment.newInstance(mAuth.getCurrentUser()));
        //Manually displaying the first fragment - one time only
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frame_layout, fragments.get("buying"), "buying")
                .commit();
        mgoogleApi = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(AppInvite.API)
                .build();
        //check if app is launched from Deep Link
        boolean autoLaunchDl = false;
        AppInvite.AppInviteApi.getInvitation(mgoogleApi, this, autoLaunchDl)
                .setResultCallback(new ResultCallback<AppInviteInvitationResult>() {
                    @Override
                    public void onResult(@NonNull AppInviteInvitationResult appInviteInvitationResult) {
                        if (appInviteInvitationResult.getStatus().isSuccess()) {
                            //deep link present, we can extract
                            Intent intent = appInviteInvitationResult.getInvitationIntent();
                            String deeplink = AppInviteReferral.getDeepLink(intent);
                            //TODO: Use for referral programs and other gamification techniques (Gain more recording time)
                            FirebaseCrash.log("Deep link activated");
                        }
                    }
                });

        // ATTENTION: This was auto-generated to handle app links.
        Intent appLinkIntent = getIntent();
        String appLinkAction = appLinkIntent.getAction();

        if (TextUtils.equals(appLinkAction, "android.intent.action.VIEW")) {
            Uri appLinkData = appLinkIntent.getData();
            Log.d(LOGTAG, "data path: "+appLinkData.getPath());
            if (TextUtils.equals(appLinkData.getPath(), "/buy")){
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.frame_layout, fragments.get("buying"), "buying")
                        .commit();
            }else if (TextUtils.equals(appLinkData.getPath(), "/sell")){
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.frame_layout, fragments.get("selling"), "selling")
                        .commit();
            }else if (TextUtils.equals(appLinkData.getPath(), "/me")){
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.frame_layout, fragments.get("profile"), "profile")
                        .commit();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateUser(mAuth.getCurrentUser());
        boolean showHints = new PrefManager(this).getShouldShowHints();
        if (showHints)
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    showHint();
                }
            }, 500);
        //clear any video residues
        Utils.deleteEmptyVideos(this);
        //subscribe for new Posts
        FirebaseMessaging.getInstance().subscribeToTopic(Utils.TOPIC_FEEDS);
    }

    private void showHint() {
        SimpleHintContentHolder hintBlock = new SimpleHintContentHolder.Builder(findViewById(R.id.action_buying).getContext())
                .setContentTitle(getString(R.string.intro_title1))
                .setContentText(getString(R.string.intro_text1))
                .setTitleStyle(R.style.MaterialStyledDialogs_TitleHeader)
                .setContentStyle(R.style.MaterialStyledDialogs_Description)
                .build();
        new HintCase(findViewById(R.id.action_buying).getRootView().getRootView())
                .setTarget(findViewById(R.id.action_buying), new CircularShape(), HintCase.TARGET_IS_NOT_CLICKABLE)
                .setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.bg_screen2, null))
                .setShapeAnimators(new RevealCircleShapeAnimator(), new UnrevealCircleShapeAnimator())
                .setHintBlock(hintBlock, new FadeInContentHolderAnimator(), new SlideOutFromRightContentHolderAnimator())
                .setOnClosedListener(new HintCase.OnClosedListener() {
                    @Override
                    public void onClosed() {
                        SimpleHintContentHolder secondHintBlock = new SimpleHintContentHolder.Builder(findViewById(R.id.action_selling).getContext())
                                .setContentTitle(getString(R.string.intro_title2))
                                .setContentText(getString(R.string.intro_text2))
                                .setTitleStyle(R.style.MaterialStyledDialogs_TitleHeader)
                                .setContentStyle(R.style.MaterialStyledDialogs_Description)
                                .build();
                        new HintCase(findViewById(R.id.action_selling).getRootView().getRootView())
                                .setTarget(findViewById(R.id.action_selling), new CircularShape())
                                .setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.bg_screen3, null))
                                .setShapeAnimators(new RevealCircleShapeAnimator(), new UnrevealCircleShapeAnimator())
                                .setHintBlock(secondHintBlock, new SlideInFromRightContentHolderAnimator())
                                .setOnClosedListener(new HintCase.OnClosedListener() {
                                    @Override
                                    public void onClosed() {
                                        SimpleHintContentHolder thirdHintBlock = new SimpleHintContentHolder.Builder(findViewById(R.id.action_profile).getContext())
                                                .setContentTitle(getString(R.string.intro_title3))
                                                .setContentText(getString(R.string.intro_text3))
                                                .setTitleStyle(R.style.MaterialStyledDialogs_TitleHeader)
                                                .setContentStyle(R.style.MaterialStyledDialogs_Description)
                                                .build();
                                        new HintCase(findViewById(R.id.action_profile).getRootView().getRootView())
                                                .setTarget(findViewById(R.id.action_profile), new CircularShape())
                                                .setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.dot_dark_screen2, null))
                                                .setShapeAnimators(new RevealCircleShapeAnimator(), new UnrevealCircleShapeAnimator())
                                                .setHintBlock(thirdHintBlock, new SlideInFromRightContentHolderAnimator())
                                                .show();
                                        new PrefManager(MainActivity.this).setShouldShowHints(false);
                                        Bundle bundle = new Bundle();
                                        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, Utils.ANALYTICS_PARAM__TUTORIAL_ID);
                                        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, Utils.ANALYTICS_PARAM__TUTORIAL_NAME);
                                        bundle.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, Utils.ANALYTICS_PARAM__TUTORIAL_CATEGORY);
                                        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "text");
                                        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.TUTORIAL_COMPLETE, bundle);
                                    }
                                })
                                .show();
                    }
                })
                .show();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void updateUser(@NonNull final FirebaseUser user) throws IllegalArgumentException, NullPointerException{

        FirebaseDatabase.getInstance()
                .getReference(Utils.DATABASE_USERS)
                .child(user.getUid())
                .child("lastUpdatedTime")
                .setValue(System.currentTimeMillis());
    }
}