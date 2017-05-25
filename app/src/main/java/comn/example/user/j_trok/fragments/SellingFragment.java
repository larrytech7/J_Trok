package comn.example.user.j_trok.fragments;


import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.afollestad.materialcamera.MaterialCamera;
import com.google.firebase.auth.FirebaseUser;
import com.iceteck.silicompressorr.SiliCompressor;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import comn.example.user.j_trok.R;
import comn.example.user.j_trok.models.User;
import comn.example.user.j_trok.utility.Utils;

import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass.
 */
public class SellingFragment extends Fragment {

    private static final int CAMERA_RQ_IMAGE = 400;
    private Unbinder unbinder;
    User mAuthenticatedUser;

    @BindView(R.id.imageView)
    ImageView imageView;

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
        View rootView = inflater.inflate(R.layout.fragment_sell, container, false);
        unbinder = ButterKnife.bind(this, rootView);
        return rootView;
    }

    @OnClick(R.id.btnValidate)
    public void getPhotoShot(){
        //take demo picture

        new MaterialCamera(this)
                .stillShot()
                .saveDir(Utils.getImageDirPath(getActivity()))
                .start(CAMERA_RQ_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Received recording or error from MaterialCamera
        if (requestCode == CAMERA_RQ_IMAGE && resultCode == RESULT_OK) {

                String filePath = data.getDataString();
                Toast.makeText(getActivity(), "Saved to: " + filePath, Toast.LENGTH_LONG).show();
                //compress image
            try {
                Bitmap bitmap = SiliCompressor
                        .with(getActivity())
                        .getCompressBitmap(data.getDataString());
                imageView.setImageBitmap(bitmap);
                Log.d("ImageCompression", "Info: byte size->"+bitmap.getByteCount());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(Utils.CURRENT_USER, mAuthenticatedUser);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        mAuthenticatedUser = (User) savedInstanceState.getSerializable(Utils.CURRENT_USER);
    }
}
