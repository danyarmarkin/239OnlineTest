package com.onlinetest.android.a239onlinetest;

public class Question {
    private String mQuestionText;
    private int mQuestionNumber;
    private long mQuestionId;

    Question(){

    }

    public String getQuestionText() {
        return mQuestionText;
    }

    public void setQuestionText(String questionText) {
        mQuestionText = questionText;
    }

    public int getQuestionNumber() {
        return mQuestionNumber;
    }

    public void setQuestionNumber(int questionNumber) {
        mQuestionNumber = questionNumber;
    }

    public long getQuestionId() {
        return mQuestionId;
    }

    public void setQuestionId(long questionId) {
        mQuestionId = questionId;
    }
}
