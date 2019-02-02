package com.example.poker;

public class Users {

    String userId;
    String userName;
    //String profileImage;

    public Users() {

    }

    public Users(String userId, String userName) {
        this.userId = userId;
        this.userName = userName;
        // this.profileImage = profileImage;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserName() {
        return userName;
    }


}
