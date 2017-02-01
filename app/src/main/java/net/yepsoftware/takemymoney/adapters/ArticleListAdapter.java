package net.yepsoftware.takemymoney.adapters;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import net.yepsoftware.takemymoney.R;
import net.yepsoftware.takemymoney.model.Article;

import java.util.ArrayList;

/**
 * Created by Maxi on 18/1/2017.
 */
public class ArticleListAdapter extends ArrayAdapter<Article> {

    ArrayList<Article> articles;
    boolean displayState;

    public ArticleListAdapter(Context context, ArrayList<Article> articles, boolean displayState) {
        super(context, 0 , articles);
        this.articles = articles;
        this.displayState = displayState;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.search_list_item, parent, false);
        }

        TextView title = (TextView) convertView.findViewById(R.id.title);
        TextView description = (TextView) convertView.findViewById(R.id.description);
        TextView price = (TextView) convertView.findViewById(R.id.price);
        TextView state = (TextView) convertView.findViewById(R.id.status);

        Article article = getItem(position);
        title.setText(article.title);
        description.setText(article.description);
        if (article.price != 0){
            price.setText("$" + String.valueOf(article.price));
        } else {
            price.setText("");
        }
        if (displayState){
            state.setText(article.state.toString());
            switch (article.state){
                case ACTIVE:
                    state.setTextColor(ContextCompat.getColor(getContext(), R.color.articleActive));
                    break;
                case DISABLED:
                    state.setTextColor(ContextCompat.getColor(getContext(), R.color.articleDisabled));
                    break;
                case SOLD:
                    state.setTextColor(ContextCompat.getColor(getContext(), R.color.articleSold));
                    break;
            }
            state.setVisibility(View.VISIBLE);
        }

        return convertView;

    }
}