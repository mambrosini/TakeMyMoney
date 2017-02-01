package net.yepsoftware.takemymoney.activities;

import android.provider.Contacts;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

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
