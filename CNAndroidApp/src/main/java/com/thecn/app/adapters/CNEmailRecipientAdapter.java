package com.thecn.app.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.SystemClock;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.ex.chips.BaseRecipientAdapter;
import com.android.ex.chips.DropdownChipLayouter;
import com.android.ex.chips.RecipientEntry;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.thecn.app.models.user.User;
import com.thecn.app.stores.ResponseCallback;
import com.thecn.app.stores.SearchStore;
import com.thecn.app.stores.StoreUtil;
import com.thecn.app.tools.volley.MyVolley;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Used in {@link com.thecn.app.activities.composeemail.ComposeEmailActivity} to
 * get information and layout for listed search results in {@link com.thecn.app.views.CNEmailRecipientEditTextView}
 */
public class CNEmailRecipientAdapter extends BaseRecipientAdapter {

    private volatile long lastRequestTime = 0;
    private Timer mRequestTimer = new Timer(); //timer for sending search queries to server
    private final Object mRequestTimerLock = new Object();

    private SearchRequestTask mRequestTask; //timer task to run when search timer runs out
    private final Object mRequestTaskLock = new Object();

    private volatile String mSearchKeywords; //holds keywords used in last search

    private volatile boolean mWaitingForReply = false;

    /**
     * A timer task that makes a search request using the text content
     */
    private class SearchRequestTask extends TimerTask {
        @Override
        public void run() {
            synchronized (mRequestTaskLock) {
                if (mSearchKeywords != null) {
                    //always uses the current search keywords
                    makeSearchRequest();
                    mRequestTask = null;
                }
            }
        }
    }

    /**
     * New instance
     * @param context passes to superclass
     */
    public CNEmailRecipientAdapter(Context context) {
        super(context);
    }

    /**
     * Performs filtering in separate thread if {@link #mSearchKeywords} not null
     */
    public void performFiltering() {
        if (mSearchKeywords != null) {
            new Thread(new FilterRunnable(mSearchKeywords)).start();
        }
    }

    /**
     * Performs filtering in separate thread.
     * @param filterText text to filter by
     */
    public void performFiltering(String filterText) {
        new Thread(new FilterRunnable(filterText)).start();
    }

    /**
     * Callbacks for stages in search 'lifecycle'
     */
    public interface SearchingCallbacks {
        public void onSearchStart();
        public void onSearchComplete();
        public void onSearchResultsSubmitted();
        public void onSearchingCancelled();
    }

    private SearchingCallbacks mSearchingCallbacks;

    /**
     * Set the searching callbacks for this adapter
     * @param searchingCallbacks callbacks for searching
     */
    public void registerSearchingCallbacks(SearchingCallbacks searchingCallbacks) {
        mSearchingCallbacks = searchingCallbacks;
    }

    /**
     * Set search callbacks to null
     */
    public void removeSearchCallbacks() {
        mSearchingCallbacks = null;
    }

    /**
     * Used to send a filter to the server as a search request.
     */
    private class FilterRunnable implements Runnable {

        private String mFilterText;

        /**
         * New instance
         * @param filterText text to filter by
         */
        public FilterRunnable(String filterText) {
            mFilterText = filterText;
        }

        /**
         * Format search string, removing all previous entries and improper punctuation.
         * If format for search is correct and this query is different from the last,
         * send info to server.  If query the same, show old results.  If incorrect search
         * format (must be longer than 2 characters, etc.) cancel all pending searches.
         */
        @Override
        public void run() {

            //set filter text as last search
            mSearchKeywords = mFilterText;

            if (mSearchKeywords.length() > 0) {
                //get rid of last comma or semicolon
                int lastPos = mSearchKeywords.length() - 1;
                char charAt = mSearchKeywords.charAt(lastPos);
                if (charAt == ',' || charAt == ';') {
                    mSearchKeywords = mSearchKeywords.substring(0, lastPos);
                }
            }

            //remove all previous entries in text view, then remove all commas and semicolons
            mSearchKeywords =
                    mSearchKeywords
                    .replaceAll("[^,;]*[,;]\\s?", "").replaceAll("[,;]", "").trim().toLowerCase();

            //remove @ sign if user is trying to find user by cn number
            if (mSearchKeywords.length() > 0) {
                char charAt = mSearchKeywords.charAt(0);
                if (charAt == '@') {
                    mSearchKeywords = mSearchKeywords.substring(1);
                }
            }

            //search keywords must be at least three characters
            if (mSearchKeywords.length() > 2 && !mSearchKeywords.contains("@")) {

                if (mEntriesSearchKeywords != null) {
                    //if the last search does not equal this search, then do a search
                    if (!mSearchKeywords.equals(mEntriesSearchKeywords)) {
                        search();
                    } else {
                        //if the last search does equal this search, show the last results
                        if (mSearchingCallbacks != null) mSearchingCallbacks.onSearchResultsSubmitted();
                    }
                } else {
                    //if no results have been collected, search for first time
                    search();
                }

            } else {
                if (mSearchingCallbacks != null) mSearchingCallbacks.onSearchingCancelled();

                //cancel request timer task
                synchronized (mRequestTaskLock) {
                    if (mRequestTask != null) {
                        mRequestTask.cancel();
                        mRequestTask = null;
                    }
                }

                //purge timer of timer tasks
                synchronized (mRequestTimerLock) {
                    mRequestTimer.purge();
                }
            }
        }

        /**
         * Makes a search request to server if no pending request exists.  If so,
         * check whether enough time passed to send another request.  If so, send it,
         * if not, schedule it.
         */
        private void search() {

            if (mSearchingCallbacks != null) mSearchingCallbacks.onSearchStart();

            synchronized (mRequestTaskLock) {
                //if there is no request scheduled, check when next request should be made
                if (mRequestTask == null) {
                    long timeTillNextRequest = getTimeTillNextRequest();

                    //If next request should be made now, do it now.
                    //Otherwise, schedule a task to perform the request once
                    //the time has passed
                    if (timeTillNextRequest <= 0) {
                        makeSearchRequest();
                    } else {
                        mRequestTask = new SearchRequestTask();

                        synchronized (mRequestTimerLock) {
                            mRequestTimer.schedule(mRequestTask, timeTillNextRequest);
                        }
                    }
                }
            }
        }
    }

    /**
     * Get time until next request.  They are never made less than one second apart.
     * @return time till next request.
     */
    private long getTimeTillNextRequest() {
        return 1000 - (SystemClock.elapsedRealtime() - lastRequestTime);
    }

    /**
     * Sets a timestamp and makes a request
     */
    private void makeSearchRequest() {
        lastRequestTime = SystemClock.elapsedRealtime();
        notifyWaitingForReply(true);

        final String textContent = mSearchKeywords;

        SearchStore.userSearchByKeyword(textContent, 10, new ResponseCallback() {

            @Override
            public void onResponse(JSONObject response) {
                if (StoreUtil.success(response)) {
                    if (mSearchingCallbacks != null) mSearchingCallbacks.onSearchComplete();

                    ArrayList<User> users = SearchStore.getUserListData(response);

                    if (users != null) {
                        //If the current search keyword does not equal what we searched for,
                        //do not update the entries
                        if (wasLastSearch(textContent)) {
                            updateEntries(CNRecipientEntry.constructEntriesFromUsers(users));
                            if (mSearchingCallbacks != null)
                                mSearchingCallbacks.onSearchResultsSubmitted();
                        }
                    }
                }

                notifyWaitingForReply(false);
            }

            @Override
            public void onError(VolleyError error) {
                notifyWaitingForReply(false);
            }
        });
    }

    /**
     * Set {@link #mWaitingForReply} to be true when waiting flag set
     * OR there is a pending request task
     * @param waiting whether search result has returned.
     */
    private void notifyWaitingForReply(boolean waiting) {
        synchronized (mRequestTaskLock) {
            mWaitingForReply = waiting || mRequestTask != null;
        }
    }

    /**
     * Tells whether keywords were used in the last search.
     * @param text text to test
     * @return true if keywords were used in last search, false otherwise
     */
    public boolean wasLastSearch(String text) {
        return mSearchKeywords.equals(text);
    }

    //Keeps track of the search keywords that were used to find the currently displayed entries
    //in the drop down list.
    private volatile String mEntriesSearchKeywords;

    /**
     * Additionally sets {@link #mEntriesSearchKeywords}
     */
    @Override
    public void updateEntries(List<RecipientEntry> entries) {
        super.updateEntries(entries);
        mEntriesSearchKeywords = mSearchKeywords;
    }

    /**
     * Sets entries used in adapter
     * @param entries entries
     */
    public void setEntries(List<RecipientEntry> entries) {
        mEntries = entries;
    }

    /**
     * Gets array list of entries.  Tests whether they are the proper data type
     * because of some stuff under the hood in the super class.  Returns null if not correct type.
     * @return list of entries or null.
     */
    public ArrayList<RecipientEntry> getEntries() {
        ArrayList<RecipientEntry> retVal = null;

        if (mEntries instanceof ArrayList) {
            try {
                retVal = (ArrayList<RecipientEntry>) mEntries;
            } catch(ClassCastException e) {
                retVal = null;
            }
        }

        return retVal;
    }

    public String getEntriesSearchKeywords() {
        return mEntriesSearchKeywords;
    }

    public void setEntriesSearchKeywords(String keywords) {
        mEntriesSearchKeywords = keywords;
    }

    public void setSearchKeywords(String keywords) {
        mSearchKeywords = keywords;
    }

    public String getSearchKeywords() {
        return mSearchKeywords;
    }

    /**
     * Test whether entries in adapter were found using parameter string
     * @param text string to test
     * @return true if entries found using parameter string
     */
    public boolean wereEntriesFoundBy(String text) {
        return mEntriesSearchKeywords != null && mEntriesSearchKeywords.equals(text);
    }

    /**
     * Return whether or not waiting for reply for search request.  This includes the case
     * where a search request is scheduled but hasn't been sent yet.
     * @return true if waiting for a reply from server
     */
    public boolean isWaitingForReply() {
        return mWaitingForReply;
    }

    /**
     * Extension of RecipientEntry class to allow for association of a User object
     */
    public static class CNRecipientEntry extends RecipientEntry implements Serializable {
        protected CNRecipientEntry(int entryType, String displayName, String destination,
                               int destinationType, String destinationLabel, long contactId, Long directoryId,
                               long dataId, Uri photoThumbnailUri, boolean isFirstLevel, boolean isValid,
                               String lookupKey, User user) {

            super(entryType, displayName, destination, destinationType, destinationLabel,
                  contactId, directoryId, dataId, photoThumbnailUri, isFirstLevel, isValid, lookupKey);
            mUser = user;
        }

        private User mUser;
        private transient Bitmap mIconBitmap;

        /**
         * Get an instance of this class using info in User object
         * @param user user to use to construct entry
         * @return entry that can be used for this adapter.
         */
        public static CNRecipientEntry constructEntryFromUser(User user) {
            if (user != null) {
                String displayName = user.getDisplayName();
                String cnNumber = user.getCNNumber();
                cnNumber = cnNumber != null ? cnNumber : "";

                if (displayName != null) {
                    return new CNRecipientEntry(ENTRY_TYPE_PERSON, displayName,
                            cnNumber, ContactsContract.CommonDataKinds.Email.TYPE_OTHER,
                            "Email", 0, null, 0, null, true, true, null, user);
                }
            }

            return null;
        }

        /**
         * Construct list of entries from list of users.  Uses {@link #constructEntryFromUser(com.thecn.app.models.user.User)}
         * @param users list of users
         * @return list of entries
         */
        public static List<RecipientEntry> constructEntriesFromUsers(List<User> users) {
            List<RecipientEntry> entries = new ArrayList<RecipientEntry>();

            for (User user : users) {
                RecipientEntry entry = constructEntryFromUser(user);
                if (entry != null) {
                    entries.add(entry);
                }
            }

            return entries;
        }

        /**
         * Get user associated with entry
         * @return user
         */
        public User getUser() {
            return mUser;
        }

        /**
         * Set bitmap to use in icon
         * @param bitmap bitmap
         */
        public void setIconBitmap(Bitmap bitmap) {
            mIconBitmap = bitmap;
        }

        /**
         * Get bitmap to use in icon
         * @return bitmap
         */
        public Bitmap getIconBitmap() {
            return mIconBitmap;
        }

    }

    /**
     * Set a new empty array list as data source
     */
    public void clear() {
        updateEntries(new ArrayList<RecipientEntry>());
    }

    /**
     * Used to create view for an item in the dropdown search results list.  This appears when
     * the user has searched for users using keywords in a {@link com.thecn.app.views.CNEmailRecipientEditTextView}
     */
    public static class CNDropdownChipLayouter extends DropdownChipLayouter {

        /**
         * New instance
         * @param inflater for inflating views
         * @param context for resources
         */
        public CNDropdownChipLayouter(LayoutInflater inflater, Context context) {
            super(inflater, context);
        }

        /**
         * Binds the avatar icon to the image view. If we don't want to show the image, hides the
         * image view.
         */
        @Override
        protected void bindIconToView(boolean showImage, RecipientEntry entry, ImageView view,
                                      AdapterType type, int position) {
            if (view == null) {
                return;
            }

            showImage = showImage && type == AdapterType.BASE_RECIPIENT && entry instanceof CNRecipientEntry;

            if (showImage) {

                final CNRecipientEntry cnEntry = (CNRecipientEntry) entry;

                int defaultPhotoID = getDefaultPhotoResId();
                view.setImageResource(defaultPhotoID);

                try {
                    String avatarUrl = cnEntry.getUser().getAvatar().getView_url() + ".w160.jpg";

                    MyVolley.ImageParams params = new MyVolley.ImageParams(avatarUrl, view);
                    params.placeHolderID = params.errorImageResourceID = defaultPhotoID;
                    params.maxWidth = view.getWidth();
                    params.maxHeight = view.getHeight();
                    params.index = position;
                    params.listener = new MyVolley.MyIndexedImageListener(params) {
                        @Override
                        public void myOnResponse(Bitmap bm, boolean isImmediate) {
                            super.myOnResponse(bm, isImmediate);

                            if (bm != null) {
                                cnEntry.setIconBitmap(bm);
                            }
                        }
                    };

                    MyVolley.loadImage(params);

                    view.setVisibility(View.VISIBLE);

                } catch (NullPointerException e) {
                    view.setVisibility(View.GONE);
                }

            } else {
                view.setVisibility(View.GONE);
            }
        }

        @Override
        protected void bindTextToView(CharSequence text, TextView view) {
            if (view == null) {
                return;
            }

            if (text != null && text.length() > 0) {
                view.setText(text);
                view.setVisibility(View.VISIBLE);
            } else {
                view.setVisibility(View.GONE);
            }
        }
    }
}
