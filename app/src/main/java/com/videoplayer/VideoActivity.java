package com.videoplayer;

import android.Manifest;
import android.app.AlertDialog;
import android.app.PictureInPictureParams;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Rational;
import android.util.SparseBooleanArray;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.github.rubensousa.previewseekbar.PreviewBar;
import com.github.rubensousa.previewseekbar.PreviewSeekBar;
import com.github.rubensousa.previewseekbar.exoplayer.PreviewTimeBar;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;


public class VideoActivity extends AppCompatActivity {

    private Timer _timer = new Timer();

    private String path = "";
    private HashMap<String, Object> data = new HashMap<>();
    private String title = "";
    private double videoHeight = 0;
    private double videoWidth = 0;
    private boolean isLoading = false;
    private double playbackState = 0;
    private boolean isPortrait = false;
    private boolean loading = false;
    private double position = 0;
    private boolean flag = false;
    private ExoPlayer player;
    private MediaSource mediaSource;

    private PlayerView player_view;

    private TimerTask t;
    private TimerTask tt;
    private TimerTask check;
    private AlertDialog.Builder builder;

    @Override
    protected void onCreate(Bundle _savedInstanceState) {
        super.onCreate(_savedInstanceState);
        setContentView(R.layout.activity_video);
        initialize(_savedInstanceState);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1000);
        } else {
            initializeLogic();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1000) {
            initializeLogic();
        }
    }

    private void initialize(Bundle _savedInstanceState) {
        player_view = findViewById(R.id.player_view);
        builder = new AlertDialog.Builder(this);
    }

    private void initializeLogic() {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (getIntent().hasExtra("data")) {
            data = new Gson().fromJson(getIntent().getStringExtra("data"), new TypeToken<HashMap<String, Object>>() {
            }.getType());
            videoHeight = Double.parseDouble(data.get("videoHeight").toString());
            videoWidth = Double.parseDouble(data.get("videoWidth").toString());
            if (videoWidth > videoHeight) {
                setRequestedOrientation(Build.VERSION.SDK_INT < 9 ?

                        ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE :
                        ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
            }
            path = data.get("videoPath").toString();
            title = Uri.parse(path).getLastPathSegment();
            int orientation = this.getResources().getConfiguration().orientation;
            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                // code for portrait mode
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN);
            } else {
                // code for landscape mode
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN);
            }
        }
        _hide_navigation_bar();
        loading = true;

//        player = ExoPlayerFactory.newSimpleInstance(this, new DefaultTrackSelector(new Factory(new DefaultBandwidthMeter())), new DefaultLoadControl());
        player = new ExoPlayer.Builder(this).build();
        if (getIntent().hasExtra("k")) {
            path = getIntent().getStringExtra("k");
            if (getIntent().getStringExtra("k").contains(".mp4")) {

                String Url_Player = getIntent().getStringExtra("k");
                Uri videoUrl = Uri.parse(Url_Player);
                mediaSource = new ProgressiveMediaSource.Factory(new DefaultDataSourceFactory(this, "video_player"))
                        .createMediaSource(MediaItem.fromUri(videoUrl));
                title = URLUtil.guessFileName(getIntent().getStringExtra("k"), null, null);

            } else {

                String HLS_URL = getIntent().getStringExtra("k");
                Uri uri = Uri.parse(HLS_URL);
                mediaSource = buildMediaSource(uri);

                title = Uri.parse(getIntent().getStringExtra("k")).getLastPathSegment();
            }
        } else {

            String path_video = path;
            File vidoFile = new File(path_video);
            String video = String.valueOf(Uri.fromFile(vidoFile));
            Uri videoUri = Uri.parse(video);
            mediaSource = new ProgressiveMediaSource.Factory(new DefaultDataSourceFactory(this, "video_player"))
                    .createMediaSource(MediaItem.fromUri(videoUri));
        }


        player_view.setPlayer(player);

        player_view.setKeepScreenOn(true);

        player.prepare(mediaSource);
        player.setPlayWhenReady(true);
        _dfg();
        _Events();
    }

    @Override
    public void onBackPressed() {
        finish();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (player != null) {

            player_view.setPlayer(null);

            player.release();

            player = null;

        }
    }

    public void _Extra() {
    }

    protected void onRestart() {
        super.onRestart();
        if (player != null) {
            _hide_navigation_bar();

            player.setPlayWhenReady(true);
            player.getPlaybackState();

        }
    }

    {
    }


    public void _Progress(final ProgressBar _prgs, final String _color) {
        if
        (Build.VERSION.SDK_INT >= 21) {
            _prgs.getIndeterminateDrawable().setColorFilter(Color.parseColor(_color),
                    PorterDuff.Mode.SRC_IN);
        }
    }


    public void _hide_navigation_bar() {
        getWindow().getDecorView()
                .setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                );
    }


    public void _dfg() {
        //Holo dark theme
        builder = new AlertDialog.Builder(this, ProgressDialog.THEME_HOLO_DARK);
    }


    public void _extr() {
    }

    private PreviewTimeBar previewTimeBar;
    private PreviewSeekBar previewSeekBar;
    private ImageView mPreview2;

    {
    }


    public void _speed_types() {
    }

    String[] speed = {"x 0.25", "x 0.75", "Normal", "x 1.25", "x 1.5", "×1.75", "×2",};

    {
    }




    private MediaSource buildMediaSource(Uri uri) {
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this, "exoplayer-codelab");
        return new HlsMediaSource.Factory(dataSourceFactory).createMediaSource(MediaItem.fromUri(uri));
    }




    public void _Events() {

        final ImageView btFullScreen = (ImageView) player_view.findViewById(R.id.bt_fullscreen);

        final ProgressBar prg = (ProgressBar) player_view.findViewById(R.id.progress);

        final ImageView play1 = (ImageView) player_view.findViewById(R.id.exo_play);

        final ImageView pause1 = (ImageView) player_view.findViewById(R.id.exo_pause);

        final ImageView more = (ImageView) player_view.findViewById(R.id.more);

        final TextView mtitle = (TextView) player_view.findViewById(R.id.mtext);

        final ImageView pip = (ImageView) player_view.findViewById(R.id.pip);

        final ImageView back = (ImageView) player_view.findViewById(R.id.back);
        mPreview2 = player_view.findViewById(R.id.imageView2);

        previewTimeBar = player_view.findViewById(R.id.exo_progress);
        //made by GuChiDevStudio.Ltda 
        _Progress(prg, "#FFFFFF");
        try {
            // Listen for scrub touch changes
            previewTimeBar.addOnScrubListener(new PreviewBar.OnScrubListener() {
                @Override
                public void onScrubStart(PreviewBar previewBar) {
                }

                @Override
                public void onScrubMove(PreviewBar previewBar, int progress, boolean fromUser) {


//                    try {
//                        mPreview2.setImageBitmap(UtilsGuChiDevStudio.setPath(path, progress));
//                    } catch (Exception e) {
//                        Utilities.showMessage(getApplicationContext(), "ERROR: ".concat(e.toString()));
//                    }

                }

                @Override
                public void onScrubStop(PreviewBar previewBar) {

                }
            });
        } catch (Exception e) {
            Utilities.showMessage(getApplicationContext(), "ERROR: ".concat(e.toString()));
        }

        player.addListener(new Player.Listener() {
            @Override
            public void onEvents(Player player, Player.Events events) {
                Player.Listener.super.onEvents(player, events);
            }
        });

        player.addListener( new Player.Listener() {

            public void onTimelineChanged(Timeline timeline, Object manifest, int reason) {

            }

            public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
                loading = false;
                pause1.setVisibility(View.VISIBLE);
                prg.setVisibility(View.GONE);
            }

            public void onLoadingChanged(boolean isLoading) {

            }

            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                if (playbackState == 2) {
                    loading = true;
                    prg.setVisibility(View.VISIBLE);
                } else {
                    if (playbackState == 3) {
                        loading = false;
                        prg.setVisibility(View.GONE);
                    }
                }
                if (playbackState == player.STATE_READY && playWhenReady) {
                    previewTimeBar.hidePreview();
                }
            }

            public void onRepeatModeChanged(int repeatMode) {

            }

            public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

            }

            public void onPlayerError(ExoPlaybackException error) {
                Utilities.showMessage(getApplicationContext(), "ERROR: ".concat(error.toString()));
            }

            public void onPositionDiscontinuity(int reason) {

            }

            public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

            }

            public void onSeekProcessed() {

            }

        });
        mtitle.setText(title);
        check = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (loading) {
                            pause1.setVisibility(View.GONE);
                            play1.setVisibility(View.GONE);
                            play1.setAlpha((float) (0));
                            pause1.setAlpha((float) (0));
                        } else {
                            play1.setAlpha((float) (1));
                            pause1.setAlpha((float) (1));
                        }
                    }
                });
            }
        };
        _timer.scheduleAtFixedRate(check, (int) (0), (int) (1));
        more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View _view) {
                builder.setTitle("VIDEO SPEED");
                builder.setItems(speed, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int position) {


                        if ((position + 1) == 1) {

                            try {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    PlaybackParameters param = new PlaybackParameters(0.25f);
                                    player.setPlaybackParameters(param);
                                }

                                _hide_navigation_bar();
                                Utilities.showMessage(getApplicationContext(), "x 0.25");
                            } catch (Exception e) {
                                _hide_navigation_bar();
                                Utilities.showMessage(getApplicationContext(), "Error");
                            }
                        } else {
                            if ((position + 1) == 2) {

                                try {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                        PlaybackParameters param = new PlaybackParameters(0.75f);
                                        player.setPlaybackParameters(param);
                                    }

                                    _hide_navigation_bar();
                                    Utilities.showMessage(getApplicationContext(), "x 0.75");
                                } catch (Exception e) {
                                    _hide_navigation_bar();
                                    Utilities.showMessage(getApplicationContext(), "Error");
                                }
                            } else {
                                if ((position + 1) == 3) {

                                    try {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                            PlaybackParameters param = new PlaybackParameters(1f);
                                            player.setPlaybackParameters(param);
                                        }

                                        _hide_navigation_bar();
                                        Utilities.showMessage(getApplicationContext(), "Normal");
                                    } catch (Exception e) {
                                        _hide_navigation_bar();
                                        Utilities.showMessage(getApplicationContext(), "Error");
                                    }
                                } else {
                                    if ((position + 1) == 4) {

                                        try {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                PlaybackParameters param = new PlaybackParameters(1.25f);
                                                player.setPlaybackParameters(param);
                                            }

                                            _hide_navigation_bar();
                                        } catch (Exception e) {
                                            _hide_navigation_bar();
                                            Utilities.showMessage(getApplicationContext(), "Error");
                                        }
                                    } else {
                                        if ((position + 1) == 5) {

                                            try {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                    PlaybackParameters param = new PlaybackParameters(1.5f);
                                                    player.setPlaybackParameters(param);
                                                }


                                                _hide_navigation_bar();
                                            } catch (Exception e) {
                                                _hide_navigation_bar();
                                                Utilities.showMessage(getApplicationContext(), "Error");
                                            }
                                        } else {
                                            if ((position + 1) == 6) {

                                                try {
                                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                        PlaybackParameters param = new PlaybackParameters(1.75f);
                                                        player.setPlaybackParameters(param);
                                                    }
                                                    _hide_navigation_bar();
                                                } catch (Exception e) {
                                                    _hide_navigation_bar();
                                                    Utilities.showMessage(getApplicationContext(), "Error");
                                                }
                                            } else {
                                                if ((position + 1) == 7) {

                                                    try {
                                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                            PlaybackParameters param = new PlaybackParameters(2f);
                                                            player.setPlaybackParameters(param);
                                                        }
                                                        _hide_navigation_bar();
                                                    } catch (Exception e) {
                                                        _hide_navigation_bar();
                                                        Utilities.showMessage(getApplicationContext(), "Error");
                                                    }
                                                } else {
                                                    //Ads...
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                });
                builder.create().show();
            }
        });
        // on Click FULL SCREEN 
        btFullScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View _view) {
                if (flag) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    flag = false;
                } else {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    flag = true;
                }
            }
        });
        pip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View _view) {
                if (Build.VERSION.SDK_INT >= 26) {
                    //Trigger PiP mode
                    try {
                        Rational rational = new Rational(player_view.getWidth(), player_view.getHeight());
                        PictureInPictureParams mParams =
                                new PictureInPictureParams.Builder()
                                        .setAspectRatio(rational)
                                        .build();
                        enterPictureInPictureMode(mParams);
                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(VideoActivity.this, "API 26 needed to perform PiP", Toast.LENGTH_SHORT).show();
                    ;
                }
            }
        });
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View _view) {
                finish();
            }
        });
    }


    @Deprecated
    public void showMessage(String _s) {
        Toast.makeText(getApplicationContext(), _s, Toast.LENGTH_SHORT).show();
    }

    @Deprecated
    public int getLocationX(View _v) {
        int _location[] = new int[2];
        _v.getLocationInWindow(_location);
        return _location[0];
    }

    @Deprecated
    public int getLocationY(View _v) {
        int _location[] = new int[2];
        _v.getLocationInWindow(_location);
        return _location[1];
    }

    @Deprecated
    public int getRandom(int _min, int _max) {
        Random random = new Random();
        return random.nextInt(_max - _min + 1) + _min;
    }

    @Deprecated
    public ArrayList<Double> getCheckedItemPositionsToArray(ListView _list) {
        ArrayList<Double> _result = new ArrayList<Double>();
        SparseBooleanArray _arr = _list.getCheckedItemPositions();
        for (int _iIdx = 0; _iIdx < _arr.size(); _iIdx++) {
            if (_arr.valueAt(_iIdx))
                _result.add((double) _arr.keyAt(_iIdx));
        }
        return _result;
    }

    @Deprecated
    public float getDip(int _input) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, _input, getResources().getDisplayMetrics());
    }

    @Deprecated
    public int getDisplayWidthPixels() {
        return getResources().getDisplayMetrics().widthPixels;
    }

    @Deprecated
    public int getDisplayHeightPixels() {
        return getResources().getDisplayMetrics().heightPixels;
    }
}