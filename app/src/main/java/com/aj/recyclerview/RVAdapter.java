package com.aj.recyclerview;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aj.foodcourt2.R;
import com.aj.queuing.QueuingDisplayObject;

import java.util.List;

/**
 * Created by Joost on 01/07/2015.
 */
public class RVAdapter extends RecyclerView.Adapter<ActivityViewHolder>{

    List<QueuingDisplayObject> queuingDisplayObjects;
    RecyclerViewClickListener recyclerViewClickListener;

    public RVAdapter(List<QueuingDisplayObject> queuingDisplayObjects, RecyclerViewClickListener recyclerViewClickListener){
        this.queuingDisplayObjects = queuingDisplayObjects;
        this.recyclerViewClickListener = recyclerViewClickListener;
    }


    @Override
    public ActivityViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_card, parent, false);
        ActivityViewHolder avh = new ActivityViewHolder(v, recyclerViewClickListener);
        return avh;
    }


    @Override
    public void onBindViewHolder(ActivityViewHolder holder, int position) {
        holder.tvTitle.setText(queuingDisplayObjects.get(position).getTitle());
        holder.tvTimeInfo.setText(queuingDisplayObjects.get(position).getTime());
        holder.tvActivityInfo.setText(queuingDisplayObjects.get(position).getInfo());
    }

    /**
     * Returns the total number of items in the data set hold by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        return queuingDisplayObjects.size();
    }

    /**
     * Called by RecyclerView when it starts observing this Adapter.
     * <p/>
     * Keep in mind that same adapter may be observed by multiple RecyclerViews.
     *
     * @param recyclerView The RecyclerView instance which started observing this adapter.
     * @see #onDetachedFromRecyclerView(RecyclerView)
     */
    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public void addObject(QueuingDisplayObject obj){
        queuingDisplayObjects.add(obj);
        notifyDataSetChanged();
    }

    public QueuingDisplayObject getLastObject(){
        return queuingDisplayObjects.get(queuingDisplayObjects.size()-1);
    }
}
