/**
 * PlaylistPickerDialog.java
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

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import com.himebaugh.deadstreams.R;
import com.himebaugh.deadstreams.database.Playlist;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


public class PlaylistPickerDialog extends DialogFragment implements PlaylistListAdapter.OnClickHandler {

    private static final String TAG = PlaylistPickerDialog.class.getSimpleName();

    private RecyclerView mRecyclerView;
    private PlaylistListAdapter mAdapter;
    private PlaylistListViewModel playListViewModel;

    private OnPlaylistPickedListener mListener;


    public static PlaylistPickerDialog newInstance() {

        return new PlaylistPickerDialog();
    }

    public PlaylistPickerDialog() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        @SuppressLint("InflateParams")
        View rootView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_playlist_picker, null);

        Toolbar toolbar = rootView.findViewById(R.id.dialog_playlist_picker_toolbar);
        toolbar.setTitle(R.string.choose_playlist);
        toolbar.setTitleTextColor(0xffffffff);

        mRecyclerView = rootView.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        // Initialize the adapter and attach it to the RecyclerView
        mAdapter = new PlaylistListAdapter(this);
        mRecyclerView.setAdapter(mAdapter);

        setupViewModel();

        Button newPlaylistButton = rootView.findViewById(R.id.new_playlist);
        // TODO: newPlaylistButton.setOnClickListener(mOnClickListener);

        builder.setView(rootView);

        return builder.create();
    }

    @Override
    public void onClick(@NonNull Playlist playlist) {

        if (mListener != null) {
            mListener.onPlaylistPicked(playlist);
        }

        dismiss();
    }

    public void setListener(OnPlaylistPickedListener listener) {
        mListener = listener;
    }

    public interface OnPlaylistPickedListener {
        void onPlaylistPicked(Playlist playlist);
    }

    private void setupViewModel() {

        playListViewModel = ViewModelProviders.of(this).get(PlaylistListViewModel.class);

        playListViewModel.loadAllPlaylistsLiveData().observe(this, playlists -> {

            mAdapter.loadShows(playlists);

            Log.i(TAG, "setupViewModel: playlists.size()=" + playlists.size());
        });
    }

}
