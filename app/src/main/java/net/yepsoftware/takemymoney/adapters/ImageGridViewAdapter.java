package net.yepsoftware.takemymoney.adapters;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import net.yepsoftware.takemymoney.R;

import java.util.ArrayList;

/**
 * Created by mambrosini on 2/1/17.
 */
public class ImageGridViewAdapter extends ArrayAdapter<String> {
    ArrayList<String> images;

    public ImageGridViewAdapter(Context context, ArrayList<String> images) {
        super(context, 0 , images);
        this.images = images;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.image_gridview_item, parent, false);
        }

        ImageView imageView = (ImageView) convertView.findViewById(R.id.imageView);
        final ProgressBar progressBar = (ProgressBar) convertView.findViewById(R.id.progressBar);
        Picasso.with(getContext())
                .load(getItem(position))
                .noFade().resize(150,150)
                .centerCrop()
                .into(imageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError() {
                        progressBar.setVisibility(View.GONE);
                    }
                });

        return convertView;

    }
}
