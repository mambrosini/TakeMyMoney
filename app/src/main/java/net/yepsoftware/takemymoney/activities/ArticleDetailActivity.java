package net.yepsoftware.takemymoney.activities;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
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
import net.yepsoftware.takemymoney.services.ImageDownloadService;

import java.util.ArrayList;

public class ArticleDetailActivity extends ChildActivity {

    private DatabaseReference usersDBRef;
    private DatabaseReference articlesDBRef;
    private Article article;
    private ProgressDialog progressDialog;
    private TextView stateTextView;
    private ImageView imageView1;
    private ImageView imageView2;
    private ImageView imageView3;
    private View overlay1;
    private View overlay2;
    private View overlay3;
    private boolean fromMyArticles;

    Menu menu;

    private static final int MENU_SOLD = 0;
    private static final int MENU_DISABLE = 1;
    private static final int MENU_ACTIVATE = 2;

    private BroadcastReceiver broadcastReceiver;

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
                Article.stringToState(getIntent().getStringExtra("state")));

        fromMyArticles = getIntent().getBooleanExtra("FROM_MY_ARTICLES", false);

        TextView title = (TextView) findViewById(R.id.title);
        TextView description = (TextView) findViewById(R.id.description);
        TextView price = (TextView) findViewById(R.id.price);
        stateTextView = (TextView) findViewById(R.id.state);
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
//                        Picasso.with(ArticleDetailActivity.this).load(article.images.get(i)).into(imageView1, new Callback() {
//                            @Override
//                            public void onSuccess() {
//                                overlay1.setVisibility(View.GONE);
//                            }
//                            @Override
//                            public void onError() {}
//                        });
                        break;
                    case 1:
                        overlay2.setVisibility(View.VISIBLE);
//                        Picasso.with(ArticleDetailActivity.this).load(article.images.get(i)).into(imageView2, new Callback() {
//                            @Override
//                            public void onSuccess() {
//                                overlay2.setVisibility(View.GONE);
//                            }
//                            @Override
//                            public void onError() {}
//                        });
                        break;
                    case 2:
                        overlay3.setVisibility(View.VISIBLE);
//                        Picasso.with(ArticleDetailActivity.this).load(article.images.get(i)).into(imageView3, new Callback() {
//                            @Override
//                            public void onSuccess() {
//                                overlay3.setVisibility(View.GONE);
//                            }
//                            @Override
//                            public void onError() {}
//                        });
                        break;
                }

                Intent startIntent = new Intent(ArticleDetailActivity.this,
                        ImageDownloadService.class);
                startIntent.putExtra("url",article.images.get(i));
                startIntent.putExtra("imageNumber", i);
                startService(startIntent);
            }
        }

        if (fromMyArticles){
            findViewById(R.id.button).setVisibility(View.GONE);
            updateStateText();
            articlesDBRef = FirebaseDatabase.getInstance().getReference().child("articles").child(getIntent().getStringExtra("articleKey"));
        } else {
            stateTextView.setVisibility(View.GONE);
        }

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int result = intent.getIntExtra(ImageDownloadService.MESSAGE, 0);
                String filePath = intent.getStringExtra(ImageDownloadService.FILEPATH);
                switch (result) {
                    case ImageDownloadService.DOWNLOAD_ERROR:
                        Toast.makeText(getApplicationContext(), "Error",
                                Toast.LENGTH_SHORT).show();
                        break;

                    case ImageDownloadService.DOWNLOAD_SUCCESS:
                        Bitmap bmp = BitmapFactory.decodeFile(filePath);
                        if (bmp != null) {
                            switch (intent.getIntExtra(ImageDownloadService.IMAGE_NUMBER, -1)){
                                case 0:
                                    setImage(bmp, imageView1, overlay1, filePath);
                                    break;
                                case 1:
                                    setImage(bmp, imageView2, overlay2, filePath);
                                    break;
                                case 2:
                                    setImage(bmp, imageView3, overlay3, filePath);
                                    break;
                            }
                            Toast.makeText(getApplicationContext(),
                                    "Download finished",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplicationContext(),
                                    "Error",
                                    Toast.LENGTH_SHORT).show();
                        }
                        break;
                }
            }
        };

        LocalBroadcastManager.getInstance(this).registerReceiver((broadcastReceiver), new IntentFilter(ImageDownloadService.IMAGE_DOWLOADED_BROADCAST));

    }

    private void setImage(final Bitmap bitmap, ImageView imageView, View overlay, final String filePath){
        overlay.setVisibility(View.GONE);
        imageView.setImageBitmap(bitmap);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse(filePath), "image/*");
                startActivity(intent);
            }
        });
    }

    private void updateStateText(){
        stateTextView.setText(article.state.toString());
        switch (article.state){
            case ACTIVE:
                stateTextView.setTextColor(ContextCompat.getColor(ArticleDetailActivity.this, R.color.articleActive));
                break;
            case DISABLED:
                stateTextView.setTextColor(ContextCompat.getColor(ArticleDetailActivity.this, R.color.articleDisabled));
                break;
            case SOLD:
                stateTextView.setTextColor(ContextCompat.getColor(ArticleDetailActivity.this, R.color.articleSold));
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (fromMyArticles){
            this.menu = menu;
            if (article.state.equals(Article.State.ACTIVE)){
                menu.add(0,MENU_SOLD,0,"Sold");
                menu.add(0,MENU_DISABLE,0,"Disable");
            } else if (article.state.equals(Article.State.DISABLED)){
                menu.add(0,MENU_ACTIVATE,0, "Activate");
            } else if (article.state.equals(Article.State.SOLD)){
                menu.add(0,MENU_ACTIVATE,0, "Republish");
            }
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case MENU_SOLD:
                article.state = Article.State.SOLD;
                articlesDBRef.setValue(article);
                menu.clear();
                menu.add(0,MENU_ACTIVATE,0, "Republish");
                break;
            case MENU_DISABLE:
                article.state = Article.State.DISABLED;
                articlesDBRef.setValue(article);
                menu.clear();
                menu.add(0,MENU_ACTIVATE,0, "Activate");
                break;
            case MENU_ACTIVATE:
                article.state = Article.State.ACTIVE;
                articlesDBRef.setValue(article);
                menu.clear();
                menu.add(0,MENU_SOLD,0,"Sold");
                menu.add(0,MENU_DISABLE,0,"Disable");
                break;
        }
        updateStateText();
        return super.onOptionsItemSelected(item);
    }

    public void getInfo(View v){
        progressDialog = UIUtils.showProgressDialog(ArticleDetailActivity.this, "Getting info...");
        usersDBRef = FirebaseDatabase.getInstance().getReference().child("users").child(article.uid);
        usersDBRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                progressDialog.dismiss();

                User user = new User("", String.valueOf(dataSnapshot.child("email").getValue(String.class)),
                        String.valueOf(dataSnapshot.child("secondaryEmail").getValue(String.class)),
                        String.valueOf(dataSnapshot.child("phone").getValue(String.class)));
                UIUtils.showContactInfoDialog(ArticleDetailActivity.this, user, article.title);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
