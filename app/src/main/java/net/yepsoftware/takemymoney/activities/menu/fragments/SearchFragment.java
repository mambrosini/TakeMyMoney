package net.yepsoftware.takemymoney.activities.menu.fragments;

import android.animation.LayoutTransition;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
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
import net.yepsoftware.takemymoney.helpers.PreferencesHelper;
import net.yepsoftware.takemymoney.model.Article;
import net.yepsoftware.takemymoney.model.SearchQuery;

import java.util.ArrayList;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SearchFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SearchFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SearchFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match

    private DatabaseReference requestDBRef;
    private DatabaseReference responseDBRef;
    private EditText searchEditText;
    private ImageView imageView;
    private RelativeLayout rootLayout;
    private LinearLayout searchLayout;
    private ViewGroup.LayoutParams searchParams;
    private ListView searchListView;
    private ArrayList<Article> searchedArticles;
    private ArticleListAdapter articleListAdapter;
    private ProgressBar progressBar;

    private Animation fadeOut;
    private Animation fadeIn;

    private OnFragmentInteractionListener mListener;

    public SearchFragment() {
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
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        searchListView = (ListView) view.findViewById(R.id.listView);

        requestDBRef = FirebaseDatabase.getInstance().getReference("search/request");

        rootLayout = (RelativeLayout) view.findViewById(R.id.rootLayout);
        LayoutTransition lt = new LayoutTransition();
        lt.disableTransitionType(LayoutTransition.CHANGING);
        rootLayout.setLayoutTransition(lt);
        searchLayout = (LinearLayout) view.findViewById(R.id.searchLayout);
        searchLayout.setLayoutTransition(lt);
        searchParams = searchLayout.getLayoutParams();

        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);

        imageView = (ImageView) view.findViewById(R.id.image);
        searchEditText = (EditText) view.findViewById(R.id.search_field);
        searchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH || event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    if (!searchEditText.getText().toString().isEmpty() && searchEditText.getText().toString().split("\\s+").length != 0) {
                        progressBar.setVisibility(View.VISIBLE);
                        if (imageView.getVisibility() == View.VISIBLE){
                            moveViewToTop();
                        }

                        InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);

                        searchedArticles.clear();
                        articleListAdapter.notifyDataSetChanged();

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
                                    articleListAdapter.notifyDataSetChanged();
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
        articleListAdapter = new ArticleListAdapter(getActivity(), searchedArticles);
        searchListView.setAdapter(articleListAdapter);

        fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setInterpolator(new DecelerateInterpolator());
        fadeOut.setDuration(150);
        fadeOut.setFillAfter(true);

        fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new DecelerateInterpolator());
        fadeIn.setDuration(300);
        fadeIn.setFillAfter(true);

        searchListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), ArticleDetailActivity.class);
                Article article = searchedArticles.get(position);
                intent.putExtra("uid", article.uid);
                intent.putExtra("title", article.title);
                intent.putExtra("description", article.description);
                intent.putExtra("price", article.price);
                startActivity(intent);
            }
        });

        view.setFocusableInTouchMode(true);
        view.requestFocus();
        view.setOnKeyListener( new View.OnKeyListener()
        {
            @Override
            public boolean onKey( View v, int keyCode, KeyEvent event )
            {
                if( keyCode == KeyEvent.KEYCODE_BACK )
                {
                    if (imageView.getVisibility() == View.GONE){
                        imageView.setVisibility(View.VISIBLE);
                        imageView.startAnimation(fadeIn);
                        searchLayout.setLayoutParams(searchParams);
                    } else {
                        getActivity().onBackPressed();
                    }
                }
                return false;
            }
        } );

        return view;
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

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onSearchInteraction(uri);
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
        void onSearchInteraction(Uri uri);
    }
}
