package comn.example.user.j_trok.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import comn.example.user.j_trok.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class SellingFragment extends Fragment {

    public static SellingFragment newInstance() {
        SellingFragment fragment = new SellingFragment();
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
        return inflater.inflate(R.layout.fragment_sell, container, false);
    }

}
