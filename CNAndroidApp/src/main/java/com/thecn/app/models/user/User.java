package com.thecn.app.models.user;

import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.util.Log;

import com.google.gson.annotations.SerializedName;
import com.thecn.app.AppSession;
import com.thecn.app.models.profile.Avatar;
import com.thecn.app.models.conexus.Conexus;
import com.thecn.app.models.profile.Country;
import com.thecn.app.models.course.Course;
import com.thecn.app.models.util.Time;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Model for user construct in server json
 */
public class User implements Serializable {

    //system admin user id (from server)
    private static final String SYS_ID = "system_user";

    @SerializedName("id")
    private String id;

    @SerializedName("cn_number")
    private String CNNumber;

    @SerializedName("first_name")
    private String firstName;

    @SerializedName("last_name")
    private String lastName;

    @SerializedName("display_name")
    private String displayName;

    @SerializedName("avatar")
    private Avatar avatar;

    @SerializedName("country")
    private Country country;

    @SerializedName("courses")
    private ArrayList<Course> courses;

    @SerializedName("conexuses")
    private ArrayList<Conexus> conexuses;

    @SerializedName("profile")
    private UserProfile userProfile;

    @SerializedName("score")
    private Score score;

    @SerializedName("hide_public_contents")
    private boolean hidePublicContents;

    @SerializedName("receive_type")
    private String receiveType;

    @SerializedName("relations")
    private Relations relations;

    @SerializedName("status")
    private int status;

    @SerializedName("visible_settings")
    private VisibilitySettings visibilitySettings;

    @SerializedName("display_login_time")
    private String loginTime;

    @SerializedName("count")
    private Count count;

    private transient ArrayList<User> following;

    private transient ArrayList<User> followers;

    private Settings settings;

    /**
     * Check if system user
     * @return true if user is system user
     */
    public boolean isSystemUser() {
        return id != null && id.equals(SYS_ID);
    }

    public User(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCNNumber() {
        return CNNumber;
    }

    public void setCNNumber(String CNNumber) {
        this.CNNumber = CNNumber;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Avatar getAvatar() {
        return avatar;
    }

    public void setAvatar(Avatar avatar) {
        this.avatar = avatar;
    }

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
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

    public UserProfile getUserProfile() {
        return userProfile;
    }

    public void setUserProfile(UserProfile userProfile) {
        this.userProfile = userProfile;
    }

    public Score getScore() {
        return score;
    }

    public void setScore(Score score) {
        this.score = score;
    }

    public String getReceiveType() {
        return receiveType;
    }

    public void setReceiveType(String receiveType) {
        this.receiveType = receiveType;
    }

    public Relations getRelations() {
        return relations;
    }

    public void setRelations(Relations relations) {
        this.relations = relations;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(String loginTime) {
        this.loginTime = loginTime;
    }

    public VisibilitySettings getVisibilitySettings() {
        return visibilitySettings;
    }

    public void setVisibilitySettings(VisibilitySettings visibilitySettings) {
        this.visibilitySettings = visibilitySettings;
    }

    public boolean isHidePublicContents() {
        return hidePublicContents;
    }

    public void setHidePublicContents(boolean hidePublicContents) {
        this.hidePublicContents = hidePublicContents;
    }

    public Settings getSettings() {
        return settings;
    }

    public void setSettings(Settings settings) {
        this.settings = settings;
    }

    public Count getCount() {
        return count;
    }

    public void setCount(Count count) {
        this.count = count;
    }

    public ArrayList<User> getFollowing() {
        return following;
    }

    public void setFollowing(ArrayList<User> following) {
        this.following = following;
    }

    public ArrayList<User> getFollowers() {
        return followers;
    }

    public void setFollowers(ArrayList<User> followers) {
        this.followers = followers;
    }

    /**
     * Checks whether this user is the same user currently logged into the app.
     * @return true if this user is the logged in user.
     */
    public boolean isMe() {
        //relations can be null if not requested in network requests
        if (relations == null) {
            relations = new Relations();

            String myID = AppSession.getInstance().getUser().getId();
            boolean isMyself = id != null && myID != null && id.equals(myID);

            relations.setMyself(isMyself);
        }

        return relations.isMyself();
    }

    /**
     * Model to represent local settings and values for a user.
     */
    public static class Settings implements Serializable {
        private String id;         //id of user
        private boolean playQuery; //whether user has been asked to install google play services

        private boolean showNotifications;
        private boolean showGeneralNotifications;
        private boolean showEmailNotifications;
        private boolean showFollowerNotifications;

        private int userSpecifiedRefreshTime; //interval user has specified as notification polling interval
        private long refreshNotificationInterval;
        private long lastNotificationRefreshTime;

        public static final int FIVE_MINUTES = 0;
        public static final int TEN_MINUTES = 1;
        public static final int FIFTEEN_MINUTES = 2;
        public static final int THIRTY_MINUTES = 3;
        public static final int HOUR = 4;
        public static final int TWO_HOURS = 5;
        public static final int FIVE_HOURS = 6;
        public static final int TEN_HOURS = 7;
        public static final int DAY = 8;

        public Settings() {
            userSpecifiedRefreshTime = HOUR;
            refreshNotificationInterval = Time.TEN_HOURS;
            lastNotificationRefreshTime = 0;

            showGeneralNotifications = true;
            showEmailNotifications = true;
            showFollowerNotifications = true;
        }

        public Settings(String id, boolean playQuery, boolean showNotifications) {
            this();
            this.id = id;
            this.playQuery = playQuery;
            this.showNotifications = showNotifications;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public boolean isPlayQuery() {
            return playQuery;
        }

        public void setPlayQuery(boolean playQuery) {
            this.playQuery = playQuery;
        }

        public boolean isShowNotifications() {
            return showNotifications;
        }

        public void setShowNotifications(boolean showNotifications) {
            this.showNotifications = showNotifications;
        }

        public long getRefreshNotificationInterval() {
            return refreshNotificationInterval;
        }

        public void setRefreshNotificationInterval(long refreshNotificationInterval) {
            this.refreshNotificationInterval = refreshNotificationInterval;
        }

        public long getLastNotificationRefreshTime() {
            return lastNotificationRefreshTime;
        }

        public void setLastNotificationRefreshTime(long lastNotificationRefreshTime) {
            this.lastNotificationRefreshTime = lastNotificationRefreshTime;
        }

        public boolean isShowGeneralNotifications() {
            return showGeneralNotifications;
        }

        public void setShowGeneralNotifications(boolean showGeneralNotifications) {
            this.showGeneralNotifications = showGeneralNotifications;
        }

        public boolean isShowEmailNotifications() {
            return showEmailNotifications;
        }

        public void setShowEmailNotifications(boolean showEmailNotifications) {
            this.showEmailNotifications = showEmailNotifications;
        }

        public boolean isShowFollowerNotifications() {
            return showFollowerNotifications;
        }

        public void setShowFollowerNotifications(boolean showFollowerNotifications) {
            this.showFollowerNotifications = showFollowerNotifications;
        }

        public int getUserSpecifiedRefreshTime() {
            return userSpecifiedRefreshTime;
        }

        public void setUserSpecifiedRefreshTime(int userSpecifiedRefreshTime) {
            this.userSpecifiedRefreshTime = userSpecifiedRefreshTime;
        }
    }

    /**
     * Model for relations construct in server json
     */
    public static class Relations implements Serializable {
        @SerializedName("is_my_colleague_user")
        private boolean colleague;

        @SerializedName("is_my_follower_user")
        private boolean follower;

        @SerializedName("is_my_following_user")
        private boolean following;

        private boolean pendingFollower; //for pending network requests
        private boolean pendingFollowing;

        @SerializedName("is_myself")
        private boolean myself;

        public boolean isColleague() {
            return colleague;
        }

        public void setColleague(boolean colleague) {
            this.colleague = colleague;
        }

        public boolean isFollower() {
            return follower;
        }

        public void setFollower(boolean follower) {
            this.follower = follower;
        }

        public boolean isFollowing() {
            return following;
        }

        public void setFollowing(boolean following) {
            this.following = following;
        }

        public boolean isPendingFollower() {
            return pendingFollower;
        }

        public void setPendingFollower(boolean pendingFollower) {
            this.pendingFollower = pendingFollower;
        }

        public boolean isPendingFollowing() {
            return pendingFollowing;
        }

        public void setPendingFollowing(boolean pendingFollowing) {
            this.pendingFollowing = pendingFollowing;
        }

        public boolean isMyself() {
            return myself;
        }

        public void setMyself(boolean myself) {
            this.myself = myself;
        }
    }

    /**
     * Model for count contruct in user json
     * Represents count of certain attributes of user.
     */
    public static class Count implements Serializable {
        @SerializedName("colleague")
        public int colleauge;

        @SerializedName("conexus")
        public int conexus;

        @SerializedName("content")
        public int content;

        @SerializedName("course")
        public int course;

        @SerializedName("follower")
        public int follower;

        @SerializedName("following")
        public int following;

        @SerializedName("login")
        public int logins;

        @SerializedName("remind")
        public int remind;
    }

    /**
     * Model of visibility_settings construct in user json.
     * Represents visibility settings of different attributes of user.
     */
    public static class VisibilitySettings implements Serializable{
        @SerializedName("about")
        private String about;

        @SerializedName("birthday")
        private String birthday;

        @SerializedName("country_id")
        private String countryID;

        @SerializedName("current_city_id")
        private String currentCityID;

        @SerializedName("current_work")
        private String currentWork;

        @SerializedName("facebook_address")
        private String facebookAddress;

        @SerializedName("gender")
        private String gender;

        @SerializedName("interests")
        private String interests;

        @SerializedName("origin_country_id")
        private String originCountryID;

        @SerializedName("other_languages")
        private String otherLanguages;

        @SerializedName("position")
        private String position;

        @SerializedName("primary_email")
        private String primaryEmail;

        @SerializedName("primary_language")
        private String primaryLanguage;

        @SerializedName("schools")
        private String schools;

        @SerializedName("secondary_email")
        private String secondaryEmail;

        @SerializedName("tag_line")
        private String tagLine;

        @SerializedName("time_zone_id")
        private String timeZoneID;

        @SerializedName("twitter_name")
        private String twitterName;

        @SerializedName("website")
        private String website;

        @SerializedName("works")
        private String works;

        @SerializedName("zip_code")
        private String zipCode;

        public String getAbout() {
            return about;
        }

        public void setAbout(String about) {
            this.about = about;
        }

        public String getBirthday() {
            return birthday;
        }

        public void setBirthday(String birthday) {
            this.birthday = birthday;
        }

        public String getCountryID() {
            return countryID;
        }

        public void setCountryID(String countryID) {
            this.countryID = countryID;
        }

        public String getCurrentCityID() {
            return currentCityID;
        }

        public void setCurrentCityID(String currentCityID) {
            this.currentCityID = currentCityID;
        }

        public String getCurrentWork() {
            return currentWork;
        }

        public void setCurrentWork(String currentWork) {
            this.currentWork = currentWork;
        }

        public String getFacebookAddress() {
            return facebookAddress;
        }

        public void setFacebookAddress(String facebookAddress) {
            this.facebookAddress = facebookAddress;
        }

        public String getGender() {
            return gender;
        }

        public void setGender(String gender) {
            this.gender = gender;
        }

        public String getInterests() {
            return interests;
        }

        public void setInterests(String interests) {
            this.interests = interests;
        }

        public String getOriginCountryID() {
            return originCountryID;
        }

        public void setOriginCountryID(String originCountryID) {
            this.originCountryID = originCountryID;
        }

        public String getOtherLanguages() {
            return otherLanguages;
        }

        public void setOtherLanguages(String otherLanguages) {
            this.otherLanguages = otherLanguages;
        }

        public String getPosition() {
            return position;
        }

        public void setPosition(String position) {
            this.position = position;
        }

        public String getPrimaryEmail() {
            return primaryEmail;
        }

        public void setPrimaryEmail(String primaryEmail) {
            this.primaryEmail = primaryEmail;
        }

        public String getPrimaryLanguage() {
            return primaryLanguage;
        }

        public void setPrimaryLanguage(String primaryLanguage) {
            this.primaryLanguage = primaryLanguage;
        }

        public String getSchools() {
            return schools;
        }

        public void setSchools(String schools) {
            this.schools = schools;
        }

        public String getSecondaryEmail() {
            return secondaryEmail;
        }

        public void setSecondaryEmail(String secondaryEmail) {
            this.secondaryEmail = secondaryEmail;
        }

        public String getTagLine() {
            return tagLine;
        }

        public void setTagLine(String tagLine) {
            this.tagLine = tagLine;
        }

        public String getTimeZoneID() {
            return timeZoneID;
        }

        public void setTimeZoneID(String timeZoneID) {
            this.timeZoneID = timeZoneID;
        }

        public String getTwitterName() {
            return twitterName;
        }

        public void setTwitterName(String twitterName) {
            this.twitterName = twitterName;
        }

        public String getWebsite() {
            return website;
        }

        public void setWebsite(String website) {
            this.website = website;
        }

        public String getWorks() {
            return works;
        }

        public void setWorks(String works) {
            this.works = works;
        }

        public String getZipCode() {
            return zipCode;
        }

        public void setZipCode(String zipCode) {
            this.zipCode = zipCode;
        }
    }

    /**
     * Get a Spannable that shows the user's basic info, constructed from
     * this user object's data.
     * This includes:
     *
     * gender
     * primary email
     * primary language
     * country
     * time zone
     *
     * @return basic info spannable.
     */
    public Spannable getBasicInfoSpannable() {
        SpannableStringBuilder masterBuilder = new SpannableStringBuilder();

        String gender, email, language, country, timeZone;
        gender = email = language = country = timeZone = null;

        if (userProfile != null) {
            gender = userProfile.getGender();
            email = userProfile.getPrimaryEmail();
            language = userProfile.getPrimaryLanguage();
            timeZone = userProfile.getTimeZone();
        }

        if (this.country != null) {
            country = this.country.getName();
        }

        boolean hasBasicInfo = gender != null ||
                email != null ||
                language != null ||
                country != null ||
                timeZone != null ||
                loginTime != null;

        if (hasBasicInfo) {

            ArrayList<SpannableStringBuilder> list = new ArrayList<>();
            list.ensureCapacity(6);

            createSpannable(list, "Gender", gender);
            createSpannable(list, "Primary Email", email);
            createSpannable(list, "Primary Language", language);
            createSpannable(list, "Country", country);
            createSpannable(list, "Time Zone", timeZone);
            createSpannable(list, "Last Visited", loginTime);

            if (list.size() > 0) {
                masterBuilder.append(list.get(0));

                for (int i = 1; i < list.size(); i++) {
                    masterBuilder.append("\n");
                    masterBuilder.append(list.get(i));
                }
            }
        }

        return masterBuilder;
    }

    /**
     * Helper for {@link #getBasicInfoSpannable()}
     * Creates a spannable for an attribute (group) of basic info.  Returns if content is null.
     * Adds spannable to the given list if it was created.
     * @param list list to add spannable to
     * @param groupName name of group (attribute)
     * @param groupContent content of group (attribute)
     */
    private void createSpannable(ArrayList<SpannableStringBuilder> list, String groupName, String groupContent) {
        if (groupContent == null || groupContent.isEmpty()) return;

        SpannableStringBuilder builder = new SpannableStringBuilder(groupName + ": " + groupContent);
        builder.setSpan(new StyleSpan(Typeface.BOLD), 0, groupName.length() + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        list.add(builder);
    }
}
