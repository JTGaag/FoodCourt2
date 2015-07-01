package com.aj.recyclerview;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.aj.foodcourt2.R;

/**
 * Created by Joost on 01/07/2015.
 */
public class ActivityViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener{

    CardView cv;
    TextView tvTitle, tvTimeInfo, tvActivityInfo;

    private RecyclerViewClickListener recyclerViewClickListener;

    public ActivityViewHolder(View itemView, RecyclerViewClickListener recyclerViewClickListener) {
        super(itemView);
        this.recyclerViewClickListener = recyclerViewClickListener;
        cv = (CardView)itemView.findViewById(R.id.card_view);
        tvTitle = (TextView)itemView.findViewById(R.id.card_title);
        tvTimeInfo = (TextView)itemView.findViewById(R.id.card_time_info);
        tvActivityInfo = (TextView)itemView.findViewById(R.id.card_activity_info);
        itemView.setOnClickListener(this);
        itemView.setOnLongClickListener(this);
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        recyclerViewClickListener.onClick(v, getPosition(), false);
    }

    /**
     * Called when a view has been clicked and held.
     *
     * @param v The view that was clicked and held.
     * @return true if the callback consumed the long click, false otherwise.
     */
    @Override
    public boolean onLongClick(View v) {
        recyclerViewClickListener.onClick(v, getPosition(), true);
        return true;
    }
}
