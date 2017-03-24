package comn.example.user.j_trok;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class ItemDetail extends AppCompatActivity {
    TextView terms_and_conditions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);

        terms_and_conditions = (TextView) findViewById(R.id.terms_and_conditions);
        // hide until its title is clicked
        terms_and_conditions.setVisibility(View.GONE);
    }
        /**
         * onClick handler
         */
    public void toggle_contents(View v){
        terms_and_conditions.setVisibility( terms_and_conditions.isShown()
                ? View.GONE
                : View.VISIBLE );
    }
}
