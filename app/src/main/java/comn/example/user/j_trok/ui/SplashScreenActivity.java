package comn.example.user.j_trok.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import comn.example.user.j_trok.R;

public class SplashScreenActivity extends AppCompatActivity {

    private TextView sloganTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        sloganTextView = (TextView) findViewById(R.id.textViewSlogan);

        new CountDownTimer(3000, 1000){

            int i = 7;

            @Override
            public void onTick(long l) {
                //change the text in the splash message

                sloganTextView.setText(getString(R.string.slogan, i--));
            }

            @Override
            public void onFinish() {
                Intent intent = new Intent(SplashScreenActivity.this,IntroSliderActivity.class);
                startActivity(intent);
                SplashScreenActivity.this.finish();
            }
        }.start();
    }

}