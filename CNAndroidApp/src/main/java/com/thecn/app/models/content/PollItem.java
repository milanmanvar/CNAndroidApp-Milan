package com.thecn.app.models.content;

import com.google.gson.annotations.SerializedName;
import com.thecn.app.models.user.User;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Model for poll item construct in server json.
 */
public class PollItem implements Serializable {
    @SerializedName("chart_data")
    private ArrayList<ChartMember> chartData;

    @SerializedName("choices")
    private ArrayList<Choice> choices;

    @SerializedName("correct_response_text")
    private String correctResponse;

    @SerializedName("count_attachment")
    private int attachmentCount;

    @SerializedName("count_file")
    private int fileCount;

    @SerializedName("count_link")
    private int linkCount;

    @SerializedName("count_picture")
    private int pictureCount;

    @SerializedName("count_video")
    private int videoCount;

    //not sure what the purpose of this...seems to be a duplicate
    @SerializedName("display_correct_response_text")
    private String displayCorrectResponse;

    @SerializedName("display_submissions_count")
    private int displaySubmissionCount;

    @SerializedName("display_text")
    private String displayText;

    @SerializedName("has_submissions_count")
    private boolean hasSubmissionsCount;

    @SerializedName("id")
    private String id;

    @SerializedName("is_display_result")
    private boolean displayResult;

    @SerializedName("is_display_user")
    private boolean displayUser;

    @SerializedName("is_enable")
    private boolean enabled;

    @SerializedName("is_end")
    private boolean ended;

    @SerializedName("is_owner")
    private boolean ownerIsMe;

    @SerializedName("is_pictures")
    private boolean hasPictures;

    @SerializedName("is_short_answer_type")
    private boolean isShortAnswer;

    @SerializedName("is_user_submit")
    private boolean userHasSubmitted;

    @SerializedName("question_count")
    private int questionCount;

    @SerializedName("question_order")
    private int questionOrder;

    @SerializedName("result_message")
    private boolean showResultMessage;

    @SerializedName("submission_count")
    private int submissionCount;

    @SerializedName("survey_type")
    private String surveyType;

    @SerializedName("text")
    private String text;

    @SerializedName("submissions")
    private ArrayList<Submission> submissions;

    @SerializedName("pictures")
    private ArrayList<Picture> pictures;

    @SerializedName("videos")
    private ArrayList<Video> videos;

    @SerializedName("links")
    private ArrayList<Link> links;

    @SerializedName("attachments")
    private ArrayList<Attachment> attachments;

    private int countTotal;

    public int getCountTotal() {
        return countTotal;
    }

    public void setCountTotal(int countTotal) {
        this.countTotal = countTotal;
    }

    private String shortAnswer;

    /**
     * Type of input to show user.
     */
    public enum InputType {
        SHORT_ANSWER, ONE_CHOICE, MULTIPLE_CHOICE
    }

    private InputType mInputType;

    /**
     * Get input type enum
     * @return input type of poll item.
     */
    public InputType getInputType() {

        if (mInputType == null) {
            mInputType = getInputTypeFromData();
        }

        return mInputType;
    }

    /**
     * Uses surveyType string to determine input type.
     * @return input type of this poll item.
     */
    public InputType getInputTypeFromData() {
        InputType inputType = null;

        if (surveyType.equals("short_answer")) {
            inputType = InputType.SHORT_ANSWER;
        } else if (
                surveyType.equals("yes_no") ||
                        surveyType.equals("scale_5") ||
                        surveyType.equals("scale_10") ||
                        surveyType.equals("one_choice") ||
                        surveyType.equals("true_false") ||
                        surveyType.equals("agree_disagree") ||
                        surveyType.equals("agree_noopinion_disagree") ||
                        surveyType.equals("stronglyagree_agree_noopinion_disagree_stronglydisagree")
                ) {

            inputType = InputType.ONE_CHOICE;
        } else if (surveyType.equals("multiple_choice")) {
            inputType = InputType.MULTIPLE_CHOICE;
        }

        return inputType;
    }

    public void setInputType(InputType inputType) {
        mInputType = inputType;
    }

    /**
     * Denotes how to show poll submissions.
     * With user and answer,
     * just answer,
     * or nothing (not shown).
     */
    public enum SubmissionDisplayType {
        USER_ANSWER, ANSWER, NOTHING
    }

    private SubmissionDisplayType mSubmissionDisplayType;

    /**
     * Get submission display type enum
     * @return submission display type
     */
    public SubmissionDisplayType getSubmissionDisplayType() {

        if (mSubmissionDisplayType == null) {
            mSubmissionDisplayType = getSubmissionDisplayTypeFromData();
        }

        return mSubmissionDisplayType;
    }

    /**
     * Construct submission display type from data.
     * @return submission display type enum
     */
    public SubmissionDisplayType getSubmissionDisplayTypeFromData() {
        SubmissionDisplayType displayType;

        if (getDisplayResult() && getDisplayUser()) {
            displayType = SubmissionDisplayType.USER_ANSWER;
        } else if (getDisplayResult()) {
            displayType = SubmissionDisplayType.ANSWER;
        } else {
            displayType = SubmissionDisplayType.NOTHING;
        }

        return displayType;
    }

    public void setSubmissionDisplayType(SubmissionDisplayType mSubmissionDisplayType) {
        this.mSubmissionDisplayType = mSubmissionDisplayType;
    }

    public int getSubmissionCount() {
        return submissionCount;
    }

    public void setSubmissionCount(int submissionCount) {
        this.submissionCount = submissionCount;
    }

    public String getShortAnswer() {
        return shortAnswer;
    }

    public void setShortAnswer(String shortAnswer) {
        this.shortAnswer = shortAnswer;
    }

    public ArrayList<ChartMember> getChartData() {
        return chartData;
    }

    public void setChartData(ArrayList<ChartMember> chartData) {
        this.chartData = chartData;
    }

    public ArrayList<Choice> getChoices() {
        return choices;
    }

    public void setChoices(ArrayList<Choice> choices) {
        this.choices = choices;
    }

    public String getCorrectResponse() {
        return correctResponse;
    }

    public void setCorrectResponse(String correctResponse) {
        this.correctResponse = correctResponse;
    }

    public int getAttachmentCount() {
        return attachmentCount;
    }

    public void setAttachmentCount(int attachmentCount) {
        this.attachmentCount = attachmentCount;
    }

    public int getFileCount() {
        return fileCount;
    }

    public void setFileCount(int fileCount) {
        this.fileCount = fileCount;
    }

    public int getLinkCount() {
        return linkCount;
    }

    public void setLinkCount(int linkCount) {
        this.linkCount = linkCount;
    }

    public int getPictureCount() {
        return pictureCount;
    }

    public void setPictureCount(int pictureCount) {
        this.pictureCount = pictureCount;
    }

    public int getVideoCount() {
        return videoCount;
    }

    public void setVideoCount(int videoCount) {
        this.videoCount = videoCount;
    }

    public String getDisplayCorrectResponse() {
        return displayCorrectResponse;
    }

    public void setDisplayCorrectResponse(String displayCorrectResponse) {
        this.displayCorrectResponse = displayCorrectResponse;
    }

    public int getDisplaySubmissionCount() {
        return displaySubmissionCount;
    }

    public void setDisplaySubmissionCount(int displaySubmissionCount) {
        this.displaySubmissionCount = displaySubmissionCount;
    }

    public String getSurveyType() {
        return surveyType;
    }

    public void setSurveyType(String surveyType) {
        this.surveyType = surveyType;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getDisplayText() {
        return displayText;
    }

    public void setDisplayText(String displayText) {
        this.displayText = displayText;
    }

    public boolean getHasSubmissionsCount() {
        return hasSubmissionsCount;
    }

    public void setHasSubmissionsCount(boolean hasSubmissionsCount) {
        this.hasSubmissionsCount = hasSubmissionsCount;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean getDisplayResult() {
        return displayResult;
    }

    public void setDisplayResult(boolean displayResult) {
        this.displayResult = displayResult;
    }

    public boolean getDisplayUser() {
        return displayUser;
    }

    public void setDisplayUser(boolean displayUser) {
        this.displayUser = displayUser;
    }

    public boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean getEnded() {
        return ended;
    }

    public void setEnded(boolean ended) {
        this.ended = ended;
    }

    public boolean getOwnerIsMe() {
        return ownerIsMe;
    }

    public void setOwnerIsMe(boolean ownerIsMe) {
        this.ownerIsMe = ownerIsMe;
    }

    public boolean getHasPictures() {
        return hasPictures;
    }

    public void setHasPictures(boolean hasPictures) {
        this.hasPictures = hasPictures;
    }

    public boolean getIsShortAnswer() {
        return isShortAnswer;
    }

    public void setIsShortAnswer(boolean isShortAnswer) {
        this.isShortAnswer = isShortAnswer;
    }

    public boolean getUserHasSubmitted() {
        return userHasSubmitted;
    }

    public void setUserHasSubmitted(boolean userHasSubmitted) {
        this.userHasSubmitted = userHasSubmitted;
    }

    public int getQuestionCount() {
        return questionCount;
    }

    public void setQuestionCount(int questionCount) {
        this.questionCount = questionCount;
    }

    public int getQuestionOrder() {
        return questionOrder;
    }

    public void setQuestionOrder(int questionOrder) {
        this.questionOrder = questionOrder;
    }

    public boolean getShowResultMessage() {
        return showResultMessage;
    }

    public void setShowResultMessage(boolean showResultMessage) {
        this.showResultMessage = showResultMessage;
    }

    public ArrayList<Submission> getSubmissions() {
        return submissions;
    }

    public void setSubmissions(ArrayList<Submission> submissions) {
        this.submissions = submissions;
    }

    public ArrayList<Picture> getPictures() {
        return pictures;
    }

    public void setPictures(ArrayList<Picture> pictures) {
        this.pictures = pictures;
    }

    public ArrayList<Video> getVideos() {
        return videos;
    }

    public void setVideos(ArrayList<Video> videos) {
        this.videos = videos;
    }

    public ArrayList<Link> getLinks() {
        return links;
    }

    public void setLinks(ArrayList<Link> links) {
        this.links = links;
    }

    public ArrayList<Attachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(ArrayList<Attachment> attachments) {
        this.attachments = attachments;
    }

    /**
     * Model for chart member json construct from server.
     * Used to show pie chart and its legend.
     */
    public static class ChartMember implements Serializable {
        @SerializedName("name")
        private String name;

        @SerializedName("id")
        private String id;

        @SerializedName("count")
        private int count;

        private int color;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public int getColor() {
            return color;
        }

        public void setColor(int color) {
            this.color = color;
        }
    }

    /**
     * Models choice construct in server json.
     * Represents a choice the user can make when answering a poll item.
     */
    public static class Choice implements Serializable {
        @SerializedName("seq_id")
        private String sequenceId;

        @SerializedName("subject")
        private String subject;

        private boolean selected;

        public Choice(String sequenceId, String subject) {
            this.sequenceId = sequenceId;
            this.subject = subject;
        }

        public String getSequenceId() {
            return sequenceId;
        }

        public void setSequenceId(String sequenceId) {
            this.sequenceId = sequenceId;
        }

        public String getSubject() {
            return subject;
        }

        public void setSubject(String subject) {
            this.subject = subject;
        }

        public boolean isSelected() {
            return selected;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }
    }

    /**
     * Model for short answer submission construct in server json.
     * May or may not have user associated with it.
     */
    public static class Submission implements Serializable {
        @SerializedName("answer")
        private ArrayList<String> answers;

        @SerializedName("user")
        private User user;

        public ArrayList<String> getAnswers() {
            return answers;
        }

        public void setAnswers(ArrayList<String> answers) {
            this.answers = answers;
        }

        public User getUser() {
            return user;
        }

        public void setUser(User user) {
            this.user = user;
        }
    }
}
