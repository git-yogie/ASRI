package com.yogie.anemiaapps.helper;

import static android.icu.number.NumberRangeFormatter.with;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.widget.Toast;
import android.app.AlarmManager;


import androidx.core.app.AlarmManagerCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.yogie.anemiaapps.MainActivity;
import com.yogie.anemiaapps.R;

public class AlaramReceiver extends BroadcastReceiver {
    public static String NOTIFICATION_ID = "notification-id";
    public static String NOTIFICATION = "notification";
    private static final String CHANNEL_ID = "com.yogie.anemiaapps.NOTIFICATION_CHANNEL";


//    @Override
//    public void onReceive(Context context, Intent intent) {
//
//        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
//        int importance = NotificationManager.IMPORTANCE_HIGH;
//        NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, "Pengingat", importance);
//        notificationManager.createNotificationChannel(notificationChannel);
//
//        Notification notification = intent.getParcelableExtra(NOTIFICATION);
//        int id = intent.getIntExtra(NOTIFICATION_ID, 0);
//        notificationManager.notify(id, notification);
//
//        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
//        if (vibrator != null) {
//            vibrator.vibrate(VibrationEffect.createOneShot(4000, VibrationEffect.DEFAULT_AMPLITUDE));
//        }
//
//        Uri alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
//        if (alarmUri == null) {
//            alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
//        }
//        Ringtone ringtone = RingtoneManager.getRingtone(context, alarmUri);
//        ringtone.play();
//        // Consider the user experience when deciding the duration to play the ringtone
//        // Consider whether showing this toast is necessary if you're also showing a notification
//        Toast.makeText(context, "Saatnya Minum Obat", Toast.LENGTH_LONG).show();
//    }


    @Override
    public void onReceive(Context context, Intent intent) {

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = intent.getParcelableExtra(NOTIFICATION);


        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel notificationChannel = new NotificationChannel(MainActivity.NOTIFICATION_CHANNEL_ID,
                    "Pengingat", importance);

            assert notificationManager != null;
            notificationManager.createNotificationChannel(notificationChannel);

            int id = intent.getIntExtra(NOTIFICATION_ID, 0);
            if (notificationManager != null) {
                notificationManager.notify(id, notification);
            }
            Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.vibrate(4000);

            Toast.makeText(context, "Waktunya minum obat yaa.", Toast.LENGTH_LONG).show();
            Uri alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            if (alarmUri == null) {
                alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            }

            // setting default ringtone
            Ringtone ringtone = RingtoneManager.getRingtone(context, alarmUri);

            // play ringtone
            ringtone.play();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    ringtone.stop();
                }
            }, 15000);

        }

        Toast.makeText(context, "Saatnya Minum Obat", Toast.LENGTH_LONG).show();

    }
}