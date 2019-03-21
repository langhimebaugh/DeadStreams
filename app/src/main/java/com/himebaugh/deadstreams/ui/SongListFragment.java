/**
 * SongListFragment.java
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

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import android.content.ComponentName;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.RemoteException;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
//import androidx.core.app.Fragment;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
//import android.support.v7.widget.DefaultItemAnimator;
//import android.support.v7.widget.DividerItemDecoration;
//import android.support.v7.widget.LinearLayoutManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.himebaugh.deadstreams.R;
import com.himebaugh.deadstreams.player.MusicLibrary;
import com.himebaugh.deadstreams.player.MusicService;
import com.himebaugh.deadstreams.utils.DateUtils;

import java.util.List;


public class SongListFragment extends Fragment implements SongListAdapter.OnClickHandler, SongListAdapter.OnLongClickHandler {

    private final static String TAG = SongListFragment.class.getName();

    private static final String KEY_INSTANCE_STATE_SHOW_ID = "state_show_id";
    private static final String KEY_INSTANCE_STATE_SHOW_IDENTIFIER = "state_show_identifier";
    private static final String KEY_INSTANCE_STATE_SHOW_DATE = "state_show_date";
    private static final String KEY_INSTANCE_STATE_SHOW_VENUE = "state_show_venue";
    private static final String KEY_INSTANCE_STATE_SHOW_CITY = "state_show_city";
    private static final String KEY_INSTANCE_STATE_SHOW_STATE = "state_show_state";
    private static final String KEY_INSTANCE_STATE_RV_POSITION = "key_instance_state_rv_position";

    private static final int DEFAULT_SHOW_ID = -1;
    private int mShowId = DEFAULT_SHOW_ID;

    private String mShowIdentifier;
    private String mShowDate;
    private String mShowVenue;
    private String mShowCity;
    private String mShowState;
    private RecyclerView mRecyclerView;
    private SongListAdapter mSongListAdapter;
    private LinearLayoutManager mLayoutManager;
    private Parcelable mLayoutManagerSavedState;
    private ProgressBar mLoadingIndicator;
    private MediaMetadataCompat mCurrentMetadata;
    private MediaBrowserCompat mMediaBrowser;


    public static SongListFragment newInstance(int mShowId, String mShowIdentifier, String mShowDate, String mShowVenue, String mShowCity, String mShowState) {

        Bundle arguments = new Bundle();
        arguments.putInt(KEY_INSTANCE_STATE_SHOW_ID, mShowId);
        arguments.putString(KEY_INSTANCE_STATE_SHOW_IDENTIFIER, mShowIdentifier);
        arguments.putString(KEY_INSTANCE_STATE_SHOW_DATE, mShowDate);
        arguments.putString(KEY_INSTANCE_STATE_SHOW_VENUE, mShowVenue);
        arguments.putString(KEY_INSTANCE_STATE_SHOW_CITY, mShowCity);
        arguments.putString(KEY_INSTANCE_STATE_SHOW_STATE, mShowState);

        SongListFragment fragment = new SongListFragment();
        fragment.setArguments(arguments);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null && getArguments().containsKey(KEY_INSTANCE_STATE_SHOW_ID)) {
            mShowId = getArguments().getInt(KEY_INSTANCE_STATE_SHOW_ID);
            mShowIdentifier = getArguments().getString(KEY_INSTANCE_STATE_SHOW_IDENTIFIER);
            mShowDate = getArguments().getString(KEY_INSTANCE_STATE_SHOW_DATE);
            mShowVenue = getArguments().getString(KEY_INSTANCE_STATE_SHOW_VENUE);
            mShowCity = getArguments().getString(KEY_INSTANCE_STATE_SHOW_CITY);
            mShowState = getArguments().getString(KEY_INSTANCE_STATE_SHOW_STATE);
        }

        setHasOptionsMenu(true);

        mMediaBrowser = new MediaBrowserCompat(getActivity(), new ComponentName(getActivity(), MusicService.class), mConnectionCallback, null);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_song_list, container, false);

        // part of the CollapsingToolbarLayout...
        TextView showInfo = rootView.findViewById(R.id.show_info);
        TextView venue = rootView.findViewById(R.id.venue);

        if (savedInstanceState != null) {
            mShowId = savedInstanceState.getInt(KEY_INSTANCE_STATE_SHOW_ID);
            mShowIdentifier = savedInstanceState.getString(KEY_INSTANCE_STATE_SHOW_IDENTIFIER);
            mShowDate = savedInstanceState.getString(KEY_INSTANCE_STATE_SHOW_DATE);
            mShowVenue = savedInstanceState.getString(KEY_INSTANCE_STATE_SHOW_VENUE);
            mShowCity = savedInstanceState.getString(KEY_INSTANCE_STATE_SHOW_CITY);
            mShowState = savedInstanceState.getString(KEY_INSTANCE_STATE_SHOW_STATE);
            // THANKS ROWLAND
            mLayoutManagerSavedState = savedInstanceState.getParcelable(KEY_INSTANCE_STATE_RV_POSITION);
        }

        if (mShowDate != null && !mShowDate.isEmpty()) {
            venue.setText(mShowVenue);
            String showString = DateUtils.parseShowDate(mShowDate) + " - " + mShowCity + " " + mShowState;
            showInfo.setText(showString);
        }

        // The ProgressBar that will indicate to the user that we are loading data.
        mLoadingIndicator = rootView.findViewById(R.id.pb_loading_indicator);

        mLayoutManager = new LinearLayoutManager(getActivity());

        mRecyclerView = rootView.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(mLayoutManager);  // getActivity()
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        // Initialize the adapter and attach it to the RecyclerView
        mSongListAdapter = new SongListAdapter(getActivity(), this, this);
        mRecyclerView.setAdapter(mSongListAdapter);

        setupViewModel();

        return rootView;
    }

    private void setupViewModel() {

        SongListViewModel songListViewModel = ViewModelProviders.of(this).get(SongListViewModel.class);

        songListViewModel.loadSongsByIdentifier(mShowIdentifier).observe(this, songList -> {

            Boolean loaded = MusicLibrary.loadSongList(songList);

            if (loaded) {
                hideLoadingIndicator();
            }
            // Show the list or the loading screen based on whether the data exists and is loaded
            if (songList.size() != 0) {
                hideLoadingIndicator();
            } else {
                showLoadingIndicator();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();

        mMediaBrowser.connect();
    }

    @Override
    public void onStop() {
        super.onStop();

        MediaControllerCompat controller = MediaControllerCompat.getMediaController(getActivity());
        if (controller != null) {
            controller.unregisterCallback(mMediaControllerCallback);
        }
        if (mMediaBrowser != null && mMediaBrowser.isConnected()) {
            if (mCurrentMetadata != null) {
                mMediaBrowser.unsubscribe(mCurrentMetadata.getDescription().getMediaId());
            }
            mMediaBrowser.disconnect();
        }
    }

    private void updateMetadata(MediaMetadataCompat metadata) {
        mCurrentMetadata = metadata;
    }

    @Override
    public void onClick(@NonNull MediaBrowserCompat.MediaItem mediaItem) {

        if (mediaItem.isPlayable()) {
            MediaControllerCompat.getMediaController(getActivity()).getTransportControls().playFromMediaId(mediaItem.getMediaId(), null);
        }
    }

    @Override
    public void onLongClick(@NonNull MediaBrowserCompat.MediaItem mediaItem) {


        Log.i(TAG, "onLongClick: mediaItem.getMediaId()=" + mediaItem.getMediaId());

        Log.i(TAG, "onLongClick: title" + mediaItem.getDescription().getTitle().toString() );
        Log.i(TAG, "onLongClick: subtitle" + mediaItem.getDescription().getSubtitle().toString() );
        Log.i(TAG, "onLongClick: description" + mediaItem.getDescription().getDescription().toString() );

        Log.i(TAG, "onLongClick: media description" + mediaItem.getDescription().getMediaDescription().toString());

        Log.i(TAG, "onLongClick: MusicLibrary.getMusicFilename()=" + MusicLibrary.getMusicFilename(mediaItem.getMediaId()) );

        Log.i(TAG, "onLongClick: MusicLibrary.getSongID()=" + MusicLibrary.getSongID(mediaItem.getMediaId()) );
    }

    // Set the View for the data visible and hide the loading indicator.
    private void hideLoadingIndicator() {
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    // Set the loading indicator visible and hide the View.
    private void showLoadingIndicator() {
        mRecyclerView.setVisibility(View.INVISIBLE);
        mLoadingIndicator.setVisibility(View.VISIBLE);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {

        outState.putInt(KEY_INSTANCE_STATE_SHOW_ID, mShowId);
        outState.putString(KEY_INSTANCE_STATE_SHOW_IDENTIFIER, mShowIdentifier);
        outState.putString(KEY_INSTANCE_STATE_SHOW_DATE, mShowDate);
        outState.putString(KEY_INSTANCE_STATE_SHOW_VENUE, mShowVenue);
        outState.putString(KEY_INSTANCE_STATE_SHOW_CITY, mShowCity);
        outState.putString(KEY_INSTANCE_STATE_SHOW_STATE, mShowState);
        // THANKS ROWLAND
        outState.putParcelable(KEY_INSTANCE_STATE_RV_POSITION, mLayoutManager.onSaveInstanceState());

        super.onSaveInstanceState(outState);
    }

    private final MediaBrowserCompat.ConnectionCallback mConnectionCallback =
            new MediaBrowserCompat.ConnectionCallback() {
                @Override
                public void onConnected() {
                    mMediaBrowser.subscribe(mMediaBrowser.getRoot(), mSubscriptionCallback);
                    try {
                        MediaControllerCompat mediaController = new MediaControllerCompat(getActivity(), mMediaBrowser.getSessionToken());

                        updateMetadata(mediaController.getMetadata());
                        mediaController.registerCallback(mMediaControllerCallback);
                        MediaControllerCompat.setMediaController(getActivity(), mediaController);
                    } catch (RemoteException e) {
                        throw new RuntimeException(e);
                    }
                }
            };

    // Receive callbacks from the MediaController.
    // Here we update our state such as which queue is being shown, the current title and description and the PlaybackState.
    private final MediaControllerCompat.Callback mMediaControllerCallback =
            new MediaControllerCompat.Callback() {
                @Override
                public void onMetadataChanged(MediaMetadataCompat metadata) {

                    updateMetadata(metadata);
                    mSongListAdapter.notifyDataSetChanged();
                }

                @Override
                public void onPlaybackStateChanged(PlaybackStateCompat state) {

                    mSongListAdapter.notifyDataSetChanged();
                }

                @Override
                public void onSessionDestroyed() {

                    mSongListAdapter.notifyDataSetChanged();
                }
            };

    private final MediaBrowserCompat.SubscriptionCallback mSubscriptionCallback = new MediaBrowserCompat.SubscriptionCallback() {
        @Override
        public void onChildrenLoaded(String parentId, List<MediaBrowserCompat.MediaItem> mediaItemList) {

            mSongListAdapter.clear();
            mSongListAdapter.addAll(mediaItemList);
            mSongListAdapter.notifyDataSetChanged();

            if (mLayoutManagerSavedState != null) {
                mLayoutManager.onRestoreInstanceState(mLayoutManagerSavedState);
            }
        }
    };

}
