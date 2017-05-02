package com.orobator.exoplayerdemo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import java.io.File;

public class MainActivity extends AppCompatActivity {
  SimpleExoPlayer exoPlayer;
  EventLogger eventLogger;
  Handler mainHandler;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    ButterKnife.bind(this);

    mainHandler = new Handler();
  }

  @OnClick(R.id.init_player_Button) void initializePlayer() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
          != PackageManager.PERMISSION_GRANTED) {
        requestPermissions(new String[] { Manifest.permission.READ_EXTERNAL_STORAGE }, 0);
        return;
      }
    }

    Toast.makeText(this, "Initializing Player", Toast.LENGTH_SHORT).show();

    DefaultRenderersFactory renderersFactory = new DefaultRenderersFactory(this, null,
        DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF);

    DefaultTrackSelector trackSelector = new DefaultTrackSelector();

    exoPlayer = ExoPlayerFactory.newSimpleInstance(renderersFactory, trackSelector);

    eventLogger = new EventLogger(trackSelector);

    exoPlayer.addListener(eventLogger);
    exoPlayer.setAudioDebugListener(eventLogger);
    exoPlayer.setMetadataOutput(eventLogger);
    exoPlayer.setPlayWhenReady(true);

    File songFile = new File("/sdcard/Music/Music/Tory Lanez/Say It/01 Say It.mp3");

    Uri songUri = Uri.fromFile(songFile);

    MediaSource mediaSource =
        new ExtractorMediaSource(songUri, new DefaultDataSourceFactory(this, "Dummy User Agent"),
            new DefaultExtractorsFactory(), mainHandler, eventLogger);

    exoPlayer.prepare(mediaSource);
  }

  @Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
      @NonNull int[] grantResults) {
    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
      initializePlayer();
    } else {
      Toast.makeText(this, "Storage Permission Denied", Toast.LENGTH_SHORT).show();
    }
  }
}
