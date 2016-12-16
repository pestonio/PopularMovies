package com.example.android.popularmovies;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.popularmovies.Data.FavouriteMoviesHelper;
import com.example.android.popularmovies.Data.MovieContract;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static android.view.View.GONE;

/**
 * Created by pestonio on 20/11/2016.
 */

public class DetailActivity extends AppCompatActivity {
    private static final String LOG_TAG = DetailActivity.class.getSimpleName();
    String API_KEY = "api_key=XXXXXXXXXXXXXXXXXXXXXXXXX";
    private boolean isFavourite;
    private MoviesClass currentMovie;
    private List<String> movieTrailers;
    private List<String> movieReviews;
    private View marginTrailerOne;
    private View marginTrailerTwo;
    private View marginTrailerThree;
    private View marginTrailerFour;
    private FavouriteMoviesHelper mDbHelper;

    //Create a URL from a string
    private static URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException exception) {
            Log.e(LOG_TAG, "Error with creating URL", exception);
            return null;
        }
        return url;
    }

    private static String makeHttpRequest(URL url) throws IOException {

        String jsonResponse = "";
        if (url == null) {
            return null;
        }
        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setReadTimeout(10000);
            urlConnection.setConnectTimeout(15000);
            urlConnection.connect();
            inputStream = urlConnection.getInputStream();
            jsonResponse = readFromStream(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            } else {
                Log.e(LOG_TAG, "Error" + urlConnection.getResponseCode());
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line = bufferedReader.readLine();
            while (line != null) {
                output.append(line);
                line = bufferedReader.readLine();
            }
        }
        return output.toString();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }
        mDbHelper = new FavouriteMoviesHelper(getApplicationContext());

        TextView title = (TextView) findViewById(R.id.movie_title);
        TextView year = (TextView) findViewById(R.id.movie_year);
        TextView rating = (TextView) findViewById(R.id.movie_rating);
        TextView synopsis = (TextView) findViewById(R.id.movie_overview);
        ImageView poster = (ImageView) findViewById(R.id.movie_image);

        marginTrailerOne = findViewById(R.id.trailer_margin_one);
        marginTrailerTwo = findViewById(R.id.trailer_margin_two);
        marginTrailerThree = findViewById(R.id.trailer_margin_three);
        marginTrailerFour = findViewById(R.id.trailer_margin_four);
        marginTrailerOne.setVisibility(GONE);
        marginTrailerTwo.setVisibility(GONE);
        marginTrailerThree.setVisibility(GONE);
        marginTrailerFour.setVisibility(GONE);

        movieTrailers = new ArrayList<>();
        movieReviews = new ArrayList<>();
        Intent intent = getIntent();
        currentMovie = intent.getParcelableExtra("Movie");
        title.setText(currentMovie.getMovieTitle());
        year.setText(R.string.released);
        year.append(currentMovie.getMovieReleaseDate());
        rating.setText(R.string.user_rating);
        rating.append(currentMovie.getMovieRating());
        synopsis.setText(currentMovie.getMovieOverview());
        Picasso.with(DetailActivity.this).load(currentMovie.getMoviePoster()).into(poster);
        MovieTrailersTask trailersTask = new MovieTrailersTask();
        trailersTask.execute();
        MovieReviewsTask reviewsTask = new MovieReviewsTask();
        reviewsTask.execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.detail_view_favourite, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        SQLiteDatabase database = mDbHelper.getReadableDatabase();
        String[] projection = {MovieContract.MovieEntry.COLUMN_MOVIE_KEY};
        String selection = MovieContract.MovieEntry.COLUMN_MOVIE_KEY + " = ?";
        String[] selectionArgs = {currentMovie.getMovieId()};

        Cursor cursor = database.query(MovieContract.MovieEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, null);
        for (int i = 0; i < cursor.getCount(); i++) {
            cursor.moveToNext();
            int keyIndex = cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_KEY);
            String key = cursor.getString(keyIndex);
            if (Objects.equals(key, currentMovie.getMovieId())) {
                menu.findItem(R.id.action_detail_fave).setIcon(R.drawable.ic_action_detail_already_liked);
                isFavourite = true;
            } else {
                menu.findItem(R.id.action_detail_fave).setIcon(R.drawable.ic_action_detail_like);
                isFavourite = false;
            }
        }
        database.close();
        cursor.close();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        if (id == R.id.action_detail_fave && !isFavourite) {
            item.setIcon(R.drawable.ic_action_detail_already_liked);

            ContentValues values = new ContentValues();

            values.put(MovieContract.MovieEntry.COLUMN_MOVIE_TITLE, currentMovie.getMovieTitle());
            values.put(MovieContract.MovieEntry.COLUMN_MOVIE_SYNOPSIS, currentMovie.getMovieOverview());
            values.put(MovieContract.MovieEntry.COLUMN_MOVIE_RELEASE_DATE, currentMovie.getMovieReleaseDate());
            values.put(MovieContract.MovieEntry.COLUMN_MOVIE_RATING, currentMovie.getMovieRating());
            values.put(MovieContract.MovieEntry.COLUMN_MOVIE_POSTER, currentMovie.getMoviePoster());
            values.put(MovieContract.MovieEntry.COLUMN_MOVIE_KEY, currentMovie.getMovieId());

            long newRowId = database.insert(MovieContract.MovieEntry.TABLE_NAME, null, values);

            Toast.makeText(this, "Item will be added to favourites", Toast.LENGTH_SHORT).show();
            isFavourite = true;
            database.close();
            return true;
        } else if (id == R.id.action_detail_fave) {
            item.setIcon(R.drawable.ic_action_detail_like);
            String selection = MovieContract.MovieEntry.COLUMN_MOVIE_TITLE + " LIKE ?";
            String[] selectionArgs = {currentMovie.getMovieTitle()};
            database.delete(MovieContract.MovieEntry.TABLE_NAME, selection, selectionArgs);

            Toast.makeText(this, "Item will be removed from favourites", Toast.LENGTH_SHORT).show();
            isFavourite = false;
            database.close();
            return false;
        }
        database.close();
        return super.onOptionsItemSelected(item);
    }

    private List extractTrailerFromJson(String detailsJson) {
        if (TextUtils.isEmpty(detailsJson)) {
            return null;
        }
        try {
            JSONObject baseObject = new JSONObject(detailsJson);
            JSONArray baseArray = baseObject.getJSONArray("results");
            for (int i = 0; i < baseArray.length(); i++) {
                JSONObject videoId = baseArray.getJSONObject(i);
                String trailerType = videoId.getString("type");
                if (trailerType.equals("Trailer")) {
                    String movieTrailerId = videoId.getString("key");
                    movieTrailers.add(movieTrailerId);

                    String trailerDesc = videoId.getString("name");
                    movieTrailers.add(trailerDesc);
                }
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Problem parsing movie JSON results", e);
        }
        Log.v(LOG_TAG, "Trailer ID" + movieTrailers.toString());

        return movieTrailers;
    }

    private class MovieTrailersTask extends AsyncTask<String, Void, List> {

        @Override
        protected List doInBackground(String... strings) {
            String movieId = currentMovie.getMovieId();
            String BASE_DETAIL_URL = "https://api.themoviedb.org/3/movie/";
            String movieVideo = BASE_DETAIL_URL + movieId + "/videos?" + API_KEY;
            URL url = createUrl(movieVideo);
            String detailJsonResponse = "";
            try {
                detailJsonResponse = makeHttpRequest(url);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return extractTrailerFromJson(detailJsonResponse);
        }

        @Override
        protected void onPostExecute(List movieDetailClasses) {
            updateTrailerUi(movieDetailClasses);
        }

        private void updateTrailerUi(List movieTrailers) {
            int numberOfTrailers = movieTrailers.size();
            TextView emptyTextView = (TextView) findViewById(R.id.empty_trailers);
            TextView anyTrailers = (TextView) findViewById(R.id.any_trailers);

            if (numberOfTrailers == 0) {
                View firstTrailer = findViewById(R.id.first_trailer);
                View secondTrailer = findViewById(R.id.second_trailer);
                View thirdTrailer = findViewById(R.id.third_trailer);
                anyTrailers.setVisibility(GONE);
                firstTrailer.setVisibility(GONE);
                secondTrailer.setVisibility(GONE);
                thirdTrailer.setVisibility(GONE);
                emptyTextView.setText(R.string.empty_trailers_list);
                emptyTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent trailerSearchIntent = new Intent(Intent.ACTION_VIEW,
                                Uri.parse("https://www.youtube.com/results?search_query=".concat(currentMovie.getMovieTitle())
                                        + " Trailer"));
                        startActivity(trailerSearchIntent);
                    }
                });
            }
            if (numberOfTrailers > 0) {
                TextView trailerTitleOneTextView = (TextView) findViewById(R.id.trailer_title_one);
                ImageView trailerOne = (ImageView) findViewById(R.id.trailer_img_one);
                marginTrailerOne.setVisibility(View.VISIBLE);
                marginTrailerTwo.setVisibility(View.VISIBLE);
                anyTrailers.setVisibility(View.VISIBLE);
                anyTrailers.setText(R.string.trailers_present);
                emptyTextView.setVisibility(GONE);
                final String trailerKeyOne = movieTrailers.get(0).toString();
                String trailerTitleOne = movieTrailers.get(1).toString();
                trailerTitleOneTextView.setText(trailerTitleOne);
                Picasso.with(DetailActivity.this).load("https://img.youtube.com/vi/" + trailerKeyOne + "/0.jpg").into(trailerOne);
                trailerOne.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/watch?v=".concat(trailerKeyOne)));
                        startActivity(intent);
                    }
                });
            }
            if (numberOfTrailers > 2) {
                TextView trailerTitleTwoTextView = (TextView) findViewById(R.id.trailer_title_two);
                ImageView trailerTwo = (ImageView) findViewById(R.id.trailer_img_two);
                marginTrailerThree.setVisibility(View.VISIBLE);
                final String trailerKeyTwo = movieTrailers.get(2).toString();
                String trailerTitleTwo = movieTrailers.get(3).toString();
                trailerTitleTwoTextView.setText(trailerTitleTwo);
                Picasso.with(DetailActivity.this).load("https://img.youtube.com/vi/" + trailerKeyTwo + "/0.jpg").into(trailerTwo);
                trailerTwo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/watch?v=".concat(trailerKeyTwo)));
                        startActivity(intent);
                    }
                });
            }
            if (numberOfTrailers > 4) {
                TextView trailerTitleThreeTextView = (TextView) findViewById(R.id.trailer_title_three);
                ImageView trailerThree = (ImageView) findViewById(R.id.trailer_img_three);
                marginTrailerFour.setVisibility(View.VISIBLE);
                final String trailerKeyThree = movieTrailers.get(4).toString();
                String trailerTitleThree = movieTrailers.get(5).toString();
                trailerTitleThreeTextView.setText(trailerTitleThree);
                Picasso.with(DetailActivity.this).load("https://img.youtube.com/vi/" + trailerKeyThree + "/0.jpg").into(trailerThree);
                trailerThree.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/watch?v=".concat(trailerKeyThree)));
                        startActivity(intent);
                    }
                });
            }
        }
    }

    public class MovieReviewsTask extends AsyncTask<String, Void, List> {


        @Override
        protected List<String> doInBackground(String... strings) {
            String movieId = currentMovie.getMovieId();
            String BASE_REVIEW_URL = "https://api.themoviedb.org/3/movie/";
            String movieReview = BASE_REVIEW_URL + movieId + "/reviews?" + API_KEY;
            URL url = createUrl(movieReview);
            String detailJsonResponse = "";
            try {
                detailJsonResponse = makeHttpRequest(url);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return extractReviewFromJson(detailJsonResponse);
        }

        @Override
        protected void onPostExecute(List list) {
            updateReviewUi(list);
        }

        private void updateReviewUi(List movieReviews) {
            int numberOfReviews = movieReviews.size();
            TextView anyReviews = (TextView) findViewById(R.id.any_reviews);
            TextView authorReviewOne = (TextView) findViewById(R.id.author);
            TextView authorReviewTwo = (TextView) findViewById(R.id.author_two);
            TextView authorReviewThree = (TextView) findViewById(R.id.author_three);
            final TextView reviewTextOne = (TextView) findViewById(R.id.review_text);
            final TextView reviewTextTwo = (TextView) findViewById(R.id.review_text_two);
            final TextView reviewTextThree = (TextView) findViewById(R.id.review_text_three);
            View reviewLayoutOne = findViewById(R.id.review_one);
            View reviewLayoutTwo = findViewById(R.id.review_two);
            View reviewLayoutThree = findViewById(R.id.review_three);
            reviewLayoutOne.setVisibility(GONE);
            reviewLayoutTwo.setVisibility(GONE);
            reviewLayoutThree.setVisibility(GONE);
            if (numberOfReviews == 0) {
                anyReviews.setText(R.string.no_reviews);
            }
            if (numberOfReviews > 0) {
                String reviewAuthorOne = movieReviews.get(0).toString();
                String reviewContentOne = movieReviews.get(1).toString();
                anyReviews.setText(R.string.reviews_present);
                authorReviewOne.setText(getString(R.string.review_author).concat(reviewAuthorOne));
                reviewTextOne.setText(reviewContentOne);
                reviewLayoutOne.setVisibility(View.VISIBLE);
                if (reviewTextOne.getLineCount() <= 3) {
                    TextView expandOne = (TextView) findViewById(R.id.expand_review_one);
                    expandOne.setVisibility(GONE);
                }
                final boolean[] isTextViewClicked = {false};
                reviewTextOne.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (isTextViewClicked[0]) {
                            reviewTextOne.setMaxLines(3);
                            isTextViewClicked[0] = false;
                        } else {
                            reviewTextOne.setMaxLines(Integer.MAX_VALUE);
                            isTextViewClicked[0] = true;
                        }
                    }
                });
            }
            if (numberOfReviews > 2) {
                String reviewAuthorTwo = movieReviews.get(2).toString();
                String reviewContentTwo = movieReviews.get(3).toString();
                authorReviewTwo.setText(getString(R.string.review_author).concat(reviewAuthorTwo));
                reviewTextTwo.setText(reviewContentTwo);
                reviewLayoutTwo.setVisibility(View.VISIBLE);
                if (reviewTextTwo.getLineCount() <= 3) {
                    TextView expandTwo = (TextView) findViewById(R.id.expand_review_two);
                    expandTwo.setVisibility(GONE);
                }
                final boolean[] isTextViewTwoClicked = {false};
                reviewTextTwo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (isTextViewTwoClicked[0]) {
                            reviewTextTwo.setMaxLines(3);
                            isTextViewTwoClicked[0] = false;
                        } else {
                            reviewTextTwo.setMaxLines(Integer.MAX_VALUE);
                            isTextViewTwoClicked[0] = true;
                        }
                    }
                });
            }
            if (numberOfReviews > 4) {
                String reviewAuthorThree = movieReviews.get(4).toString();
                String reviewContentThree = movieReviews.get(5).toString();
                authorReviewThree.setText(getString(R.string.review_author).concat(reviewAuthorThree));
                reviewTextThree.setText(reviewContentThree);
                reviewLayoutThree.setVisibility(View.VISIBLE);
                if (reviewTextThree.getLineCount() <= 3) {
                    TextView expandThree = (TextView) findViewById(R.id.expand_review_three);
                    expandThree.setVisibility(GONE);
                }
                final boolean[] isTextViewThreeClicked = {false};
                reviewTextThree.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (isTextViewThreeClicked[0]) {
                            reviewTextThree.setMaxLines(3);
                            isTextViewThreeClicked[0] = false;
                        } else {
                            reviewTextThree.setMaxLines(Integer.MAX_VALUE);
                            isTextViewThreeClicked[0] = true;
                        }
                    }
                });
            }
        }
    }

    private List extractReviewFromJson(String detailsJson) {
        if (TextUtils.isEmpty(detailsJson)) {
            return null;
        }
        try {
            JSONObject baseObject = new JSONObject(detailsJson);
            JSONArray baseArray = baseObject.getJSONArray("results");
            for (int i = 0; i < baseArray.length(); i++) {
                JSONObject reviewId = baseArray.getJSONObject(i);
                String reviewAuthor = reviewId.getString("author");
                String reviewContent = reviewId.getString("content");
                movieReviews.add(reviewAuthor);
                movieReviews.add(reviewContent);
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Problem parsing movie JSON results", e);
        }
        return movieReviews;
    }
}

