package com.aj.recyclerview;

import android.view.View;

/**
 * Created by Joost on 01/07/2015.
 */
public interface RecyclerViewClickListener {
    /**
     * Called when the view is clicked.
     *
     * @param v view that is clicked
     * @param position of the clicked item
     * @param isLongClick true if long click, false otherwise
     */
    public void onClick(View v, int position, boolean isLongClick);
}
