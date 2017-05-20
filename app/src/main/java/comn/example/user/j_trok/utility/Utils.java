package comn.example.user.j_trok.utility;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;

/**
 * Created by Larry Akah on 5/19/17.
 */

public class Utils {

    private static final String TAG = Utils.class.getName();
    public static final String PREF_RECORDING_DURATION = "RECORDING_DURATION";
    public static final String PREF_SHOW_HINTS = "SHOW_HINTS_PREF";

    public static void deleteFilesAtPath( File parentDir )
    {
        File[] files = parentDir.listFiles();
        if ( files != null && files.length > 0 )
        {
            Log.d(TAG, "Deleting Empty Files in " + parentDir );
            for ( File file: files )
            {
                if ( file.getName().endsWith( ".mp4" ) && file.length() == 0 )
                {

                    File myFile = new File( parentDir + "/" + file.getName() );
                    Log.d(TAG, "Delete file: " + myFile.getAbsolutePath());
                    myFile.delete();
                }
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
}
