package com.example.emotionplayer;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.affectiva.android.affdex.sdk.Frame;
import com.affectiva.android.affdex.sdk.detector.CameraDetector;
import com.affectiva.android.affdex.sdk.detector.Face;

import com.opencsv.CSVWriter;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Error;
import com.spotify.sdk.android.player.PlayerEvent;
import com.spotify.sdk.android.player.Spotify;
import com.spotify.sdk.android.player.SpotifyPlayer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.lang.Thread.sleep;

public class MainActivity extends AppCompatActivity implements SpotifyPlayer.NotificationCallback, ConnectionStateCallback, CameraDetector.CameraEventListener, CameraDetector.ImageListener
{
    // Create instance variables for the Surface View and Camera Detector references
    SurfaceView cameraDetectorSurfaceView;
    CameraDetector cameraDetector;

    // Music playlists
    private String[] calm = {"spotify:track:13HVjjWUZFaWilh2QUJKsP",            // James Bay - Let It Go
                             "spotify:track:255TTKJjoyiLYixY0MDbID",            // Sampha - Close But Not Quite
                             "spotify:track:2vDT1uU6hZgdp3PbWGr0Xy",            // Lukas Graham - 7 Years
                             "spotify:track:3kxfsdsCpFgN412fpnW85Y",            // Childish Gambino - Rebone
                             "spotify:track:39KG4kom3enSx4GTThuDGt",            // Khalid - Coaster
                             "spotify:track:4kflIGfjdZJW4ot2ioixTB",            // Adele - Someone Like You
                             "spotify:track:4HUIsMWVGNHsxlhbc3YQ5y",            // PnB Rock - Selfish
                             "spotify:track:6vpmEb3q6U7trxPRMC2lnf"             // Taylor Bennett - Dancing In The Rain
                            };

    // Calm and Focused (No Lyrics)
    private String[] calmB = {"spotify:track:7eTDwpm5XKKssoMhMbo67y",           // Dustin O'Halloran - Home
                              "spotify:track:6oBFAvHdD7gzPzIzNGJlaj",           // Gunnarsson - Water
                              "spotify:track:3yIIVPBulzJQ5NAlcSkl0e",           // Wouter Dewit - Dance Dance Dance
                              "spotify:track:2EmL6GboAQZHgF2wXIV1EW",           // Martén Legrand - Pinehouse
                              "spotify:track:2uiE8q36U9WITyudN0AAfg",           // L'Homme Moyen - In This Light
                              "spotify:track:4DQPbY5zjuRKDndrswinwP",           // The Man on the Guitar - Am I Wrong (Guitar)
                              "spotify:track:0fP8S6WD9NBX3KzGJpLfkC",           // Will Taylor - Apologize (Guitar)
                              "spotify:track:7hrWRhCCAE7kBfVBkXLEdT"            // The Theorist - Hotline Bling (Piano)
                             };

    private String[] joyful = {"spotify:track:3qXnYw6t2Q9no7NwCwbO5Z",          // Levi Stephens - I Love You
                               "spotify:track:61Ivix5DTnDPVjp1dgLyov",          // Tom Misch - Discoo Yes
                               "spotify:track:1P5OPN8MbPgFznEp1b7OiN",          // Pharrell Williams - Brand New
                               "spotify:track:4rmPQGwcLQjCoFq5NrTA0D",          // Bruno Mars - Uptown Funk
                               "spotify:track:6QgjcU0zLnzq5OrUoSZ3OK",          // Portugal The Man - Feel It Still
                               "spotify:track:0am001WwFBVGDGLwRh3ixi",          // Clean Bandit - Rather Be
                               "spotify:track:6Z8R6UsFuGXGtiIxiD8ISb",          // Capital Cities - Safe And sound
                               "spotify:track:4kbj5MwxO1bq9wjT5g9HaA"           // WALK THE MOON - Shut Up And Dance
                              };

    private String[] joyfulB = {"spotify:track:2jg4Yc8071puvDRYi22B3a",         // Stevie Wonder - Signed, Sealed, Delivered
                                "spotify:track:01gwPP2h3ajRnqiIphUtR7",         // Jackson 5 - ABC
                                "spotify:track:4o6BgsqLIBViaGVbx5rbRk",         // Daryl Hall & John Oates - You Make My Dreams
                                "spotify:track:7GVUmCP00eSsqc4tzj1sDD",         // Redbone - Come and Get Your Love
                                "spotify:track:3LmpQiFNgFCnvAnhhvKUyI",         // Bee Gees - Stayin' Alive
                                "spotify:track:5lA3pwMkBdd24StM90QrNR",         // Michael Jackson - PYT
                                "spotify:track:74z2lfZ7fj3IqoK71lHkZw",         // Electric Light Orchestra - Mr. Blue Sky
                                "spotify:track:7Cuk8jsPPoNYQWXK9XRFvG"          // Earth, Wind, Fire - September
                               };

    // Index trackers
    private int joyPlaylistIndex = 0;
    private int joyBPlaylistIndex = 0;
    private int calmPlaylistIndex = 0;
    private int calmBPlaylistIndex = 0;

    // Specify the max processing rate used by the Camera Detector. (This is in FPS, Frames per Second)
    private int maxProcessingRate = 10;

    // Data points for user emotions
    private List<Float> simpleAVGJoy = new ArrayList<>();
    private List<Float> simpleAVGAnger = new ArrayList<>();
    private List<Float> simpleAVGSadness = new ArrayList<>();
    private List<Float> simpleAVGCalm = new ArrayList<>();

    private int maxDataPoints = 250;
    private final int THRESHOLD = 40;

    // Spotify Player
    private static final String CLIENT_ID = "becdcf434e6044c4b1b5bc9bbd38ccb7";
    private static final String REDIRECT_URI = "emotionplayerapp://callback";
    private SpotifyPlayer mPlayer;
    private String auth;

    // Button
    private Button skip;
    private Button pause;
    private Button repeat;

    // Text
    private TextView newText;
    private TextView songInfo;

    // Progress Bar
    private ProgressBar bar;
    private long timeDur;
    private long progressStatus;
    private TextView current;
    private TextView end;


    // Status check
    private boolean pause_flg = false;
    private boolean control = StartActivity.getControl();
    private boolean repeat_flg = false;
    private int choice = StartActivity.getChoice();
    private boolean is_queued = false;
    private boolean is_paused = false;
    private boolean debug_mode = false;
    private boolean is_happy_before = false;

    // Request code that will be used to verify if the result comes from correct activity
    // Can be any integer
    private static final int REQUEST_CODE = 1337;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cameraDetectorSurfaceView = findViewById(R.id.cameraDetectorSurfaceView);
        cameraDetector = new CameraDetector(this, CameraDetector.CameraType.CAMERA_FRONT, cameraDetectorSurfaceView);
        cameraDetector.setMaxProcessRate(maxProcessingRate);
        cameraDetector.setImageListener(this);
        cameraDetector.setOnCameraEventListener(this);
        cameraDetector.setDetectAllEmotions(true);
        cameraDetector.start();

        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID,
                AuthenticationResponse.Type.TOKEN,
                REDIRECT_URI);

        builder.setScopes(new String[]{"user-read-playback-state user-read-currently-playing", "streaming"});
        builder.setScopes(new String[]{"user-read-private", "streaming"});

        AuthenticationRequest request = builder.build();

        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);

        // Skip Button
        skip = findViewById(R.id.skip);
        skip.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(calmPlaylistIndex > calm.length || joyPlaylistIndex > joyful.length)
                    System.out.println("Playlist ended, can't skip to next song");
                else
                {
                    mPlayer.skipToNext(null);
                }
            }
        });

        // Pause/Play Button
        pause = findViewById(R.id.pause);
        pause.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(!pause_flg)
                {
                    pause_flg = true;
                    mPlayer.pause(null);
                    pause.setText("Play");
                }
                else
                {
                    pause_flg = false;
                    mPlayer.resume(null);
                    pause.setText("Pause");
                }
            }
        });

        // Repeat Button
        repeat = findViewById(R.id.repeat);
        repeat.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(!repeat_flg)
                {
                    repeat_flg = true;
                    mPlayer.setRepeat(null, true);
                    repeat.setText("Repeating");
                }
                else
                {
                    repeat_flg = false;
                    mPlayer.setRepeat(null, false);
                    repeat.setText("Repeat");
                }
            }
        });

        newText = findViewById(R.id.queueNotice);
        newText.setVisibility(View.INVISIBLE);
        newText.setText("A new song has been queued");

        songInfo = findViewById(R.id.songInfo);

        bar = findViewById(R.id.progressBar);

        current = findViewById(R.id.currentTime);
        current.setText("0:00");

        end = findViewById(R.id.endTime);
        end.setText("0:00");

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent)
    {
        super.onActivityResult(requestCode, resultCode, intent);

        // Check if result comes from the correct activity
        if(requestCode == REQUEST_CODE)
        {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
            if(response.getType() == AuthenticationResponse.Type.TOKEN)
            {
                Config playerConfig = new Config(this, response.getAccessToken(), CLIENT_ID);
                auth = playerConfig.oauthToken;
                Spotify.getPlayer(playerConfig, this, new SpotifyPlayer.InitializationObserver()
                {
                    @Override
                    public void onInitialized(SpotifyPlayer spotifyPlayer)
                    {
                        mPlayer = spotifyPlayer;
                        mPlayer.addConnectionStateCallback(MainActivity.this);
                        mPlayer.addNotificationCallback(MainActivity.this);
                    }

                    @Override
                    public void onError(Throwable throwable)
                    {
                        Log.e("MainActivity", "Could not initialize player: " + throwable.getMessage());
                    }
                });
            }
        }
    }

    @Override
    protected void onDestroy()
    {
        Spotify.destroyPlayer(this);
        super.onDestroy();
    }

    @Override
    public void onPlaybackEvent(PlayerEvent playerEvent)
    {
        Log.d("MainActivity", "Playback event received: " + playerEvent.name());
        switch(playerEvent)
        {
            case kSpPlaybackNotifyTrackChanged:
                is_queued = false;
                newText.setVisibility(View.INVISIBLE);
                showInfo();
                break;
            case kSpPlaybackNotifyPause:
                is_paused = true;
                break;
            case kSpPlaybackNotifyPlay:
                is_paused = false;
                break;
            default:
                break;
        }
    }

    @Override
    public void onPlaybackError(Error error)
    {
        Log.d("MainActivity", "Playback error received: " + error.name());
        switch(error)
        {
            default:
                break;
        }
    }

    @Override
    public void onLoggedIn()
    {
        Log.d("MainActivity", "User logged in");

        // This is the line that plays a song.
        if(!control)
        {
            mPlayer.playUri(null, calm[calmPlaylistIndex++], 0, 0);
        }
        else
        {
            if (choice == 1)                // Calm
            {
                mPlayer.playUri(null, calm[calmPlaylistIndex++], 0, 0);
            }
            else if (choice == 2)           // Happy
            {
                mPlayer.playUri(null, joyful[joyPlaylistIndex++], 0, 0);
            }
        }
        showInfo();
    }

    @Override
    public void onLoggedOut()
    {
        Log.d("MainActivity", "User logged out");
    }

    @Override
    public void onLoginFailed(Error var1)
    {
        Log.d("MainActivity", "Login failed");
    }

    @Override
    public void onTemporaryError()
    {
        Log.d("MainActivity", "Temporary error occurred");
    }

    @Override
    public void onConnectionMessage(String message)
    {
        Log.d("MainActivity", "Received connection message: " + message);
    }

    /**
     * Sizing the Camera Detector container for what works best with the Affectiva SDK
     */
    @Override
    public void onCameraSizeSelected(int cameraHeight, int cameraWidth, Frame.ROTATE rotation)
    {
        ViewGroup.LayoutParams params = cameraDetectorSurfaceView.getLayoutParams();

        params.height = cameraHeight;
        params.width = cameraWidth;

        cameraDetectorSurfaceView.setLayoutParams(params);
    }

    /**
     * Process image results from Affectiva SDK
     * */
    @Override
    public void onImageResults(List<Face> faces, Frame frame, float v)
    {
        if (faces == null)
            return; //frame was not processed

        if (faces.size() == 0)
            return; //no face found

        Face face = faces.get(0);

        float joy = face.emotions.getJoy();
        float anger = face.emotions.getAnger();
        float sadness = face.emotions.getSadness();

        float calmness = joy + anger + sadness;

        if (debug_mode)
        {
            System.out.println("Joy: " + joy);
            System.out.println("Anger: " + anger);
            System.out.println("Surprise: " + sadness);
            System.out.println("Calm: " + calmness);
        }

        if(control)         // Control Experiment
            executionControl(joy, calmness, anger, sadness, choice);
        else
            execution(joy, calmness, anger, sadness);

    }

    /**
     * Shows cover album and song info at the top right
     * Also shows the progress bar
     */
    private void showInfo()
    {
        // Wait for kSpPlaybackNotifyMetadataChanged
        try {
            sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ImageView art = findViewById(R.id.coverArt);

        setSongInfo(art);

        timeDur = mPlayer.getMetadata().currentTrack.durationMs;
        bar.setProgress(0);
        bar.setMax((int) TimeUnit.MILLISECONDS.toSeconds(timeDur));

        end.setText(String.format("%d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(timeDur),
                TimeUnit.MILLISECONDS.toSeconds(timeDur) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(timeDur))
        ));

        setProgressValue();

    }

    /**
     * Updates song info for every new song
     * @param art
     */
    private void setSongInfo(final ImageView art)
    {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try
                {
                    synchronized (this) {
                        wait(1000);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                runInfo(art);
                            }
                        });
                    }
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }

            private void runInfo(ImageView art) {
                String artPic = mPlayer.getMetadata().currentTrack.albumCoverWebUrl;
                new DownloadImageTask(art).execute(artPic);

                String name = mPlayer.getMetadata().currentTrack.name;
                String artist = mPlayer.getMetadata().currentTrack.artistName;
                String album = mPlayer.getMetadata().currentTrack.albumName;

                songInfo.setText(String.format("%s\n%s\n%s", name, artist, album));
            }
        });
        thread.start();
    }

    /**
     * Updates the progress song as song is played
     */
    private void setProgressValue() {

        // set the progress
        progressStatus = mPlayer.getPlaybackState().positionMs;
        bar.setProgress((int) TimeUnit.MILLISECONDS.toSeconds(progressStatus));

        // thread is used to change the progress value
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try
                {
                    synchronized (this) {
                        wait(1000);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                current.setText(String.format("%d:%02d",
                                        TimeUnit.MILLISECONDS.toMinutes(progressStatus),
                                        TimeUnit.MILLISECONDS.toSeconds(progressStatus) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(progressStatus))
                                ));
                            }
                        });
                    }
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
                setProgressValue();
            }
        });
        thread.start();
    }

    /**
     * Swap playlists for appropriate queues
     *
     * @param arr1
     * @param arr2
     * @param index1
     * @param index2
     */
    private void swapQueues(String[] arr1, String[] arr2, int index1, int index2)
    {
        String[] t = arr1.clone();
        System.arraycopy(arr2, 0, arr1, 0, t.length);
        System.arraycopy(t, 0, arr2, 0, t.length);

        int tmpNum = index1;
        index1 = index2;
        index2 = tmpNum;    // index 2 = index 1;
    }

    /**
     * Depending on the user's choice, the music player plays appropriate songs. Playlists are
     * swapped so that if the facial expression matches the user’s target emotion, then that playlist
     * must be playing the right music. Otherwise queue a different playlist to try to get to the
     * user’s desired emotion.
     *
     * @param joy
     * @param calmness
     * @param anger
     * @param sadness
     * @param choice
     */
    private void executionControl(float joy, float calmness, float anger, float sadness, int choice)
    {
        // Queue one song after new song starts playing
        // Data points only taken when music is playing
        if (is_queued || is_paused)
            return;

        if (choice == 1)            // Want to feel calm
        {
            if (simpleMovingAverage(calmness, 4) && (calmPlaylistIndex < calm.length))
            {
                System.out.println("User is calm");
                queueTheMusic(calm, calmPlaylistIndex++);
            }
            else if ((simpleMovingAverage(joy, 1) || simpleMovingAverage(anger, 2) || simpleMovingAverage(sadness, 3)) && calmBPlaylistIndex < calmB.length)
            {
                System.out.println("User is not calm");
                queueTheMusic(calmB, calmBPlaylistIndex++);
                swapQueues(calm, calmB, calmPlaylistIndex, calmBPlaylistIndex);
            }
        }
        else if (choice == 2)       // Want to feel happy
        {
            // Calm user down
            if ((simpleMovingAverage(anger, 2) || simpleMovingAverage(sadness, 3)) && calmPlaylistIndex < calm.length)
            {
                System.out.println("User is angry/sad");
                queueTheMusic(calm, calmPlaylistIndex++);
            }
            else if (simpleMovingAverage(calmness, 4) && (joyBPlaylistIndex < joyfulB.length))
            {
                System.out.println("User is calm");
                queueTheMusic(joyfulB, joyBPlaylistIndex++);

                if(is_happy_before)
                {
                    swapQueues(joyful, joyfulB, joyPlaylistIndex, joyBPlaylistIndex);
                }
            }
            else if (simpleMovingAverage(joy, 1) && (joyPlaylistIndex < joyful.length))
            {
                System.out.println("User is happy");
                queueTheMusic(joyful, joyPlaylistIndex++);

                is_happy_before = true;
            }
        }

    }

    /**
     * The user automatically given songs that try to boost his/her mood. If the user is angry or sad,
     * then the music player will try to calm the user down. If the user is calm or, the music player
     * will try to boost or keep the user's mood up.
     *
     * @param joy
     * @param calmness
     * @param anger
     * @param sadness
     */
    private void execution(float joy, float calmness, float anger, float sadness)
    {
        // Queue one song after new song starts playing
        // Data points only taken when music is playing
        if (is_queued || is_paused)
            return;

        // Calm user down
        if ((simpleMovingAverage(anger, 2) || simpleMovingAverage(sadness, 3)) && calmPlaylistIndex < calm.length)
        {
            System.out.println("Anger/Sadness detected");
            queueTheMusic(calm, calmPlaylistIndex++);
        }
        // Boost or keep user's mood up
        else if ((simpleMovingAverage(joy, 1) || simpleMovingAverage(calmness, 4)) && joyPlaylistIndex < joyful.length)
        {
            System.out.println("Joy/Calm detected");
            queueTheMusic(joyful, joyPlaylistIndex++);
        }

    }

    private void queueTheMusic(String[] playlist, int playlistIndex)
    {
        // Delay needed to queue songs
        try {
            sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        mPlayer.queue(null, playlist[playlistIndex]);
        is_queued = true;
        newText.setVisibility(View.VISIBLE);
        mPlayer.resume(null);
    }

    private boolean simpleMovingAverage(float emotion, int type)
    {
        boolean change = false;
        switch(type)
        {
            case 1:  // Joy
                change = addToMovingAverage(emotion, simpleAVGJoy, false);
//                addToCSV(emotion, 1);
                break;
            case 2:  // Anger
                change = addToMovingAverage(emotion, simpleAVGAnger, false);
//                addToCSV(emotion, 2);
                break;
            case 3:  // Sad
                change = addToMovingAverage(emotion, simpleAVGSadness, false);
//                addToCSV(emotion, 3);
                break;
            case 4:  // Calm
                change = addToMovingAverage(emotion, simpleAVGCalm, true);
//                addToCSV(emotion, 4);
                break;
            default:
                break;
        }

        return change;

    }

    /**
     * Writes data points to csv file
     *
     * @param emotion
     * @param type
     */
    private void addToCSV(float emotion, int type)
    {
        String baseDir = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();

        String fileName = "data5.csv";
        String filePath = baseDir + File.separator + fileName;
        File f = new File(filePath);
        CSVWriter writer = null;
        // File exist
        if(f.exists() && !f.isDirectory()){
            FileWriter mFileWriter = null;
            try {
                mFileWriter = new FileWriter(filePath, true);
            } catch (IOException e) {
                e.printStackTrace();
            }
            writer = new CSVWriter(mFileWriter);
        }
        else {
            try {
                writer = new CSVWriter(new FileWriter(filePath));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        List<String[]> content = new ArrayList<>();
        content.add(new String[] {Integer.toString(type), Float.toString(emotion)});

        writer.writeAll(content);

        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Simple moving average (SMA) of 250 data points for each emotion. When the threshold of 40 is
     * reached for the average of data, the next appropriate song is queued and the data points in
     * the arrays are cleared.
     *
     * @param emotion
     * @param emotionAVG
     * @param isCalmAvg
     * @return Returns true if threshold is met for one of the list of emotions
     */
    private boolean addToMovingAverage(float emotion, List<Float> emotionAVG, boolean isCalmAvg)
    {
        if(emotionAVG.size() < maxDataPoints)
            emotionAVG.add(emotion);
        else
        {
            emotionAVG.remove(0);
            emotionAVG.add(emotion);
        }

        float sum = 0;
        float average;

        float zero = 0;
        // Calculates average
        if((emotionAVG.size() == maxDataPoints) && (!emotionAVG.contains(zero)))
        {
            for(int i = 0; i < maxDataPoints; i++)
                sum += emotionAVG.get(i);

            average = sum / maxDataPoints;

            //  Clear data structure when new song is queued
            //  New data points for every new song
            if(average < 10 && isCalmAvg)
            {
                clearData();
                return true;
            }
            else if(average >= THRESHOLD && !isCalmAvg)
            {
                clearData();
                return true;
            }
        }

        return false;
    }

    private void clearData()
    {
        simpleAVGJoy.clear();
        simpleAVGAnger.clear();
        simpleAVGSadness.clear();
        simpleAVGCalm.clear();
    }

}

/**
 * For showing album cover
 */
class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
    private ImageView bmImage;

    public DownloadImageTask(ImageView bmImage) {
        this.bmImage = bmImage;
    }

    protected Bitmap doInBackground(String... urls) {
        String urldisplay = urls[0];
        Bitmap mIcon11 = null;
        try {
            InputStream in = new java.net.URL(urldisplay).openStream();
            mIcon11 = BitmapFactory.decodeStream(in);
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
            e.printStackTrace();
        }
        return mIcon11;
    }

    protected void onPostExecute(Bitmap result) {
        bmImage.setImageBitmap(result);
    }
}