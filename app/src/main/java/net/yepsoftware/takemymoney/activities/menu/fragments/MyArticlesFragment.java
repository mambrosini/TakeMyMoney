package net.yepsoftware.takemymoney.activities.menu.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import net.yepsoftware.takemymoney.R;
import net.yepsoftware.takemymoney.activities.ArticleDetailActivity;
import net.yepsoftware.takemymoney.activities.AuthenticationActivity;
import net.yepsoftware.takemymoney.activities.NewArticleActivity;
import net.yepsoftware.takemymoney.activities.RegistrationActivity;
import net.yepsoftware.takemymoney.adapters.ArticleListAdapter;
import net.yepsoftware.takemymoney.helpers.AuthUtils;
import net.yepsoftware.takemymoney.helpers.PreferencesHelper;
import net.yepsoftware.takemymoney.helpers.UIUtils;
import net.yepsoftware.takemymoney.model.Article;
import net.yepsoftware.takemymoney.model.SearchQuery;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MyArticlesFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MyArticlesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MyArticlesFragment extends Fragment {

    private ListView listView;
    private ArticleListAdapter articleListAdapter;
    private ArrayList<Article> articles;
    private DatabaseReference requestDBRef;
    private DatabaseReference responseDBRef;
    private ProgressDialog progressDialog;
    private LinearLayout authLayout;
    private ArrayList<String> articleKeys;

    private TextView label;
    private Button button;
    private TextView label2;
    private Button button2;

    private FirebaseAuth mAuth;

    private OnFragmentInteractionListener mListener;

    public MyArticlesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_my_articles, container, false);

        label = (TextView)view.findViewById(R.id.label);
        button = (Button) view.findViewById(R.id.button);
        label2 = (TextView)view.findViewById(R.id.label2);
        button2 = (Button) view.findViewById(R.id.button2);
        listView = (ListView) view.findViewById(R.id.listView);
        authLayout = (LinearLayout) view.findViewById(R.id.authLayout);

        mAuth = FirebaseAuth.getInstance();

        articleKeys = new ArrayList<>();

        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onMyArticlesInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onMyArticlesInteraction(Uri uri);
    }

    @Override
    public void onResume() {
        super.onResume();
        customizeLayout();
    }

    private void customizeLayout(){
        switch (PreferencesHelper.getAppState(getActivity())){
            case PreferencesHelper.APP_STATE_AUTHENTICATED:
                setLayoutAuthenticatedState();
                break;
            case PreferencesHelper.APP_STATE_UNAUTHENTICATED:
                setLayoutUnauthenticatedState();
                break;
            case PreferencesHelper.APP_STATE_UNREGISTERED:
                setLayoutUnregisteredState();
                break;
        }
    }

    private void setLayoutAuthenticatedState(){
        authLayout.setVisibility(View.GONE);
        label.setVisibility(View.GONE);
        button.setVisibility(View.GONE);
        label2.setVisibility(View.GONE);
        button2.setVisibility(View.GONE);
        listView.setVisibility(View.VISIBLE);
        articles = new ArrayList<>();
        articleListAdapter = new ArticleListAdapter(getActivity(), articles, true);
        listView.setAdapter(articleListAdapter);

        progressDialog = UIUtils.showProgressDialog(getActivity(), "Retrieving posts...");

        requestDBRef = FirebaseDatabase.getInstance().getReference("search/request");
        responseDBRef = FirebaseDatabase.getInstance().getReference("search/response");

        String key = requestDBRef.push().getKey();
        requestDBRef.child(key).setValue(new SearchQuery().searchByUser(PreferencesHelper.getUserId(getActivity())));
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
                            String articleKey = hitMap.get("_id").toString();
                            articleKeys.add(articleKey);
                            articles.add(new Article(detailsMap.get("uid").toString(), detailsMap.get("title").toString(), detailsMap.get("description").toString(), Double.valueOf(String.valueOf(detailsMap.get("price"))), (ArrayList<String>) detailsMap.get("images"), Article.stringToState(detailsMap.get("state").toString())));
                        }
                    } else {
                        articles.add(new Article("", "You don't have any articles posted...", "", 0.0,  null, Article.State.ACTIVE));
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

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if ((articles.get(position)).uid != "") {
                    Intent intent = new Intent(getActivity(), ArticleDetailActivity.class);
                    Article article = articles.get(position);
                    intent.putExtra("articleKey", articleKeys.get(position));
                    intent.putExtra("uid", article.uid);
                    intent.putExtra("title", article.title);
                    intent.putExtra("description", article.description);
                    intent.putExtra("price", article.price);
                    intent.putExtra("images", article.images);
                    intent.putExtra("state", article.state.toString());
                    intent.putExtra("FROM_MY_ARTICLES", true);
                    startActivity(intent);
                }
            }
        });
    }

    private void setLayoutUnauthenticatedState(){
        authLayout.setVisibility(View.VISIBLE);
        listView.setVisibility(View.GONE);
        label2.setVisibility(View.GONE);
        button2.setVisibility(View.GONE);
        label.setVisibility(View.VISIBLE);
        button.setVisibility(View.VISIBLE);
        label.setText("You need to Sign In in order to manage your posts");
        button.setText("Sign In");
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });
    }

    private void setLayoutUnregisteredState(){
        authLayout.setVisibility(View.VISIBLE);
        listView.setVisibility(View.GONE);
        label.setVisibility(View.VISIBLE);
        button.setVisibility(View.VISIBLE);
        label2.setVisibility(View.VISIBLE);
        button2.setVisibility(View.VISIBLE);
        label.setText("You need to Register in order to sell articles");
        button.setText("Register");
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), RegistrationActivity.class);
                startActivity(intent);
            }
        });
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });
    }

    private void signIn() {
        AuthUtils.signIn(getActivity(), mAuth, new Callable() {
            @Override
            public Object call() throws Exception {
                customizeLayout();
                return null;
            }
        });
    }
}
