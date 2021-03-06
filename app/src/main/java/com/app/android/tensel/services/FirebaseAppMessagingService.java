package com.app.android.tensel.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.RemoteInput;
import android.support.v4.app.TaskStackBuilder;
import android.text.TextUtils;
import android.util.Log;

import com.app.android.tensel.R;
import com.app.android.tensel.models.User;
import com.app.android.tensel.ui.MainActivity;
import com.app.android.tensel.ui.PostDetailActivity;
import com.app.android.tensel.ui.PrivateChatActivity;
import com.app.android.tensel.ui.ReplyActivity;
import com.app.android.tensel.ui.SalesPostDetails;
import com.app.android.tensel.utility.PrefManager;
import com.app.android.tensel.utility.Utils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import static com.facebook.login.widget.ProfilePictureView.TAG;

public class FirebaseAppMessagingService extends FirebaseMessagingService {

    private int numMessages = 0;
    private int pvChats = 0;
    private ArrayList<String> messageList = new ArrayList<>();
    private final static String LOGTAG = "FirebaseCloudMessaging";
    //notification Types
    private static final int NOTIFICATION_TYPE_FEED = 1;
    private static final int NOTIFICATION_TYPE_SELLS = 5;
    private static final int NOTIFICATION_TYPE_COMMENT_POST = 2;
    private static final int NOTIFICATION_TYPE_COMMENT_SALE = 6;
    private static final int NOTIFICATION_TYPE_ADS = 3;
    private static final int NOTIFICATION_TYPE_PV = 4;
    private User current_user;

    public FirebaseAppMessagingService() {
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        current_user = Utils.getUserConfig(firebaseAuth.getCurrentUser());
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        //process message payload
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            JSONObject payload = new JSONObject(remoteMessage.getData());
            try {
                String title = payload.getString("user");
                String body = payload.getString("body");
                int type = payload.getInt("type");
                String key = payload.getString("key");
                String userid = payload.getString("userid");
                String authorId = payload.getString("authorid");
                String profile = payload.getString("profile");

                Log.e(LOGTAG, payload.toString(2));
                //configure notifications based on the type
                switch (type){
                    case NOTIFICATION_TYPE_FEED:
                        NotificationCompat.Builder builder = getNotification(PostDetailActivity.class, this, title, body,
                                Utils.FEED_DETAIL_ID, key, "", "", "");
                        builder.setNumber(++numMessages);
                        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
                        bigTextStyle.bigText(body);
                        bigTextStyle.setSummaryText(getString(R.string.new_posts, numMessages));
                        builder.setStyle(bigTextStyle);
                        builder.setSmallIcon(R.drawable.ic_play);
                        builder.setGroup("posts");

                        //get profile picture
                        try {
                            Bitmap profileBitmap = Picasso.with(this).load(profile).get();
                            builder.setLargeIcon(profileBitmap);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        //fire
                        Notification notif = builder.build();
                        notif.vibrate = new long[] { 100, 250, 100, 500};
                        notif.sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                        if (new PrefManager(this).getBooleanPreference(Utils.ITEM_NOTIFICATION_PREF, true)) {
                            if (!TextUtils.equals(title, current_user.getUserName()))
                                nm.notify(NOTIFICATION_TYPE_FEED, notif);
                        }
                        break;
                    case NOTIFICATION_TYPE_SELLS:
                        NotificationCompat.Builder sbuilder = getNotification(SalesPostDetails.class, this, title, body,
                                Utils.FEED_DETAIL_ID, key, "", "", "");
                        sbuilder.setNumber(++numMessages);
                        NotificationCompat.BigTextStyle sbigTextStyle = new NotificationCompat.BigTextStyle();
                        sbigTextStyle.bigText(body);
                        sbigTextStyle.setSummaryText(getString(R.string.new_posts, numMessages));
                        sbuilder.setStyle(sbigTextStyle);
                        sbuilder.setSmallIcon(R.mipmap.ic_send);
                        sbuilder.setGroup("posts");
                        sbuilder.setGroupSummary(true);

                        //get profile picture
                        try {
                            Bitmap profileBitmap = Picasso.with(this).load(profile).get();
                            sbuilder.setLargeIcon(profileBitmap);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        //fire
                        Notification snotif = sbuilder.build();
                        snotif.vibrate = new long[] { 100, 250, 100, 500};
                        snotif.sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                        if (new PrefManager(this).getBooleanPreference(Utils.ITEM_NOTIFICATION_PREF, true)) {
                            if (!TextUtils.equals(title, current_user.getUserName()))
                                nm.notify(NOTIFICATION_TYPE_FEED, snotif);
                        }
                        break;
                    case NOTIFICATION_TYPE_COMMENT_POST:
                        messageList.add(body);
                        NotificationCompat.Builder cbuilder = getNotification(PostDetailActivity.class, this, title, body,
                        Utils.FEED_DETAIL_ID, key, "", "", "");
                        cbuilder.setSmallIcon(R.mipmap.ic_launcher);
                        //set action button
                        RemoteInput remoteInput = new RemoteInput.Builder(Utils.INSTANT_REPLY)
                                .setLabel(getString(R.string.reply))
                                .build();
                        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
                        inboxStyle.setSummaryText(getString(R.string.new_comments, numMessages));
                        inboxStyle.setBigContentTitle(title);
                        for(String m : messageList){
                            inboxStyle.addLine(m);
                        }
                        //cbuilder.addAction(commentAction);
                        cbuilder.setStyle(inboxStyle);
                        cbuilder.setNumber(++numMessages);
                        cbuilder.setGroup("comments");

                        //get profile picture
                        try {
                            Bitmap profileBitmap = Picasso.with(this).load(profile).get();
                            cbuilder.setLargeIcon(profileBitmap);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        //fire
                        Notification notification = cbuilder.build();
                        notification.vibrate = new long[] { 100, 250, 100, 500};
                        notification.sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                        if (new PrefManager(this).getBooleanPreference(Utils.COMMENT_NOTIFICATION_PREF, true)) {
                            if (!TextUtils.equals(title, current_user.getUserName()))
                                nm.notify(NOTIFICATION_TYPE_COMMENT_POST, notification);
                        }

                        break;
                    case NOTIFICATION_TYPE_COMMENT_SALE:
                        messageList.add(body);
                        NotificationCompat.Builder salebuilder = getNotification(SalesPostDetails.class, this, title, body,
                                Utils.FEED_DETAIL_ID, key, "", "", "");
                        salebuilder.setSmallIcon(R.mipmap.ic_launcher);
                        //set action button
                        RemoteInput sremoteInput = new RemoteInput.Builder(Utils.INSTANT_REPLY)
                                .setLabel(getString(R.string.reply))
                                .build();
                        NotificationCompat.InboxStyle sinboxStyle = new NotificationCompat.InboxStyle();
                        sinboxStyle.setSummaryText(getString(R.string.new_comments, numMessages));
                        sinboxStyle.setBigContentTitle(title);
                        for(String m : messageList){
                            sinboxStyle.addLine(m);
                        }
                        //cbuilder.addAction(commentAction);
                        salebuilder.setStyle(sinboxStyle);
                        salebuilder.setNumber(++numMessages);
                        salebuilder.setGroup("comments");
                        salebuilder.setGroupSummary(true);

                        //get profile picture
                        try {
                            Bitmap profileBitmap = Picasso.with(this).load(profile).get();
                            salebuilder.setLargeIcon(profileBitmap);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        //fire
                        Notification snotification = salebuilder.build();
                        snotification.vibrate = new long[] { 100, 250, 100, 500};
                        snotification.sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                        if (new PrefManager(this).getBooleanPreference(Utils.COMMENT_NOTIFICATION_PREF, true)) {
                            if (!TextUtils.equals(title, current_user.getUserName()))
                                nm.notify(NOTIFICATION_TYPE_COMMENT_SALE, snotification);
                        }

                        break;
                    case NOTIFICATION_TYPE_PV:

                        final NotificationCompat.Builder mbuilder = getNotification(PrivateChatActivity.class, this,
                                title, body,
                                Utils.FEED_DETAIL_ID, key, userid, authorId, profile);
                        mbuilder.setSmallIcon(R.drawable.ic_chat);
                        PendingIntent replyPendingIntent = PendingIntent
                                .getActivity(this, 1, new Intent(this, ReplyActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
                        //set action button
                        RemoteInput mRemoteInput = new RemoteInput.Builder(Utils.INSTANT_REPLY)
                                .setLabel(getString(R.string.reply))
                                .build();
                        NotificationCompat.Action commentAction = new NotificationCompat.Action.Builder(R.drawable.ic_send,
                                getString(R.string.reply),
                                replyPendingIntent)
                                .setAllowGeneratedReplies(true)
                                .addRemoteInput(mRemoteInput)
                                .build();
                        mbuilder.addAction(commentAction);
                        //cbuilder.setStyle(inboxStyle);
                        mbuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(body));
                        mbuilder.setNumber(++pvChats);
                        mbuilder.setGroup("chats");
                        mbuilder.setGroupSummary(true);

                        //get profile picture
                        try {
                            Bitmap profileBitmap = Picasso.with(this).load(profile).get();
                            mbuilder.setLargeIcon(profileBitmap);
                        } catch (IOException e) {
                            e.printStackTrace();
                            FirebaseCrash.report(e.getCause()); //report Error for profile image errors
                        }

                        //fire the notification
                        Notification mnotification = mbuilder.build();
                        mnotification.vibrate = new long[] { 100, 250, 100, 500};
                        mnotification.sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                        if(!TextUtils.equals(title, current_user.getUserName()))
                            nm.notify(NOTIFICATION_TYPE_PV, mnotification);

                        break;
                }


            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if (remoteMessage.getNotification() != null){
            //extract Data and send Notification
            String title = remoteMessage.getNotification().getTitle();
            String body =remoteMessage.getNotification().getBody();
            NotificationCompat.Builder builder = getNotification(MainActivity.class, this, title, body, "","", "", "", "");
            builder.setSmallIcon(R.mipmap.ic_launcher);
            nm.notify(NOTIFICATION_TYPE_ADS, builder.build());
        }
    }

    @Override
    public void onDeletedMessages() {
        super.onDeletedMessages();
        Log.d(LOGTAG, "Message Deleted!");
    }

    @Override
    public void onMessageSent(String s) {
        super.onMessageSent(s);
        Log.d(LOGTAG, "Message Sent!");
    }

    /**
     * Build a notification object used to issue different categories of notifications from the FCM service
     * @param c application class to open by default
     * @param context service context
     * @param title notification title
     * @param content notification content/body
     * @param extraKey extra intent key for data to the intent
     * @param extraValue value of extra data to the intent
     * @param userid userid of user in the data payload
     * @return a builder for customization of the given notification
     */
    private NotificationCompat.Builder getNotification(Class c, Context context, String title,
                                  String content, String extraKey, String extraValue, String userid,
                                                       String authorId, String userProfile){
        Intent intent1 = new Intent(context, c);
        intent1.putExtra(extraKey, extraValue);
        intent1.putExtra(Utils.USER, userid);
        intent1.putExtra(Utils.AUTHOR_ID, authorId);
        intent1.putExtra(Utils.PROFILE_IMG, userProfile);

        //intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(context);
        taskStackBuilder.addParentStack(c);
        taskStackBuilder.addNextIntent(intent1);

        PendingIntent pendingIntent = taskStackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
                //PendingIntent.getActivity(context, 0, intent1, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setTicker(context.getString(R.string.app_name));
        builder.setContentTitle(title);
        builder.setContentText(content);
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
        builder.setContentIntent(pendingIntent);
        builder.setSound(Uri.parse("android.resource://"+getPackageName()+"/"+R.raw.send_sound));
        builder.setOngoing(false);
        builder.setAutoCancel(true);
        builder.setWhen(0);

        return builder;
    }

}
