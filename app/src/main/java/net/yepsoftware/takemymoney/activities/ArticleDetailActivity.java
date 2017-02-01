package net.yepsoftware.takemymoney.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import net.yepsoftware.takemymoney.R;
import net.yepsoftware.takemymoney.helpers.PreferencesHelper;
import net.yepsoftware.takemymoney.helpers.UIUtils;
import net.yepsoftware.takemymoney.model.Article;
import net.yepsoftware.takemymoney.model.User;

public class ArticleDetailActivity extends ChildActivity {

    private DatabaseReference usersDBRef;
    private Article article;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_detail);

        article = new Article(getIntent().getStringExtra("uid"),
                getIntent().getStringExtra("title"),
                getIntent().getStringExtra("description"),
                getIntent().getDoubleExtra("price",-1),
                Article.State.ACTIVE);

        TextView title = (TextView) findViewById(R.id.title);
        TextView description = (TextView) findViewById(R.id.description);
        TextView price = (TextView) findViewById(R.id.price);

        title.setText(article.title);
        description.setText(article.description);
        price.setText("$" + String.valueOf(article.price));
    }

    public void getInfo(View v){
        progressDialog = UIUtils.showProgressDialog(ArticleDetailActivity.this, "Getting info...");
        usersDBRef = FirebaseDatabase.getInstance().getReference().child("users").child(article.uid);
        usersDBRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                progressDialog.dismiss();

                User user = new User("", String.valueOf(dataSnapshot.child("email").getValue(String.class)),
                        String.valueOf(dataSnapshot.child("secondayEmail").getValue(String.class)),
                        String.valueOf(dataSnapshot.child("phone").getValue(String.class)));
                UIUtils.showContactInfoDialog(ArticleDetailActivity.this, user);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
