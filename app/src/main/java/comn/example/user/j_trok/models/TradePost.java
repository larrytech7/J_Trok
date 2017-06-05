package comn.example.user.j_trok.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Larry Akah on 5/19/17.
 */

public class TradePost implements Serializable {

    private String tradePostId;
    private String authorName;
    private String authorId;
    private String authorProfileImage;
    private String tradeNameTitle;
    private long tradeAmount; //amount selling or buying for
    private String currency; //currency
    private String tradeDescription;
    private String tradeLocation;
    private long tradeTime; //time first posted
    private String videoThumbnailUrl;
    private String tradeVideoUrl;
    private String tradeImageUrl;
    private List<String> tags;
    private Map<String, Boolean> likes;

    public TradePost() {
        this.tags = new ArrayList<>();
    }

    public String getTradePostId() {
        return tradePostId;
    }

    public void setTradePostId(String tradePostId) {
        this.tradePostId = tradePostId;
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

    public String getTradeNameTitle() {
        return tradeNameTitle;
    }

    public void setTradeNameTitle(String tradeNameTitle) {
        this.tradeNameTitle = tradeNameTitle;
    }

    public long getTradeAmount() {
        return tradeAmount;
    }

    public void setTradeAmount(long tradeAmount) {
        this.tradeAmount = tradeAmount;
    }

    public String getTradeDescription() {
        return tradeDescription;
    }

    public void setTradeDescription(String tradeDescription) {
        this.tradeDescription = tradeDescription;
    }

    public String getTradeLocation() {
        return tradeLocation;
    }

    public void setTradeLocation(String tradeLocation) {
        this.tradeLocation = tradeLocation;
    }

    public long getTradeTime() {
        return tradeTime;
    }

    public void setTradeTime(long tradeTime) {
        this.tradeTime = tradeTime;
    }

    public String getTradeVideoUrl() {
        return tradeVideoUrl;
    }

    public void setTradeVideoUrl(String tradeVideoUrl) {
        this.tradeVideoUrl = tradeVideoUrl;
    }

    public String getTradeImageUrl() {
        return tradeImageUrl;
    }

    public void setTradeImageUrl(String tradeImageUrl) {
        this.tradeImageUrl = tradeImageUrl;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getVideoThumbnailUrl() {
        return videoThumbnailUrl;
    }

    public void setVideoThumbnailUrl(String videoThumbnailUrl) {
        this.videoThumbnailUrl = videoThumbnailUrl;
    }

    public String getCurrency() {
        return currency == null? "$" : currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Map<String, Boolean> getLikes() {
        return likes == null ? new HashMap<String, Boolean>() : likes;
    }

    public void setLikes(Map<String, Boolean> likes) {
        this.likes = likes;
    }
}
