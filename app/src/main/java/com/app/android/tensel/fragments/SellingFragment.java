package com.app.android.tensel.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.app.android.tensel.R;
import com.app.android.tensel.adapters.SalesAdpater;
import com.app.android.tensel.models.SalePost;
import com.app.android.tensel.models.User;
import com.app.android.tensel.utility.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.quinny898.library.persistentsearch.SearchBox;
import com.quinny898.library.persistentsearch.SearchResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import static android.app.Activity.RESULT_OK;
import static com.app.android.tensel.R.layout.fragment_sell;


public class SellingFragment extends Fragment implements SearchBox.SearchListener {

    private User mAuthenticatedUser;
    private Unbinder unbinder;
    @BindView(R.id.empty_view)
    LinearLayout emptyView;
    @BindView(R.id.searchbox)
    public SearchBox searchBox;
    @BindView(R.id.recycler_view)
    public RecyclerView recyclerView;
    private DatabaseReference mDatabaseRef;
    private SalesAdpater adapter;
    private RecyclerView.AdapterDataObserver observer;

    public static SellingFragment newInstance(FirebaseUser user) {
        SellingFragment fragment = new SellingFragment();
        User muser = new User();
        muser.setUserEmail(user.getEmail());
        muser.setUserName(user.getDisplayName());
        muser.setUserCountry("");
        muser.setUserCity("");
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
        View rootView = inflater.inflate(fragment_sell, container, false);
        unbinder = ButterKnife.bind(this, rootView);

        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        //observer
        observer = new RecyclerView.AdapterDataObserver() {

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                if (itemCount > 0){
                    emptyView.setVisibility(View.GONE);
                }else{
                    emptyView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                super.onItemRangeRemoved(positionStart, itemCount);
                if (itemCount > 0){
                    emptyView.setVisibility(View.GONE);
                }else{
                    emptyView.setVisibility(View.VISIBLE);
                }
            }

        };

        recyclerView.setHasFixedSize(true);
        adapter = new SalesAdpater(getActivity(), mAuthenticatedUser, SalePost.class,
                R.layout.item_for_sale,
                SalesAdpater.MyViewHolder.class,
                mDatabaseRef.child(Utils.FIREBASE_SELLS).orderByChild("timestamp"));
        adapter.registerAdapterDataObserver(observer);
        recyclerView.setAdapter(adapter);

        String[] categories = getResources().getStringArray(R.array.categories);
        for(String category : categories){
            SearchResult option = new SearchResult(category, getResources().getDrawable(R.drawable.ic_history));
            searchBox.addSearchable(option);
        }
        searchBox.setAnimateDrawerLogo(true);
        searchBox.setLogoText(getString(R.string.search_selling, mAuthenticatedUser.getUserName()));
        searchBox.setLogoTextColor(R.color.bg_screen1);
        //search.setMenuVisibility(SearchBox.GONE);
        searchBox.enableVoiceRecognition(this);
        searchBox.setHint(getString(R.string.search_hint));
        searchBox.setSearchListener(this);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        searchBox.toggleSearch();
        searchBox.clearResults();
        searchBox.clearFocus();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        adapter.unregisterAdapterDataObserver(observer);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (isAdded() && requestCode == SearchBox.VOICE_RECOGNITION_CODE && resultCode == RESULT_OK) {
            ArrayList<String> matches = data
                    .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            searchBox.populateEditText(matches.get(0));
        }
    }

    @OnClick(R.id.buttonRequestStuff)
    public void sellStuff(){
        //post stuff for sell
        new MaterialDialog.Builder(getContext())
                .title(getString(R.string.title_selling))
                .backgroundColor(ResourcesCompat.getColor(getResources(), R.color.bg_screen1, null))
                .icon(getResources().getDrawable(R.drawable.ic_status))
                .widgetColor(ResourcesCompat.getColor(getResources(), R.color.white, null))
                .inputType(InputType.TYPE_TEXT_VARIATION_LONG_MESSAGE)
                .input(getString(R.string.desc_selling), mAuthenticatedUser.getUserStatusText(), false, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        Log.d("SELLING", ""+input);
                    }
                })
                .positiveColor(ResourcesCompat.getColor(getResources(), R.color.white, null))
                .positiveText(getString(R.string.send))
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                        String request = dialog.getInputEditText().getText().toString();
                        SalePost salePost = new SalePost(mAuthenticatedUser.getUserName(), mAuthenticatedUser.getUserId(), request, System.currentTimeMillis());
                        Map<String, Object> update = new HashMap<>();
                        update.put("buys", mAuthenticatedUser.getBuys()+1);
                        mDatabaseRef.child(Utils.DATABASE_USERS)
                                .child(mAuthenticatedUser.getUserId())
                                .updateChildren(update);
                        String id = mDatabaseRef.child(Utils.FIREBASE_SELLS)
                                .push().getKey();
                        salePost.setPostId(id);
                        //subscribe FCM for messages
                        FirebaseMessaging.getInstance().subscribeToTopic(id);

                        salePost.setAuthorProfileImage(mAuthenticatedUser.getUserProfilePhoto());
                        mDatabaseRef.child(Utils.FIREBASE_SELLS)
                                .child(id)
                                .setValue(salePost)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isComplete())
                                    Utils.showMessage(getActivity(), getString(R.string.success));
                            }
                        });
                    }
                })
                .show();

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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onSearchOpened() {

    }

    @Override
    public void onSearchCleared() {

    }

    @Override
    public void onSearchClosed() {

    }

    @Override
    public void onSearchTermChanged(String s) {
        SalesAdpater adapter = new SalesAdpater(getActivity(), mAuthenticatedUser, SalePost.class,
                R.layout.item_for_sale,
                SalesAdpater.MyViewHolder.class,
                mDatabaseRef.child(Utils.FIREBASE_SELLS)
                        .orderByChild("content")
                        .startAt(s)
                        .endAt(s+"\uf8ff"));
        recyclerView.setAdapter(adapter);

    }

    @Override
    public void onSearch(String s) {
        SalesAdpater adapter = new SalesAdpater(getActivity(), mAuthenticatedUser, SalePost.class,
                R.layout.item_for_sale,
                SalesAdpater.MyViewHolder.class,
                mDatabaseRef.child(Utils.FIREBASE_SELLS)
                        .orderByChild("content")
                        .startAt(s)
                        .endAt(s+"\uf8ff"));
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onResultClick(SearchResult searchResult) {
        SalesAdpater adapter = new SalesAdpater(getActivity(), mAuthenticatedUser, SalePost.class,
                R.layout.item_for_sale,
                SalesAdpater.MyViewHolder.class,
                mDatabaseRef.child(Utils.FIREBASE_SELLS)
                        .orderByChild("content")
                        .startAt(searchResult.toString())
                        .endAt(searchResult.toString()+"\uf8ff"));
        recyclerView.setAdapter(adapter);
    }
}