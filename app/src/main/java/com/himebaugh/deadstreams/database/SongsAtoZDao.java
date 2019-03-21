/**
 * SongsAtoZDao.java
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

@Dao
public interface SongsAtoZDao {

    @Query("SELECT * FROM songs_a_z_table ")
    List<SongsAtoZ> loadAllSongsAtoZ();

    @Query("SELECT * FROM songs_a_z_table WHERE searchName" + " = :searchname")
    LiveData<SongsAtoZ> loadSongsAtoZBySearchname(String searchname);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Long[] insertAllSongsAtoZ(List<SongsAtoZ> songList);

}
