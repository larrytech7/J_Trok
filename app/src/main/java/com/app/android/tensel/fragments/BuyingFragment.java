package com.app.android.tensel.fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import com.adroitandroid.chipcloud.ChipCloud;
import com.adroitandroid.chipcloud.ChipListener;
import com.afollestad.materialcamera.MaterialCamera;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.app.android.tensel.Manifest;
import com.app.android.tensel.R;
import com.app.android.tensel.adapters.FeedsAdapter;
import com.app.android.tensel.models.TradePost;
import com.app.android.tensel.models.User;
import com.app.android.tensel.utility.PrefManager;
import com.app.android.tensel.utility.Utils;
import com.app.android.tensel.utility.videocompression.MediaController;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.github.javiersantos.materialstyleddialogs.enums.Style;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.popalay.tutors.TutorialListener;
import com.popalay.tutors.Tutors;
import com.popalay.tutors.TutorsBuilder;
import com.quinny898.library.persistentsearch.SearchBox;
import com.quinny898.library.persistentsearch.SearchResult;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import static android.app.Activity.RESULT_OK;

public class BuyingFragment extends Fragment implements TutorialListener, SearchBox.SearchListener {

    private static final String LOGTAG = "BuyingFragment";
    private final static int CAMERA_RQ_VIDEO = 6969;
    private static final int MANAGE_MEDIA_CONTENT = 8088;
    private Unbinder unbinder;
    private Tutors tutors;
    private Iterator<Map.Entry<String, View>> iterator;
    private User mAuthenticatedUser;
    @BindView(R.id.searchbox)
    public SearchBox search;
    @BindView(R.id.recycler_view)
    public RecyclerView recyclerView;
    @BindView(R.id.empty_view)
    public LinearLayout emptyLayout;
    private FirebaseDatabase firebaseDatabase;
    private RecyclerView.AdapterDataObserver observer;
    private FeedsAdapter adapter;

    public static BuyingFragment newInstance(FirebaseUser user) {
        BuyingFragment fragment = new BuyingFragment();
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
        Utils.deleteEmptyVideos(getActivity());
        if (getArguments() != null && mAuthenticatedUser == null){
            mAuthenticatedUser = (User) getArguments().getSerializable(Utils.CURRENT_USER);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_buy, container, false);
        unbinder = ButterKnife.bind(this, rootView);
        //firebase
        firebaseDatabase = FirebaseDatabase.getInstance();
        //configure recycler view
        observer = new RecyclerView.AdapterDataObserver() {

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                if (positionStart > 0){
                    emptyLayout.setVisibility(View.GONE);
                }
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                super.onItemRangeRemoved(positionStart, itemCount);
                if (positionStart > 0){
                    emptyLayout.setVisibility(View.GONE);
                }else
                    emptyLayout.setVisibility(View.VISIBLE);
            }
        };

        recyclerView.setHasFixedSize(true);
        adapter = new FeedsAdapter(TradePost.class, R.layout.custom_view, FeedsAdapter.MyViewHolder.class,
                firebaseDatabase.getReference("trades").orderByChild("tradeTime"), getActivity(), mAuthenticatedUser);
        adapter.registerAdapterDataObserver(observer);
        recyclerView.setAdapter(adapter);
        if (adapter.getItemCount() > 0 ){
            emptyLayout.setVisibility(View.GONE);
        }else{
            emptyLayout.setVisibility(View.VISIBLE);
        }

        //configure search
        search = (SearchBox) rootView.findViewById(R.id.searchbox);
        String[] categories = getResources().getStringArray(R.array.categories);
        for(String category : categories){
            SearchResult option = new SearchResult(category, getResources().getDrawable(R.drawable.ic_history));
            search.addSearchable(option);
        }
        search.setAnimateDrawerLogo(true);
        search.setLogoText(getString(R.string.search, mAuthenticatedUser.getUserName()));
        search.setLogoTextColor(R.color.bg_screen1);
        //search.setMenuVisibility(SearchBox.GONE);
        search.enableVoiceRecognition(this);
        search.setHint(getString(R.string.search_hint));
        search.setSearchListener(this);

        return rootView;
    }

    @OnClick(R.id.fabCreatePost)
    public void startPostCreate(){

        /*new MaterialCamera(this)
                .qualityProfile(MaterialCamera.QUALITY_HIGH)
                .countdownSeconds(new PrefManager(getActivity()).getVideoRecordingDuration())
                .saveDir(Utils.getVideoDirPath(getActivity()))
                .start(CAMERA_RQ_VIDEO);*/
        //use device camera app
//        videoUploadUri = Utils.getVideoFileForUpload(getActivity());
        if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(),
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            startVideoCapture();
        }else{
            requestPermissions(new String[]{android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, MANAGE_MEDIA_CONTENT);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MANAGE_MEDIA_CONTENT)
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                startVideoCapture();
            }else{
                Utils.showMessage(getActivity(), "Permission: "+permissions[0]+" result: "+grantResults[0]);
            }
    }

    private void startVideoCapture(){
        Intent videoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        videoIntent.putExtra(MediaStore.EXTRA_OUTPUT, Utils.getVideoDirPath(getActivity()));
        videoIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, new PrefManager(getActivity()).getVideoRecordingDuration());
        startActivityForResult(videoIntent, CAMERA_RQ_VIDEO);
    }

    @Override
    public void onStart() {
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
        tutorials.put(getString(R.string.message_sell), getActivity().findViewById(R.id.fabCreatePost));
        iterator = tutorials.entrySet().iterator();
        boolean showHints = new PrefManager(getActivity()).getShouldShowHints();
        //Check preference if first time so as to know if to show hints or not
        if (showHints)
            showHint(iterator);
        //attach real-time listener for the user's data in firebase
        firebaseDatabase.getReference().child(Utils.DATABASE_USERS)
                .child(mAuthenticatedUser.getUserId())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        User user = dataSnapshot.getValue(User.class);
                        if (user != null){
                            mAuthenticatedUser.setSells(user.getSells());
                            mAuthenticatedUser.setBuys(user.getBuys());
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        //databaseError.toException().printStackTrace();
                    }
                });

    }

    private void showHint(Iterator<Map.Entry<String, View>> iterator) {
        if (iterator == null){
            return;
        }
        if (iterator.hasNext()) {
            Map.Entry<String, View> data = iterator.next();
            tutors.show(getFragmentManager(), data.getValue(),
                    data.getKey(),
                    !iterator.hasNext());
        }
    }

    /**
     * Show dialog to fill in details for this post
     * @param filePath path to file to be uploaded with details filled-in
     * @param videoFileNumber the number of the video file generated in MediaStore
     */
    private void showPublishDialog(final String filePath, final String videoFileNumber){

        View view = View.inflate(getActivity(), R.layout.publish_video_post, null);
        final CheckBox auctionCheckBox = (CheckBox) view.findViewById(R.id.auctionChoiceCheckbox);
        final SeekBar auctionPeriodSeekBar = (SeekBar) view.findViewById(R.id.seekBar);

        auctionCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    auctionPeriodSeekBar.setVisibility(View.VISIBLE);
                }else{
                    auctionPeriodSeekBar.setVisibility(View.GONE);
                    auctionPeriodSeekBar.setProgress(0);
                }
            }
        });
        auctionPeriodSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //update the checkbox text with the number of hours selected
                String checkboxtext = getString(R.string.checkbox_auction_string);
                String updatedText = String.format(Locale.ENGLISH, "%s. Expires in %d Hour(s)", checkboxtext, progress);
                auctionCheckBox.setText(updatedText);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        final String[] categories = getResources().getStringArray(R.array.categories);
        final List<String> selectedCategories = new ArrayList<>();

        final ChipCloud categoryChip = (ChipCloud) view.findViewById(R.id.category_chip_cloud);
        new ChipCloud.Configure()
                .chipCloud(categoryChip)
                .labels(categories)
        .selectTransitionMS(500)
        .deselectTransitionMS(250)
        .chipListener(new ChipListener() {
            @Override
            public void chipSelected(int i) {
                // SAVE VALUE of selected tag
                selectedCategories.add(categories[i]);
            }

            @Override
            public void chipDeselected(int i) {
                //Remove value from list of selected tags
                selectedCategories.remove(categories[i]);
            }
        }).build();
        new MaterialStyledDialog.Builder(getActivity())
                .setStyle(Style.HEADER_WITH_ICON)
                .setIcon(R.mipmap.ic_send)
                .setHeaderColor(R.color.bg_screen1)
                .withIconAnimation(true)
                .setDescription(getString(R.string.publish_description))
                .setCustomView(view)
                .setScrollable(true)
                .setPositiveText(getString(R.string.publish))
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        // collect values and publish to firebase
                        //String pTitle = ((EditText) dialog.findViewById(R.id.postTitleEditText)).getText().toString();
                        String pDesc = ((EditText) dialog.findViewById(R.id.postDescEditText)).getText().toString();
                        //String pPrice = ((EditText) dialog.findViewById(R.id.priceEditTextView)).getText().toString();
                        //String pLocation = ((EditText) dialog.findViewById(R.id.locationEditTextView)).getText().toString();
                        if (!TextUtils.isEmpty(pDesc)) {

                            TradePost tradePost = new TradePost();
                            tradePost.setTradeNameTitle(pDesc);
                            tradePost.setTradeDescription(pDesc);
                            tradePost.setTradeAmount(Utils.fetchPrice(pDesc).getAmount()); //fetch price from description
                            tradePost.setCurrency(Utils.fetchPrice(pDesc).getCurrency()); //fetch currency from description
                            tradePost.setTradeLocation(mAuthenticatedUser.getUserCity()); //assume location is user's city
                            tradePost.setTags(selectedCategories);
                            tradePost.setVideoThumbnailUrl(videoFileNumber); //temporal filepath
                            tradePost.setTradeVideoUrl(filePath);
                            tradePost.setLikes(new HashMap<String, Boolean>());
                            tradePost.setAuction(auctionCheckBox.isChecked());
                            tradePost.setAuctionDuration(auctionPeriodSeekBar.getProgress());
                            publishPost(tradePost);
                        }else{
                            //publish error,warning about missing fields
                            Toast.makeText(getContext(), getString(R.string.mcam_error), Toast.LENGTH_SHORT).show();
                            dialog.show();
                        }
                    }
                })
                .setNegativeText(getString(R.string.cancel))
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                })
                .setCancelable(false)
                .show();
    }

    /**
     * Upload post resource to firebase
     * @param tradePost
     */
    private void publishPost(final TradePost tradePost) {
        //create video thumbnail and upload post
        try {
            //configure tradepost author
            tradePost.setAuthorId(mAuthenticatedUser.getUserId());
            tradePost.setAuthorName(mAuthenticatedUser.getUserName());
            tradePost.setAuthorProfileImage(mAuthenticatedUser.getUserProfilePhoto());
            tradePost.setTradeTime(System.currentTimeMillis());

            //send broadcast
            Intent publishIntent = new Intent(Utils.PUBLISH_ITEM_INTENT);
            publishIntent.putExtra(Utils.CURRENT_USER, mAuthenticatedUser);
            publishIntent.putExtra(Utils.ITEM_TRADE_POST, tradePost);
            getActivity().sendBroadcast(publishIntent);

        } catch (Throwable throwable) {
            throwable.printStackTrace();
            FirebaseCrash.report(throwable);
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        //search.toggleSearch();
        search.clearResults();
        search.clearFocus();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        adapter.unregisterAdapterDataObserver(observer);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Received recording or error from MaterialCamera
        if (requestCode == CAMERA_RQ_VIDEO) {

            if (resultCode == RESULT_OK) {
                String filePath = data.getDataString();
                final String[] videoFilenumber = filePath.split("/");

                Log.d(LOGTAG, "Saved to: " + filePath);
                try {
                    //compress video at this point
                    new AsyncTask<Uri, Integer, Boolean>() {
                        private ProgressDialog mProgressDialog;

                        @Override
                        protected Boolean doInBackground(Uri... paths) {
                            Log.e(LOGTAG, "path: " + paths[0].getPath());
                            int progress = 0;
                            boolean converted = false;
                            try {
                                converted = MediaController.getInstance()
                                        .convertVideo(Utils.getFilePath(getActivity(), paths[0]),
                                                new File(Utils.getVideoDirPath(getActivity())));
                                publishProgress(++progress);
                            } catch (URISyntaxException e) {
                                e.printStackTrace();
                            }
                            return converted;
                        }

                        @Override
                        protected void onProgressUpdate(Integer... values) {
                            super.onProgressUpdate(values);
                            //Update progressdialog
                            Log.d(LOGTAG, "Video compression Progress ... " + (values[0] / 100) * 100 );
                            mProgressDialog.setProgress((values[0]/100) * 100 );
                        }

                        @Override
                        protected void onPreExecute() {
                            super.onPreExecute();
                            mProgressDialog = new ProgressDialog(getActivity());
                            mProgressDialog.setIndeterminate(false);
                            mProgressDialog.setProgress(0);
                            mProgressDialog.setMessage(getString(R.string.preparing));
                            mProgressDialog.setCancelable(false);
                            mProgressDialog.setCanceledOnTouchOutside(false);
                            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                            mProgressDialog.show();
                        }

                        @Override
                        protected void onPostExecute(Boolean isConverted) {
                            mProgressDialog.dismiss();
                            if (isConverted) {
                                //log converted path
                                Log.d(LOGTAG, "Compressed Video Path: " + MediaController.cachedFile.getPath());
                                //upload this version of the file to the cloud. Here'd be a suitable place to call the showPublishDialog method
                                showPublishDialog(MediaController.cachedFile.getPath(), videoFilenumber[videoFilenumber.length -1]);
                            }
                            super.onPostExecute(isConverted);
                        }
                    }.execute(data.getData());
                }catch (Exception ex){
                    FirebaseCrash.report(ex.getCause());
                }

            } else if(data != null) {
                Exception e = (Exception) data.getSerializableExtra(MaterialCamera.ERROR_EXTRA);
                e.printStackTrace();
                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
        if (isAdded() && requestCode == SearchBox.VOICE_RECOGNITION_CODE && resultCode == RESULT_OK) {
            ArrayList<String> matches = data
                    .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            search.populateEditText(matches.get(0));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onNext() {
        showHint(iterator);
    }

    @Override
    public void onComplete() {
        tutors.close();
        new PrefManager(getActivity()).setShouldShowHints(false);
    }

    @Override
    public void onCompleteAll() {
        tutors.close();
        new PrefManager(getActivity()).setShouldShowHints(false);
    }

    @Override
    public void onSearchOpened() {

    }

    @Override
    public void onSearchCleared() {

    }

    @Override
    public void onSearchClosed() {
        //
    }

    @Override
    public void onSearchTermChanged(String s) {
        //Apply real-time update of the Adapter in the recyclerview
        FeedsAdapter adapter = new FeedsAdapter(TradePost.class, R.layout.custom_view, FeedsAdapter.MyViewHolder.class,
                firebaseDatabase.getReference("trades")
                        .orderByChild("tradeNameTitle")
                        .startAt(s).endAt(s+"\uf8ff"), getActivity(), mAuthenticatedUser);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onSearch(String s) {
        //Toast.makeText(getActivity(), "REGEX: "+Utils.fetchPrice(s), Toast.LENGTH_LONG).show();
        //Build new adapter and replace current one
        FeedsAdapter adapter = new FeedsAdapter(TradePost.class, R.layout.custom_view, FeedsAdapter.MyViewHolder.class,
                firebaseDatabase.getReference("trades")
                        .orderByChild("tradeNameTitle")
                        .startAt(s)
                        .endAt(s+"\uf8ff"), getActivity(), mAuthenticatedUser);
        if (adapter.getItemCount() > 0)
            recyclerView.setAdapter(adapter);
        else
            recyclerView.setAdapter(new FeedsAdapter(TradePost.class, R.layout.custom_view, FeedsAdapter.MyViewHolder.class,
                    firebaseDatabase.getReference("trades")
                            .orderByChild("tradeNameTitle")
                            .startAt("[a-zA-Z0-9]*")
                            .endAt(s), getActivity(), mAuthenticatedUser));
    }

    @Override
    public void onResultClick(SearchResult searchResult) {
        //perform search with data from the searchResult object
        //Toast.makeText(getActivity(), "Clicked: "+searchResult.toString(), Toast.LENGTH_LONG).show();
        FeedsAdapter adapter = new FeedsAdapter(TradePost.class, R.layout.custom_view, FeedsAdapter.MyViewHolder.class,
                firebaseDatabase.getReference("trades")
                        .orderByChild("tradeNameTitle")
                        .startAt(searchResult.toString())
                        .endAt(searchResult.toString()+"\uf8ff"), getActivity(), mAuthenticatedUser);
        recyclerView.setAdapter(adapter);
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
}