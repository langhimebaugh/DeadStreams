/**
 * RandomSongListActivity.java
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
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.RemoteException;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;
import com.himebaugh.deadstreams.R;
import com.himebaugh.deadstreams.player.LoadSongsJobService;
import com.himebaugh.deadstreams.player.MusicLibrary;
import com.himebaugh.deadstreams.player.MusicService;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;


public class RandomSongListActivity extends AppCompatActivity implements SongListAdapter.OnClickHandler, SongListAdapter.OnLongClickHandler, NavigationView.OnNavigationItemSelectedListener, SwipeRefreshLayout.OnRefreshListener {


    private static final String TAG = RandomSongListActivity.class.getSimpleName();

    /* PREFS */
    private static final String PREF_SWIPE_REFRESH_INITIALIZED = "pref_swipe_refreshed";

    private static final String KEY_INSTANCE_STATE_SHOW_ID = "state_show_id";
    private static final String KEY_INSTANCE_STATE_SHOW_IDENTIFIER = "state_show_identifier";
    private static final String KEY_INSTANCE_STATE_SHOW_DATE = "state_show_date";
    private static final String KEY_INSTANCE_STATE_SHOW_VENUE = "state_show_venue";
    private static final String KEY_INSTANCE_STATE_SHOW_CITY = "state_show_city";
    private static final String KEY_INSTANCE_STATE_SHOW_STATE = "state_show_state";
    private static final String KEY_INSTANCE_STATE_RV_POSITION = "key_instance_state_rv_position";
    private static final String KEY_INSTANCE_STATE_SWIPE_REFRESH_INITIALIZED = "key_instance_state_swipe_refreshed";

    private static final int DEFAULT_SHOW_ID = -1;
    private int mShowId = DEFAULT_SHOW_ID;

    // Used to show swipe instructions for loading random songs
    private boolean mSwipeRefreshInitialized = false;

    private String mShowIdentifier;
    private String mShowDate;
    private String mShowVenue;
    private String mShowCity;
    private String mShowState;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private SwipeRefreshLayout mEmptySwipeRefreshLayout;
    private SongListAdapter mSongListAdapter;
    private LinearLayoutManager mLayoutManager;
    private Parcelable mLayoutManagerSavedState;
    private MediaMetadataCompat mCurrentMetadata;
    private MediaBrowserCompat mMediaBrowser;

    private RandomSongListViewModel mSongListViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_random_play);
        // setContentView(R.layout.activity_song_list);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

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
            Intent intent = getIntent();
            if (intent != null && intent.hasExtra(ShowListActivity.EXTRA_SHOW_ID)) {
                mShowId = 999999;
                // mShowIdentifier = intent.getStringExtra(ShowListActivity.EXTRA_SHOW_IDENTIFIER);
                mShowIdentifier = "RANDOM";
                mShowDate = "";
                mShowVenue = "Random Playlist";
                mShowCity = "";
                mShowState = "";
                String showString = " ";
                mShowInfo.setText(showString);
                setTitle(mShowVenue);
            }
        } else {
            mShowId = 999999;
            // mShowIdentifier = savedInstanceState.getString(KEY_INSTANCE_STATE_SHOW_IDENTIFIER);
            mShowIdentifier = "RANDOM";
            mShowDate = "";
            mShowVenue = "Random Playlist";
            mShowCity = "";
            mShowState = "";
            String showString = " ";
            mShowInfo.setText(showString);
            setTitle(mShowVenue);
            // THANKS ROWLAND
            mLayoutManagerSavedState = savedInstanceState.getParcelable(KEY_INSTANCE_STATE_RV_POSITION);
        }

        mLayoutManager = new LinearLayoutManager(this);

        mSwipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        mEmptySwipeRefreshLayout = findViewById(R.id.empty_swipe_refresh_layout);
        // mInitialSwipeRefreshLayout = findViewById(R.id.initial_swipe_refresh_layout);

        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(mLayoutManager);  // getActivity()
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        // Initialize the adapter and attach it to the RecyclerView
        mSongListAdapter = new SongListAdapter(this, this, this);
        mRecyclerView.setAdapter(mSongListAdapter);

        mSwipeRefreshLayout.setOnRefreshListener(this);
        mEmptySwipeRefreshLayout.setOnRefreshListener(this);

        SharedPreferences sharedPreferences = android.preference.PreferenceManager.getDefaultSharedPreferences(this);
        mSwipeRefreshInitialized = sharedPreferences.getBoolean(PREF_SWIPE_REFRESH_INITIALIZED, false);

        setupViewModel();

        mMediaBrowser = new MediaBrowserCompat(this, new ComponentName(this, MusicService.class), mConnectionCallback, null);

        // loads the first time...
        if (!mSwipeRefreshInitialized) {
            initializeIt();
        }
    }

    @Override
    public void onRefresh() {
        mSwipeRefreshLayout.setRefreshing(true);

        // YES IT WORKS!!!!!!!!!
        // as seen here: https://github.com/wangxujie/MediaBrowserStudy/blob/521391101f83e7e39d75f1604a28afc09f997c67/app/src/main/java/com/wangx/mediabrowserstudy/MediaListFragment.java
        mMediaBrowser.unsubscribe(mMediaBrowser.getRoot());
        mMediaBrowser.subscribe(mMediaBrowser.getRoot(), mSubscriptionCallback);

        // get new songs...
        LoadSongsJobService.startActionLoadRandomSongs(this);
    }

    private void initializeIt() {
        mSwipeRefreshLayout.setRefreshing(true);

        // get new songs...
        LoadSongsJobService.startActionLoadRandomSongs(this);
    }

    private void setupViewModel() {

        // SongListViewModel songListViewModel
        mSongListViewModel = ViewModelProviders.of(this).get(RandomSongListViewModel.class);

        // songListViewModel.loadSongsByIdentifierLiveData(mShowIdentifier).observe(this, songList -> {
        mSongListViewModel.loadRandomSongsLiveData().observe(this, songList -> {

            // Show the list or the loading screen based on whether the data exists and is loaded
            if (songList.size() != 0) {
                showList();
            } else {
                showEmptyList();
            }

            boolean loaded = MusicLibrary.loadSongList(songList);

            if (!mSwipeRefreshInitialized && loaded) {
                // Save the last Show Id
                SharedPreferences sharedPreferences = android.preference.PreferenceManager.getDefaultSharedPreferences(this);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(PREF_SWIPE_REFRESH_INITIALIZED, true);
                editor.apply();
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

            // mMediaBrowser.unsubscribe(mMediaBrowser.getRoot());

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

    private void showEmptyList() {
        mSwipeRefreshLayout.setVisibility(View.GONE);
        mEmptySwipeRefreshLayout.setVisibility(View.VISIBLE);
    }

    private void showList() {
        mEmptySwipeRefreshLayout.setVisibility(View.GONE);
        mSwipeRefreshLayout.setVisibility(View.VISIBLE);
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

        outState.putBoolean(KEY_INSTANCE_STATE_SWIPE_REFRESH_INITIALIZED, mSwipeRefreshInitialized);

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

        mSwipeRefreshInitialized = savedInstanceState.getBoolean(KEY_INSTANCE_STATE_SWIPE_REFRESH_INITIALIZED);
    }

    // OK
    private final MediaBrowserCompat.ConnectionCallback mConnectionCallback =
            new MediaBrowserCompat.ConnectionCallback() {
                @Override
                public void onConnected() {

                    mMediaBrowser.subscribe(mMediaBrowser.getRoot(), mSubscriptionCallback);

                    try {
                        MediaControllerCompat mediaController = new MediaControllerCompat(RandomSongListActivity.this, mMediaBrowser.getSessionToken());

                        updateMetadata(mediaController.getMetadata());
                        mediaController.registerCallback(mMediaControllerCallback);
                        MediaControllerCompat.setMediaController(RandomSongListActivity.this, mediaController);
                    } catch (RemoteException e) {
                        throw new RuntimeException(e);
                    }
                }
            };

    // OK
    private final MediaControllerCompat.Callback mMediaControllerCallback =
            new MediaControllerCompat.Callback() {
                @Override
                public void onMetadataChanged(MediaMetadataCompat metadata) {

                    updateMetadata(metadata);
                    //mSongListAdapter.setCurrentMediaMetadata(metadata);
                    mSongListAdapter.notifyDataSetChanged();
                }

                @Override
                public void onPlaybackStateChanged(PlaybackStateCompat state) {

                    mSongListAdapter.notifyDataSetChanged();  // To update the Song List Icon @+id/play_eq
                }

                @Override
                public void onSessionDestroyed() {

                    //mSongListAdapter.notifyDataSetChanged();
                }
            };

    // OK
    private final MediaBrowserCompat.SubscriptionCallback mSubscriptionCallback = new MediaBrowserCompat.SubscriptionCallback() {
        @Override
        public void onChildrenLoaded(String parentId, List<MediaBrowserCompat.MediaItem> mediaItemList) {

            mSongListAdapter.clear();
            mSongListAdapter.addAll(mediaItemList);
            mSongListAdapter.notifyDataSetChanged();

            mSwipeRefreshLayout.setRefreshing(false);

            if (mLayoutManagerSavedState != null) {
                mLayoutManager.onRestoreInstanceState(mLayoutManagerSavedState);
            }
        }
    };

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (id) {
            case R.id.nav_shows:                            // ALL SHOWS
                startActivity(new Intent(this, ShowListActivity.class));
                break;
            case R.id.nav_favorites:                        // FAVORITE SHOWS
                startActivity(new Intent(this, FavoritesActivity.class));
                break;
            case R.id.nav_playlists:                        // CUSTOM PLAYLIST
                startActivity(new Intent(this, PlaylistListActivity.class));
                break;
            case R.id.nav_random:                           // RANDOM PLAY
                // startActivity(new Intent(this, RandomSongListActivity.class));
                break;
            case R.id.nav_settings:                         // SETTINGS
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            case R.id.nav_about:                            // ABOUT
                startActivity(new Intent(this, AboutActivity.class));
                break;
            default:
                //
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

}
