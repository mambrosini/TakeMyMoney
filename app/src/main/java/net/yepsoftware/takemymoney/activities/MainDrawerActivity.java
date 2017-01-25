package net.yepsoftware.takemymoney.activities;

import android.animation.LayoutTransition;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import net.yepsoftware.takemymoney.R;
import net.yepsoftware.takemymoney.activities.menu.fragments.MyArticlesFragment;
import net.yepsoftware.takemymoney.activities.menu.fragments.SearchFragment;
import net.yepsoftware.takemymoney.adapters.ArticleListAdapter;
import net.yepsoftware.takemymoney.helpers.PreferencesHelper;
import net.yepsoftware.takemymoney.helpers.UIUtils;
import net.yepsoftware.takemymoney.model.Article;
import net.yepsoftware.takemymoney.model.SearchQuery;

import java.util.ArrayList;
import java.util.Map;

public class MainDrawerActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        SearchFragment.OnFragmentInteractionListener,
        MyArticlesFragment.OnFragmentInteractionListener{

    private LinearLayout headerLayout;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_drawer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        headerLayout = (LinearLayout) navigationView.getHeaderView(0);

        customizeDrawerHeader();

        displayView(R.id.nav_camera);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        assert fab != null;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent;
                switch (PreferencesHelper.getAppState(MainDrawerActivity.this)){
                    case PreferencesHelper.APP_STATE_AUTHENTICATED:
                        intent = new Intent(MainDrawerActivity.this, NewArticleActivity.class);
                        startActivity(intent);
                        break;
                    case PreferencesHelper.APP_STATE_UNAUTHENTICATED:
                        intent = new Intent(MainDrawerActivity.this, AuthenticationActivity.class);
                        startActivity(intent);
                        break;
                    case PreferencesHelper.APP_STATE_UNREGISTERED:
                        intent = new Intent(MainDrawerActivity.this, RegistrationActivity.class);
                        startActivity(intent);
                        break;
                }
            }
        });

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {

                    Log.d("FirebaseAuth", "User signed in.");
                } else {
                    Log.d("FirebaseAuth", "User signed out.");
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

    private void customizeDrawerHeader() {
        String account;
        int imageResID;

        if (PreferencesHelper.getUsername(getApplicationContext()).equals("")){
            account = "Anonymous";
            imageResID = R.drawable.ic_account_circle_white;
        } else {
            account = PreferencesHelper.getUsername(getApplicationContext());
            imageResID = R.mipmap.ic_launcher;
        }

        for (int i = 0; i < headerLayout.getChildCount(); i++) {
            View view = headerLayout.getChildAt(i);
            if (view instanceof TextView){
                if (view.getId() == R.id.account) {
                    ((TextView)view).setText(account);
                }
            } else if (view instanceof ImageView){
                ((ImageView)view).setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), imageResID));
            }
        }
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
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
            PreferencesHelper.resetSettingsAndUnlinkDevice(MainDrawerActivity.this);
            customizeDrawerHeader();
            return true;
        } else if (id == R.id.link_device){
            progressDialog = UIUtils.showProgressDialog(MainDrawerActivity.this, "Signin in...");
            mAuth.signInWithEmailAndPassword("test@takemymoney.com", "Maxman16")
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            progressDialog.dismiss();
                            if (!task.isSuccessful()) {
                                Log.w("FirebaseAuth", "signInWithEmail:failed", task.getException());
                                Toast.makeText(MainDrawerActivity.this, "Auth Failed",
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                PreferencesHelper.setTestAccount(MainDrawerActivity.this);
                                customizeDrawerHeader();
                            }
                        }
                    });

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        displayView(id);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void displayView(int viewId) {

        Fragment fragment = null;
        String title = getString(R.string.app_name);

        if (viewId == R.id.nav_camera) {
            fragment = new SearchFragment();
            title  = "Search";
        } else if (viewId == R.id.nav_gallery) {
            fragment = new MyArticlesFragment();
            title  = "My Articles";
        } else if (viewId == R.id.nav_slideshow) {
            fragment = new SearchFragment();
            title  = "Search";
        } else if (viewId == R.id.nav_manage) {
            fragment = new SearchFragment();
            title  = "Search";
        } else if (viewId == R.id.nav_share) {
            fragment = new SearchFragment();
            title  = "Search";
        } else if (viewId == R.id.nav_send) {
            fragment = new SearchFragment();
            title  = "Search";
        }

        if (fragment != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_frame, fragment);
            ft.commit();
        }

        // set the toolbar title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

    }

    @Override
    public void onSearchInteraction(Uri uri) {

    }

    @Override
    public void onMyArticlesInteraction(Uri uri) {

    }
}
