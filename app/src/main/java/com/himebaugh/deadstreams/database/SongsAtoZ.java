/**
 * SongsAtoZ.java
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


@Entity(tableName = "songs_a_z_table",
        indices = {@Index("searchName"), @Index("altName")})
public class SongsAtoZ {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private String displayName;
    private String searchName;
    private String altName;


    @Ignore
    public SongsAtoZ() {
    }

    @Ignore
    public SongsAtoZ(String displayName, String searchName, String altName) {
        this.displayName = displayName;
        this.searchName = searchName;
        this.altName = altName;
    }

    public SongsAtoZ(int id, String displayName, String searchName, String altName) {
        this.id = id;
        this.displayName = displayName;
        this.searchName = searchName;
        this.altName = altName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getSearchName() {
        return searchName;
    }

    public void setSearchName(String searchName) {
        this.searchName = searchName;
    }

    public String getAltName() {
        return altName;
    }

    public void setAltName(String altName) {
        this.altName = altName;
    }

    @Override
    public String toString() {
        return getDisplayName();
    }

}
