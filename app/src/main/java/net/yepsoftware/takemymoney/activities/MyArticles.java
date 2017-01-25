package net.yepsoftware.takemymoney.activities;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import net.yepsoftware.takemymoney.R;
import net.yepsoftware.takemymoney.adapters.ArticleListAdapter;
import net.yepsoftware.takemymoney.helpers.PreferencesHelper;
import net.yepsoftware.takemymoney.helpers.UIUtils;
import net.yepsoftware.takemymoney.model.Article;
import net.yepsoftware.takemymoney.model.SearchQuery;

import java.util.ArrayList;
import java.util.Map;

public class MyArticles extends ChildActivity {

    private ListView listView;
    private ArticleListAdapter articleListAdapter;
    private ArrayList<Article> articles;
    private DatabaseReference requestDBRef;
    private DatabaseReference responseDBRef;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_articles);

        articles = new ArrayList<>();
        listView = (ListView) findViewById(R.id.listView);
        articleListAdapter = new ArticleListAdapter(getApplicationContext(), articles);
        listView.setAdapter(articleListAdapter);

        progressDialog = UIUtils.showProgressDialog(MyArticles.this, "Retrieving posts...");

        requestDBRef = FirebaseDatabase.getInstance().getReference("search/request");
        responseDBRef = FirebaseDatabase.getInstance().getReference("search/response");

        String key = requestDBRef.push().getKey();
        requestDBRef.child(key).setValue(new SearchQuery().searchByUser(PreferencesHelper.getUserId(getApplicationContext())));
        responseDBRef = FirebaseDatabase.getInstance().getReference("search/response").child(key);
        responseDBRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (s != null && s.equals("_shards")){
                    Map<String, Object> shardsMap = (Map<String, Object>) dataSnapshot.getValue();
                    ArrayList<Map<String, Object>> hitsArrayList = (ArrayList<Map<String, Object>>) shardsMap.get("hits");
                    if (hitsArrayList != null && hitsArrayList.size() > 0) {
                        for (Map<String, Object> hitMap : hitsArrayList) {
                            Map<String, Object> detailsMap = (Map<String, Object>) hitMap.get("_source");
                            articles.add(new Article(detailsMap.get("uid").toString(), detailsMap.get("title").toString(), detailsMap.get("description").toString(), Double.valueOf(String.valueOf(detailsMap.get("price")))));
                        }
                    } else {
                        articles.add(new Article("", "You don't have any articles posted...", "", 0.0));
                    }
                    progressDialog.dismiss();
                    articleListAdapter.notifyDataSetChanged();
                }
            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {}
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {}
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {}
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });

    }
}
