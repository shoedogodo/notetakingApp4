package com.example.notetakingapp4;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AudioPlayerActivity extends AppCompatActivity {

    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_player);

        Button playButton = findViewById(R.id.play_button);

        Intent intent = getIntent();
        Uri audioUri = Uri.parse(intent.getStringExtra("audio_uri"));

        mediaPlayer = MediaPlayer.create(this, audioUri);

        if (mediaPlayer == null) {
            // MediaPlayer 创建失败，可能是因为 URI 无效或媒体文件问题
            Toast.makeText(this, "Unable to play audio. Please check the file.", Toast.LENGTH_LONG).show();
            playButton.setEnabled(false); // 禁用播放按钮
            return;
        }


        playButton.setOnClickListener(v -> {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                playButton.setText("Play");
            } else {
                mediaPlayer.start();
                playButton.setText("Stop");
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}