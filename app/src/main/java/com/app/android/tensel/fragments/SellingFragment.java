package com.app.android.tensel.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.app.android.tensel.R;
import com.app.android.tensel.adapters.SalesAdpater;

import static com.app.android.tensel.R.layout.fragment_sell;


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
        final View rootView = inflater.inflate(fragment_sell, container, false);


        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        SalesAdpater adapter = new SalesAdpater(new String[]{"Item one", "Item two", "Item three", "Item four", "Item five", "Item six", "Item Seven", "Item eight", "Item nine", "Item ten"});
        recyclerView.setAdapter(adapter);

        StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(staggeredGridLayoutManager);

        return rootView;
    }
}

