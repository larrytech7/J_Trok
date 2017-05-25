package comn.example.user.j_trok.fragments;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseUser;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import comn.example.user.j_trok.R;
import comn.example.user.j_trok.models.User;
import comn.example.user.j_trok.utility.Utils;

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
    private User mAuthenticatedUser;


    public static ProfileFragment newInstance(FirebaseUser user) {
        ProfileFragment fragment = new ProfileFragment();
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
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        unbind = ButterKnife.bind(this, view);
        usernameTextView.setText(mAuthenticatedUser.getUserName());
        emailTextView.setText(mAuthenticatedUser.getUserEmail());
        userCountryTextView.setText(mAuthenticatedUser.getUserCountry());
        return view;
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
        mAuthenticatedUser = (User) savedInstanceState.getSerializable(Utils.CURRENT_USER);
    }
}
