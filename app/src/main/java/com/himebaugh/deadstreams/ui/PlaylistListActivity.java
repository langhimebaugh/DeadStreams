/**
 * PlaylistListActivity.java
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

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.himebaugh.deadstreams.R;
import com.himebaugh.deadstreams.database.Playlist;
import com.himebaugh.deadstreams.utils.NetworkUtils;

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

public class PlaylistListActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, PlaylistListAdapter.OnClickHandler {

    private static final String TAG = PlaylistListActivity.class.getSimpleName();

    public static final String FRAGMENT_TAG = "ShowListActivity";

    public static final String EXTRA_PLAYLIST_ID = "extra_playlist_id"; // Extra for the show ID to be received in the intent
    public static final String EXTRA_PLAYLIST_NAME = "extra_playlist_name";
    public static final String EXTRA_ORIGIN = "extra_origin";

    private RecyclerView mRecyclerView;
    private PlaylistListAdapter mAdapter;
    private PlaylistListViewModel playListViewModel;

    private ProgressBar mLoadingIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_list);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // The ProgressBar that will indicate to the user that we are loading data.
        mLoadingIndicator = findViewById(R.id.pb_loading_indicator);

        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        // Initialize the adapter and attach it to the RecyclerView
        mAdapter = new PlaylistListAdapter(this);
        mRecyclerView.setAdapter(mAdapter);

        setupViewModel();
    }

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
                // startActivity(new Intent(this, PlaylistListActivity.class));
                break;
            case R.id.nav_random:                           // RANDOM PLAY
                // startActivity(new Intent(this, RandomPlayActivity.class));
                startActivity(new Intent(this, RandomSongListActivity.class));
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

    private void setupViewModel() {

        playListViewModel = ViewModelProviders.of(this).get(PlaylistListViewModel.class);

        playListViewModel.loadAllPlaylistsLiveData().observe(this, playlists -> {

            mAdapter.loadShows(playlists);

            Log.i(TAG, "setupViewModel: playlists.size()=" + playlists.size());

            // Show the list or the loading screen based on whether the data exists and is loaded
            if (playlists != null && playlists.size() != 0) hideLoadingIndicator();
            else showLoadingIndicator();
        });
    }


    @Override
    public void onClick(@NonNull Playlist playlist) {

        if (NetworkUtils.isNetworkAvailable(this)) {

            // check internet before moving forward.
            // avoids errors by not running LoadSongsJobService with internet down.

//            if (mTwoPane) {
//                // update fragment
//                getSupportFragmentManager().beginTransaction()
//                        .replace(R.id.song_list_container,
//                                SongListFragment.newInstance(show.getId(), show.getIdentifier(), show.getDate(), show.getVenue(), show.getCity(), show.getState()))
//                        .commitNow();
//            } else {
                //
                Intent intent = new Intent(this, PlaylistSongsListActivity.class);

                intent.putExtra(EXTRA_PLAYLIST_ID, playlist.getId());
                intent.putExtra(EXTRA_PLAYLIST_NAME, playlist.getPlaylistName());
                intent.putExtra(EXTRA_ORIGIN, "PlaylistListActivity");

                startActivity(intent);
//            }

        } else {
            // no internet
            // display notification here
            Log.i(TAG, "onClick: but internet down");
            Toast.makeText(this, R.string.message_check_internet, Toast.LENGTH_SHORT).show();
        }

    }

    // This method will make the View for the weather data visible and hide the error message and loading indicator.
    private void hideLoadingIndicator() {
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    // This method will make the loading indicator visible and hide the weather View and error message.
    private void showLoadingIndicator() {
        mRecyclerView.setVisibility(View.INVISIBLE);
        mLoadingIndicator.setVisibility(View.VISIBLE);
    }

}