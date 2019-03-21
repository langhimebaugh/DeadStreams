/**
 * MusicService.java
 *
 * This file is part of
 * DeadStreams - An Android App to Stream Grateful Dead music concert/shows from the Internet Archive (archive.org).
 * Developed by Langdon Himebaugh for the Android Nanodegree Capstone-Project.
 *
 * Copyright (c) 2018-19 - Langdon Himebaugh
 * Licensed under the MIT-License
 * http://opensource.org/licenses/MIT
 */

package com.himebaugh.deadstreams.player;

import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import com.himebaugh.deadstreams.BasicApp;
import com.himebaugh.deadstreams.DataRepository;
import com.himebaugh.deadstreams.database.LastPlayed;
import com.himebaugh.deadstreams.widget.PlayerWidget;

import java.util.List;

import androidx.annotation.Nullable;
import androidx.media.MediaBrowserServiceCompat;
import androidx.media.session.MediaButtonReceiver;


public class MusicService extends MediaBrowserServiceCompat implements LoadSongsJobService.Callback {

    private static final String TAG = MusicService.class.getSimpleName();

    private MediaSessionCompat mSession;
    private PlaybackManager mPlayback;

    private DataRepository mRepository;

    private Result<List<MediaBrowserCompat.MediaItem>> mResult;

    public MusicService() {

    }

    final MediaSessionCompat.Callback mCallback = new MediaSessionCompat.Callback() {

        @Override
        public void onPlayFromUri(Uri uri, Bundle extras) {
            super.onPlayFromUri(uri, extras);
        }

        @Override
        public void onPlayFromMediaId(String mediaId, Bundle extras) {

            if (mediaId == null ) {
                Log.i(TAG, "onPlayFromMediaId: mediaId == null");
                return;
            }

            MediaMetadataCompat metadata = MusicLibrary.getMetadata(MusicService.this, mediaId);  // THUNDER?
            if (metadata == null ) {
                Log.i(TAG, "onPlayFromMediaId: mediaId == null");
                return;
            }
            mSession.setMetadata(metadata);
            if (!mSession.isActive()) {
                mSession.setActive(true);
            }

            if (metadata.getDescription() != null) {
                LastPlayed lastPlayed = new LastPlayed();

                lastPlayed.setId(1);
                if (metadata.getDescription().getTitle() != null) {
                    lastPlayed.setSongName(metadata.getDescription().getTitle().toString());
                } else {
                    lastPlayed.setSongName("");
                }
                if (metadata.getDescription().getDescription() != null) {
                    lastPlayed.setSongDescription(metadata.getDescription().getDescription().toString());
                } else {
                    lastPlayed.setSongDescription("");
                }

                lastPlayed.setSongUri(metadata.getDescription().getMediaId());

                mRepository.updateLastPlayed(lastPlayed);
            }

            if (metadata.getDescription().getMediaId() != null) {

                registerHeadphoneDisconnectReceiver();

                int duration = (int) metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION);

                int songTrack = (int) metadata.getLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER);

                mPlayback.play(metadata);
            } else {
                Log.i(TAG, "onPlayFromMediaId: ERROR - metadata.getDescription().getMediaId() == null");
            }

        }

        @Override
        public void onPlay() {

            if (mPlayback.getCurrentMediaId() != null ) {

                onPlayFromMediaId(mPlayback.getCurrentMediaId(), null);
            }
        }

        @Override
        public void onPause() {

            mPlayback.pause();

            unregisterHeadphoneDisconnectReceiver();
        }

        @Override
        public void onStop() {

            updateWidget("", 0, "onStop");
            unregisterHeadphoneDisconnectReceiver();


            mPlayback.stop();

            stopSelf();

        }

        @Override
        public void onSkipToNext() {
            onPlayFromMediaId(MusicLibrary.getNextSong(mPlayback.getCurrentMediaId()), null);
        }

        @Override
        public void onSkipToPrevious() {
            onPlayFromMediaId(MusicLibrary.getPreviousSong(mPlayback.getCurrentMediaId()), null);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        // Start a new MediaSession
        mSession = new MediaSessionCompat(this, "MusicService");
        mSession.setCallback(mCallback);
        mSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                          MediaSessionCompat.FLAG_HANDLES_QUEUE_COMMANDS |
                          MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        setSessionToken(mSession.getSessionToken());

        mRepository = ((BasicApp) getApplication()).getRepository();

        final MediaNotificationManager mediaNotificationManager = new MediaNotificationManager(this);

        mPlayback = new PlaybackManager(this, new PlaybackManager.Callback() {

            @Override
            public void onPlaybackStatusChanged(PlaybackStateCompat state) {
                mSession.setPlaybackState(state);

                // #WIDGET
                String songTitle = "";
                String mediaID = "";
                if (mPlayback.getCurrentMedia() != null
                        && mPlayback.getCurrentMedia().getDescription() != null
                        && mPlayback.getCurrentMedia().getDescription().getTitle() != null
                        && mPlayback.getCurrentMedia().getDescription().getMediaId() != null) {
                    songTitle = mPlayback.getCurrentMedia().getDescription().getTitle().toString();
                    mediaID = mPlayback.getCurrentMedia().getDescription().getMediaId();
                }
                updateWidget(mediaID, state.getState(), songTitle);

                mediaNotificationManager.update(mPlayback.getCurrentMedia(), state, getSessionToken());
            }

            @Override
            public void onCompletion() {
                mCallback.onSkipToNext();
            }

            @Override
            public void onReady() {

            }

        });

    }


    @Override
    public void onDestroy() {

        mPlayback.stop();
        mSession.release();

        updateWidget("", 0, "Click to Select a Song");

        unregisterHeadphoneDisconnectReceiver();

        super.onDestroy();
    }

    @Override
    public BrowserRoot onGetRoot(String clientPackageName, int clientUid, Bundle rootHints) {
        return new BrowserRoot(MusicLibrary.getRoot(), null);
    }

    @Override
    public void onLoadChildren(final String parentMediaId, final Result<List<MediaBrowserCompat.MediaItem>> result) {

        // **********************************************************************************
        // THE PROBLEM: User goes from the ShowList to ShowDetail and has to download songs.
        //              This method gets called right away and has an empty list...
        //              So the the only way to refresh is this...
        // **********************************************************************************

        result.detach();    // The results are not ready, so "detach" the results before the method returns.

        mResult = result;   // Store result in member variable to reference in onMusicCatalogReady() below.

        // Verify the MusicLibrary is ready, this will be notified that the results are
        // ready to be returned in onMusicCatalogReady() below.
        LoadSongsJobService.startActionVerifyLoaded(this, this);

    }

    @Override
    public void onMusicCatalogReady(boolean success) {

        if (success) {
            // All set to return the MediaItems as the result
            Log.i(TAG, "onMusicCatalogReady: success");
            mResult.sendResult(MusicLibrary.getMediaItems());
        } else {
            // returning empty. the UI will remain blank.
            // internet could be down...

            Log.i(TAG, "onMusicCatalogReady: returning empty");
            mResult.sendResult(MusicLibrary.getMediaItems());
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // Handles Play/Pause etc. from the widget & notification
        MediaButtonReceiver.handleIntent(mSession, intent);

        return super.onStartCommand(intent, flags, startId);
    }

    private void updateWidget(String mediaID, int playbackState, String songTitle){

        Intent intent = new Intent(this, PlayerWidget.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);

        int[] ids = AppWidgetManager.getInstance(this).getAppWidgetIds(new ComponentName(getApplication(), PlayerWidget.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        intent.putExtra("MediaID", mediaID);
        intent.putExtra("PlaybackState", playbackState);
        intent.putExtra("SongTitle", songTitle);

        // Calls onReceive in PlayerWidget
        sendBroadcast(intent);
    }

    private final BroadcastReceiver headphoneDisconnectReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            // It's going to get noisy so lets pause
            mCallback.onPause();
        }

    };

    private void registerHeadphoneDisconnectReceiver() {

        registerReceiver(headphoneDisconnectReceiver, new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY));
    }

    private void unregisterHeadphoneDisconnectReceiver() {

        try {
            unregisterReceiver(headphoneDisconnectReceiver);
        } catch (Exception e) {
            // catch this: java.lang.IllegalArgumentException: Receiver not registered:
        }
    }
}