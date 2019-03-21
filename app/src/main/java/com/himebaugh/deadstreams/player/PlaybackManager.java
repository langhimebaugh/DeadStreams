/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.himebaugh.deadstreams.player;

import android.content.Context;
import android.media.AudioManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.PowerManager;
import android.os.SystemClock;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import static android.content.Context.POWER_SERVICE;
import static com.google.android.exoplayer2.ExoPlaybackException.TYPE_RENDERER;
import static com.google.android.exoplayer2.ExoPlaybackException.TYPE_SOURCE;
import static com.google.android.exoplayer2.ExoPlaybackException.TYPE_UNEXPECTED;


class PlaybackManager implements AudioManager.OnAudioFocusChangeListener, Player.EventListener {

    private static final String TAG = PlaybackManager.class.getSimpleName();

    private static final String WAKE_LOCK = "DeadStreams:WAKE_LOCK";
    private static final String WIFI_LOCK = "DeadStreams:WIFI_LOCK";
    private final PowerManager.WakeLock mWakeLock;
    private final WifiManager.WifiLock mWifiLock;

    private final Context mContext;
    private int mState;
    private boolean mPlayOnFocusGain;
    private volatile MediaMetadataCompat mCurrentMedia;

    private SimpleExoPlayer mMediaPlayer;

    private final Callback mCallback;
    private final AudioManager mAudioManager;

    public PlaybackManager(Context context, Callback callback) {
        this.mContext = context;
        this.mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        this.mCallback = callback;

        PowerManager powerManager = (PowerManager) context.getSystemService(POWER_SERVICE);
        mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKE_LOCK);
        mWifiLock = ((WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE)).createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, WIFI_LOCK);
    }

    private boolean isPlaying() {
        return mPlayOnFocusGain || (mMediaPlayer != null && mMediaPlayer.getPlayWhenReady());
    }

    public MediaMetadataCompat getCurrentMedia() {
        return mCurrentMedia;
    }

    public String getCurrentMediaId() {
        return mCurrentMedia == null ? null : mCurrentMedia.getDescription().getMediaId();
    }

    private long getCurrentStreamPosition() {
        return mMediaPlayer != null ? mMediaPlayer.getCurrentPosition() : 0;
    }

    public void play(MediaMetadataCompat metadata) {
        String mediaId = metadata.getDescription().getMediaId();
        boolean mediaChanged = (mCurrentMedia == null || !getCurrentMediaId().equals(mediaId));

        if (mMediaPlayer == null) {

            mMediaPlayer = ExoPlayerFactory.newSimpleInstance(
                    new DefaultRenderersFactory(mContext),
                    new DefaultTrackSelector(),
                    new DefaultLoadControl());

            mMediaPlayer.addListener(this);
        }

        if (mediaChanged) {
            mCurrentMedia = metadata;

            try {
                String userAgent = Util.getUserAgent(mContext, "Dead-Streams");

                // Produces DataSource instances through which media data is loaded.
                DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(mContext, userAgent, null);
                // Produces Extractor instances for parsing the media data.
                ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
                // The MediaSource represents the media to be played.
                ExtractorMediaSource.Factory extractorMediaFactory = new ExtractorMediaSource.Factory(dataSourceFactory);
                extractorMediaFactory.setExtractorsFactory(extractorsFactory);
                MediaSource mediaSource = extractorMediaFactory.createMediaSource(Uri.parse(MusicLibrary.getSongUri(mediaId)));

                // Prepares media to play (happens on background thread) and triggers
                // {@code onPlayerStateChanged} callback when the stream is ready to play.
                mMediaPlayer.prepare(mediaSource);
            } catch (Exception e) {
                //throw new RuntimeException(e);
            }
        }

        if (grantedAudioFocus()) {
            acquireLocks();
            mPlayOnFocusGain = false;
            mMediaPlayer.setPlayWhenReady(true);
            mState = PlaybackStateCompat.STATE_PLAYING;
            updatePlaybackState();
        } else {
            mPlayOnFocusGain = true;
        }
    }

    public void pause() {
        if (isPlaying()) {
            mMediaPlayer.setPlayWhenReady(false);
            mAudioManager.abandonAudioFocus(this);
            releaseLocks();
        }
        mState = PlaybackStateCompat.STATE_PAUSED;
        updatePlaybackState();
    }

    public void stop() {
        mState = PlaybackStateCompat.STATE_STOPPED;
        updatePlaybackState();
        // Give up Audio focus
        mAudioManager.abandonAudioFocus(this);
        releaseLocks();
        // Relax all resources
        releaseMediaPlayer();
    }

    // Try to get the system audio focus.
    private boolean grantedAudioFocus() {
        int result = mAudioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }

    // Called by AudioManager on audio focus changes.
    @Override
    public void onAudioFocusChange(int focusChange) {

        boolean gotFullFocus = false;
        boolean canDuck = false;

        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                gotFullFocus = true;
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                // We have lost focus. If we can duck (low playback volume), we can keep playing.
                // Otherwise, we need to pause the playback.
                canDuck = focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK;
                break;
        }

        if (gotFullFocus || canDuck) {
            if (mMediaPlayer != null) {
                if (mPlayOnFocusGain) {
                    mPlayOnFocusGain = false;
                    mMediaPlayer.setPlayWhenReady(true);
                    mState = PlaybackStateCompat.STATE_PLAYING;
                    updatePlaybackState();
                }
                float volume = canDuck ? 0.2f : 1.0f;
                mMediaPlayer.setVolume(volume);
            }
        } else if (mState == PlaybackStateCompat.STATE_PLAYING) {
            mMediaPlayer.setPlayWhenReady(false);
            mState = PlaybackStateCompat.STATE_PAUSED;
            updatePlaybackState();
        }
    }

    private void releaseMediaPlayer() {
        if (mMediaPlayer != null) {
            mMediaPlayer.removeListener(this);
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    @PlaybackStateCompat.Actions
    private long getAvailableActions() {
        long actions = PlaybackStateCompat.ACTION_PLAY
                | PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID
                | PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH
                | PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS;
        if (isPlaying()) {
            actions |= PlaybackStateCompat.ACTION_PAUSE;
        }
        return actions;
    }

    private void updatePlaybackState() {
        if (mCallback == null) {
            return;
        }
        PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder().setActions(getAvailableActions());
        stateBuilder.setState(mState, getCurrentStreamPosition(), 1.0f, SystemClock.elapsedRealtime());
        mCallback.onPlaybackStatusChanged(stateBuilder.build());
    }

    public interface Callback {
        void onPlaybackStatusChanged(PlaybackStateCompat state);

        void onCompletion();

        void onReady();
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        switch (playbackState) {
            case Player.STATE_IDLE:
            case Player.STATE_BUFFERING:
                break;
            case Player.STATE_READY:
                if (mCallback != null) {
                    mCallback.onReady();
                }
                break;
            case Player.STATE_ENDED:
                // player has finished playing the media.
                // The media player finished playing the current song.
                if (mCallback != null) {
                    mCallback.onCompletion();

                }
                break;
        }
    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {

        mState = PlaybackStateCompat.STATE_ERROR;
        updatePlaybackState();
        switch (error.type) {
            case TYPE_RENDERER:
                break;
            case TYPE_SOURCE:
                break;
            case TYPE_UNEXPECTED:
                break;
            default:
                break;
        }
    }

    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest, int reason) {

    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

    }

    @Override
    public void onLoadingChanged(boolean isLoading) {

    }

    @Override
    public void onRepeatModeChanged(int repeatMode) {

    }

    @Override
    public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

    }

    @Override
    public void onPositionDiscontinuity(int reason) {

    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

    }

    @Override
    public void onSeekProcessed() {

    }

    private void acquireLocks() {
        if (mWakeLock != null && !mWakeLock.isHeld()) mWakeLock.acquire(10*60*1000L /*10 minutes*/);
        if (mWifiLock != null && !mWifiLock.isHeld()) mWifiLock.acquire();
    }

    private void releaseLocks() {
        if (mWakeLock != null && mWakeLock.isHeld()) mWakeLock.release();
        if (mWifiLock != null && mWifiLock.isHeld()) mWifiLock.release();
    }
}
