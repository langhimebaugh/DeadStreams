/**
 * LastPlayed.java
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

@Entity(tableName = "last_played_table")
public class LastPlayed {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private String showIdentifier;          // not used yet
    private String songName;
    private String songUri;
    private String songDescription;
    private String songTrack;               // not used yet
    private String songFileName;            // not used yet

    @Ignore
    public LastPlayed() {
    }

    @Ignore
    public LastPlayed(String songName, String songUri, String songDescription) {
        this.songName = songName;
        this.songUri = songUri;
        this.songDescription = songDescription;
    }

    public LastPlayed(int id, String showIdentifier, String songName, String songUri, String songDescription, String songTrack, String songFileName) {
        this.id = id;
        this.showIdentifier = showIdentifier;
        this.songName = songName;
        this.songUri = songUri;
        this.songDescription = songDescription;
        this.songTrack = songTrack;
        this.songFileName = songFileName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getShowIdentifier() {
        return showIdentifier;
    }

    public void setShowIdentifier(String showIdentifier) {
        this.showIdentifier = showIdentifier;
    }

    public String getSongName() {
        return songName;
    }

    public void setSongName(String songName) {
        this.songName = songName;
    }

    public String getSongUri() {
        return songUri;
    }

    public void setSongUri(String songUri) {
        this.songUri = songUri;
    }

    public String getSongDescription() {
        return songDescription;
    }

    public void setSongDescription(String songDescription) {
        this.songDescription = songDescription;
    }

    public String getSongTrack() {
        return songTrack;
    }

    public void setSongTrack(String songTrack) {
        this.songTrack = songTrack;
    }

    public String getSongFileName() {
        return songFileName;
    }

    public void setSongFileName(String songFileName) {
        this.songFileName = songFileName;
    }


}
