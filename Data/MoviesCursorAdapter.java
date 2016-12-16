package com.example.android.popularmovies.Data;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;

import com.example.android.popularmovies.R;
import com.squareup.picasso.Picasso;

/**
 * Created by pestonio on 12/12/2016.
 */

public class MoviesCursorAdapter extends CursorAdapter {


    public MoviesCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ImageView imageView = (ImageView) view.findViewById(R.id.list_item_image);
        int imageColumnIndex = cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_POSTER);
        String moviePoster = cursor.getString(imageColumnIndex);
        Picasso.with(context).load(moviePoster).placeholder(R.drawable.ic_placeholder).into(imageView);
    }
}
