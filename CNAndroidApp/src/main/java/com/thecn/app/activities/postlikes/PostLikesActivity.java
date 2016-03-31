package com.thecn.app.activities.postlikes;

import android.os.Bundle;

import com.thecn.app.R;
import com.thecn.app.activities.navigation.NavigationActivity;
import com.thecn.app.models.content.Post;

/**
 * Shows a list of people who have liked a post.
 */
public class PostLikesActivity extends NavigationActivity {

    private static final String TAG = PostLikesActivity.class.getSimpleName();

    /**
     * Set action bar, get data, add fragment.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setActionBarAndTitle("Post Likes");

        Post post = (Post) getIntent().getSerializableExtra("post");

        hideProgressBar();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, PostLikesFragment.newInstance(post))
                    .commit();
        }
    }
}
