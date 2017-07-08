package com.app.android.tensel.fragments;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.SwitchCompat;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.app.android.tensel.R;
import com.app.android.tensel.models.User;
import com.app.android.tensel.utility.PrefManager;
import com.app.android.tensel.utility.Utils;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;


public class ProfileFragment extends Fragment {

    private Unbinder unbind;
    @BindView(R.id.salesCreated)
    TextView salesTextView;
    @BindView(R.id.requestsMade)
    TextView requestRequestTextView;
    @BindView(R.id.userPhoto)
    ImageView profilePhotoImageView;
    @BindView(R.id.userName)
    TextView usernameTextView;
    @BindView(R.id.userCountryTextView)
    TextView userCountryTextView;
    @BindView(R.id.emailTextView)
    TextView emailTextView;
    @BindView(R.id.phoneTextView)
    TextView phoneTextView;
    @BindView(R.id.aboutTextView)
    TextView aboutTextView;
    @BindView(R.id.locationTextView)
    TextView locationTextView;
    @BindView(R.id.commentNotificationSwitch)
    SwitchCompat commentSwitchSetting;
    @BindView(R.id.itemNotificationSwitch)
    SwitchCompat itemSwitchSetting;

    private User mAuthenticatedUser;
    private FirebaseDatabase firebaseDatabase;
    private PrefManager prefManager;

    public static ProfileFragment newInstance(FirebaseUser user) {
        ProfileFragment fragment = new ProfileFragment();
        User muser = new User();
        muser.setUserEmail(user.getEmail());
        muser.setUserName(user.getDisplayName());
        muser.setUserId(user.getUid());
        muser.setUserProfilePhoto(user.getPhotoUrl().toString());

        Bundle args = new Bundle();
        args.putSerializable(Utils.CURRENT_USER, muser);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null && mAuthenticatedUser == null){
            mAuthenticatedUser = (User) getArguments().getSerializable(Utils.CURRENT_USER);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        unbind = ButterKnife.bind(this, view);
        prefManager = new PrefManager(getActivity());

        usernameTextView.setText(mAuthenticatedUser.getUserName());
        emailTextView.setText(mAuthenticatedUser.getUserEmail());
        userCountryTextView.setText(mAuthenticatedUser.getUserCountry());
        commentSwitchSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (((SwitchCompat) view).isChecked()){
                    //save preference to turn on notifications for comments
                    Toast.makeText(getActivity(), "Comment switch", Toast.LENGTH_LONG).show();
                    prefManager.setBooleanPreference(Utils.COMMENT_NOTIFICATION_PREF, true);
                }else{
                    //Turn off preference
                    prefManager.setBooleanPreference(Utils.COMMENT_NOTIFICATION_PREF, false);
                }
            }
        });
        itemSwitchSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (((SwitchCompat) view).isChecked()){
                    //save preference to turn on notifications for new items posted for sales
                    Toast.makeText(getActivity(), "Item switch", Toast.LENGTH_LONG).show();
                    prefManager.setBooleanPreference(Utils.ITEM_NOTIFICATION_PREF, true);
                }else{
                    //turn off preference
                    prefManager.setBooleanPreference(Utils.ITEM_NOTIFICATION_PREF, false);
                }
            }
        });
        initSettings();

        Picasso.with(getContext())
                .load(mAuthenticatedUser.getUserProfilePhoto())
                //.resize(120,120)
                .placeholder(R.drawable.app_icon)
                .error(R.drawable.ic_clear)
                .into(profilePhotoImageView);

        //firebase
        firebaseDatabase = FirebaseDatabase.getInstance();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        //attach real-time listener for the user's data in firebase
        firebaseDatabase.getReference().child(Utils.DATABASE_USERS)
                .child(mAuthenticatedUser.getUserId())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        User user = dataSnapshot.getValue(User.class);
                        try {
                            mAuthenticatedUser.setUserPhoneNumber(user.getUserPhoneNumber());
                            mAuthenticatedUser.setUserStatusText(user.getUserStatusText());
                            mAuthenticatedUser.setUserCity(user.getUserCity());
                            mAuthenticatedUser.setUserCountry(user.getUserCountry());
                            mAuthenticatedUser.setSells(user.getSells());
                            mAuthenticatedUser.setBuys(user.getBuys());

                            phoneTextView.setText(user.getUserPhoneNumber());
                            aboutTextView.setText(user.getUserStatusText());
                            locationTextView.setText(user.getUserCity());
                            userCountryTextView.setText(user.getUserCity()+"-"+user.getUserCountry());
                            salesTextView.setText(String.valueOf(user.getSells()));
                            requestRequestTextView.setText(String.valueOf(user.getBuys()));
                        } catch (Exception e) {
                            e.printStackTrace();
                            FirebaseCrash.report(e.getCause());
                        }
                        //salesTextView.setText(user.getSells());
                        //requestRequestTextView.setText(user.getBuys());
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        //databaseError.toException().printStackTrace();
                    }
                });

    }

    /**
     * Restore user settings for these parameters to latest states
     */
    private void initSettings(){
        boolean setComment = prefManager.getBooleanPreference(Utils.COMMENT_NOTIFICATION_PREF, false);
        boolean setItem = prefManager.getBooleanPreference(Utils.ITEM_NOTIFICATION_PREF, false);
        itemSwitchSetting.setChecked(setItem);
        commentSwitchSetting.setChecked(setComment);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbind.unbind();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(Utils.CURRENT_USER, mAuthenticatedUser);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null)
            mAuthenticatedUser = (User) savedInstanceState.getSerializable(Utils.CURRENT_USER);
    }

    @OnClick(R.id.buttonEditContact)
    public void editContact(){
        //
        new MaterialDialog.Builder(getContext())
                .title(getString(R.string.phone_contact))
                .backgroundColor(ResourcesCompat.getColor(getResources(), R.color.bg_screen1, null))
                .icon(getResources().getDrawable(R.drawable.ic_contact_phone))
                .widgetColor(ResourcesCompat.getColor(getResources(), R.color.white, null))
                .inputType(InputType.TYPE_CLASS_PHONE)
                .inputRange(9,15, ResourcesCompat.getColor(getResources(), R.color.google_plus_red, null))
                .input(getString(R.string.update_contact), mAuthenticatedUser.getUserPhoneNumber(), false, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        Log.d("PORIFLE", ""+input);
                    }
                })
                .positiveColor(ResourcesCompat.getColor(getResources(), R.color.white, null))
                .positiveText(getString(R.string.update))
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        String contact = dialog.getInputEditText().getText().toString();
                        Map<String, Object> update = new HashMap<>();
                        update.put("userPhoneNumber", contact);
                        firebaseDatabase.getReference().child(Utils.DATABASE_USERS)
                                .child(mAuthenticatedUser.getUserId())
                                .updateChildren(update);
                    }
                })
                .show();
    }

    @OnClick(R.id.buttonEditLocation)
    public void editLocation(){
        String city = mAuthenticatedUser.getUserCity() == null ? "" : mAuthenticatedUser.getUserCity();
        String country = mAuthenticatedUser.getUserCountry() == null ? "" : mAuthenticatedUser.getUserCountry();

        new MaterialDialog.Builder(getContext())
                .title(getString(R.string.location_hint))
                .backgroundColor(ResourcesCompat.getColor(getResources(), R.color.bg_screen1, null))
                .icon(getResources().getDrawable(R.drawable.ic_location))
                .widgetColor(ResourcesCompat.getColor(getResources(), R.color.white, null))
                .inputType(InputType.TYPE_CLASS_TEXT)
                .inputRange(2,255, ResourcesCompat.getColor(getResources(), R.color.google_plus_red, null))
                .input(getString(R.string.update_location), city +"-"+country,
                        false, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        Log.d("PORIFLE", ""+input);
                    }
                })
                .positiveColor(ResourcesCompat.getColor(getResources(), R.color.white, null))
                .positiveText(getString(R.string.update))
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        String location = dialog.getInputEditText().getText().toString();
                        //Seperate town from country
                        String[] vals = location.split("[-]");
                        Map<String, Object> update = new HashMap<>();
                        update.put("userCity", vals.length > 1 ? vals[0] : location);
                        update.put("userCountry", vals.length > 1 ? vals[1] : "");
                        firebaseDatabase.getReference().child(Utils.DATABASE_USERS)
                                .child(mAuthenticatedUser.getUserId())
                                .updateChildren(update);
                        Log.d("ProfileFragement", "Location: "+vals[0]);
                    }
                })
                .show();
    }

    @OnClick(R.id.buttonEditStatus)
    public void editStatus(){
        new MaterialDialog.Builder(getContext())
                .title(getString(R.string.about))
                .backgroundColor(ResourcesCompat.getColor(getResources(), R.color.bg_screen1, null))
                .icon(getResources().getDrawable(R.drawable.ic_status))
                .widgetColor(ResourcesCompat.getColor(getResources(), R.color.white, null))
                .inputType(InputType.TYPE_CLASS_TEXT)
                .input(getString(R.string.update_status), mAuthenticatedUser.getUserStatusText(), false, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        Log.d("PORIFLE", ""+input);
                    }
                })
                .positiveColor(ResourcesCompat.getColor(getResources(), R.color.white, null))
                .positiveText(getString(R.string.update))
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                        String status = dialog.getInputEditText().getText().toString();
                        Map<String, Object> update = new HashMap<>();
                        update.put("userStatusText", status);
                        firebaseDatabase.getReference().child(Utils.DATABASE_USERS)
                                .child(mAuthenticatedUser.getUserId())
                                .updateChildren(update);
                    }
                })
                .show();
    }

}
