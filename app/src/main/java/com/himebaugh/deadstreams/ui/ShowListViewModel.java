/**
 * ShowListViewModel.java
 *
 * This file is part of
 * DeadStreams - An Android App to Stream Grateful Dead music concert/shows from the Internet Archive (archive.org).
 * Developed by Langdon Himebaugh for the Android Nanodegree Capstone-Project.
 *
 * Copyright (c) 2018-19 - Langdon Himebaugh
 * Licensed under the MIT-License
 * http://opensource.org/licenses/MIT
 */

package com.himebaugh.deadstreams.ui;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.himebaugh.deadstreams.BasicApp;
import com.himebaugh.deadstreams.DataRepository;
import com.himebaugh.deadstreams.database.Show;

import java.util.List;

// The ShowListViewModel provides the interface between the UI and the data layer of the app, represented by the Repository
class ShowListViewModel extends AndroidViewModel {

    private final DataRepository mRepository;
    private final LiveData<List<Show>> mAllShows;

    public ShowListViewModel(Application application) {
        super(application);

        mRepository = ((BasicApp) application).getRepository();

        mAllShows = mRepository.loadAllShows();
    }

    // Expose the LiveData Shows query so the UI can observe it.
    LiveData<List<Show>> getAllShows() {

        return mAllShows;
    }

    void updateShow(Show show) {

        mRepository.updateShow(show);
    }
}