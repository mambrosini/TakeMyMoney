package net.yepsoftware.takemymoney.activities;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
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
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import net.yepsoftware.takemymoney.R;
import net.yepsoftware.takemymoney.helpers.UIUtils;
import net.yepsoftware.takemymoney.model.Article;
import net.yepsoftware.takemymoney.model.User;

import java.util.ArrayList;

public class ArticleDetailActivity extends ChildActivity {

    private DatabaseReference usersDBRef;
    private Article article;
    private ProgressDialog progressDialog;
    private ImageView imageView1;
    private ImageView imageView2;
    private ImageView imageView3;
    private View overlay1;
    private View overlay2;
    private View overlay3;
    private boolean fromMyArticles;

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

        fromMyArticles = getIntent().getBooleanExtra("FROM_MY_ARTICLES", false);

        TextView title = (TextView) findViewById(R.id.title);
        TextView description = (TextView) findViewById(R.id.description);
        TextView price = (TextView) findViewById(R.id.price);
        imageView1 = (ImageView) findViewById(R.id.image1);
        imageView2 = (ImageView) findViewById(R.id.image2);
        imageView3 = (ImageView) findViewById(R.id.image3);
        overlay1 = findViewById(R.id.overlay1);
        overlay2 = findViewById(R.id.overlay2);
        overlay3 = findViewById(R.id.overlay3);

        title.setText(article.title);
        description.setText(article.description);
        price.setText("$" + String.valueOf(article.price));

        if (!article.images.isEmpty()){
            findViewById(R.id.imageLayout).setVisibility(View.VISIBLE);
            for (int i = 0; i < article.images.size(); i++) {
                switch (i){
                    case 0:
                        overlay1.setVisibility(View.VISIBLE);
                        Picasso.with(ArticleDetailActivity.this).load(article.images.get(i)).into(imageView1, new Callback() {
                            @Override
                            public void onSuccess() {
                                overlay1.setVisibility(View.GONE);
                            }
                            @Override
                            public void onError() {}
                        });
                        break;
                    case 1:
                        overlay2.setVisibility(View.VISIBLE);
                        Picasso.with(ArticleDetailActivity.this).load(article.images.get(i)).into(imageView2, new Callback() {
                            @Override
                            public void onSuccess() {
                                overlay2.setVisibility(View.GONE);
                            }
                            @Override
                            public void onError() {}
                        });
                        break;
                    case 2:
                        overlay3.setVisibility(View.VISIBLE);
                        Picasso.with(ArticleDetailActivity.this).load(article.images.get(i)).into(imageView3, new Callback() {
                            @Override
                            public void onSuccess() {
                                overlay3.setVisibility(View.GONE);
                            }
                            @Override
                            public void onError() {}
                        });
                        break;
                }
            }
        }

        if (fromMyArticles){
            findViewById(R.id.button).setVisibility(View.GONE);
        }

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
