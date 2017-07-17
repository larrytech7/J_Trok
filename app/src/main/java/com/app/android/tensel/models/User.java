package com.app.android.tensel.models;

import java.io.Serializable;

/**
 * Created by Larry Akah on 5/19/17.
 */

public class User implements Serializable {

    private String userId;
    private String userName;
    private String userEmail;
    private String userProfilePhoto;
    private String userCity;
    private String userCountry;
    private String userStatusText;
    private int buys; //number of stuffs user has had to request to buy
    private int sells; //number of stuffs, user has had to post for sell
    private String userPhoneNumber;
    private long lastUpdatedTime;

    public User() {
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserProfilePhoto() {
        return userProfilePhoto;
    }

    public void setUserProfilePhoto(String userProfilePhoto) {
        this.userProfilePhoto = userProfilePhoto;
    }

    public String getUserCity() {
        return userCity;
    }

    public void setUserCity(String userCity) {
        this.userCity = userCity;
    }

    public String getUserCountry() {
        return userCountry;
    }

    public void setUserCountry(String userCountry) {
        this.userCountry = userCountry;
    }

    public String getUserStatusText() {
        return userStatusText;
    }

    public void setUserStatusText(String userStatusText) {
        this.userStatusText = userStatusText;
    }

    public int getBuys() {
        return buys;
    }

    public void setBuys(int buys) {
        this.buys = buys;
    }

    public int getSells() {
        return sells;
    }

    public void setSells(int sells) {
        this.sells = sells;
    }

    public String getUserPhoneNumber() {
        return userPhoneNumber;
    }

    public void setUserPhoneNumber(String userPhoneNumber) {
        this.userPhoneNumber = userPhoneNumber;
    }

    public long getLastUpdatedTime() {
        return lastUpdatedTime;
    }

    public void setLastUpdatedTime(long lastUpdatedTime) {
        this.lastUpdatedTime = lastUpdatedTime;
    }

    @Override
    public String toString() {
        return "username: "+getUserName()+", email: "+getUserEmail()+", id: "+getUserId()+", Phone: "+getUserPhoneNumber();
    }
}
