package com.example.voicememos;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.os.IBinder;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.view.MenuItem;
import android.view.View;
import android.os.Bundle;
import android.os.Environment;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.MediaController;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class ListFilesActivity extends AppCompatActivity {

    private ArrayList<File> Files = new ArrayList<File>();
    private AudioService musicSrv;
    private Intent playIntent;
    private boolean musicBound=false;
    private ServiceConnection musicConnection = new ServiceConnection(){

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            AudioService.MusicBinder binder = (AudioService.MusicBinder)service;
            //get service
            musicSrv = binder.getService();
            //pass list
            musicSrv.setList(Files);
            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_files);

        String path = getApplicationContext().getFilesDir().getAbsolutePath();
        File directory = new File(path);
        File[] files = directory.listFiles();

        for (int i = 0; i < files.length; i++){
            Files.add(files[i]);
        }

        ListView listView = (ListView)findViewById(R.id.list_files);

        ArrayAdapter<File> arrayAdapter = new ArrayAdapter<File>(
                this,
                android.R.layout.simple_list_item_1,
                Files );

        listView.setAdapter(arrayAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?>adapter,View v, int i, long l){
                File item = (File)adapter.getItemAtPosition(i);

                musicSrv.setSong(i);
                musicSrv.playAudio();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        if(playIntent==null){
            playIntent = new Intent(this, AudioService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
        }
    }

    /*@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_shuffle:
                //shuffle
                break;
            case R.id.action_end:
                stopService(playIntent);
                musicSrv=null;
                System.exit(0);
                break;
        }
        return super.onOptionsItemSelected(item);
    }*/

    @Override
    protected void onDestroy() {
        stopService(playIntent);
        musicSrv = null;
        super.onDestroy();
    }
}