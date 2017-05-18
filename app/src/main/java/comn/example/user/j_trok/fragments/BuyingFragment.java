package comn.example.user.j_trok.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.afollestad.materialcamera.MaterialCamera;

import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import comn.example.user.j_trok.R;
import comn.example.user.j_trok.adapters.MyAdapter;
import comn.example.user.j_trok.utility.Utils;

import static android.app.Activity.RESULT_OK;
import static butterknife.ButterKnife.bind;

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
        //ButterKnife.bind(getActivity());

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
                .countdownSeconds(7.0f)
                .saveDir(Utils.getVideoDirPath(getActivity()))
                .start(CAMERA_RQ_VIDEO);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Received recording or error from MaterialCamera
        if (requestCode == CAMERA_RQ_VIDEO) {

            if (resultCode == RESULT_OK) {
                Toast.makeText(getActivity(), "Saved to: " + data.getDataString(), Toast.LENGTH_LONG).show();
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