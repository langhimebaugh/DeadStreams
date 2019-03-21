/**
 * PlaylistSongsListActivity.java
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

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.RemoteException;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.himebaugh.deadstreams.R;
import com.himebaugh.deadstreams.database.PlaylistSong;
import com.himebaugh.deadstreams.database.Song;
import com.himebaugh.deadstreams.player.MusicLibrary;
import com.himebaugh.deadstreams.player.MusicService;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


public class PlaylistSongsListActivity extends AppCompatActivity implements SongListAdapter.OnClickHandler, SongListAdapter.OnLongClickHandler {

    private final static String TAG = PlaylistSongsListActivity.class.getSimpleName();

    private static final String KEY_INSTANCE_STATE_PLAYLIST_ID = "state_playlist_id";
    private static final String KEY_INSTANCE_STATE_PLAYLIST_NAME = "state_playlist_name";
    private static final String KEY_INSTANCE_STATE_RV_POSITION = "state_playlist_rv_position";

    private static final int DEFAULT_PLAYLIST_ID = 1;
    private int mPlaylistId = DEFAULT_PLAYLIST_ID;
    
    private String mPlaylistName;

    private RecyclerView mRecyclerView;
    private SongListAdapter mSongListAdapter;
    private LinearLayoutManager mLayoutManager;
    private Parcelable mLayoutManagerSavedState;
    private ProgressBar mLoadingIndicator;
    private MediaMetadataCompat mCurrentMetadata;
    private MediaBrowserCompat mMediaBrowser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_list);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Show the Up button in the action bar.
        // SET THE UP BUTTON TO THE BACK BUTTON SEE BELOW
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        if (findViewById(R.id.two_pane_detected) != null) {
            // This view will be present only in large-screen layouts (res/values-sw600dp).
            // If this view is present, then the activity should be in two-pane mode.
            // Handle case where user goes from ShowList in portrait mode to SongList in portrait mode and then rotates the device.
            // Redirect to ShowList and display two-pane mode.
            // Don't belong here, so leave....

            Intent intent = getIntent();
            if (intent != null && intent.hasExtra(PlaylistListActivity.EXTRA_ORIGIN)) {

                String origin = intent.getStringExtra(PlaylistListActivity.EXTRA_ORIGIN);

                if (origin.equals("FavoritesActivity")) {
                    // If started from FavoritesActivity then redirect back to there.
                    startActivity(new Intent(this, FavoritesActivity.class));
                } else {
                    startActivity(new Intent(this, PlaylistListActivity.class));
                }
            }
        }

        // part of the CollapsingToolbarLayout...
        TextView mShowInfo = findViewById(R.id.show_info);

        if (savedInstanceState == null) {
            Log.i(TAG, "onCreate: savedInstanceState == null");
            Intent intent = getIntent();
            if (intent != null && intent.hasExtra(PlaylistListActivity.EXTRA_PLAYLIST_ID)) {
                mPlaylistId = intent.getIntExtra(PlaylistListActivity.EXTRA_PLAYLIST_ID, DEFAULT_PLAYLIST_ID);
                mPlaylistName = intent.getStringExtra(PlaylistListActivity.EXTRA_PLAYLIST_NAME);
                mShowInfo.setText(mPlaylistName);
                // setTitle(mPlaylistName);
                setTitle("My Playlist");
            }
        } else {
            mPlaylistId = savedInstanceState.getInt(KEY_INSTANCE_STATE_PLAYLIST_ID);
            mPlaylistName = savedInstanceState.getString(KEY_INSTANCE_STATE_PLAYLIST_NAME);
            mShowInfo.setText(mPlaylistName);
            setTitle("My Playlist");
            // THANKS ROWLAND
            mLayoutManagerSavedState = savedInstanceState.getParcelable(KEY_INSTANCE_STATE_RV_POSITION);
        }

        // The ProgressBar that will indicate to the user that we are loading data.
        mLoadingIndicator = findViewById(R.id.pb_loading_indicator);

        mLayoutManager = new LinearLayoutManager(this);

        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(mLayoutManager);  // getActivity()
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        // Initialize the adapter and attach it to the RecyclerView
        mSongListAdapter = new SongListAdapter(this, this, this);
        mRecyclerView.setAdapter(mSongListAdapter);

        setupViewModel();

        mMediaBrowser = new MediaBrowserCompat(this, new ComponentName(this, MusicService.class), mConnectionCallback, null);
    }

    private void setupViewModel() {

        PlaylistSongsListViewModel playListSongsViewModel = ViewModelProviders.of(this).get(PlaylistSongsListViewModel.class);

        playListSongsViewModel.loadPlaylistByPlaylistId(mPlaylistId).observe(this, playlistSongs -> {

            List<Song> songList = convertToSongList(playlistSongs);

            boolean loaded = MusicLibrary.loadSongList(songList);

            if (loaded) {
                hideLoadingIndicator();
            }
            // Show the list or the loading screen based on whether the data exists and is loaded
            if (playlistSongs.size() != 0) {
                hideLoadingIndicator();
            } else {
                showLoadingIndicator();
            }
        });
    }

    private List<Song> convertToSongList(List<PlaylistSong> playlistSongs) {

        List<Song> songList = new ArrayList<>();

        for (PlaylistSong playlistSong : playlistSongs) {

            Song song = new Song();
            song.setAlbum(playlistSong.getSongAlbum());
            song.setName(playlistSong.getSongName());
            song.setTitle(playlistSong.getSongTitle());
            song.setTrack(playlistSong.getSongTrack());
            song.setLength(playlistSong.getSongLength());
            song.setBitrate(playlistSong.getSongBitrate());
            song.setCreator(playlistSong.getSongCreator());
            song.setFormat(playlistSong.getSongFormat());
            song.setIdentifier(playlistSong.getShowIdentifier());

            songList.add(song);
        }

        return songList;
    }

    @Override
    public void onStart() {
        super.onStart();

        mMediaBrowser.connect();
    }

    @Override
    public void onStop() {
        super.onStop();

        MediaControllerCompat controller = MediaControllerCompat.getMediaController(this);
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
            // Clicked to play
            MediaControllerCompat.getMediaController(this).getTransportControls().playFromMediaId(mediaItem.getMediaId(), null);
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
    protected void onSaveInstanceState(Bundle outState) {

        outState.putInt(KEY_INSTANCE_STATE_PLAYLIST_ID, mPlaylistId);
        outState.putString(KEY_INSTANCE_STATE_PLAYLIST_NAME, mPlaylistName);
        // THANKS ROWLAND
        outState.putParcelable(KEY_INSTANCE_STATE_RV_POSITION, mLayoutManager.onSaveInstanceState());

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mPlaylistId = savedInstanceState.getInt(KEY_INSTANCE_STATE_PLAYLIST_ID);
        mPlaylistName = savedInstanceState.getString(KEY_INSTANCE_STATE_PLAYLIST_NAME);
    }

    private final MediaBrowserCompat.ConnectionCallback mConnectionCallback =
            new MediaBrowserCompat.ConnectionCallback() {
                @Override
                public void onConnected() {

                    mMediaBrowser.subscribe(mMediaBrowser.getRoot(), mSubscriptionCallback);
                    MediaSessionCompat.Token token = mMediaBrowser.getSessionToken();

                    try {
                        MediaControllerCompat mediaController = new MediaControllerCompat(PlaylistSongsListActivity.this, token);
                        updateMetadata(mediaController.getMetadata());
                        mediaController.registerCallback(mMediaControllerCallback);
                        MediaControllerCompat.setMediaController(PlaylistSongsListActivity.this, mediaController);
                    } catch (RemoteException e) {
                        throw new RuntimeException(e);
                    }
                }
            };

    private final MediaControllerCompat.Callback mMediaControllerCallback =
            new MediaControllerCompat.Callback() {
                @Override
                public void onMetadataChanged(MediaMetadataCompat metadata) {

                    updateMetadata(metadata);
                }

                @Override
                public void onPlaybackStateChanged(PlaybackStateCompat state) {

                    mSongListAdapter.notifyDataSetChanged();  // To update the Song List Icon @+id/play_eq
                }

                @Override
                public void onSessionDestroyed() {

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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // Over riding the UP button to equal the BACK button
                // Because this is the "detail activity" for both PlaylistListActivity and FavoritesActivity
                // Only way to get the user back to the correct spot
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}