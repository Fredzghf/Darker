package com.example.darker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

public class DarkerTileService extends TileService {
    SharedPreferences settings;
    Intent intent;

    public DarkerTileService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        intent = new Intent(getApplicationContext(), DarkerService.class);
        settings = getSharedPreferences("Darker", 0);
    }

    @Override
    public void onTileAdded() {
        super.onTileAdded();
        // Update state
        boolean active = settings.getBoolean("Darker", false);
        getQsTile().setState(active ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
        // Update looks
        getQsTile().updateTile();
    }

    @Override
    public void onStartListening() {
        super.onStartListening();
        // Update state
        boolean active = settings.getBoolean("Darker", false);
        getQsTile().setState(active ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
        if(active)startService(intent);
        // Update looks
        getQsTile().updateTile();
    }

    @Override
    public void onClick() {
        super.onClick();
        if (getQsTile().getState() == Tile.STATE_INACTIVE) {
            // Turn on
            getQsTile().setState(Tile.STATE_ACTIVE);
            startService(intent);
            try{
                DarkerActivity.getActivity().changeState(true);
            }catch (Exception e){
                saveState(true);
            }
        } else {
            // Turn off
            getQsTile().setState(Tile.STATE_INACTIVE);
            stopService(intent);
            try{
                DarkerActivity.getActivity().changeState(false);
            }catch (Exception e){
                saveState(false);
            }
        }
        // Update looks
        getQsTile().updateTile();
    }

    public void saveState(boolean state){
        SharedPreferences.Editor editor = settings.edit();;
        editor.putBoolean("Darker", state);
        editor.commit();
    }
}
