/**
 * SongDao.java
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

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;


@Dao
public interface SongDao {

    @Query("SELECT * FROM song_table WHERE identifier" + " = :identifier")
    LiveData<List<Song>> loadSongListLiveDataByIdentifier(String identifier);

    @Query("SELECT * FROM song_table WHERE identifier" + " = :identifier")
    List<Song> loadSongListByIdentifier(String identifier);

    @Query("SELECT * FROM song_table WHERE title" + " = :title")
    List<Song> loadSongListByTitle(String title);

    @Query("SELECT * FROM song_table WHERE id" + " = :id")
    Song loadSongById(int id);

    // WHERE isRandom = 'true'

    @Query("SELECT * FROM song_table WHERE isRandom = 1")
    LiveData<List<Song>> loadRandomSongsLiveData();

    @Query("SELECT * FROM song_table WHERE isRandom = 1")
    List<Song> loadRandomSongs();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertSongList(List<Song> songList);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Long[] insertAllSongs(List<Song> songList);

    @Update
    void updateSongs(List<Song> songs);

}
