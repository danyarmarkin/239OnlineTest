package com.onlinetest.android.a239onlinetest;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.net.ConnectException;
import java.util.Arrays;
import java.util.List;

public class MenuActivity extends AppCompatActivity {

    private EditText mName, mSurname, mTestName;
    private Button mGoToTest;
    public static User mUser;
    private String TAG = "MenuActivity";
    private FirebaseUser mFirebaseUser;
    private String mUserId;
    private Button mExitButton;

    private FirebaseAuth mAuth;

    private static final int RC_SIGN_IN = 228;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        new AuthActivity();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        mName = findViewById(R.id.user_name);
        mSurname = findViewById(R.id.user_surname);
        mTestName = findViewById(R.id.test_name);

        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (mFirebaseUser != null) {
            mUserId = mFirebaseUser.getUid();
            pullFromDatabase("users/"+mUserId);
        }

        mGoToTest = findViewById(R.id.go_to_test_button);
        mGoToTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mName.getText() != null && mSurname.getText() != null && mTestName != null){

                    mUser.setName(mName.getText().toString());
                    mUser.setSurname(mSurname.getText().toString());
                    Intent i = new Intent(MenuActivity.this, MainActivity.class);
                    String testName = mTestName.getText().toString();
                    i.putExtra(MainActivity.EXTRA_TEST_NAME, testName);

                    startActivity(i);
                    Log.d(TAG, mUser.getUserID());
                }
            }
        });

        mExitButton = findViewById(R.id.exit_button);
        mExitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();

                //authUserInit();

                mAuth = FirebaseAuth.getInstance();


            }
        });


        authUserInit();

        mAuth = FirebaseAuth.getInstance();


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                Log.d(TAG, "user authed");
                // ...
            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                // ...
                Log.e(TAG, "user not authed! Error code: " + response.getError().getErrorCode());
            }
        }
    }




    protected void authUserInit(){
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.GoogleBuilder().build());

        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build(),
                RC_SIGN_IN);
    }
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    private void updateUI(FirebaseUser user) {
        // Some code... \_(*-*)_/
    }

    public void pullFromDatabase(final String res) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference(res);

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                try {
                    String value = dataSnapshot.getValue(String.class).toString();
                    Log.d(TAG, "Value is: " + value);
                    if(value != null){
                        mUser = new User(value.split("#")[0], value.split("#")[1], mUserId);
                        mName.setText(mUser.getName());
                        mSurname.setText(mUser.getSurname());
                    } else{
                        mUser = new User("&&", "&&", mUserId);
                    }
                }catch (NullPointerException e){
                    e.getStackTrace();
                    mUser = new User("&&", "&&", mUserId);
                }

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }
}
