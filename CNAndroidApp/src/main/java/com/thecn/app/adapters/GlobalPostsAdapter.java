package com.thecn.app.adapters;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.thecn.app.AppSession;
import com.thecn.app.fragments.BaseGlobalPostListFragment;
import com.thecn.app.models.content.Post;
import com.thecn.app.tools.CallbackManager;
import com.thecn.app.tools.controllers.PostViewController;

import java.math.BigInteger;
import java.util.ArrayList;

/**
 * Used to display a list of posts.
 */
public class GlobalPostsAdapter extends BaseAdapter {

    private ArrayList<Post> posts = new ArrayList<Post>(); //data
    private CallbackManager<BaseGlobalPostListFragment> manager;

    public GlobalPostsAdapter(CallbackManager<BaseGlobalPostListFragment> manager) {
        this.manager = manager;
    }

    @Override
    public int getCount() {
        return posts.size();
    }

    @Override
    public Post getItem(int position) {
        return posts.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * Add post to end of list, update view
     * @param post post to add
     */
    public void add(Post post) {
        posts.add(post);
        notifyDataSetChanged();
    }

    /**
     * Add post to list at index, update view
     * @param index position where should be added
     * @param post post to add
     */
    public void add(int index, Post post) {
        posts.add(index, post);
        notifyDataSetChanged();
    }

    /**
     * Add all posts to end of list, update view
     * @param posts posts to add
     */
    public void addAll(ArrayList<Post> posts) {
        this.posts.addAll(posts);
        notifyDataSetChanged();
    }

    /**
     * Replace post if has same id as one in list.
     * @param newPost post to replace old post with.
     */
    public void replacePost(Post newPost) {
        BigInteger id = newPost.getIntegerID();
        if (id == null) return;

        for (int i = 0; i < posts.size(); i++) {
            BigInteger otherID = posts.get(i).getIntegerID();
            if (otherID != null && otherID.equals(id)) {
                set(i, newPost);
                break;
            }
        }
    }

    /**
     * Remove post from list if id matches one given.
     * @param post get id from this post object
     * @return true if removed.
     */
    public boolean removePost(Post post) {
        BigInteger id = post.getIntegerID();
        if (id == null) return false;

        for (int i = 0; i < posts.size(); i++) {
            BigInteger otherID = posts.get(i).getIntegerID();
            if (otherID != null && otherID.equals(id)) {
                remove(i);
                return true;
            }
        }

        return false;
    }

    /**
     * Get post at index.
     * @param index index of post to get
     * @return post
     */
    public Post get(int index) {
        return posts.get(index);
    }

    /**
     * Get list used in adapter
     * @return array list of posts
     */
    public ArrayList<Post> getList() {
        return posts;
    }

    /**
     * Set post at position.
     * @param index position to set post into
     * @param post data to add
     */
    public void set(int index, Post post) {
        posts.set(index, post);
        notifyDataSetChanged();
    }

    /**
     * Remove post at index
     * @param index index of post to remove
     * @return true if removed
     */
    public boolean remove(int index) {
        Post removedPost = posts.remove(index);
        notifyDataSetChanged();
        return removedPost != null;
    }

    /**
     * Remove all posts from list, update view.
     */
    public void clear() {
        posts.clear();
        notifyDataSetChanged();
    }

    /**
     * Use {@link PostViewController} to set up view.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final Post post = getItem(position);
        if (post.getUser().isMe()) {
            post.setUser(AppSession.getInstance().getUser());
        }

        PostViewController holder;

        if (convertView == null) {

            holder = new PostViewController(manager, parent);
            convertView = holder.getRootView();

            convertView.setTag(holder);

        } else {
            holder = (PostViewController) convertView.getTag();
        }

        holder.setUpView(post, position);

        return convertView;
    }
}
