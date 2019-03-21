/**
 * PlaylistSongDao.java
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
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface PlaylistSongDao {

    @Query("SELECT * FROM playlist_songs_table WHERE id" + " = :id")
    LiveData<PlaylistSong> loadPlaylistById(int id);

    @Query("SELECT * FROM playlist_songs_table WHERE playlistId" + " = :playlistId")
    LiveData<List<PlaylistSong>> loadPlaylistByPlaylistId(int playlistId);

    @Insert
    void insertPlaylistSong(PlaylistSong playlistSong);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Long[] insertPlaylistSongs(List<PlaylistSong> playlistSong);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updatePlaylist(PlaylistSong playlistSong);

    @Delete
    void deletePlaylist(PlaylistSong playlistSong);

}
