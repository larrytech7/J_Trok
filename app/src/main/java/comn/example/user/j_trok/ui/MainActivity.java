package comn.example.user.j_trok.ui;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;

import comn.example.user.j_trok.R;
import comn.example.user.j_trok.fragments.BuyingFragment;
import comn.example.user.j_trok.fragments.ProfileFragment;
import comn.example.user.j_trok.fragments.SellingFragment;

public class MainActivity extends AppCompatActivity {

    private static final String LOGTAG = "MAinActivity";
    private FragmentTransaction transaction;

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
                        //toggleIcon(item);
                        switch (item.getItemId()) {
                            case R.id.action_buying:
                                selectedFragment = getSupportFragmentManager().findFragmentByTag("buying");
                                transaction.addToBackStack("buying");
                                break;
                            case R.id.action_selling:
                                selectedFragment = getSupportFragmentManager().findFragmentByTag("selling");
                                transaction.addToBackStack("selling");
                                break;
                            case R.id.action_profile:
                                selectedFragment = getSupportFragmentManager().findFragmentByTag("profile");
                                transaction.addToBackStack("profile");
                                break;
                        }
                        transaction.replace(R.id.frame_layout, selectedFragment)
                                .commit();
                        return true;
                    }
                });

        //Manually displaying the first fragment - one time only
        transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_layout, SellingFragment.newInstance(), "selling")
                .replace(R.id.frame_layout, ProfileFragment.newInstance(), "profile")
                .replace(R.id.frame_layout, BuyingFragment.newInstance(), "buying")
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