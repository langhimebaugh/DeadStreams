/**
 * FavoritesActivity.java
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
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.himebaugh.deadstreams.R;
import com.himebaugh.deadstreams.database.Show;
import com.himebaugh.deadstreams.player.LoadSongsJobService;
import com.himebaugh.deadstreams.utils.NetworkUtils;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


public class FavoritesActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, ShowListAdapter.OnClickHandler {


    private static final String TAG = FavoritesActivity.class.getSimpleName();

    public static final String FRAGMENT_TAG = "FavoritesActivity";

    public static final String EXTRA_SHOW_ID = "extra_show_id"; // Extra for the show ID to be received in the intent
    public static final String EXTRA_SHOW_IDENTIFIER = "extra_show_identifier";
    public static final String EXTRA_SHOW_DATE = "extra_show_date";
    public static final String EXTRA_SHOW_VENUE = "extra_show_venue";
    public static final String EXTRA_SHOW_CITY = "extra_show_city";
    public static final String EXTRA_SHOW_STATE = "extra_show_state";
    public static final String EXTRA_ORIGIN = "extra_origin";

    /* PREFS */
    private static final String PREF_FAV_SHOW_ID = "pref_fav_show_id";
    private static final String PREF_FAV_SHOW_IDENTIFIER = "pref_fav_show_identifier";
    private static final String PREF_FAV_SHOW_DATE = "pref_fav_show_date";
    private static final String PREF_FAV_SHOW_VENUE = "pref_fav_show_venue";
    private static final String PREF_FAV_SHOW_CITY = "pref_fav_show_city";
    private static final String PREF_FAV_SHOW_STATE = "pref_fav_show_state";

    private RecyclerView mRecyclerView;
    private ShowListAdapter mAdapter;
    private FavoritesViewModel favoriteListViewModel;

    private int mShowId;
    private String mShowIdentifier;
    private String mShowDate;
    private String mShowVenue;
    private String mShowCity;
    private String mShowState;

    private ProgressBar mLoadingIndicator;

    private TextView mEmptyView;

    // Whether or not the activity is in two-pane mode, i.e. running on a tablet device.
    private boolean mTwoPane;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_favorites);

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

        mEmptyView = findViewById(R.id.empty_view);

        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        // Initialize the adapter and attach it to the RecyclerView
        mAdapter = new ShowListAdapter(this);
        mRecyclerView.setAdapter(mAdapter);

        setupViewModel();

        if (findViewById(R.id.song_list_container) != null) {
            // The detail container view will be present only in the large-screen layouts (res/values-sw600dp).
            // If this view is present, then the activity should be in two-pane mode.
            mTwoPane = true;

            // SharedPreferences used in two-pane mode rather than onSaveInstanceState & onRestoreInstanceState
            // two reasons:
            // #1 On startup, better to have previous played song list rather than welcome screen.
            // #2 Best when user goes in portrait mode in All Songs to Favorite Songs and then rotates the screen.

            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            mShowId = sharedPreferences.getInt(PREF_FAV_SHOW_ID, 0);
            mShowIdentifier = sharedPreferences.getString(PREF_FAV_SHOW_IDENTIFIER, "");
            mShowDate = sharedPreferences.getString(PREF_FAV_SHOW_DATE, "");
            mShowVenue = sharedPreferences.getString(PREF_FAV_SHOW_VENUE, "");
            mShowCity = sharedPreferences.getString(PREF_FAV_SHOW_CITY, "");
            mShowState = sharedPreferences.getString(PREF_FAV_SHOW_STATE, "");
        }

        if (savedInstanceState == null && mTwoPane) {
            // Present SongListFragment upon startup using SharedPreferences to load the previous show.
            if (mShowId != 0) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.song_list_container, SongListFragment.newInstance(mShowId, mShowIdentifier, mShowDate, mShowVenue, mShowCity, mShowState), FRAGMENT_TAG)
                        .commitNow();
            } else {
                // first time run? show a welcome fragment screen
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.song_list_container, new SongListFragmentEmpty(), FRAGMENT_TAG)
                        .commitNow();
            }
        } else if (savedInstanceState != null && mTwoPane) {

            // Try to attach an existing fragment
            FragmentManager fragmentManager = getSupportFragmentManager();
            SongListFragment songListFragment = (SongListFragment) fragmentManager.findFragmentByTag(FRAGMENT_TAG);

            // No fragment available, so create a new one.
            if (songListFragment == null) {

                if (mShowId != 0) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.song_list_container, SongListFragment.newInstance(mShowId, mShowIdentifier, mShowDate, mShowVenue, mShowCity, mShowState), FRAGMENT_TAG)
                            .commitNow();
                } else {
                    // most likely never get here as mShowId is loaded from sharedPreferences
                    // show a welcome fragment screen
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.song_list_container, new SongListFragmentEmpty(), FRAGMENT_TAG)
                            .commitNow();
                }
            }
        }

    }

    private void setupViewModel() {

        favoriteListViewModel = ViewModelProviders.of(this).get(FavoritesViewModel.class);

        favoriteListViewModel.getFavoriteShows().observe(this, showList -> {

            mAdapter.loadShows(showList);

            // Show the list or the loading screen based on whether the data exists and is loaded
            if (showList != null && showList.size() != 0) {
                hideLoadingIndicator();
            } else if (showList == null) {
                showLoadingIndicator();
            } else {
                showNoFavoritesSelectedIndicator();
            }

        });

    }

    @Override
    public void onClick(@NonNull Show show) {

        if (NetworkUtils.isNetworkAvailable(this)) {

            // check internet before moving forward.
            // avoids errors by not running LoadSongsJobService with internet down.

            // Save the last Show Id
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt(PREF_FAV_SHOW_ID, show.getId());
            editor.putString(PREF_FAV_SHOW_IDENTIFIER, show.getIdentifier());
            editor.putString(PREF_FAV_SHOW_DATE, show.getDate());
            editor.putString(PREF_FAV_SHOW_VENUE, show.getVenue());
            editor.putString(PREF_FAV_SHOW_CITY, show.getCity());
            editor.putString(PREF_FAV_SHOW_STATE, show.getState());
            editor.apply();

            LoadSongsJobService.startActionLoadSongs(this, show.getIdentifier());

            if (mTwoPane) {

                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.song_list_container,
                                SongListFragment.newInstance(show.getId(), show.getIdentifier(), show.getDate(), show.getVenue(), show.getCity(), show.getState()))
                        .commitNow();
            } else {

                Intent intent = new Intent(this, SongListActivity.class);

                intent.putExtra(EXTRA_SHOW_ID, show.getId());
                intent.putExtra(EXTRA_SHOW_IDENTIFIER, show.getIdentifier());
                intent.putExtra(EXTRA_SHOW_DATE, show.getDate());
                intent.putExtra(EXTRA_SHOW_VENUE, show.getVenue());
                intent.putExtra(EXTRA_SHOW_CITY, show.getCity());
                intent.putExtra(EXTRA_SHOW_STATE, show.getState());
                intent.putExtra(EXTRA_ORIGIN, "FavoritesActivity");

                startActivity(intent);
            }
        } else {
            // no internet
            // display notification here
            Log.i(TAG, "onClick: but internet down");
            Toast.makeText(this, R.string.message_check_internet, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onFavorite(@NonNull Show show) {
        // Toggle the Favorite value
        if (show.getFavorite() == 1) {
            show.setFavorite(0);
        } else {
            show.setFavorite(1);
        }

        favoriteListViewModel.updateShow(show);
    }

    // This method will make the View for the weather data visible and hide the error message and loading indicator.
    private void hideLoadingIndicator() {
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        mEmptyView.setVisibility(View.INVISIBLE);
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    // This method will make the loading indicator visible and hide the weather View and error message.
    private void showLoadingIndicator() {
        mRecyclerView.setVisibility(View.INVISIBLE);
        mEmptyView.setVisibility(View.INVISIBLE);
        mLoadingIndicator.setVisibility(View.VISIBLE);
    }

    private void showNoFavoritesSelectedIndicator() {
        mRecyclerView.setVisibility(View.INVISIBLE);
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        mEmptyView.setVisibility(View.VISIBLE);
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
                // startActivity(new Intent(this, FavoritesActivity.class));
                break;
            case R.id.nav_playlists:                        // CUSTOM PLAYLIST
                startActivity(new Intent(this, PlaylistListActivity.class));
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

}
