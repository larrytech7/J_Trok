package com.app.android.tensel.models;

import java.io.Serializable;

/**
 * Created by USER on 08/07/2017.
 */

public class SalePost implements Serializable {

    String authorName;
    String authorId;
    String content;
    long timestamp;

    public SalePost() {
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
