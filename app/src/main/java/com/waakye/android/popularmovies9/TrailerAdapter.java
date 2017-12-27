package com.waakye.android.popularmovies9;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by lesterlie on 12/27/17.
 */

public class TrailerAdapter extends RecyclerView.Adapter<TrailerAdapter.TrailerViewHolder> {

    public static final String LOG_TAG = TrailerAdapter.class.getSimpleName();

    private String[] mTrailerData;

    /**
     * Constructor for TrailerAdapter that accepts a number of Trailer items to display and the
     * specification for the ListItemClickListener
     *
     * @param dataSet    String[] containing the data to populate views to be used by RecyclerView
     */
    public TrailerAdapter(String[] dataSet) { mTrailerData = dataSet;}


    /**
     * This gets called when each new ViewHolder is created.  This happens when the RecyclerView is
     * laid out. Once enough ViewHolders are created to fill the screen, then this method doesn't
     * need to be called anymore.
     * @param viewGroup     The ViewGroup that these ViewHolders are contained within.
     * @param viewType      If your RecyclerView has more than one type of item (which ours doesn't)
     *                      you can use this viewType integer to provide a different layout.  See
     *                      {@link android.support.v7.widget.RecyclerView.Adapter#getItemViewType(int)}
     *                      for more details.
     * @return              A new TrailerViewHolder that holds the View for each list item
     */
    @Override
    public TrailerViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        int layoutForListItem = R.layout.trailer_list_item;
        boolean shouldAttachToParentImmediately = false;

        // Create a new view
        View view = inflater.inflate(layoutForListItem, viewGroup, shouldAttachToParentImmediately);
        TrailerViewHolder viewHolder = new TrailerViewHolder(view);

        return viewHolder;
    }

    /**
     * OnBindViewHolder is called by the RecyclerView to display the data at the specified position.
     * In this method, we update the contents of the ViewHolder to display the correct indices in
     * the list for this particular position, using the 'position' argument that is conveniently
     * passed into us.
     *
     * @param viewHolder    The ViewHolder which should be updated to represent the contents of the
     *                      item at the given position in the data set
     * @param position  The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(TrailerViewHolder viewHolder, final int position) {
        Log.d(LOG_TAG, "#" + position);

        viewHolder.getTextView().setText(mTrailerData[position]);
    }

    /**
     * This method simply returns the number of items to display.  It is used behind the scenes to
     * help layout our Views and for animations.
     *
     * @return      The number of items available in our movie list
     */
    @Override
    public int getItemCount() {
        return mTrailerData.length;
    }


    /**
     * Cache of the children views for a list item
     */
    public class TrailerViewHolder extends RecyclerView.ViewHolder {

        // Will display the position in the list
        TextView listItemTrailerView;

        /**
         * Constructor for our ViewHolder.  Within this constructor, we get a reference to the
         * TextViews and set an onClickListener to listen for clicks.  Those will be handled by the
         * onClick method below
         * @param itemView  The View that you inflated in
         *                  {@link TrailerAdapter#onCreateViewHolder(ViewGroup, int)}
         */
        public TrailerViewHolder(View itemView){
            super(itemView);

            // Define click listener for the ViewHolder's view
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.i(LOG_TAG, "Element " + getAdapterPosition() + " clicked.");
                }
            });

            listItemTrailerView = (TextView) itemView.findViewById(R.id.text_view_item_trailer);
        }

        public TextView getTextView() {
            return listItemTrailerView;
        }

        /**
         * Create a convenience method that takes an integer as input and use that integer to
         * display the appropriate text within a list item.
         *
         * @param listIndex Position of the item in the list
         */
        public void bind(int listIndex) {
            listItemTrailerView.setText(String.valueOf(listIndex));
        }
    }

    public void setTrailerData(String[] trailerData) {
        Log.i(LOG_TAG, "setTrailerData() method is called...");
        mTrailerData = trailerData;
        notifyDataSetChanged();
    }
}
