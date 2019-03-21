/**
 * PlaylistsUtils.java
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

import android.util.Log;


import com.himebaugh.deadstreams.BasicApp;
import com.himebaugh.deadstreams.DataRepository;
import com.himebaugh.deadstreams.player.MusicLibrary;

import androidx.lifecycle.ViewModelProviders;


public class PlaylistsUtils {

    private final static String TAG = PlaylistsUtils.class.getSimpleName();

    private static DataRepository mRepository;


    public static void addSongToPlaylist(long playlistId, long songId) {




        //mRepository = ((BasicApp) getApplication()).getRepository();


        Log.i(TAG, "addSongToPlaylist: playlistId = " + playlistId);

        Log.i(TAG, "addSongToPlaylist: songId = " + songId);


        //mRepository.updateLastPlayed(lastPlayed);





//        Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId);
//        final int base = getSongCount(resolver, uri);
//        insert(resolver, uri, songId, base + 1);

    }

//    private static void insert(ContentResolver resolver, Uri uri, long songId, int index) {
//
//        ContentValues values = new ContentValues();
//        values.put(MediaStore.Audio.Playlists.Members.PLAY_ORDER, index);
//        values.put(MediaStore.Audio.Playlists.Members.AUDIO_ID, songId);
//        resolver.insert(uri, values);
//
//    }

}
