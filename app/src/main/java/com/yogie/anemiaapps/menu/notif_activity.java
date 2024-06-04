package com.yogie.anemiaapps.menu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.google.android.material.button.MaterialButton;
import com.yogie.anemiaapps.MainActivity;
import com.yogie.anemiaapps.R;
import com.yogie.anemiaapps.helper.AlaramReceiver;

import java.util.Calendar;


public class notif_activity extends AppCompatActivity {

    public static final String NOTIFICATION_CHANNEL_ID = "10001";
    private final static String default_notification_channel_id = "default";

    MaterialButton btnNotif;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notif);

        btnNotif = findViewById(R.id.mengerti);


        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            if (checkSelfPermission(Manifest.permission.USE_FULL_SCREEN_INTENT) != PackageManager.PERMISSION_GRANTED
                    || checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.USE_FULL_SCREEN_INTENT, Manifest.permission.POST_NOTIFICATIONS},
                        1);
            }
        }

        btnNotif.setOnClickListener(v -> {
            startAlarm(9, 0, 0);
            startAlarm(15, 0, 0);
            startAlarm(21, 0, 0);

            Intent intent = new Intent(notif_activity.this, MainActivity.class);
            startActivity(intent);
        });


    }

    @SuppressLint("ScheduleExactAlarm")
    private void startAlarm(int hour, int minute, int second){

        Calendar now = Calendar.getInstance();

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, second);


        if (Calendar.getInstance().after(calendar)) {
            calendar.add(Calendar.DATE, 1);
        }

        // Unique request code
        int requestCode = hour * 100 + minute; // Example: 9:00:00 -> 900
        scheduleNotification(getNotification("Halo, Waktunya minum obat!"), calendar.getTimeInMillis(), requestCode);


    }




    @SuppressLint("ScheduleExactAlarm")
    private void scheduleNotification(Notification notification, long delay, int requestCode) {
        Intent notificationIntent = new Intent(this, AlaramReceiver.class);
        notificationIntent.putExtra(AlaramReceiver.NOTIFICATION_ID, 1);
        notificationIntent.putExtra(AlaramReceiver.NOTIFICATION, notification);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, requestCode,
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        long interval = AlarmManager.INTERVAL_DAY; // 24 jam

        if (alarmManager != null) {
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, delay, interval, pendingIntent);

        }
    }

    private Notification getNotification(String content){
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("notification", "calendar");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_MUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getLayoutInflater().getContext(), default_notification_channel_id);
        builder.setContentTitle("Pengingat");
        builder.setContentText(content);
        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(true);
        builder.setSmallIcon(R.drawable.ic_android_black_24dp);
        builder.setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND);
        builder.setChannelId(NOTIFICATION_CHANNEL_ID);
        builder.setPriority(NotificationCompat.PRIORITY_HIGH);
        return builder.build();
    }

    private boolean checkAlarm(int requestCode) {
        Intent intent = new Intent(this, AlaramReceiver.class);
        return PendingIntent.getBroadcast(this, requestCode, intent, PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE) == null;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Izin diberikan. Anda dapat melanjutkan tugas yang memerlukan izin ini.
            } else {
                // Izin ditolak. Anda perlu menangani kasus ini.
            }
        }

    }
}