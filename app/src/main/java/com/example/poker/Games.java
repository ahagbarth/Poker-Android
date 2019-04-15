package com.example.poker;

public class Games {

    String gameName;
    int numUsers;


    public Games() {

    }

    public Games(String gameName, int numUsers) {
        this.gameName = gameName;
        this.numUsers = numUsers;
    }

    public String getGameName() {
        return gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public int getNumUsers() {
        return numUsers;
    }

    public void setNumUsers(int numUsers) {
        this.numUsers = numUsers;
    }
}
