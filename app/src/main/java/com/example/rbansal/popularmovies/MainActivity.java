package com.example.rbansal.popularmovies;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.Spinner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    //declare adapter globally so that it is sccessible to FetchWeatherTask
    private PicassoAdapter mPicassoAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/

        GridView gridView = (GridView) findViewById(R.id.grid_view);
        mPicassoAdapter = new PicassoAdapter(this,new ArrayList<Movie>());
        gridView.setAdapter(mPicassoAdapter);

    }
    private String getValue(String key) {
        String res;
        if(key.equals("POPULAR")) {
            res = "popular";
        }
        else {
            res = "top_rated";
        }
        return res;
    }
    private void updateMovies(String key) {
        FetchMovie movieTask = new FetchMovie();
        movieTask.execute(getValue(key));
    }
   /* @Override
    public void onStart() {
        super.onStart();
        updateMovies("TOP RATED");
    }*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem item = menu.findItem(R.id.spinner);
        final Spinner spinner = (Spinner) MenuItemCompat.getActionView(item);
        //spinner.setBackgroundColor(Color.parseColor("#FFFFFF"));
        spinner.setPopupBackgroundResource(R.drawable.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.sort, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        //binding a listener
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

                String catg = spinner.getSelectedItem().toString();
                updateMovies(catg);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {}

        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
       /* if (id == R.id.action_settings) {
            return true;
        }*/

        return super.onOptionsItemSelected(item);
    }

    //image adapter class

    public class FetchMovie extends AsyncTask<String, Void, Movie[]> {
        private final String LOG_TAG = FetchMovie.class.getSimpleName();
        //formatting movie data
        private Movie formatMovieData(Movie m) {
            final String BASE_POSTER_PATH = "http://image.tmdb.org/t/p/";
            final String POSTER_SIZE = "w185";
            m.imgUrl = BASE_POSTER_PATH + POSTER_SIZE + m.imgUrl;
            return m;
        }
        //method for extracting data from json string
        private Movie[] getMovieDataFromJson(String jsonStr)
        throws JSONException {
            //name of json items neede
            final String TITLE = "title";
            final String POSTER_PATH = "poster_path";
            final String PAGE_NO = "page";
            final String TOTAL_PAGES = "total_pages";
            final String TOTAL_RESULTS = "total_results";
            final String RESULTS = "results";
            JSONObject movieJson = new JSONObject(jsonStr);
            JSONArray resultArray = movieJson.getJSONArray(RESULTS);
            Movie[] resultMovies = new Movie[resultArray.length()];

            for(int i=0;i<resultArray.length();i++) {
                JSONObject movie = resultArray.getJSONObject(i);
                resultMovies[i] = formatMovieData(new Movie(movie.getString(POSTER_PATH), movie.getString(TITLE)));
            }

            return  resultMovies;
        }

        @Override
        protected Movie[] doInBackground(String... params) {
            //checking for parameteres
            if(params.length == 0)
            {
                return  null;
            }
            //making http request
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String movieJsonStr = null;
            //values of parametres to be passed in url
            String category = "popular";
            String api_key = "619641613cb9bf7bbfec1d8abbcc63da";
            int page = 1;

            try {
                //parametres to be passed in url
                String base_url = "http://api.themoviedb.org/3/movie/";
                final String API_KEY = "api_key" ;
                final String PAGE_PARAM = "page";
                //building final url
                base_url = base_url + params[0] + "?";
                Uri builtUri = Uri.parse(base_url).buildUpon()
                        .appendQueryParameter(API_KEY, api_key)
                        .appendQueryParameter(PAGE_PARAM, Integer.toString(page))
                        .build();
                URL url = new URL(builtUri.toString());
                // Create the request
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                movieJsonStr = buffer.toString();
            } catch (IOException e) {
                Log.e("PlaceholderFragment", "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            } finally{
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("PlaceholderFragment", "Error closing stream", e);
                    }
                }
            }
            try {
                return getMovieDataFromJson(movieJsonStr);

            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
            //this will happen only if there is an error ehile parsing data
            return null;
        }

        @Override
        protected void onPostExecute(Movie[] result) {
            if(result!=null) {
                mPicassoAdapter.clear();
                for(Movie temp : result) {
                    mPicassoAdapter.add(temp);
                }
            }
        }
    }


    }
