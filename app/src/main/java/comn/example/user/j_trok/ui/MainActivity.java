package comn.example.user.j_trok.ui;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;

import com.joanfuentes.hintcase.HintCase;
import com.joanfuentes.hintcaseassets.contentholderanimators.FadeInContentHolderAnimator;
import com.joanfuentes.hintcaseassets.contentholderanimators.SlideInFromRightContentHolderAnimator;
import com.joanfuentes.hintcaseassets.contentholderanimators.SlideOutFromRightContentHolderAnimator;
import com.joanfuentes.hintcaseassets.hintcontentholders.SimpleHintContentHolder;
import com.joanfuentes.hintcaseassets.shapeanimators.RevealCircleShapeAnimator;
import com.joanfuentes.hintcaseassets.shapeanimators.UnrevealCircleShapeAnimator;
import com.joanfuentes.hintcaseassets.shapes.CircularShape;
import com.popalay.tutors.Tutors;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import comn.example.user.j_trok.R;
import comn.example.user.j_trok.fragments.BuyingFragment;
import comn.example.user.j_trok.fragments.ProfileFragment;
import comn.example.user.j_trok.fragments.SellingFragment;
import comn.example.user.j_trok.utility.PrefManager;

public class MainActivity extends AppCompatActivity {

    private static final String LOGTAG = "MAinActivity";
    Map<String, Fragment> fragments = new HashMap<>();
    private Tutors tutors;
    private Iterator<Map.Entry<String, View>> iterator;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bottomNavigationView = (BottomNavigationView)
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
                                break;
                            case R.id.action_selling:
                                fragment = "selling";
                                break;
                            case R.id.action_profile:
                                fragment = "profile";
                                break;
                        }
                        selectedFragment = fragments.get(fragment);
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

    @Override
    protected void onStart() {
        super.onStart();
        boolean showHints = new PrefManager(this).getShouldShowHints();
        if (showHints)
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    showHint();
                }
            }, 500);
    }

    private void showHint() {
        SimpleHintContentHolder hintBlock = new SimpleHintContentHolder.Builder(findViewById(R.id.action_buying).getContext())
                .setContentTitle("Need to sell awesome stuff? ")
                .setContentText("Looking to make an awesome sale. Here you come! In 7s the deal is done!")
                .setTitleStyle(R.style.MaterialStyledDialogs_TitleHeader)
                .setContentStyle(R.style.MaterialStyledDialogs_Description)
                .build();
        new HintCase(findViewById(R.id.action_buying).getRootView().getRootView())
                .setTarget(findViewById(R.id.action_buying), new CircularShape(), HintCase.TARGET_IS_NOT_CLICKABLE)
                .setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.bg_screen2, null))
                .setShapeAnimators(new RevealCircleShapeAnimator(), new UnrevealCircleShapeAnimator())
                .setHintBlock(hintBlock, new FadeInContentHolderAnimator(), new SlideOutFromRightContentHolderAnimator())
                .setOnClosedListener(new HintCase.OnClosedListener() {
                    @Override
                    public void onClosed() {
                        SimpleHintContentHolder secondHintBlock = new SimpleHintContentHolder.Builder(findViewById(R.id.action_selling).getContext())
                                .setContentTitle("Buy awesome stuff!")
                                .setContentText("Looking for awesome stuff to buy? Here they are! ")
                                .setTitleStyle(R.style.MaterialStyledDialogs_TitleHeader)
                                .setContentStyle(R.style.MaterialStyledDialogs_Description)
                                .build();
                        new HintCase(findViewById(R.id.action_selling).getRootView().getRootView())
                                .setTarget(findViewById(R.id.action_selling), new CircularShape())
                                .setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.bg_screen3, null))
                                .setShapeAnimators(new RevealCircleShapeAnimator(), new UnrevealCircleShapeAnimator())
                                .setHintBlock(secondHintBlock, new SlideInFromRightContentHolderAnimator())
                                .setOnClosedListener(new HintCase.OnClosedListener() {
                                    @Override
                                    public void onClosed() {
                                        SimpleHintContentHolder thirdHintBlock = new SimpleHintContentHolder.Builder(findViewById(R.id.action_profile).getContext())
                                                .setContentTitle("Your great profile!")
                                                .setContentText("Let others know that they can indeed trust you! Complete your profile")
                                                .setTitleStyle(R.style.MaterialStyledDialogs_TitleHeader)
                                                .setContentStyle(R.style.MaterialStyledDialogs_Description)
                                                .build();
                                        new HintCase(findViewById(R.id.action_profile).getRootView().getRootView())
                                                .setTarget(findViewById(R.id.action_profile), new CircularShape())
                                                .setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.dot_dark_screen2, null))
                                                .setShapeAnimators(new RevealCircleShapeAnimator(), new UnrevealCircleShapeAnimator())
                                                .setHintBlock(thirdHintBlock, new SlideInFromRightContentHolderAnimator())
                                                .show();
                                        new PrefManager(MainActivity.this).setShouldShowHints(false);
                                    }
                                })
                                .show();
                    }
                })
                .show();
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