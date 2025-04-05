package com.example.music_player;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;



public class MainActivity extends AppCompatActivity {

    TextView songTitle;
    SeekBar seekBar;
    Button playBtn, nextBtn, prevBtn;

    MediaPlayer mediaPlayer;
    Handler handler = new Handler();
    ArrayList<File> songs;
    int position = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            initPlayer();
        } else {
            Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
        }
    }

    void initPlayer() {
        setContentView(R.layout.activity_main);

        songTitle = findViewById(R.id.songTitle);
        seekBar = findViewById(R.id.seekBar);
        playBtn = findViewById(R.id.playBtn);
        nextBtn = findViewById(R.id.nextBtn);
        prevBtn = findViewById(R.id.prevBtn);

        songs = findSongs(Environment.getExternalStorageDirectory());
        playSong(position);

        playBtn.setOnClickListener(v -> {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                playBtn.setText("Play");
            } else {
                mediaPlayer.start();
                playBtn.setText("Pause");
            }
        });

        nextBtn.setOnClickListener(v -> {
            position = (position + 1) % songs.size();
            playSong(position);
        });

        prevBtn.setOnClickListener(v -> {
            position = (position - 1 < 0) ? songs.size() - 1 : position - 1;
            playSong(position);
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) mediaPlayer.seekTo(progress);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    void playSong(int pos) {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }

        File songFile = songs.get(pos);
        mediaPlayer = MediaPlayer.create(this, Uri.parse(songFile.getAbsolutePath()));
        songTitle.setText(songFile.getName());
        mediaPlayer.start();
        playBtn.setText("Pause");

        seekBar.setMax(mediaPlayer.getDuration());

        handler.postDelayed(updateSeekBar, 1000);
    }

    Runnable updateSeekBar = new Runnable() {
        @Override
        public void run() {
            if (mediaPlayer != null) {
                seekBar.setProgress(mediaPlayer.getCurrentPosition());
                handler.postDelayed(this, 1000);
            }
        }
    };

    public ArrayList<File> findSongs(File root) {
        ArrayList<File> songList = new ArrayList<>();
        File[] files = root.listFiles();

        for (File file : files) {
            if (file.isDirectory() && !file.isHidden()) {
                songList.addAll(findSongs(file));
            } else {
                if (file.getName().endsWith(".mp3")) {
                    songList.add(file);
                }
            }
        }
        return songList;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }
}
