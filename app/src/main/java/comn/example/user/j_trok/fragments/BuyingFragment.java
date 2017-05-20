package comn.example.user.j_trok.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
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
import com.popalay.tutors.TutorialListener;
import com.popalay.tutors.Tutors;
import com.popalay.tutors.TutorsBuilder;
import com.quinny898.library.persistentsearch.SearchBox;
import com.quinny898.library.persistentsearch.SearchResult;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import comn.example.user.j_trok.R;
import comn.example.user.j_trok.adapters.MyAdapter;
import comn.example.user.j_trok.utility.PrefManager;
import comn.example.user.j_trok.utility.Utils;
import comn.example.user.j_trok.utility.videocompression.MediaController;

import static android.app.Activity.RESULT_OK;

public class BuyingFragment extends Fragment implements TutorialListener, SearchBox.SearchListener {

    private static final String LOGTAG = "BuyingFragment";
    private RecyclerView recyclerView;
    private final static int CAMERA_RQ_VIDEO = 6969;
    private Unbinder unbinder;
    private Tutors tutors;
    private Iterator<Map.Entry<String, View>> iterator;
    private SearchBox search;

    public static BuyingFragment newInstance() {
        BuyingFragment fragment = new BuyingFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.deleteEmptyVideos(getActivity());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_buy, container, false);
        unbinder = ButterKnife.bind(this, rootView);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(staggeredGridLayoutManager);

        MyAdapter adapter = new MyAdapter(new String[]{"Author one", "Author two", "Author three", "Author four", "Author five"});
        recyclerView.setAdapter(adapter);

        //configure search
        search = (SearchBox) rootView.findViewById(R.id.searchbox);
        String[] categories = getResources().getStringArray(R.array.categories);
        for(String category : categories){
            SearchResult option = new SearchResult(category, getResources().getDrawable(R.drawable.ic_history));
            search.addSearchable(option);
        }
        search.setAnimateDrawerLogo(true);
        search.setLogoText(getString(R.string.search));
        search.setLogoTextColor(R.color.bg_screen1);
        //search.setMenuVisibility(SearchBox.GONE);
        search.enableVoiceRecognition(getActivity());
        search.setHint(getString(R.string.search_hint));
        search.setSearchListener(this);

        return rootView;
    }

    @OnClick(R.id.fabCreatePost)
    public void startPostCreate(View view){

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
        String[] categories = getResources().getStringArray(R.array.categories);
        final ChipCloud categoryChip = (ChipCloud) view.findViewById(R.id.category_chip_cloud);
        new ChipCloud.Configure()
                .chipCloud(categoryChip)
                .labels(categories)
        .selectTransitionMS(500)
        .deselectTransitionMS(250)
        .chipListener(new ChipListener() {
            @Override
            public void chipSelected(int i) {
                //TODO: SAVE VALUE of selected tag
            }

            @Override
            public void chipDeselected(int i) {
                //TODO: Remove value from list of selected tags
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
                        //TODO: collect values and publish to firebase
                        String value = ((EditText) dialog.findViewById(R.id.postTitleEditText)).getText().toString();
                        Toast.makeText(getActivity(), String.format("title %s file: %s", value, filePath), Toast.LENGTH_LONG).show();
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Received recording or error from MaterialCamera
        if (requestCode == CAMERA_RQ_VIDEO) {

            if (resultCode == RESULT_OK) {
                String filePath = data.getDataString();
                Toast.makeText(getActivity(), "Saved to: " + filePath, Toast.LENGTH_LONG).show();
                //compress video at this point
                new AsyncTask<Uri, String, Boolean>(){

                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    protected Boolean doInBackground(Uri... paths) {
                        Log.e(LOGTAG, "path: "+paths[0].getPath());
                        boolean converted = false;
                        try {
                            converted = MediaController.getInstance()
                                    .convertVideo(Utils.getFilePath(getActivity(), paths[0]),
                                            new File(Utils.getVideoDirPath(getActivity())));
                        } catch (URISyntaxException e) {
                            e.printStackTrace();
                        }
                        return converted;
                    }

                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                    }

                    @Override
                    protected void onPostExecute(Boolean isConverted) {
                        if (isConverted){
                            //log converted path
                            Log.d(LOGTAG, "Path: "+MediaController.cachedFile.getPath());
                            //TODO. upload this version of the file to the cloud
                        }
                        super.onPostExecute(isConverted);
                    }
                }.execute(data.getData());

                showPublishDialog(filePath);
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

    }

    @Override
    public void onSearchTermChanged(String s) {
        Toast.makeText(getActivity(), "Search changed to: "+s, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onSearch(String s) {
        Toast.makeText(getActivity(), "Searching: "+s, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onResultClick(SearchResult searchResult) {
        Toast.makeText(getActivity(), "Clicked: "+searchResult.toString(), Toast.LENGTH_LONG).show();
    }

}