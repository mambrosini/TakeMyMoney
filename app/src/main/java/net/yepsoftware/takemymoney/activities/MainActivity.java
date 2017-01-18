package net.yepsoftware.takemymoney.activities;

import android.animation.LayoutTransition;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import net.yepsoftware.takemymoney.R;
import net.yepsoftware.takemymoney.adapters.SearchAdapter;
import net.yepsoftware.takemymoney.helpers.UIUtils;
import net.yepsoftware.takemymoney.model.Article;
import net.yepsoftware.takemymoney.model.SearchQuery;

import java.util.ArrayList;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    private DatabaseReference requestDBRef;
    private DatabaseReference responseDBRef;
    private EditText searchEditText;
    RelativeLayout.LayoutParams searchParams;
    private ImageView imageView;
    RelativeLayout.LayoutParams imageParams;
    private RelativeLayout rootLayout;
    private ListView searchListView;
    private ArrayList<Article> searchedArticles;
    private SearchAdapter searchAdapter;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        rootLayout = (RelativeLayout) findViewById(R.id.rootLayout);
        LayoutTransition layoutTransition = new LayoutTransition();
        layoutTransition.enableTransitionType(LayoutTransition.CHANGING);
        rootLayout.setLayoutTransition(layoutTransition);

        searchListView = (ListView) findViewById(R.id.listView);

        requestDBRef = FirebaseDatabase.getInstance().getReference("search/request");

        imageView = (ImageView) findViewById(R.id.image);
        imageParams = (RelativeLayout.LayoutParams) imageView.getLayoutParams();
        searchEditText = (EditText) findViewById(R.id.search_field);
        searchParams = (RelativeLayout.LayoutParams) searchEditText.getLayoutParams();
        searchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    if (!searchEditText.getText().toString().isEmpty() && searchEditText.getText().toString().split("\\s+").length != 0) {
                        progressDialog = UIUtils.showProgressDialog(MainActivity.this, "Searching...");
                        moveViewToTop();

                        searchedArticles.clear();
                        searchAdapter.notifyDataSetChanged();

                        String key = requestDBRef.push().getKey();
                        requestDBRef.child(key).setValue(new SearchQuery(searchEditText.getText().toString()));
                        responseDBRef = FirebaseDatabase.getInstance().getReference("search/response").child(key);
                        responseDBRef.addChildEventListener(new ChildEventListener() {
                            @Override
                            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                                if (s != null && s.equals("_shards")){
                                    Map<String, Object> shardsMap = (Map<String, Object>) dataSnapshot.getValue();
                                    ArrayList<Map<String, Object>> hitsArrayList = (ArrayList<Map<String, Object>>) shardsMap.get("hits");
                                    for (Map<String, Object> hitMap : hitsArrayList){
                                        Map<String, Object> detailsMap = (Map<String, Object>) hitMap.get("_source");
                                        searchedArticles.add(new Article(detailsMap.get("title").toString(), detailsMap.get("description").toString(), Double.valueOf(String.valueOf(detailsMap.get("price")))));
                                    }
                                    progressDialog.dismiss();
                                    searchAdapter.notifyDataSetChanged();
                                }
                            }

                            @Override
                            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                                Log.d("@@@@@", "onChildChanged");
                            }

                            @Override
                            public void onChildRemoved(DataSnapshot dataSnapshot) {
                                Log.d("@@@@@", "onChildRemoved");
                            }

                            @Override
                            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                                Log.d("@@@@@", "onChildMoved");
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Log.d("@@@@@", "onCancelled");
                            }
                        });
                    }
                    return true;
                }
                return false;
            }
        });

        searchedArticles = new ArrayList<>();
        searchAdapter = new SearchAdapter(getApplicationContext(), searchedArticles);
        searchListView.setAdapter(searchAdapter);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, NewArticleActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void moveViewToTop()
    {
        if (searchParams.equals(searchEditText.getLayoutParams())) {
            RelativeLayout.LayoutParams params2 = new RelativeLayout.LayoutParams(imageView.getWidth() / 4, imageView.getHeight() / 4);
            params2.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            params2.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            params2.setMargins(0, 0, 20, 0);
            imageView.setLayoutParams(params2);

            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(searchEditText.getWidth(), searchEditText.getHeight());
            params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            params.addRule(RelativeLayout.RIGHT_OF, imageView.getId());
            searchEditText.setLayoutParams(params);
            UIUtils.hideKeyboard(MainActivity.this);


            searchListView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onBackPressed() {
        if (searchParams.equals(searchEditText.getLayoutParams())){
            super.onBackPressed();
        } else {
            searchListView.setVisibility(View.GONE);
            searchEditText.setLayoutParams(searchParams);
            imageView.setLayoutParams(imageParams);
        }
    }
}
