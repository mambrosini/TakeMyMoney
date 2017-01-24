package net.yepsoftware.takemymoney.activities;

import android.animation.LayoutTransition;
import android.content.Context;
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
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import net.yepsoftware.takemymoney.R;
import net.yepsoftware.takemymoney.adapters.SearchAdapter;
import net.yepsoftware.takemymoney.helpers.PreferencesHelper;
import net.yepsoftware.takemymoney.model.Article;
import net.yepsoftware.takemymoney.model.SearchQuery;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    private DatabaseReference requestDBRef;
    private DatabaseReference responseDBRef;
    private EditText searchEditText;
    private ImageView imageView;
    private RelativeLayout rootLayout;
    private LinearLayout searchLayout;
    private ViewGroup.LayoutParams searchParams;
    private ListView searchListView;
    private ArrayList<Article> searchedArticles;
    private SearchAdapter searchAdapter;
    private ProgressBar progressBar;

    private Animation fadeOut;
    private Animation fadeIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        searchListView = (ListView) findViewById(R.id.listView);

        requestDBRef = FirebaseDatabase.getInstance().getReference("search/request");

        rootLayout = (RelativeLayout) findViewById(R.id.rootLayout);
        LayoutTransition lt = new LayoutTransition();
        lt.disableTransitionType(LayoutTransition.CHANGING);
        rootLayout.setLayoutTransition(lt);
        searchLayout = (LinearLayout) findViewById(R.id.searchLayout);
        searchLayout.setLayoutTransition(lt);
        searchParams = searchLayout.getLayoutParams();

        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        imageView = (ImageView) findViewById(R.id.image);
        searchEditText = (EditText) findViewById(R.id.search_field);
        searchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    if (!searchEditText.getText().toString().isEmpty() && searchEditText.getText().toString().split("\\s+").length != 0) {
                        progressBar.setVisibility(View.VISIBLE);
                        if (imageView.getVisibility() == View.VISIBLE){
                            moveViewToTop();
                        }

                        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);

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
                                    if (hitsArrayList != null && hitsArrayList.size() > 0) {
                                        for (Map<String, Object> hitMap : hitsArrayList) {
                                            Map<String, Object> detailsMap = (Map<String, Object>) hitMap.get("_source");
                                            searchedArticles.add(new Article(detailsMap.get("uid").toString(), detailsMap.get("title").toString(), detailsMap.get("description").toString(), Double.valueOf(String.valueOf(detailsMap.get("price")))));
                                        }
                                    } else {
                                        searchedArticles.add(new Article("", "Didn't find a match for your search...", "", 0.0));
                                    }
                                    progressBar.setVisibility(View.GONE);
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

        fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setInterpolator(new DecelerateInterpolator());
        fadeOut.setDuration(150);
        fadeOut.setFillAfter(true);

        fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new DecelerateInterpolator());
        fadeIn.setDuration(300);
        fadeIn.setFillAfter(true);

//        Display display = getWindowManager().getDefaultDisplay();
//        Point size = new Point();
//        display.getSize(size);
//        int height = size.y;
//
//        translate = new TranslateAnimation(0, 0, 0, - height/2 + searchEditText.getHeight()/2);
//        translate.setInterpolator(new DecelerateInterpolator());
//        translate.setDuration(1000);
//        translate.setFillAfter(true);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        assert fab != null;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent;
                switch (PreferencesHelper.getAppState(getApplicationContext())){
                    case PreferencesHelper.APP_STATE_AUTHENTICATED:
                        intent = new Intent(MainActivity.this, NewArticleActivity.class);
                        startActivity(intent);
                        break;
                    case PreferencesHelper.APP_STATE_UNAUTHENTICATED:
                        intent = new Intent(MainActivity.this, AuthenticationActivity.class);
                        startActivity(intent);
                        break;
                    case PreferencesHelper.APP_STATE_UNREGISTERED:
                        intent = new Intent(MainActivity.this, RegistrationActivity.class);
                        startActivity(intent);
                        break;
                }
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
        } else if (id == R.id.unlink_device){
            FirebaseAuth.getInstance().signOut();
            PreferencesHelper.setAppState(getApplicationContext(), PreferencesHelper.APP_STATE_UNREGISTERED);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void moveViewToTop() {
        imageView.startAnimation(fadeOut);
        imageView.postOnAnimationDelayed(new Runnable() {
            @Override
            public void run() {
                imageView.setVisibility(View.GONE);
            }
        }, 150);
        searchListView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBackPressed() {
        if (imageView.getVisibility() == View.GONE){
            imageView.setVisibility(View.VISIBLE);
            imageView.startAnimation(fadeIn);
            searchLayout.setLayoutParams(searchParams);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        PreferencesHelper.setAppState(getApplicationContext(), PreferencesHelper.APP_STATE_UNREGISTERED);
    }
}
