package com.thecn.app.models.content;

import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.annotations.SerializedName;
import com.thecn.app.models.conexus.Conexus;
import com.thecn.app.models.course.Course;
import com.thecn.app.models.user.User;
import com.thecn.app.tools.text.CNNumberLinker;
import com.thecn.app.tools.CallbackManager;

import java.io.Serializable;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Model for post construct in server json.
 * This model encompasses content of types:
 * Post (name refers to general content and subtype)
 * Poll (survey)
 * Quiz
 * Event
 * Classcast
 * Sharelink
 */
public class Post implements Serializable {

    @SerializedName("id")
    private String id;

    @SerializedName("display_time")
    private String displayTime;

    @SerializedName("ctime")
    private double cTime;

    @SerializedName("title")
    private String title;

    @SerializedName("text")
    private String text;

    @SerializedName("user")
    private User user;

    @SerializedName("type")
    private String postType;

    @SerializedName("user_position")
    private String userPosition;

    @SerializedName("courses")
    private ArrayList<Course> courses;

    @SerializedName("conexuses")
    private ArrayList<Conexus> conexuses;

    @SerializedName("pictures")
    private ArrayList<Picture> pictures;

    @SerializedName("has_set_good")
    private boolean isLiked;

    @SerializedName("is_deletable")
    private boolean isDeletable;

    @SerializedName("is_editable")
    private boolean isEditable;

    @SerializedName("is_from_admin")
    private boolean isFromAdmin;

    @SerializedName("is_owner")
    private boolean isOwner;

    @SerializedName("is_repostable")
    private boolean isRepostable;

    @SerializedName("is_end")
    private boolean ended;

    @SerializedName("is_user_submit")
    private boolean userSubmitted;

    @SerializedName("count")
    private ContentCount count;

    //FOR EVENTS
    @SerializedName("where")
    private String eventLocation;

    @SerializedName("display_start_time")
    private String eventStartTime;

    @SerializedName("display_end_time")
    private String eventEndTime;

    //FOR POLLS
    @SerializedName("items")
    private ArrayList<PollItem> items;

    //FOR QUIZES
    @SerializedName("grade_type")
    private String gradeType;

    @SerializedName("view_submissions")
    private String viewSubmissions;

    @SerializedName("total_score")
    private String totalPointValue;

    @SerializedName("start_time")
    private String startTime;

    @SerializedName("end_time")
    private String endTime;

    //FOR CLASS CAST
    @SerializedName("length")
    private int length;

    public static String getYoutubeLinkRegex() {
        return youtubeLinkRegex;
    }

    //FOR SHARELINK
    @SerializedName("description")
    private String description;

    @SerializedName("videos")
    private ArrayList<Video> videos;

    @SerializedName("original_share_link")
    private String originalLink;

    @SerializedName("result_display_type")
    private String resultDisplayType;

    @SerializedName("display_result_date")
    private String resultDate;

    @SerializedName("display_result_time")
    private String resultTime;

    @SerializedName("attachments")
    private ArrayList<Attachment> attachments;

    @SerializedName("links")
    private ArrayList<Link> links;

    private BigInteger integerID;

    /**
     * Converts string id into big integer.
     * @return id in integer form
     */
    public BigInteger getIntegerID() {
        if (integerID == null) {
            if (id != null) {
                integerID = new BigInteger(id, 16);
            }
        }

        return integerID;
    }

    public ArrayList<Attachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(ArrayList<Attachment> attachments) {
        this.attachments = attachments;
    }

    public ArrayList<Link> getLinks() {
        return links;
    }

    public void setLinks(ArrayList<Link> links) {
        this.links = links;
    }

    public String getResultDisplayType() {
        return resultDisplayType;
    }

    public void setResultDisplayType(String resultDisplayType) {
        this.resultDisplayType = resultDisplayType;
    }

    public String getResultDate() {
        return resultDate;
    }

    public void setResultDate(String resultDate) {
        this.resultDate = resultDate;
    }

    public String getResultTime() {
        return resultTime;
    }

    public void setResultTime(String resultTime) {
        this.resultTime = resultTime;
    }

    //for testing youtube links
    private static final String youtubeLinkRegex = "(https?://)?(www\\.)?" +
            "(youtube(-nocookie)?\\.com/(((e(mbed)?|v|user)/.*?)|((watch)?(\\?feature=player_embedded)?[\\?&]v=))" +
            "|(youtu\\.be/))" +
            "[A-Za-z0-9-_]{11}.*";

    public boolean isUserSubmitted() {
        return userSubmitted;
    }

    public void setUserSubmitted(boolean userSubmitted) {
        this.userSubmitted = userSubmitted;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDisplayTime() {
        return displayTime;
    }

    public void setDisplayTime(String displayTime) {
        this.displayTime = displayTime;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getPostType() {
        return postType;
    }

    public void setPostType(String postType) {
        this.postType = postType;
    }

    public String getUserPosition() {
        return userPosition;
    }

    public void setUserPosition(String userPosition) {
        this.userPosition = userPosition;
    }

    /**
     * Gets list of pictures.  If post is sharelink video post,
     * remove the first one (which is a video and not a picture).
     * @return list of pictures in this post.
     */
    public ArrayList<Picture> getPictures() {
        if(isShareLinkVideoPost()){
            ArrayList<Picture> picturesWithoutVideoThumb = new ArrayList<>(pictures);
            if (picturesWithoutVideoThumb.size() > 0)
                picturesWithoutVideoThumb.remove(0);

            return picturesWithoutVideoThumb;
        } else return pictures;
    }

    public boolean isEnded() {
        return ended;
    }

    public void setEnded(boolean ended) {
        this.ended = ended;
    }

    public void setPictures(ArrayList<Picture> pictures) {
        this.pictures = pictures;
    }

    public boolean isLiked() {
        return isLiked;
    }

    public void setLiked(boolean isLiked) {
        this.isLiked = isLiked;
    }

    public boolean isDeletable() {
        return isDeletable;
    }

    public void setDeletable(boolean isDeletable) {
        this.isDeletable = isDeletable;
    }

    public boolean isEditable() {
        return isEditable;
    }

    public void setEditable(boolean isEditable) {
        this.isEditable = isEditable;
    }

    public boolean isFromAdmin() {
        return isFromAdmin;
    }

    public void setFromAdmin(boolean isFromAdmin) {
        this.isFromAdmin = isFromAdmin;
    }

    public boolean isOwner() {
        return isOwner;
    }

    public void setOwner(boolean isOwner) {
        this.isOwner = isOwner;
    }

    public boolean isRepostable() {
        return isRepostable;
    }

    public void setRepostable(boolean isRepostable) {
        this.isRepostable = isRepostable;
    }

    public ContentCount getCount() {
        return count;
    }

    public void setCount(ContentCount count) {
        this.count = count;
    }

    public String getEventLocation() {
        return eventLocation;
    }

    public void setEventLocation(String eventLocation) {
        this.eventLocation = eventLocation;
    }

    public String getEventStartTime() {
        return eventStartTime;
    }

    public void setEventStartTime(String eventStartTime) {
        this.eventStartTime = eventStartTime;
    }

    public String getEventEndTime() {
        return eventEndTime;
    }

    public void setEventEndTime(String eventEndTime) {
        this.eventEndTime = eventEndTime;
    }

    public ArrayList<PollItem> getItems() {
        return items;
    }

    public void setItems(ArrayList<PollItem> items) {
        this.items = items;
    }

    public String getGradeType() {
        return gradeType;
    }

    public void setGradeType(String gradeType) {
        this.gradeType = gradeType;
    }

    public String getViewSubmissions() {
        return viewSubmissions;
    }

    public void setViewSubmissions(String viewSubmissions) {
        this.viewSubmissions = viewSubmissions;
    }

    public String getTotalPointValue() {
        return totalPointValue;
    }

    public void setTotalPointValue(String totalPointValue) {
        this.totalPointValue = totalPointValue;
    }

    public double getcTime() {
        return cTime;
    }

    public void setcTime(double cTime) {
        this.cTime = cTime;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ArrayList<Course> getCourses() {
        return courses;
    }

    public void setCourses(ArrayList<Course> courses) {
        this.courses = courses;
    }

    public ArrayList<Conexus> getConexuses() {
        return conexuses;
    }

    public void setConexuses(ArrayList<Conexus> conexuses) {
        this.conexuses = conexuses;
    }

    /**
     * Returns list of videos in this post.  If this is a sharelink video post,
     * add the link as a video object so that it can be displayed as such.
     * @return list of videos in post.
     */
    public ArrayList<Video> getVideos() {
        if(isShareLinkVideoPost()){
            ArrayList<Video> videosWithLink = new ArrayList<>(videos);
            videosWithLink.add(0, new Video(originalLink));
            return videosWithLink;
        } else return videos;
    }

    public void setVideos(ArrayList<Video> videos) {
        this.videos = videos;
    }

    public String getOriginalLink() {
        return originalLink;
    }

    public void setOriginalLink(String originalLink) {
        this.originalLink = originalLink;
    }

    /**
     * Tells if this post is sharelink and the link is a youtube link
     * @return true if share link video post
     */
    private boolean isShareLinkVideoPost() {
        return getPostType().equals("sharelink")
                && originalLink != null
                && originalLink.matches(youtubeLinkRegex);
    }

    /**
     * code below is for processing post information in preparation for display
     * intended to be done in a background thread BEFORE displaying
     */

    private transient boolean isFullView;

    /**
     * Set the post to display content for full view (not truncated).
     * @param isFullView true if display whole post content.
     */
    public void setFullView(boolean isFullView) {
        this.isFullView = isFullView;
    }

    /**
     * Tells whether post is displaying entire content (not truncated).
     * @return true or false
     */
    public boolean isFullView() {
        return isFullView;
    }

    private transient CNNumberLinker cnNumberLinker;

    /**
     * Sets reference to callback manager, to be used for opening profile activities when
     * a cn number is clicked.
     * @param callbackManager manager to use when opening profile
     * @return true if it was set.
     */
    public boolean setCallbackManager(CallbackManager<? extends Fragment> callbackManager) {
        if (cnNumberLinker == null) return false;
        cnNumberLinker.setCallbackManager(callbackManager);
        return true;
    }

    /**
     * Prepare data for display on Android devices.
     */
    public void processData() {
        getIntegerID();
        initEnumPostType();
        initTimeText();
        initProcessedTitle();
        initContentText();
        initPostFromText();
    }

    private transient CharSequence contentText;

    /**
     * Format content depending on its type.
     * Sets {@link #contentText}
     */
    private void initContentText() {
        switch (enumType) {
            case POST:
                contentText = formatContent(text);
                break;
            case SHARELINK:
                contentText = getShareLinkContent();
                break;
            case POLL:
                contentText = getPollContent();
                break;
            case EVENT:
                contentText = getEventContent();
                break;
            case QUIZ:
                contentText = getQuizContent();
                break;
            case CLASSCAST:
                contentText = getClasscastContent();
                break;
        }
    }

    public CharSequence getContentText() {
        return contentText;
    }

    /**
     * Display text of post first, then description of link, then show actual link
     * in line afterwards.  Space these sections of text appropriately.
     * @return content
     */
    private CharSequence getShareLinkContent() {
        String content = text;

        if (description != null && description.length() > 0) {
            //add space between content so far and description
            //IF there is any content.
            if (content.length() != 0)
                content += "<br><br>";
            content += description;
        }

        content = content.trim();

        //format the content so far.
        CharSequence formattedContent = formatContent(content);

        //if content not empty, add two lines in between
        String separator = formattedContent.length() == 0 ? "" : "\n\n";

        if (originalLink != null) {
            //concat all together
            formattedContent = TextUtils.concat(formattedContent, separator, originalLink);
        }
        return formattedContent;
    }

    /**
     * Add text of post, then show poll questions listed one by one after each other.
     * @return content
     */
    private CharSequence getPollContent() {
        String content = "";

        if (items != null && items.size() > 0) {

            for (int i = 0; i < items.size() - 1; i++) {
                content += items.get(i).getDisplayText() + "<br>";
            }

            content += items.get(items.size() - 1).getDisplayText();

            return formatContent(content);
        }

        return content;
    }

    /**
     * Shows beginning and ending time of event and location of event.
     * @return content
     */
    private CharSequence getEventContent() {
        String content = "";

        if (eventStartTime != null)
            content += "<b>Begins:</b> " + eventStartTime;

        if (eventEndTime != null)
            content += "<br><b>Ends:</b> " + eventEndTime;

        if (eventLocation != null)
            content += "<br><b>Where:</b> " + eventLocation;

        if (text != null)
            content += "<br><br>" + text;

        return formatContent(content);
    }

    /**
     * Show quiz name, description, grade type (record), whether submissions
     * can be viewed, total point value, and end date in lines one after the other.
     * @return content
     */
    private CharSequence getQuizContent() {
        String content = "";

        if (processedTitle != null)
            content += "<b>Quiz Name:</b> " + processedTitle;

        if (text != null)
            content += "<br><b>Description:</b> " + text;

        content += "<br><b>Record:</b> " + getProcessedGradeType();
        content += "<br><b>View Submission:</b> " + getProcessedViewSubmissions();

        if (totalPointValue != null)
            content += "<br><b>Total Point Value:</b> " + totalPointValue + " PTS";

        content += "<br><b>Available Date:</b> " + getProcessedStartTime();
        content += "<br><b>End Date:</b> " + getProcessedEndTime();

        CharSequence formattedContent = formatContent(content);

        return TextUtils.concat(formattedContent, "\n\n",
                "For now, please use a desktop computer to access this Quiz.");
    }

    /**
     * Show when classcast is active, how long it will last, and the status of the classcast.
     * @return contnet
     */
    private CharSequence getClasscastContent() {
        String content = "";

        if (text != null) content += text;

        content += "<br><b>When:</b> "
                + getProcessedStartTime() + " to " + getProcessedEndTime();
        content += "<br><b>Duration:</b> " + getTimeStringFromMinutes(length);
        content += "<br><b>Status:</b> " + getClassCastStatus();

        CharSequence formattedContent = formatContent(content);
        return TextUtils.concat(formattedContent, "\n\n",
                "Please use a desktop computer to access this ClassCast.");
    }

    /**
     * Return a different string based on json string.
     * @return grade type string
     */
    private String getProcessedGradeType() {
        if (gradeType != null) {
                 if (gradeType.equals("highest")) return "HIGHEST SCORE";
            else if (gradeType.equals("last"))    return "LAST SCORE";
            else if (gradeType.equals("average")) return "AVERAGE SCORE";
        }

        return "";
    }

    /**
     * Return different string based on json string value
     * @return view submission string
     */
    private String getProcessedViewSubmissions() {
        if (viewSubmissions != null) {
            if (viewSubmissions.equals("yes"))
                return "Students can View Submissions without Correct Answers";
            else if (viewSubmissions.equals("yes_n_answer"))
                return "Students can View Submissions with Correct Answers";
            else if (viewSubmissions.equals("no"))
                return "Students cannot View Submissions";
        }

        return "";
    }

    private transient String postFromText;

    /**
     * Init text that shows where this post was made from.
     *
     */
    private void initPostFromText() {
        postFromText = "";
        int otherCount = 0;

        boolean entryExists = courses != null && courses.size() > 0;

        //add name of first course
        if (entryExists) {
            try {
                postFromText += courses.get(0).getName();
            } catch (NullPointerException e) {
                // no name
            }

            for (int i = 1; i < courses.size(); i++) {
                otherCount++;
            }
        }

        //add name of first conexus
        if (conexuses != null && conexuses.size() > 0) {
            try {
                if (entryExists) otherCount++;
                else postFromText += conexuses.get(0).getName();
            } catch (NullPointerException e) {
                // no name
            }

            for (int i = 1; i < conexuses.size(); i++) {
                otherCount++;
            }
        }

        //if there is more than one course or more than one conexus,
        //show message that tells there are others
        if (otherCount > 0) {
            postFromText += ", " + otherCount + " other";

            if (otherCount > 1)
                postFromText += "s";
        }
    }

    public String getPostFromText() {
        return postFromText;
    }

    //type of post
    public enum Type {
        POST, SHARELINK, POLL, EVENT, QUIZ, CLASSCAST
    }

    private transient Type enumType;

    /**
     * Get enum type of the post
     * @return enum type of post
     */
    public Type getEnumType() {
        if (enumType == null) {
            initEnumPostType();
        }

        return enumType;
    }

    /**
     * Init enum type from the json string data.
     */
    private void initEnumPostType() {
        if (postType != null) {
            if (postType.equals("post"))      enumType = Type.POST;
            else if (postType.equals("sharelink")) enumType = Type.SHARELINK;
            else if (postType.equals("survey"))    enumType = Type.POLL;
            else if (postType.equals("event"))     enumType = Type.EVENT;
            else if (postType.equals("quiz"))      enumType = Type.QUIZ;
            else if (postType.equals("classcast")) enumType = Type.CLASSCAST;
        }
    }

    private transient String timeText;

    public String getTimeText() {
        return timeText;
    }

    private void initTimeText() {
        timeText = displayTime;
    }

    //pattern for finding breaks in html
    private static final Pattern breakPattern = Pattern.compile("<\\s*/?\\s*br\\s*/?\\s*>");
    //limit of characters allowed in a truncated post.
    private static final int contentCharLimit = 360;

    private transient boolean contentTruncated = false;

    //fragment is used to get Activity when content is clicked, so as to open another activity

    /**
     * Get the content that will be displayed in the post.  May be truncated if it has too many break
     * tags or has too many characters.
     * todo there has to be a better way to do this.
     * @param text text to modify
     * @return formatted text, ready to display.
     */
    private CharSequence formatContent(String text) {
        contentTruncated = false;
        if (!isFullView) text = truncateTextByNumLines(text);

        cnNumberLinker = new CNNumberLinker();

        //linkify cn numbers
        CharSequence formatText = cnNumberLinker.linkify(text);

        //if not full view and text too long, truncate.
        if (!isFullView && formatText.length() > contentCharLimit) {
            formatText = formatText.subSequence(0, contentCharLimit);
            contentTruncated = true;
        }

        //add ellipses if truncated.
        if (contentTruncated) {
            String ellipses = "...";
            formatText = TextUtils.concat(formatText, ellipses);
        }

        return formatText;
    }

    /**
     * Uses a matcher to search for break tags.  If more than 7 are found,
     * truncates the rest of the content.
     * @param text text to search
     * @return original text or truncated (if too many lines)
     */
    private String truncateTextByNumLines(String text) {
        Matcher breakMatcher = breakPattern.matcher(text);

        boolean keepGoing = true;
        int matchCount = 0;
        while (keepGoing) {
            if (breakMatcher.find()) {
                matchCount ++;
                if (matchCount > 7) {
                    text = text.substring(0, breakMatcher.start() - 1);
                    contentTruncated = true;
                    keepGoing = false;
                }
            } else {
                keepGoing = false;
            }
        }

        return text;
    }

    private transient Spanned processedTitle;

    /**
     * Use from html to get processed title
     */
    public void initProcessedTitle() {
        if (title != null && !title.isEmpty()) {
            processedTitle = Html.fromHtml(title);
        }
    }

    public Spanned getProcessedTitle() {
        return processedTitle;
    }

    /**
     * Gets human readable string from long type json attribute
     * @return human readable string
     */
    private String getProcessedStartTime() {
        if (startTime != null) {
            try {
                Long time = Long.parseLong(startTime);
                return getStringFromTimestamp(time);
            } catch (NumberFormatException e) {
                // whoops
            }
        }

        return "";
    }

    /**
     * Gets human readable string from long type json attribute
     * @return human readable string
     */
    private String getProcessedEndTime() {
        if (endTime != null) {
            try {
                Long time = Long.parseLong(endTime);
                return getStringFromTimestamp(time);
            } catch (NumberFormatException e) {
                // whoops
            }
        }

        return "";
    }

    /**
     * Get human readable string from a long timestamp
     * @param timeStamp timestamp to examine
     * @return human readable string
     */
    private String getStringFromTimestamp(long timeStamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeStamp * 1000);
        SimpleDateFormat dateFormat = new SimpleDateFormat("M/d/yyyy, h:mm a");

        return dateFormat.format(calendar.getTime());
    }

    /**
     * Calculates number of hours that the minutes make.
     * Returns hours and left over minutes in human readable form.
     * @param minutes number of minutes
     * @return human readable string.
     */
    private String getTimeStringFromMinutes(int minutes) {
        int numHours = minutes / 60;
        int numExtraMinutes = minutes % 60;

        String hourString = Integer.toString(numHours) + " Hour";
        if (numHours != 1) hourString += "s";

        String minuteString = "";
        if (numExtraMinutes != 0) {
            minuteString = " and " + Integer.toString(numExtraMinutes) + " Minute";
            if (numExtraMinutes != 1) minuteString += "s";
        }

        return hourString + minuteString;
    }

    /**
     * Gets human readable string that shows the status of a classcast.
     * @return human readable string.
     */
    private String getClassCastStatus() {
        try {
            long begin = Long.parseLong(startTime) * 1000;
            long end = Long.parseLong(endTime) * 1000;

            long currTime = Calendar.getInstance().getTimeInMillis();

            if (begin < currTime)
                if (currTime < end)
                    return "OPEN";
                else
                    return "ENDED";
            else return "COMING SOON";

        } catch (NumberFormatException e) {
            // oopsie
        } catch (NullPointerException e) {
            // poopsie
        }

        return "";
    }
}
