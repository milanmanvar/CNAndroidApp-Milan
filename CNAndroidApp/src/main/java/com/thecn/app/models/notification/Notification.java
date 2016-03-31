package com.thecn.app.models.notification;

import com.thecn.app.AppSession;
import com.thecn.app.activities.navigation.NavigationActivity;
import com.thecn.app.models.conexus.Conexus;
import com.thecn.app.models.course.Course;
import com.thecn.app.models.user.User;
import com.thecn.app.models.content.Post;
import com.thecn.app.models.content.Reflection;
import com.thecn.app.stores.NotificationStore;
import com.thecn.app.tools.CallbackManager;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Model for notification construct in server json.
 */
public class Notification implements Serializable {

    //SerializedName not needed because a custom deserializer is used

    //actions to take when a notification is clicked.
    public transient static final int VIEW_NOTHING = -1;
    public transient static final int VIEW_POST = 0;
    public transient static final int VIEW_COURSE = 1;
    public transient static final int VIEW_CONEXUS = 2;

    private int linkTypeFlag = VIEW_NOTHING;

    private String displayTime;

    private String id;

    private String mark;

    private String type;

    private String systemMessage;

    private ArrayList<User> users;

    private ArrayList<Course> courses;

    private ArrayList<Conexus> conexuses;

    private ArrayList<Post> posts;

    private ArrayList<Reflection> reflections;

    private String avatarUrl;

    private String modelType;

    public Notification() {}

    public String getDisplayTime() {
        return displayTime;
    }

    public void setDisplayTime(String displayTime) {
        this.displayTime = displayTime;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMark() {
        return mark;
    }

    public void setMark(String mark) {
        this.mark = mark;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public ArrayList<User> getUsers() {
        return users;
    }

    public void setUsers(ArrayList<User> users) {
        this.users = users;
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

    public ArrayList<Post> getPosts() {
        return posts;
    }

    public void setPosts(ArrayList<Post> posts) {
        this.posts = posts;
    }

    public ArrayList<Reflection> getReflections() {
        return reflections;
    }

    private String notificationDescription;

    public void setReflections(ArrayList<Reflection> reflections) {
        this.reflections = reflections;
    }

    public String getSystemMessage() {
        return systemMessage;
    }

    public void setSystemMessage(String systemMessage) {
        this.systemMessage = systemMessage;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public interface NotificationClickCallback {
        public void onLinkClick();

        public void onUserClick();
    }

    public String getNotificationDescription() {
        return notificationDescription;
    }

    public void setNotificationDescription(String notificationDescription) {
        this.notificationDescription = notificationDescription;
    }

    public int getLinkTypeFlag() {
        return linkTypeFlag;
    }

    public void setLinkTypeFlag(int linkTypeFlag) {
        this.linkTypeFlag = linkTypeFlag;
    }

    public void setModelType(String modelType) {
        modelType = modelType.equals("survey") ? "poll" : modelType;

        this.modelType = modelType;
    }

    public String getModelType() {
        return modelType;
    }

    /**
     * Set content of the notification based on its type.
     */
    public void setUp() {
        final User user = users.get(0);
        Post post = null;
        Reflection reflection;
        Course course = null;
        Conexus conexus = null;

        if (type != null) {

            //set notification description based on type.
            if (type.equals("accept_colleague")) {
                notificationDescription = user.getDisplayName() + " accepted your colleague request.";
            } else if (type.equals("accept_conexus_invite")) {
                conexus = (Conexus) getFirstEntry(conexuses);
                notificationDescription = user.getDisplayName() +
                        " accepts your invitation to join the Conexus " + conexus.getName() + ".";
            } else if (type.equals("accept_course_invite")) {
                course = (Course) getFirstEntry(courses);
                notificationDescription = user.getDisplayName() +
                        " accepts your invitation to join the Course " + course.getName() + ".";
            } else if (type.equals("accept_join_conexus")) {
                conexus = (Conexus) getFirstEntry(conexuses);
                notificationDescription = user.getDisplayName() +
                        " accepts your request to join the Conexus " + conexus.getName() + ".";
            } else if (type.equals("accept_join_course")) {
                course = (Course) getFirstEntry(courses);
                notificationDescription = user.getDisplayName() +
                        " accepts your request to join the Course " + course.getName() + ".";
            } else if (type.equals("add_content_comment")) {
                post = (Post) getFirstEntry(posts);
                if (post != null && post.getId() != null) {
                    reflection = (Reflection) getFirstEntry(reflections);
                    if (reflection != null && reflection.getId() != null) {
                        notificationDescription = user.getDisplayName() +
                                " reflected on your " + modelType + ".";
                    } else {
                        notificationDescription = user.getDisplayName() +
                                " reflected on your " + modelType + ", but this reflection has been deleted.";
                    }
                } else {
                    notificationDescription = user.getDisplayName() +
                            " reflected on your " + modelType + ", but this " + modelType + " has been deleted.";
                }
            } else if (type.equals("add_follow")) {
                notificationDescription = user.getDisplayName() +
                        " is now following you.";
            } else if (type.equals("join_conexus")) {
                conexus = (Conexus) getFirstEntry(conexuses);
                notificationDescription = user.getDisplayName() +
                        " joined your Conexus " + conexus.getName();
            } else if (type.equals("join_course")) {
                course = (Course) getFirstEntry(courses);
                notificationDescription = user.getDisplayName() +
                        " joined your Course " + course.getName();
            } else if (type.equals("like_content")) {
                post = (Post) getFirstEntry(posts);
                if (post != null && post.getId() != null) {
                    notificationDescription = user.getDisplayName() +
                            " liked your " + modelType + ".";
                } else {
                    notificationDescription = user.getDisplayName() +
                            " liked your " + modelType + ", but this " + modelType + " has been deleted.";
                }
            } else if (type.equals("mentioned")) {
                post = (Post) getFirstEntry(posts);
                if (post.getId() != null) {
                    notificationDescription = user.getDisplayName() +
                            " mentioned you in a " + modelType + ".";
                } else {
                    notificationDescription = user.getDisplayName() +
                            " mentioned you in a " + modelType + ", but this " + modelType + " has been deleted.";
                }
            } else if (type.equals("others_add_content_comment")) {
                post = (Post) getFirstEntry(posts);
                if (post.getId() != null) {
                    reflection = (Reflection) getFirstEntry(reflections);
                    if (reflection != null && reflection.getId() != null) {
                        notificationDescription = user.getDisplayName() +
                                " reflected on a " + modelType + " you also reflected on.";
                    } else {
                        notificationDescription = user.getDisplayName() +
                                " reflected on a " + modelType + " you also reflected on," +
                                " but this reflection has been deleted.";
                    }
                } else {
                    notificationDescription = user.getDisplayName() +
                            " reflected on a " + modelType + " you also reflected on," +
                            " but this " + modelType + " has been deleted.";
                }
            } else if (type.equals("expired_remind")) {
                post = (Post) getFirstEntry(posts);
                course = (Course) getFirstEntry(courses);
                conexus = (Conexus) getFirstEntry(conexuses);

                String name = "";
                if (course != null) {
                    name = course.getName();
                } else if (conexus != null) {
                    name = conexus.getName();
                }

                if (post != null && post.getId() != null) {
                    notificationDescription = "The event " + user.getDisplayName() +
                            " created in " + name + " is approaching.";
                } else {
                    notificationDescription = "The event " + user.getDisplayName() +
                            " created in " + name + " is approaching, but this event has been deleted.";
                }
            } else if (type.equals("system_message")) {
                if (systemMessage != null && systemMessage.length() > 0) {
                    notificationDescription = user.getDisplayName() + " " + systemMessage;
                } else {
                    notificationDescription = user.getDisplayName() + " sent you a system message, but" +
                            " this message has been deleted.";
                }
            } else if (type.equals("answer_survey")) {
                post = (Post) getFirstEntry(posts);
                if (post != null && post.getId() != null) {
                    notificationDescription = user.getDisplayName() + " answered your " + modelType;
                } else {
                    notificationDescription = user.getDisplayName() + " answered your " + modelType +
                            ", but this " + modelType + " has been deleted.";
                }
            } else if (type.equals("others_like_content")) {
                post = (Post) getFirstEntry(posts);
                if (post != null && post.getId() != null) {
                    notificationDescription = user.getDisplayName() + " liked a " + modelType +
                            " you reflected on.";
                } else {
                    notificationDescription = user.getDisplayName() + " liked a " + modelType +
                            " you reflected on, but this " + modelType + " has been deleted.";
                }
            } else {
                notificationDescription = "This notification is not supported.";
            }
        }

        try {
            avatarUrl = user.getAvatar().getView_url() + ".w160.jpg";
        } catch (NullPointerException e) {
            //something went wrong
        }

        if (post != null && post.getId() != null) {
            linkTypeFlag = VIEW_POST;
        } else if (course != null) {
            linkTypeFlag = VIEW_COURSE;
        } else if (conexus != null) {
            linkTypeFlag = VIEW_CONEXUS;
        } else {
            linkTypeFlag = VIEW_NOTHING;
        }
    }

    /**
     * Get first entry in an arraylist
     * @param list an arraylist
     * @return first entry of list or null
     */
    private Object getFirstEntry(ArrayList list) {
        if (list != null && list.size() > 0) {
            return list.get(0);
        }
        return null;
    }

    //callback called when user taps (clicks) on notification
    private transient NotificationClickCallback callback;

    public NotificationClickCallback getCallback() {
        return callback;
    }

    /**
     * Get an appropriate action for this notification when it is clicked.
     * @param manager for references to an activity.
     * @return appropriate callback
     */
    public NotificationClickCallback getNewCallback(final CallbackManager manager) {

        final User user = users.get(0);

        if (linkTypeFlag == VIEW_POST) {
            //notification opens a post

            final Post post = posts.get(0);
            callback = new NotificationClickCallback() {
                @Override
                public void onLinkClick() {
                    //mark notification read and open post page
                    NotificationStore.markNotificationRead(id);

                    getNavigationActivity(manager).openPostPage(post, false);
                }

                @Override
                public void onUserClick() {
                    getNavigationActivity(manager).openProfileByID(user.getId());
                }
            };
        } else if (linkTypeFlag == VIEW_COURSE) {
            //notification opens a course

            final Course course = courses.get(0);
            callback = new NotificationClickCallback() {
                @Override
                public void onLinkClick() {
                    //check verification
                    //mark notification read and open course

                    NavigationActivity activity = getNavigationActivity(manager);

                    if (AppSession.checkVerification(activity)) {
                        return;
                    }

                    NotificationStore.markNotificationRead(id);
                    activity.openCoursePage(course);
                }

                @Override
                public void onUserClick() {
                    getNavigationActivity(manager).openProfileByID(user.getId());
                }
            };
        } else if (linkTypeFlag == VIEW_CONEXUS) {
            //notification opens a conexus

            final Conexus conexus = conexuses.get(0);
            callback = new NotificationClickCallback() {
                @Override
                public void onLinkClick() {
                    //check verification
                    //mark notification read and open conexus

                    NavigationActivity activity = getNavigationActivity(manager);
                    if (AppSession.checkVerification(activity)) {
                        return;
                    }

                    NotificationStore.markNotificationRead(id);
                    activity.openConexusPage(conexus);
                }

                @Override
                public void onUserClick() {
                    getNavigationActivity(manager).openProfileByID(user.getId());
                }
            };
        } else {
            //just open the user's profile page.

            callback = new NotificationClickCallback() {
                @Override
                public void onLinkClick() {
                    NotificationStore.markNotificationRead(id);
                    getNavigationActivity(manager).openProfileByID(user.getId());
                }

                @Override
                public void onUserClick() { getNavigationActivity(manager).openProfileByID(user.getId());}
            };
        }

        return callback;
    }

    /**
     * Cast activity references by manager to {@link com.thecn.app.activities.navigation.NavigationActivity}
     * @param manager for references to an activity.
     * @return cast activity
     */
    private static NavigationActivity getNavigationActivity(CallbackManager manager) {
        return (NavigationActivity) manager.getActivity();
    }
}
