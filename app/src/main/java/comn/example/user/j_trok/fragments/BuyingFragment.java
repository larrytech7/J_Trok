package comn.example.user.j_trok.fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.adroitandroid.chipcloud.ChipCloud;
import com.adroitandroid.chipcloud.ChipListener;
import com.afollestad.materialcamera.MaterialCamera;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.github.javiersantos.materialstyleddialogs.enums.Style;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import comn.example.user.j_trok.R;
import comn.example.user.j_trok.adapters.FeedsAdapter;
import comn.example.user.j_trok.models.TradePost;
import comn.example.user.j_trok.models.User;
import comn.example.user.j_trok.utility.PrefManager;
import comn.example.user.j_trok.utility.Utils;
import comn.example.user.j_trok.utility.videocompression.MediaController;

import static android.app.Activity.RESULT_OK;

public class BuyingFragment extends Fragment implements TutorialListener, SearchBox.SearchListener {

    private static final String LOGTAG = "BuyingFragment";
    private final static int CAMERA_RQ_VIDEO = 6969;
    private Unbinder unbinder;
    private Tutors tutors;
    private Iterator<Map.Entry<String, View>> iterator;
    private SearchBox search;
    private User mAuthenticatedUser;
    @BindView(R.id.searchbox)
    public SearchBox searchBox;
    @BindView(R.id.recycler_view)
    public RecyclerView recyclerView;
    private FirebaseStorage firebaseStorage;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseAnalytics mFirebaseAnalytics;

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
        firebaseStorage = FirebaseStorage.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getActivity());

        //configure recycler view
        recyclerView.setHasFixedSize(true);
        StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        staggeredGridLayoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
        //staggeredGridLayoutManager.setSpanCount(2);
        recyclerView.setLayoutManager(staggeredGridLayoutManager);

        FeedsAdapter adapter = new FeedsAdapter(TradePost.class, R.layout.custom_view, FeedsAdapter.MyViewHolder.class,
                firebaseDatabase.getReference("trades").orderByChild("tradeTime"), getActivity(), mAuthenticatedUser);
        recyclerView.setAdapter(adapter);

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
        search.enableVoiceRecognition(getActivity());
        search.setHint(getString(R.string.search_hint));
        search.setSearchListener(this);

        return rootView;
    }

    @OnClick(R.id.fabCreatePost)
    public void startPostCreate(){

        new MaterialCamera(this)
                .qualityProfile(MaterialCamera.QUALITY_1080P)
                .countdownSeconds(new PrefManager(getActivity()).getVideoRecordingDuration())
                .saveDir(Utils.getVideoDirPath(getActivity()))
                .start(CAMERA_RQ_VIDEO);
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
     */
    private void showPublishDialog(final String filePath){

        View view = View.inflate(getActivity(), R.layout.publish_video_post, null);
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
                            tradePost.setVideoThumbnailUrl(filePath); //temporal filepath
                            tradePost.setTradeVideoUrl(filePath);
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
     * TODO: Task needs to be moved to background
     * @param tradePost
     */
    private void publishPost(final TradePost tradePost) {
        //create video thumbnail and upload post
        try {
            final MaterialDialog mProgressDialog = new MaterialDialog.Builder(getActivity())
                    .progress(true, 0)
                    .autoDismiss(false)
                    .widgetColor(ResourcesCompat.getColor(getResources(), R.color.bg_screen3, null))
                    .content(getString(R.string.publishing)).show();

            //configure tradepost author
            tradePost.setAuthorId(mAuthenticatedUser.getUserId());
            tradePost.setAuthorName(mAuthenticatedUser.getUserName());
            tradePost.setAuthorProfileImage(mAuthenticatedUser.getUserProfilePhoto());
            tradePost.setTradeTime(System.currentTimeMillis());

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ThumbnailUtils.createVideoThumbnail(tradePost.getVideoThumbnailUrl(),
                    MediaStore.Video.Thumbnails.MICRO_KIND)
                    .compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
            final StorageReference storageReference = firebaseStorage.getReference()
                    .child(Utils.STORAGE_REF_VIDEO_THUMBS+File.separatorChar+Utils.getFileName(tradePost.getVideoThumbnailUrl())+".JPG");
            final StorageTask<UploadTask.TaskSnapshot> thumbsTask = storageReference.putBytes(byteArrayOutputStream.toByteArray()).addOnCompleteListener(getActivity(), new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if (task.isComplete() & task.isSuccessful()) {
                                //start uploading video file. Get url to stored thumbnail and get set to the model
                                tradePost.setVideoThumbnailUrl(task.getResult().getDownloadUrl().toString());

                            } else {
                                //Notify on Error
                                Utils.showMessage(getActivity(), getString(R.string.upload_error));

                            }
                        }
                    });
            InputStream videoStream = new FileInputStream(new File(tradePost.getTradeVideoUrl()));
            //Uri videoUpload = Uri.fromFile(new File(tradePost.getTradeVideoUrl()));
            firebaseStorage.getReference().child(Utils.STORAGE_REF_VIDEO+File.separatorChar+Utils.getFileName(tradePost.getTradeVideoUrl()))
                    .putStream(videoStream).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    //upload video after thumbsTask has completed
                    if (thumbsTask.isComplete()){
                        if (task.isComplete() && task.isSuccessful()) {
                            tradePost.setTradeVideoUrl(task.getResult().getDownloadUrl().toString());
                            DatabaseReference ref = firebaseDatabase.getReference().child(Utils.DATABASE_TRADES).push();
                            String key = ref.getKey();
                            tradePost.setTradePostId(key);
                            ref.setValue(tradePost).addOnSuccessListener(getActivity(), new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Map<String, Object> update = new HashMap<>();
                                    update.put("sells", mAuthenticatedUser.getSells()+1);
                                    firebaseDatabase.getReference().child(Utils.DATABASE_USERS)
                                            .child(mAuthenticatedUser.getUserId())
                                            .updateChildren(update);
                                    //Flag for new Event
                                    Bundle bundle = new Bundle();
                                    bundle.putString(FirebaseAnalytics.Param.ITEM_ID, tradePost.getTradePostId());
                                    bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, tradePost.getTradeNameTitle());
                                    bundle.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, Utils.ANALYTICS_PARAM_ARTICLE_SELL_CATEGORY);
                                    bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "text");
                                    mFirebaseAnalytics.logEvent(Utils.CUSTOM_EVENT_ARTICLE_PUBLISHED, bundle);
                                }
                            });
                            Utils.showMessage(getActivity(), getString(R.string.uploaded));
                        }else{
                            Utils.showMessage(getActivity(), getString(R.string.error_uploading));
                        }
                    }else{
                      Utils.showMessage(getActivity(), getString(R.string.uploading_thumbs));
                    }
                    mProgressDialog.dismiss();
                }
            });
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Received recording or error from MaterialCamera
        if (requestCode == CAMERA_RQ_VIDEO) {

            if (resultCode == RESULT_OK) {
                String filePath = data.getDataString();
                Toast.makeText(getActivity(), "Saved to: " + filePath, Toast.LENGTH_LONG).show();
                //compress video at this point
                new AsyncTask<Uri, Integer, Boolean>(){
                    private ProgressDialog mPrograssDialog;

                    @Override
                    protected Boolean doInBackground(Uri... paths) {
                        Log.e(LOGTAG, "path: "+paths[0].getPath());
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
                        Log.d(LOGTAG, "Video compression Progress ... "+values[0]);
                        mPrograssDialog.setSecondaryProgress(values[0] * 10);
                        //mPrograssDialog.setProgress(values[0] * 10);
                    }

                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                        mPrograssDialog = new ProgressDialog(getActivity());
                        mPrograssDialog.setIndeterminate(true);
                        mPrograssDialog.setMessage(getString(R.string.preparing));
                        mPrograssDialog.setCancelable(false);
                        mPrograssDialog.setCanceledOnTouchOutside(false);
                        mPrograssDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                        mPrograssDialog.show();
                    }

                    @Override
                    protected void onPostExecute(Boolean isConverted) {
                        mPrograssDialog.dismiss();
                        if (isConverted){
                            //log converted path
                            Log.d(LOGTAG, "Path: "+MediaController.cachedFile.getPath());
                            //upload this version of the file to the cloud. Here'd be a suitable place to call the showPublishDialog method
                            showPublishDialog(MediaController.cachedFile.getPath());
                        }
                        super.onPostExecute(isConverted);
                    }
                }.execute(data.getData());

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
        //TODO. Apply real-time update of the Adapter in the recyclerview

    }

    @Override
    public void onSearch(String s) {
        Toast.makeText(getActivity(), "REGEX: "+Utils.fetchPrice(s), Toast.LENGTH_LONG).show();
        //TODO. build new adapter and replace current one
    }

    @Override
    public void onResultClick(SearchResult searchResult) {
        //TODO. perform search with data from the searchResult object
        Toast.makeText(getActivity(), "Clicked: "+searchResult.toString(), Toast.LENGTH_LONG).show();
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