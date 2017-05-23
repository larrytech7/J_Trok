package comn.example.user.j_trok.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import comn.example.user.j_trok.R;

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


    public static ProfileFragment newInstance() {
        ProfileFragment fragment = new ProfileFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        unbind = ButterKnife.bind(container.getContext(), view);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbind.unbind();
    }
}
