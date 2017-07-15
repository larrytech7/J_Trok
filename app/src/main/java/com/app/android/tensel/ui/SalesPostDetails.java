package com.app.android.tensel.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.app.android.tensel.R;
import com.app.android.tensel.models.User;
import com.app.android.tensel.utility.Utils;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class SalesPostDetails extends AppCompatActivity {

    @BindView(R.id.chatsRecyclerView)
    RecyclerView chatsRecyclerview;
    @BindView(R.id.authorNameTextView)
    TextView authorNameTextView;
    @BindView(R.id.dateDetailTextView)
    TextView textViewDate;
    @BindView(R.id.contentTextView)
    TextView contentTextView;
    @BindView(R.id.authorImageView)
    CircleImageView authorImageView;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    private FirebaseAuth firebaseAuth;
    private FirebaseAnalytics mFirebaseAnalytics;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sales_post_details);

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        firebaseAuth = FirebaseAuth.getInstance();
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        user = Utils.getUserConfig(firebaseAuth.getCurrentUser());
    }
}
