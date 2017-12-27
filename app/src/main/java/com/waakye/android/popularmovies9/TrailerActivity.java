package com.waakye.android.popularmovies9;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.waakye.android.popularmovies9.utilities.MovieDbJsonUtils;
import com.waakye.android.popularmovies9.utilities.NetworkUtils;

import org.json.JSONException;

import java.io.IOException;
import java.net.URL;

/**
 * Created by lesterlie on 12/27/17.
 */

/**
 * Things to do with the movie id:
 * 1) Get all the trailers related to this movieId
 * 2) Show the trailers as a list in a RecyclerView or ListView
 * 3) When a user clicks an item in the recyclerView or list, then show the trailer using an intent
 */

public class TrailerActivity extends AppCompatActivity implements TrailerAdapter.ListItemClickListener {

    public static String LOG_TAG = TrailerActivity.class.getSimpleName();

    private TextView mTrailerResultsTextView;

    private String movieId;

    // TextView to display the error message
    private TextView mTrailerActivityErrorMessageDisplay;

    // Loading Indicator
    private ProgressBar mTrailerActivityLoadingIndicator;

    private static final int NUM_LIST_ITEMS = 100;

    private TrailerAdapter mAdapter;

    private RecyclerView mTrailersList;

    protected String[] simplerJsonTrailerData;

    private Toast mToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trailer);

        Intent intent = getIntent();

        movieId = intent.getStringExtra("movieId");
        Log.i(LOG_TAG, "the movie id is " + movieId);

        mTrailersList = (RecyclerView)findViewById(R.id.recycler_view_trailers);

        mTrailerActivityErrorMessageDisplay = (TextView)findViewById(R.id.trailer_activity_text_view_error_message_display);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mTrailersList.setLayoutManager(layoutManager);

        mTrailersList.setHasFixedSize(true);

        mTrailersList.setAdapter(mAdapter);

        mAdapter = new TrailerAdapter(this, simplerJsonTrailerData, this);

        mTrailerActivityLoadingIndicator = (ProgressBar) findViewById(R.id.trailer_activity_progress_bar_loading_indicator);

        makeTrailerQuery(movieId);

    }

    private void makeTrailerQuery(String movieId){

        Log.i(LOG_TAG, "makeTrailerQuery() method called...");
        new TrailersQueryTask().execute(movieId);
    }

    /**
     * This method will make the View for the JSON data visible and hide the error message
     *
     * Since it is okay to redundantly set the visibility of a View, we don't need to check whether
     * each view is current visible or invisible
     */
    private void showJsonDataView(){
        Log.i(LOG_TAG, "showJsonDataView() method called...");
        // First, make sure the error is invisible
        mTrailerActivityErrorMessageDisplay.setVisibility(View.INVISIBLE);
        // Then, make sure the JSON is visible
        mTrailersList.setVisibility(View.VISIBLE);
    }

    /**
     * This method will make the error message visible and hide the JSON View.
     *
     * Since it is okay to redundantly set  the visiblity of a View, we don't need to check whether
     * each view is currently visible or invisible
     */
    private void showErrorMessage(){
        Log.i(LOG_TAG, "showErrorMessage() method called...");
        // First, hide the currently visible data
        mTrailersList.setVisibility(View.INVISIBLE);
        // Then, show the error
        mTrailerActivityErrorMessageDisplay.setVisibility(View.VISIBLE);
    }

    /**
     * This callback is invoked when you click on an item in the list.
     *
     * @param clickedItemIndex Index in the list of the item that was clicked.
     */
    @Override
    public void onListItemClick(int clickedItemIndex) {
        if (mToast != null) {
            mToast.cancel();
        }

        String toastMessage = "Item #" + clickedItemIndex + " clicked.";
        mToast = Toast.makeText(this, toastMessage, Toast.LENGTH_LONG);

        mToast.show();
    }

    public class TrailersQueryTask extends AsyncTask<String, Void, String[]> {

        // Override onPreExecute to set the loading indicator to visible
        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            mTrailerActivityLoadingIndicator.setVisibility(View.VISIBLE);
        }

        @Override
        protected String[] doInBackground(String... params) {
            Log.i(LOG_TAG, "TrailersQueryTask doInBackground() method called...");

            // If there's no search terms, then there's nothing to look up
            if(params.length == 0){
                return null;
            }

            URL trailerSearchUrl = NetworkUtils.createMovieTrailerUrl(movieId);

            try {
                String jsonTrailerResponse = NetworkUtils.getResponseFromHttpUrl(trailerSearchUrl);

                simplerJsonTrailerData = MovieDbJsonUtils
                        .getTrailerNameStringsFromJson(TrailerActivity.this, jsonTrailerResponse);
                return simplerJsonTrailerData;


            } catch (IOException e){
                e.printStackTrace();
                return null;
            } catch (JSONException e){
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String[] trailersSearchResults){
            Log.i(LOG_TAG, "TrailersQueryTask onPostExecute() method called...");
            // As soon as the loading is complete, hide the loading indicator
            mTrailerActivityLoadingIndicator.setVisibility(View.INVISIBLE);
            if(trailersSearchResults != null && !trailersSearchResults.equals("")){
                // Call showJsonDataView if we have valid, non-null results
                showJsonDataView();
                mAdapter.setTrailerData(simplerJsonTrailerData);
                mTrailersList.setAdapter(mAdapter);
            } else {
                // Call showErrorMessage if the result is null in onPostExecute
                showErrorMessage();
            }
        }
    }
}
