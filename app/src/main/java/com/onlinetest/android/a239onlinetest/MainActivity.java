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

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    private String TAG = "MainActivity.java";

    private EditText answer;
    private Button enter;
    private TextView question;
    private int mQuestionNumber = 0;
    private String mResAnswer = "answer1";
    private String mResQuestion = "task1/task";
    private WebView mWebView;
    private String mTestName;

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
        mQuestionNumber = 0;



        mWebView = findViewById(R.id.question_web_view);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setLoadWithOverviewMode(true);
        mWebView.getSettings().setBuiltInZoomControls(true);
//        mWebView.loadDataWithBaseURL("http://bar", "<script type='text/x-mathjax-config'>"
//                +"MathJax.Hub.Config({ "
//                +"showMathMenu: false, "
//                +"jax: ['input/TeX','output/HTML-CSS'], "
//                +"extensions: ['tex2jax.js'], "
//                +"TeX: { extensions: ['AMSmath.js','AMSsymbols.js',"
//                +"'noErrors.js','noUndefined.js'] } "
//                +"});</script>"
//                +"<script type='text/javascript' "
//                +"src='src='file:///android_asset/MathJax/MathJax.js'"
//                +"></script><span id='math'></span>","text/html","utf-8","");
        mWebView.loadUrl("file:///android_asset/MainJax/main.html");



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


        pullFromDatabase(mResQuestion);
        question1 = new Question();
        question.setText(question1.getQuestionText());

        initUserToDatabase(mTestName + "_" + mUser.getUserID(), mUser.getSurname() + " " + mUser.getName());
        initUserToDatabase(mUser.getUserID(), mUser.getName() + "#" + mUser.getSurname());



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
                String value = dataSnapshot.getValue(String.class).toString();
                Log.d(TAG, "Value is: " + value);
                mQuestionNumber = Integer.parseInt(value.split("#")[0]);
                question1.setQuestionText(value.replace(mQuestionNumber+"#", ""));
                question.setText(question1.getQuestionText());
                showQuestionOnWebView(value.replace(mQuestionNumber+"#", ""));
                answer.setText(null);

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
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

