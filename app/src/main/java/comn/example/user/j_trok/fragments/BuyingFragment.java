package comn.example.user.j_trok.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.StaggeredGridLayoutManager;
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

import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import comn.example.user.j_trok.R;
import comn.example.user.j_trok.adapters.MyAdapter;
import comn.example.user.j_trok.utility.PrefManager;
import comn.example.user.j_trok.utility.Utils;

import static android.app.Activity.RESULT_OK;

public class BuyingFragment extends Fragment implements SearchView.OnQueryTextListener{

    private RecyclerView recyclerView;
    private final static int CAMERA_RQ_VIDEO = 6969;
    private Unbinder unbinder;

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
                showPublishDialog(filePath);
            } else if(data != null) {
                Exception e = (Exception) data.getSerializableExtra(MaterialCamera.ERROR_EXTRA);
                e.printStackTrace();
                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }
}