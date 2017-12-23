package com.app.android.tensel.utility;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.Toast;

import com.app.android.tensel.R;
import com.app.android.tensel.models.Price;
import com.app.android.tensel.models.User;
import com.google.firebase.auth.FirebaseUser;

import java.io.File;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.regex.Pattern;


/**
 * Created by Larry Akah on 5/19/17.
 */

public class Utils {

    private static final String TAG = Utils.class.getName();
    public static final String PREF_RECORDING_DURATION = "RECORDING_DURATION";
    public static final String PREF_SHOW_HINTS = "SHOW_HINTS_PREF";
    public static final String ANALYTICS_PARAM__TUTORIAL_ID = "TUTORIAL_ID";
    public static final String ANALYTICS_PARAM__TUTORIAL_NAME = "HOME_TUTORIAL";
    public static final String ANALYTICS_PARAM__TUTORIAL_CATEGORY = "TUTORIALS";
    public static final String CURRENT_USER = "CURRENT_USER";
    public static final String ANALYTICS_PARAM__LAUNCH_ID = "LAUNCH_ID";
    public static final String ANALYTICS_PARAM__LAUNCH_NAME = "APP_LAUNCH_NAME";
    public static final String ANALYTICS_PARAM__LAUNCH_CATEGORY = "APP_LUNCH_CATEGORY";
    public static final String STORAGE_REF_VIDEO = "media/videos";
    public static final String STORAGE_REF_VIDEO_THUMBS = "media/videothumbs";
    public static final String DATABASE_TRADES = "trades";
    public static final String ANALYTICS_PARAM_ARTICLE_SELL_CATEGORY = "ARTICLE_SELL_POSTED";
    public static final String CUSTOM_EVENT_ARTICLE_PUBLISHED = "ARTICLE_PUBLISHED";
    public static final String DATABASE_USERS = "users";
    public static final String FEED_DETAIL_ID = "FEED_ID";
    public static final String CHAT_EVENT = "COMMENT_EVENT";
    public static final String ANALYTICS_PARAM_CHATS_ID = "COMMENT_ID";
    public static final String ANALYTICS_PARAM_CHAT_NAME = "COMMENT_NAME";
    public static final String ANALYTICS_PARAM_CHAT_CATEGORY = "CHAT_CATEGORY";
    public static final String INSTANT_REPLY = "INSTANT_REPLY";
    public static final String TOPIC_FEEDS = "trades";
    public static final String PREF_SHOW_PV_HINTS = "PV_HINTS";
    public static final String USER = "USER";
    public static final String ANALYTICS_PARAM_PV_CHATS_ID = "PV_CHAT_ID";
    public static final String ANALYTICS_PARAM_PV_CHAT_NAME = "PV_CHAT_NAME";
    public static final String PV = "pvchats";
    public static final String ITEM_NOTIFICATION_PREF = "ITEM_PREFERENCE_NOTIF";
    public static final String COMMENT_NOTIFICATION_PREF = "COMMENT_PREFERENCE_NOTIF";
    public static final String FIREBASE_SELLS = "sells";
    public static final String SELL_DETAIL_ID = "SELLS_DETAIL_ID";
    public static final String PROFILE_IMG = "USER_PROFILE_PHOTO";
    public static final String AUTHOR_ID = "AUTHOR_ID";
    public static final String STORAGE_REF_IMAGES = "media/images";
    public static final String ITEM_TRADE_POST = "app.tensel.tradepost";
    public static final String PUBLISH_ITEM_INTENT = "com.app.tensel.publish";

    public static User getUserConfig(@NonNull  FirebaseUser user){
        User muser = new User();
        muser.setUserEmail(user.getEmail());
        muser.setUserName(user.getDisplayName());
        muser.setUserCountry("");
        muser.setUserCity("");
        muser.setUserId(user.getUid());
        muser.setUserProfilePhoto(user.getPhotoUrl().toString());
        return muser;
    }

    public static void deleteFilesAtPath( File parentDir )
    {
        File[] files = parentDir.listFiles();
        if ( files != null && files.length > 0 )
        {
            Log.d(TAG, "Deleting Empty Files in " + parentDir );
            for ( File file: files )
            {
                file.delete();
            }
        }
    }

    public static void deleteEmptyVideos( Context ctx )
    {
        deleteFilesAtPath( new File(getVideoDirPath( ctx )) );
    }

    public static File getSdCard()
    {
        return Environment.getExternalStorageDirectory();
    }

    public static String getVideoDirPath( Context ctx )
    {
        //Check if directory exists, else create new one then return the resulting path
        File directory = new File(getSdCard().getAbsolutePath() + "/Tensel/media/videos");
        if (directory.isDirectory())
            return getSdCard().getAbsolutePath() + "/Tensel/media/videos";
        else
            return directory.mkdirs() ? directory.toString() : getSdCard().getAbsolutePath() + "/Android/data/" + ctx.getPackageName() + "/media/videos";
    }

    public static Uri getVideoFileForUpload(Context context){
        return Uri.fromFile(new File(getVideoDirPath(context), "VID_"+ new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
                .format(new Date())+".mp4"));
    }

    public static boolean isVideoDownloaded(String videoname){
        File mfile = new File(getSdCard().getAbsolutePath()+"/"+Environment.DIRECTORY_DOWNLOADS, videoname);
        return mfile.exists();
    }

    public static Uri getDownloadedVideo(String name){
        return Uri.fromFile(new File(getSdCard().getAbsolutePath()+"/"+Environment.DIRECTORY_DOWNLOADS, name));
    }

    public static String getFileName(@NonNull String file){
        File mfile = new File(file);
        return mfile.isFile() ? mfile.getName() :
                String.valueOf(System.currentTimeMillis()+ Math.floor(new Random().nextDouble()));
    }

    public static String getImageDirPath( Context ctx )
    {
        return getSdCard().getAbsolutePath() + "/Android/data/" + ctx.getPackageName() + "/media/images";
    }

    @SuppressLint("NewApi")
    public static String getFilePath(Context context, Uri uri) throws URISyntaxException {
        String selection = null;
        String[] selectionArgs = null;
        // Uri is different in versions after KITKAT (Android 4.4), we need to
        if (Build.VERSION.SDK_INT >= 19 && DocumentsContract.isDocumentUri(context.getApplicationContext(), uri)) {
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                return Environment.getExternalStorageDirectory() + "/" + split[1];
            } else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                uri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
            } else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                if ("image".equals(type)) {
                    uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                selection = "_id=?";
                selectionArgs = new String[]{
                        split[1]
                };
            }
        }
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = {
                    MediaStore.Images.Media.DATA
            };
            Cursor cursor = null;
            try {
                cursor = context.getContentResolver()
                        .query(uri, projection, selection, selectionArgs, null);
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    public static void showMessage(Context activity, String s) {
        Toast.makeText(activity, s, Toast.LENGTH_SHORT).show();
    }

    /**
     * Use regular expression to extract the price suggested in given text
     * @param pDesc text to extract from
     * @return Price price extracted or 0
     */
    public static Price fetchPrice(String pDesc) {
        String[] parts = pDesc.split("[ ]"); //split with spaces
        long amount = 0;
        String currency = "$";
        Pattern pattern =  Pattern.compile("[0-9]{2,}[a-zA-Z.$]*");
        //loop through parts, to find a match
        for (String part : parts){
            if (pattern.matcher(part).matches()) { //matches an amount type
                //now filter if it's entirely numeric or followed by currency
                if (Pattern.matches("[0-9]{2,}", part)) {
                    amount = Long.parseLong(part);
                }
                else{
                    currency = part;
                    amount = Long.parseLong(part.replaceAll("[a-zA-Z$]+", ""));
                    currency = currency.replaceAll("[0-9]+", "");
                    //match and extract the numeric part of the amount
                    //Matcher matcher = Pattern.compile("[a-zA-Z$]+").matcher(part);
                    //amount = matcher.matches() ? Long.parseLong(part.substring(0, matcher.start())) : 1;
                }

                break;
            }
        }
        return new Price(amount, currency);
    }

    public static Uri getCleanUri(@NonNull String tradeVideoUrl) {
        String newUrl = tradeVideoUrl.replace("%2F", "/");
        String[] parts = tradeVideoUrl.split("[/]{2}");
        String[] ssp = parts[1].split("[?]");

        //return Uri.fromParts("https",ssp[0],ssp[1]);
        return Uri.parse(tradeVideoUrl);
    }

    /**
     * Transform source imageview into zoomed imageview with animation
     * @param thumbView Thumb source imageview
     * @param imageResId drawable to use for expanded imageview
     * @param rootView root layout of expanded imageview
     * @param mShortAnimationDuration duration of the animation transformation
     */
    public static void zoomImageFromThumb(final View thumbView, Drawable imageResId, View rootView, final long mShortAnimationDuration) {
        // If there's an animation in progress, cancel it
        // immediately and proceed with this one.
        final AnimatorSet[] mCurrentAnimator = {null};
        if (mCurrentAnimator[0] != null) {
            mCurrentAnimator[0].cancel();
        }

        // Load the high-resolution "zoomed-in" image.
        final ImageView expandedImageView = (ImageView) rootView.findViewById(
                R.id.expanded_image);

        expandedImageView.setImageDrawable(imageResId);

        // Calculate the starting and ending bounds for the zoomed-in image.
        // This step involves lots of math. Yay, math.
        final Rect startBounds = new Rect();
        final Rect finalBounds = new Rect();
        final Point globalOffset = new Point();

        // The start bounds are the global visible rectangle of the thumbnail,
        // and the final bounds are the global visible rectangle of the container
        // view. Also set the container view's offset as the origin for the
        // bounds, since that's the origin for the positioning animation
        // properties (X, Y).

        thumbView.getGlobalVisibleRect(startBounds);
        rootView.getGlobalVisibleRect(finalBounds, globalOffset);
        startBounds.offset(-globalOffset.x, -globalOffset.y);
        finalBounds.offset(-globalOffset.x, -globalOffset.y);

        // Adjust the start bounds to be the same aspect ratio as the final
        // bounds using the "center crop" technique. This prevents undesirable
        // stretching during the animation. Also calculate the start scaling
        // factor (the end scaling factor is always 1.0).
        float startScale;
        if ((float) finalBounds.width() / finalBounds.height()
                > (float) startBounds.width() / startBounds.height()) {
            // Extend start bounds horizontally
            startScale = (float) startBounds.height() / finalBounds.height();
            float startWidth = startScale * finalBounds.width();
            float deltaWidth = (startWidth - startBounds.width()) / 2;
            startBounds.left -= deltaWidth;
            startBounds.right += deltaWidth;
        } else {
            // Extend start bounds vertically
            startScale = (float) startBounds.width() / finalBounds.width();
            float startHeight = startScale * finalBounds.height();
            float deltaHeight = (startHeight - startBounds.height()) / 2;
            startBounds.top -= deltaHeight;
            startBounds.bottom += deltaHeight;
        }

        // Hide the thumbnail and show the zoomed-in view. When the animation
        // begins, it will position the zoomed-in view in the place of the
        // thumbnail.

        thumbView.setAlpha(0f); // This set the initial image completely transparent, when showing the zoom-in image
        //rootView.setAlpha(0.3f);
        expandedImageView.setVisibility(View.VISIBLE);

        // Set the pivot point for SCALE_X and SCALE_Y transformations
        // to the top-left corner of the zoomed-in view (the default
        // is the center of the view).
        expandedImageView.setPivotX(0f);
        expandedImageView.setPivotY(0f);


        // Construct and run the parallel animation of the four translation and
        // scale properties (X, Y, SCALE_X, and SCALE_Y).
        AnimatorSet set = new AnimatorSet();
        set
                .play(ObjectAnimator.ofFloat(expandedImageView, View.X,
                        startBounds.left, finalBounds.left))
                .with(ObjectAnimator.ofFloat(expandedImageView, View.Y,
                        startBounds.top, finalBounds.top))
                .with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_X,
                        startScale, 1f)).with(ObjectAnimator.ofFloat(expandedImageView,
                View.SCALE_Y, startScale, 1f));
        set.setDuration(mShortAnimationDuration);
        set.setInterpolator(new DecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mCurrentAnimator[0] = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mCurrentAnimator[0] = null;
            }
        });
        set.start();
        mCurrentAnimator[0] = set;

        // Upon clicking the zoomed-in image, it should zoom back down
        // to the original bounds and show the thumbnail instead of
        // the expanded image.
        final float startScaleFinal = startScale;
        expandedImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCurrentAnimator[0] != null) {
                    mCurrentAnimator[0].cancel();
                }

                // Animate the four positioning/sizing properties in parallel,
                // back to their original values.
                AnimatorSet set = new AnimatorSet();
                set.play(ObjectAnimator
                        .ofFloat(expandedImageView, View.X, startBounds.left))
                        .with(ObjectAnimator
                                .ofFloat(expandedImageView,
                                        View.Y, startBounds.top))
                        .with(ObjectAnimator
                                .ofFloat(expandedImageView,
                                        View.SCALE_X, startScaleFinal))
                        .with(ObjectAnimator
                                .ofFloat(expandedImageView,
                                        View.SCALE_Y, startScaleFinal));
                set.setDuration(mShortAnimationDuration);
                set.setInterpolator(new DecelerateInterpolator());
                set.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        thumbView.setAlpha(1f);
                        expandedImageView.setVisibility(View.GONE);
                        mCurrentAnimator[0] = null;
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        thumbView.setAlpha(1f);
                        expandedImageView.setVisibility(View.GONE);
                        mCurrentAnimator[0] = null;
                    }
                });
                set.start();
                mCurrentAnimator[0] = set;
            }
        });

    }


    /**
     * Return a string with the status of an auction based on the period of the auction
     * @param tradeTime in millisecond representing the beginning of the auction
     * @param auctionDuration duration in hours of the auction
     * @return
     */
    public static String getExpiration(long tradeTime, int auctionDuration) {
        long nowtime = System.currentTimeMillis();
        long endtime = tradeTime + (auctionDuration * 60 * 60 * 1000);
        if (endtime <= nowtime)
            return "EXPIRED";
        else{
            //calculate time left. \ of millisecond difference
            long timelapse = endtime - nowtime;
            String timeLeft = "";
            if (timelapse < 1000 * 60){ //less than one minute
                timeLeft = String.format(Locale.ENGLISH, "Expires in %d seconds", timelapse / (1000*60) );
            }else if (timelapse < 1000 * 60 * 60){ //less than one hour
                timeLeft = String.format(Locale.ENGLISH, "Expires in %d Minutes", timelapse / (1000*60*60) );
            }else if (timelapse > 1000 * 60 * 60 *24){ //over one hour
                timeLeft = String.format(Locale.ENGLISH, "Expires in %d Hours", timelapse / (1000*60*60*24) );
            }

            return timeLeft;
        }
    }

    public static Bitmap getVideoThumbnail(Context ac, String video_id){
        Bitmap bitmap;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inDither = false;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;

        bitmap = MediaStore.Video.Thumbnails.getThumbnail(ac.getContentResolver(),
                Long.parseLong(video_id),
                MediaStore.Images.Thumbnails.MINI_KIND,
                options);

        return bitmap;
    }
}
