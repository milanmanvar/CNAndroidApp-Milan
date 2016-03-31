package com.thecn.app.activities.homefeed;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.thecn.app.AppSession;
import com.thecn.app.R;
import com.thecn.app.activities.navigation.NavigationActivity;
import com.thecn.app.fragments.BasePostListFragment;
import com.thecn.app.tools.network.PostLoader;

/**
* Created by philjay on 3/2/15.
*/
public class HomeFeedFragment extends BasePostListFragment {

    public static final String TAG = HomeFeedFragment.class.getSimpleName();

    //different types of filters that can be applied to the Home Feed
    public static final String NEW_POSTS = "New Posts";
    public static final String NEW_REFLECTIONS = "New Reflections";
    public static final String MOST_LIKED = "Most Liked Posts";
    public static final String MOST_REFLECTED = "Most Reflected Posts";
    public static final String MOST_VISITED = "Most Visited Links";
    public static final String INSTRUCTOR_POSTS = "Posts from My Instructors";
    public static final String FOLLOWING = "Following";
    public static final String MY_POSTS = "My Posts";
    public static final String REFLECTIONS_MY_POSTS = "Reflections on My Posts";
    public static final String PUBLIC_POSTS = "Public Posts";
    public static final String POST_OF_WEEK = "Post of the Week";
    public static final String ADMIN = "CN Admin Posts";
    public static final String HIGHLIGHT = "Highlighted Posts";

    @Override
    public PostLoader.MethodList getPostLoaderMethodList() {
        PostLoader.MethodList list = new PostLoader.MethodList();
        list.ensureCapacity(11);
        PostLoader.Method method;

        method = new PostLoader.Method(PostLoader.SOURCE_HOME);
        method.name = NEW_POSTS;
        list.add(method);

        method = new PostLoader.Method(PostLoader.SOURCE_HOME);
        method.name = NEW_REFLECTIONS;
        method.filterType = PostLoader.FILTER_NEW_REFLECTIONS;
        list.add(method);

        method = new PostLoader.Method(PostLoader.SOURCE_HOME);
        method.name = MOST_LIKED;
        method.filterType = PostLoader.FILTER_MOST_LIKED;
        list.add(method);

        method = new PostLoader.Method(PostLoader.SOURCE_HOME);
        method.name = MOST_REFLECTED;
        method.filterType = PostLoader.FILTER_MOST_REFLECTED;
        list.add(method);

        method = new PostLoader.Method(PostLoader.SOURCE_HOME);
        method.name = MOST_VISITED;
        method.filterType = PostLoader.FILTER_MOST_VISITED;
        list.add(method);

        list.startSecondList();

        method = new PostLoader.Method(PostLoader.SOURCE_INSTRUCTOR);
        method.name = INSTRUCTOR_POSTS;
        list.add(method);

        method = new PostLoader.Method(PostLoader.SOURCE_FOLLOWING);
        method.name = FOLLOWING;
        list.add(method);

        String myID = AppSession.getInstance().getUser().getId();

        method = new PostLoader.Method(PostLoader.SOURCE_USER);
        method.name = MY_POSTS;
        method.id = myID;
        list.add(method);

        method = new PostLoader.Method(PostLoader.SOURCE_USER);
        method.name = REFLECTIONS_MY_POSTS;
        method.id = myID;
        method.filterType = PostLoader.FILTER_NEW_REFLECTIONS;
        list.add(method);

        method = new PostLoader.Method(PostLoader.SOURCE_PUBLIC);
        method.name = PUBLIC_POSTS;
        list.add(method);

        method = new PostLoader.Method(PostLoader.SOURCE_TOP);
        method.name = POST_OF_WEEK;
        method.period = PostLoader.PERIOD_WEEK;
        list.add(method);

        method = new PostLoader.Method(PostLoader.SOURCE_ADMIN);
        method.name = ADMIN;
        list.add(method);

        method = new PostLoader.Method(PostLoader.SOURCE_HIGHLIGHT);
        method.name = HIGHLIGHT;
        list.add(method);

        list.setShowContentToggle(true);

        return list;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        addPostButton(R.id.post_button, R.id.header_post_button);
//        int buttonHeight = (int) getResources().getDimension(R.dimen.post_button_height);
//        getListView().setPadding(0, buttonHeight, 0 , 0);
    }

    @Override
    public View getRootView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_post_list, null);
    }

    @Override
    public String toString() {
        return TAG;
    }

    @Override
    public void onResume() {
        Intent intent = getActivity().getIntent();
        if (intent.getBooleanExtra(NavigationActivity.REFRESH_FLAG_KEY, false)) {
            intent.putExtra(NavigationActivity.REFRESH_FLAG_KEY, false);
            resetFilterAndRefresh();
        }
        super.onResume();
//        showPostButton();
    }
}
