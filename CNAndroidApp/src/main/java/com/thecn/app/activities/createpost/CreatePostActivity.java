package com.thecn.app.activities.createpost;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.thecn.app.R;

/**
 * Activity for creating a post
 */

public class CreatePostActivity extends FragmentActivity {

    /**
     * Adds a CreatePostFragment if savedInstanceState null
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fullscreen_container);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, new CreatePostFragment(), CreatePostFragment.TAG)
                    .commit();
        }
    }
}