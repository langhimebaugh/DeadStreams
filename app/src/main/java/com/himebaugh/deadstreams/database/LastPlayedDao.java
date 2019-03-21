/**
 * LastPlayedDao.java
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
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;


@Dao
public interface LastPlayedDao {

    @Query("SELECT * FROM last_played_table WHERE id = 1")
    LiveData<LastPlayed> getLastPlayedLiveData();

    @Insert
    long insertLastPlayed(LastPlayed lastPlayed);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateLastPlayed(LastPlayed lastPlayed);
}