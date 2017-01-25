package net.yepsoftware.takemymoney.adapters;

import android.content.Context;
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

    public ArticleListAdapter(Context context, ArrayList<Article> articles) {
        super(context, 0 , articles);
        this.articles = articles;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.search_list_item, parent, false);
        }

        TextView title = (TextView) convertView.findViewById(R.id.title);
        TextView description = (TextView) convertView.findViewById(R.id.description);
        TextView price = (TextView) convertView.findViewById(R.id.price);

        Article article = getItem(position);
        title.setText(article.title);
        description.setText(article.description);
        if (article.price != 0){
            price.setText("$" + String.valueOf(article.price));
        } else {
            price.setText("");
        }

        return convertView;

    }
}