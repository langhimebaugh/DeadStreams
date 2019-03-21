/**
 * PlaylistListAdapter.java
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

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.himebaugh.deadstreams.R;
import com.himebaugh.deadstreams.database.Playlist;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


public class PlaylistListAdapter extends RecyclerView.Adapter<PlaylistListAdapter.ItemViewHolder> {

    private static final String TAG = PlaylistListAdapter.class.getSimpleName();
    private List<Playlist> mPlaylist;

    // An on-click handler to make it easy for an Activity to interface with the RecyclerView
    private final OnClickHandler mClickHandler;

    // The interface that receives onClick messages
    public interface OnClickHandler {
        void onClick(@NonNull Playlist playlist);
    }

    public PlaylistListAdapter(@NonNull OnClickHandler clickHandler) {

        mClickHandler = clickHandler;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {

        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.playlist_list_item, viewGroup, false);

        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        Playlist playlist = mPlaylist.get(position);
        holder.playlistNameTextView.setText(playlist.getPlaylistName());
    }

    @Override
    public int getItemCount() {
        if (mPlaylist == null) {
            return 0;
        }
        return mPlaylist.size();
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        final TextView playlistNameTextView;

        ItemViewHolder(View itemView) {
            super(itemView);

            playlistNameTextView = itemView.findViewById(R.id.tv_playlist_name);
            itemView.setOnClickListener(this);
        }


        @Override
        public void onClick(View v) {

            int adapterPosition = getAdapterPosition();

            Playlist playlist = mPlaylist.get(adapterPosition);

            mClickHandler.onClick(playlist);
        }
    }

    public void loadShows(List<Playlist> playlist) {

        mPlaylist = playlist;
        notifyDataSetChanged();
    }

}