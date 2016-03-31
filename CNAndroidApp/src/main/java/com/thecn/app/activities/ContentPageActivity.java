package com.thecn.app.activities;

import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.LayoutParams;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.thecn.app.R;
import com.thecn.app.activities.navigation.NavigationActivity;

/**
 * Used for such content as Course, and Conexus pages
 * contains a SlidingUpPanelLayout for switching between pages
 * One fragment is on static pane, all other fragments are interchanged
 * using buttons on the sliding pane.
 * todo this is stupid, do something better
 */
public abstract class ContentPageActivity extends NavigationActivity {

    private SlidingUpPanelLayout slidingLayout;

    private FragmentPackage staticFragmentPkg; //fragment on static pane
    private FragmentPackage[] fragmentPkgs; //all other fragments on sliding pane
    private int currentFragmentIndex;

    private RadioGroup buttonLayout;
    private LinearLayout lPost;

    /**
     * Used to get fragments from sub class
     */
    public interface FragmentCallback {
        public Fragment getFragment();
    }

    /**
     * Class to associate a Fragment, its name,
     * and its callback object (for getting the fragment itself)
     */
    public static class FragmentPackage {
        private String fragmentName;
        private String fragmentKey;
        private Fragment fragment;
        private FragmentCallback fragmentCallback;

        public FragmentPackage(String fragmentName, String fragmentKey, FragmentCallback fragmentCallback) {
            this.fragmentName = fragmentName;
            this.fragmentKey = fragmentKey;
            this.fragmentCallback = fragmentCallback;
        }

        public String getFragmentKey() { return fragmentKey; }

        public String getFragmentName() {
            return fragmentName;
        }

        public Fragment getFragment() {
            return fragment;
        }

        public void setFragment(Fragment fragment) {
            this.fragment = fragment;
        }

        public Fragment setFragmentFromCallback() {
            return fragment = fragmentCallback.getFragment();
        }

        public boolean isFragmentSet() {
            return fragment != null;
        }
    }

    /**
     * Get sliding layout, set up views and fragments.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        NavigationActivity.putWholePageResult(this, true);

        slidingLayout = (SlidingUpPanelLayout)
                findViewById(R.id.sliding_layout);
        slidingLayout.setSlidingEnabled(false);
        slidingLayout.setPanelHeight(getResources().getDimensionPixelSize(R.dimen.post_button_height));
        lPost = (LinearLayout) findViewById(R.id.actual_post_button);

        //both of these methods specified by child class
        staticFragmentPkg = getStaticFragmentPackage();
        fragmentPkgs = getFragmentPackages();

        if (savedInstanceState != null) {
            //get fragments already created
            currentFragmentIndex = savedInstanceState.getInt(FRAGMENT_INDEX_TAG);

//            unbundleFragment(savedInstanceState, staticFragmentPkg);
            for (FragmentPackage pkg : fragmentPkgs) {
                unbundleFragment(savedInstanceState, pkg);
            }


        } else {
            //specify no fragment yet chosen
            currentFragmentIndex = -1;
        }

        createButtons();

        lPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pushCreatePostActivity();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (slidingLayout.isExpanded()) {
            //collapse sliding layout if expanded
            slidingLayout.collapsePane();
            buttonLayout.check(0);
        } else {
            super.onBackPressed();
        }
    }

    private static final String FRAGMENT_INDEX_TAG = "FRAGMENT_INDEX";

    /**
     * Add fragment packages to the outState and the currently selected fragment
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        bundleFragmentPkg(outState, staticFragmentPkg);
        for (FragmentPackage pkg : fragmentPkgs) {
            bundleFragmentPkg(outState, pkg);
        }

        outState.putInt(FRAGMENT_INDEX_TAG, currentFragmentIndex);
    }

    /**
     * Adds fragment to support fragment manager
     * @param outState bundle to add fragment with
     * @param pkg fragment and meta data
     */
    private void bundleFragmentPkg(Bundle outState, FragmentPackage pkg) {
        if (pkg.isFragmentSet()) {
            String key = pkg.fragmentKey;
            Fragment fragment = pkg.fragment;
            getSupportFragmentManager().putFragment(outState, key, fragment);
        }
    }

    /**
     * Get back fragment from savedInstanceState, set it inside fragment package object
     * @param savedInstanceState bundle to get fragment from
     * @param pkg package to add fragment to
     */
    private void unbundleFragment(Bundle savedInstanceState, FragmentPackage pkg) {
        Fragment fragment = getSupportFragmentManager()
                .getFragment(savedInstanceState, pkg.fragmentKey);

        pkg.setFragment(fragment);
    }

    /**
     * Used to populate RadioGroup with radio buttons dynamically,
     * based on the number of fragments specified by the child class
     */
    private void createButtons() {
        buttonLayout = (RadioGroup) findViewById(R.id.controls_container);

        //this is a RadioGroup.LayoutParams
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        params.weight = 1;

        RadioButton buttonHolder;

        buttonHolder = getRadioButton(staticFragmentPkg);
        buttonHolder.setId(0);
        buttonHolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                slidingLayout.collapsePane();
            }
        });
        buttonLayout.addView(buttonHolder, params);

        for (int i = 0; i < fragmentPkgs.length; i++) {
            final FragmentPackage fp = fragmentPkgs[i];
            final int index = i;

            buttonHolder = getRadioButton(fp);
            buttonHolder.setId(i + 1);
            buttonHolder.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //on click, switch to this fragment in top pane
                    //and slide the top pane over the bottom pane
                    openFragment(index);
                    slidingLayout.expandPane();
                }
            });

            buttonLayout.addView(buttonHolder, params);
        }

        buttonLayout.check(currentFragmentIndex + 1);
    }

    /**
     * Helper method for createButtons.  Customizes button.
     * @param fp fragment package that contains text to set in button
     * @return the instantiated RadioButton
     */
    private RadioButton getRadioButton(FragmentPackage fp) {
        RadioButton button = new RadioButton(this);

        button.setBackgroundResource(R.drawable.group_fragment_button);
        button.setButtonDrawable(new StateListDrawable()); //removes circle part of button
        button.setText(fp.getFragmentName());
        button.setGravity(Gravity.CENTER);

        return button;
    }

    /**
     * Sets initial fragments shown in layout
     * @param index index of fragment to add to sliding pane
     */
    public void initFragments(int index) {

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container_below, staticFragmentPkg.setFragmentFromCallback())
                .commit();

        //openFragment(index);
    }

    /**
     * Overridden to use different layout
     */
    @Override
    protected void mySetContentView() {
        setContentView(R.layout.activity_content_page);
    }

    /**
     * Used by subclasses to specify static fragment on static pane
     * Any relevant fields MUST be instantiated if specified in this method (duh)
     * @return one fragment to rule them all
     */
    protected abstract FragmentPackage getStaticFragmentPackage();

    /**
     * Used by subclasses to specify fragments to open by index
     * Any relevant fields MUST be instantiated if specified in this method (duh)
     * @return list of Fragments grouped with their names and instance functions
     */
    protected abstract FragmentPackage[] getFragmentPackages();

    /**
     * Opens a fragment in the sliding view
     * @param index index of fragment
     */
    public void openFragment(int index) {

        int numFragments = fragmentPkgs.length;

        //if proper index
        if (-1 < index && index < numFragments) {
            Fragment fragment = fragmentPkgs[index].getFragment();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

            if (fragment == null) {
                //create fragment if does not exist
                fragment = fragmentPkgs[index].setFragmentFromCallback();
                transaction.add(R.id.container_above, fragment);
            } else {
                //show fragment if exists.
                transaction.show(fragment);
            }

            //iterates through all other fragments in view and hides them
            //uses modular arithmetic
            for (int i = (index + 1) % numFragments;
                 i != index;
                 i = (i + 1) % numFragments) {

                Fragment fragmentToHide = fragmentPkgs[i].getFragment();

                if (fragmentToHide != null) {
                    transaction.hide(fragmentToHide);
                }

            }

            transaction.commit();
            currentFragmentIndex = index;
        }
    }

    /**
     * Get the index of fragment currently displayed
     * @return index of fragment
     */
    protected int getCurrentFragmentIndex() {
        return currentFragmentIndex;
    }
}
