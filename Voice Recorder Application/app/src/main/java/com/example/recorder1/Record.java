package com.example.recorder1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Record extends AppCompatActivity {


    private static final int Request_Audio_permission_code=101;
    MediaRecorder mediaRecorder;
    MediaPlayer mediaPlayer;
    ImageView ibRecord;
    ImageView ibPlay;
    TextView tvTime;
    TextView tvRecordingPath;
    ImageView voice1;
    boolean isRecording=false;
    boolean isPlaying=false;

    Button button;
    int seconds=0;
    String path=null;
    int dummySeconds=0;
    int playableSeconds=0;
    Handler handler;

    ExecutorService executorService= Executors.newSingleThreadExecutor();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();

        ibRecord = findViewById(R.id.ib_record);
        ibPlay = findViewById(R.id.ib_play);
        tvTime = findViewById(R.id.tv_time);
        tvRecordingPath = findViewById(R.id.tv_recording_path);
        voice1 = findViewById(R.id.logo);
        mediaPlayer = new MediaPlayer();

        button = (Button) findViewById(R.id.button_back_record);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openPage2();
            }

        });
        button = (Button) findViewById(R.id.button_analyse_record);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openAnalyse();
            }

        });

        ibRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkRecordingPermission())
                {
                    if(!isRecording)
                    {
                        isRecording=true;
                        executorService.execute(new Runnable() {
                            @Override
                            public void run() {
                                mediaRecorder=new MediaRecorder();
                                mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                                mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                                mediaRecorder.setOutputFile(getRecordingFilePath());
                                path=getRecordingFilePath();
                                mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

                                mediaRecorder.setAudioSamplingRate(16000);

                                try {
                                    mediaRecorder.prepare();
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                                mediaRecorder.start();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        voice1.setVisibility(View.VISIBLE);
                                        tvRecordingPath.setText(getRecordingFilePath());
                                        playableSeconds=0;
                                        seconds=0;
                                        dummySeconds=0;
                                        ibRecord.setImageDrawable(ContextCompat.getDrawable(Record.this,R.drawable.recordpause1));
                                        runTimer();
                                    }
                                });

                            }

                        });

                    }

                    else
                    {
                        executorService.execute(new Runnable() {
                            @Override
                            public void run() {
                                mediaRecorder.stop();
                                mediaRecorder.release();
                                mediaRecorder=null;
                                playableSeconds=seconds;
                                dummySeconds=seconds;
                                seconds=0;
                                isRecording=false;


                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        voice1.setVisibility(View.VISIBLE);
                                        handler.removeCallbacksAndMessages(null);
                                        ibRecord.setImageDrawable(ContextCompat.getDrawable(Record.this,R.drawable.record));
                                    }
                                });

                            }
                        });
                    }
                }
                else
                {
                    requestRecordingPermission();
                }
            }
        });

        ibPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isPlaying)
                {
                    if(path!=null)
                    {
                        try {
                            mediaPlayer.setDataSource(getRecordingFilePath());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    else
                    {
                        Toast.makeText(getApplicationContext(),"List is Empty",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    try {
                        mediaPlayer.prepare();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    mediaPlayer.start();
                    isPlaying=true;
                    ibPlay.setImageDrawable(ContextCompat.getDrawable(Record.this,R.drawable.pause));
                    runTimer();
                }
                else
                {
                    mediaPlayer.stop();
                    mediaPlayer.release();
                    mediaPlayer=null;
                    mediaPlayer=new MediaPlayer();
                    isPlaying=false;
                    seconds=0;
                    handler.removeCallbacksAndMessages(null);
                    voice1.setVisibility(View.VISIBLE);
                    ibPlay.setImageDrawable(ContextCompat.getDrawable(Record.this,R.drawable.play));
                }
            }
        });

    }

    public void openPage2() {
        Intent intent = new Intent(this, Page2.class);
        startActivity(intent);
    }
    public void openAnalyse() {
        Intent intent = new Intent(this, maintainance.class);
        startActivity(intent);
    }
    private void runTimer()
    {
        handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                int minutes=(seconds%3600)/60;
                int secs=seconds%60;
                String time=String.format(Locale.getDefault(),"%02d:%02d",minutes,secs);
                tvTime.setText(time);

                if(isRecording || (isPlaying && playableSeconds!=-1))
                {
                    seconds++;
                    playableSeconds--;

                    if(playableSeconds==-1 && isPlaying)
                    {
                        mediaPlayer.stop();
                        mediaPlayer.release();
                        isPlaying=false;
                        mediaPlayer=null;
                        mediaPlayer= new MediaPlayer();
                        playableSeconds= dummySeconds;
                        seconds=0;
                        handler.removeCallbacksAndMessages(null);
                        voice1.setVisibility(View.VISIBLE);
                        ibPlay.setImageDrawable(ContextCompat.getDrawable(Record.this,R.drawable.play));
                        return;

                    }
                }

                handler.postDelayed(this,1000);
            }
        });
    }

    private void requestRecordingPermission()
    {
        ActivityCompat.requestPermissions(Record.this,new String[]{android.Manifest.permission.RECORD_AUDIO},Request_Audio_permission_code);
    }

    public boolean checkRecordingPermission()
    {
        if(ContextCompat.checkSelfPermission( this,android.Manifest.permission.RECORD_AUDIO)== PackageManager.PERMISSION_DENIED)
        {
            requestRecordingPermission();
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==Request_Audio_permission_code)
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

    private String getRecordingFilePath()
    {
        ContextWrapper contextWrapper= new ContextWrapper(getApplicationContext());
        File music= contextWrapper.getExternalFilesDir(Environment.DIRECTORY_MUSIC);
        File file= new File(music, "testFile"+".wav");
        return file.getPath();

    }
}