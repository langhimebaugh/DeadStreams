/**
 * ShowDao.java
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

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ShowDao {

    @Query("SELECT * FROM show_table ")
    LiveData<List<Show>> loadAllShows();

    @Query("SELECT * FROM show_table WHERE favorite = 1 ")
    LiveData<List<Show>> loadFavoriteShows();

    @Query("SELECT * FROM show_table WHERE id" + " = :id")
    LiveData<Show> loadShowById(int id);

    @Query("SELECT * FROM show_table WHERE identifier" + " = :identifier")
    LiveData<Show> loadShowByIdentifier(String identifier);

    @Query("SELECT * FROM show_table WHERE identifier" + " = :identifier")
    Show loadShowByIdentifier2(String identifier);

    @Insert
    void insertShow(Show show);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Long[] insertAllShows(List<Show> showList);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateShow(Show show);

    @Delete
    void deleteShow(Show show);

}
