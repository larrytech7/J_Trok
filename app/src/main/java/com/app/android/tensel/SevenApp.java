package com.app.android.tensel;

import android.app.Application;
//import android.content.Context;

//import com.danikula.videocache.HttpProxyCacheServer;
import com.facebook.FacebookSdk;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;

import berlin.volders.rxdownload.RxDownloadManager;

/**
 * Created by Larry Akah on 5/27/17.
 */

public class SevenApp extends Application {

    //private HttpProxyCacheServer proxy;

    @Override
    public void onCreate() {
        FirebaseApp.initializeApp(this);
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        super.onCreate();
    }

    /*public static HttpProxyCacheServer getProxy(Context context) {
        SevenApp app = (SevenApp) context.getApplicationContext();
        return app.proxy == null ? (app.proxy = app.newProxy()) : app.proxy;
    }

    private HttpProxyCacheServer newProxy() {
        return new HttpProxyCacheServer.Builder(this)
                .maxCacheFilesCount(20)
                .build();
    }*/

}
