package comn.example.user.j_trok;

import android.app.Application;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by Larry Akah on 5/27/17.
 */

public class SevenApp extends Application {

    @Override
    public void onCreate() {
        FirebaseApp.initializeApp(this);
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        super.onCreate();
    }

}
