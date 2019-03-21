/**
 * NetworkUtils.java
 *
 * This file is part of
 * DeadStreams - An Android App to Stream Grateful Dead music concert/shows from the Internet Archive (archive.org).
 * Developed by Langdon Himebaugh for the Android Nanodegree Capstone-Project.
 *
 * Copyright (c) 2018-19 - Langdon Himebaugh
 * Licensed under the MIT-License
 * http://opensource.org/licenses/MIT
 */

package com.himebaugh.deadstreams.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.himebaugh.deadstreams.database.PlaylistSong;
import com.himebaugh.deadstreams.database.Show;
import com.himebaugh.deadstreams.database.Song;
import com.himebaugh.deadstreams.database.SongsAtoZ;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Scanner;

public class NetworkUtils {

    private static final String TAG = NetworkUtils.class.getSimpleName();

    private static final String SHOW_LIST_JSON_URL =
            "https://firebasestorage.googleapis.com/v0/b/deadstreams-ldh.appspot.com/o/show-list.json?alt=media&token=8d618eb1-81f0-4b56-b7be-d124e17dbb83";
            // OLD "https://firebasestorage.googleapis.com/v0/b/deadstreams-ldh.appspot.com/o/dead-shows.json?alt=media&token=57fd7e4c-379e-4e8b-8ac5-4c91d1e6927d";

    private static final String SONG_LIST_BY_IDENTIFIER_JSON_URL =
            "https://firebasestorage.googleapis.com/v0/b/deadstreams-ldh.appspot.com/o/song-list-by-identifier.json?alt=media&token=9073e612-e7c1-4938-a0d0-03d1b8bd74db";

    private static final String SONGS_A_TO_Z_JSON_URL =
            "https://firebasestorage.googleapis.com/v0/b/deadstreams-ldh.appspot.com/o/songs-a-to-z.json?alt=media&token=8256fa78-f77a-4a83-a621-a8c1938f44fe";

    // TODO: playlist...
    private static final String PLAYLIST_JSON_URL =
            "https://firebasestorage.googleapis.com/v0/b/deadstreams-ldh.appspot.com/o/playlist.json?alt=media&token=aa6ac0d2-8fed-48d2-bf80-093157c90c37";


    private static URL getShowListUrl() {

        return buildUrl(SHOW_LIST_JSON_URL);
    }

    private static URL getSongListByIdentifierUrl() {

        return buildUrl(SONG_LIST_BY_IDENTIFIER_JSON_URL);
    }

    private static URL getSongsAtoZUrl() {

        return buildUrl(SONGS_A_TO_Z_JSON_URL);
    }

    private static URL getPlaylistUrl() {

        return buildUrl(PLAYLIST_JSON_URL);
    }

    public static URL buildUrl(String stringUrl) {

        Uri builtUri = Uri.parse(stringUrl).buildUpon()
                .build();

        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }

    private static String getJsonFromHttpUrl(URL url) {

        String jsonString = null;

        try {
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                jsonString = scanner.next();
            }

            urlConnection.disconnect();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return jsonString;
    }

    public static String getXmlFromHttpUrl(URL url) {

        String xmlString = null;

        try {
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                xmlString = scanner.next();
            }

            urlConnection.disconnect();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return xmlString;
    }

    public static List<Show> getShowList() {

        List<Show> returnShowList = null;

        String showJsonResults = getJsonFromHttpUrl(getShowListUrl());

        if (showJsonResults != null) {
            Gson gson = new Gson();
            Type listType = new TypeToken<List<Show>>() {
            }.getType();
            returnShowList = gson.fromJson(showJsonResults, listType);
        }

        return returnShowList;

    }

    public static List<Song> getSongList() {

        List<Song> returnSongList = null;

        String songJsonResults = getJsonFromHttpUrl(getSongListByIdentifierUrl());

        if (songJsonResults != null) {
            Gson gson = new Gson();
            Type listType = new TypeToken<List<Song>>() {
            }.getType();
            returnSongList = gson.fromJson(songJsonResults, listType);
        }

        return returnSongList;

    }

    public static List<SongsAtoZ> getSongsAtoZ() {

        List<SongsAtoZ> returnSongsAtoZ = null;

        String songsAtoZJsonResults = getJsonFromHttpUrl(getSongsAtoZUrl());

        if (songsAtoZJsonResults != null) {
            Gson gson = new Gson();
            Type listType = new TypeToken<List<SongsAtoZ>>() {
            }.getType();
            returnSongsAtoZ = gson.fromJson(songsAtoZJsonResults, listType);
        }

        return returnSongsAtoZ;

    }

    public static List<PlaylistSong> getPlaylistSongs() {

        List<PlaylistSong> returnPlaylistSongs = null;

        String playlistJsonResults = getJsonFromHttpUrl(getPlaylistUrl());

        if (playlistJsonResults != null) {
            Gson gson = new Gson();
            Type listType = new TypeToken<List<PlaylistSong>>() {
            }.getType();
            returnPlaylistSongs = gson.fromJson(playlistJsonResults, listType);
        }

        return returnPlaylistSongs;

    }

    public static boolean isNetworkAvailable(Context context) {

        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();

        return activeNetwork != null && activeNetwork.isConnected();
    }

}
