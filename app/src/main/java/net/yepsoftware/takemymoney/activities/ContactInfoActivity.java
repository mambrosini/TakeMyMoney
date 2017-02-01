package net.yepsoftware.takemymoney.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.provider.Contacts;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import net.yepsoftware.takemymoney.R;
import net.yepsoftware.takemymoney.helpers.PreferencesHelper;
import net.yepsoftware.takemymoney.helpers.UIUtils;
import net.yepsoftware.takemymoney.model.User;

public class ContactInfoActivity extends ChildActivity {

    private TextInputEditText email;
    private TextInputEditText secondaryEmail;
    private TextInputEditText phone;
    private Button button;

    private boolean needToSave;

    private DatabaseReference usersDBRef;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_info);

        needToSave = false;

        usersDBRef = FirebaseDatabase.getInstance().getReference().child("users");

        email = (TextInputEditText) findViewById(R.id.email);
        secondaryEmail = (TextInputEditText) findViewById(R.id.secondaryMail);
        phone = (TextInputEditText) findViewById(R.id.phone);
        button = (Button) findViewById(R.id.button);

        email.setText(PreferencesHelper.getMail(getApplicationContext()));
        secondaryEmail.setText(PreferencesHelper.getSecondaryMail(getApplicationContext()));
        phone.setText(PreferencesHelper.getPhone(getApplicationContext()));

        addTextWatcher(email);
        addTextWatcher(secondaryEmail);
        addTextWatcher(phone);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UIUtils.hideKeyboard(ContactInfoActivity.this);
                if (needToSave){
                    String key = PreferencesHelper.getUserId(getApplicationContext());
                    User dbUser = new User(key, email.getText().toString(), secondaryEmail.getText().toString(), phone.getText().toString());
                    usersDBRef.child(key).setValue(dbUser);
                    PreferencesHelper.savePhone(getApplicationContext(), phone.getText().toString());
                    PreferencesHelper.saveSecondaryMail(getApplicationContext(), secondaryEmail.getText().toString());
                }
                finish();
            }
        });

        progressDialog = UIUtils.showProgressDialog(ContactInfoActivity.this, "Getting info...");
        usersDBRef = FirebaseDatabase.getInstance().getReference().child("users").child(PreferencesHelper.getUserId(ContactInfoActivity.this));
        usersDBRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                PreferencesHelper.savePhone(getApplicationContext(), String.valueOf(dataSnapshot.child("phone").getValue(String.class)));
                PreferencesHelper.saveSecondaryMail(getApplicationContext(), dataSnapshot.child("secondaryEmail").getValue(String.class));
                email.setText(PreferencesHelper.getMail(getApplicationContext()));
                secondaryEmail.setText(PreferencesHelper.getSecondaryMail(getApplicationContext()));
                phone.setText(PreferencesHelper.getPhone(getApplicationContext()));
                progressDialog.dismiss();
                usersDBRef = FirebaseDatabase.getInstance().getReference().child("users");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void addTextWatcher(TextView textView){
        textView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                needToSave = true;
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
}
