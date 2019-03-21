/**
 * InitializeDatabaseJobService.java
 *
 * This file is part of
 * DeadStreams - An Android App to Stream Grateful Dead music concert/shows from the Internet Archive (archive.org).
 * Developed by Langdon Himebaugh for the Android Nanodegree Capstone-Project.
 *
 * Copyright (c) 2018-19 - Langdon Himebaugh
 * Licensed under the MIT-License
 * http://opensource.org/licenses/MIT
 */

package com.himebaugh.deadstreams.database;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import com.himebaugh.deadstreams.BasicApp;
import com.himebaugh.deadstreams.DataRepository;
import com.himebaugh.deadstreams.R;
import com.himebaugh.deadstreams.utils.NetworkUtils;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;
import androidx.preference.PreferenceManager;


public class InitializeDatabaseJobService extends JobIntentService {

    // startActionLoadData: !!!!!!!!!!!!
    // Shows Loaded: 2130
    // Songs Loaded: 14369
    // Songs AtoZ Loaded: 454
    // Playlists Loaded: 6

    private final static String TAG = InitializeDatabaseJobService.class.getSimpleName();

    private static final String ACTION_LOAD_INITIAL_DATA = "com.himebaugh.deadstreams.database.action.LOAD_INITIAL_DATA";

    // Unique job ID for this service
    private static final int JOB_ID = 2000;

    // Result receiver object to send results
    private ResultReceiver mResultReceiver;
    public static final String RECEIVER = "RECEIVER";
    public static final int DOWNLOAD_RESULT = 123;

    public static void startActionLoadData(Context context, LoadDataResultReceiver resultReceiver) {

        Intent intent = new Intent(context, InitializeDatabaseJobService.class);
        intent.putExtra(RECEIVER, resultReceiver);
        intent.setAction(ACTION_LOAD_INITIAL_DATA);

        Log.i(TAG, "startActionLoadData: !!!!!!!!!!!!");

        enqueueWork(context, InitializeDatabaseJobService.class, JOB_ID, intent);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {

        mResultReceiver = intent.getParcelableExtra(RECEIVER);

        final String action = intent.getAction();
        if (ACTION_LOAD_INITIAL_DATA.equals(action)) {
            handleActionLoadData();
        }
    }

    private void handleActionLoadData() {

        Boolean showsLoadedsuccessfully = true;
        Boolean songsLoadedsuccessfully = true;
        Boolean songsAtoZLoadedsuccessfully = true;
        Boolean lastPlayedLoadedsuccessfully = true;
        Boolean playlistsLoadedsuccessfully = true;
        Boolean downloadCompletedSuccessfully = false;

        DataRepository mRepository = ((BasicApp) getApplication()).getRepository();

        // will be false the first time run
        // will be true once downloaded with more than 100 records...

        // InitializeDatabaseJobService: Shows Loaded: 2130
        // InitializeDatabaseJobService: Songs Loaded: 14369
        // InitializeDatabaseJobService: Songs AtoZ Loaded: 454

        // ****************************
        // SHOWS
        // ****************************
        if (!showsDownloadComplete()) {
            List<Show> showList = NetworkUtils.getShowList();

            if (showList != null) {     // probably not null if NetworkAvailable
                Long[] rowsInserted = mRepository.insertAllShows(showList);
                Log.i(TAG, "Shows Loaded: " + rowsInserted.length);

                if (rowsInserted.length >= 100) {
                    showsLoadedsuccessfully = true;

                    // save, so we don't do this again.
                    SharedPreferences sharedPreferences = android.preference.PreferenceManager.getDefaultSharedPreferences(this);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean(getString(R.string.shows_download_complete), true);
                    editor.apply();
                } else {
                    showsLoadedsuccessfully = false;
                }
            }
        }

        // ****************************
        // SONGS
        // ****************************
        if (!songsDownloadComplete()) {
            List<Song> songList = NetworkUtils.getSongList();

            if (songList != null) {     // probably not null if NetworkAvailable
                Long[] rowsInserted = mRepository.insertAllSongs(songList);
                Log.i(TAG, "Songs Loaded: " + rowsInserted.length);

                if (rowsInserted.length >= 100) {
                    songsLoadedsuccessfully = true;

                    // save, so we don't do this again.
                    SharedPreferences sharedPreferences = android.preference.PreferenceManager.getDefaultSharedPreferences(this);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean(getString(R.string.songs_download_complete), true);
                    editor.apply();
                } else {
                    songsLoadedsuccessfully = false;
                }
            }
        }

        // ****************************
        // SONGS AtoZ
        // ****************************
        if (!songsAtoZDownloadComplete()) {
            List<SongsAtoZ> songsAtoZ = NetworkUtils.getSongsAtoZ();

            if (songsAtoZ != null) {     // probably not null if NetworkAvailable
                Long[] rowsInserted = mRepository.insertAllSongsAtoZ(songsAtoZ);
                Log.i(TAG, "Songs AtoZ Loaded: " + rowsInserted.length);

                if (rowsInserted.length >= 50) {
                    songsAtoZLoadedsuccessfully = true;

                    // save, so we don't do this again.
                    SharedPreferences sharedPreferences = android.preference.PreferenceManager.getDefaultSharedPreferences(this);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean(getString(R.string.songs_atoz_download_complete), true);
                    editor.apply();
                } else {
                    songsAtoZLoadedsuccessfully = false;
                }
            }
        }

        // ****************************
        // LAST PLAYED
        // ****************************
        if (!lastPlayedInitialized()) {
            LastPlayed lastPlayed = new LastPlayed();
            lastPlayed.setId(1);
            lastPlayed.setSongName(getApplicationContext().getString(R.string.default_song_name));

            long rowInserted = mRepository.insertLastPlayed(lastPlayed);

            if (rowInserted == 1) {
                lastPlayedLoadedsuccessfully = true;

                // save, so we don't do this again.
                SharedPreferences sharedPreferences = android.preference.PreferenceManager.getDefaultSharedPreferences(this);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(getString(R.string.last_played_initialized), true);
                editor.apply();
            } else {
                lastPlayedLoadedsuccessfully = false;
            }
        }

        // ****************************
        // PLAYLIST
        // ****************************
        //List<String> list = new ArrayList<String>();
        List<Playlist> playlists = new ArrayList<>();
        Playlist playlist = new Playlist(1,"Sample Playlist");

        boolean added = playlists.add(playlist);
        if (added) {     // probably not null if NetworkAvailable
            Log.i(TAG, "handleActionLoadData: ADDED");
            Long[] rowsInserted = mRepository.insertPlaylists(playlists);
            Log.i(TAG, "handleActionLoadData: rowsInserted="+rowsInserted);
        } else {
            Log.i(TAG, "handleActionLoadData: NOT ADDED");
        }

        // ****************************
        // PLAYLIST SONGS
        // ****************************
        if (!playlistsDownloadComplete()) {
            List<PlaylistSong> playlistSongs = NetworkUtils.getPlaylistSongs();

            if (playlistSongs != null) {     // probably not null if NetworkAvailable
                Long[] rowsInserted = mRepository.insertPlaylistSongs(playlistSongs);
                Log.i(TAG, "PlaylistSongs Loaded: " + rowsInserted.length);

                if (rowsInserted.length >= 5) {
                    playlistsLoadedsuccessfully = true;

                    // save, so we don't do this again.
                    SharedPreferences sharedPreferences = android.preference.PreferenceManager.getDefaultSharedPreferences(this);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean(getString(R.string.playlist_download_complete), true);
                    editor.apply();
                } else {
                    playlistsLoadedsuccessfully = false;
                }
            }
        }

        if (showsLoadedsuccessfully && songsLoadedsuccessfully && songsAtoZLoadedsuccessfully && lastPlayedLoadedsuccessfully && playlistsLoadedsuccessfully) {

            // set complete
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(getString(R.string.initial_download_complete), true);
            editor.apply();

            downloadCompletedSuccessfully = true;
        }

        Bundle bundle = new Bundle();
        bundle.putBoolean(getString(R.string.initial_download_complete), downloadCompletedSuccessfully);
        mResultReceiver.send(DOWNLOAD_RESULT, bundle);
    }

    public Boolean showsDownloadComplete() {
        SharedPreferences sharedPreferences = android.preference.PreferenceManager.getDefaultSharedPreferences(this);
        return sharedPreferences.getBoolean(getString(R.string.shows_download_complete), false);
    }

    public Boolean songsDownloadComplete() {
        SharedPreferences sharedPreferences = android.preference.PreferenceManager.getDefaultSharedPreferences(this);
        return sharedPreferences.getBoolean(getString(R.string.songs_download_complete), false);
    }

    public Boolean songsAtoZDownloadComplete() {
        SharedPreferences sharedPreferences = android.preference.PreferenceManager.getDefaultSharedPreferences(this);
        return sharedPreferences.getBoolean(getString(R.string.songs_atoz_download_complete), false);
    }

    public Boolean lastPlayedInitialized() {
        SharedPreferences sharedPreferences = android.preference.PreferenceManager.getDefaultSharedPreferences(this);
        return sharedPreferences.getBoolean(getString(R.string.last_played_initialized), false);
    }

    public Boolean playlistsDownloadComplete() {
        SharedPreferences sharedPreferences = android.preference.PreferenceManager.getDefaultSharedPreferences(this);
        return sharedPreferences.getBoolean(getString(R.string.playlist_download_complete), false);
    }

//    public Boolean initialDownloadComplete() {
//        SharedPreferences sharedPreferences = android.preference.PreferenceManager.getDefaultSharedPreferences(this);
//        return sharedPreferences.getBoolean(getString(R.string.initial_download_complete), false);
//    }

}