package com.yogie.anemiaapps;

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
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.view.WindowCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.yogie.anemiaapps.fragmentMenu.Fragment_Beranda;
import com.yogie.anemiaapps.fragmentMenu.Fragment_calendar;
import com.yogie.anemiaapps.fragmentMenu.Fragment_pengaturan;
import com.yogie.anemiaapps.helper.AlaramReceiver;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {


    private static final String CHANNEL_ID = "com.yogie.anemiaapps.NOTIFICATION_CHANNEL";
    public static final String NOTIFICATION_CHANNEL_ID = "10001";
    private final static String default_notification_channel_id = "default";
    private static final int CAMERA_REQUEST_CODE = 100;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        Fragment_Beranda fragment_beranda = new Fragment_Beranda(this);
        Fragment_calendar fragment_calendar = new Fragment_calendar(this);
        Fragment_pengaturan fragment_pengaturan = new Fragment_pengaturan(this);

        final int beranda = R.id.beranda;
        final int calendar = R.id.calendar;
        final int pengaturan = R.id.pengaturan;
        fragment_calendar.onCreateView(getLayoutInflater(), null, savedInstanceState);

        Intent notification = getIntent();
        if(notification.getStringExtra("notification") != null) {
            replaceFragment(fragment_calendar);
        }else{
            replaceFragment(fragment_beranda);
        }


        BottomNavigationView navView = findViewById(R.id.bottom_navigation);
        navView.setOnItemSelectedListener(item -> {
            item.setCheckable(true);
            if (item.getItemId() == beranda) {
                replaceFragment(fragment_beranda);
            } else if (item.getItemId() == calendar) {
                replaceFragment(fragment_calendar);
            } else if (item.getItemId() == pengaturan) {
                replaceFragment(fragment_pengaturan);
            } else {
                return false;
            }
            return true;
        });



        /**
         * hour, minute, second, requestCode
         * request kode digunakan untuk membedakan intent dari setiap alarm
         */
//        if (checkAlarm(1)) {
//            Toast.makeText(this, "", Toast.LENGTH_SHORT).show();
//            startAlarm(9, 0, 0, 1);
//        }
//
//        if (checkAlarm(2)) {
//            startAlarm(15, 0, 0, 2);
//        }
//
//        if (checkAlarm(3)) {
//            startAlarm(21, 0, 0, 3);
//        }


        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);

    }

    protected void replaceFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.fade_in_slide_down, R.anim.fade_out_slide_up);
        transaction.replace(R.id.frame_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
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

        if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Izin kamera diberikan
            } else {
                // Izin kamera ditolak
            }
        }

    }


}