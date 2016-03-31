package com.thecn.app.tools.network;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.thecn.app.R;
import com.thecn.app.fragments.BasePostListFragment;
import com.thecn.app.models.util.TextIconEntry;
import com.thecn.app.stores.PostStore;

import java.util.ArrayList;

/**
 * Used to specify a methods for loading posts.  Used in {@link com.thecn.app.fragments.BasePostListFragment}.
 */
public class PostLoader {

    public static final int CONTENT_ALL =       0;
    public static final int CONTENT_POST =      1;
    public static final int CONTENT_POLL =      2;
    public static final int CONTENT_QUIZ =      3;
    public static final int CONTENT_EVENT =     4;
    public static final int CONTENT_CLASSCAST = 5;

    public static final int ALL_CHOICES =        0x3f;
    public static final int CHOICE_ALL_CONTENT = 0x20;
    public static final int CHOICE_POST =        0x10;
    public static final int CHOICE_POLL =        0x08;
    public static final int CHOICE_QUIZ =        0x04;
    public static final int CHOICE_EVENT =       0x02;
    public static final int CHOICE_CLASSCAST =   0x01;

    public static final int SOURCE_HOME =        0;
    public static final int SOURCE_COURSE =      1;
    public static final int SOURCE_CONEXUS =     2;
    public static final int SOURCE_TOP =         3;
    public static final int SOURCE_USER =        4;
    public static final int SOURCE_FOLLOWING =   5;
    public static final int SOURCE_COLLEAGUES =  6;
    public static final int SOURCE_PUBLIC =      7;
    public static final int SOURCE_REPOSTS =     8;
    public static final int SOURCE_ADMIN =       9;
    public static final int SOURCE_INSTRUCTOR = 10;
    public static final int SOURCE_HIGHLIGHT =  11;
    public static final int SOURCE_HIGHLIGHT_COURSE = 12;
    public static final int SOURCE_HIGHLIGHT_CONEXUS = 13;

    public static final int FILTER_NEW_POSTS =       0;
    public static final int FILTER_NEW_REFLECTIONS = 1;
    public static final int FILTER_MOST_LIKED =      2;
    public static final int FILTER_MOST_REFLECTED =  3;
    public static final int FILTER_MOST_VISITED =    4;

    public static final int PERIOD_DAY =   0;
    public static final int PERIOD_WEEK =  1;
    public static final int PERIOD_MONTH = 2;
    public static final int PERIOD_YEAR =  3;

    /**
     * Params for post loader.  Contains {@link com.thecn.app.tools.network.PostLoader.Method} as
     * well as other values.  This class is used to specify additional flags that are set apart
     * from the loading method itself, which is specified by {@link com.thecn.app.tools.network.PostLoader.Method}
     */
    public static class Params {
        public PostLoader.Method method;
        public int contentType;
        public boolean interpretAsGlobal;
        public boolean showGlobalPosts;

        public Params(PostLoader.Method method) {
            this(method, PostLoader.CONTENT_ALL);
        }

        public Params(PostLoader.Method method, int contentType) {
            this.method = method;
            this.contentType = contentType;
        }

        public boolean equals(Params other) {
            return method.equals(other.method) &&
                    contentType == other.contentType &&
                    interpretAsGlobal == other.interpretAsGlobal &&
                    showGlobalPosts == other.showGlobalPosts;
        }
    }

    /**
     * Used to specify a network call that will return posts.
     */
    public static class Method implements Parcelable {

        public int sourceType;

        public String name; //display name to show user
        public String id;

        public int filterType;
        public int period;

        /**
         * New instance with source type
         * @param sourceType type of api method to use
         */
        public Method(int sourceType) {
            this.sourceType = sourceType;
        }

        /**
         * New instance from existing method
         * @param method existing method
         */
        public Method(Method method) {
            sourceType = method.sourceType;
            name = method.name;
            id = method.id;

            filterType = method.filterType;
            period = method.period;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Method)) return false;

            Method other = (Method) o;

            return other.sourceType == sourceType &&
                   other.filterType == filterType &&
                   other.period == period         &&
                   TextUtils.equals(other.id, id);
        }

        /**
         * Create method object from parcel
         * @param in parcel
         */
        public Method(Parcel in) {
            int[] integers = new int[3];
            in.readIntArray(integers);
            String[] strings = new String[2];
            in.readStringArray(strings);

            sourceType = integers[0];
            filterType = integers[1];
            period = integers[2];

            name = strings[0];
            id = strings[1];
        }

        @Override
        public int describeContents() {
            return 0;
        }

        /**
         * Write this object into a parcel.
         * @param parcel parcel to write to
         */
        @Override
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeIntArray(new int[] {
                    sourceType,
                    filterType,
                    period,
            });

            parcel.writeStringArray(new String[] {
                    name,
                    id
            });
        }

        /**
         * Used to create Method objects from parcels.
         */
        public static final Creator<Method> CREATOR = new Creator<Method>() {
            @Override
            public Method createFromParcel(Parcel parcel) {
                return new Method(parcel);
            }

            @Override
            public Method[] newArray(int size) {
                return new Method[size];
            }
        };
    }

    /**
     * Specifies a list of methods for loading posts.  The user can choose
     * from these methods and certain flags in this list apply to all methods it contains.
     */
    public static class MethodList implements Parcelable {

        private static final String ALL = "All Content";
        private static final String POSTS = "Posts";
        private static final String POLLS = "Polls";
        private static final String QUIZZES = "Quizzes";
        private static final String EVENTS = "Events";
        private static final String CLASSCASTS = "ClassCasts";

        private int contentType; //posts, polls, etc.
        private boolean allowContentTypeChange; //allows user to set content type
        private boolean showContentToggle; //show toggle for global/public posts
        private boolean interpretAsGlobalPosts; //whether to interpret as global posts or public posts toggle
        private boolean showGlobalPosts; //whether to show global/public posts

        private int selectedIndex; //selected method
        private int contentChoice = ALL_CHOICES;

        private ArrayList<Method> list;
        private int secondListStartIndex = -1;
        //if their are public choice methods, they must be first in list

        /**
         * By default, include all content
         */
        public MethodList() {
            this(CONTENT_ALL);
        }

        /**
         * New instance with content type
         * @param contentType specifies content type
         */
        public MethodList(int contentType) {
            list = new ArrayList<>();
            this.contentType = contentType;
        }

        /**
         * Set content choice
         * @param contentChoice the content choice
         */
        public void setContentChoice(int contentChoice) {
            this.contentChoice = contentChoice;
        }

        /**
         * Get content choice
         * @return the content choice
         */
        public int getContentChoice() {
            return contentChoice;
        }

        /**
         * Get array of choices user can choose from when choosing content
         * @param contentChoice specific content choice setting
         * @return list of text icon entries, can be displayed in dialog
         */
        public static TextIconEntry[] getContentChoiceArray(int contentChoice) {
            TextIconEntry[] array = new TextIconEntry[6];

            if ((contentChoice & CHOICE_ALL_CONTENT) == CHOICE_ALL_CONTENT) {
                array[0] = new TextIconEntry(CONTENT_ALL, ALL, R.drawable.ic_launcher);
            }
            if ((contentChoice & CHOICE_POST) == CHOICE_POST) {
                array[1] = new TextIconEntry(CONTENT_POST, POSTS, R.drawable.ic_post_no_bg);
            }
            if ((contentChoice & CHOICE_POLL) == CHOICE_POLL) {
                array[2] = new TextIconEntry(CONTENT_POLL, POLLS, R.drawable.ic_poll_colored);
            }
            if ((contentChoice & CHOICE_QUIZ) == CHOICE_QUIZ) {
                array[3] = new TextIconEntry(CONTENT_QUIZ, QUIZZES, R.drawable.ic_quiz);
            }
            if ((contentChoice & CHOICE_EVENT) == CHOICE_EVENT) {
                array[4] = new TextIconEntry(CONTENT_EVENT, EVENTS, R.drawable.ic_event);
            }
            if ((contentChoice & CHOICE_CLASSCAST) == CHOICE_CLASSCAST) {
                array[5] = new TextIconEntry(CONTENT_CLASSCAST, CLASSCASTS, R.drawable.ic_classcast);
            }

            return array;
        }

        /**
         * Get list of content choices made from {@link #getContentChoiceArray(int)}
         * @param contentChoice content choice setting
         * @return list of text icon entries
         */
        public static ArrayList<TextIconEntry> getContentChoiceList(int contentChoice) {
            ArrayList<TextIconEntry> list = new ArrayList<>();
            TextIconEntry[] array = getContentChoiceArray(contentChoice);
            for (TextIconEntry entry : array) {
                if (entry != null) list.add(entry);
            }

            return list;
        }

        public boolean showContentToggle() {
            return showContentToggle;
        }

        public void setShowContentToggle(boolean showContentToggle) {
            this.showContentToggle = showContentToggle;
        }

        public void setInterpretAsGlobalPosts(boolean interpret) {
            interpretAsGlobalPosts = interpret;
        }

        public boolean interpretAsGlobalPosts() {
            return interpretAsGlobalPosts;
        }

        public void setShowGlobalPosts(boolean show) {
            showGlobalPosts = show;
        }

        public boolean showGlobalPosts() {
            return showGlobalPosts;
        }

        public boolean add(Method method) {
            return list.add(method);
        }

        public void startSecondList() {
            secondListStartIndex = list.size();
        }

        public int getSecondListStartIndex() {
            return secondListStartIndex;
        }

        public Method get(int index) {
            return list.get(index);
        }

        public Method getSelectedMethod() {
            return get(selectedIndex);
        }

        public int size() {
            return list.size();
        }

        public int getContentType() {
            return contentType;
        }

        public void setContentType(int contentType) {
            if (contentType < CONTENT_ALL || contentType > CONTENT_CLASSCAST) return;

            this.contentType = contentType;
        }

        public int getSelectedIndex() {
            return selectedIndex;
        }

        public void setSelectedIndex(int selectedIndex) {
            if (selectedIndex >= list.size() || selectedIndex < 0) return;

            this.selectedIndex = selectedIndex;
        }

        public boolean contentTypeChangeAllowed() {
            return allowContentTypeChange;
        }

        public void setAllowContentTypeChange(boolean allow) {
            allowContentTypeChange = allow;
        }

        public void ensureCapacity(int minimumCapacity) {
            list.ensureCapacity(minimumCapacity);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        /**
         * Create method list from parcel
         * @param in parcel to create new instance from
         */
        public MethodList(Parcel in) {
            int[] integers = new int[8];
            in.readIntArray(integers);
            contentType = integers[0];
            selectedIndex = integers[1];
            contentChoice = integers[2];
            secondListStartIndex = integers[3];
            showGlobalPosts = integers[4] != 0;
            interpretAsGlobalPosts = integers[5] != 0;
            allowContentTypeChange = integers[6] != 0;
            showContentToggle = integers[7] != 0;

            list = new ArrayList<>();
            in.readTypedList(list, Method.CREATOR);
        }

        /**
         * Write this instance to a parcel
         */
        @Override
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeIntArray(new int[]{
                    contentType,
                    selectedIndex,
                    contentChoice,
                    secondListStartIndex,
                    showGlobalPosts ? 1 : 0,
                    interpretAsGlobalPosts ? 1 : 0,
                    allowContentTypeChange ? 1 : 0,
                    showContentToggle ? 1 : 0
            });
            parcel.writeTypedList(list);
        }

        /**
         * Used to create a new instance from a parcel.
         */
        public static final Creator<MethodList> CREATOR = new Creator<MethodList>() {
            @Override
            public MethodList createFromParcel(Parcel parcel) {
                return new MethodList(parcel);
            }

            @Override
            public MethodList[] newArray(int i) {
                return new MethodList[0];
            }
        };
    }

    /**
     * Make a network call to get posts using params and underlying post loading method
     * @param f fragment loading posts
     * @param params params specifying post loading method
     * @return callback to be called when network call returns.
     */
    public static BasePostListFragment.PostCallback loadPosts(BasePostListFragment f, Params params) {

        PostStore.ListParams storeParams = new PostStore.ListParams(f.getLimit(), f.getOffset());
        BasePostListFragment.PostCallback callback = new BasePostListFragment.PostCallback(f.getCallbackManager());

        //default to home posts
        if (params.method == null) {
            PostStore.getHomePosts(storeParams, callback);
            return callback;
        }

        //pass post loader params into post store params
        storeParams.filter = getFilterString(params.method.filterType);
        storeParams.contentType = getContentTypeString(params.contentType);

        if (params.method.id != null) {
            storeParams.id = params.method.id;
        }

        if (params.interpretAsGlobal && params.showGlobalPosts) {
            storeParams.includeSameCategoryContent = 1;
        }

        //call certain methods depending on source type
        switch (params.method.sourceType) {
            case SOURCE_HOME:
                PostStore.getHomePosts(storeParams, callback);
                break;
            case SOURCE_COURSE:
                PostStore.getPostsFromCourse(storeParams, callback);
                break;
            case SOURCE_CONEXUS:
                PostStore.getPostsFromConexus(storeParams, callback);
                break;
            case SOURCE_TOP:
                storeParams.period = getPeriodString(params.method.period);
                PostStore.getTopContent(storeParams, callback);
                break;
            case SOURCE_USER:
                PostStore.getPostsFromUser(storeParams, callback);
                break;
            case SOURCE_FOLLOWING:
                PostStore.getPostsFromFollowing(storeParams, callback);
                break;
            case SOURCE_COLLEAGUES:
                PostStore.getPostsFromColleagues(storeParams, callback);
                break;
            case SOURCE_PUBLIC:
                PostStore.getPublicPosts(storeParams, callback);
                break;
            case SOURCE_REPOSTS:
                PostStore.getReposts(storeParams, callback);
                break;
            case SOURCE_ADMIN:
                PostStore.getAdminPosts(storeParams, callback);
                break;
            case SOURCE_INSTRUCTOR:
                PostStore.getContentFromInstructors(storeParams, callback);
                break;
            case SOURCE_HIGHLIGHT:
                PostStore.getHighlightedPosts(storeParams, callback);
                break;
            case SOURCE_HIGHLIGHT_COURSE:
                PostStore.getHighlightPostsFromCourse(storeParams, callback);
                break;
            case SOURCE_HIGHLIGHT_CONEXUS:
                PostStore.getHighlightPostsFromConexus(storeParams, callback);
                break;
        }

        return callback;
    }

    /**
     * Get content type json representation
     * @param contentType content type specification
     * @return json representation of content type
     */
    private static String getContentTypeString(int contentType) {
        switch (contentType) {
            case CONTENT_POST:
                return PostStore.POST;
            case CONTENT_POLL:
                return PostStore.POLL;
            case CONTENT_QUIZ:
                return PostStore.QUIZ;
            case CONTENT_EVENT:
                return PostStore.EVENT;
            case CONTENT_CLASSCAST:
                return PostStore.CLASSCAST;
            default:
                return null;
        }
    }

    /**
     * Get json representation of filter type
     * @param filterType filter type specification
     * @return json representation of filter type
     */
    private static String getFilterString(int filterType) {
        switch (filterType) {
            case FILTER_NEW_POSTS:
                return PostStore.FILTER_MOST_NEW;
            case FILTER_NEW_REFLECTIONS:
                return PostStore.FILTER_MOST_NEW_COMMENT;
            case FILTER_MOST_LIKED:
                return PostStore.FILTER_MOST_LIKED;
            case FILTER_MOST_REFLECTED:
                return PostStore.FILTER_MOST_REFLECTED;
            case FILTER_MOST_VISITED:
                return PostStore.FILTER_MOST_VISITED;
            default:
                return null;
        }
    }

    /**
     * Get json representation of period type
     * @param period period type specification
     * @return json representation of period
     */
    private static String getPeriodString(int period) {
        switch (period) {
            case PERIOD_DAY:
                return PostStore.PERIOD_DAY;
            case PERIOD_WEEK:
                return PostStore.PERIOD_WEEK;
            case PERIOD_MONTH:
                return PostStore.PERIOD_MONTH;
            case PERIOD_YEAR:
                return PostStore.PERIOD_YEAR;
            default:
                return null;
        }
    }

}
