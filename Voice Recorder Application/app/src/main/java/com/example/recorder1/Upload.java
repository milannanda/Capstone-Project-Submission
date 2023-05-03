package com.example.recorder1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.database.Cursor;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
public class Upload extends AppCompatActivity {
Button button;
    TextView textview2;
    TextView textview3;
    Button button1;
    Button button2;
    SeekBar seekbar1;

    String duration;
    MediaPlayer mediaPlayer;
    ScheduledExecutorService timer;
    public static final int PICK_FILE =99;
    private static final int Request_Storage_permission_code=101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        button1 = findViewById(R.id.button1);
        button2 = findViewById(R.id.button2);
        textview2 = findViewById(R.id.textView2);
        textview3 = findViewById(R.id.textView3);
        seekbar1 = findViewById(R.id.seekbar1);
        button =findViewById(R.id.button_back_upload);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openPage2();
            }

        });
        button = (Button) findViewById(R.id.button_analyse_upload);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openAnalyse();
            }

        });


        button1.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (checkStoragePermission()) {
                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("audio/*");
                    startActivityForResult(intent, PICK_FILE);
                }
                else {
                    requestStoragePermission();
                }
            }
    });


    button2.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mediaPlayer != null) {
                if (mediaPlayer.isPlaying()){
                    mediaPlayer.pause();
                    button2.setText("PLAY");
                    timer.shutdown();
                } else {
                    mediaPlayer.start();
                    button2.setText("PAUSE");

                    timer = Executors.newScheduledThreadPool(1);
                    timer.scheduleAtFixedRate(new Runnable() {
                        @Override
                        public void run() {
                            if (mediaPlayer != null) {
                                if (!seekbar1.isPressed()) {
                                    seekbar1.setProgress(mediaPlayer.getCurrentPosition());
                                }
                            }
                        }
                    }, 10, 10, TimeUnit.MILLISECONDS);
                }
            }
        }
    });

        seekbar1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (mediaPlayer != null){
                int millis = mediaPlayer.getCurrentPosition();
                long total_secs = TimeUnit.SECONDS.convert(millis, TimeUnit.MILLISECONDS);
                long mins = TimeUnit.MINUTES.convert(total_secs, TimeUnit.SECONDS);
                long secs = total_secs - (mins*60);
                textview3.setText(mins + ":" + secs + " / " + duration);
            }
        }
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            if (mediaPlayer != null) {
                mediaPlayer.seekTo(seekbar1.getProgress());
            }
        }
    });

        button2.setEnabled(false);
}
    public void openAnalyse() {
        Intent intent = new Intent(this, maintainance.class);
        startActivity(intent);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FILE && resultCode == RESULT_OK){
            if (data != null){
                Uri uri = data.getData();
                createMediaPlayer(uri);
            }
        }
    }

    public void createMediaPlayer(Uri uri){
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioAttributes(
                new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
        );
        try {
            mediaPlayer.setDataSource(getApplicationContext(), uri);
            mediaPlayer.prepare();

            textview2.setText(getNameFromUri(uri));
            button2.setEnabled(true);

            int millis = mediaPlayer.getDuration();
            long total_secs = TimeUnit.SECONDS.convert(millis, TimeUnit.MILLISECONDS);
            long mins = TimeUnit.MINUTES.convert(total_secs, TimeUnit.SECONDS);
            long secs = total_secs - (mins*60);
            duration = mins + ":" + secs;
            textview3.setText("00:00 / " + duration);
            seekbar1.setMax(millis);
            seekbar1.setProgress(0);

            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    releaseMediaPlayer();
                }
            });
        } catch (IOException e){
            textview2.setText(e.toString());
        }
    }

    @SuppressLint("Range")
    public String getNameFromUri(Uri uri){
        String fileName = "";
        Cursor cursor = null;
        cursor = getContentResolver().query(uri, new String[]{
                MediaStore.Images.ImageColumns.DISPLAY_NAME
        }, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            fileName = cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DISPLAY_NAME));
        }
        if (cursor != null) {
            cursor.close();
        }
        return fileName;
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseMediaPlayer();
    }

    public void releaseMediaPlayer(){
        if (timer != null) {
            timer.shutdown();
        }
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        button2.setEnabled(false);
        textview2.setText("TITLE");
        textview3.setText("00:00 / 00:00");
        seekbar1.setMax(100);
        seekbar1.setProgress(0);
    }
    public void openPage2() {
        Intent intent = new Intent(this, Page2.class);
        startActivity(intent);
    }

    private void requestStoragePermission()
    {
        ActivityCompat.requestPermissions(Upload.this,new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},Request_Storage_permission_code);
    }
    public boolean checkStoragePermission()
    {
        if(ContextCompat.checkSelfPermission( this, android.Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_DENIED)
        {
            requestStoragePermission();
            return false;
        }
        return true;

    }

    public void onRequestPermissionsResult1(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==Request_Storage_permission_code)
        {
            if (grantResults.length>0)
            {
                boolean permissionToRecord=grantResults[0]==PackageManager.PERMISSION_GRANTED;
                if(permissionToRecord)
                {
                    Toast.makeText(getApplicationContext(),"Permission Given", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(getApplicationContext(),"Permission Denied", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}