package com.example.android.popularmovies;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.popularmovies.Data.FavouriteMoviesHelper;
import com.example.android.popularmovies.Data.MovieContract;
import com.example.android.popularmovies.Data.MoviesCursorAdapter;

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

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private TextView emptyText;
    private ImageView emptyImage;
    private MovieAdapter adapter;
    private String BASE_URL;
    private GridView mMovieGrid;
    private boolean favouriteView;
    private ArrayList<MoviesClass> faveMovies;
    private MoviesCursorAdapter adapter1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        favouriteView = false;
        emptyText = (TextView) findViewById(R.id.empty_text);
        emptyImage = (ImageView) findViewById(R.id.empty_image);
        mMovieGrid = (GridView) findViewById(R.id.gridView);
        final ArrayList<MoviesClass> moviesClassArrayList = new ArrayList<>();
        MovieSyncTask task = new MovieSyncTask();
        task.execute();
        adapter = new MovieAdapter(this, moviesClassArrayList);
        mMovieGrid.setAdapter(adapter);
        mMovieGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Intent detailIntent = new Intent(getApplicationContext(), DetailActivity.class);
                if (favouriteView) {
                    detailIntent.putExtra("Movie", faveMovies.get(position));
                    startActivity(detailIntent);
                } else {
                    MoviesClass moviesClass = adapter.getItem(position);
                    detailIntent.putExtra("Movie", moviesClass);
                    startActivity(detailIntent);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Cursor cursor = null;
        if (favouriteView) {
            FavouriteMoviesHelper mDbHelper = new FavouriteMoviesHelper(getApplicationContext());
            SQLiteDatabase database = mDbHelper.getReadableDatabase();
            String[] projection = {
                    MovieContract.MovieEntry._ID,
                    MovieContract.MovieEntry.COLUMN_MOVIE_KEY,
                    MovieContract.MovieEntry.COLUMN_MOVIE_POSTER,
                    MovieContract.MovieEntry.COLUMN_MOVIE_RATING,
                    MovieContract.MovieEntry.COLUMN_MOVIE_RELEASE_DATE,
                    MovieContract.MovieEntry.COLUMN_MOVIE_TITLE,
                    MovieContract.MovieEntry.COLUMN_MOVIE_SYNOPSIS};
            cursor = database.query(MovieContract.MovieEntry.TABLE_NAME, projection, null, null, null, null, null);
            cursor.moveToNext();

            adapter1 = new MoviesCursorAdapter(this, cursor);
            mMovieGrid.setAdapter(adapter1);
            faveMovies = new ArrayList<>();


            int titleColumnIndex = cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_TITLE);
            int synopsisColumnIndex = cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_SYNOPSIS);
            int releaseColumnIndex = cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_RELEASE_DATE);
            int ratingColumnIndex = cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_RATING);
            int posterColumnIndex = cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_POSTER);
            int keyColumnIndex = cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_KEY);

            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToPosition(i);
                String title = cursor.getString(titleColumnIndex);
                String synopsis = cursor.getString(synopsisColumnIndex);
                String release = cursor.getString(releaseColumnIndex);
                String rating = cursor.getString(ratingColumnIndex);
                String poster = cursor.getString(posterColumnIndex);
                String key = cursor.getString(keyColumnIndex);

                MoviesClass favouriteClass = new MoviesClass(title, synopsis, release, rating, poster, key);
                faveMovies.add(favouriteClass);
            }
            database.close();
        }
    }

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

    private List<MoviesClass> extractFeatureFromJson(String movieJSON) {
        List<MoviesClass> movieList = new ArrayList<>();
        if (TextUtils.isEmpty(movieJSON)) {
            return null;
        }
        try {
            JSONObject baseObject = new JSONObject(movieJSON);
            JSONArray moviesArray = baseObject.getJSONArray("results");
            for (int i = 0; i < moviesArray.length(); i++) {
                JSONObject responseObject = moviesArray.getJSONObject(i);
                String movieTitle = responseObject.getString("title");
                String releaseDate = responseObject.getString("release_date");
                String overview = responseObject.getString("overview");
                String ratings = responseObject.getString("vote_average");
                String poster = responseObject.getString("poster_path");
                String posterPath = "http://image.tmdb.org/t/p/w342/" + poster;
                String movieId = responseObject.getString("id");
                MoviesClass movies = new MoviesClass(movieTitle, overview, releaseDate, ratings, posterPath, movieId);
                Log.v(LOG_TAG, "extractFeatureFromJson " + movies.toString());
                movieList.add(movies);
            }

        } catch (JSONException e) {
            Log.e(LOG_TAG, "Problem parsing movie JSON results", e);
        }
        return movieList;
    }

    private class MovieSyncTask extends AsyncTask<String, Void, List<MoviesClass>> {

        @Override
        protected List<MoviesClass> doInBackground(String... strings) {
            if (BASE_URL == null) {
                BASE_URL = "https://api.themoviedb.org/3/movie/popular?";
            }
            String API_KEY = "api_key=XXXXXXXXXXXXXXXXXXXXXXXXXXXX";
            URL url = createUrl(BASE_URL + API_KEY);
            String jsonResponse = "";
            try {
                jsonResponse = makeHttpRequest(url);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return extractFeatureFromJson(jsonResponse);
        }

        @Override
        protected void onPostExecute(List<MoviesClass> movies) {
            if (movies == null) {
                emptyText.setText(R.string.offline);
                emptyImage.setImageResource(R.drawable.ic_movie_icon_15134);
                return;
            } else {
                emptyImage.setVisibility(View.GONE);
                emptyText.setVisibility(View.GONE);
            }
            updateUi(movies);
        }
    }

    private void updateUi(List<MoviesClass> movies) {
        emptyText.setText("");
        adapter.clear();
        adapter.addAll(movies);
        adapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.grid_view_sort, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        int id = menuItem.getItemId();
        if (id == R.id.action_sort_pop) {
            favouriteView = false;
            BASE_URL = "https://api.themoviedb.org/3/movie/popular?";
            MovieSyncTask popTask = new MovieSyncTask();
            popTask.execute();
            mMovieGrid.setAdapter(adapter);
            return true;
        }
        if (id == R.id.action_sort_rate) {
            favouriteView = false;
            BASE_URL = "https://api.themoviedb.org/3/movie/top_rated?";
            MovieSyncTask rateTask = new MovieSyncTask();
            adapter.clear();
            rateTask.execute();
            mMovieGrid.setAdapter(adapter);
            return true;
        }
        Cursor cursor = null;
        if (id == R.id.action_sort_fave) {
            favouriteView = true;
            FavouriteMoviesHelper mDbHelper = new FavouriteMoviesHelper(getApplicationContext());
            SQLiteDatabase database = mDbHelper.getReadableDatabase();
            String[] projection = {
                    MovieContract.MovieEntry._ID,
                    MovieContract.MovieEntry.COLUMN_MOVIE_KEY,
                    MovieContract.MovieEntry.COLUMN_MOVIE_POSTER,
                    MovieContract.MovieEntry.COLUMN_MOVIE_RATING,
                    MovieContract.MovieEntry.COLUMN_MOVIE_RELEASE_DATE,
                    MovieContract.MovieEntry.COLUMN_MOVIE_TITLE,
                    MovieContract.MovieEntry.COLUMN_MOVIE_SYNOPSIS};
            cursor = database.query(MovieContract.MovieEntry.TABLE_NAME, projection, null, null, null, null, null);
            cursor.moveToNext();

            adapter1 = new MoviesCursorAdapter(this, cursor);
            mMovieGrid.setAdapter(adapter1);
            faveMovies = new ArrayList<>();

            int titleColumnIndex = cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_TITLE);
            int synopsisColumnIndex = cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_SYNOPSIS);
            int releaseColumnIndex = cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_RELEASE_DATE);
            int ratingColumnIndex = cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_RATING);
            int posterColumnIndex = cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_POSTER);
            int keyColumnIndex = cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_KEY);

            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToPosition(i);
                String title = cursor.getString(titleColumnIndex);
                String synopsis = cursor.getString(synopsisColumnIndex);
                String release = cursor.getString(releaseColumnIndex);
                String rating = cursor.getString(ratingColumnIndex);
                String poster = cursor.getString(posterColumnIndex);
                String key = cursor.getString(keyColumnIndex);

                MoviesClass favouriteClass = new MoviesClass(title, synopsis, release, rating, poster, key);
                faveMovies.add(favouriteClass);
            }
            return true;
        }
        return super.onOptionsItemSelected(menuItem);

    }
}
