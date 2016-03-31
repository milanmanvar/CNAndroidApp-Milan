package com.thecn.app.tools.controllers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

import com.thecn.app.AppSession;
import com.thecn.app.activities.homefeed.HomeFeedFragment;
import com.thecn.app.activities.profile.ProfileHomeFragment;
import com.thecn.app.activities.conexus.ConexusPostsFragment;
import com.thecn.app.activities.course.CoursePostsFragment;
import com.thecn.app.models.conexus.Conexus;
import com.thecn.app.models.course.Course;
import com.thecn.app.models.content.Post;

/**
 * Controller used to handle changes to posts through broadcast managers.
 */
public class PostChangeController {

    private static final String UPDATE_FILTER = "post_update";
    private static final String DELETE_FILTER = "post_delete";

    private BroadcastReceiver addReceiver;
    private BroadcastReceiver updateReceiver;
    private BroadcastReceiver deleteReceiver;

    /**
     * Interface implemented by fragments, etc, for performing actions
     * when a post is changed/added/deleted
     */
    public static interface Listener {
        public void onPostUpdated(Post post);
        public void onPostAdded(Post post);
        public void onPostDeleted(Post post);
    }

    /**
     * Set up.  Create new broadcast receivers and register them with local broadcast manager
     * @param listener listener implementing interface.
     * @param context used to get local broadcast manager, etc.
     * @param id id of post
     */
    public void registerReceivers(final Listener listener, Context context, final String id) {
        addReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Post post = (Post) intent.getSerializableExtra("post");
                listener.onPostAdded(post);
            }
        };

        updateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Post post = (Post) intent.getSerializableExtra("post");
                listener.onPostUpdated(post);
            }
        };

        deleteReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Post post = (Post) intent.getSerializableExtra("post");
                listener.onPostDeleted(post);
            }
        };

        //register receivers
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(context);
        manager.registerReceiver(addReceiver, new IntentFilter(id));
        manager.registerReceiver(updateReceiver, new IntentFilter(UPDATE_FILTER));
        manager.registerReceiver(deleteReceiver, new IntentFilter(DELETE_FILTER));
    }

    /**
     * Unregister post change broadcast receivers
     * @param context used to get local broadcast manager
     */
    public void unregisterReceivers(Context context) {
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(context);
        manager.unregisterReceiver(addReceiver);
        manager.unregisterReceiver(updateReceiver);
        manager.unregisterReceiver(deleteReceiver);
    }

    /**
     * Send a broadcast that this post was just added.
     * @param post the newly added post
     */
    public static void sendAddedBroadcast(Post post) {
        Context context = AppSession.getInstance().getApplicationContext();

        if (post != null && context != null) {

            broadcastAddedIntent(post, HomeFeedFragment.TAG, context);

            //post only "added" when user has posted it, so only send to user profile
            broadcastAddedIntent(
                    post,
                    ProfileHomeFragment.TAG + AppSession.getInstance().getUser().getId(),
                    context
            );

            //send to all courses user is member of
            String[] courseIDs = Course.getIds(post.getCourses());
            if (courseIDs != null) {
                for (String courseID : courseIDs) {
                    if (courseID != null) {
                        broadcastAddedIntent(
                                post,
                                CoursePostsFragment.TAG + courseID,
                                context
                        );
                    }
                }
            }

            //send to all conexuses user is member of
            String[] conexusIDs = Conexus.getIds(post.getConexuses());
            if (conexusIDs != null) {
                for (String conexusID : conexusIDs) {
                    if (conexusID != null) {
                        broadcastAddedIntent(
                                post,
                                ConexusPostsFragment.TAG + conexusID,
                                context
                        );
                    }
                }
            }
        }
    }

    /**
     * Send broadcast that post was added.
     * @param post added post
     * @param filter intent filter to use
     * @param context used to get local broadcast manager
     */
    public static void broadcastAddedIntent(Post post, String filter, Context context) {
        Intent intent = new Intent(filter);
        intent.putExtra("post", post);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    /**
     * Send broadcast that post was updated
     * @param post the post updated
     * @param id extra identifier
     */
    public static void sendUpdatedBroadcast(Post post, String id) {
        Context context = AppSession.getInstance().getApplicationContext();

        if (post != null && context != null) {
            Intent intent = new Intent(UPDATE_FILTER);
            intent.putExtra("post", post);
            intent.putExtra("id", id);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        }
    }

    /**
     * Send broadcast that post was deleted
     * @param post the post that was deleted
     * @param id extra identifier
     */
    public static void sendDeletedBroadcast(Post post, String id) {
        Context context = AppSession.getInstance().getApplicationContext();

        if (post != null && context != null) {
            Intent intent = new Intent(DELETE_FILTER);
            intent.putExtra("post", post);
            intent.putExtra("id", id);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        }
    }
}
