package com.thecn.app.stores;

import com.android.volley.Request;
import com.google.gson.Gson;
import com.thecn.app.AppSession;
import com.thecn.app.models.content.Post;
import com.thecn.app.tools.network.GlobalGson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Used to make network calls for {@link com.thecn.app.models.content.Post}s to server.
 * Also has methods for converting json data from server to Java models.
 */
public class PostStore extends BaseStore {

    public static final String TAG = PostStore.class.getSimpleName();

    //json filter params
    public static final String FILTER_MOST_NEW = "most_new";
    public static final String FILTER_MOST_NEW_COMMENT = "most_new_comment";
    public static final String FILTER_MOST_LIKED = "most_good";
    public static final String FILTER_MOST_REFLECTED = "most_comment";
    public static final String FILTER_MOST_VISITED = "most_link_view";

    //for getting post of the (day, week, etc.)
    public static final String PERIOD_DAY = "day";
    public static final String PERIOD_WEEK = "week";
    public static final String PERIOD_MONTH = "month";
    public static final String PERIOD_YEAR = "year";

    //json strings
    public static final String CONTENT = "content";
    public static final String POST = "post";
    public static final String POLL = "survey";
    public static final String QUIZ = "quiz";
    public static final String EVENT = "event";
    public static final String CLASSCAST = "classcast";


    public static  String taskId = "";

    private static final String COMMON_POST_PARAM = "with_content_count=1&with_content_user=1&with_content_original_content=1&" +
            "with_content_attachments=1&with_content_pictures=1&with_content_links=1&with_content_videos=1&" +
            "with_user_country=1&with_content_courses=1&with_content_conexuses=1";

    private static final String COMMON_GLOBAL_POST_PARAM = "with_content_count=1&with_content_user=1&with_content_original_content=0&with_content_attachments=1&with_content_pictures=1&with_content_links=1&with_content_videos=1&with_content_comments=0&with_content_comment_user=0&with_content_courses=0&with_content_conexuses=0&with_user_country=1&with_content_courses=1&with_content_conexuses=1&with_user_profile=0&&course_content_list_order=most_new&with_content_comment_sub_comments=0";

    /**
     * Parameters used to construct a query for a list of posts.
     */
    public static class ListParams {
        public int limit;     //limit on size of list returned
        public int offset;    //offset in server's list of posts
        public String filter; //filter applied (most new, most liked, etc.)
        public String period; //period to use when getting post of the (specific period)
        public String id;     //id of content to get posts from (user, course, conexus)
        public String contentType; //post, poll, quiz, etc.
        public int includeSameCategoryContent = 0; //include posts from global classmates, etc.

        /**
         * New instance.  Limit and offset required
         * @param limit limit on size of list returned
         * @param offset offset in server's overall list of posts
         */
        public ListParams(int limit, int offset) {
            this.limit = limit;
            this.offset = offset;
        }
    }

    /**
     * Construct Post model from json
     * @param response data from server
     * @return new Post or null if error
     */
    public static Post getData(JSONObject response) {
        try {
            return fromJSON(response.getJSONObject("data"));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Construct list of posts from json
     * @param response data from server
     * @return new list of posts or null if error
     */
    public static ArrayList<Post> getListData(JSONObject response) {
        try {
            return fromJSON(response.getJSONArray("data"));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Construct Post model from json
     * @param json data from server
     * @return new Post or null on error
     */
    public static Post fromJSON(JSONObject json) {

        Gson gson = GlobalGson.getGson();
       // Gson gson = new Gson();
        try {
           // boolean test = json.isNull(json.toString());
            return gson.fromJson(json.toString(), Post.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Construct new list of Posts from json
     * @param jsonArray data from server
     * @return new list of Posts or null on error
     */
    public static ArrayList<Post> fromJSON(JSONArray jsonArray) {

        ArrayList<Post> posts = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                Post post = fromJSON(jsonArray.getJSONObject(i));
                if (post != null && post.getPostType() != null) {
                    String postType = post.getPostType();
                    //check to make sure type supported
                    boolean add = postType.equals("post")
                            || postType.equals("survey")
                            || postType.equals("event")
                            || postType.equals("quiz")
                            || postType.equals("classcast")
                            || postType.equals("sharelink");

                    if (add) posts.add(post);
                } else {
                    StoreUtil.logNullAtIndex(TAG, i);
                }
            } catch (Exception e) {
                //do nothing
            }
        }

        return posts.size() > 0 ? posts : null;
    }

    public static final Object SEND_POST_TAG = new Object();

    /**
     * Send data for a new post to the server.  Uses paramters to
     * construct json object payload to be sent to server.
     * @param text content of post
     * @param courseRelations courses to post to
     * @param conexusRelations conexuses to post to
     * @param groupIDs visibility groups to post to
     * @param imageIDs images to add to post
     * @param youtubeLinks youtube videos to add to post.
     * @param callback code to run on network response
     */
    public static void makePost(String text,
                                String[] courseRelations,
                                String[] conexusRelations,
                                String[] groupIDs,
                                String[] imageIDs,
                                String[] youtubeLinks,
                                final ResponseCallback callback) {

        String apiMethod = "post";

        String query = "/" + apiMethod + "/?return_detail=1&with_content_count=1&with_content_user=1"
                + "&with_content_original_content=1&with_content_attachments=1&with_content_pictures=1"
                + "&with_content_links=1&with_content_videos=1&with_content_courses=1"
                + "&with_content_conexuses=1&with_user_country=1&with_user_profile=1";

        HashMap<String, String[]> relations = new HashMap<>();
        relations.put("course_ids", courseRelations);
        relations.put("conexus_ids", conexusRelations);

        ArrayList<String> videoObject = new ArrayList<>(Arrays.asList(youtubeLinks));

        HashMap<String, Object> params = new HashMap<>();
        params.put("text", text);
        params.put("relations", relations);
        params.put("auth_assignment_group_ids", groupIDs);
        params.put("pictures", imageIDs);
        params.put("videos", videoObject);

        try {
            Gson gson = GlobalGson.getGson();
            JSONObject json = new JSONObject(gson.toJson(params));
            APIParams apiParams = new APIParams(query, Request.Method.POST, callback);
            apiParams.jsonObject = json;
            apiParams.tag = SEND_POST_TAG;

            api(apiParams);

        } catch (Exception e) {
            //do nothing
        }
    }

    /**
     * Tell the server that the user liked this post.
     * @param post the post being liked
     * @param callback code to run on network response
     */
    public static void likePost(final Post post, final ResponseCallback callback) {
        String apiMethod = "content_good";

        String query = "/" + apiMethod;
        HashMap<String, String> params = new HashMap<>();
        params.put("content_id", post.getId());

        api(query, Request.Method.POST, params, callback);
    }

    /**
     * Tell the server that the user no longer likes this post
     * @param post the post being unliked
     * @param callback code to run on network response
     */
    public static void unlikePost(final Post post, final ResponseCallback callback) {
        String apiMethod = "content_good";

        String query = "/" + apiMethod + "/" + post.getId();

        api(query, Request.Method.DELETE, callback);
    }

    /**
     * Get posts to display in {@link com.thecn.app.activities.homefeed.HomeFeedActivity}
     * List of posts from multiple sources
     * @param params used to construct query
     * @param callback code to run on network response
     */
    public static void getHomePosts(ListParams params, final ResponseCallback callback) {
        if (params.filter == null) params.filter = FILTER_MOST_NEW;

        String query = "/content/?" + COMMON_POST_PARAM +
                "&content_list_order=" + params.filter +
                "&limit=" + params.limit + "&offset=" + params.offset;

        api(query, Request.Method.GET, callback);
    }

    /**
     * Get "posts of the (period)" posts.  Returns most popular posts within a period of time.
     * @param params used to construct query
     * @param callback code to run on network response
     */
    public static void getTopContent(ListParams params, final ResponseCallback callback) {
        if (params.period == null) params.period = PERIOD_DAY;

        String query = "/public_content_top/?" + COMMON_POST_PARAM +
                "&period=" + params.period +
                "&limit=" + params.limit +
                "&offset=" + params.offset;

        api(query, Request.Method.GET, callback);
    }

    /**
     * Get a single post by id
     * @param postId id of post to get
     * @param callback code to run on network response
     */
    public static void getPostById(String postId, final ResponseCallback callback) {

        PostStore.taskId = "";
        String query = "/content/" + postId + "?" + COMMON_POST_PARAM;

        api(query, Request.Method.GET, callback);
    }

    public static void getPostById(String postId,String taskId, final ResponseCallback callback) {

        String query;
        if(taskId!=null && taskId.length()>0) {

            PostStore.taskId = taskId;
            query = "/content/" + postId + "?task_id=" + taskId + "&" + COMMON_POST_PARAM;
        }else {
            query = "/content/" + postId + "?" + COMMON_POST_PARAM;
            PostStore.taskId = "";
        }

        api(query, Request.Method.GET, callback);
    }

    /**
     * Get posts made by a specific user.
     * @param params used to construct query
     * @param callback code to run on network response
     */
    public static void getPostsFromUser(ListParams params, final ResponseCallback callback) {
        if (params.filter == null) params.filter = FILTER_MOST_NEW;

        String query = "/user_content/?" + COMMON_POST_PARAM +
                "&user_content_list_order=" + params.filter +
                "&user_id=" + params.id +
                "&limit=" + params.limit +
                "&offset=" + params.offset;

        if (params.contentType != null) query += "&content_type=" + params.contentType;

        api(query, Request.Method.GET, callback);
    }

    /**
     * Get posts from users that the logged in user is following
     * @param params used to construct query
     * @param callback code to run on network response
     */
    public static void getPostsFromFollowing(ListParams params, final ResponseCallback callback) {
        String query = "/content_from_following/?" + COMMON_POST_PARAM +
                "&limit=" + params.limit + "&offset=" + params.offset;
        api(query, Request.Method.GET, callback);
    }

    /**
     * Get posts from colleagues of this user.
     * @param params used to construct query
     * @param callback code to run on network response
     */
    public static void getPostsFromColleagues(ListParams params, final ResponseCallback callback) {
        String query = "/content_from_colleague/?" + COMMON_POST_PARAM +
                "&limit=" + params.limit + "&offset=" + params.offset;
        api(query, Request.Method.GET, callback);
    }

    /**
     * Get public CN posts
     * @param params used to construct query
     * @param callback code to run on network response
     */
    public static void getPublicPosts(ListParams params, final ResponseCallback callback) {
        String query = "/content_from_public/?" + COMMON_POST_PARAM +
                "&limit=" + params.limit + "&offset=" + params.offset;
        api(query, Request.Method.GET, callback);
    }

    /**
     * Get posts that have been reposted by a different user
     * @param params used to construct query
     * @param callback code to run on network response
     */
    public static void getReposts(ListParams params, final ResponseCallback callback) {
        String query = "/content_from_repost/?" + COMMON_POST_PARAM +
                "&limit=" + params.limit + "&offset=" + params.offset;
        api(query, Request.Method.GET, callback);
    }

    /**
     * Get posts that were made by CN admin
     * @param params used to construct query
     * @param callback code to run on network response
     */
    public static void getAdminPosts(ListParams params, final ResponseCallback callback) {
        String query = "/content_from_admin/?" + COMMON_POST_PARAM +
                "&limit=" + params.limit + "&offset=" + params.offset;
        api(query, Request.Method.GET, callback);
    }

    /**
     * Tell the server whether public posts should be hidden from the user
     * when posts are retrieved using {@link #getHomePosts(com.thecn.app.stores.PostStore.ListParams, ResponseCallback)}
     * @param hide whether to hide public posts
     * @param callback code to run on network response
     */
    public static void setHidePublicPosts(boolean hide, final ResponseCallback callback) {
        String userID = AppSession.getInstance().getUser().getId();
        String param = hide ? "1" : "0";
        String query = "/user_hide_public_contents/" + userID + "?hide_public_contents=" + param;
        api(query, Request.Method.PUT, callback);
    }

    /**
     * Get posts made by this user's instructors
     * @param params used to construct query
     * @param callback code to run on network response
     */
    public static void getContentFromInstructors(ListParams params, final ResponseCallback callback) {
        String query = "/content_from_instructor/?" + COMMON_POST_PARAM +
                "&limit=" + params.limit + "&offset=" + params.offset;

        if (params.contentType != null) query += "&content_type=" + params.contentType;
        if (params.id != null) query += "&course_id=" + params.id;

        api(query, Request.Method.GET, callback);
    }

    /**
     * Get posts from a course
     * @param params used to construct query
     * @param callback code to run on network response
     */
    public static void getPostsFromCourse(ListParams params, final ResponseCallback callback) {

        String query = "/course_content/?" + COMMON_POST_PARAM +
                "&course_id=" + params.id +
                "&include_the_same_category_contents=" + params.includeSameCategoryContent +
                "&limit=" + params.limit + "&offset=" + params.offset;

        if (params.contentType != null) query += "&content_type=" + params.contentType;
        if (params.filter != null) query += "&course_content_list_order=" + params.filter;

        api(query, Request.Method.GET, callback);
    }

    /**
     * Get posts from a course
     * @param params used to construct query
     * @param callback code to run on network response
     */
    public static void getGlobalPostsFromCourse(ListParams params, final ResponseCallback callback) {

        String query = "/course_category_content/?" + COMMON_POST_PARAM +
                "&course_id=" + params.id +
                "&limit=" + params.limit + "&offset=" + params.offset;

        if (params.contentType != null) query += "&content_type=" + params.contentType;
        if (params.filter != null) query += "&course_content_list_order=" + params.filter;

        api(query, Request.Method.GET, callback);
    }

    /**
     * Get course posts that have been highlighted as notable content.
     * @param params used to construct query
     * @param callback code to run on network response
     */
    public static void getHighlightPostsFromCourse(ListParams params, final ResponseCallback callback) {
        String query = "/course_highlight_content/?" + COMMON_POST_PARAM +
                "&course_id=" + params.id +
                "&include_the_same_category_contents=" + params.includeSameCategoryContent +
                "&limit=" + params.limit + "&offset=" + params.offset;

        if (params.contentType != null) query += "&content_type=" + params.contentType;
        if (params.filter != null) query += "&course_content_list_order=" + params.filter;

        api(query, Request.Method.GET, callback);
    }

    /**
     * Get posts made in a conexus
     * @param params used to construct query
     * @param callback code to run on network response
     */
    public static void getPostsFromConexus(ListParams params, final ResponseCallback callback) {

        String query = "/conexus_content/?" + COMMON_POST_PARAM +
                "&conexus_id=" + params.id +
                "&limit=" + params.limit + "&offset=" + params.offset;

        if (params.contentType != null) query += "&content_type=" + params.contentType;
        if (params.filter != null) query += "&conexus_content_list_order=" + params.filter;

        api(query, Request.Method.GET, callback);
    }

    /**
     * Get posts made in a conexus that have been highlighted as notable
     * @param params used to construct query
     * @param callback code to run on network response
     */
    public static void getHighlightPostsFromConexus(ListParams params, final ResponseCallback callback) {
        String query = "/conexus_highlight_content/?" + COMMON_POST_PARAM +
                "&conexus_id=" + params.id +
                "&include_the_same_category_contents=" + params.includeSameCategoryContent +
                "&limit=" + params.limit + "&offset=" + params.offset;

        if (params.contentType != null) query += "&content_type=" + params.contentType;
        if (params.filter != null) query += "&conexus_content_list_order=" + params.filter;

        api(query, Request.Method.GET, callback);
    }

    /**
     * Get posts that have been highlighted as notable.  These posts are general posts
     * that are filtered out of the same api method that {@link #getHomePosts(com.thecn.app.stores.PostStore.ListParams, ResponseCallback)}
     * uses.
     * @param params used to construct query
     * @param callback code to run on network response
     */
    public static void getHighlightedPosts(ListParams params, final ResponseCallback callback) {
        String query = "/highlight_content/?" + COMMON_POST_PARAM +
                "&limit=" + params.limit + "&offset=" + params.offset;

        api(query, Request.Method.GET, callback);
    }

    /**
     * Get a list of likes for a specified post.
     * @param params used to construct query
     * @param callback code to run on network response
     */
    public static void getPostLikes(ListParams params, ResponseCallback callback) {

        String query = "/content_good/?with_user_profile=0&with_user_relations=0&with_user_country=1&with_user_count=0&with_user_score=1&" +
                "content_id=" + params.id +
                "&limit=" + params.limit + "&offset=" + params.offset;

        api(query, Request.Method.GET, callback);
    }

    /**
     * Tell the server to delete a post.
     * @param postId id of post to delete
     * @param callback code to run on network response
     */
    public static void deletePost(String postId, final ResponseCallback callback) {

        String query = "/post/" + postId;

        api(query, Request.Method.DELETE, callback);
    }

    //returns false on failure to make request

    /**
     * Send a response to a poll item to the server.
     * @param answers list of answers (either one choice, many choices, or one short answer)
     * @param contentId poll id
     * @param itemId poll item id
     * @param callback code to run on network response
     * @return true if submission was sent successfully
     */
    public static boolean sendPollSubmission(ArrayList<String> answers, String contentId, String itemId, ResponseCallback callback) {
        String query = "/survey_submission/";

        HashMap<String, Object> payload = new HashMap<>();
        payload.put("answers", answers);
        payload.put("content_id", contentId);
        payload.put("item_id", itemId);

        try {
            String payloadString = GlobalGson.getGson().toJson(payload);
            JSONObject jsonPayload = new JSONObject(payloadString);

            api(query, Request.Method.POST, jsonPayload, callback);
        } catch(Exception e) {
            return false;
        }

        return true;
    }
}