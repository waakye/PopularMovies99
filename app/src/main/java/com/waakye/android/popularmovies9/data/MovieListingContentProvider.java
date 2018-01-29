package com.waakye.android.popularmovies9.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.waakye.android.popularmovies9.data.MovieListingContract.MovieListingEntry;

import static com.waakye.android.popularmovies9.data.MovieListingContract.MovieListingEntry.TABLE_NAME;

/**
 * Created by lesterlie on 1/5/18.
 */

public class MovieListingContentProvider extends ContentProvider {

    public static final String LOG_TAG = MovieListingContentProvider.class.getSimpleName();

    // Define the final integer constants for the directory of favorites and a single favorite movie
    public static final int FAVORITES = 100;
    public static final int FAVORITE_WITH_ID = 101;

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private MovieListingDbHelper mOpenHelper;

    @Override
    public boolean onCreate(){
        mOpenHelper = new MovieListingDbHelper(getContext());
        return true;
    }

    public static UriMatcher buildUriMatcher(){
        String content = MovieListingContract.CONTENT_AUTHORITY;

        UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        matcher.addURI(content, MovieListingContract.PATH_FAVORITES, FAVORITES);
        matcher.addURI(content, MovieListingContract.PATH_FAVORITES + "/#", FAVORITE_WITH_ID);

        return matcher;
    }

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
//    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
//
//    // Static initializer.  This is run the first time anything is called from this class.
//    static {
//        // The calls to addURI() go here, for all of the content URI patterns that the provider
//        // should recognize.  All paths added to the UriMatcher have a corresponding code to
//        // return when a match is found.
//
//        // The content URI of the form "content://com.waakye.android.popularmovies9/favorites"
//        // will map to the integer code {@link #FAVORITES}. This URI is used to provide access to
//        // MULTIPLE rows of the favorites table.
//        sUriMatcher.addURI(MovieListingContract.CONTENT_AUTHORITY,
//                MovieListingContract.PATH_FAVORITES, FAVORITES);
//
//        // The content URI of the form "content://com.waakye.android.popularmovies9/favorites/#"
//        // will map to the integer code {@link #FAVORITE_WITH_ID}.  This is used to provide access to
//        // ONE single row of the favorites table.
//        //
//        // In this case, the "#" wildcard is used where "#" can be substituted for an integer.
//        // For example, "content://com.waakye.android.popularmovies9/favorites/3" matches, but
//        // "content://com.waakye.android.popularmovies9/favorites" does not match.
//        sUriMatcher.addURI(MovieListingContract.CONTENT_AUTHORITY,
//                MovieListingContract.PATH_FAVORITES + "/#", FAVORITE_WITH_ID);
//    }
//
//    // Member variable for a MovieListingDbHelper that's initialized in the onCreate() method
//    private MovieListingDbHelper mMovieListingDbHelper;
//
//    @Override
//    public boolean onCreate() {
//        Context context = getContext();
//        mMovieListingDbHelper = new MovieListingDbHelper(context);
//        return true;
//    }

    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)){
            case FAVORITES:
                return MovieListingEntry.CONTENT_TYPE;
            case FAVORITE_WITH_ID:
                return MovieListingEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    // Implement query to handle request for data by URI
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {

        // Get access to underlying database (read-only for query)
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        // This cursor will hold the result of the query
        Cursor retCursor;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);

        // Query for the favorites directory and write a default case
        switch(match){
            // Query for the favorites directory
            case FAVORITES:
                retCursor = db.query(TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case FAVORITE_WITH_ID:
                long _id = ContentUris.parseId(uri);
                retCursor = db.query(TABLE_NAME,
                        projection,
                        MovieListingEntry._ID + " =?",
                        new String[]{String.valueOf(_id)},
                        null,
                        null,
                        sortOrder
                );
                break;
            // Default exception
            default:
                throw new UnsupportedOperationException("Cannot query unknown URI: " + uri);
        }

        // Set a notification URI on the Cursor and return that Cursor
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);

        return retCursor;

    }

    // Implement insert to handle requests to insert a single new row of data
    @Override
    public Uri insert(Uri uri, ContentValues values) {

        // Get access to the favorite database (to write new data to)
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        long _id;
        Uri returnUri; // URI to be returned

        // Write URI matching code to identify the match for the favorites directory
        switch (sUriMatcher.match(uri)){
            case FAVORITES:
                // Inserting values into favorites table
                _id = db.insert(TABLE_NAME, null, values);
                if ( _id > 0 ){
                    returnUri = MovieListingEntry.buildFavoritesUti(_id);
                } else {
                    throw new UnsupportedOperationException("Unable to insert rows into " + uri);
                }
                break;
            default:
                // Default case throws an UnsupportedOperationException
                throw new UnsupportedOperationException("insert Unknown uri: " + uri);
        }

        // Notify the resolver if uri has been changed, and return the newly inserted URI
        getContext().getContentResolver().notifyChange(uri, null);

        // Return constructed uri (this points to the newly inserted row of data)
        return returnUri;
    }

    /**
     * Deletes data at given URI with optional arguments for more fine tuned deletions
     *
     * @param uri           The full URI to query
     * @param selection     An option restriction to apply to rows when deleting
     * @param selectionArgs Used in conjunction with the selection statement
     * @return              The number of rows deleted
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        // Get access to the database and write URI matching code to recognize a single item
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int rows; // Number of rows affected

        // Use selections to delete a favorite movie by its row ID
        switch(sUriMatcher.match(uri)){
            case FAVORITES:
                rows = db.delete(MovieListingEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Because null could delete all rows:
        if(selection == null || rows != 0){
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rows;
    }
//    public int delete(Uri uri, String selection, String[] selectionArgs){
//
//        // Get a writable database
//        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
//
//        int rowsDeleted;
//
//        final int match = sUriMatcher.match(uri);
//        switch (match) {
//            case FAVORITES:
//                rowsDeleted = db.delete(MovieListingEntry.TABLE_NAME, selection, selectionArgs);
//                break;
//            case FAVORITE_WITH_ID:
//                selection = MovieListingEntry._ID + "=?";
//                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
//                rowsDeleted = db.delete(MovieListingEntry.TABLE_NAME, selection, selectionArgs);
//                break;
//            default:
//                throw new IllegalArgumentException("Deletion is not supported for " + uri);
//        }
//
//        if(rowsDeleted != 0){
//            getContext().getContentResolver().notifyChange(uri, null);
//        }
//
//        return rowsDeleted;
//    }


    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {

        throw new UnsupportedOperationException("Not yet implemented");
    }
}
