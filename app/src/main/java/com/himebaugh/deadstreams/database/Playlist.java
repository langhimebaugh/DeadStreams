/**
 * Playlist.java
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


import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@SuppressWarnings("ALL")
@Entity(tableName = "playlist_table")
public class Playlist {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private String playlistName;

    @Ignore
    public Playlist(String playlistName) {
        this.playlistName = playlistName;
    }

    public Playlist(int id, String playlistName) {
        this.id = id;
        this.playlistName = playlistName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPlaylistName() {
        return playlistName;
    }

    public void setPlaylistName(String playlistName) {
        this.playlistName = playlistName;
    }

}