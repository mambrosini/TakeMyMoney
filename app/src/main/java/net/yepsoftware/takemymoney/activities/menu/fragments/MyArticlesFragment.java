package net.yepsoftware.takemymoney.activities.menu.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

        articles = new ArrayList<>();
        listView = (ListView) view.findViewById(R.id.listView);
        articleListAdapter = new ArticleListAdapter(getActivity(), articles);
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
}
