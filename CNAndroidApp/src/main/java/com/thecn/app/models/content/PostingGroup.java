package com.thecn.app.models.content;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Used to model visibility settings of a user's post.
 */
public class PostingGroup implements Serializable {

    public transient static final PostingGroup allMembers = new PostingGroup("public", "All CN Members");
    public transient static final PostingGroup myColleagues = new PostingGroup("my_colleague", "My Colleagues");
    public transient static final PostingGroup myFollowers = new PostingGroup("my_follower", "My Followers");
    public transient static final PostingGroup onlyMe = new PostingGroup("only_me", "Only Me");

    public transient static final PostingGroup course = new PostingGroup("course", "Course");
    public transient static final PostingGroup conexus = new PostingGroup("conexus", "Conexus");

    public static final PostingGroup[] allGroups = {allMembers, /*myColleagues,*/ myFollowers, onlyMe};

    String mId;
    String mName;

    /**
     * New instance
     * @param id json id for converting to json to send to server
     * @param name used to display group to user
     */
    public PostingGroup(String id, String name) {
        mId = id;
        mName = name;
    }

    public String getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public void setId(String id) {
        mId = id;
    }

    public void setName(String name) {
        mName = name;
    }

    /**
     * Get all ids in a list of posting groups
     * @param list list of posting groups
     * @return string array of the ids in the posting groups.
     */
    public static String[] getIds(ArrayList<PostingGroup> list) {

        String[] ids = new String[list.size()];

        for (int i = 0; i < list.size(); i++) {
            ids[i] = list.get(i).getId();
        }

        return ids;
    }
}
