package com.app.android.tensel.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.app.android.tensel.R;
import com.app.android.tensel.adapters.SalesAdpater;
import com.app.android.tensel.models.User;
import com.app.android.tensel.utility.Utils;
import com.google.firebase.auth.FirebaseUser;
import com.quinny898.library.persistentsearch.SearchBox;
import com.quinny898.library.persistentsearch.SearchResult;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import static android.app.Activity.RESULT_OK;
import static com.app.android.tensel.R.layout.fragment_sell;


public class SellingFragment extends Fragment implements SearchBox.SearchListener {

    private User mAuthenticatedUser;
    private Unbinder unbinder;
    @BindView(R.id.searchbox)
    public SearchBox searchBox;

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


        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        SalesAdpater adapter = new SalesAdpater(new String[]{"Item one", "Item two", "Item three", "Item four", "Item five", "Item six", "Item Seven", "Item eight", "Item nine", "Item ten"});
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

    }

    @Override
    public void onSearch(String s) {

    }

    @Override
    public void onResultClick(SearchResult searchResult) {

    }
}