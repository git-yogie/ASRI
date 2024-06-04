package com.yogie.anemiaapps.menu;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.EventDay;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.yogie.anemiaapps.R;
import com.yogie.anemiaapps.helper.UserPref;
import com.yogie.anemiaapps.helper.bottomsheet_fragment;
import com.yogie.anemiaapps.helper.pilih_waktu_input;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class calendar_activity extends AppCompatActivity {


    CalendarView calendarView;

    List<EventDay> events;
    UserPref userPref;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    ImageView status_1,status_2,status_3;
    TextView waktu_minum_1,waktu_minum_2,waktu_minum_3;

    CardView card_pagi,card_siang,card_malam;

    ScrollView scrollView;

    TextView tanggal;

    bottomsheet_fragment bottomSheet;
    pilih_waktu_input bottom_SheetPilihWaktu;
    RelativeLayout relativeLayout_tanggal;
    Button inputData;
    String tanggal_dipilih;

    @Override
    protected void onResume() {

        super.onResume();
        CollectionReference ref = db.collection("users").document(userPref.getUserData().get("documentId").toString()).collection("minum_obat");
        ref.get().addOnSuccessListener(queryDocumentSnapshots -> {

            for (QueryDocumentSnapshot document : queryDocumentSnapshots){

                Long tgl = document.getLong("created_at");
                setEvent(tgl,R.drawable.check_solid);

            }
            calendarView.setEvents(events);
            Log.d("data-obat",events.size() + " ");
        });


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if(getSupportActionBar() != null){
            getSupportActionBar().setTitle("Kalender Minum Obat");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }else{
            Log.d("toolbar","null");
        }

        tanggal = findViewById(R.id.tv_tanggal);

        calendarView = findViewById(R.id.calendarView);
        status_1 = findViewById(R.id.check_1);
        waktu_minum_1 = findViewById(R.id.waktu_minum_1);

        status_2 = findViewById(R.id.check_2);
        waktu_minum_2 = findViewById(R.id.waktu_minum_2);

        status_3 = findViewById(R.id.check_3);
        waktu_minum_3 = findViewById(R.id.waktu_minum_3);

        card_pagi = findViewById(R.id.cardView_pagi);
        card_siang = findViewById(R.id.cardView_siang);
        card_malam = findViewById(R.id.cardView_malam);

        scrollView = findViewById(R.id.scrollView_butki);
        scrollView.setVisibility(ScrollView.GONE);

        relativeLayout_tanggal = findViewById(R.id.linearlayout_tanggal);
        relativeLayout_tanggal.setVisibility(RelativeLayout.GONE);
        inputData = findViewById(R.id.inputData);
        inputData.setOnClickListener(v -> {
            showBottomSheetPilihWaktu(tanggal_dipilih);
        });

        events = new ArrayList<>();


        Snackbar.make(calendarView, "Silahkan pilih tanggal untuk melihat catatan", Snackbar.LENGTH_SHORT).show();

        calendarView.setOnDayClickListener(eventDay -> {

            this.tanggal_dipilih = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(eventDay.getCalendar().getTime());
            this.tanggal.setText(tanggal_dipilih);

            checkDb(tanggal_dipilih);
        });

        db = FirebaseFirestore.getInstance();
        userPref = new UserPref(this);

        CollectionReference ref = db.collection("users").document(userPref.getUserData().get("documentId").toString()).collection("minum_obat");
        ref.get().addOnSuccessListener(queryDocumentSnapshots -> {

            for (QueryDocumentSnapshot document : queryDocumentSnapshots){

                Long tgl = document.getLong("created_at");
                setEvent(tgl,R.drawable.check_solid);

            }
            calendarView.setEvents(events);
            Log.d("data-obat",events.size() + " ");
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    public boolean onSupportNavigateUp() {
        finishAfterTransition();
        return true;
    }

    private void setEvent(Long tgl,int Icon){
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        try {
            Calendar calendar = Calendar.getInstance();
//            calendar.setTime(Objects.requireNonNull(sdf.parse(tgl)));
            calendar.setTimeInMillis(tgl);
            EventDay eventDay = new EventDay(calendar, Icon);

            events.add(eventDay);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void checkDb(String tanggal){
        reset();
        relativeLayout_tanggal.setVisibility(RelativeLayout.VISIBLE);
        CollectionReference ref = db.collection("users").document(userPref.getUserData().get("documentId").toString()).collection("minum_obat");

        Query query = ref.whereEqualTo("created_at_day",tanggal);
        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
            if(queryDocumentSnapshots.isEmpty()){
                Snackbar.make(calendarView, "Tidak ada data pada tanggal "+tanggal, Snackbar.LENGTH_SHORT).show();
                scrollView.setVisibility(ScrollView.GONE);
            } else {
                Snackbar.make(calendarView, "Menampilkan data tanggal "+tanggal, Snackbar.LENGTH_SHORT).show();
                scrollView.setVisibility(ScrollView.VISIBLE);
                for (QueryDocumentSnapshot document : queryDocumentSnapshots){
                    String jadwal = document.getString("jadwal");
                    String waktu = document.getString("created_at_time");

                    if(jadwal.equals("pagi")){
                        cardPagi(waktu,document);
                    } else if(jadwal.equals("siang")){
                        cardSiang(waktu,document);
                    } else if(jadwal.equals("malam")) {
                        cardMalam(waktu,document);
                    }
                }
            }
        });
    }




    @SuppressLint("UseCompatLoadingForDrawables")

    private void reset(){
        status_1 = findViewById(R.id.check_1);
        status_1.setImageDrawable(getDrawable(R.drawable.hitung_imt));
        waktu_minum_1 = findViewById(R.id.waktu_minum_1);
        waktu_minum_1.setText("dilewatkan");

        status_2 = findViewById(R.id.check_2);
        status_2.setImageDrawable(getDrawable(R.drawable.hitung_imt));
        waktu_minum_2 = findViewById(R.id.waktu_minum_2);
        waktu_minum_2.setText("dilewatkan");

        status_3 = findViewById(R.id.check_3);
        status_3.setImageDrawable(getDrawable(R.drawable.hitung_imt));
        waktu_minum_3 = findViewById(R.id.waktu_minum_3);
        waktu_minum_3.setText("dilewatkan");

        relativeLayout_tanggal.setVisibility(RelativeLayout.GONE);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void cardPagi(String waktu, DocumentSnapshot document){

       status_1 = findViewById(R.id.check_1);
       status_1.setImageDrawable(getDrawable(R.drawable.resource_true));
       waktu_minum_1 = findViewById(R.id.waktu_minum_1);
       waktu_minum_1.setText(waktu);

       card_pagi.setOnClickListener(v -> {
           showBottomSheet(document,"pagi");
       });

    }

    private void cardSiang(String waktu, DocumentSnapshot document){
        status_2 = findViewById(R.id.check_2);
        status_2.setImageDrawable(getDrawable(R.drawable.resource_true));
        waktu_minum_2 = findViewById(R.id.waktu_minum_2);
        waktu_minum_2.setText(waktu);

        card_siang.setOnClickListener(v -> {
           showBottomSheet(document,"siang");
        });

    }

    private void cardMalam(String waktu, DocumentSnapshot document){
        status_3 = findViewById(R.id.check_3);
        status_3.setImageDrawable(getDrawable(R.drawable.resource_true));
        waktu_minum_3 = findViewById(R.id.waktu_minum_3);
        waktu_minum_3.setText(waktu);

        card_malam.setOnClickListener(v -> {
            showBottomSheet(document,"malam");
        });
    }

    private void showBottomSheet(DocumentSnapshot document,String jadwal){
        bottomSheet = new bottomsheet_fragment(document,userPref.getUserData().get("documentId").toString(),"pagi");
        bottomSheet.show(getSupportFragmentManager(),"bottomSheet");
    }

    private void showBottomSheetPilihWaktu(String tanggal){
        bottom_SheetPilihWaktu = new pilih_waktu_input(this,tanggal);
        bottom_SheetPilihWaktu.show(getSupportFragmentManager(),"bottomSheetPilihWaktu");
    }



}