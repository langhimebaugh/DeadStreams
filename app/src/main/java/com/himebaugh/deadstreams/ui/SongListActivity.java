/**
 * SongListActivity.java
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
import com.himebaugh.deadstreams.player.MusicLibrary;
import com.himebaugh.deadstreams.player.MusicService;
import com.himebaugh.deadstreams.utils.DateUtils;

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


public class SongListActivity extends AppCompatActivity implements SongListAdapter.OnClickHandler, SongListAdapter.OnLongClickHandler {

    private final static String TAG = SongListActivity.class.getSimpleName();

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
            if (intent != null && intent.hasExtra(ShowListActivity.EXTRA_ORIGIN)) {

                String origin = intent.getStringExtra(ShowListActivity.EXTRA_ORIGIN);

                if (origin.equals("FavoritesActivity")) {
                    // If started from FavoritesActivity then redirect back to there.
                    startActivity(new Intent(this, FavoritesActivity.class));
                } else {
                    startActivity(new Intent(this, ShowListActivity.class));
                }
            }
        }

        // part of the CollapsingToolbarLayout...
        TextView mShowInfo = findViewById(R.id.show_info);

        if (savedInstanceState == null) {
            Log.i(TAG, "onCreate: savedInstanceState == null");
            Intent intent = getIntent();
            if (intent != null && intent.hasExtra(ShowListActivity.EXTRA_SHOW_ID)) {
                mShowId = intent.getIntExtra(ShowListActivity.EXTRA_SHOW_ID, DEFAULT_SHOW_ID);
                mShowIdentifier = intent.getStringExtra(ShowListActivity.EXTRA_SHOW_IDENTIFIER);
                mShowDate = intent.getStringExtra(ShowListActivity.EXTRA_SHOW_DATE);
                mShowVenue = intent.getStringExtra(ShowListActivity.EXTRA_SHOW_VENUE);
                mShowCity = intent.getStringExtra(ShowListActivity.EXTRA_SHOW_CITY);
                mShowState = intent.getStringExtra(ShowListActivity.EXTRA_SHOW_STATE);
                String showString = DateUtils.parseShowDate(mShowDate) + " - " + mShowCity + " " + mShowState;
                mShowInfo.setText(showString);
                setTitle(mShowVenue);
            }
        } else {
            mShowId = savedInstanceState.getInt(KEY_INSTANCE_STATE_SHOW_ID);
            mShowIdentifier = savedInstanceState.getString(KEY_INSTANCE_STATE_SHOW_IDENTIFIER);
            mShowDate = savedInstanceState.getString(KEY_INSTANCE_STATE_SHOW_DATE);
            mShowVenue = savedInstanceState.getString(KEY_INSTANCE_STATE_SHOW_VENUE);
            mShowCity = savedInstanceState.getString(KEY_INSTANCE_STATE_SHOW_CITY);
            mShowState = savedInstanceState.getString(KEY_INSTANCE_STATE_SHOW_STATE);
            String showString = DateUtils.parseShowDate(mShowDate) + " - " + mShowCity + " " + mShowState;
            mShowInfo.setText(showString);
            setTitle(mShowVenue);
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

        // ADD TO PLAYLIST ???

        Log.i(TAG, "onLongClick: mediaItem.getMediaId()=" + mediaItem.getMediaId());

        Log.i(TAG, "onLongClick: title" + mediaItem.getDescription().getTitle().toString() );
        Log.i(TAG, "onLongClick: subtitle" + mediaItem.getDescription().getSubtitle().toString() );
        Log.i(TAG, "onLongClick: description" + mediaItem.getDescription().getDescription().toString() );

        Log.i(TAG, "onLongClick: media description" + mediaItem.getDescription().getMediaDescription().toString());

        Log.i(TAG, "onLongClick: MusicLibrary.getMusicFilename()=" + MusicLibrary.getMusicFilename(mediaItem.getMediaId()) );

        Log.i(TAG, "onLongClick: MusicLibrary.getSongID()=" + MusicLibrary.getSongID(mediaItem.getMediaId()) );

        // send song.id to playlist picker
        showPlaylistPicker( MusicLibrary.getSongID(mediaItem.getMediaId()) );

    }

    private void showPlaylistPicker(int songId) {
        PlaylistPickerDialog picker = PlaylistPickerDialog.newInstance();
        // picker.setListener(playlist -> PlaylistsUtils.addSongToPlaylist(playlist.getId(), songId ));

        picker.setListener(playlist -> addSongToPlaylistTest(playlist.getId(), songId ));

        picker.show( getSupportFragmentManager() , "pick_playlist");
    }

    private void addSongToPlaylistTest(long playlistId, long songId) {

        PlaylistSongsListViewModel playListSongsViewModel = ViewModelProviders.of(this).get(PlaylistSongsListViewModel.class);

        playListSongsViewModel.addSongToPlaylist(playlistId, songId);

        // mViewModel = ViewModelProviders.of(this).get(CategorySelectDialogViewModel.class);
        //mViewModel.updateSelected(mArgCategory);


        Log.i(TAG, "TEST addSongToPlaylist: playlistId = " + playlistId);

        Log.i(TAG, "TEST addSongToPlaylist: songId = " + songId);

//        Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId);
//        final int base = getSongCount(resolver, uri);
//        insert(resolver, uri, songId, base + 1);

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

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mShowId = savedInstanceState.getInt(KEY_INSTANCE_STATE_SHOW_ID);
        mShowIdentifier = savedInstanceState.getString(KEY_INSTANCE_STATE_SHOW_IDENTIFIER);
        mShowDate = savedInstanceState.getString(KEY_INSTANCE_STATE_SHOW_DATE);
        mShowVenue = savedInstanceState.getString(KEY_INSTANCE_STATE_SHOW_VENUE);
        mShowCity = savedInstanceState.getString(KEY_INSTANCE_STATE_SHOW_CITY);
        mShowState = savedInstanceState.getString(KEY_INSTANCE_STATE_SHOW_STATE);
    }

    private final MediaBrowserCompat.ConnectionCallback mConnectionCallback =
            new MediaBrowserCompat.ConnectionCallback() {
                @Override
                public void onConnected() {

                    mMediaBrowser.subscribe(mMediaBrowser.getRoot(), mSubscriptionCallback);
                    MediaSessionCompat.Token token = mMediaBrowser.getSessionToken();

                    try {
                        MediaControllerCompat mediaController = new MediaControllerCompat(SongListActivity.this, token);
                        updateMetadata(mediaController.getMetadata());
                        mediaController.registerCallback(mMediaControllerCallback);
                        MediaControllerCompat.setMediaController(SongListActivity.this, mediaController);
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
                // Because this is the "detail activity" for both ShowListActivity and FavoritesActivity
                // Only way to get the user back to the correct spot
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}