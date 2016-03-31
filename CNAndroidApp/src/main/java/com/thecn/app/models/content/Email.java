package com.thecn.app.models.content;

import com.google.gson.annotations.SerializedName;
import com.thecn.app.models.conexus.Conexus;
import com.thecn.app.models.course.Course;
import com.thecn.app.models.user.User;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Model for email object in server's json
 */
public class Email implements Serializable {

    //type of invitation email
    public static final int INVITE_COURSE =  0;
    public static final int INVITE_CONEXUS = 1;

    //states for invitation emails
    public static final int INVITE_STATE_RECEIVED = 0;
    public static final int INVITE_STATE_WAITING =  1;
    public static final int INVITE_STATE_ACCEPTED = 2;
    public static final int INVITE_STATE_IGNORED = 3;

    @SerializedName("content")
    private String content;

    @SerializedName("display_time")
    private String displayTime;

    @SerializedName("id")
    private String id;

    @SerializedName("is_reply_email")
    private Boolean isReply;

    @SerializedName("parent_email_id")
    private String parentId;

    @SerializedName("is_sender")
    private Boolean isSender;

    @SerializedName("is_unread")
    private Boolean isUnread;

    @SerializedName("subject")
    private String subject;

    @SerializedName("type")
    private String type;

    @SerializedName("sub_emails")
    private ArrayList<Email> subEmails;

    @SerializedName("sender")
    private Address serializedSender;

    @SerializedName("receivers")
    private ArrayList<Address> serializedReceivers;

    @SerializedName("reply_type")
    private String replyType;

    @SerializedName("origin")
    private Email origin;

    private Boolean isInvite;

    private int inviteType;

    private int inviteState;

    private boolean isDeleted;

    /**
     * Return true if this email is a course or conexus invite.
     * @return true if email is an invitation
     */
    public boolean isInvite() {
        if (isInvite != null) return isInvite;

        if (type.equals("course_invite")) {
            inviteType = INVITE_COURSE;
            isInvite = true;
        } else if (type.equals("conexus_invite")) {
            inviteType = INVITE_CONEXUS;
            isInvite = true;
        } else {
            isInvite = false;
        }

        return isInvite;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    public int getInviteType() {
        return inviteType;
    }

    public void setInviteType(int inviteType) {
        this.inviteType = inviteType;
    }

    public int getInviteState() {
        return inviteState;
    }

    public void setInviteState(int inviteState) {
        this.inviteState = inviteState;
    }

    public Email getOrigin() {
        return origin;
    }

    public void setOrigin(Email origin) {
        this.origin = origin;
    }

    public String getReplyType() {
        return replyType;
    }

    public void setReplyType(String replyType) {
        this.replyType = replyType;
    }

    public Address getSerializedSender() {
        return serializedSender;
    }

    public void setSerializedSender(Address serializedSender) {
        this.serializedSender = serializedSender;
    }

    public ArrayList<Address> getSerializedReceivers() {
        return serializedReceivers;
    }

    public void setSerializedReceivers(ArrayList<Address> serializedReceivers) {
        this.serializedReceivers = serializedReceivers;
    }

    private ArrayList<Address> nonMemberRecipients;

    public ArrayList<Address> getNonMemberRecipients() {
        return nonMemberRecipients;
    }

    public void setNonMemberRecipients(ArrayList<Address> nonMemberRecipients) {
        this.nonMemberRecipients = nonMemberRecipients;
    }

    /**
     * Model for address in server json.
     * May be just an email address or may be a user.
     */
    public static class Address implements Serializable {
        @SerializedName("id")
        private String id;

        @SerializedName("type")
        private String type;

        @SerializedName("receive_type")
        private String receiveType;

        private transient User user;

        public Address(String id, String type) {
            this.id = id;
            this.type = type;
        }

        public User getUser() {
            return user;
        }

        public void setUser(User user) {
            this.user = user;
        }

        public String getType() {
            return type;
        }

        public String getId() {
            return id;
        }

        public void setReceiveType(String receiveType) {
            this.receiveType = receiveType;
        }

        public String getReceiveType() {
            return receiveType;
        }
    }

    private transient ArrayList<User> toUsers;

    private transient ArrayList<User> ccUsers;

    private transient User sender;

    private transient Course course;

    private transient Conexus conexus;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

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

    public boolean isReply() {
        return isReply;
    }

    public void setReply(boolean isReply) {
        this.isReply = isReply;
    }

    public boolean isSender() {
        return isSender;
    }

    public void setSender(boolean isSender) {
        this.isSender = isSender;
    }

    public boolean isUnread() {
        return isUnread;
    }

    public void setUnread(boolean isUnread) {
        this.isUnread = isUnread;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public ArrayList<Email> getSubEmails() {
        return subEmails;
    }

    public void setSubEmails(ArrayList<Email> subEmails) {
        this.subEmails = subEmails;
    }

    public User getSender() {
        return sender;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public ArrayList<User> getToUsers() {
        return toUsers;
    }

    public void setToUsers(ArrayList<User> toUsers) {
        this.toUsers = toUsers;
    }

    public ArrayList<User> getCCUsers() {
        return ccUsers;
    }

    public void setCCUsers(ArrayList<User> ccUsers) {
        this.ccUsers = ccUsers;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public Conexus getConexus() {
        return conexus;
    }

    public void setConexus(Conexus conexus) {
        this.conexus = conexus;
    }

    /**
     * Constructs {@link com.thecn.app.models.content.Email.Address} from id of user.
     * @param user user to get id from
     * @return address
     * @throws NullPointerException if there is no id
     */
    public static Address getAddressFromUser(User user) throws NullPointerException{
        String id = user.getId();
        if (id == null) {
            throw mUserIDNullPointer;
        }

        return new Email.Address(id, "user");
    }

    private static final NullPointerException mUserIDNullPointer = new NullPointerException("User ID should not be null");
}
