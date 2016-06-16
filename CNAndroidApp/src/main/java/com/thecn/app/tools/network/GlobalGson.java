package com.thecn.app.tools.network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.thecn.app.models.conexus.Conexus;
import com.thecn.app.models.course.Course;
import com.thecn.app.models.course.ScoreUser;
import com.thecn.app.models.course.UserScore;
import com.thecn.app.models.content.Email;
import com.thecn.app.models.notification.Notification;
import com.thecn.app.models.content.PollItem;
import com.thecn.app.models.content.Post;
import com.thecn.app.models.content.Reflection;
import com.thecn.app.models.user.User;
import com.thecn.app.models.user.UserCurrentWork;
import com.thecn.app.models.user.UserPosition;
import com.thecn.app.models.notification.UserNewMessage;

import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * Uses gson singleton to fix problems in json grabbed from website (there are inconsistencies).
 * Contains custom deserializers for handling these errors.
 */
public class GlobalGson {

    //use custom deserializers (see below)
    private static Gson gson = new GsonBuilder()
            .registerTypeAdapter(new TypeToken<UserPosition>(){}.getType(),
                    new UserPositionDeserializer())
            .registerTypeAdapter(new TypeToken<UserCurrentWork>() {
            }.getType(),
                    new UserCurrentWorkDeserializer())
            .registerTypeAdapter(new TypeToken<Notification>(){}.getType(),
                    new NotificationDeserializer())
            .registerTypeAdapter(new TypeToken<Email>(){}.getType(),
                    new EmailDeserializer())
            .registerTypeAdapter(new TypeToken<UserNewMessage>(){}.getType(),
                    new UserNewMessageDeserializer())
            .registerTypeAdapter(new TypeToken<ScoreUser>(){}.getType(),
                    new ScoreUserDeserializer())
            .registerTypeAdapter(new TypeToken<PollItem.Submission>(){}.getType(),
                    new PollSubmissionDeserializer())
            .create();

    public static Gson getGson() {
        return gson;
    }

    private GlobalGson() {}

    /**
     * Deserializer for poll submissions.
     */
    public static class PollSubmissionDeserializer implements JsonDeserializer<PollItem.Submission> {

        public PollItem.Submission deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {

            if (!json.isJsonObject()) {
                return null;
            }

            PollItem.Submission submission = new PollItem.Submission();

            JsonObject submissionJson = json.getAsJsonObject();
            JsonElement holder;

            holder = submissionJson.get("answer");
            if (holder != null && holder.isJsonArray()) {
                ArrayList<String> answers = new ArrayList<String>();
                JsonArray answersJson = holder.getAsJsonArray();

//                for (JsonElement e : answersJson) {
//                    String answer = getString(e);
//                    answers.add(answer);
//                }
//
//                submission.setAnswers(answers);
            }

            holder = submissionJson.get("user");
            if (holder != null && holder.isJsonObject()) {
                User user = gson.fromJson(holder, User.class);
                submission.setUser(user);
            }

            return submission;
        }
    }

    /**
     * Deserializer for user new message class
     */
    public static class UserNewMessageDeserializer implements JsonDeserializer<UserNewMessage> {

        public UserNewMessage deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {

            if (json.isJsonArray()) {
                JsonArray jArray = (JsonArray) json;

                UserNewMessage message = new UserNewMessage();

                for (JsonElement e : jArray) {
                    if (e.isJsonObject()) {
                        JsonObject jObject = (JsonObject) e;

                        String type = getString(jObject.get("type"));
                        String id = getString(jObject.get("id"));

                        if (type != null && id != null) {
                            if (type.equals("notification")) {
                                message.addGeneralNotification(id);
                            } else if (type.equals("email")) {
                                message.addEmailNotification(id);
                            } else if (type.equals("new_follower")) {
                                message.addFollowerNotification(id);
                            }
                        }
                    }
                }

                return message;
            }

            return null;
        }
    }

    /**
     * Deserializer for UserPosition object
     */
    public static class UserPositionDeserializer implements JsonDeserializer<UserPosition> {

        public UserPosition deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {

            if (!json.isJsonArray()) {
                JsonObject jObject = (JsonObject) json;

                String position = getString(jObject.get("position"));
                String schoolName = getString(jObject.get("school_name"));
                String type = getString(jObject.get("type"));
                String webAddress = getString(jObject.get("web_address"));

                return new UserPosition(position, schoolName, type, webAddress);
            }

            return null;
        }
    }

    /**
     * Deserializer for ScoreUser object
     */
    public static class ScoreUserDeserializer implements JsonDeserializer<ScoreUser> {

        public ScoreUser deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {

            if (!json.isJsonArray()) {
                JsonObject jObject = (JsonObject) json;

                String id = getString(jObject.get("id"));
                if (id != null) {
                    ScoreUser scoreUser = new ScoreUser(id);

                    boolean hasFlag = getBoolean(jObject.get("has_flag"), false);
                    scoreUser.setHasFlag(hasFlag);

                    JsonElement holder;

                    holder = jObject.get("model");
                    User model;
                    if (holder != null && holder.isJsonObject()) {
                        model = gson.fromJson(holder, User.class);
                    } else {
                        model = null;
                    }
                    scoreUser.setModel(model);

                    holder = jObject.get("score");
                    UserScore score;
                    if (holder != null && holder.isJsonObject()) {
                        score = gson.fromJson(holder, UserScore.class);
                    } else {
                        score = null;
                    }
                    scoreUser.setUserScore(score);

                    String userPosition = getString(jObject.get("user_position"));
                    scoreUser.setUserPosition(userPosition);
                    String userType = getString(jObject.get("user_type"));
                    scoreUser.setUserType(userType);

                    return scoreUser;
                }
            }

            return null;
        }
    }

    /**
     * Deserializer for UserCurrentWork object
     */
    public static class UserCurrentWorkDeserializer implements  JsonDeserializer<UserCurrentWork> {

        public UserCurrentWork deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {

            if (!json.isJsonArray()) {
                JsonObject jObject = (JsonObject) json;

                String company = getString(jObject.get("company"));
                String position = getString(jObject.get("position"));

                return new UserCurrentWork(company, position);
            }

            return null;
        }
    }

    /**
     * Deserializer for Notification object.
     */
    public static class NotificationDeserializer implements JsonDeserializer<Notification> {

        public Notification deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {

            if (json != null && json.isJsonObject()) {
                JsonObject jObject = (JsonObject) json;

                Notification notification = new Notification();

                String displayTime = getString(jObject.get("display_time"));
                notification.setDisplayTime(displayTime);

                String id = getString(jObject.get("id"));
                notification.setId(id);

                String mark = getString(jObject.get("mark"));
                notification.setMark(mark);

                String type = getString(jObject.get("type"));
                if(type.equalsIgnoreCase("like_content"))
                    return null;
                notification.setType(type);

                ArrayList<Course> courses = new ArrayList<Course>();
                ArrayList<Conexus> conexuses = new ArrayList<Conexus>();
                ArrayList<Post> posts = new ArrayList<Post>();
                ArrayList<Reflection> reflections = new ArrayList<Reflection>();

                JsonElement holder;
                holder = jObject.get("extra_data");
                if (holder != null && holder.isJsonArray()) {
                    JsonArray jArray = (JsonArray) holder;

                    for (JsonElement e : jArray) {
                        if (e != null && e.isJsonObject()) {
                            JsonObject extraObj = (JsonObject) e;

                            String dataType = getString(extraObj.get("data_type"));

                            if (dataType != null) {
                                if (dataType.equals("model")) {

                                    String modelType = getString(extraObj.get("data_model_name"));

                                    if (modelType != null) {
                                        holder = extraObj.get("value");

                                        if (modelType.equals("course")) {
                                            courses.add(gson.fromJson(holder, Course.class));
                                            notification.setModelType(modelType);

                                        } else if (modelType.equals("conexus")) {
                                            conexuses.add(gson.fromJson(holder, Conexus.class));
                                            notification.setModelType(modelType);

                                        } else if (modelType.equals("post")       ||
                                                   modelType.equals("sharelink")  ||
                                                   modelType.equals("survey")     ||
                                                   modelType.equals("event")      ||
                                                   modelType.equals("classcast")) {

                                            posts.add(gson.fromJson(holder, Post.class));
                                            notification.setModelType(modelType);

                                        } else if (modelType.equals("content_comment")) {
                                            reflections.add(gson.fromJson(holder, Reflection.class));

                                        }
                                    }
                                } else if (dataType.equals("text")) {
                                    String sysMsg = getString(extraObj.get("value"));
                                    notification.setSystemMessage(sysMsg);
                                }
                            }
                        }
                    }
                }

                notification.setCourses(courses);
                notification.setConexuses(conexuses);
                notification.setPosts(posts);
                notification.setReflections(reflections);

                ArrayList<User> users = new ArrayList<>();

                holder = jObject.get("users");
                if (holder != null && holder.isJsonArray()) {
                    JsonArray jArray = (JsonArray) holder;

                    for (JsonElement e : jArray) {
                        if (e != null && e.isJsonObject()) {
                            users.add(gson.fromJson(e, User.class));
                        }
                    }
                }

                notification.setUsers(users);
                notification.setUp();

                return notification;
            }

            return null;
        }

    }

    /**
     * Deserializer for Email object
     */
    public static class EmailDeserializer implements JsonDeserializer<Email>{

        public Email deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {

            if (json.isJsonObject()) {
                JsonObject jObject = json.getAsJsonObject();

                Email email = new Email();

                String content = getString(jObject.get("content"));
                email.setContent(content);

                String displayTime = getString(jObject.get("display_time"));
                email.setDisplayTime(displayTime);

                String id = getString(jObject.get("id"));
                email.setId(id);

                String parentId = getString(jObject.get("parent_email_id"));
                email.setParentId(parentId);

                boolean isReply = getBoolean(jObject.get("is_reply_email"));
                email.setReply(isReply);

                boolean isSender = getBoolean(jObject.get("is_sender"));
                email.setSender(isSender);

                boolean isUnread = getBoolean(jObject.get("is_unread"));
                email.setUnread(isUnread);

                String subject = getString(jObject.get("subject"));
                email.setSubject(subject);

                String type = getString(jObject.get("type"));
                email.setType(type);

                JsonElement holder;

                holder = jObject.get("extra_data");
                if (holder != null && holder.isJsonArray()) {
                    JsonArray jArray = holder.getAsJsonArray();

                    if (jArray.size() > 0) {
                        holder = jArray.get(0);

                        if (holder.isJsonObject()) {
                            JsonObject extraObject = holder.getAsJsonObject();

                            String model = getString(extraObject.get("model"));
                            if (model != null) {
                                if (model.equals("course")) {

                                    String courseID = getString(extraObject.get("value"));

                                    if (courseID != null) {
                                        String courseName = getString(extraObject.get("course_name"));
                                        String courseNumber = getString(extraObject.get("course_number"));

                                        Course course = new Course(courseID);
                                        course.setName(courseName);
                                        course.setCourseNumber(courseNumber);

                                        email.setCourse(course);
                                    }

                                } else if (model.equals("conexus")) {

                                    String conexusID = getString(extraObject.get("value"));

                                    if (conexusID != null) {
                                        String conexusName = getString(extraObject.get("conexus_name"));
                                        String conexusNumber = getString(extraObject.get("conexus_number"));

                                        Conexus conexus = new Conexus(conexusID);
                                        conexus.setName(conexusName);
                                        conexus.setConexusNumber(conexusNumber);

                                        email.setConexus(conexus);
                                    }
                                }
                            }
                        }
                    }
                }

                holder = jObject.get("receivers");
                if (holder != null && holder.isJsonArray()) {
                    JsonArray jArray = holder.getAsJsonArray();
                    ArrayList<User> toUsers = new ArrayList<>();
                    ArrayList<User> ccUsers = new ArrayList<>();
                    ArrayList<Email.Address> nonMembers = new ArrayList<>();

                    for (JsonElement e : jArray) {
                        if (e != null && e.isJsonObject()) {
                            JsonObject extraObject = e.getAsJsonObject();

                            String modelType = getString(extraObject.get("type"));
                            holder = extraObject.get("model");

                            if (modelType != null && holder != null && holder.isJsonObject()) {
                                if (modelType.equals("user")) {

                                    User user = gson.fromJson(holder, User.class);

                                    if (user != null) {
                                        String receiveType = user.getReceiveType();

                                        if (receiveType != null) {
                                            if (receiveType.equals("normal")) {
                                                toUsers.add(user);
                                            } else if (receiveType.equals("cc")) {
                                                ccUsers.add(user);
                                            }
                                        }
                                    }
                                } else if (modelType.equals("email")) {
                                    JsonObject modelObject = holder.getAsJsonObject();
                                    try {
                                        String emailAddress = modelObject.get("id").getAsString();
                                        String receiveType = modelObject.get("receive_type").getAsString();

                                        Email.Address address = new Email.Address(emailAddress, modelType);
                                        address.setReceiveType(receiveType);

                                        nonMembers.add(address);
                                    } catch (ClassCastException exception) {
                                        //do nothing
                                    } catch (IllegalStateException exception) {
                                        //do nothing
                                    }

                                }
                            }
                        }
                    }

                    if (toUsers.size() > 0) {
                        email.setToUsers(toUsers);
                    }

                    if (ccUsers.size() > 0) {
                        email.setCCUsers(ccUsers);
                    }

                    if (nonMembers.size() > 0) {
                        email.setNonMemberRecipients(nonMembers);
                    }
                }

                holder = jObject.get("sender");
                User sender;
                if (holder != null && holder.isJsonObject()) {
                    sender = gson.fromJson(holder, User.class);
                } else {
                    sender = null;
                }
                email.setSender(sender);

                holder = jObject.get("sub_emails");
                if (holder != null && holder.isJsonArray()) {
                    JsonArray jArray = holder.getAsJsonArray();
                    ArrayList<Email> subEmails = new ArrayList<Email>();

                    for (JsonElement e : jArray) {
                        if (e != null && e.isJsonObject()) {
                            subEmails.add(gson.fromJson(e, Email.class));
                        }
                    }

                    if (subEmails.size() > 0) {
                        email.setSubEmails(subEmails);
                    }


                }

                return email;
            }

            return null;
        }
    }

    /**
     * Get boolean from jElement with default as false
     * @param jElement element to get boolean from
     * @return boolean value
     */
    private static boolean getBoolean(JsonElement jElement) {
        return getBoolean(jElement, false);
    }

    /**
     * Try to get a boolean value from json element.  Uses failValue on failure
     * @param jElement element to get boolean from
     * @param failValue value to return on fail
     * @return boolean value
     */
    private static boolean getBoolean(JsonElement jElement, boolean failValue) {
        boolean retVal;

        if (jElement != null) {
            try {
                retVal = jElement.getAsBoolean();
            } catch (ClassCastException e) {
                retVal = failValue;
            } catch (IllegalStateException e) {
                retVal = failValue;
            }
        } else {
            retVal = failValue;
        }

        return retVal;
    }

    /**
     * Get string value from json element
     * @param jElement json element to get string from
     * @return string value
     */
    private static String getString(JsonElement jElement) {
        return getString(jElement, null);
    }

    /**
     * Get string value from json element
     * @param jElement json element to get string from
     * @param failValue value to use on failure
     * @return string value
     */
    private static String getString(JsonElement jElement, String failValue) {
        String retVal;

        if (jElement != null) {
            try {
                retVal = jElement.getAsString();
            } catch (ClassCastException e) {
                retVal = failValue;
            } catch (IllegalStateException e) {
                retVal = failValue;
            }
        } else {
            retVal = failValue;
        }

        return retVal;
    }
}
