package com.example.android.popularmovies;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by pestonio on 20/11/2016.
 */

public class MoviesClass implements Parcelable{

    private String mMovieTitle;
    private String mMovieOverview;
    private String mMovieReleaseDate;
    private String mMovieRating;
    private String mMoviePoster;
    private String mMovieId;

    public MoviesClass(String movieTitle, String movieOverview, String releaseDate, String movieRating, String moviePoster, String movieId){
        mMovieTitle = movieTitle;
        mMovieOverview = movieOverview;
        mMovieReleaseDate = releaseDate;
        mMovieRating = movieRating;
        mMoviePoster = moviePoster;
        mMovieId = movieId;
    }

    public String getMovieTitle(){
        return mMovieTitle;
    }
    public String getMovieOverview(){
        return mMovieOverview;
    }
    public String getMovieReleaseDate(){
        return mMovieReleaseDate;
    }
    public String getMovieRating(){
        return mMovieRating;
    }
    public String getMoviePoster(){
        return mMoviePoster;
    }
    public String getMovieId(){return mMovieId;}

    protected MoviesClass(Parcel in) {
        mMovieTitle = in.readString();
        mMovieOverview = in.readString();
        mMovieReleaseDate = in.readString();
        mMovieRating = in.readString();
        mMoviePoster = in.readString();
        mMovieId = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mMovieTitle);
        dest.writeString(mMovieOverview);
        dest.writeString(mMovieReleaseDate);
        dest.writeString(mMovieRating);
        dest.writeString(mMoviePoster);
        dest.writeString(mMovieId);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<MoviesClass> CREATOR = new Parcelable.Creator<MoviesClass>() {
        @Override
        public MoviesClass createFromParcel(Parcel in) {
            return new MoviesClass(in);
        }

        @Override
        public MoviesClass[] newArray(int size) {
            return new MoviesClass[size];
        }
    };
}