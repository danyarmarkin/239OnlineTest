package com.onlinetest.android.a239onlinetest;


public class User {
    private String mName, mSurname, mUserID;
    private String TAG = "User";


    User(String name, String surname, String userID){
        mName = name;
        mSurname = surname;
        mUserID = userID;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getSurname() {
        return mSurname;
    }

    public void setSurname(String surname) {
        mSurname = surname;
    }

    public String getUserID() {
        return mUserID;
    }


}
