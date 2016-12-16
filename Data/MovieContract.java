package com.example.android.popularmovies.Data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by pestonio on 12/12/2016.
 */

public class MovieContract {

    private MovieContract(){}

    public static final String CONTENT_AUTHORITY = "com.example.android.popularmovies";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_INVENTORY = "popularmovies";

    public static class MovieEntry implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_INVENTORY);

        public static final String TABLE_NAME = "faveMovies";
        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_MOVIE_TITLE = "title";
        public static final String COLUMN_MOVIE_SYNOPSIS = "synopsis";
        public static final String COLUMN_MOVIE_RELEASE_DATE = "releaseDate";
        public static final String COLUMN_MOVIE_RATING = "rating";
        public static final String COLUMN_MOVIE_POSTER = "posterPath";
        public static final String COLUMN_MOVIE_KEY = "movieKey";
    }
}
