package com.example.darker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class DarkerActivity extends AppCompatActivity {
    static DarkerActivity activity;
    SharedPreferences settings;
    SharedPreferences.Editor editor;
    Intent intent;
    SeekBar brightness;
    TextView percentage;
    Switch mySwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        intent = new Intent(getApplicationContext(), DarkerService.class);
        settings = getSharedPreferences("Darker", 0);
        editor = settings.edit();
        setContentView(R.layout.darker_activity);
        brightness = findViewById(R.id.brightness);
        percentage = findViewById(R.id.percentage);
        boolean active = settings.getBoolean("Darker", false);
        int state = settings.getInt("Brightness", 50);
        ((TextView)findViewById(R.id.quick1Brightness)).setText(settings.getInt("quick1", 20)+"%");
        ((TextView)findViewById(R.id.quick2Brightness)).setText(settings.getInt("quick2", 50)+"%");
        ((TextView)findViewById(R.id.quick3Brightness)).setText(settings.getInt("quick3", 80)+"%");
        findViewById(R.id.quick1Brightness).setOnClickListener(e->changeState(settings.getInt("quick1", 20)));
        findViewById(R.id.quick2Brightness).setOnClickListener(e->changeState(settings.getInt("quick2", 50)));
        findViewById(R.id.quick3Brightness).setOnClickListener(e->changeState(settings.getInt("quick3", 80)));
        findViewById(R.id.quick1Save).setOnClickListener(e->setQuick("quick1", R.id.quick1Brightness));
        findViewById(R.id.quick2Save).setOnClickListener(e->setQuick("quick2", R.id.quick2Brightness));
        findViewById(R.id.quick3Save).setOnClickListener(e->setQuick("quick3", R.id.quick3Brightness));
        changeState(active);
        changeState(state);
        brightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser) {
                changeState(progressValue);
                if(mySwitch.isChecked())startService(intent);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        activity=this;
        if (Build.VERSION.SDK_INT >= 23 && !Settings.canDrawOverlays(this))startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName())));
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        final MenuItem toggle = menu.findItem(R.id.myswitch);
        mySwitch = (Switch) toggle.getActionView();
        boolean active = settings.getBoolean("Darker", false);
        mySwitch.setChecked(active);
        mySwitch.setOnCheckedChangeListener((buttonView, isChecked) -> changeState(isChecked));
        return true;
    }

    public void saveState(int state){
        editor.putInt("Brightness", state);
        editor.commit();
    }

    public void saveState(boolean state){
        editor.putBoolean("Darker", state);
        editor.commit();
    }

    public void changeState(boolean state){
        brightness.setActivated(state);
        if(mySwitch!=null)mySwitch.setChecked(state);
        if (state) startService(intent);
        if (!state) stopService(intent);
        saveState(state);
    }

    public void changeState(int state){
        DarkerService.brightness=state;
        brightness.setProgress(state);
        percentage.setText(state+"%");
        saveState(state);
    }

    private void setQuick(String name, int id){
        editor.putInt(name, brightness.getProgress());
        editor.commit();
        ((TextView)findViewById(id)).setText(brightness.getProgress()+"%");
        if(mySwitch.isChecked())startService(intent);
    }

    public static DarkerActivity getActivity(){
        return activity;
    }
}