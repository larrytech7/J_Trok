package comn.example.user.j_trok.ui;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;

import java.util.HashMap;
import java.util.Map;

import comn.example.user.j_trok.R;
import comn.example.user.j_trok.fragments.BuyingFragment;
import comn.example.user.j_trok.fragments.ProfileFragment;
import comn.example.user.j_trok.fragments.SellingFragment;

public class MainActivity extends AppCompatActivity {

    private static final String LOGTAG = "MAinActivity";
    Map<String, Fragment> fragments = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final BottomNavigationView bottomNavigationView = (BottomNavigationView)
                findViewById(R.id.navigation);

        bottomNavigationView.setOnNavigationItemSelectedListener
                (new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        Fragment selectedFragment = null;
                        FragmentTransaction transaction  = getSupportFragmentManager().beginTransaction();
                        String fragment = "";
                        //toggleIcon(item);
                        switch (item.getItemId()) {
                            case R.id.action_buying:
                                fragment = "buying";
                                selectedFragment =  fragments.get(fragment);
                                break;
                            case R.id.action_selling:
                                fragment = "selling";
                                selectedFragment = fragments.get(fragment);
                                break;
                            case R.id.action_profile:
                                fragment = "profile";
                                selectedFragment = fragments.get(fragment);
                                break;
                        }
                        transaction.replace(R.id.frame_layout, selectedFragment, fragment)
                                .commit();
                        return true;
                    }
                });

        fragments = new HashMap<>();
        fragments.put("selling", SellingFragment.newInstance());
        fragments.put("buying", BuyingFragment.newInstance());
        fragments.put("profile", ProfileFragment.newInstance());
        //Manually displaying the first fragment - one time only
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frame_layout, fragments.get("buying"), "buying")
                .commit();

    }

    private void toggleIcon(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_selling:
                item.setIcon(getResources().getDrawable(R.drawable.ic_sell_selected));
                break;
            case R.id.action_buying:
                item.setIcon(getResources().getDrawable(R.drawable.ic_shop_selected));
                break;
            case R.id.action_profile:
                item.setIcon(getResources().getDrawable(R.drawable.ic_profile_selected));
                break;
        }
    }
}