package com.app.android.tensel.broadcasts;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.support.v4.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.app.android.tensel.R;
import com.app.android.tensel.models.TradePost;
import com.app.android.tensel.models.User;
import com.app.android.tensel.ui.MainActivity;
import com.app.android.tensel.utility.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class PublishingBroadcastReceiver extends BroadcastReceiver {

    private NotificationCompat.Builder notificationBuilder;
    private NotificationManager mNotificationManager;
    private static final String LOGTAG = "PublishingBroadcast";

    @Override
    public void onReceive(final Context context, Intent intent) {
        // This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast to publish a new item/article to firebase
        notificationBuilder = new NotificationCompat.Builder(context);
        mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        startProgressNotification(context);
        final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        final FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);
        final FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        DatabaseReference ref = firebaseDatabase.getReference().child(Utils.DATABASE_TRADES).push();
        String key = ref.getKey();
        final TradePost tradePost = (TradePost) intent.getSerializableExtra(Utils.ITEM_TRADE_POST);
        final User mAuthenticatedUser = (User) intent.getSerializableExtra(Utils.CURRENT_USER);

        try {
            //launch progress notification
            tradePost.setTradePostId(key);
            //subscribe FCM for messages on this item
            FirebaseMessaging.getInstance().subscribeToTopic(key);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            Bitmap bitmap = Utils.getVideoThumbnail(context, tradePost.getVideoThumbnailUrl());
            byte[] bitmapBytes = new byte[0];
            if (bitmap != null){
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                bitmapBytes = byteArrayOutputStream.toByteArray();
            }
            final StorageReference storageReference = firebaseStorage.getReference()
                    .child(Utils.STORAGE_REF_VIDEO_THUMBS+File.separatorChar+Utils.getFileName(tradePost.getVideoThumbnailUrl())+".JPG");
            final StorageTask<UploadTask.TaskSnapshot> thumbsTask = storageReference.putBytes(bitmapBytes)//(byteArrayOutputStream.toByteArray())
                    .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if (task.isComplete() & task.isSuccessful()) {
                                //start uploading video file. Get url to stored thumbnail and get set to the model
                                tradePost.setVideoThumbnailUrl(task.getResult().getDownloadUrl().toString());

                            } else {
                                //Notify on Error
                                Utils.showMessage(context, context.getString(R.string.upload_error));

                            }
                        }
                    });

            InputStream videoStream = new FileInputStream(new File(tradePost.getTradeVideoUrl()));
            //Upload compressed video to firebase
            firebaseStorage.getReference().child(Utils.STORAGE_REF_VIDEO+File.separatorChar+Utils.getFileName(tradePost.getTradeVideoUrl()))
                    .putStream(videoStream)
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            //update the progress notification
                            Long progress = taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount() ;
                            Log.e(LOGTAG, "Progress: "+progress.intValue());
                            updateProgressNotification(context, progress.intValue() * 100);
                        }
                    })
                    .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    //upload video after thumbsTask has completed
                    if (thumbsTask.isComplete())
                        if (task.isComplete() && task.isSuccessful()) {
                            tradePost.setTradeVideoUrl(task.getResult().getDownloadUrl().toString());
                            DatabaseReference ref = firebaseDatabase.getReference().child(Utils.DATABASE_TRADES).push();
                            String key = ref.getKey();
                            tradePost.setTradePostId(key);
                            //subscribe FCM for messages on this item
                            FirebaseMessaging.getInstance().subscribeToTopic(key);

                            ref.setValue(tradePost).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Map<String, Object> update = new HashMap<>();
                                    update.put("sells", mAuthenticatedUser.getSells() + 1);
                                    firebaseDatabase.getReference().child(Utils.DATABASE_USERS)
                                            .child(mAuthenticatedUser.getUserId())
                                            .updateChildren(update);
                                    //Flag for new Event
                                    Bundle bundle = new Bundle();
                                    bundle.putString(FirebaseAnalytics.Param.ITEM_ID, tradePost.getTradePostId());
                                    bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, tradePost.getTradeNameTitle());
                                    bundle.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, Utils.ANALYTICS_PARAM_ARTICLE_SELL_CATEGORY);
                                    bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "text");
                                    mFirebaseAnalytics.logEvent(Utils.CUSTOM_EVENT_ARTICLE_PUBLISHED, bundle);
                                }
                            });
                        }
                }
            });

        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    private void updateProgressNotification(Context context, int progress) {
        notificationBuilder.setProgress(100, progress, false);
        if (progress == 100)
            notificationBuilder.setSound(Uri.parse("android.resource://"+context.getPackageName()+"/"+R.raw.send_sound));
        mNotificationManager.notify(0, notificationBuilder.build());
    }

    /**
     * Setup the publishing notification object
     * @param context component context
     */
    private void startProgressNotification(Context context){
        if (notificationBuilder != null){

            // Creates an explicit intent for an Activity in your app
            Intent resultIntent = new Intent(context, MainActivity.class);

            // The stack builder object will contain an artificial back stack for
            // the
            // started Activity.
            // This ensures that navigating backward from the Activity leads out of
            // your application to the Home screen.
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            // Adds the back stack for the Intent (but not the Intent itself)
            stackBuilder.addParentStack(MainActivity.class);
            // Adds the Intent that starts the Activity to the top of the stack
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            notificationBuilder.setTicker(context.getString(R.string.app_name));
            notificationBuilder.setContentTitle(context.getString(R.string.publishing));
            //notificationBuilder.setContentText("");
            notificationBuilder.setSmallIcon(R.mipmap.ic_send);
            notificationBuilder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher));
            notificationBuilder.setContentIntent(resultPendingIntent);
            notificationBuilder.setOngoing(true);
            notificationBuilder.setAutoCancel(true);
            notificationBuilder.setProgress(100, 0, false);
            notificationBuilder.setWhen(0);
            //notify
            mNotificationManager.notify(0, notificationBuilder.build());
        }
    }
}
