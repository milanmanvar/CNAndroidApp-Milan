package com.thecn.app.models.notification;

import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Model for user new message construct in server json.
 * This model represents a count of all new notifications that the user has.
 */
public class UserNewMessage implements Serializable {

    private static final int OLD_EMAIL_CACHE_SIZE = 30;

    //for checking whether an alert notification has been sent for a particular notification id
    private ArrayList<String> generalIds;
    private ArrayList<String> emailIds;
    private ArrayList<String> followerIds;

    //temporary hack fix to stave off a bug where email notifications are sent twice
    private ArrayList<String> oldEmailIdCache;

    /**
     * Init all arraylists
     */
    public UserNewMessage() {
        generalIds = new ArrayList<>();
        emailIds = new ArrayList<>();
        followerIds = new ArrayList<>();

        oldEmailIdCache = new ArrayList<>();
    }

    /**
     * Create using the array lists of the other object.
     * @param other other user new message object.
     */
    public UserNewMessage(UserNewMessage other) {
        this();

        if (other != null) {
            generalIds.addAll(other.getGeneralIds());
            emailIds.addAll(other.getEmailIds());
            followerIds.addAll(other.getFollowerIds());
        }
    }

    /**
     * Replaces old array lists with those of the "update" object.
     * Adds old email ids to the old email id cache.
     * RUN IN BACKGROUND THREAD
     * @param update object used to update this object
     */
    public void update(UserNewMessage update) {
        if (update == null) return;

        generalIds = update.generalIds;

        pushOldEmailIds(emailIds);
        emailIds = update.emailIds;

        followerIds = update.followerIds;
    }

    /**
     * Returns true if all notification lists are not null.
     * @return true if valid for use
     */
    public boolean isValid() {
        return !(generalIds == null || emailIds == null || followerIds == null);
    }

    /**
     * Get count
     * @return count of new general notifications
     */
    public int getGenNotificationCount() {
        return generalIds.size();
    }

    /**
     * Get count
     * @return count of new email notifications
     */
    public int getEmailCount() {
        return emailIds.size();
    }

    /**
     * Get count
     * @return count of new follower notifications
     */
    public int getFollowerCount() {
        return followerIds.size();
    }

    /**
     * Get count
     * @return total count of all new notifications
     */
    public int getTotal() {
        return getGenNotificationCount() + getEmailCount() + getFollowerCount();
    }

    /**
     * Clear general notification list
     */
    public void clearGeneralNotifications() {
        generalIds.clear();
    }

    /**
     * Clear email notification list.
     * Add old emails to cache before clearing list.
     */
    public void clearEmailNotifications() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                pushOldEmailIds(new ArrayList<>(emailIds));
            }
        }).start();

        emailIds.clear();
    }

    /**
     * Clear follower notification list
     */
    public void clearFollowerNotifications() {
        followerIds.clear();
    }

    /**
     * Add a general notification id to list
     * @param id id of new notification
     */
    public void addGeneralNotification(String id) {
        generalIds.add(id);
    }

    /**
     * Add an email notification id to list
     * @param id id of new notification
     */
    public void addEmailNotification(String id) {
        emailIds.add(id);
    }

    /**
     * Add follower notification id to list
     * @param id id of new notification
     */
    public void addFollowerNotification(String id) {
        followerIds.add(id);
    }

    /**
     * Tells whether newMessage contains new notification that is not in THIS object.
     * @param newMessage new UserNewMessage object
     * @return true if there is a new notification.
     */
    public boolean hasNewNotification(UserNewMessage newMessage) {
        return hasNewNotification(newMessage.generalIds, generalIds) ||
               hasNewNotification(newMessage.emailIds, emailIds) && hasNewNotification(newMessage.emailIds, oldEmailIdCache) ||
               hasNewNotification(newMessage.followerIds, followerIds);
    }

    //returns true if there is an element in the newList that is not in the old list

    /**
     * Checks if there is an element in newList not present in oldList
     * @param newList list of new string ids
     * @param oldList list of old string ids
     * @return true if there is a new element, false otherwise
     */
    private boolean hasNewNotification(ArrayList<String> newList, ArrayList<String> oldList) {
        boolean equalFound = true;

        for (int i = 0; equalFound && i < newList.size(); i++) {
            equalFound = false;

            for (int j = 0; !equalFound && j < oldList.size(); j++) {
                equalFound = newList.get(i).equals(oldList.get(j));
            }
        }

        return !equalFound;
    }

    /**
     * Push all ids onto the old email cache that are not already in the cache.
     * RUN IN BACKGROUND THREAD
     * @param ids list of prospective ids to push
     */
    private void pushOldEmailIds(ArrayList<String> ids) {
        ArrayList<String> idsCopy = new ArrayList<>(ids);
        ArrayList<String> emailCacheCopy = new ArrayList<>(oldEmailIdCache);

        //make sure no duplicates added to the cache
        for (int i = 0; i < idsCopy.size(); i++) {
            boolean equal = false;

            for (int j = 0; !equal && j < emailCacheCopy.size(); j++) {
                if (idsCopy.get(i).equals(emailCacheCopy.get(j))) {
                    equal = true;
                    //replace duplicate with null
                    idsCopy.set(i, null);
                }
            }
        }

        ArrayList<String> newList = new ArrayList<>();
        newList.ensureCapacity(ids.size() + oldEmailIdCache.size());

        //don't add new id if it has been set to null
        for (String id : idsCopy) {
            if (id != null) newList.add(id);
        }
        newList.addAll(emailCacheCopy);

        //if new list larger than desired cache size, trim it down.
        if (newList.size() > OLD_EMAIL_CACHE_SIZE) {
            newList = new ArrayList<>(newList.subList(0, OLD_EMAIL_CACHE_SIZE));
        }

        //set old email cache to reference new list just created.
        oldEmailIdCache = newList;
    }

    /**
     * Get copy of list
     * @return list of general notification ids
     */
    public ArrayList<String> getGeneralIds() {
        return new ArrayList<>(generalIds);
    }

    /**
     * Get copy of list
     * @return list of email notification ids
     */
    public ArrayList<String> getEmailIds() {
        return new ArrayList<>(emailIds);
    }

    /**
     * Get copy of list
     * @return list of follower notification ids
     */
    public ArrayList<String> getFollowerIds() {
        return new ArrayList<>(followerIds);
    }
}
