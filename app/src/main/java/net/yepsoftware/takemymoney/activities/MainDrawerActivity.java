package net.yepsoftware.takemymoney.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import net.yepsoftware.takemymoney.R;
import net.yepsoftware.takemymoney.activities.menu.fragments.MyArticlesFragment;
import net.yepsoftware.takemymoney.activities.menu.fragments.SearchFragment;
import net.yepsoftware.takemymoney.activities.menu.fragments.SettingsFragment;
import net.yepsoftware.takemymoney.helpers.AuthUtils;
import net.yepsoftware.takemymoney.helpers.PreferencesHelper;
import net.yepsoftware.takemymoney.helpers.UIUtils;

import java.util.concurrent.Callable;

public class MainDrawerActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        SearchFragment.OnFragmentInteractionListener,
        MyArticlesFragment.OnFragmentInteractionListener,
        SettingsFragment.OnFragmentInteractionListener{

    private NavigationView navigationView;
    private Menu appMenu;
    private LinearLayout headerLayout;
    private FirebaseAuth mAuth;
    private ProgressDialog progressDialog;

    public static final int PAGE_SEARCH = 0;
    public static final int PAGE_MY_ARTICLES = 1;
    public static final int PAGE_SETTINGS = 2;

    public static final int MENU_AUTHENTICATE_REG = 0;
    public static final int MENU_AUTHENTICATE_SIGN = 0;
    public static final int MENU_SIGN_OUT = 1;
    public static final int MENU_CHANGE_ACCOUNT = 2;
    public static final int MENU_LINK_TEST_ACCOUNT = 3;


    private int currentPage;

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

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        headerLayout = (LinearLayout) navigationView.getHeaderView(0);

        navigate(PAGE_SEARCH);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        assert fab != null;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navigateToSellPage();
            }
        });

        mAuth = FirebaseAuth.getInstance();

        if (PreferencesHelper.getAppState(getApplicationContext()).equals(PreferencesHelper.APP_STATE_AUTHENTICATED)) {
            PreferencesHelper.setAppState(getApplicationContext(), PreferencesHelper.APP_STATE_UNAUTHENTICATED);
        }

        if (PreferencesHelper.isAutoLogin(getApplicationContext())  && PreferencesHelper.getAppState(MainDrawerActivity.this).equals(PreferencesHelper.APP_STATE_UNAUTHENTICATED)) {
            progressDialog = UIUtils.showProgressDialog(MainDrawerActivity.this, "Signin in...");
            firebaseSignIn(PreferencesHelper.getMail(getApplicationContext()),
                    PreferencesHelper.getPassword(getApplicationContext()),
                    new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            progressDialog.dismiss();
                            if (!task.isSuccessful()) {
                                Log.w("FirebaseAuth", "signInWithEmail:failed", task.getException());
                                Toast.makeText(MainDrawerActivity.this, "Auth Failed",
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                PreferencesHelper.setAppState(getApplicationContext(), PreferencesHelper.APP_STATE_AUTHENTICATED);
                                refreshUI();
                            }
                        }
                    });
        }
    }

    private void navigateToSellPage() {
        Intent intent;
        switch (PreferencesHelper.getAppState(MainDrawerActivity.this)){
            case PreferencesHelper.APP_STATE_AUTHENTICATED:
                intent = new Intent(MainDrawerActivity.this, NewArticleActivity.class);
                startActivity(intent);
                break;
            case PreferencesHelper.APP_STATE_UNAUTHENTICATED:
                if (PreferencesHelper.isAutoLogin(getApplicationContext())){
                    AuthUtils.signIn(MainDrawerActivity.this, mAuth, new Callable() {
                        @Override
                        public Object call() throws Exception {
                            Intent intent = new Intent(MainDrawerActivity.this, NewArticleActivity.class);
                            startActivity(intent);
                            return null;
                        }
                    });
                } else {
                    UIUtils.showPreSellAuthDialog(MainDrawerActivity.this, false);
                }
                break;
            case PreferencesHelper.APP_STATE_UNREGISTERED:
                UIUtils.showPreSellAuthDialog(MainDrawerActivity.this, true);
                break;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    private void customizeDrawerHeader() {
        String account;
        int imageResID;

        switch (PreferencesHelper.getAppState(getApplicationContext())){
            case PreferencesHelper.APP_STATE_UNAUTHENTICATED:
                account = PreferencesHelper.getMail(getApplicationContext());
                imageResID = R.drawable.ic_account_circle_white;
                break;
            case PreferencesHelper.APP_STATE_UNREGISTERED:
                account = "Anonymous";
                imageResID = R.drawable.ic_account_circle_white;
                break;
            case PreferencesHelper.APP_STATE_AUTHENTICATED:
                account = PreferencesHelper.getMail(getApplicationContext());
                imageResID = R.mipmap.ic_launcher;
                break;
            default:
                account = "Anonymous";
                imageResID = R.drawable.ic_account_circle_white;
                break;
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
        } else if (currentPage != PAGE_SEARCH){
            navigate(PAGE_SEARCH);
        } else {
            SearchFragment searchFragment = ((SearchFragment)getSupportFragmentManager().getFragments().get(0));
            if (searchFragment != null && searchFragment.isCloseable()) {
                super.onBackPressed();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        appMenu = menu;
        customizeMenu();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == MENU_AUTHENTICATE_REG) {
            UIUtils.showAuthDialog(MainDrawerActivity.this);
            return true;
        } else if (id == MENU_AUTHENTICATE_SIGN){
            UIUtils.showAuthDialog(MainDrawerActivity.this);
            return true;
        } else if (id == MENU_SIGN_OUT){
            FirebaseAuth.getInstance().signOut();
            PreferencesHelper.setAppState(getApplicationContext(), PreferencesHelper.APP_STATE_UNAUTHENTICATED);
            refreshUI();
            navigate(PAGE_SEARCH);
            return true;
        }  else if (id == MENU_CHANGE_ACCOUNT){
            FirebaseAuth.getInstance().signOut();
            PreferencesHelper.resetSettingsAndUnlinkDevice(MainDrawerActivity.this);
            refreshUI();
            navigate(PAGE_SEARCH);
            return true;
        }

        else if (id == MENU_LINK_TEST_ACCOUNT){
            progressDialog = UIUtils.showProgressDialog(MainDrawerActivity.this, "Signin in...");
            firebaseSignIn("test@takemymoney.com", "Maxman16",
                    new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            progressDialog.dismiss();
                            if (!task.isSuccessful()) {
                                Log.w("FirebaseAuth", "signInWithEmail:failed", task.getException());
                                Toast.makeText(MainDrawerActivity.this, "Auth Failed",
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                PreferencesHelper.setTestAccount(MainDrawerActivity.this);
                                refreshUI();
                            }
                        }
                    });

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void firebaseSignIn(String user, String password , OnCompleteListener onCompleteListener) {
        mAuth.signInWithEmailAndPassword(user, password)
                .addOnCompleteListener(this, onCompleteListener);
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

    public void navigate(int destination){
        switch (destination){
            case PAGE_SEARCH:
                displayView(R.id.nav_search);
                break;
            case PAGE_MY_ARTICLES:
                displayView(R.id.nav_my_articles);
                break;
            case PAGE_SETTINGS:
                displayView(R.id.nav_settings);
                break;
        }
    }

    public void displayView(int viewId) {

        Fragment fragment = null;
        String title = getString(R.string.app_name);
        navigationView.setCheckedItem(viewId);
        if (viewId == R.id.nav_search) {
            currentPage = PAGE_SEARCH;
            fragment = new SearchFragment();
            title  = "Search";
        } else if (viewId == R.id.nav_my_articles) {
            currentPage = PAGE_MY_ARTICLES;
            fragment = new MyArticlesFragment();
            title  = "My Articles";
        } else if (viewId == R.id.nav_settings) {
            currentPage = PAGE_SETTINGS;
            fragment = new SettingsFragment();
            title  = "Settings";
        } else if (viewId == R.id.nav_share) {
            currentPage = PAGE_SEARCH;
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

    public void refreshUI(){
        customizeDrawerHeader();
        customizeMenu();
    }

    private void customizeMenu(){
        if (appMenu != null) {
            switch (PreferencesHelper.getAppState(getApplicationContext())) {
                case PreferencesHelper.APP_STATE_UNAUTHENTICATED:
                    appMenu.clear();
                    appMenu.add(0,MENU_AUTHENTICATE_REG, 0, "Authenticate");
                    appMenu.add(0, MENU_CHANGE_ACCOUNT, 0, "Change account");
                    break;
                case PreferencesHelper.APP_STATE_UNREGISTERED:
                    appMenu.clear();
                    appMenu.add(0,MENU_AUTHENTICATE_SIGN, 0, "Authenticate");
                    appMenu.add(0,MENU_LINK_TEST_ACCOUNT, 0, "Link Test Account");
                    break;
                case PreferencesHelper.APP_STATE_AUTHENTICATED:
                    appMenu.clear();
                    appMenu.add(0,MENU_SIGN_OUT, 0, "Sign Out");
                    appMenu.add(0, MENU_CHANGE_ACCOUNT, 0, "Change account");
                    break;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshUI();
    }

    @Override
    public void onSearchInteraction(Uri uri) {

    }

    @Override
    public void onMyArticlesInteraction(Uri uri) {

    }

    @Override
    public void onSettingsInteraction(Uri uri) {

    }
}
