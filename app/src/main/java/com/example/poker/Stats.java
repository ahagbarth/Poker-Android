package com.example.poker;

public class Stats {
    String userId;
    String userName;
    int userLevel;
    int userEXP;
    int userWins;
    int userLoss;
    int userBalance;
    int userBet;

    public Stats(){

    }

    public Stats(String userId, String userName, int userLevel, int userEXP, int userWins, int userLoss, int userBalance, int userBet) {
        this.userId = userId;
        this.userName = userName;
        this.userLevel = userLevel;
        this.userEXP = userEXP;
        this.userWins = userWins;
        this.userLoss = userLoss;
        this.userBalance = userBalance;
        this.userBet = userBet;
    }


    public int getUserBalance() {
        return userBalance;
    }

    public void setUserBalance(int userBalance) {
        this.userBalance = userBalance;
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

    public int getUserLevel() {
        return userLevel;
    }

    public void setUserLevel(int userLevel) {
        this.userLevel = userLevel;
    }

    public int getUserEXP() {
        return userEXP;
    }

    public void setUserEXP(int userEXP) {
        this.userEXP = userEXP;
    }

    public int getUserWins() {
        return userWins;
    }

    public void setUserWins(int userWins) {
        this.userWins = userWins;
    }

    public int getUserLoss() {
        return userLoss;
    }

    public void setUserLoss(int userLoss) {
        this.userLoss = userLoss;
    }

    public int getUserBet() {
        return userBet;
    }

    public void setUserBet(int userBet) {
        this.userBet = userBet;
    }
}
