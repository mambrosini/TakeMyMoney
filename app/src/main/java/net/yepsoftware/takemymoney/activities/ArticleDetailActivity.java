package net.yepsoftware.takemymoney.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import net.yepsoftware.takemymoney.R;
import net.yepsoftware.takemymoney.adapters.ImageGridViewAdapter;
import net.yepsoftware.takemymoney.helpers.PreferencesHelper;
import net.yepsoftware.takemymoney.helpers.UIUtils;
import net.yepsoftware.takemymoney.model.Article;
import net.yepsoftware.takemymoney.model.User;

import java.util.ArrayList;

public class ArticleDetailActivity extends ChildActivity {

    private DatabaseReference usersDBRef;
    private Article article;
    private ProgressDialog progressDialog;
    private GridView gridView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_detail);

        ArrayList<String> images =  getIntent().getStringArrayListExtra("images");

        article = new Article(getIntent().getStringExtra("uid"),
                getIntent().getStringExtra("title"),
                getIntent().getStringExtra("description"),
                getIntent().getDoubleExtra("price",-1),
                images,
                Article.State.ACTIVE);

        TextView title = (TextView) findViewById(R.id.title);
        TextView description = (TextView) findViewById(R.id.description);
        TextView price = (TextView) findViewById(R.id.price);
        gridView = (GridView) findViewById(R.id.imageGridView);
        ImageGridViewAdapter imageGridViewAdapter = new ImageGridViewAdapter(getApplicationContext(), images);
        gridView.setAdapter(imageGridViewAdapter);

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
                UIUtils.showContactInfoDialog(ArticleDetailActivity.this, user, article.title);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
