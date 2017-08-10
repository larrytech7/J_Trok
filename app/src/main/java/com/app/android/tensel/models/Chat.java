package com.app.android.tensel.models;

import java.io.Serializable;

/**
 * Created by Larry Akah on 5/19/17.
 */

public class Chat implements Serializable {

    private String authorName;
    private String authorId;
    private String authorProfileImage;
    private String chatText;
    private long chatDateTime;
    private String chatExtraImageUrl;
    private String itemAuthorId; //this is the userid of the author/owner of the item (tradepost or sale) user is chatting on
    private boolean hasImage;

    public Chat() {
    }

    public Chat(String authorName, String authorId, String authorProfileImage, String chatText, long chatDateTime, String chatExtraImageUrl) {
        this.authorName = authorName;
        this.authorId = authorId;
        this.authorProfileImage = authorProfileImage;
        this.chatText = chatText;
        this.chatDateTime = chatDateTime;
        this.chatExtraImageUrl = chatExtraImageUrl;
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

    public String getAuthorProfileImage() {
        return authorProfileImage;
    }

    public void setAuthorProfileImage(String authorProfileImage) {
        this.authorProfileImage = authorProfileImage;
    }

    public String getChatText() {
        return chatText;
    }

    public void setChatText(String chatText) {
        this.chatText = chatText;
    }

    public long getChatDateTime() {
        return chatDateTime;
    }

    public void setChatDateTime(long chatDateTime) {
        this.chatDateTime = chatDateTime;
    }

    public String getChatExtraImageUrl() {
        return chatExtraImageUrl;
    }

    public void setChatExtraImageUrl(String chatExtraImageUrl) {
        this.chatExtraImageUrl = chatExtraImageUrl;
    }

    public String getItemAuthorId() {
        return itemAuthorId;
    }

    public void setItemAuthorId(String itemAuthorId) {
        this.itemAuthorId = itemAuthorId;
    }

    public boolean isHasImage() {
        return hasImage;
    }

    public void setHasImage(boolean hasImage) {
        this.hasImage = hasImage;
    }
}
