package net.yepsoftware.takemymoney.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import net.yepsoftware.takemymoney.R;
import net.yepsoftware.takemymoney.helpers.PreferencesHelper;
import net.yepsoftware.takemymoney.helpers.UIUtils;

public class AuthenticationActivity extends ChildActivity {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private ProgressDialog progressDialog;

    TextInputEditText email;
    TextInputEditText password;
    CheckBox checkBox;

    boolean navigateToNewArticle;
    boolean linkDevice;

    private DatabaseReference usersDBRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);

        usersDBRef = FirebaseDatabase.getInstance().getReference().child("users");

        navigateToNewArticle = getIntent().getBooleanExtra(RegistrationActivity.NAVIGATE_TO_NEW_ARTICLE, false);
        linkDevice = false;

        email = (TextInputEditText) findViewById(R.id.email);
        checkBox = (CheckBox) findViewById(R.id.checkbox);
        password = (TextInputEditText) findViewById(R.id.password);

        email.setText(PreferencesHelper.getMail(getApplicationContext()));

        checkBox.setChecked(PreferencesHelper.isAutoLogin(getApplicationContext()));

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Log.d("FirebaseAuth", "User signed in.");
                    PreferencesHelper.setAutoLogin(getApplicationContext(), checkBox.isChecked());
                    if (PreferencesHelper.getAppState(getApplicationContext()).equals(PreferencesHelper.APP_STATE_UNREGISTERED)){
                        PreferencesHelper.saveUserId(getApplicationContext(), user.getUid());
                        PreferencesHelper.saveMail(getApplicationContext(), email.getText().toString());
                        PreferencesHelper.savePassword(getApplicationContext(), password.getText().toString());
                        usersDBRef = FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid());
                        usersDBRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                PreferencesHelper.savePhone(getApplicationContext(), String.valueOf(dataSnapshot.child("phone").getValue(String.class)));
                                PreferencesHelper.saveSecondaryMail(getApplicationContext(), dataSnapshot.child("secondaryEmail").getValue(String.class));

                                progressDialog.dismiss();
                                finish();
                                if (navigateToNewArticle){
                                    Intent intent = new Intent(AuthenticationActivity.this, NewArticleActivity.class);
                                    startActivity(intent);
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                    PreferencesHelper.setAppState(getApplicationContext(), PreferencesHelper.APP_STATE_AUTHENTICATED);
                } else {
                    Log.d("FirebaseAuth", "User signed out.");
                }
            }
        };
    }

    public void signInWithMailAndPassword(View v){
        UIUtils.hideKeyboard(AuthenticationActivity.this);
        email.clearFocus();
        password.clearFocus();
        if (!email.getText().toString().equals("") &&
                !password.getText().toString().equals("")) {
            progressDialog = UIUtils.showProgressDialog(AuthenticationActivity.this, "Signin in...");
            mAuth.signInWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            Log.d("AuthenticationActivity", "createUserWithEmail:onComplete:" + task.isSuccessful());
                            if (!task.isSuccessful()) {
                                Toast.makeText(AuthenticationActivity.this, "Sign in failed!",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } else {
            Toast.makeText(getApplicationContext(),"You must fill all the fields.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
}
