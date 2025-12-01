package org.t246osslab.easybuggy4sb.core.model;

import java.io.Serializable;
import java.util.Date;

public class Forum implements Serializable {

    private static final long serialVersionUID = 1L;
    private String id = null;
    private Date time = null;
    private String username = null;
    private String picture = null;
    private String message = null;
    private String fileName = null;

    @Override
    public String toString() {
        return "id" + id + ", time=" + time + ", username=" + username + ", picture=" + picture
                + ", message=" + message+ ", fileName=" + fileName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
