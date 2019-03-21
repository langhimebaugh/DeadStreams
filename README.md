# Dead Streams (Capstone-Project)

Stream Grateful Dead music concert/shows from the Internet Archive (archive.org). 
Audio is from soundboard recordings or audience uploads and free to use.

### Video Walkthrough and Screenshots

<img src="https://github.com/langhimebaugh/DeadStreams/blob/master/DeadStreams.gif" width="250"> - <img src="https://github.com/langhimebaugh/DeadStreams/blob/master/Screenshot_All_Shows.png" width="250"> - <img src="https://github.com/langhimebaugh/DeadStreams/blob/master/Screenshot_Random_Playlist.png" width="250">

## Project Rubric

I believe it meets these specifications

* [x] App conforms to common standards found in the Android Nanodegree General Project Guidelines.
* [x] App correctly preserves and restores user or app state, that is , student uses a bundle to save app state and restores it via onSaveInstanceState/onRestoreInstanceState. For example,
* [x] When a list item is selected, it remains selected on rotation.
* [x] When an activity is displayed, the same activity appears on rotation.
* [x] User text input is preserved on rotation.
* [x] Maintains list items positions on device rotation.
* [x] When the app is resumed after the device wakes from sleep (locked) state, the app returns the user to the exact state in which it was last used.
* [x] When the app is relaunched from Home or All Apps, the app restores the app state as closely as possible to the previous state.
* [x] App is written solely in the Java Programming Language
* [x] App utilizes stable release versions of all libraries, Gradle, and Android Studio.
* [x] App integrates a third-party library.
* [x] App validates all input from servers and users. If data does not exist or is in the wrong format, the app logs this fact and does not crash.
* [x] App includes support for accessibility. That includes content descriptions, navigation using a D-pad, and, if applicable, non-audio versions of audio cues.
* [x] App keeps all strings in a strings.xml file and enables RTL layout switching on all layouts.
* [x] App provides a widget to provide relevant information to the user on the home screen.
* [x] App integrates two or more Google services. Google service integrations can be a part of Google Play Services or Firebase.
   *  Using Firebase Storage at gs://deadstreams-ldh.appspot.com [only for downloading so might not count]
   *  Using Admob in PlaybackControlsFragment
   *  Using Analytics in ShowListActivity
* [x] Each service imported in the build.gradle is used in the app.
* [x] Location is NOT used, student meets specifications.
* [x] Admob is used and the app displays test ads.
* [x] Analytics is used and the app creates only one analytics instance.
* [x] Maps is NOT used, student meets specifications.
* [x] Identity is NOT used, student meets specifications.
* [x] App theme extends AppCompat.
* [x] App uses an app bar and associated toolbars.
* [x] App uses standard and simple transitions between activities.
* [x] App builds from a clean repository checkout with no additional configuration.
* [x] App builds and deploys using the installRelease Gradle task.
* [x] App is equipped with a signing configuration, and the keystore and passwords are included in the repository. Keystore is referred to by a relative path.
* [x] All app dependencies are managed by Gradle.
* [x] App stores data locally either by using Room.
* [x] Content provider is NOT used, yet.
* [x] Room is used and LiveData and ViewModel are used when required and no unnecessary calls to the database are made.
* [x] Uses an IntentService (LoadSongsJobService) to download a list of songs on a per request basis.
* [x] Uses an AsyncTask "favoriting" a show in ShowListActivity & FavoritesActivity.
* [x] Uses an IntentService (LoadShowsJobService) for downloading and processing the initial data file. 5 seconds on my phone.  Called in AppDatabase

## Notes

Over 600 hours from design to completion!

Designed for phone & tablet.  
Control playback from the widget or notification.
Admob banners that show upon startup and disappear upon play. (And the AdView is paused)
Settings to control widget background.
Uses Picasso in the RandomPlayActivity CollapsingToolbar Image
Uses Firebase Storage for retrieval of initial data.
Uses Analytics.
Uses Room with LiveData & ViewModel

I started out going through this Google I/O 2016 codelab
- https://github.com/googlecodelabs/android-music-player
- https://codelabs.developers.google.com/codelabs/android-music-player/#0

I ended up using 5 files for the basis of my player:
- MediaNotificationManager.java
- MusicLibrary.java
- MusicPlayerActivity.java
- MusicService.java   
- PlaybackManager.java

It still took significant work to integrate my ideas into this starter code.  I got stuck at three different points for a few weeks each time until I was able to work past the issues.

I had/have challenges with:
 - Maintaining the RecyclerView position on rotation in the SongListFragment
 - Maintaining the state of the collapsing toolbars on rotation
 - Loading the MusicLibrary with new songs, especially while playing other songs from another show.
 - Updating widget when App/Service is destroyed to clear last playing song to set message to click.
 - Best practice for downloading initial data into AppDatabase in scenario where internet goes down. (upon creation can only happen once)
 - Started out with different navigation technique that took me far down the wrong path.
 - ViewModel setup improperly led to issues in a fragment but not an activity.
 - Needing to use SharedPreferences rather than onSaveInstanceState (when navigating from one screen in portrait to another, then rotating in two-pane mode)

I finished the Random Playlist, and partially completed the Playlist.

This still needs work on the Playlist and Search functions and fixing a few bugs!!!

Could also convert to Kotlin and integrate the "New Universal Music Player" logic as mentioned here...
- https://android-developers.googleblog.com/2018/06/a-new-universal-music-player.html
- https://github.com/googlesamples/android-UniversalMusicPlayer

