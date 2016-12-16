package com.example.android.popularmovies.Data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by pestonio on 12/12/2016.
 */

public class FavouriteMoviesHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "faveMovies.db";

    public FavouriteMoviesHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase dB) {
        String SQL_CREATE_TABLE =
                "CREATE TABLE " + MovieContract.MovieEntry.TABLE_NAME + " ("
                + MovieContract.MovieEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + MovieContract.MovieEntry.COLUMN_MOVIE_TITLE + " TEXT NOT NULL, "
                + MovieContract.MovieEntry.COLUMN_MOVIE_SYNOPSIS + " TEXT NOT NULL, "
                + MovieContract.MovieEntry.COLUMN_MOVIE_RELEASE_DATE + " TEXT NOT NULL, "
                + MovieContract.MovieEntry.COLUMN_MOVIE_RATING + " TEXT NOT NULL, "
                + MovieContract.MovieEntry.COLUMN_MOVIE_POSTER + " TEXT NOT NULL, "
                + MovieContract.MovieEntry.COLUMN_MOVIE_KEY + " TEXT NOT NULL);";
        dB.execSQL(SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase dB, int oldVersion, int newVersion) {
        String deleteEntries = "DROP TABLE IF EXISTS " + MovieContract.MovieEntry.TABLE_NAME;
        dB.execSQL(deleteEntries);
        onCreate(dB);
    }
}
