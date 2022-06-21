package com.example.darker;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.renderscript.RenderScript;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class DarkerService extends Service {
    static int brightness = 50;
    SharedPreferences settings;
    SharedPreferences.Editor editor;
    ViewGroup view;
    WindowManager wm;
    Notification notif;
    NotificationManagerCompat notificationManager;
    RemoteViews notificationLayout;
    NotificationCompat.Builder builder;

    private static final int FLAGS = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL    |
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN   |
            WindowManager.LayoutParams.FLAG_FULLSCREEN        |
            WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR |
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE      |
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS   |
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;

    final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            FLAGS,
            PixelFormat.TRANSLUCENT);

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        settings = getSharedPreferences("Darker", 0);
        editor = settings.edit();
        brightness = settings.getInt("Brightness", 50);
        view = new ViewGroup(getBaseContext()) {
            @Override
            protected void onLayout(boolean b, int i, int i1, int i2, int i3) {
                applyBrightness();
            }
        };
        wm.addView(view, params);
        createNotif();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction()!=null)
        switch (intent.getAction()){
            case "quick1":
                brightness=settings.getInt("quick1", 20);
                break;
            case "quick2":
                brightness=settings.getInt("quick2", 50);
                break;
            case "quick3":
                brightness=settings.getInt("quick3", 80);
                break;
            case "minus":
                brightness-=5;
                if(brightness<0)brightness=0;
                break;
            case "plus":
                brightness+=5;
                if(brightness>100)brightness=100;
            default:break;
        }
        try {DarkerActivity.getActivity().changeState(brightness);}catch (Exception e){editor.putInt("brightness", brightness);editor.commit();}
        applyBrightness();
        addNotif();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public boolean stopService(Intent name) {
        clear();
        return super.stopService(name);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        clear();
    }

    private void applyBrightness(){
        view.setBackgroundColor(Color.argb((int)((100-brightness) / 100f * 255),0,0,0));
    }

    private void clear(){
        wm.removeView(view);
        NotificationManagerCompat.from(this).cancel(1);
    }

    public void addNotif(){
        notificationLayout.setTextViewText(R.id.brightness, brightness+"%");
        notificationLayout.setTextViewText(R.id.quick1, settings.getInt("quick1", 20)+"");
        notificationLayout.setTextViewText(R.id.quick2, settings.getInt("quick2", 50)+"");
        notificationLayout.setTextViewText(R.id.quick3, settings.getInt("quick3", 80)+"");
        notificationLayout.setProgressBar(R.id.progressBar, 100, brightness, false);
        builder.setPriority(NotificationManager.IMPORTANCE_DEFAULT);
        notif = builder.build();
        notificationManager.notify(1, notif);
    }

    public void createNotif(){
        createNotificationChannel();
        notificationLayout = new RemoteViews(getPackageName(), R.layout.notification_layout_animated);

        String[] buttons = new String[]{"quick1", "quick2", "quick3", "minus", "plus"};
        int[] viewId = new int[]{R.id.quick1, R.id.quick2, R.id.quick3, R.id.minus, R.id.plus};
        Intent intentNotif = new Intent(getApplicationContext(), DarkerService.class);
        PendingIntent pendingIntentNotif;
        for (int i=0; i<buttons.length; i++){
            intentNotif.setAction(buttons[i]);
            pendingIntentNotif = PendingIntent.getService(getApplicationContext(), i, intentNotif, PendingIntent.FLAG_UPDATE_CURRENT);
            notificationLayout.setOnClickPendingIntent(viewId[i], pendingIntentNotif);
        }

        builder = new NotificationCompat.Builder(this, "Darker")
                .setSmallIcon(R.drawable.ic_logo)
                .setCustomContentView(notificationLayout);
        addNotif();
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(getString(R.string.app_name), getString(R.string.app_name), NotificationManager.IMPORTANCE_HIGH);
            channel.setSound(null, null);
            channel.enableLights(false);
            channel.enableVibration(false);
            notificationManager = NotificationManagerCompat.from(this);
            notificationManager.createNotificationChannel(channel);
        }
    }

}
