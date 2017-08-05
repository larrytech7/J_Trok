package com.app.android.tensel.models;

import java.io.Serializable;

/**
 * Created by USER on 08/07/2017.
 */

public class SalePost implements Serializable {

    String authorName;
    String authorId;
    String authorProfileImage;
    String content;
    long timestamp;
    String postId;

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public SalePost() {
    }

    public SalePost(String authorName, String authorId, String content, long timestamp) {
        this.authorName = authorName;
        this.authorId = authorId;
        this.content = content;
        this.timestamp = timestamp;
    }

    public String getAuthorProfileImage() {
        return authorProfileImage;
    }

    public void setAuthorProfileImage(String authorProfileImage) {
        this.authorProfileImage = authorProfileImage;
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

    @Override
    public String toString() {
        return authorName+", "+authorId+" "+content+", "+timestamp;
    }
}
