package com.thecn.app.models.content;

import com.google.gson.annotations.SerializedName;
import com.thecn.app.tools.text.TextUtil;

import java.io.Serializable;

/**
 * Model for attachment data in server's json
 */
public class Attachment implements Serializable {

    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("download_url")
    private String downloadURL;

    @SerializedName("view_url")
    private String viewURL;

    @SerializedName("extension")
    private String extension;

    @SerializedName("size")
    private String size;

    @SerializedName("display_time")
    private String displayTime;

    @SerializedName("mime_type")
    private String mimeType;

    public Attachment(String id) {
        this.id = id;
    }

    /**
     * Gets file name for this attachment with extension included.
     * @return file name with extension
     */
    public String getNameWithExtension() {

        String retVal;

        if (TextUtil.isNullOrEmpty(name)) {
            retVal = "theCN_file_" + TextUtil.getTimeStamp();
        } else {
            retVal = name;
        }

        if (!TextUtil.isNullOrEmpty(extension)) {
            retVal += "." + extension;
        }

        return retVal;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDownloadURL() {
        return downloadURL;
    }

    public void setDownloadURL(String downloadURL) {
        this.downloadURL = downloadURL;
    }

    public String getViewURL() {
        return viewURL;
    }

    public void setViewURL(String viewURL) {
        this.viewURL = viewURL;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getDisplayTime() {
        return displayTime;
    }

    public void setDisplayTime(String displayTime) {
        this.displayTime = displayTime;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
}
