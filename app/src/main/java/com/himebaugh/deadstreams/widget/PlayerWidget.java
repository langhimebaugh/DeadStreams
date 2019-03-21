/**
 * PlayerWidget.java
 *
 * This file is part of
 * DeadStreams - An Android App to Stream Grateful Dead music concert/shows from the Internet Archive (archive.org).
 * Developed by Langdon Himebaugh for the Android Nanodegree Capstone-Project.
 *
 * Copyright (c) 2018-19 - Langdon Himebaugh
 * Licensed under the MIT-License
 * http://opensource.org/licenses/MIT
 */

package com.himebaugh.deadstreams.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.media.session.PlaybackStateCompat;
import android.widget.RemoteViews;

import com.himebaugh.deadstreams.R;
import com.himebaugh.deadstreams.ui.ShowListActivity;

import androidx.media.session.MediaButtonReceiver;
import androidx.preference.PreferenceManager;


/**
 * Implementation of App Widget functionality.
 */
public class PlayerWidget extends AppWidgetProvider {

    private static final String TAG = PlayerWidget.class.getSimpleName();
    private static final String WIDGET_COLOR = "pref_widget_color";

    private static int mPlaybackState = 0;
    private static String mSongTitle = "";

    // called from MusicService
    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        mPlaybackState = intent.getIntExtra("PlaybackState", 0);
        mSongTitle = intent.getStringExtra("SongTitle");

        ComponentName thisWidget = new ComponentName(context.getPackageName(), PlayerWidget.class.getName());
        int[] appWidgetIds = AppWidgetManager.getInstance(context).getAppWidgetIds(thisWidget);
        onUpdate(context, AppWidgetManager.getInstance(context), appWidgetIds);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    private static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {

        // Get Shared Preferences
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

        // Construct the RemoteViews object
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_player);

        // Set background to WIDGET_COLOR
        switch (Integer.parseInt(preferences.getString(WIDGET_COLOR, "1"))) {
            case 1:
                remoteViews.setInt(R.id.widget_layout, "setBackgroundResource", R.drawable.widget_background_black);
                break;
            case 2:
                remoteViews.setInt(R.id.widget_layout, "setBackgroundResource", R.drawable.widget_background_transparent);
                break;
            default:
                remoteViews.setInt(R.id.widget_layout, "setBackgroundResource", R.drawable.widget_background_black);
        }

        // Get info on the current playback state & song from the member variable [updated in onReceive from the MusicService]
        if (mPlaybackState == PlaybackStateCompat.STATE_PLAYING) {
            remoteViews.setImageViewResource(R.id.player_play_pause, R.drawable.ic_pause_white_36dp);
        } else {
            remoteViews.setImageViewResource(R.id.player_play_pause, R.drawable.ic_play_arrow_white_36dp);
        }

        remoteViews.setTextViewText(R.id.title_textview, mSongTitle);

        // An intent and click handler to launch the app when tapped
        PendingIntent openActivity = PendingIntent.getActivity(context, 0, new Intent(context.getApplicationContext(), ShowListActivity.class), 0);
        remoteViews.setOnClickPendingIntent(R.id.widget_layout, openActivity);

        PendingIntent previous = MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS);
        remoteViews.setOnClickPendingIntent(R.id.player_previous, previous);

        PendingIntent playPause = MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_PLAY_PAUSE);
        remoteViews.setOnClickPendingIntent(R.id.player_play_pause, playPause);

        PendingIntent next = MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_SKIP_TO_NEXT);
        remoteViews.setOnClickPendingIntent(R.id.player_next, next);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}