package com.app.android.tensel.utility;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.app.android.tensel.models.Price;
import com.app.android.tensel.models.User;
import com.google.firebase.auth.FirebaseUser;

import java.io.File;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
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
    public static final String CHAT_EVENT = "CHAT_EVENT";
    public static final String ANALYTICS_PARAM_CHATS_ID = "CHAT_ID";
    public static final String ANALYTICS_PARAM_CHAT_NAME = "CHAT_NAME";
    public static final String ANALYTICS_PARAM_CHAT_CATEGORY = "CHAT_CATEGORY";
    public static final String INSTANT_REPLY = "INSTANT_REPLY";
    public static final String TOPIC_FEEDS = "trades";
    public static final String PREF_SHOW_PV_HINTS = "PV_HINTS";

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

    public static Bitmap retriveVideoThumbnail(String videoPath) throws Throwable
    {
        Bitmap bitmap = null;
        MediaMetadataRetriever mediaMetadataRetriever = null;
        try
        {
            mediaMetadataRetriever = new MediaMetadataRetriever();
            //if (Build.VERSION.SDK_INT >= 14)
                mediaMetadataRetriever.setDataSource(videoPath, new HashMap<String, String>());
            //else
              //  mediaMetadataRetriever.setDataSource(videoPath);
            bitmap = mediaMetadataRetriever.getFrameAtTime();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new Throwable(
                    "Exception in retriveVideoFrameFromVideo(String videoPath)"
                            + e.getMessage());

        }
        finally
        {
            if (mediaMetadataRetriever != null)
            {
                mediaMetadataRetriever.release();
            }
        }
        return bitmap;
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
        return getSdCard().getAbsolutePath() + "/Android/data/" + ctx.getPackageName() + "/media/videos";
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
        File newfile = new File(file);
        return newfile.isFile() ? newfile.getName() : null;
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

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public static boolean isMediaDocument(Uri uri) {
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
}
