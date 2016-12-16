package com.example.android.popularmovies;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;


/**
 * Created by pestonio on 20/11/2016.
 */

public class MovieAdapter extends ArrayAdapter<MoviesClass> {

    public MovieAdapter(Context context, ArrayList<MoviesClass> movieItem) {
        super(context, 0, movieItem);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
       View listItemView;
        listItemView = LayoutInflater.from(getContext()).inflate(R.layout.list_item, parent, false);
        ImageView movieImage = (ImageView) listItemView.findViewById(R.id.list_item_image);
        MoviesClass currentMovie = getItem(position);
        if (currentMovie != null) {
            Picasso.with(getContext()).load(currentMovie.getMoviePoster()).into(movieImage);
        }
        return listItemView;
}
}
