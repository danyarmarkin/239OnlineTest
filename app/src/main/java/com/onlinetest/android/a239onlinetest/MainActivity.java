package com.onlinetest.android.a239onlinetest;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    private String TAG = "MainActivity.java";

    private EditText answer;
    private Button enter;
    private TextView question;
    private int mCurrentIndex = 0;
    private String mResAnswer = "answer1";
    private String mResQuestion = "task1/task";
    private WebView mWebView;
    private String mTestName;
    private TextView mQuestionNumber;
    private ImageButton mNextButton, mPreviousButton;

    private ArrayList<Question> mQuestionBank;

    private User mUser = MenuActivity.mUser;

    private Question question1;
    public static final String EXTRA_TEST_NAME = "com.239onlinetesting.android.239online.test_name";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Bundle bundle = getIntent().getExtras();
        mTestName = bundle.getString(EXTRA_TEST_NAME);
        mResQuestion = mTestName + "/task";
        mResAnswer = mTestName + "/users_answers/"+mUser.getUserID()+"_";

        mQuestionBank = new ArrayList<Question>();

        mWebView = findViewById(R.id.question_web_view);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setLoadWithOverviewMode(true);
        mWebView.getSettings().setBuiltInZoomControls(true);
        mWebView.loadUrl("file:///android_asset/MainJax/main.html");
        mQuestionNumber = findViewById(R.id.question_number);
        mPreviousButton = findViewById(R.id.previous_button);
        mNextButton = findViewById(R.id.next_button);

        answer = findViewById(R.id.answer);
        enter = findViewById(R.id.enter);
        question = findViewById(R.id.question);


        enter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (answer.getText() != null) {
                    pushToDatabase(mResAnswer + mQuestionNumber, answer.getText().toString());
                } else {
                    Toast toast = Toast.makeText(getApplicationContext(), "error", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });


        pullFromDatabase(mTestName + "/t");

        initUserToDatabase(mTestName + "_" + mUser.getUserID(), mUser.getSurname() + " " + mUser.getName());
        initUserToDatabase(mUser.getUserID(), mUser.getName() + "#" + mUser.getSurname());

        mPreviousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mCurrentIndex > 0){
                    mCurrentIndex= (mCurrentIndex-1)%mQuestionBank.size();
                    showNextQuestion(mQuestionBank.get(mCurrentIndex));
                } else {
                    mCurrentIndex = mQuestionBank.size()-1;
                    showNextQuestion(mQuestionBank.get(mCurrentIndex));
                }
            }
        });

        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCurrentIndex < mQuestionBank.size()){
                    mCurrentIndex= (mCurrentIndex+1)%mQuestionBank.size();
                    showNextQuestion(mQuestionBank.get(mCurrentIndex));
                }
            }
        });


    }

    private void showNextQuestion(Question question){
        answer.setText("");
        Log.d(TAG, "showNextQuestion: mCurrentIndex = " + mCurrentIndex);
        for (int i = 0; i <mQuestionBank.size(); i++){
            Log.d(TAG, "showNextQuestion: "+ mQuestionBank.get(i).getQuestionText());
        }
        try{
            Log.d(TAG + " questionText", question.getQuestionText());
            Log.d(TAG, "showNextQuestion: questionNumber = " + question.getQuestionNumber());
            showQuestionOnWebView(question.getQuestionText());
            mQuestionNumber.setText(String.valueOf(question.getQuestionNumber()));
        }catch (NullPointerException e){
            Log.e(TAG + " questionText", "Question have no text");
        }
    }

    public void pushToDatabase(String res, String val) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        DatabaseReference myRef = database.getReference(res);
        myRef.setValue(val);
        myRef.setValue("$"+val+"$");

    }

    public void initUserToDatabase(String res, String val){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference dbRef;
        Log.d(TAG, res);
            if(res == mUser.getUserID()){
                dbRef = database.getInstance().getReference("users/"+res);
                dbRef.setValue(val);
            }else{
                dbRef = database.getInstance().getReference();
                dbRef.child(mTestName).child("users").child(mUser.getUserID()).setValue(val);
            }
    }


    public void pullFromDatabase(String res) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference(res);

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                int value = dataSnapshot.getValue(Integer.class);
                Log.d(TAG, "Value is: " + value);
                for (int i = 0; i <= value; i++){
                    if (i != value){
                        mQuestionBank.add(new Question(pullQuestionsFromDatabase(mTestName+"/tasks/task"+(i+1), i), i+1));
                    } else {
                       // showNextQuestion(mQuestionBank[mCurrentIndex]);
                    }

                }
                answer.setText(null);

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }

    public String pullQuestionsFromDatabase(String res, final int n) {
        final String[] value = new String[1];
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference(res);

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                value[0] = dataSnapshot.getValue(String.class).toString();
                Log.d(TAG, "Value is: " + value[0]);
                mQuestionBank.get(n).setQuestionText(value[0]);
                if (n == 0){
                    showNextQuestion(mQuestionBank.get(0));
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
        return value[0];
    }

    private void showQuestionOnWebView(String Latex){

        mWebView.loadUrl("javascript:document.getElementById('latex').innerHTML='"
                +doubleEscapeTeX(Latex)+"';" +
                "javascript:MathJax.Hub.Queue(['Typeset',MathJax.Hub]);");
//        mWebView.loadUrl("javascript:MathJax.Hub.Queue(['Typeset',MathJax.Hub]);");

    }

    private String doubleEscapeTeX(String s) {
        String t="";
        for (int i=0; i < s.length(); i++) {
            if (s.charAt(i) == '\'') t += '\\';
            if (s.charAt(i) != '\n') t += s.charAt(i);
            if (s.charAt(i) == '\\') t += "\\";
        }
        return t;
    }

}

