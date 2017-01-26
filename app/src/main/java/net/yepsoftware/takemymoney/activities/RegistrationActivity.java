package net.yepsoftware.takemymoney.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.os.Bundle;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import net.yepsoftware.takemymoney.R;
import net.yepsoftware.takemymoney.helpers.PreferencesHelper;
import net.yepsoftware.takemymoney.helpers.UIUtils;
import net.yepsoftware.takemymoney.model.User;

public class RegistrationActivity extends ChildActivity {

    public static final String NAVIGATE_TO_NEW_ARTICLE = "navigateToNewArticle";

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private TextInputEditText email;
    private TextInputEditText secondaryEmail;
    private TextInputEditText phone;
    private TextInputEditText password;
    private CheckBox checkBox;

    private ProgressDialog progressDialog;

    private DatabaseReference usersDBRef;

    boolean navigateToNewArticle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        usersDBRef = FirebaseDatabase.getInstance().getReference().child("users");

        email = (TextInputEditText) findViewById(R.id.email);
        secondaryEmail = (TextInputEditText) findViewById(R.id.secondaryMail);
        phone = (TextInputEditText) findViewById(R.id.phone);
        password = (TextInputEditText) findViewById(R.id.password);
        checkBox = (CheckBox) findViewById(R.id.checkbox);

        navigateToNewArticle = getIntent().getBooleanExtra(NAVIGATE_TO_NEW_ARTICLE, false);

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    if (progressDialog!= null){
                        progressDialog.dismiss();
                    }
                    // User is signed in
                    Log.d("", "onAuthStateChanged:signed_in:" + user.getUid());
                    PreferencesHelper.setAppState(getApplicationContext(), PreferencesHelper.APP_STATE_AUTHENTICATED);

                    String key = user.getUid();
                    User dbUser = new User(key, email.getText().toString(), secondaryEmail.getText().toString(), phone.getText().toString());
                    usersDBRef.child(key).setValue(dbUser);

                    PreferencesHelper.saveUserId(getApplicationContext(), key);

                    finish();
                    if (navigateToNewArticle){
                        Intent intent = new Intent(RegistrationActivity.this, NewArticleActivity.class);
                        startActivity(intent);
                    }
                } else {
                    // User is signed out
                    Log.d("", "onAuthStateChanged:signed_out");
                }
            }
        };
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

    public void registerWithMailAndPassword(View v){
        UIUtils.hideKeyboard(RegistrationActivity.this);
        if (!email.getText().toString().equals("") &&
                !password.getText().toString().equals("")) {
            progressDialog = UIUtils.showProgressDialog(RegistrationActivity.this, "Registering user...");
            PreferencesHelper.saveMail(getApplicationContext(), email.getText().toString());
            if (checkBox.isChecked()){
                PreferencesHelper.savePassword(getApplicationContext(), password.getText().toString());
                PreferencesHelper.setAutoLogin(getApplicationContext(), true);
            } else {
                PreferencesHelper.setAutoLogin(getApplicationContext(), false);
            }

            mAuth.createUserWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            Log.d("AuthenticationActivity", "createUserWithEmail:onComplete:" + task.isSuccessful());
                            if (!task.isSuccessful()) {
                                Toast.makeText(RegistrationActivity.this, "Registration failed",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } else {
            Toast.makeText(getApplicationContext(),"You must fill all the fields.", Toast.LENGTH_SHORT).show();
        }
    }
}
