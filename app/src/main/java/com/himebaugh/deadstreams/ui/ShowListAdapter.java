/**
 * ShowListAdapter.java
 *
 * This file is part of
 * DeadStreams - An Android App to Stream Grateful Dead music concert/shows from the Internet Archive (archive.org).
 * Developed by Langdon Himebaugh for the Android Nanodegree Capstone-Project.
 *
 * Copyright (c) 2018-19 - Langdon Himebaugh
 * Licensed under the MIT-License
 * http://opensource.org/licenses/MIT
 */

package com.himebaugh.deadstreams.ui;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
//import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.himebaugh.deadstreams.R;
import com.himebaugh.deadstreams.database.Show;

import java.util.List;

public class ShowListAdapter extends RecyclerView.Adapter<ShowListAdapter.ItemViewHolder> {

    private static final String TAG = ShowListAdapter.class.getSimpleName();
    private List<Show> mShowList;

    // An on-click handler to make it easy for an Activity to interface with the RecyclerView
    private final OnClickHandler mClickHandler;

    // The interface that receives onClick messages
    public interface OnClickHandler {
        void onClick(@NonNull Show show);

        void onFavorite(@NonNull Show show);
    }

    public ShowListAdapter(@NonNull OnClickHandler clickHandler) {

        mClickHandler = clickHandler;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {

        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.show_list_item, viewGroup, false);

        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        Show show = mShowList.get(position);
        holder.showVenueTextView.setText(show.getVenue());
        holder.showDateTextView.setText(show.getDate());
        holder.showCityTextView.setText(show.getCity());
        holder.showStateTextView.setText(show.getState());  // String.valueOf(show.getTotalDownloads())

        if (show.getFavorite() == 1) {
            holder.showFavoriteImageView.setImageResource(R.drawable.ic_favorite_black_48dp);
            holder.showFavoriteImageView.setImageAlpha(150);
        } else {
            holder.showFavoriteImageView.setImageResource(R.drawable.ic_favorite_border_black_48dp);
            holder.showFavoriteImageView.setImageAlpha(100);
        }
    }

    @Override
    public int getItemCount() {
        if (mShowList == null) {
            return 0;
        }
        return mShowList.size();
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        final TextView showVenueTextView;
        final TextView showDateTextView;
        final TextView showCityTextView;
        final TextView showStateTextView;
        final ImageView showFavoriteImageView;

        ItemViewHolder(View itemView) {
            super(itemView);

            showVenueTextView = itemView.findViewById(R.id.tv_venue);
            showDateTextView = itemView.findViewById(R.id.tv_date);
            showCityTextView = itemView.findViewById(R.id.tv_city);
            showStateTextView = itemView.findViewById(R.id.tv_state);

            showFavoriteImageView = itemView.findViewById(R.id.favoriteImageView);

            itemView.setOnClickListener(this);
            showFavoriteImageView.setOnClickListener(this);
        }


        @Override
        public void onClick(View v) {

            int adapterPosition = getAdapterPosition();

            Show show = mShowList.get(adapterPosition);

            if (v.getId() == showFavoriteImageView.getId()) {
                mClickHandler.onFavorite(show);
            } else {
                mClickHandler.onClick(show);
            }
        }
    }

    public void loadShows(List<Show> showList) {

        mShowList = showList;
        notifyDataSetChanged();
    }

}