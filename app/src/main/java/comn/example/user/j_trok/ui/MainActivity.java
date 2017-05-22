package comn.example.user.j_trok.ui;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;

import com.joanfuentes.hintcase.HintCase;
import com.joanfuentes.hintcase.ShapeAnimator;
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
        showHint();
    }

    private void showHint() {
        final View actionBuy = findViewById(R.id.action_buying);
        SimpleHintContentHolder hintBlock = new SimpleHintContentHolder.Builder(actionBuy.getContext())
                .setContentTitle("Need to sell awesome stuff? ")
                .setContentText("Looking to make an awesome sale. Here you come! In 7s the deal is done!")
                .build();
        new HintCase(actionBuy.getRootView().getRootView())
                .setTarget(actionBuy, new CircularShape(), HintCase.TARGET_IS_NOT_CLICKABLE)
                .setBackgroundColor(getResources().getColor(R.color.bg_screen2))
                .setShapeAnimators(new RevealCircleShapeAnimator(), ShapeAnimator.NO_ANIMATOR)
                .setHintBlock(hintBlock, new FadeInContentHolderAnimator(), new SlideOutFromRightContentHolderAnimator())
                .setOnClosedListener(new HintCase.OnClosedListener() {
                    @Override
                    public void onClosed() {
                        SimpleHintContentHolder secondHintBlock = new SimpleHintContentHolder.Builder(findViewById(R.id.action_selling).getContext())
                                .setContentTitle("Buy awesome stuff!")
                                .setContentText("Looking for awesome stuff to buy? Here they are! ")
                                .setTitleStyle(R.style.MaterialStyledDialogs_TitleHeader)
                                .build();
                        new HintCase(findViewById(R.id.action_selling).getRootView().getRootView())
                                .setTarget(findViewById(R.id.action_selling), new CircularShape())
                                .setBackgroundColor(getResources().getColor(R.color.bg_screen3))
                                .setShapeAnimators(ShapeAnimator.NO_ANIMATOR, new UnrevealCircleShapeAnimator())
                                .setHintBlock(secondHintBlock, new SlideInFromRightContentHolderAnimator())
                                .setOnClosedListener(new HintCase.OnClosedListener() {
                                    @Override
                                    public void onClosed() {
                                        SimpleHintContentHolder thirdHintBlock = new SimpleHintContentHolder.Builder(findViewById(R.id.action_selling).getContext())
                                                .setContentTitle("Buy awesome stuff!")
                                                .setContentText("Looking for awesome stuff to buy? Here they are! ")
                                                .setTitleStyle(R.style.MaterialStyledDialogs_TitleHeader)
                                                .build();
                                        new HintCase(findViewById(R.id.action_selling).getRootView().getRootView())
                                                .setTarget(findViewById(R.id.action_selling), new CircularShape())
                                                .setBackgroundColor(getResources().getColor(R.color.dot_dark_screen2))
                                                .setShapeAnimators(ShapeAnimator.NO_ANIMATOR, new UnrevealCircleShapeAnimator())
                                                .setHintBlock(thirdHintBlock, new SlideInFromRightContentHolderAnimator())
                                                .show();
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