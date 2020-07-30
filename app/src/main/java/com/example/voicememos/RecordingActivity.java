package com.example.voicememos;

import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaRecorder;

import android.os.Environment;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.pm.PackageManager;

public class RecordingActivity extends AppCompatActivity {

    Boolean isReleased;
    Button Start, Stop, NewRecording;
    ImageButton PlayRecording, PauseRecording;
    String AudioSavePathInDevice = null, Filename;
    MediaRecorder mediaRecorder ;
    TextView songName, startTime;
    SeekBar songPrgs;
    Random random ;
    String RandomAudioFileName = "ABCDEFGHIJKLMNOP";
    private static int oTime =0, sTime =0, eTime =0, fTime = 5000, bTime = 5000;
    private Handler hdlr = new Handler();
    public static final int RequestPermissionCode = 1;
    MediaPlayer mediaPlayer ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recording);

        Button btn = (Button)findViewById(R.id.viewbutton);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RecordingActivity.this, ListFilesActivity.class));
            }
        });

        Start = (Button) findViewById(R.id.button);
        Stop = (Button) findViewById(R.id.button2);
        PlayRecording = (ImageButton) findViewById(R.id.playButton);
        PauseRecording = (ImageButton)findViewById(R.id.pauseButton);
        NewRecording = (Button)findViewById(R.id.newRecordingButton);
        songName = (TextView)findViewById(R.id.txtSname);
        startTime = (TextView)findViewById(R.id.txtStartTime);
        songPrgs = (SeekBar)findViewById(R.id.sBar);
        songPrgs.setClickable(false);
        Stop.setVisibility(View.INVISIBLE);
        NewRecording.setVisibility(View.INVISIBLE);
        PlayRecording.setVisibility(View.INVISIBLE);
        PauseRecording.setVisibility(View.INVISIBLE);
        songName.setVisibility(View.INVISIBLE);
        songPrgs.setVisibility(View.INVISIBLE);
        startTime.setVisibility(View.INVISIBLE);

        random = new Random();

        Start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(checkPermission()) {
                    String path = getApplicationContext().getFilesDir().getAbsolutePath() + "/";
                    Filename = CreateRandomAudioFileName(5) + "AudioRecording.mp3";
                    AudioSavePathInDevice = path + Filename;

                    MediaRecorderReady();

                    try {
                        mediaRecorder.prepare();
                        mediaRecorder.start();
                    } catch (IllegalStateException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    Start.setVisibility(View.INVISIBLE);
                    Stop.setVisibility(View.VISIBLE);

                    Toast.makeText(RecordingActivity.this, "Recording started",
                            Toast.LENGTH_LONG).show();
                } else {
                    requestPermission();
                }

            }
        });

        Stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaRecorder.stop();
                Stop.setVisibility(View.INVISIBLE);
                PlayRecording.setVisibility(View.VISIBLE);
                Start.setVisibility(View.INVISIBLE);
                NewRecording.setVisibility(View.VISIBLE);

                Toast.makeText(RecordingActivity.this, "Recording Completed",
                        Toast.LENGTH_LONG).show();

            }
        });

        PlayRecording.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) throws IllegalArgumentException,
                    SecurityException, IllegalStateException {

                Stop.setVisibility(View.INVISIBLE);
                Start.setVisibility(View.INVISIBLE);
                PauseRecording.setVisibility(View.VISIBLE);
                NewRecording.setVisibility(View.VISIBLE);

                mediaPlayer = new MediaPlayer();
                try {
                    mediaPlayer.setDataSource(AudioSavePathInDevice);
                    mediaPlayer.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                eTime = mediaPlayer.getDuration();
                sTime = mediaPlayer.getCurrentPosition();
                if(oTime == 0){
                    songPrgs.setMax(eTime);
                    oTime =1;
                }

                startTime.setText(String.format("%d min, %d sec", TimeUnit.MILLISECONDS.toMinutes(sTime),
                        TimeUnit.MILLISECONDS.toSeconds(sTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS. toMinutes(sTime))) );
                songPrgs.setProgress(sTime);
                songName.setText(Filename);
                hdlr.postDelayed(UpdateSongTime, 100);
                isReleased = false;

                songName.setVisibility(View.VISIBLE);
                songPrgs.setVisibility(View.VISIBLE);
                startTime.setVisibility(View.VISIBLE);

                mediaPlayer.start();
                Toast.makeText(RecordingActivity.this, "Recording Playing",
                        Toast.LENGTH_LONG).show();
            }
        });

        NewRecording.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Stop.setVisibility(View.INVISIBLE);
                Start.setVisibility(View.VISIBLE);
                NewRecording.setVisibility(View.INVISIBLE);
                PlayRecording.setVisibility(View.INVISIBLE);
                PauseRecording.setVisibility(View.INVISIBLE);
                songName.setVisibility(View.INVISIBLE);
                songPrgs.setVisibility(View.INVISIBLE);
                startTime.setVisibility(View.INVISIBLE);

                isReleased = true;

                if(mediaPlayer != null){
                    mediaPlayer.stop();
                    mediaPlayer.reset();
                    mediaPlayer.release();
                    mediaPlayer = null;
                    oTime = 0;
                    MediaRecorderReady();
                }
            }
        });

        PauseRecording.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Stop.setVisibility(View.INVISIBLE);
                Start.setVisibility(View.INVISIBLE);
                NewRecording.setVisibility(View.VISIBLE);
                PlayRecording.setVisibility(View.VISIBLE);
                PauseRecording.setVisibility(View.INVISIBLE);

                mediaPlayer.pause();
            }
        });

    }

    public void MediaRecorderReady(){
        mediaRecorder=new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        mediaRecorder.setOutputFile(AudioSavePathInDevice);
    }

    public String CreateRandomAudioFileName(int string){
        StringBuilder stringBuilder = new StringBuilder( string );
        int i = 0 ;
        while(i < string ) {
            stringBuilder.append(RandomAudioFileName.
                    charAt(random.nextInt(RandomAudioFileName.length())));

            i++ ;
        }
        return stringBuilder.toString();
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(RecordingActivity.this, new
                String[]{WRITE_EXTERNAL_STORAGE, RECORD_AUDIO}, RequestPermissionCode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case RequestPermissionCode:
                if (grantResults.length> 0) {
                    boolean StoragePermission = grantResults[0] ==
                            PackageManager.PERMISSION_GRANTED;
                    boolean RecordPermission = grantResults[1] ==
                            PackageManager.PERMISSION_GRANTED;

                    if (StoragePermission && RecordPermission) {
                        Toast.makeText(RecordingActivity.this, "Permission Granted",
                                Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(RecordingActivity.this,"Permission Denied",Toast.LENGTH_LONG).show();
                    }
                }
                break;
        }
    }

    public boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(),
                WRITE_EXTERNAL_STORAGE);
        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(),
                RECORD_AUDIO);
        return result == PackageManager.PERMISSION_GRANTED &&
                result1 == PackageManager.PERMISSION_GRANTED;
    }

    private Runnable UpdateSongTime = new Runnable() {
        @Override
        public void run() {
            if (isReleased == true) {
                return;
            }
            sTime = mediaPlayer.getCurrentPosition();
            startTime.setText(String.format("%d min, %d sec", TimeUnit.MILLISECONDS.toMinutes(sTime),
                    TimeUnit.MILLISECONDS.toSeconds(sTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(sTime))) );
            songPrgs.setProgress(sTime);

            if (sTime == eTime) {
                PlayRecording.setVisibility(View.VISIBLE);
                PauseRecording.setVisibility(View.INVISIBLE);
            }
            hdlr.postDelayed(this, 100);
        }
    };
}