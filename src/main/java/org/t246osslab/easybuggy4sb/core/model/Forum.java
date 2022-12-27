package org.t246osslab.easybuggy4sb.core.model;

import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Forum implements Serializable {

    private static final long serialVersionUID = 1L;
    private Date time = null;
    private String username = null;
    private String picture = null;
    private String message = null;

    @Override
    public String toString() {
        return "time=" + time + ", username=" + username + ", picture=" + picture + ", message=" + message;
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
}
