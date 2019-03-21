/**
 * PlaylistSong.java
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
import androidx.room.Index;
import androidx.room.PrimaryKey;

@SuppressWarnings("ALL")
@Entity(tableName = "playlist_songs_table",
        indices = {@Index("playlistId")})
public class PlaylistSong {

    @Ignore
    public PlaylistSong() {
    }

    @PrimaryKey(autoGenerate = true)
    private int id;
    private int playlistId;
    private String songName;        // gd1969-08-30d1t03.mp3 (file name)
    private String songAlbum;       // 1969-08-30 - Family Dog at the Great Highway
    private String songTitle;       // The Eleven -&gt;
    private String songTrack;
    private String songLength;      // 06:25
    private String songBitrate;
    private String songCreator;
    private String songFormat;
    private String showIdentifier;
    private int showId;
    private String showVenue;
    private String showCity;
    private String showState;
    private String showCoverage;
    private String showDate;
    private int showSourceId;
    private int songOrder;         // maybe used in future
    private int bandId;            // maybe used in future

    @Ignore
    public PlaylistSong(int playlistId, String songName, String showIdentifier, String songAlbum, String songTitle, String songTrack, String songLength, String songBitrate, String songCreator, String songFormat, int showId, String showVenue, String showCity, String showState, String showCoverage, String showDate, int showSourceId, int songOrder, int bandId) {
        this.playlistId = playlistId;
        this.songName = songName;
        this.showIdentifier = showIdentifier;
        this.songAlbum = songAlbum;
        this.songTitle = songTitle;
        this.songTrack = songTrack;
        this.songLength = songLength;
        this.songBitrate = songBitrate;
        this.songCreator = songCreator;
        this.songFormat = songFormat;
        this.showId = showId;
        this.showVenue = showVenue;
        this.showCity = showCity;
        this.showState = showState;
        this.showCoverage = showCoverage;
        this.showDate = showDate;
        this.showSourceId = showSourceId;
        this.songOrder = songOrder;
        this.bandId = bandId;
    }

    public PlaylistSong(int id, int playlistId, String songName, String showIdentifier, String songAlbum, String songTitle, String songTrack, String songLength, String songBitrate, String songCreator, String songFormat, int showId, String showVenue, String showCity, String showState, String showCoverage, String showDate, int showSourceId, int songOrder, int bandId) {
        this.id = id;
        this.playlistId = playlistId;
        this.songName = songName;
        this.showIdentifier = showIdentifier;
        this.songAlbum = songAlbum;
        this.songTitle = songTitle;
        this.songTrack = songTrack;
        this.songLength = songLength;
        this.songBitrate = songBitrate;
        this.songCreator = songCreator;
        this.songFormat = songFormat;
        this.showId = showId;
        this.showVenue = showVenue;
        this.showCity = showCity;
        this.showState = showState;
        this.showCoverage = showCoverage;
        this.showDate = showDate;
        this.showSourceId = showSourceId;
        this.songOrder = songOrder;
        this.bandId = bandId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPlaylistId() {
        return playlistId;
    }

    public void setPlaylistId(int playlistId) {
        this.playlistId = playlistId;
    }

    public String getSongName() {
        return songName;
    }

    public void setSongName(String songName) {
        this.songName = songName;
    }

    public String getShowIdentifier() {
        return showIdentifier;
    }

    public void setShowIdentifier(String showIdentifier) {
        this.showIdentifier = showIdentifier;
    }

    public String getSongAlbum() {
        return songAlbum;
    }

    public void setSongAlbum(String songAlbum) {
        this.songAlbum = songAlbum;
    }

    public String getSongTitle() {
        return songTitle;
    }

    public void setSongTitle(String songTitle) {
        this.songTitle = songTitle;
    }

    public String getSongTrack() {
        return songTrack;
    }

    public void setSongTrack(String songTrack) {
        this.songTrack = songTrack;
    }

    public String getSongLength() {
        return songLength;
    }

    public void setSongLength(String songLength) {
        this.songLength = songLength;
    }

    public String getSongBitrate() {
        return songBitrate;
    }

    public void setSongBitrate(String songBitrate) {
        this.songBitrate = songBitrate;
    }

    public String getSongCreator() {
        return songCreator;
    }

    public void setSongCreator(String songCreator) {
        this.songCreator = songCreator;
    }

    public String getSongFormat() {
        return songFormat;
    }

    public void setSongFormat(String songFormat) {
        this.songFormat = songFormat;
    }

    public int getShowId() {
        return showId;
    }

    public void setShowId(int showId) {
        this.showId = showId;
    }

    public String getShowVenue() {
        return showVenue;
    }

    public void setShowVenue(String showVenue) {
        this.showVenue = showVenue;
    }

    public String getShowCity() {
        return showCity;
    }

    public void setShowCity(String showCity) {
        this.showCity = showCity;
    }

    public String getShowState() {
        return showState;
    }

    public void setShowState(String showState) {
        this.showState = showState;
    }

    public String getShowCoverage() {
        return showCoverage;
    }

    public void setShowCoverage(String showCoverage) {
        this.showCoverage = showCoverage;
    }

    public String getShowDate() {
        return showDate;
    }

    public void setShowDate(String showDate) {
        this.showDate = showDate;
    }

    public int getShowSourceId() {
        return showSourceId;
    }

    public void setShowSourceId(int showSourceId) {
        this.showSourceId = showSourceId;
    }

    public int getSongOrder() {
        return songOrder;
    }

    public void setSongOrder(int songOrder) {
        this.songOrder = songOrder;
    }

    public int getBandId() {
        return bandId;
    }

    public void setBandId(int bandId) {
        this.bandId = bandId;
    }

}