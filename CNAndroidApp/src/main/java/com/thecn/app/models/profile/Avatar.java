package com.thecn.app.models.profile;

import java.io.Serializable;

/**
 * Model for avatar construct in server json
 */
public class Avatar implements Serializable {
    private String id;
    private String view_url;

    public Avatar(String id, String view_url) {
        this.id = id;
        this.view_url = view_url;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getView_url() {
        return view_url;
    }

    public void setView_url(String view_url) {
        this.view_url = view_url;
    }
}
