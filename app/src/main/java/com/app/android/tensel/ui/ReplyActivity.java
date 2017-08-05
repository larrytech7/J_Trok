package com.app.android.tensel.ui;

import android.content.Intent;
import android.support.v4.app.RemoteInput;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.app.android.tensel.utility.Utils;

public class ReplyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        //LOG reply Text
        Intent replyIntent = getIntent();
        if (replyIntent != null){
            Bundle bundle = RemoteInput.getResultsFromIntent(replyIntent);
            Log.e("ReplyActivity", "Reply: "+bundle.getCharSequence(Utils.INSTANT_REPLY));
        }

    }
}
